package org.xwoot.xwootApp;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface XWootAPI
{

    //TODO : Verifier le rechargement des donnees (fichiers) en cas d'arret de l'application 
    // (rechargement de l'instance)

    /**
     * To create/update model. The given file is the woot storage computed by the network creator (computed with
     * initialiseWootStorage and exported with exportWootStorage()). All data are uncompressed in the model directory.
     * No need to be connected with P2P/contentManager. You have to call synchronize method to update xwiki.
     * 
     * @param wst : a zip file (must be computed by an XWoot neighbor)
     * @return true if import success
     */
    boolean importWootStorage(File wst) throws XWootException;

    /**
     * To get woot storage. No need to be connected with P2P/contentManager.
     * 
     * @return : the woot storage : a zip file.
     */
    File exportWootStorage();

    /**
     * To compute woot storage. Need to be connected with content provider.
     * 
     */
    void initialiseWootStorage() throws XWootException;

    boolean isWootStorageComputed();

    void doAntiEntropy(String neighborURL) throws XWootException;

    void doAntiEntropyWithAllNeighbors() throws XWootException;

    void connectToContentManager() throws XWootException;

    void disconnectFromContentManager() throws XWootException;

    boolean isContentManagerConnected();

    boolean joinNetwork(String neighborURL) throws XWootException;

    boolean createNetwork() throws XWootException;

    void reconnectToP2PNetwork() throws XWootException;

    void disconnectFromP2PNetwork() throws XWootException;

    boolean isConnectedToP2PNetwork();

    boolean addNeighbour(String neighborURL);

    Collection<String> getNeighborsList() throws XWootException;

    void removeNeighbor(String neighborURL) throws XWootException;

    void synchronizePages() throws XWootException;
    
    List<String> getLastPages(String id) throws XWootException;

    String getXWootPeerId();

    String getContentManagerURL();
}
