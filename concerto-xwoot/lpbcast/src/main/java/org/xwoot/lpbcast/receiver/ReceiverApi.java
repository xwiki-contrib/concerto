package org.xwoot.lpbcast.receiver;

import org.xwoot.lpbcast.message.Message;

public interface ReceiverApi
{
    void connectReceiver() throws Exception;

    void disconnectReceiver() throws Exception;

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    Object getPeerId();

    boolean isReceiverConnected();

    /**
     * DOCUMENT ME!
     * 
     * @param content DOCUMENT ME!
     * @throws ReceiverException 
     */
    void receive(Message message) throws ReceiverException;

}
