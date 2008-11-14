package org.xwoot.xwootApp.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;

import org.xwoot.wikiContentManager.XWikiSwizzleClient.XwikiSwizzleClient;
import org.xwoot.xwootApp.XWoot;
import org.xwoot.xwootApp.XWootException;

public class XWootTest extends AbstractXWootTest
{

    @Test(expected = XWootException.class)
    public void testXWoot1() throws Exception
    {
        this.xwoot1 =
            new XWoot(this.xwiki1, this.wootEngine1, this.lpbCast1, "/cantBecreated" + File.separator + "Site1",
                "Site 1", new Integer(1), new ThomasRuleEngine(1, WORKINGDIR), this.ae1);
    }

    @Test(expected = RuntimeException.class)
    public void testXWoot2() throws Exception
    {
        File f = new File(WORKINGDIR + File.separatorChar + "file.tmp");
        f.delete();
        assertFalse(f.exists());
        f.createNewFile();
        assertTrue(f.exists());
        this.xwoot1 =
            new XWoot(this.xwiki1, this.wootEngine1, this.lpbCast1, f.toString(), "Site 1", new Integer(1),
                new ThomasRuleEngine(1, WORKINGDIR), this.ae1);
    }

    @Test(expected = XWootException.class)
    public void testXWoot3() throws Exception
    {
        File f = new File(WORKINGDIR + File.separatorChar + "folder");
        f.delete();
        assertFalse(f.exists());
        f.mkdir();
        assertTrue(f.exists());
        f.setReadOnly();
        assertFalse(f.canWrite());
        this.xwoot1 =
            new XWoot(this.xwiki1, this.wootEngine1, this.lpbCast1, f.toString(), "Site 1", new Integer(1),
                new ThomasRuleEngine(1, WORKINGDIR), this.ae1);
    }

    @Test
    public void testXWoot4() throws Exception
    {
        File f = new File(WORKINGDIR);
        f.mkdir();
        assertTrue(f.exists());
        assertFalse(this.lpbCast1.isSenderConnected());
        this.lpbCast1.connectSender();
        assertTrue(this.lpbCast1.isSenderConnected());
        this.xwoot1 =
            new XWoot(this.xwiki1, this.wootEngine1, this.lpbCast1, f.toString(), f.toString() + File.separator
                + "Site1", new Integer(1), new ThomasRuleEngine(1, WORKINGDIR), this.ae1);
        assertNotNull(this.xwoot1);
    }

    @Test
    public void testPageManagement() throws Exception
    {

        assertTrue(this.xwoot1.getListOfManagedPages().isEmpty());
        // xwoot1 is not connected
        this.xwoot1.addAllPageManagement();
        assertTrue(this.xwoot1.getListOfManagedPages().isEmpty());
        // create a xwiki test page
        this.xwiki1.removePage("test.1");
        this.xwiki1.createPage("test.1", "toto\n");

        assertEquals("toto\n", this.xwiki1.getPageContent("test.1"));
        // xwoot1 is connected with pages in xwiki
        this.xwoot1.connectToContentManager();
        this.xwoot1.addAllPageManagement();
        assertFalse(this.xwoot1.getListOfManagedPages().isEmpty());
        this.xwoot1.removeAllManagedPages();
        assertTrue(this.xwoot1.getListOfManagedPages().isEmpty());
        // create xwiki test pages
        this.xwiki1.removePage("test.2");
        this.xwiki1.createPage("test.2", "titi\n");
        this.xwiki1.removePage("test.3");
        this.xwiki1.createPage("test.3", "tata\n");
        List<String> list = new ArrayList<String>();
        list.add("test.1");
        list.add("test.2");
        list.add("test.3");
        this.xwoot1.disconnectFromContentManager();
        this.xwoot1.setPageManagement("test", list);
        this.xwoot1.connectToContentManager();
        this.xwoot1.setPageManagement("Faketest", list);
        assertEquals(0, this.xwoot1.getListOfManagedPages().size());
        this.xwoot1.setPageManagement("test", list);
        assertEquals(3, this.xwoot1.getListOfManagedPages().size());
        list.remove(2);
        this.xwoot1.setPageManagement("test", list);
        assertEquals(2, this.xwoot1.getListOfManagedPages().size());
        HashMap p = null;
        p = this.xwoot1.isPagesManaged(null);
        assertEquals(0, p.size());
        list.add("test.3");
        p = this.xwoot1.isPagesManaged(list);
        assertEquals(3, p.size());
        assertEquals(Boolean.valueOf(true), p.get("test.1"));
        assertEquals(Boolean.valueOf(true), p.get("test.2"));
        assertEquals(Boolean.valueOf(false), p.get("test.3"));
    }

    @Test
    public void testConnection() throws Exception
    {
        // receiver = p2pNetwork
        assertFalse(this.xwoot1.isConnectedToP2PNetwork());
        this.xwoot1.reconnectToP2PNetwork();
        assertTrue(this.xwoot1.isConnectedToP2PNetwork());
        this.xwoot1.disconnectFromP2PNetwork();
        assertFalse(this.xwoot1.isConnectedToP2PNetwork());
        this.xwoot1.reconnectToP2PNetwork();
        assertTrue(this.xwoot1.isConnectedToP2PNetwork());
        this.xwoot1.reconnectToP2PNetwork();
        assertTrue(this.xwoot1.isConnectedToP2PNetwork());

        // content provider
        assertFalse(this.xwoot1.isContentManagerConnected());
        this.xwoot1.connectToContentManager();
        assertTrue(this.xwoot1.isContentManagerConnected());
        this.xwoot1.disconnectFromContentManager();
        assertFalse(this.xwoot1.isContentManagerConnected());
    }

    // @Test
    // public void testComputeState() {
    // fail("Not yet implemented");
    // }

    @Test
    public void testGetXwiki() throws Exception
    {
        assertNull(this.xwoot1.getContentManager());
        this.xwoot1.connectToContentManager();
        assertNotNull(this.xwoot1.getContentManager());
    }

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
        /* assertTrue(this.xwoot1.getListManagedPages().isEmpty()); */
        // create xwiki test page
        this.xwiki1.removePage("test.1");
        this.xwiki1.createPage("test.1", "toto\n");

        this.xwiki1.removePage("test.2");
        this.xwiki1.createPage("test.2", "titi\n");

        this.xwiki1.removePage("test.3");
        this.xwiki1.createPage("test.3", "tata\n");

        this.xwoot1.connectToContentManager();
        this.xwoot1.reconnectToP2PNetwork();
        this.xwoot1.addAllPageManagement();
        this.xwoot1.synchronizePages();
        assertTrue(this.xwoot1.getListOfManagedPages().size() >= 3);
        assertEquals("toto\n", this.wootEngine1.getPageManager().getPage("test.1"));
        assertEquals("titi\n", this.wootEngine1.getPageManager().getPage("test.2"));
        assertEquals("tata\n", this.wootEngine1.getPageManager().getPage("test.3"));
        assertEquals("toto\n", this.xwiki1.getPageContent("test.1"));
        assertEquals("titi\n", this.xwiki1.getPageContent("test.2"));
        assertEquals("tata\n", this.xwiki1.getPageContent("test.3"));
        File f = this.xwoot1.computeState();
        assertNotNull(f);
        System.out.println(f);
        this.xwoot2.setWootStorage(f);
        this.xwoot2.connectToContentManager();
        this.xwoot2.reconnectToP2PNetwork();
        assertEquals(this.xwoot1.getListOfManagedPages().size(), this.xwoot2.getListOfManagedPages().size());
    }
}
