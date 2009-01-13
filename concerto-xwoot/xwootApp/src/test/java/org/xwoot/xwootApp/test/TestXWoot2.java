package org.xwoot.xwootApp.test;

import java.io.File;
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
import org.xwoot.wikiContentManager.XWikiSwizzleClient.XwikiSwizzleClient;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootApp.XWoot2;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.XWootObjectValue;

public class TestXWoot2 extends AbstractXWootTest
{

    private XWootObject createObject1(String pageId,String content)
    {
        List fields = new ArrayList<XWootObjectField>();
        XWootObjectField f1 = new XWootObjectField("content", content, true);
        XWootObjectField f2 = new XWootObjectField("title", "Page de test", false);
        XWootObjectField f3 = new XWootObjectField("author", "Terminator", false);
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        XWootObject obj = new XWootObject(pageId, 1, 0, "XWikiPage", false, fields, true);
        return obj;
    }

    private XWootObject createObjectContentModification(String pageId,String newContent)
    {
        List fields = new ArrayList<XWootObjectField>();
        XWootObjectField f1 = new XWootObjectField("content", newContent, true);
        fields.add(f1);
        XWootObject obj = new XWootObject(pageId, 1, 0, "XWikiPage", false, fields, false);
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
        XWootObject obj1 = this.createObject1("test.1","titi\n");
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
        XWootObject obj2 = this.createObjectContentModification("test.1","toto\n");
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

        XWootObject obj1 = this.createObject1("test.1","titi\n");
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

        XWootObject obj1 = this.createObject1("test.1","titi");
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
        XWootObject obj1 = this.createObject1("test.1","titi");
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

        XWootObject obj2 = this.createObject1("test.1","toto");
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
        XWootObject obj1 = this.createObject1("test.1","Ligne 1 sur xwiki1\n");
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
            this.createObjectContentModification("test.1","Ligne -1 sur xwiki1\nLigne 0 sur xwiki1\nLigne 1 sur xwiki1\n");
        XWootId id11 = new XWootId("test.1", 11, 1, 1);
        this.xwoot21.getContentManager().addEntryInList(id11, obj11);

        XWootObject obj12 =
            this.createObjectContentModification("test.1","Ligne 0 sur xwiki2\nLigne 1 sur xwiki1\nLigne 2 sur xwiki2\n");
        XWootId id12 = new XWootId("test.1", 11, 1, 1);
        this.xwoot22.getContentManager().addEntryInList(id12, obj12);

        XWootObject obj13 =
            this.createObjectContentModification("test.1","Ligne 1 sur xwiki1\nLigne 2 sur xwiki3\nLigne 3 sur xwiki3\n");
        XWootId id13 = new XWootId("test.1", 11, 1, 1);
        this.xwoot23.getContentManager().addEntryInList(id13, obj13);

        // Launch the synch...
        this.xwoot21.synchronize();
      
        Assert.assertEquals("Ligne -1 sur xwiki1\n" +
        		"Ligne 0 sur xwiki1\n" +
        		"Ligne 0 sur xwiki2\n" +
        		"Ligne 1 sur xwiki1\n" +
        		"Ligne 2 sur xwiki2\n" +
        		"Ligne 2 sur xwiki3\n" +
        		"Ligne 3 sur xwiki3\n",
        		this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        Assert.assertEquals(
            this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"),
            this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"));
        Assert.assertEquals(
            this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"),
            this.wootEngine3.getContentManager().getContent("test.1", "XWikiPage", "content"));
        

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
        XWootObject obj1 = this.createObject1("test.1","Ligne 1 sur xwiki1\n");
        this.xwoot21.getContentManager().addEntryInList(id1, obj1);
        XWootId id2 = new XWootId("test.1", 10, 1, 0);
        XWootObject obj2 = this.createObject1("test.1","Ligne 1 sur xwiki2\n");
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

        XWootObject obj3 = this.createObject1("test.1","Cette valeur est ecrasée par le contenu du wootEngine\n");
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

        Assert.assertEquals("Ligne 1 sur xwiki fantôme\nLigne 1 sur xwiki1\nLigne 1 sur xwiki2\n",
            this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"),
            this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content") );
        
        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");

        // simulate a change from wikiContentManager user...
        XWootObject obj4 = this.createObjectContentModification("test.1","Nouvelle ligne sur xwiki1\nLigne 1 sur xwiki1\n");
        XWootObject obj5 = this.createObjectContentModification("test.1","Ligne 1 sur xwiki1\nNouvelle ligne sur xwiki2\n");
        XWootId id4 = new XWootId("test.1", 11, 1, 1);
        XWootId id5 = new XWootId("test.1", 11, 1, 1);

        this.xwoot21.getContentManager().addEntryInList(id4, obj4);
        this.xwoot22.getContentManager().addEntryInList(id5, obj5);

        this.xwoot21.synchronize();
        
        Assert.assertEquals("Nouvelle ligne sur xwiki1\nLigne 1 sur xwiki1\nNouvelle ligne sur xwiki2\n",
            this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"),
            this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content") );

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent("test.1", "XWikiPage", "content"));
        System.out.println("-------------------");
    }

    @Test(expected = XWootException.class)
    public void testXWoot1() throws Exception
    {
        this.xwoot21 =
            new XWoot2(this.xwiki1, this.wootEngine1, this.lpbCast1, "/cantBecreated" + File.separator + "Site1", "Site1",
                new Integer(1), this.tre1, this.ae1);
    }

    @Test(expected = RuntimeException.class)
    public void testXWoot2() throws Exception
    {
        File f = new File(WORKINGDIR + File.separatorChar + "file.tmp");
        f.delete();
        Assert.assertFalse(f.exists());
        f.createNewFile();
        Assert.assertTrue(f.exists());
        this.xwoot21 =
            new XWoot2(this.xwiki1, this.wootEngine1, this.lpbCast1, f.toString(), "Site 1", new Integer(1),
                this.tre1, this.ae1);
    }

    @Test(expected = XWootException.class)
    public void testXWoot3() throws Exception
    {
        File f = new File(WORKINGDIR + File.separatorChar + "folder");
        f.delete();
        Assert.assertFalse(f.exists());
        f.mkdir();
        Assert.assertTrue(f.exists());
        f.setReadOnly();
        Assert.assertFalse(f.canWrite());
        this.xwoot21 =
            new XWoot2(this.xwiki1, this.wootEngine1, this.lpbCast1, f.toString(), "Site 1", new Integer(1),
                this.tre1, this.ae1);
    }

    @Test
    public void testXWoot4() throws Exception
    {
        File f = new File(WORKINGDIR);
        f.mkdir();
        Assert.assertTrue(f.exists());
        Assert.assertFalse(this.lpbCast1.isSenderConnected());
        this.lpbCast1.connectSender();
        Assert.assertTrue(this.lpbCast1.isSenderConnected());
        this.xwoot21 =
            new XWoot2(this.xwiki1, this.wootEngine1, this.lpbCast1, f.toString(), f.toString() + File.separator
                + "Site1", new Integer(1), this.tre1, this.ae1);
        Assert.assertNotNull(this.xwoot21);
    }

    @Test
    public void testConnection() throws Exception
    {
        // receiver = p2pNetwork
        Assert.assertFalse(this.xwoot21.isConnectedToP2PNetwork());
        this.xwoot21.reconnectToP2PNetwork();
        Assert.assertTrue(this.xwoot21.isConnectedToP2PNetwork());
        this.xwoot21.disconnectFromP2PNetwork();
        Assert.assertFalse(this.xwoot21.isConnectedToP2PNetwork());
        this.xwoot21.reconnectToP2PNetwork();
        Assert.assertTrue(this.xwoot21.isConnectedToP2PNetwork());
        this.xwoot21.reconnectToP2PNetwork();
        Assert.assertTrue(this.xwoot21.isConnectedToP2PNetwork());

        // content provider
        Assert.assertFalse(this.xwoot21.isContentManagerConnected());
        this.xwoot21.connectToContentManager();
        Assert.assertTrue(this.xwoot21.isContentManagerConnected());
        this.xwoot21.disconnectFromContentManager();
        Assert.assertFalse(this.xwoot21.isContentManagerConnected());
    }

    // @Test
    // public void testComputeState() {
    // fail("Not yet implemented");
    // }

    // @Test
    // public void testInitialiseWootStorage() {
    // fail("Not yet implemented");
    // }

    @Test
    public void testJoinNetwork()
    {
        // void
    }

    //
    // @Test
    // public void testReceive() {
    // fail("Not yet implemented");
    // }

    //
    // @Test
    // public void testRemoveNeighbour() {
    // fail("Not yet implemented");
    // }

    // With Mock it's ok but with real XWiki Content Provider this test take few
    // minutes ...
    @Test
    public void testState() throws Exception
    {

        if (this.xwiki1 instanceof XwikiSwizzleClient) {
            return;
        }
        
        this.xwoot21.connectToContentManager();
        
        // simulate XWiki user page creation
        MockXWootContentProvider mxwcp = this.xwoot21.getContentManager();
        XWootId id = new XWootId("test.1", 10, 1, 0);
        XWootObject obj1 = this.createObject1("test.1","toto\n");
        mxwcp.addEntryInList(id, obj1);
      
        XWootId id2 = new XWootId("test.2", 10, 1, 0);
        XWootObject obj2 = this.createObject1("test.2","titi\n");
        mxwcp.addEntryInList(id2, obj2);
        
        XWootId id3 = new XWootId("test.3", 10, 1, 0);
        XWootObject obj3 = this.createObject1("test.3","tata\n");
        mxwcp.addEntryInList(id3, obj3);

        this.xwoot21.createNetwork();
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.synchronize();
        
        System.out.println(this.xwoot21.getWootEngine().getContentManager().listPages()[0]);
        Assert.assertEquals(3,this.xwoot21.getWootEngine().getContentManager().listPages().length);
        Assert.assertEquals("toto\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.1", "XWikiPage", "content"));
        Assert.assertEquals("titi\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.2", "XWikiPage", "content"));
        Assert.assertEquals("tata\n", this.xwoot21.getWootEngine().getContentManager().getContent("test.3", "XWikiPage", "content"));
        
        File f = this.xwoot21.computeState();
        Assert.assertNotNull(f);
        this.xwoot22.joinNetwork("");
        this.xwoot22.importState(f);
        this.xwoot22.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        Assert.assertEquals(3,this.xwoot22.getWootEngine().getContentManager().listPages().length);
        Assert.assertEquals("toto\n", this.xwoot22.getWootEngine().getContentManager().getContent("test.1", "XWikiPage", "content"));
        Assert.assertEquals("titi\n", this.xwoot22.getWootEngine().getContentManager().getContent("test.2", "XWikiPage", "content"));
        Assert.assertEquals("tata\n", this.xwoot22.getWootEngine().getContentManager().getContent("test.3", "XWikiPage", "content"));
        
    }
}
