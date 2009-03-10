package org.xwoot.xwootApp.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.MockXWootContentProvider;
import org.xwoot.NewXWootContentProvider;
import org.xwoot.XWootContentProviderException;
import org.xwoot.XWootContentProviderInterface;
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
import org.xwoot.xwootApp.XWoot2;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.XWootObjectValue;

public class TestXWoot2 extends AbstractXWootTest
{
    
    private static final String commentContentField = "comment";

    // un XWootObject peut etre de 3 type :
    // … pages
    // … xwiki objets cumulable
    // … xwiki objets non cumulabe

    // Guid :
    // page:PageId
    // object:guid
    // object:page:class[number]

    private void initContentProvider(XWootContentProviderInterface i)
    {
        //author : Conan the barbarian 
        if (i instanceof NewXWootContentProvider) {
            /*try {
                ((XWootContentProvider) i).getRpc().removePage("test.WebHome");
            } catch (XmlRpcException e) {
                System.out.println(i.toString() + " : test.WebHome doesn't exist.");
            }
            try {
                ((XWootContentProvider) i).getRpc().removePage("test.1");
            } catch (XmlRpcException e1) {
                System.out.println(i.toString() + " : test.1 doesn't exist.");
            }
            try {
                ((XWootContentProvider) i).getRpc().removePage("test.2");

            } catch (XmlRpcException e2) {
                System.out.println(i.toString() + " : test.2 doesn't exist.");
            }
            try {
                ((XWootContentProvider) i).getRpc().removePage("test.3");
            } catch (XmlRpcException e3) {
                System.out.println(i.toString() + " : test.3 doesn't exist.");
            }
            try {
                ((XWootContentProvider) i).getRpc().removePage("test.final");
            } catch (XmlRpcException e3) {
                System.out.println(i.toString() + " : test.final doesn't exist.");
            }*/
            try {
                ((NewXWootContentProvider) i).getRpc().removeSpace("test");
            } catch (XmlRpcException e) {
                System.out.println(i.toString() + " : space 'test' doesn't exist.");
            }
        }
    }

    private XWootObject createObject(String pageId, String content, int major, int minor, boolean newlyCreated)
    {
        List fields = new ArrayList<XWootObjectField>();
        XWootObjectField f1 = new XWootObjectField("content", content, true);
        XWootObjectField f2 = new XWootObjectField("title", "", false);
        XWootObjectField f3 = new XWootObjectField("author", "Terminator", false);
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        XWootObject obj =
            new XWootObject(pageId, Integer.valueOf(major), Integer.valueOf(minor), "page:" + pageId, false, fields,
                newlyCreated);
        return obj;
    }

    private XWootObject simulateXWikiUserModification(XWootContentProviderInterface i, String pageId, String content,
        int version, int minorVersion, boolean isNewlyCreated) throws XmlRpcException
    {

        XWootObject obj = this.createObject(pageId, content, version, minorVersion, isNewlyCreated);

        if (i instanceof NewXWootContentProvider) {
            XWikiXmlRpcClient rpc = ((NewXWootContentProvider) i).getRpc();
            XWikiPage page = new XWikiPage();
            page.setId(obj.getPageId());
            page.setContent((String) obj.getFieldValue("content"));
            page.setTitle("");
            page = rpc.storePage(page);
            System.out.println(page);
        } else if (i instanceof MockXWootContentProvider) {
            XWootId id = new XWootId(pageId, 10000, version, minorVersion);
            ((MockXWootContentProvider) i).addEntryInList(id, obj);
        } else {
            return null;
        }
        return obj;
    }
    
    private XWootObject createComment(String pageId, String content, int major, int minor, boolean newlyCreated)
    {
        List fields = new ArrayList<XWootObjectField>();
        XWootObjectField f1 = new XWootObjectField(commentContentField, content, true);
        XWootObjectField f2 = new XWootObjectField("author", "Terminator", false);
        fields.add(f1);
        fields.add(f2);
        XWootObject obj =
            new XWootObject(pageId, Integer.valueOf(major), Integer.valueOf(minor), "object:" + UUID.randomUUID(), true, fields,
                newlyCreated);
        return obj;
    }

    private XWootObject simulateXWikiUserCreateXWikiComment(XWootContentProviderInterface xwiki, String pageId, String commentContent,
        int version, int minorVersion, boolean isNewlyCreated) throws Exception 
        {

        XWootObject obj = this.createComment(pageId, commentContent, version, minorVersion, isNewlyCreated);

        if (xwiki instanceof NewXWootContentProvider) {
            NewXWootContentProvider contentProvider = (NewXWootContentProvider) xwiki;
            XWikiXmlRpcClient rpc = contentProvider.getRpc();
            
            XWikiPage page = null;
            try {
                page = rpc.getPage(pageId);
            } catch (XmlRpcException e) {
                // page does not exist
            }
            
            if (page == null) {
                // We need a pageId to exist in order to create a comment, so create a page.
                page = new XWikiPage();
                page.setId(pageId);
                page.setContent("");
                page = rpc.storePage(page);
                
                // clear the modification generated by the new page. We are interested only in object in a page replication right now.
                contentProvider.getModifiedPagesIds();
                contentProvider.clearAllModifications();
            }
            
            XWikiObject comment = new XWikiObject();
            comment.setClassName("XWiki.XWikiComments");
            comment.setGuid(obj.getGuid().substring(obj.getGuid().indexOf(":") + 1));
            comment.setPageId(pageId);
            comment.setProperty("comment", commentContent);
            comment.setProperty("author", obj.getFieldValue("author"));
            
            comment = rpc.storeObject(comment);
            System.out.println("Simulated and stored xwiki object: " + comment);
        } else if (xwiki instanceof MockXWootContentProvider) {
            XWootId id = new XWootId(pageId, 10000, version, minorVersion);
            ((MockXWootContentProvider) xwiki).addEntryInList(id, obj);
        } else {
            return null;
        }
        
        return obj;
    }

    private void cleanWikis() throws XWootContentProviderException
    { //author : Conan the barbarian 
        this.xwiki21.login("Admin", "admin");
        this.initContentProvider(this.xwiki21);
        this.xwiki21.logout();
        this.xwiki22.login("Admin", "admin");
        this.initContentProvider(this.xwiki22);
        this.xwiki22.logout();
        this.xwiki23.login("Admin", "admin");
        this.initContentProvider(this.xwiki23);
        this.xwiki23.logout();
    }

    @Before
    public void start() throws XWootContentProviderException, InterruptedException
    { //author : Conan the barbarian 
        this.cleanWikis();
        Thread.sleep(100);
    }

    @After
    public void end() throws XWootContentProviderException, InterruptedException
    { //author : Conan the barbarian 
        this.cleanWikis();
        Thread.sleep(100);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithOneXWiki() throws Exception
    {
        String pageId = "test.1";
        String pageGuid = "page:" + pageId;
        String content = "titi\n";
        String content2 = "toto\n";
        // connect XWoot to content provider
        this.xwoot21.connectToContentManager();

        XWootContentProviderInterface mxwcp = this.xwoot21.getContentManager();
        this.initContentProvider(mxwcp);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp.getModifiedPagesIds();
        mxwcp.clearAllModifications();

        // simulate XWiki user page creation
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp, pageId, content, 1, 0, true);

        // synchronize xwoot
        this.xwoot21.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(),
            ((XWootObject) this.xwoot21.getTre().getValue(new XWootObjectIdentifier(pageGuid))
                .get())
                    .getGuid());

        // verify wootable field
        Assert.assertEquals(content, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());

        XWootObject xwootObj2 = this.simulateXWikiUserModification(mxwcp, pageId, content2, 2, 0, false);

        this.xwoot21.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj2.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(content2, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));
    }
    
    /**
     * Add a comment, synch, check if it exists correctly in the model.
     * Then create 2 more comments, synch, check if they are correctly in the model.
     * 
     * Last, check if comment1 is still correctly in the model after the previous comments were added.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCommentWithOneXWiki() throws Exception
    {
        String pageId = "test.1";
        String commentContent1 = "A First Comment!\n";
        String commentContent2 = "A Second Comment!\n";
        String commentContent3 = "A third Comment!\n";
        
        // connect XWoot to content provider
        this.xwoot21.connectToContentManager();

        XWootContentProviderInterface mxwcp = this.xwoot21.getContentManager();
        this.initContentProvider(mxwcp);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp.getModifiedPagesIds();
        mxwcp.clearAllModifications();

        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
        //this.xwiki21.getModifiedPagesIds();
        
        // Add a comment on the page.
        XWootObject comment1 = this.simulateXWikiUserCreateXWikiComment(mxwcp, pageId, commentContent1, 1, 0, true);
        
        //// Make sure our simulateXWikiUserCreateXWikiComment generates only one change event (the new comment).
        //Assert.assertEquals(1, this.xwoot21.getContentManager().getModifiedPagesIds().size());
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(this.actionsGeneratedModifications(mxwcp, 1, 10));
        
        // synch
        this.xwoot21.synchronize();
        
        // verify no-wootables fields
        Assert.assertEquals(comment1.getGuid(), ((XWootObject) this.xwoot21.getTre()
            .getValue(new XWootObjectIdentifier(comment1.getGuid()))
                .get())
                    .getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent1, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment1.getGuid(),
            commentContentField));
        
        // Create 2 comments.
        XWootObject comment2 = this.simulateXWikiUserCreateXWikiComment(mxwcp, pageId, commentContent2, 1, 0, true);
        XWootObject comment3 = this.simulateXWikiUserCreateXWikiComment(mxwcp, pageId, commentContent3, 1, 0, true);
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(this.actionsGeneratedModifications(mxwcp, 2, 10));
        
        // then synch.
        this.xwoot21.synchronize();
     
        // verify no-wootables fields
        Assert.assertEquals(comment2.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(comment2.getGuid()))
                .get())
                    .getGuid());
        Assert.assertEquals(comment3.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(comment3.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent2, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment2.getGuid(),
            commentContentField));
        Assert.assertEquals(commentContent3, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment3.getGuid(),
            commentContentField));
        
        // Is comment1 still there?
        
        // verify no-wootables fields
        Assert.assertEquals(comment1.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(comment1.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent1, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment1.getGuid(),
            commentContentField));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithTwoXWiki() throws Exception
    {
        String pageId = "test.1";
        String pageGuid = "page:" + pageId;
        String content = "titi\n";

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();

        // connect sites
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);
        Assert.assertTrue(this.lpbCast1.getNeighborsList().size() > 0);
        Assert.assertTrue(this.lpbCast2.getNeighborsList().size() > 0);

        XWootContentProviderInterface mxwcp1 = this.xwoot21.getContentManager();
        XWootContentProviderInterface mxwcp2 = this.xwoot22.getContentManager();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, pageId, content, 1, 0, true);

        // synchronize xwoot
        this.xwoot21.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(content, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));
        Assert.assertEquals(content, this.xwoot22.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());
    }
    
    /**
     * Make sure that your previous actions generated modifications in the contentProvider.
     * </p>
     * This is a workaround for the bug in the content provider.
     * @FIXME: not working.
     * 
     * @param xwiki the content provider to test.
     * @param numberOfExpectedModifications
     * @return true if the expectedNumberOfModifications are returned by the contentProvider.
     * @throws XWootContentProviderException if problems occur.
     */
    private boolean actionsGeneratedModifications(XWootContentProviderInterface xwiki, int numberOfExpectedModifications, int numberOfTries) throws XWootContentProviderException {
        boolean modificationsGenerated = false;
        for (int tries = 0; tries < numberOfTries; tries++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            
            int actualModifications = xwiki.getModifiedPagesIds().size();
            System.out.println("Try " + tries + " : " + actualModifications + "/" + numberOfExpectedModifications);
            if (actualModifications == numberOfExpectedModifications) {
                modificationsGenerated = true;
                System.out.println("Success.");
                break;
            }
        }
        
        return modificationsGenerated;
    }
    
    /**
     * Create a comment on xwiki1, then synchronize. Create 2 more comments on the xwiki2 then synch.
     * Result:
     * Both xwikis will have the same comments.
     * 
     * @throws if problems occur.
     */
    @Test
    public void testCommentsWithTwoXWiki() throws Exception
    {
        String pageId = "test.1";
        String commentContent1 = "A First Comment!\n";
        String commentContent2 = "A Second Comment!\n";
        String commentContent3 = "A third Comment!\n";

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();

        // connect sites
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);
        Assert.assertEquals(1, this.lpbCast1.getNeighborsList().size());
        Assert.assertEquals(1, this.lpbCast2.getNeighborsList().size());

        XWootContentProviderInterface mxwcp1 = this.xwoot21.getContentManager();
        XWootContentProviderInterface mxwcp2 = this.xwoot22.getContentManager();
        
        /*this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);*/

        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();
        
        // simulate XWiki user create comment on xw1.
        XWootObject comment1XW1 = this.simulateXWikiUserCreateXWikiComment(this.xwiki21, pageId, commentContent1, 1, 0, true);
        
        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
        //this.xwiki21.getModifiedPagesIds();
        
        // Make sure our simulateXWikiUserCreateXWikiComment generates only one change event (the new comment).
        //Assert.assertEquals(1, this.xwiki21.getModifiedPagesIds().size());
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(actionsGeneratedModifications(this.xwiki21, 1, 10));

        // synchronize xw1
        this.xwoot21.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(comment1XW1.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(comment1XW1.getGuid())).get()).getGuid());
        Assert.assertEquals(comment1XW1.getGuid(), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier(comment1XW1.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent1, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment1XW1.getGuid(),
            commentContentField));
        Assert.assertEquals(commentContent1, this.xwoot22.getWootEngine().getContentManager().getContent(pageId, comment1XW1.getGuid(),
            commentContentField));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());
        
        
        // Create 2 more comments on xw2.
        XWootObject comment2XW2 = this.simulateXWikiUserCreateXWikiComment(this.xwiki22, pageId, commentContent2, 1, 0, true);
        XWootObject comment3XW2 = this.simulateXWikiUserCreateXWikiComment(this.xwiki22, pageId, commentContent3, 1, 0, true);
        
        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
       // this.xwiki22.getModifiedPagesIds();
        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
       // this.xwiki22.getModifiedPagesIds();
        
        // Make sure our simulateXWikiUserCreateXWikiComment generates only two change event (the 2 new comments).
       // Assert.assertEquals(2, this.xwiki22.getModifiedPagesIds().size());
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(actionsGeneratedModifications(this.xwiki22, 2, 10));
        
        // synch xw2
        this.xwoot22.synchronize();
        
        // verify no-wootables fields
        Assert.assertEquals(comment2XW2.getGuid(), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier(comment2XW2.getGuid())).get()).getGuid());
        Assert.assertEquals(comment2XW2.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(comment2XW2.getGuid())).get()).getGuid());
        
        Assert.assertEquals(comment3XW2.getGuid(), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier(comment3XW2.getGuid())).get()).getGuid());
        Assert.assertEquals(comment3XW2.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(comment3XW2.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent2, this.xwoot22.getWootEngine().getContentManager().getContent(pageId, comment2XW2.getGuid(),
            commentContentField));
        Assert.assertEquals(commentContent2, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment2XW2.getGuid(),
            commentContentField));
        
        Assert.assertEquals(commentContent3, this.xwoot22.getWootEngine().getContentManager().getContent(pageId, comment3XW2.getGuid(),
            commentContentField));
        Assert.assertEquals(commentContent3, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, comment3XW2.getGuid(),
            commentContentField));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicConflictBetweenTwoXWiki() throws Exception
    {
        String pageId = "test.1";
        String pageGuid = "page:" + pageId;
        String content = "titi\n";
        String result = "titi\ntiti\n";

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();

        // connect sites
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);
        Assert.assertTrue(this.lpbCast1.getNeighborsList().size() > 0);
        Assert.assertTrue(this.lpbCast2.getNeighborsList().size() > 0);

        XWootContentProviderInterface mxwcp1 = this.xwoot21.getContentManager();
        XWootContentProviderInterface mxwcp2 = this.xwoot22.getContentManager();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, pageId, content, 1, 0, true);
        XWootObject xwootObj2 = this.simulateXWikiUserModification(mxwcp2, pageId, content, 1, 0, true);
        Assert.assertEquals(xwootObj, xwootObj2);

        // synchronize xwoot
        this.xwoot21.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot22.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(result, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));
        Assert.assertEquals(result, this.xwoot22.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testConflitBetweenVueAndModel() throws Exception
    {
        String pageId = "test.1";
        String pageGuid = "page:" + pageId;
        String content = "toto";
        String content2 = "titi";
        String result = "titi\ntoto\n";

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();

        XWootContentProviderInterface mxwcp = this.xwoot21.getContentManager();
        
        this.initContentProvider(mxwcp);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp.getModifiedPagesIds();
        mxwcp.clearAllModifications();

        // simulate XWiki user page creation
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp, pageId, content, 1, 0, true);

        // create patch to change wootEngine model : insert "titi" in first
        // position
        // add wootable content in patch
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootId wootId = new WootId(String.valueOf(0), 0);
        WootIns op0 = new WootIns(new WootRow(wootId, content2), WootId.FIRST_WOOT_ID, wootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(pageId, pageGuid, "content", false));
        op0.setOpId(wootId);
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId(pageId);
        patch.setObjectId(pageGuid);
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        // patch must contain corresponding TRE op to have the xwootObject
        // add no wootable content in patch
        XWootObject obj2 = this.createObject(pageId, content2, 1, 0, true);
        Value tre_val = new XWootObjectValue();
        ((XWootObjectValue) tre_val).setObject(obj2);
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier(obj2.getGuid());
        ThomasRuleOp tre_op = this.xwoot21.getTre().getOp(tre_id, tre_val);
        List tre_ops = new ArrayList<ThomasRuleOp>();
        tre_ops.add(tre_op);
        patch.setMDelements(tre_ops);

        Message mess = new Message();
        mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
        mess.setContent(patch);
        mess.setOriginalPeerId("test_Peer");
        mess.setRound(1);

        this.xwoot21.receiveMessage(mess);

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot21.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(result, this.xwoot21.getWootEngine().getContentManager().getContent(pageId, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot21.getContentManager().getModifiedPagesIds().size());
    }

    // /**
    // * DOCUMENT ME!
    // *
    // * @throws Exception DOCUMENT ME!
    // */
    // @Test
    // public void testConflitBetweenVueAndModel() throws Exception
    // {
    // // connect XWoot to content provider
    // this.xwoot21.reconnectToP2PNetwork();
    // this.xwoot21.connectToContentManager();
    //
    // String pageName="test.1";
    //
    // // simulate XWiki user page creation
    // XWootContentProviderInterface mxwcp = this.xwoot21.getContentManager();
    // XWootId id = new XWootId(pageName, 10, 1, 0);
    // XWootObject obj1 = this.createObject(pageName,"titi",1,0,true);
    // this.simulateXWikiUserModification(mxwcp, id, obj1);
    //
    // // create patch to change wootEngine model : insert "titi" in first
    // // position
    // Patch patch = new Patch();
    // List<WootOp> vector = new ArrayList<WootOp>();
    // WootIns op0 = new WootIns(new WootRow(new WootId(0, 0), "toto"), new WootId(-1, -1), new WootId(-2, -2));
    // op0.setContentId(new ContentId(pageName, "page:test.1", "content", false));
    // op0.setOpId(new WootId(0, 0));
    // vector.add(op0);
    // patch.setData(vector);
    // patch.setPageId(pageName);
    // patch.setObjectId("page:test.1");
    // patch.setTimestamp(10);
    // patch.setVersion(0);
    // patch.setMinorVersion(1);
    //
    // // patch must contain corresponding TRE op to have the xwootObject
    //
    // XWootObject obj2 = this.createObject(pageName,"toto",0,1,true);
    // Value tre_val = new XWootObjectValue();
    // ((XWootObjectValue) tre_val).setObject(obj2);
    // XWootObjectIdentifier tre_id = new XWootObjectIdentifier(obj2.getGuid());
    // ThomasRuleOp tre_op = this.xwoot21.getTre().getOp(tre_id, tre_val);
    // List tre_ops = new ArrayList<ThomasRuleOp>();
    // tre_ops.add(tre_op);
    // patch.setMDelements(tre_ops);
    //
    // Message mess = new Message();
    // mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
    // mess.setContent(patch);
    // mess.setOriginalPeerId("test_Peer");
    // mess.setRound(1);
    //
    // this.xwoot21.receivePatch(mess);
    //
    // Assert.assertEquals("toto\ntiti\n", this.xwoot21.getWootEngine().getContentManager().getContent(pageName,
    // "page:test.1", "content"));
    // }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithTwoConcurrentXWiki() throws Exception
    {
        String pageId = "test.1";
        String pageGuid = "page:" + pageId;
        String content1 = "Ligne1 sur XWiki1\n";
        String content2 = "Ligne1 sur XWiki2\n";
        String content3 = "Ligne 1 sur xwiki fantôme";
        String result1 = "Ligne 1 sur xwiki fantôme\nLigne1 sur XWiki1\nLigne1 sur XWiki2\n";
        String content4 = "Nouvelle ligne sur xwiki1\nLigne1 sur XWiki1\n";
        String content5 = "Ligne1 sur XWiki1\nNouvelle ligne sur xwiki2\n";
        String result2 = "Nouvelle ligne sur xwiki1\nLigne1 sur XWiki1\nNouvelle ligne sur xwiki2\n";

        // connect XWoot
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        this.xwoot22.connectToContentManager();

        // connect sites
        this.lpbCast1.addNeighbor(this.xwoot21, this.xwoot22);
        this.lpbCast2.addNeighbor(this.xwoot22, this.xwoot21);
        Assert.assertTrue(this.lpbCast1.getNeighborsList().size() > 0);
        Assert.assertTrue(this.lpbCast2.getNeighborsList().size() > 0);

        XWootContentProviderInterface mxwcp1 = this.xwoot21.getContentManager();
        XWootContentProviderInterface mxwcp2 = this.xwoot22.getContentManager();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation
        this.simulateXWikiUserModification(mxwcp1, pageId, content1, 1, 0, true);
        this.simulateXWikiUserModification(mxwcp2, pageId, content2, 1, 0, true);

        // create patch to change wootEngine model : insert "Ligne 1 sur xwiki fantôme" in first
        // position
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootId wootId = new WootId(String.valueOf(0), 0);
        WootIns op0 = new WootIns(new WootRow(wootId, content3), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(pageId, pageGuid, "content", false));
        op0.setOpId(wootId);
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId(pageId);
        patch.setObjectId(pageGuid);
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        // patch must contain corresponding TRE op to have the xwootObject

        XWootObject obj3 =
            this.createObject(pageId, "Cette valeur est ecrasée par le contenu du wootEngine\n", 1, 0, true);
        Value tre_val = new XWootObjectValue();
        ((XWootObjectValue) tre_val).setObject(obj3);
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier(obj3.getGuid());
        ThomasRuleOp tre_op = this.xwoot21.getTre().getOp(tre_id, tre_val);
        List tre_ops = new ArrayList<ThomasRuleOp>();
        tre_ops.add(tre_op);
        patch.setMDelements(tre_ops);
        Message mess = new Message();
        mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
        mess.setContent(patch);
        mess.setOriginalPeerId("test_Peer");
        mess.setRound(1);

        this.xwoot21.receiveMessage(mess);
        this.xwoot22.receiveMessage(mess);

        Assert.assertEquals(result1, this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"),
            this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");

        this.simulateXWikiUserModification(mxwcp1, pageId, content4, 2, 0, false);
        this.simulateXWikiUserModification(mxwcp2, pageId, content5, 2, 0, false);

        this.xwoot21.synchronize();

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");

        Assert.assertEquals(result2, this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"),
            this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithThreeConcurrentXWiki() throws Exception
    {
        String pageId = "test.final";
        String pageGuid = "page:" + pageId;
        String content1 = "Ligne 1 sur xwiki1\n";
        String content2 = "Ligne -1 sur xwiki1\nLigne 0 sur xwiki1\nLigne 1 sur xwiki1\n";
        String content3 = "Ligne 0 sur xwiki2\nLigne 1 sur xwiki1\nLigne 2 sur xwiki2\n";
        String content4 = "Ligne 1 sur xwiki1\nLigne 2 sur xwiki3\nLigne 3 sur xwiki3\n";
        String result =
            "Ligne -1 sur xwiki1\n" + "Ligne 0 sur xwiki1\n" + "Ligne 0 sur xwiki2\n" + "Ligne 1 sur xwiki1\n"
                + "Ligne 2 sur xwiki2\n" + "Ligne 2 sur xwiki3\n" + "Ligne 3 sur xwiki3\n";

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
        XWootContentProviderInterface mxwcp1 = this.xwoot21.getContentManager();
        XWootContentProviderInterface mxwcp2 = this.xwoot22.getContentManager();
        XWootContentProviderInterface mxwcp3 = this.xwoot23.getContentManager();
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        this.initContentProvider(mxwcp3);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();
        mxwcp3.getModifiedPagesIds();
        mxwcp3.clearAllModifications();

        this.simulateXWikiUserModification(mxwcp1, pageId, content1, 1, 0, true);

        // Launch the synch...
        this.xwoot21.synchronize();

        Assert.assertEquals(content1, this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));

        Assert.assertEquals(content1, this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));

        Assert.assertEquals(content1, this.wootEngine3.getContentManager().getContent(pageId, pageGuid, "content"));

        this.simulateXWikiUserModification(mxwcp1, pageId, content2, 2, 0, false);
        this.simulateXWikiUserModification(mxwcp2, pageId, content3, 2, 0, false);
        this.simulateXWikiUserModification(mxwcp3, pageId, content4, 2, 0, false);

        // Launch the synch...
        this.xwoot21.synchronize();
        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");
        System.out.println("woot3 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine3.getContentManager().getContent(pageId, pageGuid, "content"));
        System.out.println("-------------------");
        Assert.assertEquals(result, this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, "content"),
            this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"));
        Assert.assertEquals(this.wootEngine2.getContentManager().getContent(pageId, pageGuid, "content"),
            this.wootEngine3.getContentManager().getContent(pageId, pageGuid, "content"));

    }

    @Test(expected = XWootException.class)
    public void testXWoot1() throws Exception
    {
        this.xwoot21 =
            new XWoot2(this.xwiki21, this.wootEngine1, this.lpbCast1, "/cantBecreated" + File.separator + "Site1",
                "Site1", new Integer(1), this.tre1, this.ae1);
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
            new XWoot2(this.xwiki21, this.wootEngine1, this.lpbCast1, f.toString(), "Site 1", new Integer(1),
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
            new XWoot2(this.xwiki21, this.wootEngine1, this.lpbCast1, f.toString(), "Site 1", new Integer(1),
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
            new XWoot2(this.xwiki21, this.wootEngine1, this.lpbCast1, f.toString(), f.toString() + File.separator
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
        String pageId1 = "test.1";
        String pageGuid1 = "page:" + pageId1;
        String content1 = "toto";

        String pageId2 = "test.2";
        String pageGuid2 = "page:" + pageId2;
        String content2 = "titi";

        String pageId3 = "test.3";
        String pageGuid3 = "page:" + pageId3;
        String content3 = "tata";

        // simulate XWiki user page creation
        this.xwoot21.connectToContentManager();
        this.xwoot22.connectToContentManager();
        XWootContentProviderInterface mxwcp1 = this.xwoot21.getContentManager();
        XWootContentProviderInterface mxwcp2 = this.xwoot22.getContentManager();

        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();
        
        // simulate XWiki user page creation
        this.simulateXWikiUserModification(mxwcp1, pageId1, content1, 1, 0, true);
        this.simulateXWikiUserModification(mxwcp1, pageId2, content2, 1, 0, true);
        this.simulateXWikiUserModification(mxwcp1, pageId3, content3, 1, 0, true);

        this.xwoot21.createNetwork();
        this.xwoot21.reconnectToP2PNetwork();
        this.xwoot21.synchronize();

        Assert.assertEquals(3, this.xwoot21.getWootEngine().getContentManager().listPages().length);
        Assert.assertEquals("toto\n", this.xwoot21.getWootEngine().getContentManager().getContent(pageId1, pageGuid1,
            "content"));
        Assert.assertEquals("titi\n", this.xwoot21.getWootEngine().getContentManager().getContent(pageId2, pageGuid2,
            "content"));
        Assert.assertEquals("tata\n", this.xwoot21.getWootEngine().getContentManager().getContent(pageId3, pageGuid3,
            "content"));

        File f = this.xwoot21.computeState();
        Assert.assertNotNull(f);
        this.xwoot22.joinNetwork("");
        this.xwoot22.importState(f);
        this.xwoot22.connectToContentManager();
        this.xwoot22.reconnectToP2PNetwork();
        Assert.assertEquals(3, this.xwoot22.getWootEngine().getContentManager().listPages().length);
        Assert.assertEquals("toto\n", this.xwoot22.getWootEngine().getContentManager().getContent(pageId1, pageGuid1,
            "content"));
        Assert.assertEquals("titi\n", this.xwoot22.getWootEngine().getContentManager().getContent(pageId2, pageGuid2,
            "content"));
        Assert.assertEquals("tata\n", this.xwoot22.getWootEngine().getContentManager().getContent(pageId3, pageGuid3,
            "content"));

    }
}
