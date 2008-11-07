package org.xwoot.clockEngine;

/**
 * Exception handling for Clock engine.
 * 
 * @version $Id$
 */
public class ClockException extends Exception
{

    /** Unique ID for the serialization process. */
    private static final long serialVersionUID = -3633851466345319165L;

    /** Default constructor. */
    public ClockException()
    {
        super();

    }

    /**
     * Constructor with message as parameter.
     * 
     * @param message a message describing the exception.
     **/
    public ClockException(String message)
    {
        super(message);

    }

    /**
     * Constructor with message and cause as parameters.
     * 
     * @param message a message describing the exception.
     * @param cause the exception that caused this exception.
     **/
    public ClockException(String message, Throwable cause)
    {
        super(message, cause);

    }

}
