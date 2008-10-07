package org.xwoot.lpbcast.receiver.mockreceiver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.ReceiverApi;

public class MockReceiver implements ReceiverApi
{

    public void connectReceiver()
    {
        // TODO Auto-generated method stub

    }

    public void disconnectReceiver()
    {
        // TODO Auto-generated method stub

    }

    public Object getPeerId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isReceiverConnected()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void receive(Message message)
    {
        // TODO Auto-generated method stub

    }

    public File askState(Object from, String to) throws IOException, URISyntaxException, ClassNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
