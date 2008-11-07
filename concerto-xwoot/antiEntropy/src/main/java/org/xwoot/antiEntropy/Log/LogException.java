package org.xwoot.antiEntropy.Log;

import org.xwoot.antiEntropy.AntiEntropyException;

/**
 * Exception handling for Log manipulation.
 * 
 * @version $Id:$
 */
public class LogException extends AntiEntropyException
{

    /**
     * Unique ID used in the serialization process.
     */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public LogException()
    {
        super();
    }

    /**
     * @param cause the cause
     * @see Exception#Exception(Throwable)
     */
    public LogException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message
     * @see Exception#Exception(String)
     */
    public LogException(String message)
    {
        super(message);
    }

    /**
     * @param message the message
     * @param cause the cause
     * @see Exception#Exception(String, Throwable)
     */
    public LogException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
