package org.xwoot.test;

import java.util.Set;

import junit.framework.TestCase;

import org.xwoot.XWootContentProvider;
import org.xwoot.XWootContentProviderException;
import org.xwoot.XWootId;

public class XWootContentProviderTest extends TestCase
{
    private XWootContentProvider xwc;

    @Override
    protected void setUp() throws Exception
    {
        xwc = new XWootContentProvider("http://localhost:8080/xwiki/xmlrpc/confluence", null);
        xwc.login("Admin", "admin");
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

    @Override
    protected void tearDown() throws Exception
    {
        xwc.logout();
        xwc.dispose();
    }

}
