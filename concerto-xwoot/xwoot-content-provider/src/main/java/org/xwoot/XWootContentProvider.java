package org.xwoot;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xmlrpc.model.XWikiPageHistorySummary;

/**
 * XWootContentProvider. This class is the implementation of the XWiki interface for handling modifications and
 * performing changes. For more details see {@link http://concerto.xwiki.com/xwiki/bin/view/Main/APIChat281108}
 * 
 * @version $Id$
 */
public class XWootContentProvider
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
        this(endpoint, false);
    }

    /**
     * Constructor.
     * 
     * @param endpoint The target XWiki XMLRPC endpoint URL.
     * @param createDB If true the modifications DB is recreated (removing the previous one if it existed)
     * @throws XWootContentProviderException
     */
    public XWootContentProvider(String endpoint, boolean recreateDB) throws XWootContentProviderException
    {
        try {
            rpc = null;
            this.endpoint = endpoint;
            init(recreateDB);
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
    private void init(boolean createDB) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
        SQLException
    {
        Class.forName(DB_DRIVER).newInstance();
        connection = DriverManager.getConnection(String.format("%s%s;create=true", DB_PROTOCOL, DB_NAME));
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

            while (true) {
                List<XWikiPageHistorySummary> xphsList =
                    rpc.getModifiedPagesHistory(new Date(maxTimestamp), MODIFICATION_RESULTS_PER_CALL, start, true);

                for (XWikiPageHistorySummary xphs : xphsList) {
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
                }

                if (xphsList.size() < MODIFICATION_RESULTS_PER_CALL) {
                    break;
                }

                start = start + MODIFICATION_RESULTS_PER_CALL;
            }

            logger.info(String.format(
                "Modifcations list updated. Received %d entries starting from %s (%d). %d duplicates.",
                entriesReceived, new Date(maxTimestamp), maxTimestamp, duplicatedEntries));

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

        Set<XWootId> result = new TreeSet<XWootId>();

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
            PreparedStatement ps =
                connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp=?");
            ps.setString(1, xwootId.getPageId());
            ps.setLong(2, xwootId.getTimestamp());

            int rowsUpdated = ps.executeUpdate();

            logger.info(String.format("%s at %s (%d) cleared. %d rows updated", xwootId.getPageId(), new Date(xwootId
                .getTimestamp()), xwootId.getTimestamp(), rowsUpdated));

            ps.close();
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
                    .prepareStatement("SELECT * FROM modifications WHERE pageId=? AND timestamp < ? ORDER BY timestamp ASC");
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
            } else {
                XWikiPage page = rpc.getPage(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                XWikiPage previousPage =
                    rpc.getPage(previousModification.getPageId(), previousModification.getVersion(),
                        previousModification.getMinorVersion());

                XWootObject currentPageObject = Utils.xwikiPageToXWootObject(page, false);
                XWootObject previousPageObject = Utils.xwikiPageToXWootObject(previousPage, false);

                result.add(Utils.removeUnchangedFields(currentPageObject, previousPageObject));
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
     */
    public boolean store(XWootObject object)
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
        // TODO Auto-generated method stub
        return false;
    }

    private boolean storeXWikiPage(XWootObject object)
    {

        return false;
    }

}
