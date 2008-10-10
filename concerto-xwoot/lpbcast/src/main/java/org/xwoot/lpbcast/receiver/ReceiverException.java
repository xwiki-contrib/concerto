package org.xwoot.lpbcast.receiver;

import org.xwoot.lpbcast.LpbCastException;

public class ReceiverException extends LpbCastException
{

    /**
     * 
     */
    private static final long serialVersionUID = -2568698052987298242L;

    public ReceiverException()
    {
        super();
    }

    public ReceiverException(Throwable arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public ReceiverException(String arg0)
    {
        super(arg0);
    }

    public ReceiverException(String arg0, Throwable t)
    {
        super(arg0, t);
    }
}
