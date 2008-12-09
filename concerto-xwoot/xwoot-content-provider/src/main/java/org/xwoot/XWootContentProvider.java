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

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
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
                .execute("CREATE TABLE modifications (pageId VARCHAR(512), timestamp BIGINT, version INT, minor_version INT, cleared SMALLINT DEFAULT 0, PRIMARY KEY(pageId, timestamp, version, minor_version), UNIQUE(pageId, timestamp))");

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
     * that has not been cleared.
     * 
     * @return A list of XWootIds.
     * @throws XWootContentProviderException
     */
    public List<XWootId> getModifiedPagesIds() throws XWootContentProviderException
    {
        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        List<XWootId> result = new ArrayList<XWootId>();

        /* Download last modifications from the server */
        updateModifiedPages();

        try {
            PreparedStatement ps =
                connection
                    .prepareStatement("SELECT pageId, MIN(timestamp) FROM modifications WHERE cleared=? GROUP BY pageId");
            ps.setBoolean(1, false);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                XWootId xwootId = new XWootId(rs.getString(1), rs.getLong(2));
                result.add(xwootId);
            }

            ps.close();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
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

            logger.info(String.format("%s at %s (%d) cleared. %d rows cleared", xwootId.getPageId(), new Date(xwootId
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

            logger.info(String.format("%s at %s (%d) cleared. %d rows cleared", xwootId.getPageId(), new Date(xwootId
                .getTimestamp()), xwootId.getTimestamp(), rowsUpdated));

            ps.close();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    /**
     * Debugging method. Dump the internal table for debugging purposes. Remove this when it will be no more needed.
     * 
     * @param orderBy The field to be sorted by. Can be 'pageId' or 'timestamp'.
     * @throws SQLException
     */
    public void dumpTable(String orderBy) throws SQLException
    {
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(String.format("SELECT * FROM modifications ORDER BY %s ASC", orderBy));

        while (rs.next()) {
            System.out.format("%s %s (%d) %d %d %b\n", rs.getString(1), new Date(rs.getLong(2)), rs.getLong(2), rs
                .getInt(3), rs.getInt(4), rs.getBoolean(5));
        }
    }

    /**
     * Debugging method. Dump the internal table for debugging purposes. Shows only the rows concerning a given pageId.
     * Remove this when it will be no more needed.
     * 
     * @param pageId The pageId to be selected.
     * @param orderBy The field to be sorted by. Can be 'pageId' or 'timestamp'.
     * @throws SQLException
     */
    public void dumpTable(String pageId, String orderBy) throws SQLException
    {
        PreparedStatement ps =
            connection.prepareStatement(String.format("SELECT * FROM modifications WHERE pageId=? ORDER BY %s ASC",
                orderBy));
        ps.setString(1, pageId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.format("%s %s (%d) %d %d %b\n", rs.getString(1), new Date(rs.getLong(2)), rs.getLong(2), rs
                .getInt(3), rs.getInt(4), rs.getBoolean(5));
        }
    }

}
