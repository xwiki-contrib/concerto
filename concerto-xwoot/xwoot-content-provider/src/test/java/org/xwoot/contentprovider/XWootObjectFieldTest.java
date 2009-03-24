package org.xwoot.contentprovider;

import junit.framework.TestCase;

import org.xwoot.contentprovider.XWootObjectField;

public class XWootObjectFieldTest extends TestCase
{
    public void testStringWootableField()
    {
        new XWootObjectField("foo", "bar", true);
    }

    public void testNonStringWootableField()
    {
        try {
            new XWootObjectField("foo", new Integer(0), true);
            fail("Wootable fields should be of type String");
        } catch (IllegalArgumentException e) {
            // void
        }
    }
}
