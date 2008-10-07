package org.xwoot.clockEngine.persistent;

import org.xwoot.clockEngine.ClockException;

public class PersistentClockException extends ClockException
{

    /**
     * 
     */
    private static final long serialVersionUID = 6005925845560357815L;

    public PersistentClockException()
    {
        super();
    }

    public PersistentClockException(String arg0)
    {
        super(arg0);
    }

    public PersistentClockException(String arg0, Throwable cause)
    {
        super(arg0, cause);
    }
}
