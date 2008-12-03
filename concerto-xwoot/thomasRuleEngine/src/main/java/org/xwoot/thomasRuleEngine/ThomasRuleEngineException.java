package org.xwoot.thomasRuleEngine;

/**
 * Exception handling for LPBcast module.
 * 
 * @version $Id:$
 */
public class ThomasRuleEngineException extends Exception
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public ThomasRuleEngineException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public ThomasRuleEngineException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public ThomasRuleEngineException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public ThomasRuleEngineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
