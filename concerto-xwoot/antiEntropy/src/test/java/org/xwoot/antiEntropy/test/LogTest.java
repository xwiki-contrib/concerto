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

import junit.framework.Assert;

/**
 * This junit class test all log functions
 * 
 * @author $author$
 * @version $Revision$
 */
public class LogTest
{
    private String logFilePath;

    private Log log;

    private final static String WORKINGDIR = "/tmp/antiEntropyTest";

    @Before
    public void setUp() throws Exception
    {
        File working = new File(WORKINGDIR);
        if (!working.exists() && !working.mkdir()) {
            throw new Exception("Can't create working directory: " + WORKINGDIR);
        }
        this.logFilePath = WORKINGDIR + File.separator + "log";
        this.log = new Log(this.logFilePath);
    }

    @After
    public void tearDown() throws Exception
    {
        this.log.clearLog();
        Assert.assertEquals(this.log.logSize(), 0);
    }

    /**
     * Test the add message
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testAdd() throws Exception
    {
        String message = "toto";
        this.log.addMessage("message1", message);
        Assert.assertEquals(this.log.logSize(), 1);
    }

    /**
     * Test the diff function ...
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testDiff() throws Exception
    {
        String message0 = "toto";
        String id0 = "message0";
        this.log.addMessage(id0, message0);

        String message1 = "titi";
        String id1 = "message1";
        this.log.addMessage(id1, message1);

        String message2 = "tata";
        this.log.addMessage("message2", message2);

        String message3 = "tutu";
        this.log.addMessage("message3", message3);

        Serializable[] list = new Serializable[] {id0, id1};
        Object[] diff = this.log.getDiffKey(list);
        Assert.assertEquals(diff.length, 2);
    }

    /**
     * The the function existInLog. The added message must exist in the log.
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testExist() throws Exception
    {
        String message = "toto";
        this.log.addMessage("message", message);
        Assert.assertTrue(this.log.existInLog("message"));
    }

    /**
     * Test getting a log object. The got object must be the same than the put object if the used key is the same in the
     * two operations.
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testGetMessage() throws Exception
    {
        String message = "toto";
        this.log.addMessage("message", message);

        String message2 = (String) this.log.getMessage("message");
        Assert.assertEquals(message2, "toto");
    }

    /**
     * The the function which return all add messages keys.
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testGetMessagesId() throws Exception
    {
        for (int i = 0; i < 100; i++) {
            String message = "toto" + i;
            this.log.addMessage("message" + i, message);
        }

        Assert.assertEquals(this.log.getMessagesId().length, 100);
    }

    /**
     * Init log tests
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertEquals(this.log.logSize(), 0);
    }

    /**
     * The log size must be equals to the number of differents objects that it contains.
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testSize() throws Exception
    {
        for (int i = 0; i < 100; i++) {
            String message = "toto" + i;
            this.log.addMessage("message" + i, message);
        }

        Assert.assertEquals(this.log.logSize(), 100);
    }

    /**
     * Test adding the same message two times ; log must contains only one occurence
     * 
     * @throws Exception : TODO best exception gestion ... exceptions concerning log file
     */
    @Test
    public void testUnicity() throws Exception
    {
        String message = "titi";
        this.log.addMessage("message", message);
        this.log.addMessage("message", message);
        Assert.assertEquals(this.log.logSize(), 1);
    }
}
