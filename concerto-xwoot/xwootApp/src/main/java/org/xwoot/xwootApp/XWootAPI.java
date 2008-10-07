package org.xwoot.xwootApp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.xwoot.xwootApp.core.XWootPage;

public interface XWootAPI
{

    boolean addNeighbour(String neighborURL);

    /**
     * To create/update model. The given file is the woot storage computed by the network creator (computed with
     * initialiseWootStorage and exported with exportWootStorage()). All data are uncompressed in the model directory.
     * No need to be connected with P2P/contentManager. You have to call synchronize method to update xwiki.
     * 
     * @param wst : a zip file (must be computed by an XWoot neighbor)
     * @return true if import success
     * @throws Exception
     */
    boolean importWootStorage(File wst) throws Exception;

    /**
     * To get woot storage. No need to be connected with P2P/contentManager.
     * 
     * @return : the woot storage : a zip file.
     */
    File exportWootStorage();

    /**
     * To compute woot storage. Need to be connected with content provider.
     * 
     * @throws Exception
     */
    void initialiseWootStorage() throws Exception;

    /**
     * @throws Exception
     */
    void connectToContentManager() throws Exception;

    void disconnectFromContentManager() throws Exception;

    boolean isContentManagerConnected();

    void reconnectToP2PNetwork() throws IOException, ClassNotFoundException, Exception;

    void disconnectFromP2PNetwork() throws Exception;

    boolean isConnectedToP2PNetwork();

    void doAntiEntropy(String neighborURL) throws Exception;

    Collection<String> getNeighborsList() throws Exception;

    void removeNeighbor(String neighborURL) throws Exception;

    boolean joinNetwork(String neighborURL) throws Exception;

    boolean createNetwork() throws Exception;

    void synchronizePages() throws Exception;

    String getPeerId();

    String getContentManagerURL();

    boolean isWootStorageComputed();

    boolean isPageManaged(XWootPage page);
}
