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

package org.xwoot.xwootApp.test;

import org.junit.Test;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.sender.LpbCastAPI;

import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;

import org.xwoot.xwootApp.XWoot;
import org.xwoot.xwootApp.core.XWootPage;

import java.io.File;
import java.util.Vector;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class TestBasic extends AbstractXWootTest
{

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithOneXWiki() throws Exception
    {

        // create a test page
        XWootPage page = new XWootPage("test.1", "");

        // Configure the XWOOT engine
        this.xwoot1.addPageManagement(page);
        Assert.assertEquals(true, this.xwoot1.isPageManaged(page));

        // /////////////////////
        // Scenario execution
        // /////////////////////
        // simulate a change from wikiContentManager user...
        this.xwiki1.removePage("test.1");
        this.xwiki1.createPage("test.1", "toto\n");
        Assert.assertEquals("toto\n", this.xwiki1.getPageContent("test.1"));

        // Launch the synch without connection
        this.xwoot1.synchronize();
        Assert.assertEquals("", this.wootEngine1.getContentManager().getContent("test.1", XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();

        // Launch the synch...
        this.xwoot1.synchronize();

        Assert.assertEquals("toto\n", this.wootEngine1.getContentManager().getContent("test.1", XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("toto\n", this.xwiki1.getPageContent("test.1"));
        this.xwiki1.overwritePageContent("test.1", "t\n");
        // Launch the synch...
        this.xwoot1.synchronize();
        Assert.assertEquals("t\n", this.wootEngine1.getContentManager().getContent("test.1", XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("t\n", this.xwiki1.getPageContent("test.1"));

    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithTwoXWiki() throws Exception
    {

        // create a test page
        XWootPage page = new XWootPage("test.1", "");

        Assert.assertEquals("", this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));

        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();
        this.xwoot2.reconnectToP2PNetwork();
        this.xwoot2.connectToContentManager();

        // add pages managements
        this.xwoot1.addPageManagement(page);
        this.xwoot2.addPageManagement(page);
        Assert.assertEquals(true, this.xwoot2.isPageManaged(page));
        Assert.assertEquals(true, this.xwoot1.isPageManaged(page));

        // connect sites
        this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot2);
        this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot1);
        Assert.assertTrue(this.lpbCast1.getNeighborsList().size() > 0);

        // /////////////////////
        // Scenario execution
        // /////////////////////

        // xwoot1 : Simulate a change from XWiki1 user...
        this.xwiki1.createPage(page.getPageName(), "");
        Assert.assertEquals("", this.xwiki1.getPageContent(page.getPageName()));
        this.xwiki1.overwritePageContent(page.getPageName(), "toto\n");
        Assert.assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));

        // xwoot1 : Launch the synch in XWOOT engine 1
        this.xwoot1.synchronize();

        // xwoot1 & this.xwoot2 : verify the propagation
        Assert.assertEquals("toto\n", this.wootEngine1.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("toto\n", this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("toto\n", this.xwiki2.getPageContent(page.getPageName()));

    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithTwoXWiki2() throws Exception
    {

        // create a test page
        XWootPage page = new XWootPage("test.1", "Content Page 1 \n");
        XWootPage page2 = new XWootPage("test.2", "Yoplaboom 2 \n");

        // xwoot1 : Simulate a change from XWiki1 user...
        this.xwiki1.createPage(page.getPageName(), "Content Page 1 \n");
        Assert.assertEquals("Content Page 1 \n", this.xwiki1.getPageContent(page.getPageName()));
        this.xwiki1.createPage(page2.getPageName(), "Yoplaboom 2 \n");
        Assert.assertEquals("Yoplaboom 2 \n", this.xwiki1.getPageContent(page2.getPageName()));
        this.xwiki2.removePage(page.getPageName());
        this.xwiki2.removePage(page2.getPageName());
        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();

        // add pages managements
        this.xwoot1.addPageManagement(page);
        this.xwoot1.addPageManagement(page2);

        Assert.assertEquals(true, this.xwoot1.isPageManaged(page2));
        Assert.assertEquals(true, this.xwoot1.isPageManaged(page));

        // /////////////////////
        // Scenario execution
        // /////////////////////

        // xwoot1 : Launch the synch in XWOOT engine 1
        this.xwoot1.synchronize();
        this.xwoot1.computeState();
        File state = this.xwoot1.getState();
        Assert.assertNotNull(state);
        System.out.println("====> " + state);
        // connect XWoot
        this.xwoot2.connectToContentManager();
        Assert.assertEquals(false, this.xwoot2.isPageManaged(page2));
        Assert.assertEquals(false, this.xwoot2.isPageManaged(page));

        Assert.assertTrue(this.xwoot2.importState(state));
        Assert.assertEquals(true, this.xwoot2.isPageManaged(page2));
        Assert.assertEquals(true, this.xwoot2.isPageManaged(page));
        this.xwoot2.reconnectToP2PNetwork();
        Assert.assertEquals("Content Page 1 \n", this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("Yoplaboom 2 \n", this.wootEngine2.getContentManager().getContent(page2.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testConflitBetweenVueAndModel() throws Exception
    {

        // create a test page
        XWootPage page = new XWootPage("test.1", "");

        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();

        // Configure the XWOOT engine
        this.xwoot1.addPageManagement(page);
        Assert.assertEquals(true, this.xwoot1.isPageManaged(page));

        // /////////////////////
        // Scenario execution
        // /////////////////////

        // simulate a change from wikiContentManager user...
        this.xwiki1.removePage(page.getPageName());
        this.xwiki1.createPage(page.getPageName(), "toto\n");
        Assert.assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));

        // create patch to change wootEngine model : insert "titi" in first
        // position
        Patch patch = new Patch();
        Vector<WootOp> vector = new Vector<WootOp>();
        WootId wootId = new WootId(String.valueOf(0), 0);
        WootIns op0 = new WootIns(new WootRow(wootId, "titi"), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(page.getPageName(), XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID, false));
        op0.setOpId(wootId);
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId(page.getPageName());

        Assert.assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("", this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));

        Message mess = new Message();
        mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
        mess.setContent(patch);
        mess.setOriginalPeerId("test_Peer");
        mess.setRound(1);

        this.xwoot1.receiveMessage(mess);

        Assert.assertEquals("titi\ntoto\n", this.wootEngine1.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("titi\ntoto\n", this.xwiki1.getPageContent(page.getPageName()));

    }

    // /**
    // * DOCUMENT ME!
    // *
    // * @throws Exception DOCUMENT ME!
    // */
    // @Test
    // public void testMDWithThreeConcurrentXWiki() throws Exception
    // {
    //
    // // configure neighbors
    // this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot2);
    // this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot3);
    // this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot1);
    // this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot3);
    // this.lpbCast3.addNeighbor(this.xwoot3, this.xwoot1);
    // this.lpbCast3.addNeighbor(this.xwoot3, this.xwoot2);
    //
    // // create a test page
    // XWootPage page = new XWootPage("test.1", "");
    //
    // // connect XWoot
    // this.xwoot1.reconnectToP2PNetwork();
    // this.xwoot1.connectToContentManager();
    // this.xwoot2.reconnectToP2PNetwork();
    // this.xwoot2.connectToContentManager();
    // this.xwoot3.reconnectToP2PNetwork();
    // this.xwoot3.connectToContentManager();
    //
    // // add pages managment
    // this.xwoot1.addPageManagement(page);
    // Assert.assertEquals(true, this.xwoot1.isPageManaged(page));
    //
    // // /////////////////////
    // // Scenario execution
    // // /////////////////////
    // // simulate a change from wikiContentManager user...
    // this.xwiki1.removePage(page.getPageName());
    //
    // this.xwiki1.overwritePageContent(page.getPageName(), "");
    // Map<String, String> fields = this.xwiki1.getFields(page.getPageName());
    // Assert.assertNotNull(fields);
    //
    // // fields.put(WikiContentManager.CREATOR,"XWiki.terminator");
    // // fields.put(WikiContentManager.MODIFIER, "XWiki.terminator");
    // fields.put(WikiContentManager.PARENTID, "Terminator's dad");
    // fields.put(WikiContentManager.HOMEPAGE, "false");
    // fields.put(WikiContentManager.ID, "test.1");
    // fields.put(WikiContentManager.SPACE, "test");
    // fields.put(WikiContentManager.TITLE, "1");
    //
    // this.xwiki1.setFields(page.getPageName(), fields);
    //
    // // Launch the synch...
    // this.xwoot1.synchronizePages();
    //
    // // Assert.assertEquals("XWiki.terminator",this.xwoot1.getTre().getValue(
    // // new
    // // MDIdentifier(page.getPageName(),WikiContentManager.CREATOR)).get());
    // // Assert.assertEquals("XWiki.terminator",
    // // this.xwoot1.getTre().getValue(
    // // new MDIdentifier(page.getPageName(),
    // // WikiContentManager.MODIFIER)).get());
    // Assert.assertEquals("Terminator's dad", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.PARENTID)).get());
    // Assert.assertEquals("false", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.HOMEPAGE)).get());
    // Assert.assertEquals("test.1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.ID)).get());
    // Assert.assertEquals("test", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.SPACE)).get());
    // Assert.assertEquals("1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.TITLE)).get());
    //
    // // assertEquals("XWiki.terminator",this.xwoot1.getTre().getValue(new
    // // MDIdentifier(page.getPageName(),WikiContentManager.CREATOR)).get());
    // // Assert.assertEquals("XWiki.terminator",
    // // this.xwoot2.getTre().getValue(
    // // new MDIdentifier(page.getPageName(),
    // // WikiContentManager.MODIFIER)).get());
    // Assert.assertEquals("Terminator's dad", this.xwoot2.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.PARENTID)).get());
    // Assert.assertEquals("false", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.HOMEPAGE)).get());
    // Assert.assertEquals("test.1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.ID)).get());
    // Assert.assertEquals("test", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.SPACE)).get());
    // Assert.assertEquals("1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.TITLE)).get());
    //
    // // assertEquals("XWiki.terminator",this.xwoot1.getTre().getValue(new
    // // MDIdentifier(page.getPageName(),WikiContentManager.CREATOR)).get());
    // // Assert.assertEquals("XWiki.terminator",
    // // this.xwoot3.getTre().getValue(
    // // new MDIdentifier(page.getPageName(),
    // // WikiContentManager.MODIFIER)).get());
    // Assert.assertEquals("Terminator's dad", this.xwoot3.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.PARENTID)).get());
    // Assert.assertEquals("false", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.HOMEPAGE)).get());
    // Assert.assertEquals("test.1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.ID)).get());
    // Assert.assertEquals("test", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.SPACE)).get());
    // Assert.assertEquals("1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.TITLE)).get());
    //
    // Map<String, String> fields2 = this.xwiki2.getFields(page.getPageName());
    // fields2.put(WikiContentManager.MODIFIER, "XWiki.predator");
    // Map<String, String> fields3 = this.xwiki3.getFields(page.getPageName());
    // fields3.put(WikiContentManager.MODIFIER, "XWiki.alien");
    // fields3.put(WikiContentManager.PARENTID, "XWiki.alien's mother");
    // this.xwiki2.setFields(page.getPageName(), fields2);
    // this.xwiki3.setFields(page.getPageName(), fields3);
    // this.xwoot3.synchronizePages();
    // this.xwoot2.synchronizePages();
    // this.xwoot1.synchronizePages();
    //
    // // WARNING : First synchronizer win ...
    //
    // // assertEquals("XWiki.terminator",this.xwoot1.getTre().getValue(new
    // // MDIdentifier(page.getPageName(),WikiContentManager.CREATOR)).get());
    // // Assert.assertEquals("XWiki.alien", this.xwoot1.getTre().getValue(
    // // new MDIdentifier(page.getPageName(),
    // // WikiContentManager.MODIFIER)).get());
    // Assert.assertEquals("XWiki.alien's mother", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.PARENTID)).get());
    // Assert.assertEquals("false", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.HOMEPAGE)).get());
    // Assert.assertEquals("test.1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.ID)).get());
    // Assert.assertEquals("test", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.SPACE)).get());
    // Assert.assertEquals("1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.TITLE)).get());
    //
    // // Assert.assertEquals("XWiki.terminator",this.xwoot1.getTre().getValue(
    // // new
    // // MDIdentifier(page.getPageName(),WikiContentManager.CREATOR)).get());
    // // Assert.assertEquals("XWiki.alien", this.xwoot2.getTre().getValue(
    // // new MDIdentifier(page.getPageName(),
    // // WikiContentManager.MODIFIER)).get());
    // Assert.assertEquals("XWiki.alien's mother", this.xwoot2.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.PARENTID)).get());
    // Assert.assertEquals("false", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.HOMEPAGE)).get());
    // Assert.assertEquals("test.1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.ID)).get());
    // Assert.assertEquals("test", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.SPACE)).get());
    // Assert.assertEquals("1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.TITLE)).get());
    //
    // // assertEquals("XWiki.terminator",this.xwoot1.getTre().getValue(new
    // // MDIdentifier(page.getPageName(),WikiContentManager.CREATOR)).get());
    // // Assert.assertEquals("XWiki.alien", this.xwoot3.getTre().getValue(
    // // new MDIdentifier(page.getPageName(),
    // // WikiContentManager.MODIFIER)).get());
    // Assert.assertEquals("XWiki.alien's mother", this.xwoot3.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.PARENTID)).get());
    // Assert.assertEquals("false", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.HOMEPAGE)).get());
    // Assert.assertEquals("test.1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.ID)).get());
    // Assert.assertEquals("test", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.SPACE)).get());
    // Assert.assertEquals("1", this.xwoot1.getTre().getValue(
    // new MDIdentifier(page.getPageName(), WikiContentManager.TITLE)).get());
    //
    // }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithThreeConcurrentXWiki() throws Exception
    {

        // configure neighbors
        this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot2);
        this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot3);
        this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot1);
        this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot3);
        this.lpbCast3.addNeighbor(this.xwoot3, this.xwoot1);
        this.lpbCast3.addNeighbor(this.xwoot3, this.xwoot2);

        // create a test page
        XWootPage page = new XWootPage("test.1", "");

        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();
        this.xwoot2.reconnectToP2PNetwork();
        this.xwoot2.connectToContentManager();
        this.xwoot3.reconnectToP2PNetwork();
        this.xwoot3.connectToContentManager();

        // add pages managment
        this.xwoot1.addPageManagement(page);
        Assert.assertEquals(true, this.xwoot1.isPageManaged(page));

        // /////////////////////
        // Scenario execution
        // /////////////////////
        // simulate a change from wikiContentManager user...
        this.xwiki1.removePage(page.getPageName());
        this.xwiki1.createPage(page.getPageName(), "Ligne 1 sur xwiki1\n");
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.xwiki1.getPageContent(page.getPageName()));
        // Launch the synch...
        this.xwoot1.synchronize();

        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine1.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.xwiki2.getPageContent(page.getPageName()));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine3.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.xwiki3.getPageContent(page.getPageName()));

        // simulate a change from wikiContentManager user...
        this.xwiki1.overwritePageContent(page.getPageName(),
            "Ligne -1 sur xwiki1\nLigne 0 sur xwiki1\nLigne 1 sur xwiki\n");
        Assert.assertEquals("Ligne -1 sur xwiki1\nLigne 0 sur xwiki1\nLigne 1 sur xwiki\n", this.xwiki1
            .getPageContent(page.getPageName()));
        this.xwiki2.overwritePageContent(page.getPageName(),
            "Ligne 0 sur xwiki2\nLigne 1 sur xwiki\nLigne 2 sur xwiki2\n");
        Assert.assertEquals("Ligne 0 sur xwiki2\nLigne 1 sur xwiki\nLigne 2 sur xwiki2\n", this.xwiki2
            .getPageContent(page.getPageName()));
        this.xwiki3.overwritePageContent(page.getPageName(),
            "Ligne 1 sur xwiki1\nLigne 2 sur xwiki3\nLigne 3 sur xwiki3\n");
        Assert.assertEquals("Ligne 1 sur xwiki1\nLigne 2 sur xwiki3\nLigne 3 sur xwiki3\n", this.xwiki3
            .getPageContent(page.getPageName()));
        // Launch the synch...
        this.xwoot1.synchronize();
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID), this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID), this.wootEngine3.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals(this.xwiki1.getPageContent(page.getPageName()), this.xwiki2.getPageContent(page
            .getPageName()));
        Assert.assertEquals(this.xwiki1.getPageContent(page.getPageName()), this.xwiki3.getPageContent(page
            .getPageName()));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID), this.xwiki1.getPageContent(page.getPageName()));

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");
        System.out.println("woot3 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine3.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");

        System.out.println("xwiki1 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki1.getPageContent(page.getPageName()));
        System.out.println("-------------------");
        System.out.println("xwiki2 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki2.getPageContent(page.getPageName()));
        System.out.println("-------------------");
        System.out.println("xwiki3 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki3.getPageContent(page.getPageName()));
        System.out.println("-------------------");
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithThreeXWiki() throws Exception
    {

        // configure neighbors
        this.lpbCast1.addNeighbor(this.xwoot1.getXWootPeerId(), this.xwoot2);
        this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot3);
        this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot1);
        this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot3);
        this.lpbCast3.addNeighbor(this.xwoot3, this.xwoot1);
        this.lpbCast3.addNeighbor(this.xwoot3, this.xwoot2);

        // create a test page
        XWootPage page = new XWootPage("test.1", "");

        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();
        this.xwoot2.reconnectToP2PNetwork();
        this.xwoot2.connectToContentManager();
        this.xwoot3.reconnectToP2PNetwork();
        this.xwoot3.connectToContentManager();

        // add pages managment
        this.xwoot1.addPageManagement(page);
        this.xwoot2.addPageManagement(page);
        this.xwoot3.addPageManagement(page);

        this.xwiki1.removePage(page.getPageName());
        this.xwiki2.removePage(page.getPageName());
        this.xwiki3.removePage(page.getPageName());

        this.xwiki1.createPage(page.getPageName(), "");
        this.xwiki2.createPage(page.getPageName(), "");
        this.xwiki3.createPage(page.getPageName(), "");

        // /////////////////////
        // Scenario execution
        // /////////////////////

        // verifying
        Assert.assertEquals("", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("", this.xwiki2.getPageContent(page.getPageName()));
        Assert.assertEquals("", this.xwiki3.getPageContent(page.getPageName()));

        // Simulate a change from XWikis users...
        this.xwiki1.overwritePageContent(page.getPageName(), "toto\n");
        this.xwiki2.overwritePageContent(page.getPageName(), "titi\n");
        this.xwiki3.overwritePageContent(page.getPageName(), "tata\n");

        // verifying
        Assert.assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("titi\n", this.xwiki2.getPageContent(page.getPageName()));
        Assert.assertEquals("tata\n", this.xwiki3.getPageContent(page.getPageName()));

        Assert.assertEquals("", this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("", this.wootEngine2.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("", this.wootEngine3.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));

        // synchronizes
        this.xwoot3.synchronize();
        this.xwoot2.synchronize();
        this.xwoot1.synchronize();

        Assert.assertEquals("toto\ntiti\ntata\n", this.wootEngine1.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("toto\ntiti\ntata\n", this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("toto\ntiti\ntata\n", this.wootEngine3.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));

        Assert.assertEquals("toto\ntiti\ntata\n", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("toto\ntiti\ntata\n", this.xwiki2.getPageContent(page.getPageName()));
        Assert.assertEquals("toto\ntiti\ntata\n", this.xwiki3.getPageContent(page.getPageName()));

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");
        System.out.println("woot3 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine3.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");

        System.out.println("xwiki1 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki1.getPageContent(page.getPageName()));
        System.out.println("-------------------");
        System.out.println("xwiki2 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki2.getPageContent(page.getPageName()));
        System.out.println("-------------------");
        System.out.println("xwiki3 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki3.getPageContent(page.getPageName()));
        System.out.println("-------------------");
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithTwoConcurrentXWiki() throws Exception
    {

        // configure neighbors
        this.lpbCast1.addNeighbor(this.xwoot1, this.xwoot2);
        this.lpbCast2.addNeighbor(this.xwoot2, this.xwoot1);

        // create a test page
        XWootPage page = new XWootPage("test.1", "");

        // connect XWoot
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.connectToContentManager();
        this.xwoot2.reconnectToP2PNetwork();
        this.xwoot2.connectToContentManager();

        // add pages managment
        this.xwoot1.addPageManagement(page);
        Assert.assertEquals(true, this.xwoot1.isPageManaged(page));

        // /////////////////////
        // Scenario execution
        // /////////////////////
        // simulate a change from wikiContentManager user...
        this.xwiki1.removePage(page.getPageName());
        this.xwiki2.removePage(page.getPageName());
        this.xwiki1.createPage(page.getPageName(), "Ligne 1 sur xwiki1\n");
        this.xwiki2.createPage(page.getPageName(), "Ligne 1 sur xwiki1\n");

        // create patch to change wootEngine model
        Patch patch = new Patch();
        Vector<WootOp> vector = new Vector<WootOp>();
        WootId wootId = new WootId(String.valueOf(0), 0);
        WootIns op0 =
            new WootIns(new WootRow(wootId, "Ligne 1 sur xwiki1"), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(page.getPageName(), XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID, false));
        op0.setOpId(wootId);
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId("test.1");

        this.xwoot1.getWootEngine().deliverPatch(patch);
        this.xwoot2.getWootEngine().deliverPatch(patch);

        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine1.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.xwiki1.getPageContent(page.getPageName()));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.xwiki2.getPageContent(page.getPageName()));

        // simulate a change from wikiContentManager user...
        this.xwiki1.overwritePageContent(page.getPageName(), "Ligne 0 sur xwiki1\nLigne 1 sur xwiki1\n");
        Assert.assertEquals("Ligne 0 sur xwiki1\nLigne 1 sur xwiki1\n", this.xwiki1.getPageContent(page.getPageName()));
        this.xwiki2.overwritePageContent(page.getPageName(), "Ligne 1 sur xwiki1\nLigne 2 sur xwiki2\n");
        Assert.assertEquals("Ligne 1 sur xwiki1\nLigne 2 sur xwiki2\n", this.xwiki2.getPageContent(page.getPageName()));
        // Launch the synch...
        this.xwoot1.synchronize();

        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID), this.wootEngine2.getContentManager().getContent(page.getPageName(),
            XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        Assert.assertEquals(this.xwiki1.getPageContent(page.getPageName()), this.xwiki2.getPageContent(page
            .getPageName()));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID), this.xwiki1.getPageContent(page.getPageName()));

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
            XWoot.PAGECONTENTFIELDID));
        System.out.println("-------------------");

        System.out.println("xwiki1 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki1.getPageContent(page.getPageName()));
        System.out.println("-------------------");
        System.out.println("xwiki2 : ");
        System.out.println("-------------------");
        System.out.println(this.xwiki2.getPageContent(page.getPageName()));
        System.out.println("-------------------");
    }

    // /**
    // * DOCUMENT ME!
    // *
    // * @throws Exception DOCUMENT ME!
    // */
    // public void testDebug1() throws Exception {
    //        
    // // configure neighbors
    // this.lpbCast1.addNeighbor(this.xwoot2);
    // this.lpbCast2.addNeighbor(this.xwoot1);
    //
    // // create a test page
    // XWootPage page = new XWootPage("test.1", "");
    //
    // // add pages managment
    // this.xwoot1.addPageManagement(page);
    // assertEquals(true, this.xwoot1.isPageManaged(page));
    //
    // ///////////////////////
    // //Scenario execution
    // ///////////////////////
    // // simulate a change from wikiContentManager user...
    // this.xwiki1.removePage(page.getPageName());
    // this.xwiki2.removePage(page.getPageName());
    // this.xwiki1.createPage(page.getPageName());
    // this.xwiki1._setPageContent(page.getPageName(), "toto\n");
    // this.xwoot1.synchronize();
    //        
    // // xwoot1 & xwoot2 : verify the propagation
    // assertEquals("toto\n", this.wootEngine1.getContent(page.getPageName()), XWoot.PAGEOBJECTID,
    // XWoot.PAGECONTENTFIELDID));
    // assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));
    // assertEquals("toto\n", this.xwiki2.getPageContent(page.getPageName()));
    // assertEquals("toto\n", this.wootEngine2.getContent(page.getPageName()), XWoot.PAGEOBJECTID,
    // XWoot.PAGECONTENTFIELDID));
    //        
    // this.xwoot1.synchronize();
    //        
    // Map
    // c=((XwikiSwizzleClient)this.xwiki1).createComment("New comment for test",
    // null, "", "test.newComment", "test.1", "", "");
    //      
    //        
    // c.put(WikiContentManager.CONTENT, "Yogourt");
    // ((XwikiSwizzleClient)this.xwiki1).setComment("test.1", c);
    //        
    // // xwoot1 & xwoot2 : verify the propagation
    // assertEquals("toto\n", this.wootEngine1.getContent(page.getPageName()), XWoot.PAGEOBJECTID,
    // XWoot.PAGECONTENTFIELDID));
    // assertEquals("toto\n", this.xwiki1.getPageContent(page.getPageName()));
    // assertEquals("toto\n", this.xwiki2.getPageContent(page.getPageName()));
    // assertEquals("toto\n", this.wootEngine2.getContent(page.getPageName()), XWoot.PAGEOBJECTID,
    // XWoot.PAGECONTENTFIELDID));
    // }
}
