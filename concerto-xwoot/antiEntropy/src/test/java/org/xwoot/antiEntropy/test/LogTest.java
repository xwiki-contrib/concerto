/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.antiEntropy.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwoot.antiEntropy.Log.Log;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

/**
 * This junit class test all log functions.
 * 
 * @version $Id: $Revision$
 */
public class LogTest
{
    /** The test working directory for Log. */
    private static final String WORKING_DIR = "/tmp/xwootTests/LogTest";

    /** The test Log file path. */
    private String logFilePath;

    /** The test Log object. */
    private Log log;

    /** Test id to be added in the log. */
    private String testId1 = "1";

    /** Test id to be added in the log. */
    private String testId2 = "2";

    /** Test id to be added in the log. */
    private String testId3 = "3";

    /** Test id to be added in the log. */
    private String testId4 = "4";

    /** Test message to be added in the log. */
    private String testMessage1 = "titi";

    /** Test message to be added in the log. */
    private String testMessage2 = "tata";

    /** Test message to be added in the log. */
    private String testMessage3 = "tutu";

    /** Test message to be added in the log. */
    private String testMessage4 = "toto";

    /**
     * Makes sure the working dir exists and creates the test Log object.
     * 
     * @throws Exception if IO problems occur.
     */
    @Before
    public void setUp() throws Exception
    {
        File working = new File(WORKING_DIR);
        if (!working.exists() && !working.mkdir()) {
            throw new Exception("Can't create working directory: " + WORKING_DIR);
        }
        this.logFilePath = WORKING_DIR + File.separator + Log.LOG_FILE_NAME;
        this.log = new Log(this.logFilePath);
    }

    /**
     * Clears the log's contents.
     * 
     * @throws Exception if problems occur.
     */
    @After
    public void tearDown() throws Exception
    {
        this.log.clearLog();
        Assert.assertEquals(this.log.logSize(), 0);
    }

    /**
     * Test the add message by adding a message and getting it back.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testAdd() throws Exception
    {
        this.log.addMessage(testId1, testMessage1);
        Assert.assertEquals(this.log.logSize(), 1);

        Assert.assertEquals(testMessage1, log.getMessage(testId1));
    }

    /**
     * Test the diff function by adding all the messages in the log and after making a diff by half of them you should
     * end up with the other half.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testDiff() throws Exception
    {
        this.log.addMessage(testId1, testMessage1);
        this.log.addMessage(testId2, testMessage2);
        this.log.addMessage(testId3, testMessage3);
        this.log.addMessage(testId4, testMessage4);

        Serializable[] list = new Serializable[] {testId1, testId2};
        Object[] diff = this.log.getDiffKey(list);
        Assert.assertEquals(diff.length, 2);

        List<Object> diffAsList = Arrays.asList(diff);
        Assert.assertTrue(diffAsList.contains(testId3) && diffAsList.contains(testId4));
    }

    /**
     * The the function existInLog. The added message must exist in the log.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testExist() throws Exception
    {
        this.log.addMessage(testId1, testMessage1);
        Assert.assertTrue(this.log.existInLog(testId1));
    }

    /**
     * Test getting a log object. The returned object must be the same than the added object if the key used is the same
     * in both operations.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testGetMessage() throws Exception
    {
        this.log.addMessage(testId1, testMessage1);

        String message = (String) this.log.getMessage(testId1);
        Assert.assertEquals(message, testMessage1);
    }

    /**
     * The the function which return all add messages keys. After adding 100 messages, 100 message ids should exist in
     * the log.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testGetMessagesId() throws Exception
    {
        add100Messages();

        Assert.assertEquals(this.log.getMessageIds().length, 100);
    }

    /**
     * The log size must be equals to the number of objects that it contains.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testSize() throws Exception
    {
        add100Messages();

        Assert.assertEquals(this.log.logSize(), 100);
    }

    /**
     * Init log tests.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertEquals(this.log.logSize(), 0);
    }

    /**
     * Helper class for adding 100 messages.
     * 
     * @throws Exception if problems occur.
     **/
    private void add100Messages() throws Exception
    {
        for (int i = 0; i < 100; i++) {
            String message = testMessage1 + i;
            this.log.addMessage(testId1 + i, message);
        }
    }

    /**
     * Test adding the same message two times. Log must contains only one occurrence.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testUnicity() throws Exception
    {
        this.log.addMessage(testId1, testMessage1);
        this.log.addMessage(testId1, testMessage1);
        Assert.assertEquals(this.log.logSize(), 1);
    }
}
