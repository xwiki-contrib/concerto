package org.xwoot.clockEngine.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Unit test for the Clock module.
 * 
 * @version $Id$Revision$
 */
public class ClockTest
{
    /** Test working directory where to serialize the clock. */
    private static final String WORKING_DIR = "/tmp/xwootTests/clockTests";

    /**
     * Makes sure the Working dir is usable.
     * 
     * @throws Exception if the working dir is problematic.
     * @see FileUtil#checkDirectoryPath(String)
     */
    @BeforeClass
    public static void init() throws Exception
    {
        FileUtil.checkDirectoryPath(WORKING_DIR);
    }

    /**
     * Tests the clock's functionality.
     * 
     * @throws Exception if file access problems occur.
     */
    @Test
    public void testClock() throws Exception
    {
        Clock clock = new Clock(WORKING_DIR);
        clock.load();
        clock.reset();
        clock.store();
        assertEquals(0, clock.getValue());

        clock.load();
        clock.setValue(1);
        clock.store();

        Clock clock2 = new Clock(WORKING_DIR);
        clock2.load();
        assertEquals(1, clock2.getValue());

        clock2.reset();
        clock2.store();
        clock.load();
        assertEquals(0, clock.getValue());
    }

    /**
     * Test if the clock automatically resets itself if the file gets deleted.
     * 
     * @throws ClockException if file access problems occur.
     */
    @Test
    public void testClock2() throws ClockException
    {
        Clock clock = new Clock(WORKING_DIR);
        clock.load();
        clock.setValue(1);
        clock.store();
        clock.load();
        assertEquals(1, clock.getValue());

        File f = new File(WORKING_DIR + File.separatorChar + Clock.CLOCK_FILE_NAME);
        f.delete();
        assertFalse(f.exists());

        clock.load();
        int temp = clock.getValue();
        assertTrue(f.exists());
        assertEquals(0, temp);
    }

}
