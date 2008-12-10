package org.xwoot.test;

import org.xwoot.XWootObjectField;

import junit.framework.TestCase;

public class XWootObjectFieldTest extends TestCase
{
    public void testStringWootableField() {
        new XWootObjectField("foo", "bar", true);
    }
    
    public void testNonStringWootableField() {
        try {
            new XWootObjectField("foo", new Integer(0), true);
            fail("Wootable fields should be of type String");
        }
        catch(IllegalArgumentException e) {            
        }
    }
}
