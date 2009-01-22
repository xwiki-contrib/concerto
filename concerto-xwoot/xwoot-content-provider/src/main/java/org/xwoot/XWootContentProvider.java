package org.xwoot;

import java.net.MalformedURLException;
import java.net.URL;
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
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
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
    final Log logger = LogFactory.getLog(XWootContentProvider.class);

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

    private XWootContentProviderConfiguration configuration;

    /**
     * Constructor.
     * 
     * @param endpoint The target XWiki XMLRPC endpoint URL.
     * @throws XWootContentProviderException
     */
    public XWootContentProvider(String endpoint, Properties configurationProperties)
        throws XWootContentProviderException
    {
        this(endpoint, DB_NAME, false, configurationProperties);
    }

    public XWootContentProvider(String endpoint, String dbName, Properties configurationProperties)
        throws XWootContentProviderException
    {
        this(endpoint, dbName, false, configurationProperties);
    }

    public XWootContentProvider(String endpoint, boolean recreateDB, Properties configurationProperties)
        throws XWootContentProviderException
    {
        this(endpoint, DB_NAME, recreateDB, configurationProperties);
    }

    /**
     * Constructor.
     * 
     * @param endpoint The target XWiki XMLRPC endpoint URL.
     * @param createDB If true the modifications DB is recreated (removing the previous one if it existed)
     * @throws XWootContentProviderException
     */
    public XWootContentProvider(String endpoint, String dbName, boolean recreateDB, Properties configurationProperties)
        throws XWootContentProviderException
    {
        try {
            rpc = null;
            this.endpoint = endpoint;
            configuration = new XWootContentProviderConfiguration(configurationProperties);
            init(dbName, recreateDB);
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }

        logger.info("Initialization done.");

        URL configurationFileUrl = configuration.getConfigurationFileUrl();

        logger.info(String.format("Configured from: %s", configurationFileUrl != null ? configurationFileUrl
            : "User provided properties."));
        logger.info(String.format("Ignore patterns: %s", configuration.getIgnorePatterns()));
        logger.info(String.format("Cumulative classes: %s", configuration.getCumulativeClasses()));
        logger.info(String.format("Wootable properties: %s", configuration.getWootablePropertiesMap()));
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
             * Note: Here we set a UNIQUE(pageId, timestamp) constraint. However the resolution of a page modification
             * date is about the order of the seconds. So if a client stores several times the same page, one after
             * another on a very fast connection (e.g., on a local server) and in less than a second, we could end up
             * with duplicates because these pages will have the same timestamps. In a real scenario this should almost
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
     * Set the cleared flag for all modification that doesn't match with the provided timestamp.
     * 
     * @param pageId The pageId the modification should refer to.
     * @param timestamp The timestamp to be retained.
     * @throws XWootContentProviderException
     */
    private void clearModificationsIfTimestampDoesntMatch(String pageId, long timestamp)
        throws XWootContentProviderException
    {
        try {
            PreparedStatement ps =
                connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp<>?");
            ps.setString(1, pageId);
            ps.setLong(2, timestamp);

            int rowsUpdated = ps.executeUpdate();

            logger.info(String.format(
                "Cleared all pages '%s' with timestamp different from at %s (%d). %d rows updated", pageId, new Date(
                    timestamp), timestamp, rowsUpdated));

            ps.close();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Retrieves the last modifications starting from the last timestamp seen (the max) and updates the modifications
     * table. This optimized version clears, for each received page, all the modifications except the one with the
     * greatest timestamp (i.e., the last one). So that the next call to getModificationList will return only the latest
     * change for each changed page.
     * 
     * @throws XWootContentProviderException
     */
    private void updateModifiedPages(boolean clearAllExceptLatestVersions) throws XWootContentProviderException
    {
        /*
         * This map contains, for each received page change, the greatest timestamp seen, i.e., the timestamp of the
         * latest modification. It is used to clear previous modifications from the list and to leave only the last one
         * (i.e., the one with the greatest timestamp)
         */
        Map<String, Long> pageIdToGreatestTimestampMap = new HashMap<String, Long>();

        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        try {
            /*
             * Retrieve the timestamp of the last modification received. We will ask the server to give us further
             * modifications starting from that timestamp
             */
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT MAX(timestamp) FROM modifications");

            long maxTimestamp = 0;
            if (rs.next()) {
                maxTimestamp = rs.getLong(1);
            }

            s.close();

            logger.info(String.format("Requesting modifications list starting from timestamp %d (%s)", maxTimestamp,
                new Date(maxTimestamp)));

            /* Build a prepared statement for insertion */
            PreparedStatement ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 0)");

            int entriesReceived = 0;
            int duplicatedEntries = 0;
            int start = 0;

            int ignored = 0;
            Set<String> ignoredPages = new HashSet<String>();

            /* Start fetching modifications */
            while (true) {
                List<XWikiPageHistorySummary> xphsList =
                    rpc.getModifiedPagesHistory(new Date(maxTimestamp), MODIFICATION_RESULTS_PER_CALL, start, true);

                for (XWikiPageHistorySummary xphs : xphsList) {
                    /* Check if the page concerning the modification is ignored */
                    if (!configuration.isIgnored(xphs.getId())) {
                        ps.setString(1, xphs.getId());
                        ps.setLong(2, xphs.getModified().getTime());
                        ps.setInt(3, xphs.getVersion());
                        ps.setInt(4, xphs.getMinorVersion());

                        try {
                            ps.executeUpdate();

                            /* Update the greatest timestamp seen for the current modification */
                            Long greatestTimeStamp = pageIdToGreatestTimestampMap.get(xphs.getId());
                            if (greatestTimeStamp == null) {
                                pageIdToGreatestTimestampMap.put(xphs.getId(), xphs.getModified().getTime());
                            } else {
                                if (xphs.getModified().getTime() >= greatestTimeStamp.longValue()) {
                                    pageIdToGreatestTimestampMap.put(xphs.getId(), xphs.getModified().getTime());
                                }
                            }
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
                "Modifications list updated. Received %d entries starting from %s (%d). %d duplicates.",
                entriesReceived, new Date(maxTimestamp), maxTimestamp, duplicatedEntries));
            logger.info(String.format("Modifications list updated. Ignored %d entries.", ignored));

            ps.close();

            if (clearAllExceptLatestVersions) {
                /*
                 * Clear all the received modifications for each page, except the one with the greatest timestamp, i.e.,
                 * the most recent one.
                 */
                for (String pageId : pageIdToGreatestTimestampMap.keySet()) {
                    clearModificationsIfTimestampDoesntMatch(pageId, pageIdToGreatestTimestampMap.get(pageId));
                }
            }
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
    private void updateModifiedPagesOld() throws XWootContentProviderException
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
                    if (!configuration.isIgnored(xphs.getId())) {
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

    public Set<XWootId> getModifiedPagesIds() throws XWootContentProviderException
    {
        return getModifiedPagesIds(true);
    }

    /**
     * Returns a list of references where each reference points to a different page at its oldest modification available
     * in the modification list that has not been cleared.
     * 
     * @return A set of XWootIds.
     * @throws XWootContentProviderException
     */
    public Set<XWootId> getModifiedPagesIds(boolean clearAllExceptLatestVersions) throws XWootContentProviderException
    {
        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        logger.info("*** getModifiedPagesIds called");

        Set<XWootId> result = new TreeSet<XWootId>(new Comparator<XWootId>()
        {
            public int compare(XWootId arg0, XWootId arg1)
            {
                return (int) (arg0.getTimestamp() - arg1.getTimestamp());
            }

        });

        /* Download last modifications from the server */
        updateModifiedPages(clearAllExceptLatestVersions);

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

        logger.info(String.format("Retrieved %d modifications: %s", result.size(), result));

        logger.info("*** getModifiedPagesIds ended");

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

            logger.info(String.format("Getting modified entities for %s", xwootId));

            XWootId previousModification = getPreviousModification(xwootId);

            /* Main page */
            if (previousModification == null) {

                logger.info(String.format("No previous version exists for %s", xwootId));

                XWikiPage page = rpc.getPage(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                XWootObject object = Utils.xwikiPageToXWootObject(page, true);
                result.add(object);

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
                    object = Utils.xwikiObjectToXWootObject(xwikiObject, true, configuration);
                    result.add(object);
                }
            } else {
                logger.info(String.format("Previous version exists %s is %s", xwootId, previousModification));

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
                        XWootObject currentXWootObject =
                            Utils.xwikiObjectToXWootObject(xwikiObject, false, configuration);
                        XWootObject previousXWootObject =
                            Utils.xwikiObjectToXWootObject(previousXWikiObject, false, configuration);

                        cleanedUpXWootObject = Utils.removeUnchangedFields(currentXWootObject, previousXWootObject);

                        if (cleanedUpXWootObject.getFields().size() > 0) {
                            result.add(cleanedUpXWootObject);
                        }
                    } else {
                        result.add(Utils.xwikiObjectToXWootObject(xwikiObject, true, configuration));
                    }
                }
            }

            logger.info(System.out.format("Got modified entities: %s", result));

            return result;
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }

    }

    /**
     * Updates XWiki's data.
     * 
     * @param object : the object to update
     * @param versionAdjustement : An XWootId that contains version number information for adjusting the
     *            page-to-be-sent's version. This is useful because clients (i.e., the synchronizer) can set the
     *            "last known version number" before trying to store the page.
     * @return An XWootId containing the pageId and the new updated version of the stored page so that clients are able
     *         to know what is the version that they have stored on the server, or null if concurrent modification
     *         detected in the meanwhile.
     * @throws XWootContentProviderException
     */
    public XWootId store(XWootObject object, XWootId versionAdjustment) throws XWootContentProviderException
    {
        if (configuration.isIgnored(object.getPageId())) {
            logger.info(String.format("'%s' not stored because '%s' is on ignore list.", object.getGuid(), object
                .getPageId()));

            /* FIXME: Is it the right value to return? To be checked */
            return new XWootId(object.getPageId(), (new Date()).getTime(), object.getPageVersion(), object
                .getPageMinorVersion());
        }

        String namespace = object.getGuid().split(":")[0];

        logger.info(String.format("Storing '%s' (Associated page information: '%s', %d.%d)...", object.getGuid(),
            object.getPageId(), object.getPageVersion(), object.getPageMinorVersion()));

        if (namespace.equals(Constants.PAGE_NAMESPACE)) {
            return storeXWikiPage(object, versionAdjustment);
        } else if (namespace.equals(Constants.OBJECT_NAMESPACE)) {
            return storeXWikiObject(object, versionAdjustment);
        }

        throw new IllegalArgumentException(String.format("Invalid namespace %s\n", namespace));
    }

    private XWootId storeXWikiObject(XWootObject object, XWootId versionAdjustment)
    {
        try {
            XWikiObject xwikiObject = Utils.xwootObjectToXWikiObject(object);
            if (versionAdjustment != null) {
                xwikiObject.setPageVersion(versionAdjustment.getVersion());
                xwikiObject.setPageMinorVersion(versionAdjustment.getMinorVersion());
            }

            xwikiObject = rpc.storeObject(xwikiObject, true);

            /* If an empty object is returned then the store failed */
            if (xwikiObject.getPageId().equals("")) {
                logger.info(String.format(
                    "Server refused to store object. Associated page information: '%s' version %d.%d", object
                        .getPageId(), object.getPageVersion(), object.getPageMinorVersion()));
                return null;
            }

            /* Retrieve the page this object was stored to in order to get additional information like the timestamp. */
            XWikiPage page =
                rpc.getPage(xwikiObject.getPageId(), xwikiObject.getPageVersion(), xwikiObject.getPageMinorVersion());

            logger.info(String.format("'%s' stored. Associated page information: %s version %d.%d", object.getGuid(),
                object.getPageId(), object.getPageVersion(), object.getPageMinorVersion()));

            clearOrInsert(page.getId(), page.getModified().getTime(), page.getVersion(), page.getMinorVersion());

            return new XWootId(page.getId(), page.getModified().getTime(), page.getVersion(), page.getMinorVersion());
        } catch (Exception e) {
            logger.error(String.format("'%s' not stored due to an exception.", object.getGuid()), e);
            return null;
        }
    }

    /**
     * The store XWikiPage has the following semantics:
     * <ul>
     * <li>If the target page doesn't exist then the store succeeds</li>
     * <li>If the target page already exist then:</li>
     * <ul>
     * <li>If version adjustement is null then store fails (This prevents some cases where a page is created before that
     * the synchronization is completed. The case here is that the synchronizer doesn't have information about the
     * previous version of a page (version adjustement == null) and it tries to store a page at its first version. But
     * this version has already been created by somebody else in the meanwhile so, if the store succeeds, this
     * modification will be overwritten.</li>
     * <li>If version adjustement is not null then the page to be stored's version is set to the version provided by the
     * adjustement and the normal store with version check is performed (i.e., the page is stored iff the version of the
     * sent page matches with the version of the remote page).</li>
     * </ul>
     * </ul>
     * 
     * @param object
     * @param versionAdjustement
     * @return
     * @throws XWootContentProviderException
     */
    private XWootId storeXWikiPage(XWootObject object, XWootId versionAdjustement) throws XWootContentProviderException
    {
        try {
            XWikiPage page = Utils.xwootObjectToXWikiPage(object);
            if (versionAdjustement != null) {
                page.setVersion(versionAdjustement.getVersion());
                page.setMinorVersion(versionAdjustement.getMinorVersion());
            } else {
                /*
                 * If the version adjustement is null, we set a fake version 0.1 so that we have the following
                 * behaviour: 1) If the page doesn't exist it is created. 2) If the page exists the store fails. This is
                 * needed in order to prevent a case of page removal while synchronising.
                 */
                page.setVersion(0);
                page.setVersion(1);
            }

            page = rpc.storePage(page, true);

            /* If an empty page is returned then the store failed */
            if (page.getId().equals("")) {
                logger.info(String.format("Server refused to store page '%s' version %d.%d", page.getId(), page
                    .getVersion(), page.getMinorVersion()));
                return null;
            }

            logger.info(String.format("'%s' stored. Stored page info: '%s' version %d.%d", page.getId(), page
                .getVersion(), page.getMinorVersion()));

            clearOrInsert(page.getId(), page.getModified().getTime(), page.getVersion(), page.getMinorVersion());

            return new XWootId(page.getId(), page.getModified().getTime(), page.getVersion(), page.getMinorVersion());
        } catch (Exception e) {
            logger.error(String.format("'%s' not stored due to an exception.", object.getGuid()), e);
            return null;
        }
    }

    private void clearOrInsert(String pageId, long timestamp, int version, int minorVersion)
        throws XWootContentProviderException
    {
        try {
            /* Try to clear the line if it exists */
            PreparedStatement ps =
                connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp=?");
            ps.setString(1, pageId);
            ps.setLong(2, timestamp);

            int rowsUpdated = ps.executeUpdate();

            ps.close();

            if (rowsUpdated > 0) {
                logger.info(String.format("%s at %s (%d) cleared. %d rows updated", pageId, new Date(timestamp),
                    timestamp, rowsUpdated));
                return;
            }

            /*
             * If the entry doesn't exist then insert it and mark it as cleared so the next time it will not be returned
             * in the modification list
             */
            ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 1)");

            ps.setString(1, pageId);
            ps.setLong(2, timestamp);
            ps.setInt(3, version);
            ps.setInt(4, minorVersion);
            ps.executeUpdate();

            ps.close();

            logger.info(String.format("%s at %s (%d) inserted and cleared.", pageId, new Date(timestamp), timestamp));
        } catch (SQLException e) {
            throw new XWootContentProviderException(e);
        }
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

    /**
     * Get a list of XWootId corresponding to the entries stored in the database.
     * 
     * @param pageId
     * @return
     * @throws SQLException
     */
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

    /**
     * Get a list of XWootId corresponding to the entries stored in the database where the cleared flag is equal to the
     * passed parameter.
     * 
     * @param pageId
     * @param cleared
     * @return
     * @throws SQLException
     */
    public List<XWootId> getXWootIdsFor(String pageId, boolean cleared) throws SQLException
    {
        List<XWootId> result = new ArrayList<XWootId>();

        PreparedStatement ps =
            connection
                .prepareStatement("SELECT * FROM modifications WHERE pageId=? AND cleared=? ORDER by timestamp DESC");
        ps.setString(1, pageId);
        ps.setBoolean(2, cleared);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            XWootId xwootId = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            result.add(xwootId);
        }

        return result;
    }

    /**
     * An utility method for dumping the content of the database.
     * 
     * @param message The message to be displayed
     * @param pageId The pageId to be used in order to filter results on the pageId. null for returning all pageIds.
     * @param n The maximum number of lines to be dumped. -1 for all.
     */
    public void dumpDbLines(String message, String pageId, int n)
    {
        try {
            PreparedStatement ps = null;

            if (pageId != null) {
                ps = connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? ORDER by timestamp DESC");
                ps.setString(1, pageId);
            } else {
                ps = connection.prepareStatement("SELECT * FROM modifications ORDER by timestamp DESC");
            }

            ResultSet rs = ps.executeQuery();

            System.out.format("Database dump for Page ID: '%s'\n", pageId != null ? pageId : "ANY");
            if (message != null) {
                System.out.format("%s\n", message);
            }
            System.out.format("-----------------------------------------\n");

            int i = 0;
            while (rs.next()) {
                System.out.format("%-30s\t| %d\t| %d.%d\t| %d\n", rs.getString(1), rs.getLong(2), rs.getInt(3), rs
                    .getInt(4), rs.getShort(5));
                i++;
                if (n > 0 && i >= n) {
                    break;
                }
            }

            ps.close();
            System.out.format("-----------------------------------------\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
