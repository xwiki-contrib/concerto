package org.xwoot.wootEngine;

/**
 * Exception handling for the Woot Engine.
 * 
 * @version $Id:$
 */
public class WootEngineException extends Exception
{

    /** Unique ID for the serialization process. */
    private static final long serialVersionUID = -2568698052987298242L;

    /** Default constructor. */
    public WootEngineException()
    {
        super();
    }

    /**
     * Constructor with cause as parameters.
     * 
     * @param cause the exception that caused this exception.
     **/
    public WootEngineException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with message as parameter.
     * 
     * @param message a message describing the exception.
     **/
    public WootEngineException(String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause as parameters.
     * 
     * @param message a message describing the exception.
     * @param cause the exception that caused this exception.
     **/
    public WootEngineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
