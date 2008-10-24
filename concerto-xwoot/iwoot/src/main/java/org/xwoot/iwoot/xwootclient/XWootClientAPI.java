package org.xwoot.iwoot.xwootclient;

public interface XWootClientAPI
{
   // public boolean addNeighbour(String neighborURL);

    public void connectToContentManager();

    public void disconnectFromContentManager(); 

    public boolean isContentManagerConnected();
  
    public boolean isConnectedToP2PNetwork();
   
}
