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

package org.xwoot.thomasRuleEngine.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.mock.MockIdentifier;
import org.xwoot.thomasRuleEngine.mock.MockValue;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpDel;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpNew;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpSet;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;

import junit.framework.Assert;

/**
 * Tests for the ThomasRuleEngine class.
 * 
 * @version $Id:$
 */
public class ThomasRuleEngineTest
{
    /** Test working dir path for saving entries. */
    private static final String WORKING_DIR_PATH = FileUtil.getTestsWorkingDirectoryPathForModule("thomasRuleEngine");

    /** Test treId. */
    private int treId1 = 1;

    /** Test treId. */
    private int treId2 = 2;

    /** Test instance. */
    private ThomasRuleEngine tre1;

    /** Test instance. */
    private ThomasRuleEngine tre2;

    /** Test ID. */
    private Identifier id1;

    /** Test ID. */
    private Identifier id2;

    /** Test ID. */
    private Identifier id3;

    /** Test value. */
    private Value val1;

    /** Test value. */
    private Value val2;

    /** Test value. */
    private Value val3;

    /**
     * Check/Create working directory.
     * 
     * @throws Exception if the directory is not usable.
     */
    @BeforeClass
    public static void initFile() throws Exception
    {
        FileUtil.checkDirectoryPath(WORKING_DIR_PATH);
    }

    /**
     * Tests if the working dir was initialized.
     * 
     * @throws Exception if the working dir is null.
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertTrue(new File(WORKING_DIR_PATH).exists());
    }

    /**
     * Initialize test objects.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void initTest() throws Exception
    {
        this.tre1 = new ThomasRuleEngine(this.treId1, WORKING_DIR_PATH);
        this.tre2 = new ThomasRuleEngine(this.treId2, WORKING_DIR_PATH);

        this.tre1.clearWorkingDir();
        this.tre2.clearWorkingDir();

        String pageName = "page1";

        this.id1 = new MockIdentifier(pageName, "id1");
        this.id2 = new MockIdentifier(pageName, "id2");
        this.id3 = new MockIdentifier(pageName, "id3");

        this.val1 = new MockValue("val1");
        this.val2 = new MockValue("val2");
        this.val3 = new MockValue("val3");
    }

    /**
     * Tests the basic behavior of applyOp.
     * <p>
     * Result: as described in {@link ThomasRuleEngine#applyOp(ThomasRuleOp)}.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     */
    @Test
    public void testApplyOpBasic() throws ThomasRuleEngineException
    {
        // ////////////////////
        // (!existInBase,NewOp) => Creation
        // ////////////////////
        ThomasRuleOp op0new = tre1.getOp(id1, val1);

        tre1.applyOp(op0new);
        tre2.applyOp(op0new);

        Assert.assertEquals(val1, tre1.getValue(id1));
        Assert.assertEquals(val1, tre2.getValue(id1));

        // ////////////////////
        // (!existInBase,SetOp) => Creation + Set value
        // ////////////////////
        ThomasRuleOp op0set = tre1.getOp(id2, val1);

        tre1.applyOp(op0set);
        tre2.applyOp(op0set);

        Assert.assertEquals(val1, tre1.getValue(id2));
        Assert.assertEquals(val1, tre2.getValue(id2));

        // ////////////////////
        // (!existInBase,DelOp) => Nothing
        // ////////////////////
        ThomasRuleOp op0del = tre1.getOp(id3, null);
        Assert.assertNull(op0del);

        // /////////////////////////
        // (existInBase,BaseTc>opTc) => Nothing (local value is last writter)
        // /////////////////////////
        // new id3 with val=val1 on tre1 and tre2
        ThomasRuleOp op1greater = tre1.getOp(id3, val1);
        tre1.applyOp(op1greater);
        tre2.applyOp(op1greater);
        Assert.assertEquals(val1, tre1.getValue(id3));
        Assert.assertEquals(val1, tre2.getValue(id3));

        // del id3 on tre1
        ThomasRuleOp op1greaterBis = tre1.getOp(id3, null);
        tre1.applyOp(op1greaterBis);
        Assert.assertNull(tre1.getValue(id3));
    }

    /**
     * Tests the behavior of applyOp based on consulting the timestamps.
     * <p>
     * Result: No operation older than the last modification done on an entry is performed, as described in
     * {@link ThomasRuleEngine#applyOp(ThomasRuleOp)}.
     * 
     * @throws ThomasRuleEngineException if problems occur.
     */
    @Test
    public void testApplyOpBasedOnTimestamps() throws ThomasRuleEngineException
    {
        // /////////////////////////
        // Don't apply an operation that is older than the last operation applied on the entry.
        // /////////////////////////

        // set id3 to val2 on tre2
        ThomasRuleOp op1greaterQ = tre2.getOp(id3, val2);
        tre2.applyOp(op1greaterQ);

        // create id3 with val3 on tre1
        ThomasRuleOp op1greaterTer = tre1.getOp(id3, val3);

        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1greaterTer.getTimestampIdCreation().compareTo(op1greaterQ.getTimestampIdCreation()) == 1);
        tre1.applyOp(op1greaterTer);
        Assert.assertEquals(val3, tre1.getValue(id3));

        // verify
        Assert.assertEquals(val2, tre2.getValue(id3));
        tre1.applyOp(op1greaterQ);
        Assert.assertEquals(val3, tre1.getValue(id3));

        // ///////////////////////////////
        // (existInBase,BaseTc<opTc,NewOp) => overwrite local value
        // ///////////////////////////////
        Assert.assertTrue((op1greaterTer instanceof ThomasRuleOpNew));

        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1greaterTer.getTimestampIdCreation().compareTo(op1greaterQ.getTimestampIdCreation()) == 1);

        tre2.applyOp(op1greaterTer);
        Assert.assertEquals(val3, tre2.getValue(id3));
    }

    /**
     * Tests the behavior of applyOp.
     * <p>
     * Result: as described in {@link ThomasRuleEngine#applyOp(ThomasRuleOp)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testApplyOpBasedOnTimestamps2() throws Exception
    {

        // create a New operation. 
        ThomasRuleOp op1greaterTer = tre1.getOp(id3, val3);
        Thread.sleep(10);

        // ///////////////////////////////
        // (existInBase,BaseTc<opTc,SetOp) => overwrite local value
        // ///////////////////////////////
        tre1.applyOp(tre1.getOp(id3, null));
        tre1.applyOp(tre1.getOp(id3, val1));

        ThomasRuleOp op1lowerSet = tre1.getOp(id3, val2);
        Assert.assertTrue((op1lowerSet instanceof ThomasRuleOpSet));
        tre1.applyOp(op1lowerSet);
        
        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1lowerSet.getTimestampIdCreation().compareTo(op1greaterTer.getTimestampIdCreation()) == 1);

        tre2.applyOp(op1lowerSet);
        Assert.assertEquals(val2, tre2.getValue(id3));

        // ///////////////////////////////
        // (existInBase,BaseTc<opTc,DelOp) => overwrite local value
        // ///////////////////////////////
        tre1.applyOp(tre1.getOp(id3, null));
        tre1.applyOp(tre1.getOp(id3, val3));

        ThomasRuleOp op1lowerDel = tre1.getOp(id3, null);
        Assert.assertTrue((op1lowerDel instanceof ThomasRuleOpDel));

        tre1.applyOp(op1lowerDel);
        // compare with the latest op applied to the wanted id
        Assert.assertTrue(op1lowerDel.getTimestampIdCreation().compareTo(op1lowerSet.getTimestampIdCreation()) == 1);

        tre2.applyOp(op1lowerDel);
        Assert.assertNull(tre2.getValue(id3));

        // ///////////////////////////////////////
        // (existInBase,BaseTc==opTc,BaseTm>=opTm) => Nothin (local value is
        // last writter)
        // ///////////////////////////////////////
        tre1.applyOp(tre1.getOp(id1, null));
        tre1.applyOp(tre1.getOp(id1, val1));

        ThomasRuleOp op1TcEqualTmGreater = tre1.getOp(id1, val2);
        tre1.applyOp(op1TcEqualTmGreater);

        ThomasRuleOp op1TcEqualTmGreaterBis = tre1.getOp(id1, val3);
        tre1.applyOp(op1TcEqualTmGreaterBis);
        Assert.assertTrue(op1TcEqualTmGreater.getTimestampIdCreation().compareTo(
            op1TcEqualTmGreaterBis.getTimestampIdCreation()) == 0);
        Assert.assertTrue(op1TcEqualTmGreater.getTimestampModif().compareTo(
            op1TcEqualTmGreaterBis.getTimestampModif()) == -1);

        tre1.applyOp(op1TcEqualTmGreater);

        Assert.assertEquals(val3, tre1.getValue(id1));
    }

    /**
     * Tests the behavior of getOp.
     * <p>
     * Result: As described in {@link ThomasRuleEngine#getOp(Identifier, Value)}.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testGetOp() throws Exception
    {
        // get op new
        // ////////////////////////
        // (!existInBase,givenVal) => normal id creation
        // ////////////////////////
        ThomasRuleOp op01 = tre1.getOp(id1, val1);
        Assert.assertTrue((op01 instanceof ThomasRuleOpNew));
        // Thread.sleep(50);

        // /////////////////////////
        // (!existInBase,!givenVal) => del on unknow value == return null
        // /////////////////////////
        ThomasRuleOp op00 = tre1.getOp(id2, null);
        Assert.assertNull(op00);
        // Thread.sleep(50);

        // apply op new
        tre1.applyOp(op01);
        Assert.assertEquals(val1, tre1.getValue(id1));
        // Thread.sleep(50);

        // get op set
        // ///////////////////////////////////////
        // (existInBase,!deletedInBase,givenVal) => normal set value
        // ///////////////////////////////////////
        // NORMAL SET
        ThomasRuleOp op101 = tre1.getOp(id1, val2);
        Assert.assertTrue((op101 instanceof ThomasRuleOpSet));
        // Thread.sleep(50);

        // ///////////////////////////////////////
        // (existInBase,!deletedInBase,!givenVal) => normal del value
        // ///////////////////////////////////////
        // NORMAL DEL
        ThomasRuleOp op100 = tre1.getOp(id1, null);
        Assert.assertTrue((op100 instanceof ThomasRuleOpDel));
        // Thread.sleep(50);

        // apply op set
        tre1.applyOp(op101);
        Assert.assertEquals(val2, tre1.getValue(id1));
        // Thread.sleep(50);

        // apply op del
        Assert.assertNotNull(tre1.applyOp(op100));
        Assert.assertEquals(null, tre1.getValue(id1));
        // Thread.sleep(50);

        // get op with new val on a deleted entry
        // /////////////////////////////////////
        // (existInBase,deletedInBase,givenVal) => normal re-creation (old
        // deleted value is re-created)
        // /////////////////////////////////////
        // NORMAL RE-NEW
        ThomasRuleOp op111 = tre1.getOp(id1, val3);
        Assert.assertTrue((op111 instanceof ThomasRuleOpNew));
        Assert.assertTrue(op111.getTimestampIdCreation().compareTo(op01.getTimestampIdCreation()) > 0);
        // Thread.sleep(50);

        // get op with no val on a deleted entry
        // ///////////////////////////////////////
        // (existInBase,deletedInBase,!givenVal) => "normal" re-deletion (the
        // modif timestamp is update ...)
        // ///////////////////////////////////////
        // NORMAL RE-DEL
        ThomasRuleOp op110 = tre1.getOp(id1, null);
        Assert.assertTrue((op110 instanceof ThomasRuleOpDel));
        Assert.assertTrue(op110.getTimestampModif().compareTo(op111.getTimestampModif()) >= 0);
        // Thread.sleep(50);

        // tre1 must not apply the new op (same timestamp of creation and
        // timestamp modif of op new is older than the lattest modif)
        tre1.applyOp(op01);
        Assert.assertNull(tre1.getValue(id1));
        // Thread.sleep(50);

        // tre1 must not apply the new op (same timestamp of creation and
        // timestamp modif of op set is older than the lattest modif)
        tre1.applyOp(op101);
        Assert.assertNull(tre1.getValue(id1));
        // Thread.sleep(50);

        // ////////////////////////////////////////
        // test set with same value than in base
        // engine must return null
        // ///////////////////////////////////////
        tre1.applyOp(tre1.getOp(id1, val1));
        Assert.assertEquals(val1, tre1.getValue(id1));
        Assert.assertNull(tre1.getOp(id1, val1));
    }

    /**
     * Generate timestamps on 2 different TREs.
     * <p>
     * Result: the latest timestamp will always be greater than any other previously generated timestamp, no matter on
     * what TRE it was generated.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public synchronized void testGetTimestamp() throws Exception
    {
        Timestamp t1 = tre1.getTimestamp();
        Timestamp t2 = tre2.getTimestamp();

        this.wait(10);

        Timestamp t3 = tre1.getTimestamp();

        Assert.assertEquals(1, t3.compareTo(t1));
        Assert.assertEquals(1, t2.compareTo(t1));
        Assert.assertEquals(0, t2.compareTo(t2));
        Assert.assertEquals(-1, t1.compareTo(t3));
    }
}
