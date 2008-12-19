package org.xwoot.xwootApp.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.xwoot.MockXWootContentProvider;
import org.xwoot.XWootId;
import org.xwoot.XWootObject;
import org.xwoot.XWootObjectField;
import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.sender.LpbCastAPI;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.XWootObjectValue;

public class TestXWoot2 extends AbstractXWootTest
{

    private XWootObject createObject1(String content)
    {
        List fields = new ArrayList<XWootObjectField>();
        XWootObjectField f1 = new XWootObjectField("content", content, true);
        XWootObjectField f2 = new XWootObjectField("title", "Page de test", false);
        XWootObjectField f3 = new XWootObjectField("author", "Terminator", false);
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        XWootObject obj = new XWootObject("test.1", "XWikiPage", false, fields, true);
        return obj;
    }

    private XWootObject createObjectContentModification(String newContent)
    {
        List fields = new ArrayList<XWootObjectField>();
        XWootObjectField f1 = new XWootObjectField("content", newContent, true);
        fields.add(f1);
        XWootObject obj = new XWootObject("test.1", "XWikiPage", false, fields, false);
        return obj;
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithOneXWiki() throws Exception
    {
        // connect XWoot to content provider
        this.xwoot21.connectToContentManager();

        MockXWootContentProvider mxwcp = this.xwoot21.getContentManager();

        // simulate XWiki user page creation
        XWootId id = new XWootId("test.1", 10, 1, 0);
        XWootObject obj1 = this.createObject1("titi\n");
        mxwcp.addEntryInList(id, obj1);

        // synchronize xwoot
        this.xwoot21.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(obj1.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getGuid());
        Assert.assertEquals(obj1.getFieldValue("author"), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("author"));
        Assert.assertEquals(obj1.getFieldValue("title"), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("title"));
        // verify wootable field
        Assert.assertEquals("titi\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.1",
            "XWikiPage", "content"));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());
        XWootId id2 = new XWootId("test.1", 11, 1, 1);
        XWootObject obj2 = this.createObjectContentModification("toto\n");
        mxwcp.addEntryInList(id2, obj2);

        this.xwoot21.synchronize();
        // verify no-wootables fields
        Assert.assertEquals(obj1.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getGuid());
        Assert.assertEquals(obj1.getFieldValue("author"), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("author"));
        Assert.assertEquals(obj1.getFieldValue("title"), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("title"));
        // verify wootable field
        Assert.assertEquals("toto\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.1",
            "XWikiPage", "content"));
    }

    @Test
    public void testBasicWithTwoXWiki() throws Exception
    {
        Assert.assertEquals("", this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();

        // connect sites
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);
        Assert.assertTrue(this.lpbCast1.getNeighborsList().size() > 0);

        // xwoot1 : Simulate a change from XWiki1 user...
        MockXWootContentProvider mxwcp = this.xwoot21.getContentManager();
        XWootId id = new XWootId("test.1", 10, 1, 0);

        XWootObject obj1 = this.createObject1("titi\n");
        mxwcp.addEntryInList(id, obj1);
        this.xwoot21.synchronize();
        // verify no-wootables fields
        Assert.assertEquals(obj1.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getGuid());
        Assert.assertEquals(obj1.getFieldValue("author"), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("author"));
        Assert.assertEquals(obj1.getFieldValue("title"), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("title"));
        // verify wootable field
        Assert.assertEquals("titi\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.1",
            "XWikiPage", "content"));
        // verify no-wootables fields
        System.out.println(this.xwoot22.getTre().getValue(new XWootObjectIdentifier("test.1", obj1.getGuid())));
        Assert.assertEquals(obj1.getGuid(), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getGuid());
        Assert.assertEquals(obj1.getFieldValue("author"), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("author"));
        Assert.assertEquals(obj1.getFieldValue("title"), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier("test.1", obj1.getGuid())).get()).getFieldValue("title"));
        // verify wootable field
        Assert.assertEquals("titi\n", this.xwoot22.getWootEngine().getContentManager().getContent("test.1",
            "XWikiPage", "content"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test(expected = XWootException.class)
    public void testExceptionConflitBetweenVueAndModel() throws Exception
    {
        // connect XWoot to content provider
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();

        // simulate XWiki user page creation
        MockXWootContentProvider mxwcp = this.xwoot21.getContentManager();
        XWootId id = new XWootId("test.1", 10, 1, 0);

        XWootObject obj1 = this.createObject1("titi");
        mxwcp.addEntryInList(id, obj1);

        // create patch to change wootEngine model : insert "titi" in first
        // position
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootIns op0 = new WootIns(new WootRow(new WootId(0, 0), "toto"), new WootId(-1, -1), new WootId(-2, -2));
        op0.setContentId(new ContentId("test.1", "XWikiPage", "content", false));
        op0.setOpId(new WootId(0, 0));
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId("test.1");
        patch.setObjectId("XWikiPage");
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        Message mess = new Message();
        mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
        mess.setContent(patch);
        mess.setOriginalPeerId("test_Peer");
        mess.setRound(1);

        this.xwoot21.receivePatch(mess);
        this.xwoot21.synchronize();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testConflitBetweenVueAndModel() throws Exception
    {
        // connect XWoot to content provider
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();

        // simulate XWiki user page creation
        MockXWootContentProvider mxwcp = this.xwoot21.getContentManager();
        XWootId id = new XWootId("test.1", 10, 1, 0);
        XWootObject obj1 = this.createObject1("titi");
        mxwcp.addEntryInList(id, obj1);

        // create patch to change wootEngine model : insert "titi" in first
        // position
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootIns op0 = new WootIns(new WootRow(new WootId(0, 0), "toto"), new WootId(-1, -1), new WootId(-2, -2));
        op0.setContentId(new ContentId("test.1", "XWikiPage", "content", false));
        op0.setOpId(new WootId(0, 0));
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId("test.1");
        patch.setObjectId("XWikiPage");
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        // patch must contain corresponding TRE op to have the xwootObject

        XWootObject obj2 = this.createObject1("toto");
        Value tre_val = new XWootObjectValue();
        ((XWootObjectValue) tre_val).setObject(obj2);
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier("test.1", obj2.getGuid());
        ThomasRuleOp tre_op = this.xwoot21.getTre().getOp(tre_id, tre_val);
        List tre_ops = new ArrayList<ThomasRuleOp>();
        tre_ops.add(tre_op);
        patch.setMDelements(tre_ops);

        Message mess = new Message();
        mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
        mess.setContent(patch);
        mess.setOriginalPeerId("test_Peer");
        mess.setRound(1);

        this.xwoot21.receivePatch(mess);
        this.xwoot21.synchronize();

        Assert.assertEquals("toto\ntiti\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.1",
            "XWikiPage", "content"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithThreeConcurrentXWiki() throws Exception
    {

        // configure neighbors
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot23);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot23);
        this.lpbCast3.addNeighbor(this.xwoot23, this.xwoot21);
        this.lpbCast3.addNeighbor(this.xwoot23, this.xwoot22);

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();
        this.xwoot23.reconnectToP2PNetwork();
        this.xwoot23.connectToContentManager();

        // /////////////////////
        // Scenario execution
        // /////////////////////
        // simulate a change from wikiContentManager user...

        // simulate XWiki user page creation
        MockXWootContentProvider mxwcp = this.xwoot21.getContentManager();
        XWootId id = new XWootId("test.1", 10, 1, 0);
        XWootObject obj1 = this.createObject1("Ligne 1 sur xwiki1\n");
        mxwcp.addEntryInList(id, obj1);

        // Launch the synch...
        this.xwoot21.synchronize();

        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine1.getContentManager().getContent("test.1",
            "XWikiPage", "content"));

        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine2.getContentManager().getContent("test.1",
            "XWikiPage", "content"));

        Assert.assertEquals("Ligne 1 sur xwiki1\n", this.wootEngine3.getContentManager().getContent("test.1",
            "XWikiPage", "content"));

        // simulate a change from wikiContentManager user...
        XWootObject obj11 =
            this.createObjectContentModification("Ligne -1 sur xwiki1\nLigne 0 sur xwiki1\nLigne 1 sur xwiki1\n");
        XWootId id11 = new XWootId("test.1", 11, 1, 1);
        this.xwoot21.getContentManager().addEntryInList(id11, obj11);

        XWootObject obj12 =
            this.createObjectContentModification("Ligne 0 sur xwiki2\nLigne 1 sur xwiki1\nLigne 2 sur xwiki2\n");
        XWootId id12 = new XWootId("test.1", 11, 1, 1);
        this.xwoot22.getContentManager().addEntryInList(id12, obj12);

        XWootObject obj13 =
            this.createObjectContentModification("Ligne 1 sur xwiki1\nLigne 2 sur xwiki3\nLigne 3 sur xwiki3\n");
        XWootId id13 = new XWootId("test.1", 11, 1, 1);
        this.xwoot23.getContentManager().addEntryInList(id13, obj13);

        // Launch the synch...
        this.xwoot21.synchronize();
        /*
         * Assert.assertEquals(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content", false),
         * this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content", false));
         * Assert.assertEquals(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content", false),
         * this.wootEngine3.getContentManager().getContent("test.1", "XWikiPage", "content", false));
         */

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
        System.out.println("woot3 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine3.getContentManager().getContent("test.1", "XWikiPage", "content"));
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
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();

        // /////////////////////
        // Scenario execution
        // /////////////////////
        // simulate a change from wikiContentManager user...
        XWootId id1 = new XWootId("test.1", 10, 1, 0);
        XWootObject obj1 = this.createObject1("Ligne 1 sur xwiki1\n");
        this.xwoot21.getContentManager().addEntryInList(id1, obj1);
        XWootId id2 = new XWootId("test.1", 10, 1, 0);
        XWootObject obj2 = this.createObject1("Ligne 1 sur xwiki2\n");
        this.xwoot22.getContentManager().addEntryInList(id2, obj2);

        // create patch to change wootEngine model : insert "titi" in first
        // position
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootIns op0 =
            new WootIns(new WootRow(new WootId(0, 0), "Ligne 1 sur xwiki fantôme"), new WootId(-1, -1), new WootId(-2,
                -2));
        op0.setContentId(new ContentId("test.1", "XWikiPage", "content", false));
        op0.setOpId(new WootId(0, 0));
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId("test.1");
        patch.setObjectId("XWikiPage");
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        // patch must contain corresponding TRE op to have the xwootObject

        XWootObject obj3 = this.createObject1("Cette valeur est ecrasée par le contenu du wootEngine\n");
        Value tre_val = new XWootObjectValue();
        ((XWootObjectValue) tre_val).setObject(obj3);
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier("test.1", obj3.getGuid());
        ThomasRuleOp tre_op = this.xwoot21.getTre().getOp(tre_id, tre_val);
        List tre_ops = new ArrayList<ThomasRuleOp>();
        tre_ops.add(tre_op);
        patch.setMDelements(tre_ops);
        Message mess = new Message();
        mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
        mess.setContent(patch);
        mess.setOriginalPeerId("test_Peer");
        mess.setRound(1);

        this.xwoot21.receivePatch(mess);
        this.xwoot22.receivePatch(mess);

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");

        /*
         * Assert.assertEquals("Ligne 1 sur xwiki1\n",
         * this.xwoot21.getWootEngine().getContentManager().getContent("test.1", "XWikiPage","content",false));
         * Assert.assertEquals("Ligne 1 sur xwiki1\n",
         * this.xwoot22.getWootEngine().getContentManager().getContent("test.1", "XWikiPage","content",false));
         */

        // simulate a change from wikiContentManager user...
        XWootObject obj4 = this.createObjectContentModification("Nouvelle ligne sur xwiki1\nLigne 1 sur xwiki1\n");
        XWootObject obj5 = this.createObjectContentModification("Ligne 1 sur xwiki1\nNouvelle ligne sur xwiki2\n");
        XWootId id4 = new XWootId("test.1", 11, 1, 1);
        XWootId id5 = new XWootId("test.1", 11, 1, 1);

        this.xwoot21.getContentManager().addEntryInList(id4, obj4);
        this.xwoot22.getContentManager().addEntryInList(id5, obj5);

        this.xwoot21.synchronize();
        //
        // Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
        // XWoot.PAGECONTENTFIELDID), this.wootEngine2.getContentManager().getContent(page.getPageName(),
        // XWoot.PAGEOBJECTID, XWoot.PAGECONTENTFIELDID));
        // Assert.assertEquals(this.xwiki1.getPageContent(page.getPageName()), this.xwiki2.getPageContent(page
        // .getPageName()));
        // Assert.assertEquals(this.wootEngine1.getContentManager().getContent(page.getPageName(), XWoot.PAGEOBJECTID,
        // XWoot.PAGECONTENTFIELDID), this.xwiki1
        // .getPageContent(page.getPageName()));

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
    }

}
