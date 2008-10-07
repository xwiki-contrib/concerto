package org.xwoot.clockEngine;

public abstract class ClockFactory
{

    private final static String PERSISTENT = "org.xwoot.clockEngine.persistent.PersistentClockFactory";

    private static String location = PERSISTENT;

    public abstract Clock createClock(String id) throws ClockException;

    public static ClockFactory getFactory() throws ClockException
    {
        try {
            return (ClockFactory) Class.forName(ClockFactory.location).newInstance();
        } catch (Exception e) {
            throw new ClockException("Problem with clock factory", e);
        }
    }
}
