package org.xwoot.test;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.Constants;
import org.xwoot.Utils;
import org.xwoot.XWootContentProvider;
import org.xwoot.XWootContentProviderException;
import org.xwoot.XWootId;
import org.xwoot.XWootObject;

public class XWootContentProviderTest extends TestCase
{
    private XWootContentProvider xwc;

    @Override
    protected void setUp() throws Exception
    {
        xwc = new XWootContentProvider("http://localhost:8080/xwiki/xmlrpc/confluence", null);
        xwc.login("Admin", "admin");

        xwc.dumpDbLines("Last DB lines before test", null, 5);
    }

    public void testGetModifiedPagesIds() throws XWootContentProviderException
    {
        System.out.format("*** testGetModifiedPagesIds()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("Total modifications: %d\n", result.size());
        assertTrue(result.size() > 0);
        System.out.format("*****************************\n\n");
    }

    public void testClearModification() throws XWootContentProviderException
    {
        System.out.format("*** testClearModification()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        xwc.clearModification((XWootId) result.toArray()[0]);
        Set<XWootId> resultAfter = xwc.getModifiedPagesIds();

        System.out.format("Total modifications before: %d\n", result.size());
        System.out.format(" Total modifications after: %d\n", resultAfter.size());

        assertTrue(result.size() == resultAfter.size() + 1);
        System.out.format("*****************************\n\n");
    }

    public void testClearAllModifications() throws XWootContentProviderException
    {
        System.out.format("*** testClearAllModification()\n");
        Set<XWootId> result = xwc.getModifiedPagesIds();
        xwc.clearAllModifications();
        Set<XWootId> resultAfter = xwc.getModifiedPagesIds();

        System.out.format("Total modifications before: %d\n", result.size());
        System.out.format(" Total modifications after: %d\n", resultAfter.size());

        assertTrue(resultAfter.size() == 0);
        System.out.format("*****************************\n\n");
    }

    public void testPageModification() throws XWootContentProviderException, XmlRpcException
    {
        final String content = String.format("Modified at %s\n", System.currentTimeMillis());

        System.out.format("*** testPageModification()\n");
        xwc.clearAllModifications();
        XWikiXmlRpcClient rpc = xwc.getRpc();
        XWikiPage page = rpc.getPage("Test.Test");
        page.setContent(content);
        page = rpc.storePage(page);

        xwc.dumpDbLines(String.format("Test.Test stored at version %d.%d", page.getVersion(), page.getMinorVersion()),
            "Test.Test", 5);

        Set<XWootId> result = xwc.getModifiedPagesIds();
        assertTrue(result.size() == 1);
        XWootId xwootId = (XWootId) result.toArray()[0];
        System.out.format("%s\n", xwootId);
        assertTrue(xwootId.getPageId().equals("Test.Test"));

        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(xwootId);
        assertTrue(modifiedEntities.size() == 1);
        XWootObject xwootObject = modifiedEntities.get(0);
        System.out.format("%s\n", xwootObject);
        assertTrue(xwootObject.getGuid().startsWith(Constants.PAGE_NAMESPACE));
        assertFalse(xwootObject.isNewlyCreated());
        assertTrue(xwootObject.getFieldValue("content").equals(content));
        System.out.format("*****************************\n\n");
    }

    public void testNewPageModification() throws XWootContentProviderException, XmlRpcException
    {
        final String pageName = String.format("Test.%d", System.currentTimeMillis());
        final String content = String.format("Modified at %s\n", System.currentTimeMillis());

        System.out.format("*** testNewPageModification()\n");
        xwc.clearAllModifications();
        XWikiXmlRpcClient rpc = xwc.getRpc();
        XWikiPage page = new XWikiPage();
        page.setId(pageName);
        page.setContent(content);
        rpc.storePage(page);

        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("Result: %s\n", result);
        assertTrue(result.size() == 1);
        XWootId xwootId = (XWootId) result.toArray()[0];
        System.out.format("%s\n", xwootId);
        assertTrue(xwootId.getPageId().equals(pageName));

        List<XWootObject> modifiedEntities = xwc.getModifiedEntities(xwootId);
        assertTrue(modifiedEntities.size() == 1);
        XWootObject xwootObject = modifiedEntities.get(0);
        System.out.format("%s\n", xwootObject);
        assertTrue(xwootObject.getGuid().startsWith(Constants.PAGE_NAMESPACE));
        assertTrue(xwootObject.isNewlyCreated());
        assertTrue(xwootObject.getFieldValue("content").equals(content));
        System.out.format("*****************************\n\n");
    }

    public void testStore() throws XWootContentProviderException, XmlRpcException
    {
        final String content = String.format("Modified by XWoot at %s\n", System.currentTimeMillis());

        XWikiXmlRpcClient rpc = xwc.getRpc();

        System.out.format("*** testStore()\n");
        xwc.clearAllModifications();
        xwc.dumpDbLines("Right before store", null, 3);

        XWikiPage page = rpc.getPage("Main.WebHome");
        XWootObject xwootObject = Utils.xwikiPageToXWootObject(page, false);
        xwootObject.setFieldValue("content", content);

        xwc.store(xwootObject, null);
        page = rpc.getPage("Main.WebHome");
        System.out.format("XWiki page stored by XWoot. At version %d.%d\n", page.getVersion(), page.getMinorVersion());
        xwc.dumpDbLines("Right after store", null, 3);

        Set<XWootId> result = xwc.getModifiedPagesIds();
        System.out.format("%s\n", result);
        assertTrue(result.size() == 0);

        System.out.format("*****************************\n\n");
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
        List<XWootId> result = xwc.getXWootIdsFor("Main.WebHome", false);
        System.out.format("Not cleared items for Main.WebHome: %s\n", result);
        xwc.dumpDbLines("Multiple modifications", "Main.WebHome", 5);

        assertTrue(result.size() == 1);

        System.out.format("*****************************\n\n");
    }

    @Override
    protected void tearDown() throws Exception
    {
        xwc.dumpDbLines("Last DB lines after test", null, 5);

        xwc.logout();
        xwc.dispose();
    }

}
