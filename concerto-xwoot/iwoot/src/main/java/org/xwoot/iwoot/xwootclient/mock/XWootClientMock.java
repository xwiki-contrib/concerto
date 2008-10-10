package org.xwoot.iwoot.xwootclient.mock;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.xwoot.xwootApp.XWootAPI;

public class XWootClientMock implements XWootAPI
{

    public boolean addNeighbour(String neighborURL)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void connectToContentManager() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    public boolean createNetwork() throws Exception
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void disconnectFromContentManager() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    public void disconnectFromP2PNetwork() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    public void doAntiEntropy(String neighborURL) throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    public File exportWootStorage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getContentManagerURL()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getNeighborsList() throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getXWootPeerId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean importWootStorage(File wst) throws Exception
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void initialiseWootStorage() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    public boolean isConnectedToP2PNetwork()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isContentManagerConnected()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isWootStorageComputed()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean joinNetwork(String neighborURL) throws Exception
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void reconnectToP2PNetwork() throws IOException, ClassNotFoundException, Exception
    {
        // TODO Auto-generated method stub
        
    }

    public void removeNeighbor(String neighborURL) throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    public void synchronizePages() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

}
