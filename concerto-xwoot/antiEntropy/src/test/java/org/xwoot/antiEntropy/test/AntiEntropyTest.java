package org.xwoot.antiEntropy.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.antiEntropy.AntiEntropy;

public class AntiEntropyTest
{

    private final static String WORKINGDIR = "/tmp/xwootTests/antiEntropy";

    private String workingPath1;

    private String workingPath2;

    private AntiEntropy ae1;

    private AntiEntropy ae2;

    @BeforeClass
    public static void initFile()
    {
        if (!new File(WORKINGDIR).exists()) {
            new File(WORKINGDIR).mkdirs();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        this.workingPath1 = WORKINGDIR + File.separator + "site1";

        if (!new File(this.workingPath1).exists()) {
            new File(this.workingPath1).mkdirs();
        }

        this.workingPath2 = WORKINGDIR + File.separator + "site2";

        if (!new File(this.workingPath2).exists()) {
            new File(this.workingPath2).mkdirs();
        }

        this.ae1 = new AntiEntropy(this.workingPath1);
        this.ae2 = new AntiEntropy(this.workingPath2);

    }

    @After
    public void tearDown() throws Exception
    {
        this.ae1.getLog().clearLog();
        this.ae2.getLog().clearLog();
    }

    @Test
    public void testInit() throws Exception
    {
        assertTrue(new File(WORKINGDIR).exists());
    }

    @Test
    public void testAntiEntropy() throws IOException, ClassNotFoundException
    {
        assertEquals(this.ae1.getLog().logSize(), 0);
        assertEquals(this.ae2.getLog().logSize(), 0);

        String message1 = "toto";
        String message2 = "titi";
        String message3 = "tata";

        // receive
        this.ae1.logMessage("message1", message1);
        this.ae1.logMessage("message2", message2);
        this.ae1.logMessage("message3", message3);
        assertEquals(this.ae1.getLog().logSize(), 3);

        // anti entropy
        Object[] site2ids = this.ae2.getContentForAskAntiEntropy();
        Collection diff = this.ae1.answerAntiEntropy(site2ids);
        assertEquals(diff.size(), 3);

        this.ae2.logMessage("message1", message1);
        site2ids = this.ae2.getContentForAskAntiEntropy();
        diff = this.ae1.answerAntiEntropy(site2ids);
        assertEquals(diff.size(), 2);

        this.ae2.logMessage("message2", message2);
        site2ids = this.ae2.getContentForAskAntiEntropy();
        diff = this.ae1.answerAntiEntropy(site2ids);
        assertEquals(diff.size(), 1);

        this.ae2.logMessage("message3", message3);
        site2ids = this.ae2.getContentForAskAntiEntropy();
        diff = this.ae1.answerAntiEntropy(site2ids);
        assertEquals(diff.size(), 0);

        assertEquals(this.ae2.getLog().logSize(), 3);
    }
}
