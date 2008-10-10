package org.xwoot.lpbcast.receiver.mockreceiver;

import org.xwoot.lpbcast.receiver.ReceiverException;

public class MockReceiverException extends ReceiverException
{

    /**
     * 
     */
    private static final long serialVersionUID = -2568698052987298242L;

    public MockReceiverException()
    {
        super();
    }

    public MockReceiverException(Throwable arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public MockReceiverException(String arg0)
    {
        super(arg0);
    }

    public MockReceiverException(String arg0, Throwable t)
    {
        super(arg0, t);
    }
}
