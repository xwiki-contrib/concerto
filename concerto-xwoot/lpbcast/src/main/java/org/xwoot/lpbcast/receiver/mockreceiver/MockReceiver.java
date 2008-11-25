package org.xwoot.lpbcast.receiver.mockreceiver;

import java.io.File;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.ReceiverApi;

/**
 * A Mockup for the ReceiverAPI.
 * 
 * @version $Id:$
 */
public class MockReceiver implements ReceiverApi
{

    /** {@inheritDoc} */
    public void connectReceiver()
    {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    public void disconnectReceiver()
    {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    public Object getPeerId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public boolean isReceiverConnected()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /** {@inheritDoc} */
    public void receive(Message message)
    {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public File askState()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
