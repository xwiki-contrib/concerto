package org.xwoot.test;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.Constants;
import org.xwoot.XWootContentProvider;
import org.xwoot.Utils;
import org.xwoot.XWootContentProviderException;
import org.xwoot.XWootId;
import org.xwoot.XWootObject;

public class XWootContentProviderTest extends TestCase
{
    private XWootContentProvider xwc;

    @Override
    protected void setUp() throws Exception
    {
        System.out.format("*****************************\n");
        xwc = new XWootContentProvider("http://localhost:8080/xwiki/xmlrpc/confluence", "DB", true, null);
        xwc.login("Admin", "admin");
    }

    @Override
    protected void tearDown() throws Exception
    {
        xwc.logout();
        xwc.dispose();
        System.out.format("*****************************\n\n");
    }

    public void testGetModifiedPagesIds() throws XWootContentProviderException
    {
        System.out.format("*** testGetModifiedPagesIds()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("Total modifications: %d\n", result.size());
        assertTrue(result.size() > 0);
    }

    public void testClearModification() throws XWootContentProviderException
    {
        System.out.format("*** testClearModification()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();

        XWootId xwootId = (XWootId) result.toArray()[0];

        xwc.clearModification(xwootId);
        Set<XWootId> resultAfter = xwc.getModifiedPagesIds();

        System.out.format("Total modifications before: %d\n", result.size());
        System.out.format(" Total modifications after: %d\n", resultAfter.size());

        assertEquals(resultAfter.size() + 1, result.size());
    }

    public void testClearAllModifications() throws XWootContentProviderException
    {
        System.out.format("*** testClearAllModification()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        xwc.clearAllModifications();
        Set<XWootId> resultAfter = xwc.getModifiedPagesIds();

        System.out.format("Total modifications before: %d\n", result.size());
        System.out.format(" Total modifications after: %d\n", resultAfter.size());

        assertEquals(0, resultAfter.size());
    }

    public void testPageModification() throws Exception
    {
        final String pageName = "Main.WebHome";
        final String content = String.format("Modified at %s\n", System.currentTimeMillis());

        System.out.format("*** testPageModification()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();

        XWikiXmlRpcClient rpc = xwc.getRpc();
        XWikiPage page = rpc.getPage(pageName);
        page.setContent(content);
        page = rpc.storePage(page);

        Set<XWootId> result = xwc.getModifiedPagesIds();

        assertEquals(1, result.size());
        XWootId xwootId = (XWootId) result.toArray()[0];
        System.out.format("Modification: %s\n", xwootId);
        assertEquals(pageName, xwootId.getPageId());

        /*
         * Set the last cleared modification to the previous one so that the getModifiedEntities will have a version to
         * which compare the differences
         */
        xwc.getStateManager().clearModification(xwc.getStateManager().getPreviousModification(xwootId));
        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(xwootId);
        assertTrue(modifiedEntities.size() >= 1);
        XWootObject xwootObject = modifiedEntities.get(0);
        System.out.format("%s\n", xwootObject);
        assertTrue(xwootObject.getGuid().startsWith(Constants.PAGE_NAMESPACE));
        assertFalse(xwootObject.isNewlyCreated());
        assertTrue(xwootObject.getFieldValue("content").equals(content));
    }

    public void testNewPageModification() throws XWootContentProviderException, XmlRpcException
    {
        final String pageName = String.format("Test.%d", System.currentTimeMillis());
        final String content = String.format("Modified at %s\n", System.currentTimeMillis());

        System.out.format("*** testNewPageModification()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();

        XWikiXmlRpcClient rpc = xwc.getRpc();
        XWikiPage page = new XWikiPage();
        page.setId(pageName);
        page.setContent(content);
        rpc.storePage(page);

        page = rpc.getPage(pageName);
        assertEquals(pageName, page.getId());
        assertEquals(content, page.getContent());

        xwc.getStateManager().dumpDbLines();

        Set<XWootId> result = xwc.getModifiedPagesIds();

        xwc.getStateManager().dumpDbLines();

        System.out.format("************** Result: %s\n", result);
        assertEquals(1, result.size());
        XWootId xwootId = (XWootId) result.toArray()[0];
        System.out.format("Modification: %s\n", xwootId);
        assertEquals(pageName, xwootId.getPageId());

        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(xwootId);
        assertTrue(modifiedEntities.size() == 1);
        XWootObject xwootObject = modifiedEntities.get(0);
        System.out.format("%s\n", xwootObject);
        assertTrue(xwootObject.getGuid().startsWith(Constants.PAGE_NAMESPACE));
        assertTrue(xwootObject.isNewlyCreated());
        assertTrue(xwootObject.getFieldValue("content").equals(content));
    }

    public void testStore() throws XWootContentProviderException, XmlRpcException
    {
        final String content = String.format("Modified by XWoot at %s\n", System.currentTimeMillis());

        XWikiXmlRpcClient rpc = xwc.getRpc();

        System.out.format("*** testStore()\n");
        xwc.getModifiedPagesIds();
        xwc.clearAllModifications();

        XWikiPage page = rpc.getPage("Main.WebHome");
        XWootObject xwootObject = Utils.xwikiPageToXWootObject(page, false);
        xwootObject.setFieldValue("content", content);

        xwc.store(xwootObject, null);
        page = rpc.getPage("Main.WebHome");
        System.out.format("XWiki page stored by XWoot. At version %d.%d\n", page.getVersion(), page.getMinorVersion());

        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("%s\n", result);
        assertEquals(0, result.size());
    }

    /* WARNING: This test works only if the optimized version of the updateModifiedPages is used in getModifiedPagesIds */
    public void testMultipleModifications() throws Exception
    {
        XWikiXmlRpcClient rpc = xwc.getRpc();

        System.out.format("*** testMultipleModifications()\n");
        XWikiPage page = rpc.getPage("Main.WebHome");
        page.setContent(String.format("%d\n", System.currentTimeMillis()));
        page = rpc.storePage(page);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        page.setContent(String.format("%d\n", System.currentTimeMillis()));
        page = rpc.storePage(page);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        page.setContent(String.format("%d\n", System.currentTimeMillis()));
        page = rpc.storePage(page);

        xwc.getModifiedPagesIds();
        List<XWootId> result = xwc.getStateManager().getModificationsFor("Main.WebHome", false);
        System.out.format("Not cleared items for Main.WebHome: %s\n", result);

        assertEquals(1, result.size());
        assertEquals(page.getVersion(), result.get(0).getVersion());
        assertEquals(page.getMinorVersion(), result.get(0).getMinorVersion());

        System.out.format("*****************************\n\n");
    }

}
