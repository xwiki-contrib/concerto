package org.xwoot.xwootApp;

import java.io.File;
import java.util.Collection;
import java.util.List;


public interface XWootAPI
{

    // TODO : Verifier le rechargement des donnees (fichiers) en cas d'arret de l'application
    // (rechargement de l'instance)

    /**
     * To create/update model. The given file is the woot storage computed by the network creator (computed with
     * initialiseWootStorage and exported with exportWootStorage()). All data are uncompressed in the model directory.
     * No need to be connected with P2P/contentManager. You have to call synchronize method to update xwiki.
     * 
     * @param wst : a zip file (must be computed by an XWoot neighbor)
     * @return true if import success
     */
    boolean importState(File wst) throws XWootException;

    public File computeState() throws XWootException;

    /**
     * To get woot storage. No need to be connected with P2P/contentManager.
     * 
     * @return : the woot storage : a zip file.
     */
    File getState();

    boolean isStateComputed();

    void doAntiEntropy(Object neighbor) throws XWootException;

    void doAntiEntropyWithAllNeighbors() throws XWootException;

    void connectToContentManager() throws XWootException;

    void disconnectFromContentManager() throws XWootException;

    boolean isContentManagerConnected();

    boolean joinNetwork(String neighborURL) throws XWootException;

    boolean createNetwork() throws XWootException;

    void reconnectToP2PNetwork() throws XWootException;

    void disconnectFromP2PNetwork() throws XWootException;

    boolean isConnectedToP2PNetwork();
    
    boolean isConnectedToP2PGroup();

    boolean addNeighbour(String neighborURL);

    Collection getNeighborsList() throws XWootException;

    void removeNeighbor(String neighborURL) throws XWootException;

    void synchronize() throws XWootException;
    
    void synchronize(boolean generatePatches) throws XWootException;

    List<String> getLastPages(String id) throws XWootException;

    String getXWootPeerId();

    String getContentManagerURL();

    Object receiveMessage(Object message) throws XWootException;
}
