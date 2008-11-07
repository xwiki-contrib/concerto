package org.xwoot.antiEntropy;

/**
 * Exception handling for AntiEntropy.
 * 
 * @version $Id:$
 */
public class AntiEntropyException extends Exception
{

    /**
     * Unique ID used in the serialization process.
     */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public AntiEntropyException()
    {
        super();
    }

    /**
     * @param cause the cause
     * @see Exception#Exception(Throwable)
     */
    public AntiEntropyException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message
     * @see Exception#Exception(String)
     */
    public AntiEntropyException(String message)
    {
        super(message);
    }

    /**
     * @param message the message
     * @param cause the cause
     * @see Exception#Exception(String, Throwable)
     */
    public AntiEntropyException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
