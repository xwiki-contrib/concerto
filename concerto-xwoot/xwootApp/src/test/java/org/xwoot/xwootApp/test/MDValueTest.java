package org.xwoot.xwootApp.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.xwoot.xwootApp.core.tre.MDValue;

public class MDValueTest
{

    @Test
    public void testEqualsAndHashCodeObject()
    {

        // initial field
        MDValue v = new MDValue("Alien");
        // field with no value
        MDValue v0 = new MDValue(null);
        // v1.value!=v.value
        MDValue v1 = new MDValue("Predator");
        // v2==v
        MDValue v2 = new MDValue("Alien");
        // v3==v
        MDValue v3 = new MDValue("Alien");
        // v4==v
        MDValue v4 = v;

        // reflexive : x==x
        assertTrue(v.equals(v));

        // null : value x!=null
        assertFalse(v.equals(null));
        // deep null : v0.notnull!=v.null
        assertFalse(v0.equals(v));
        // Class : cat!=dog
        assertFalse(v.equals("yopla"));
        // normal :v.value!=v1.value
        assertFalse(v.equals(v1));
        // symmetric
        assertTrue(v.equals(v2));
        assertTrue(v2.equals(v));
        // transitive
        assertTrue(v.equals(v3));
        assertTrue(v2.equals(v3));
        // consistent
        assertTrue(v4.equals(v3));

        // hashCode
        assertEquals(v.hashCode(), v.hashCode());
        assertEquals(v.hashCode(), v2.hashCode());
        assertEquals(v.hashCode(), v3.hashCode());
        assertEquals(v4.hashCode(), v3.hashCode());
        assertEquals(v2.hashCode(), v3.hashCode());

    }

    @Test
    public void testGet()
    {
        MDValue v = new MDValue("Alien");

        assertEquals("Alien", v.get());
    }

    @Test
    public void testToString()
    {
        MDValue v = new MDValue("Predator");

        assertEquals("Predator", v.toString());
    }

}
