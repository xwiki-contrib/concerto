package org.xwoot.xwootApp.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.PageFieldValue;

public class FieldsTREIdentifierTest
{

    @Test
    public void testFieldsTREIdentifier()
    {
        XWootObjectIdentifier f = new XWootObjectIdentifier("test.0", "content");
        assertEquals("test.0", f.getPageName());
        assertEquals("content", f.getMetaDataId());
        assertEquals("test.0.content", f.getId());
        f.setPageName("test.2");
        assertEquals("test.2", f.getPageName());
        assertEquals("test.2.content", f.getId());
        f.setMetaDataId("Content");
        assertEquals("Content", f.getMetaDataId());
        assertEquals("test.2.Content", f.getId());
        XWootObjectIdentifier f2 = new XWootObjectIdentifier("test.0", "content");
        f2.setPageName("toto");
    }

    @Test
    public void testToString()
    {
        XWootObjectIdentifier f0 = new XWootObjectIdentifier("test.0", "content");
        assertEquals("test.0.content", f0.toString());
    }

    @Test
    public void testEqualsAndHashCodeObject()
    {
        XWootObjectIdentifier f0 = new XWootObjectIdentifier("test.0", "content");
        XWootObjectIdentifier f1 = new XWootObjectIdentifier("test.1", "content");
        XWootObjectIdentifier f2 = new XWootObjectIdentifier("test.1", "content");
        XWootObjectIdentifier f3 = new XWootObjectIdentifier("test.1", "content");
        XWootObjectIdentifier f4 = f3;

        PageFieldValue v = new PageFieldValue("tagada");

        // reflexive : x==x
        assertTrue(f1.equals(f1));

        // null : value x!=null
        assertFalse(f1.equals(null));

        // Class : cat!=dog
        assertFalse(f1.equals(v));

        // normal :v.value!=v1.value
        assertFalse(f1.equals(f0));
        // symmetric
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));
        // transitive
        assertTrue(f1.equals(f3));
        assertTrue(f2.equals(f3));
        // consistent
        assertTrue(f4.equals(f3));

        // hashCode
        assertEquals(f1.hashCode(), f1.hashCode());
        assertEquals(f1.hashCode(), f2.hashCode());
        assertEquals(f1.hashCode(), f3.hashCode());
        assertEquals(f4.hashCode(), f3.hashCode());
        assertEquals(f2.hashCode(), f3.hashCode());

    }

}
