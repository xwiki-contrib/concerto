package org.xwoot.iwoot.xwootclient.mock;

import java.io.File;
import java.util.Collection;

import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;

public class XWootClientMock implements XWootAPI
{

    public boolean addNeighbour(String neighborURL)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void connectToContentManager() 
    {
        // TODO Auto-generated method stub
        
    }

    public boolean createNetwork() 
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void disconnectFromContentManager() 
    {
        // TODO Auto-generated method stub
        
    }

    public void disconnectFromP2PNetwork() 
    {
        // TODO Auto-generated method stub
        
    }

    public void doAntiEntropy(String neighborURL) 
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

    public Collection<String> getNeighborsList() 
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getXWootPeerId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean importWootStorage(File wst) 
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void initialiseWootStorage() 
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

    public boolean joinNetwork(String neighborURL) 
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void reconnectToP2PNetwork() 
    {
        // TODO Auto-generated method stub
        
    }

    public void removeNeighbor(String neighborURL) 
    {
        // TODO Auto-generated method stub
        
    }

    public void synchronizePages()
    {
        // TODO Auto-generated method stub
        
    }

    public void doAntiEntropyWithAllNeighbors() throws XWootException
    {
        // TODO Auto-generated method stub
        
    }

}
