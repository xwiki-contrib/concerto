package org.xwoot;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiExtendedId;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwiki.xmlrpc.model.XWikiPageHistorySummary;

public class NewXWootContentProvider implements XWootContentProviderInterface
{
    final Log logger = LogFactory.getLog(XWootContentProvider.class);

    /**
     * Number of modification results requested for each XMLRPC call, in order to avoid server overload.
     */
    private static final int MODIFICATION_RESULTS_PER_CALL = 25;

    private XWikiXmlRpcClient rpc;

    private XWootContentProviderStateManager stateManager;

    private String endpoint;

    private XWootContentProviderConfiguration configuration;

    /**
     * Constructor.
     * 
     * @param endpoint The target XWiki XMLRPC endpoint URL.
     * @param dbName The name for the state manager's underlying DB.
     * @param createDB If true the modifications DB is recreated (removing the previous one if it existed)
     * @param configurationProperties Properties used to configure the content provider.
     * @throws XWootContentProviderException
     */
    public NewXWootContentProvider(String endpoint, String dbName, boolean createDB, Properties configurationProperties)
        throws XWootContentProviderException
    {
        try {
            rpc = null;
            this.endpoint = endpoint;
            configuration = new XWootContentProviderConfiguration(configurationProperties);
            stateManager = new XWootContentProviderStateManager(dbName, createDB);
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }

        logger.info("Initialization done. [New]");

        URL configurationFileUrl = configuration.getConfigurationFileUrl();

        logger.info(String.format("Configured from: %s", configurationFileUrl != null ? configurationFileUrl
            : "User provided properties."));
        logger.info(String.format("Ignore patterns: %s", configuration.getIgnorePatterns()));
        logger.info(String.format("Cumulative classes: %s", configuration.getCumulativeClasses()));
        logger.info(String.format("Wootable properties: %s", configuration.getWootablePropertiesMap()));
    }

    /**
     * Login to the remote XWiki.
     * 
     * @param username
     * @param password
     * @throws XWootContentProviderException
     */
    public void login(String username, String password) throws XWootContentProviderException
    {
        if (rpc != null) {
            throw new XWootContentProviderException("XWootContentProvider is already logged in.");
        }

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
     */
    public void logout()
    {
        if (rpc == null) {
            return;
        }

        try {
            rpc.logout();
        } catch (XmlRpcException e) {
            logger.warn("Exception while logging out", e);
        }

        rpc = null;
    }

    /**
     * Dispose the XWootContentManager and its state manager. This method has to be called before that the application
     * exits in order to keep things in a clean state.
     */
    public void dispose()
    {
        logout();
        stateManager.dispose();
    }

    /**
     * Retrieves the last modifications starting from the last timestamp seen (the highest) and updates the
     * modifications table. This optimized version clears, for each received page, all the modifications except the one
     * with the highest timestamp (i.e., the last one). So that the next call to getModificationList will return only
     * the latest change for each changed page.
     * 
     * @throws XWootContentProviderException
     */
    public Set<XWootId> getModifiedPagesIds() throws XWootContentProviderException
    {
        if (rpc == null) {
            throw new XWootContentProviderException("XWootContentProvider is not logged in.");
        }

        try {
            long highestModificationTimestamp = stateManager.getHighestModificationTimestamp();

            Map<String, XWootId> pageIdToLatestModificationMap = new HashMap<String, XWootId>();

            int entriesReceived = 0;
            int duplicatedEntries = 0;
            int start = 0;

            int ignoredEntries = 0;
            Set<String> ignoredPages = new HashSet<String>();

            while (true) {
                /* Retrieve the modification list */
                List<XWikiPageHistorySummary> xphsList =
                    rpc.getModifiedPagesHistory(new Date(highestModificationTimestamp), MODIFICATION_RESULTS_PER_CALL,
                        start, true);

                for (XWikiPageHistorySummary xphs : xphsList) {
                    /* Check whether the page is ignored */
                    if (!configuration.isIgnored(xphs.getId())) {
                        /* Build an XWoot id with the received data */
                        XWootId xwootId =
                            new XWootId(xphs.getBasePageId(), xphs.getModified().getTime(), xphs.getVersion(), xphs
                                .getMinorVersion());

                        /* Add the modification to the state */
                        if (!stateManager.addModification(xwootId)) {
                            duplicatedEntries++;
                        }

                        entriesReceived++;

                        /* Keep track of the latest modification received for each single page */
                        XWootId latestModification = pageIdToLatestModificationMap.get(xwootId.getPageId());
                        if (latestModification == null) {
                            pageIdToLatestModificationMap.put(xwootId.getPageId(), xwootId);
                        } else {
                            if (xwootId.getTimestamp() >= latestModification.getTimestamp()) {
                                pageIdToLatestModificationMap.put(xwootId.getPageId(), xwootId);
                            }
                        }
                    } else {
                        ignoredEntries++;
                        ignoredPages.add(xphs.getBasePageId());
                    }
                }

                /* Check if we have received all the modifications */
                if (xphsList.size() < MODIFICATION_RESULTS_PER_CALL) {
                    break;
                }

                /* Prepare for the next request */
                start = start + MODIFICATION_RESULTS_PER_CALL;
            }

            logger.info(String.format(
                "Modifcations list updated. Received %d entries starting from timestamp %d. %d duplicates.",
                entriesReceived, highestModificationTimestamp, duplicatedEntries));
            logger.info(String
                .format("Modifcations list updated. Ignored %d entries: %s", ignoredEntries, ignoredPages));

            /* For each received page, clear all the modification except the latest one */
            for (String pageId : pageIdToLatestModificationMap.keySet()) {
                stateManager.clearAllModificationExcept(pageIdToLatestModificationMap.get(pageId));
            }

            /* Return the modified pages */
            return stateManager.getNonClearedModificationsWithLowestTimestamp();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    public void clearAllModifications(XWootId xwootId) throws XWootContentProviderException
    {
        // TODO Auto-generated method stub
    }

    public void clearAllModifications() throws XWootContentProviderException
    {
        try {
            stateManager.clearAllModifications();
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    public void clearModification(XWootId xwootId) throws XWootContentProviderException
    {
        try {
            stateManager.clearModification(xwootId);
        } catch (Exception e) {
            throw new XWootContentProviderException(e);
        }
    }

    public List<XWootObject> getModifiedEntities(XWootId xwootId) throws XWootContentProviderException
    {
        try {
            List<XWootObject> result = new ArrayList<XWootObject>();

            logger.info(String.format("Getting modified entities for %s", xwootId));

            XWootId lastClearedModification = stateManager.getLastCleared(xwootId.getPageId());

            /* Main page */
            if (lastClearedModification == null) {
                logger.info(String.format("No last cleared version exists for %s", xwootId));

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
                logger.info(String.format("Last cleared version for %s is %s", xwootId, lastClearedModification));

                XWikiPage page = rpc.getPage(xwootId.getPageId(), xwootId.getVersion(), xwootId.getMinorVersion());
                XWikiPage lastClearedPage =
                    rpc.getPage(lastClearedModification.getPageId(), lastClearedModification.getVersion(),
                        lastClearedModification.getMinorVersion());

                XWootObject currentPageObject = Utils.xwikiPageToXWootObject(page, false);
                XWootObject lastClearedPageObject = Utils.xwikiPageToXWootObject(lastClearedPage, false);

                XWootObject cleanedUpXWootObject =
                    Utils.removeUnchangedFields(currentPageObject, lastClearedPageObject);

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

                    logger.info(String.format("Retrieving object with guid '%s' class '%s' at version %d.%d",
                        xwikiObjectSummary.getGuid(), xwikiObjectSummary.getClassName(), xwootId.getVersion(), xwootId
                            .getMinorVersion()));

                    XWikiObject xwikiObject = rpc.getObject(extendedId.toString(), xwikiObjectSummary.getGuid());

                    XWikiObject previousXWikiObject = null;

                    /*
                     * This is ugly because we cannot understand when there has been a network problem or the object
                     * doesn't exist.
                     */
                    try {
                        /* In order to get an object with a guid at a given version we need to use XWiki Extended Ids */
                        extendedId = new XWikiExtendedId(lastClearedModification.getPageId());
                        extendedId.setParameter(XWikiExtendedId.VERSION_PARAMETER, String.format("%d",
                            lastClearedModification.getVersion()));
                        extendedId.setParameter(XWikiExtendedId.MINOR_VERSION_PARAMETER, String.format("%d",
                            lastClearedModification.getMinorVersion()));

                        logger.info(String.format(
                            "Retrieving object with guid '%s' class '%s' at last cleared version %d.%d", xwikiObjectSummary
                                .getGuid(), xwikiObjectSummary.getClassName(), lastClearedModification.getVersion(),
                            lastClearedModification.getMinorVersion()));

                        previousXWikiObject = rpc.getObject(extendedId.toString(), xwikiObjectSummary.getGuid());
                    } catch (Exception e) {
                    }

                    if (previousXWikiObject != null) {
                        XWootObject currentXWootObject =
                            Utils.xwikiObjectToXWootObject(xwikiObject, false, configuration);
                        XWootObject lastClearedXWootObject =
                            Utils.xwikiObjectToXWootObject(previousXWikiObject, false, configuration);

                        cleanedUpXWootObject = Utils.removeUnchangedFields(currentXWootObject, lastClearedXWootObject);

                        if (cleanedUpXWootObject.getFields().size() > 0) {
                            result.add(cleanedUpXWootObject);
                        }
                    } else {
                        logger.info(String.format(
                            "Object with guid '%s' class '%s' at previous version %d.%d doesn't exist. Newly created!",
                            xwikiObjectSummary.getGuid(), xwikiObjectSummary.getClassName(), lastClearedModification
                                .getVersion(), lastClearedModification.getMinorVersion()));

                        result.add(Utils.xwikiObjectToXWootObject(xwikiObject, true, configuration));
                    }
                }
            }

            logger.info(String.format("Got modified entities: %s", result));

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

            XWootId xwootId =
                new XWootId(page.getId(), page.getModified().getTime(), page.getVersion(), page.getMinorVersion());

            stateManager.clearModification(xwootId);

            return xwootId;
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

            logger.info(String.format("'%s' stored. Stored page info: '%s' version %d.%d", page.getId(), page.getId(),
                page.getVersion(), page.getMinorVersion()));

            XWootId xwootId =
                new XWootId(page.getId(), page.getModified().getTime(), page.getVersion(), page.getMinorVersion());

            stateManager.clearModification(xwootId);

            return xwootId;
        } catch (Exception e) {
            logger.error(String.format("'%s' not stored due to an exception.", object.getGuid()), e);
            return null;
        }
    }

    public XWikiXmlRpcClient getRpc()
    {
        return rpc;
    }

    public XWootContentProviderStateManager getStateManager()
    {
        return stateManager;
    }

}
