package org.xwoot.lpbcast.receiver;

import java.io.IOException;
import java.net.URISyntaxException;

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
     * @throws Exception DOCUMENT ME!
     */
    void receive(Message message) throws IOException, ClassNotFoundException, URISyntaxException;

}
