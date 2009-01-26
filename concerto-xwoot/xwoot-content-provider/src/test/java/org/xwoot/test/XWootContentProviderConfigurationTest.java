package org.xwoot.test;

import java.util.Properties;

import junit.framework.TestCase;

import org.xwoot.XWootContentProviderConfiguration;

public class XWootContentProviderConfigurationTest extends TestCase
{
    public void testIgnoreAccept()
    {
        Properties properties = new Properties();
        properties.setProperty("ignore", "Main.*");
        properties.setProperty("accept", "Main.WebHome");

        XWootContentProviderConfiguration xwcpc = new XWootContentProviderConfiguration(properties);
        assertEquals(true, xwcpc.isIgnored("Main.Foo"));
        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));
    }

    public void testAddRemove()
    {
        Properties properties = new Properties();

        XWootContentProviderConfiguration xwcpc = new XWootContentProviderConfiguration(properties);

        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.addIgnorePattern("Main.*");
        assertEquals(true, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.addAcceptPattern("Main.WebHome");
        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.removeAcceptPattern("Main.WebHome");
        assertEquals(true, xwcpc.isIgnored("Main.WebHome"));

        xwcpc.removeIgnorePattern("Main.*");
        assertEquals(false, xwcpc.isIgnored("Main.WebHome"));
    }

}
