package org.xwoot.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.xpn.xwiki.test.XWikiTestSetup;

public class AllTests extends TestCase
{
    private static final String PATTERN = ".*" + System.getProperty("pattern", "");

    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite();
        addTestCase(suite, XWootObjectFieldTest.class);
        addTestCase(suite, XWootContentProviderConfigurationTest.class);
        addTestCase(suite, StateManagerTest.class);
        addTestCase(suite, NewXWootContentProviderTest.class);
        return new XWikiTestSetup(suite);
    }

    @SuppressWarnings("unchecked")
    private static void addTestCase(TestSuite suite, Class testClass) throws Exception
    {
        if (testClass.getName().matches(PATTERN)) {
            suite.addTest(new TestSuite(testClass));
        }
    }
}
