package org.xwoot;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiExtendedId;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xmlrpc.model.XWikiPageHistorySummary;

/**
 * XWootContentProvider. This class is the implementation of the XWiki interface for handling modifications and
 * performing changes. For more details see {@link http://concerto.xwiki.com/xwiki/bin/view/Main/APIChat281108}
 * 
 * @version $Id$
 */
public class XWootContentProvider implements XWootContentProviderInterface
{
    final Logger logger = LoggerFactory.getLogger(XWootContentProvider.class);

    private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DB_PROTOCOL = "jdbc:derby:";

    private static final String DB_NAME = "DB";

    /**
     * Number of modification results requested for each XMLRPC call, in order to avoid server overload.
     */
    private static final int MODIFICATION_RESULTS_PER_CALL = 25;

    private XWikiXmlRpcClient rpc;

    private Connection connection;

    private String endpoint;

    /**
     * Constructor.
     * 
     * @param endpoint The target XWiki XMLRPC endpoint URL.
     * @throws XWootContentProviderException
     */
    public XWootContentProvider(String endpoint) throws XWootContentProviderException
    {
        this(endpoint, DB_NAME, false);
    }

    public XWootContentProvider(String endpoint, String dbName) throws XWootContentProviderException
    {
        this(endpoint, dbName, false);
    }

    public XWootContentProvider(String endpoint, boolean recreateDB) throws XWootContentProviderException
    {
        this(endpoint, DB_NAME, recreateDB);
    }

    /**
     * Constructor.
     * 
     * @param endpoint The target XWiki XMLRPC endpoint URL.
     * @param createDB If true the modifications DB is recreated (removing the previous one if it existed)
     * @throws XWootContentProviderException
     */
    public XWootContentProvider(String endpoint, String dbName, boolean recreateDB)
        throws XWootContentProviderException
    {
        try {
            rpc = null;
            this.endpoint = endpoint;
            init(dbName, recreateDB);
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Initialize the SQL DB for keeping track of the modification history.
     * 
     * @param createDB If true the modifications DB is recreated (removing the previous one if it existed)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void init(String dbName, boolean createDB) throws InstantiationException, IllegalAccessException,
        ClassNotFoundException, SQLException
    {
        Class.forName(DB_DRIVER).newInstance();
        connection = DriverManager.getConnection(String.format("%s%s;create=true", DB_PROTOCOL, dbName));
        connection.setAutoCommit(true);

        Statement s = connection.createStatement();

        if (createDB) {
            logger.info("Recreating modifications table.");
            try {
                s.execute("DROP TABLE modifications");
            } catch (SQLException e) {
                /* Table doesn't exist */
                if (e.getErrorCode() != 30000) {
                    throw e;
                }
            }
        }

        try {
            /*
             * Note: Here we set a UNIQUE(pageId, timestamp) constraint. However the the resolution of a page
             * modification date is about the order of the seconds. So if a client stores several time the same page one
             * after another on a very fast connection (e.g., on a local server) in less than a second, we could end up
             * with duplicates because these pages will have the same timestamp. In a real scenarion this should almost
             * never happen.
             */
            s
                .execute("CREATE TABLE modifications (pageId VARCHAR(64), timestamp BIGINT, version INT, minorVersion INT, cleared SMALLINT DEFAULT 0, PRIMARY KEY(pageId, timestamp, version, minorVersion), UNIQUE(pageId, timestamp))");

            logger.info("Modifications table created.");
        } catch (SQLException e) {
            /* Table already exists */
            if (e.getErrorCode() != 30000) {
                throw e;
            }

            logger.info("Modifications table already exists.");
        }

        s.close();
    }

    /**
     * Dispose the XWootContentManager and close all DB connections. This method has to be called before that the
     * application exits in order to keep things in a clean state.
     * 
     * @throws SQLException
     */
    public void dispose()
    {
        try {
            if (rpc != null) {
                logout();
            }

            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            /* Ignore */
        }
    }

    /**
     * Login to the remote XWiki.
     * 
     * @param username
     * @param password
     * @throws XWootContentProviderException
     * @throws XmlRpcException
     * @throws MalformedURLException
     */
    public void login(String username, String password) throws XWootContentProviderException
    {
        try {
            rpc = new XWikiXmlRpcClient(endpoint);
            rpc.login(username, password);
        } catch (Exception e) {
            rpc = null;
            throw new XWootContentProviderException(e);
        }

    }

    /**
     * Logout from the remote XWiki.
     * 
     * @throws XmlRpcException
     */
    public void logout()
    {
        try {
            rpc.logout();
        } catch (XmlRpcException e) {
            logger.warn("Exception while logging out");
        }

        rpc = null;
    }

    /**
     * Retrieves the last modifications starting from the last timestamp seen (the max) and updates the modifications
     * table. This version selects only the pages with the highest version number. For example if we receive A(4.1),
     * A(5.1), A(6.1), only A(6.1) is inserted in the DB.
     * 
     * @throws XWootContentProviderException
     */
    private void updateModifiedPagesOptimized() throws XWootContentProviderException
    {
        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT MAX(timestamp) FROM modifications");

            long maxTimestamp = 0;
            if (rs.next()) {
                maxTimestamp = rs.getLong(1);
            }

            s.close();

            PreparedStatement ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 0)");

            int entriesReceived = 0;
            int duplicatedEntries = 0;
            int start = 0;

            int ignored = 0;
            Set<String> ignoredPages = new HashSet<String>();
            Map<String, XWootId> xwootIdsToBeInserted = new HashMap<String, XWootId>();

            while (true) {
                List<XWikiPageHistorySummary> xphsList =
                    rpc.getModifiedPagesHistory(new Date(maxTimestamp), MODIFICATION_RESULTS_PER_CALL, start, true);

                for (XWikiPageHistorySummary xphs : xphsList) {
                    if (!XWootContentProviderConfiguration.getDefault().isIgnored(xphs.getId())) {
                        XWootId xwootId =
                            new XWootId(xphs.getBasePageId(), xphs.getModified().getTime(), xphs.getVersion(), xphs
                                .getMinorVersion());

                        /* Get the current xwootId in the map */
                        XWootId currentXWootId = xwootIdsToBeInserted.get(xwootId.getPageId());
                        if (currentXWootId == null) {
                            /* If it doesn't exist then put the received xwootId in the map */
                            xwootIdsToBeInserted.put(xwootId.getPageId(), xwootId);
                        } else {
                            /*
                             * Check for the version. If the received xwootId references a more recent page then replace
                             * the xwootId in the map
                             */
                            if (xwootId.getVersion() > currentXWootId.getVersion()) {
                                xwootIdsToBeInserted.put(xwootId.getPageId(), xwootId);
                            } else if (xwootId.getMinorVersion() > currentXWootId.getMinorVersion()) {
                                xwootIdsToBeInserted.put(xwootId.getPageId(), xwootId);
                            }
                        }

                        entriesReceived++;
                    } else {
                        ignored++;
                        ignoredPages.add(xphs.getId());
                    }
                }

                if (xphsList.size() < MODIFICATION_RESULTS_PER_CALL) {
                    break;
                }

                start = start + MODIFICATION_RESULTS_PER_CALL;
            }

            for (XWootId xwootId : xwootIdsToBeInserted.values()) {
                ps.setString(1, xwootId.getPageId());
                ps.setLong(2, xwootId.getTimestamp());
                ps.setInt(3, xwootId.getVersion());
                ps.setInt(4, xwootId.getMinorVersion());

                try {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    /* Ignore duplicated entries that we receive */
                    if (e.getErrorCode() != 30000) {
                        throw e;
                    }
                    duplicatedEntries++;
                }
            }

            logger.info(String.format(
                "Modifcations list updated. Received %d entries starting from %s (%d). %d duplicates.",
                entriesReceived, new Date(maxTimestamp), maxTimestamp, duplicatedEntries));
            logger.info(String.format("Modifcations list updated. Ignored %d entried: %s\n", ignored, ignoredPages));

            ps.close();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }

    }

    /**
     * Retrieves the last modifications starting from the last timestamp seen (the max) and updates the modifications
     * table.
     * 
     * @throws XWootContentProviderException
     */
    private void updateModifiedPages() throws XWootContentProviderException
    {
        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT MAX(timestamp) FROM modifications");

            long maxTimestamp = 0;
            if (rs.next()) {
                maxTimestamp = rs.getLong(1);
            }

            s.close();

            PreparedStatement ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 0)");

            int entriesReceived = 0;
            int duplicatedEntries = 0;
            int start = 0;

            int ignored = 0;
            Set<String> ignoredPages = new HashSet<String>();

            while (true) {
                List<XWikiPageHistorySummary> xphsList =
                    rpc.getModifiedPagesHistory(new Date(maxTimestamp), MODIFICATION_RESULTS_PER_CALL, start, true);

                for (XWikiPageHistorySummary xphs : xphsList) {
                    if (!XWootContentProviderConfiguration.getDefault().isIgnored(xphs.getId())) {
                        ps.setString(1, xphs.getId());
                        ps.setLong(2, xphs.getModified().getTime());
                        ps.setInt(3, xphs.getVersion());
                        ps.setInt(4, xphs.getMinorVersion());

                        try {
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            /* Ignore duplicated entries that we receive */
                            if (e.getErrorCode() != 30000) {
                                throw e;
                            }

                            duplicatedEntries++;
                        }

                        entriesReceived++;
                    } else {
                        ignored++;
                        ignoredPages.add(xphs.getId());
                    }
                }

                if (xphsList.size() < MODIFICATION_RESULTS_PER_CALL) {
                    break;
                }

                start = start + MODIFICATION_RESULTS_PER_CALL;
            }

            logger.info(String.format(
                "Modifcations list updated. Received %d entries starting from %s (%d). %d duplicates.",
                entriesReceived, new Date(maxTimestamp), maxTimestamp, duplicatedEntries));
            logger.info(String.format("Modifcations list updated. Ignored %d entried: %s\n", ignored, ignoredPages));

            ps.close();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }

    }

    /**
     * Returns a list of references where each reference points to a different page at its oldest modification available
     * in the modification list that has not been cleared.
     * 
     * @return A set of XWootIds.
     * @throws XWootContentProviderException
     */
    public Set<XWootId> getModifiedPagesIds() throws XWootContentProviderException
    {
        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        Set<XWootId> result = new TreeSet<XWootId>(new Comparator<XWootId>()
        {
            public int compare(XWootId arg0, XWootId arg1)
            {
                return (int) (arg0.getTimestamp() - arg1.getTimestamp());
            }

        });

        /* Download last modifications from the server */
        updateModifiedPages();

        try {
            PreparedStatement ps =
                connection
                    .prepareStatement("SELECT pageId, MIN(timestamp) FROM modifications WHERE cleared=? GROUP BY pageId");
            ps.setInt(1, 0);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                XWootId xwootId = getXWootId(rs.getString(1), rs.getLong(2));

                if (xwootId != null) {
                    result.add(xwootId);
                } else {
                    logger.warn(String
                        .format("Unable to retrieve XWootId for (%s, %d)", rs.getString(1), rs.getLong(2)));
                }
            }

            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XWootContentProviderException(e);
        }

        return result;
    }

    /**
     * Retrieve all the information for building an XWootId. From the (pageId, timestamp) pair retrieve the associated
     * version number.
     * 
     * @param pageId
     * @param timestamp
     * @return
     */
    private XWootId getXWootId(String pageId, long timestamp)
    {
        XWootId result = null;
        try {
            PreparedStatement ps =
                connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? AND timestamp=?");
            ps.setString(1, pageId);
            ps.setLong(2, timestamp);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            }

            ps.close();
        } catch (SQLException e) {
        }

        return result;
    }

    /**
     * Set the "cleared" flag of the modification related to the id passed as parameter. This means that the
     * modification has been processed and should not be returned in subsequent calls.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    public void clearModification(XWootId xwootId) throws XWootContentProviderException
    {
        try {
            if (xwootId.getPageId().equals("test.1")) {
                dumpDbLines("* CLEAR MODIFICATIONS test.1");
            }

            PreparedStatement ps =
                connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp=?");
            ps.setString(1, xwootId.getPageId());
            ps.setLong(2, xwootId.getTimestamp());

            int rowsUpdated = ps.executeUpdate();

            logger.info(String.format("%s at %s (%d) cleared. %d rows updated", xwootId.getPageId(), new Date(xwootId
                .getTimestamp()), xwootId.getTimestamp(), rowsUpdated));

            ps.close();

            if (xwootId.getPageId().equals("test.1")) {
                dumpDbLines("test.1");
            }
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Set the "cleared" flag of all the modifications up to the one (included) related to the id passed as parameter.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    public void clearAllModifications(XWootId xwootId) throws XWootContentProviderException
    {
        try {
            PreparedStatement ps =
                connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp<=?");
            ps.setString(1, xwootId.getPageId());
            ps.setLong(2, xwootId.getTimestamp());

            int rowsUpdated = ps.executeUpdate();

            logger.info(String.format("%s at %s (%d) and before cleared. %d rows updated", xwootId.getPageId(),
                new Date(xwootId.getTimestamp()), xwootId.getTimestamp(), rowsUpdated));

            ps.close();

        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Clear all the modifications. Useful for testing purpose.
     * 
     * @throws XWootContentProviderException
     */
    public void clearAllModifications() throws XWootContentProviderException
    {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE modifications SET cleared=1");

            int rowsUpdated = ps.executeUpdate();

            logger.info(String.format("Cleared all modifications. %d rows updated", rowsUpdated));

            ps.close();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Find the previous modification with respect to the one identified by the XWootId passed as parameters.
     * 
     * @param xwootId
     * @return The XWootId of the previous modification in temporal order. null if it doesn't exist.
     * @throws XWootContentProviderException
     */
    private XWootId getPreviousModification(XWootId xwootId) throws XWootContentProviderException
    {
        try {
            XWootId result = null;
            PreparedStatement ps =
                connection
                    .prepareStatement("SELECT * FROM modifications WHERE pageId=? AND timestamp < ? ORDER BY timestamp DESC");
            ps.setString(1, xwootId.getPageId());
            ps.setLong(2, xwootId.getTimestamp());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                result = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            }

            return result;
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Returns a list of XWootObjects that contains all the entities that have been modified in the page identified by
     * the the XWootId (i.e., at a given timestamp). The contract here is that each XWootObject in the list will contain
     * only the fields that have been modified (i.e., a subset of the fields that actually make up the underlying
     * object). If the entity didn't exist in the previous version, then all the fields are present in the corresponding
     * XWootObject. Here we return only XWootObjects since we decided to process in a uniform way XWikiPages and
     * XWikiObjects, since they both can be seen as a collection of pairs name=value.
     * 
     * @param xwootId
     * @return
     * @throws XWootContentProviderException
     */
    public List<XWootObject> getModifiedEntities(XWootId xwootId) throws XWootContentProviderException
    {
        try {
            List<XWootObject> result = new ArrayList<XWootObject>();

            XWootId previousModification = getPreviousModification(xwootId);

            /* Main page */
            if (previousModification == null) {
                XWikiPage page = rpc.getPage(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                XWootObject object = Utils.xwikiPageToXWootObject(page, true);
                result.add(object);

                List<XWikiObjectSummary> xwikiObjectSummaries =
                    rpc.getObjects(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                for (XWikiObjectSummary xwikiObjectSummary : xwikiObjectSummaries) {
                    /* In order to get an object with a guid at a given version we need to use XWiki Extended Ids */
                    XWikiExtendedId extendedId = new XWikiExtendedId(xwootId.getPageId());
                    extendedId.setParameter(XWikiExtendedId.VERSION_PARAMETER, String.format("%d",
                        xwootId.getVersion()));
                    extendedId.setParameter(XWikiExtendedId.MINOR_VERSION_PARAMETER, String.format("%d",
                        xwootId.getMinorVersion()));                    
                    
                    XWikiObject xwikiObject =
                        rpc.getObject(extendedId.toString(), xwikiObjectSummary.getGuid());
                    object = Utils.xwikiObjectToXWootObject(xwikiObject, true);
                    result.add(object);
                }
            } else {
                XWikiPage page = rpc.getPage(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                XWikiPage previousPage =
                    rpc.getPage(previousModification.getPageId(), previousModification.getVersion(),
                        previousModification.getMinorVersion());

                XWootObject currentPageObject = Utils.xwikiPageToXWootObject(page, false);
                XWootObject previousPageObject = Utils.xwikiPageToXWootObject(previousPage, false);

                XWootObject cleanedUpXWootObject = Utils.removeUnchangedFields(currentPageObject, previousPageObject);

                if (cleanedUpXWootObject.getFields().size() > 0) {
                    result.add(cleanedUpXWootObject);
                }

                List<XWikiObjectSummary> xwikiObjectSummaries =
                    rpc.getObjects(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                for (XWikiObjectSummary xwikiObjectSummary : xwikiObjectSummaries) {
                    /* In order to get an object with a guid at a given version we need to use XWiki Extended Ids */
                    XWikiExtendedId extendedId = new XWikiExtendedId(xwootId.getPageId());
                    extendedId.setParameter(XWikiExtendedId.VERSION_PARAMETER, String
                        .format("%d", xwootId.getVersion()));
                    extendedId.setParameter(XWikiExtendedId.MINOR_VERSION_PARAMETER, String.format("%d", xwootId
                        .getMinorVersion()));

                    XWikiObject xwikiObject = rpc.getObject(extendedId.toString(), xwikiObjectSummary.getGuid());

                    XWikiObject previousXWikiObject = null;

                    /*
                     * This is ugly because we cannot understand when there has been a network problem or the object
                     * doesn't exist.
                     */
                    try {
                        /* In order to get an object with a guid at a given version we need to use XWiki Extended Ids */
                        extendedId = new XWikiExtendedId(previousModification.getPageId());
                        extendedId.setParameter(XWikiExtendedId.VERSION_PARAMETER, String.format("%d",
                            previousModification.getVersion()));
                        extendedId.setParameter(XWikiExtendedId.MINOR_VERSION_PARAMETER, String.format("%d",
                            previousModification.getMinorVersion()));

                        previousXWikiObject = rpc.getObject(extendedId.toString(), xwikiObjectSummary.getGuid());
                    } catch (Exception e) {
                    }

                    if (previousXWikiObject != null) {
                        XWootObject currentXWootObject = Utils.xwikiObjectToXWootObject(xwikiObject, false);
                        XWootObject previousXWootObject = Utils.xwikiObjectToXWootObject(previousXWikiObject, false);

                        cleanedUpXWootObject = Utils.removeUnchangedFields(currentXWootObject, previousXWootObject);

                        if (cleanedUpXWootObject.getFields().size() > 0) {
                            result.add(cleanedUpXWootObject);
                        }
                    } else {
                        result.add(Utils.xwikiObjectToXWootObject(xwikiObject, true));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }

    }

    /**
     * Updates xwiki's data.
     * 
     * @param object : the object to update
     * @return true if no concurrent modification detected.
     * @throws XWootContentProviderException
     */
    public boolean store(XWootObject object) throws XWootContentProviderException
    {
        String namespace = object.getGuid().split(":")[0];

        if (namespace.equals(Constants.PAGE_NAMESPACE)) {
            return storeXWikiPage(object);
        } else if (namespace.equals(Constants.OBJECT_NAMESPACE)) {
            return storeXWikiObject(object);
        }

        throw new IllegalArgumentException(String.format("Invalid namespace %s\n", namespace));
    }

    private boolean storeXWikiObject(XWootObject object)
    {
        try {
            XWikiObject xwikiObject = Utils.xwootObjectToXWikiObject(object);
            xwikiObject = rpc.storeObject(xwikiObject, true);

            /* If an empty object is returned then the store failed */
            if (xwikiObject.getPageId().equals("")) {
                return false;
            }

            /* Retrieve the page this object was stored to in order to get additional information like the timestamp. */
            XWikiPage xwikiPage =
                rpc.getPage(xwikiObject.getPageId(), xwikiObject.getPageVersion(), xwikiObject.getPageMinorVersion());

            /* Mark this page as cleared so the next time it will not be returned in the modification list */
            PreparedStatement ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 1)");

            XWikiExtendedId extendedId = new XWikiExtendedId(xwikiPage.getId());

            ps.setString(1, extendedId.getBasePageId());
            ps.setLong(2, xwikiPage.getModified().getTime());
            ps.setInt(3, xwikiPage.getVersion());
            ps.setInt(4, xwikiPage.getMinorVersion());
            ps.executeUpdate();

            ps.close();
        } catch (Exception e) {
            // throw new XWootContentProviderException(e);
            return false;
        }

        return true;
    }

    private boolean storeXWikiPage(XWootObject object) throws XWootContentProviderException
    {
        try {
            XWikiPage page = Utils.xwootObjectToXWikiPage(object);
            page = rpc.storePage(page, true);

            /* If an empty page is returned then the store failed */
            if (page.getId().equals("")) {
                return false;
            }

            /* Mark this page as cleared so the next time it will not be returned in the modification list */
            PreparedStatement ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 1)");

            ps.setString(1, page.getId());
            ps.setLong(2, page.getModified().getTime());
            ps.setInt(3, page.getVersion());
            ps.setInt(4, page.getMinorVersion());
            ps.executeUpdate();

            ps.close();
        } catch (Exception e) {
            // throw new XWootContentProviderException(e);
            return false;
        }

        return true;
    }

    /**
     * For testing purposes.
     * 
     * @return
     */
    public XWikiXmlRpcClient getRpc()
    {
        return rpc;
    }

    public List<XWootId> getXWootIdsFor(String pageId) throws SQLException
    {
        List<XWootId> result = new ArrayList<XWootId>();

        PreparedStatement ps =
            connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? ORDER by timestamp DESC");
        ps.setString(1, pageId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            XWootId xwootId = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            result.add(xwootId);
        }

        return result;
    }

    public void dumpDbLines(String pageId)
    {
        try {
            System.out.format("PageId: %s\n", pageId);
            System.out.format("-----------------------------------------\n");
            PreparedStatement ps =
                connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? ORDER by timestamp DESC");
            ps.setString(1, pageId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.format("%-30s\t| %d\t| %d.%d\t| %d\n", rs.getString(1), rs.getLong(2), rs.getInt(3), rs
                    .getInt(4), rs.getShort(5));
            }

            ps.close();
            System.out.format("-----------------------------------------\n");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void dumpDbLines(String message, int n)
    {

        try {
            System.out.format("%s\n", message);
            System.out.format("-----------------------------------------\n");
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM modifications ORDER by timestamp DESC");

            int i = 0;
            while (rs.next()) {
                System.out.format("%-30s\t| %d\t| %d.%d\t| %d\n", rs.getString(1), rs.getLong(2), rs.getInt(3), rs
                    .getInt(4), rs.getShort(5));
                i++;
                if (i >= n) {
                    break;
                }
            }

            s.close();
            System.out.format("-----------------------------------------\n");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
