package org.xwoot.clockEngine.Persistent.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;
import org.xwoot.clockEngine.ClockFactory;
import org.xwoot.clockEngine.persistent.PersistentClock;

public class PersistentClockTest
{

    private final static String WORKINGDIR = "/tmp/clockTests";

    @BeforeClass
    public static void setup() throws Exception
    {
        File working = new File(WORKINGDIR);

        if (!working.exists() && !working.mkdir()) {
            throw new Exception("Can't create working directory: " + WORKINGDIR);
        }
    }

    @Test(expected = ClockException.class)
    public void testCreate2() throws ClockException, IOException
    {
        File f = new File(WORKINGDIR + File.separatorChar + "file.tmp");
        f.delete();
        assertFalse(f.exists());
        f.createNewFile();
        assertTrue(f.exists());
        ClockFactory.getFactory().createClock(f.toString());
    }

    @Test(expected = ClockException.class)
    public void testCreate3() throws ClockException
    {
        File f = new File(WORKINGDIR + File.separatorChar + "folder");
        f.delete();
        assertFalse(f.exists());
        f.mkdir();
        assertTrue(f.exists());
        f.setReadOnly();
        assertFalse(f.canWrite());
        ClockFactory.getFactory().createClock(f.toString());
    }

    @Test
    public void testCreate5() throws ClockException
    {
        File f = new File(WORKINGDIR);
        f.mkdir();
        assertTrue(f.exists());
        Clock c = ClockFactory.getFactory().createClock(f.toString());
        assertNotNull(c);
    }

    @Test
    public void testCreate6() throws ClockException
    {
        File f = new File(WORKINGDIR + "tmp");
        f.delete();
        assertFalse(f.exists());
        Clock c = ClockFactory.getFactory().createClock(f.toString());
        assertNotNull(c);
    }

    @Test
    public void testClock() throws Exception
    {
        Clock clock = ClockFactory.getFactory().createClock(WORKINGDIR);
        clock.load();
        clock.reset();
        clock.store();
        assertEquals(0, clock.getValue());
        clock.load();
        clock.setValue(1);
        clock.store();
        Clock clock2 = ClockFactory.getFactory().createClock(WORKINGDIR);
        clock2.load();
        assertEquals(1, clock2.getValue());
        clock2.reset();
        clock2.store();
        clock.load();
        assertEquals(0, clock.getValue());
        clock.store();
    }

    @Test
    public void testClock3() throws ClockException
    {
        Clock clock = ClockFactory.getFactory().createClock(WORKINGDIR);
        clock.load();
        clock.reset();
        clock.store();
        clock.load();
        assertEquals(0, clock.getValue());
        clock.setValue(1);
        clock.store();
        clock.load();
        assertEquals(1, clock.getValue());
        File f = new File(WORKINGDIR + File.separatorChar + PersistentClock.CLOCKFILENAME);
        f.delete();
        assertFalse(f.exists());
        clock.load();
        int temp = clock.getValue();
        assertTrue(f.exists());
        assertEquals(0, temp);
    }

}
