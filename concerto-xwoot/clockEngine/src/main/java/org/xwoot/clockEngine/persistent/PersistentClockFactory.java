package org.xwoot.clockEngine.persistent;

import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;
import org.xwoot.clockEngine.ClockFactory;

public class PersistentClockFactory extends ClockFactory
{

    @Override
    public Clock createClock(String id) throws ClockException
    {

        return new PersistentClock(id);
    }

}
