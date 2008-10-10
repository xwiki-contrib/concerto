package org.xwoot.lpbcast.sender;

import org.xwoot.lpbcast.LpbCastException;

public class SenderException extends LpbCastException
{

    /**
     * 
     */
    private static final long serialVersionUID = -2568698052987298242L;

    public SenderException()
    {
        super();
    }

    public SenderException(Throwable arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public SenderException(String arg0)
    {
        super(arg0);
    }

    public SenderException(String arg0, Throwable t)
    {
        super(arg0, t);
    }
}
