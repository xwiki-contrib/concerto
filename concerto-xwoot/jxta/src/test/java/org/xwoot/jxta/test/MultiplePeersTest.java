/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwoot.jxta.test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.UUID;

import junit.framework.Assert;

import net.jxta.protocol.PeerGroupAdvertisement;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.jxta.test.multiplePeers.BroadcastMessageToGroup;
import org.xwoot.jxta.test.multiplePeers.DiscoverPeersInGroup;
import org.xwoot.jxta.test.util.PeerClassLoader;
import org.xwoot.xwootUtil.FileUtil;

/**
 * TODO DOCUMENT ME!
 */
public class MultiplePeersTest
{
    /** Working dir for tests. */
    public static final String WORKING_DIR = FileUtil.getTestsWorkingDirectoryPathForModule("jxta");

    public static final String MAIN_THREAD_LOCK = "wait for peers to finish";

    public static PeerGroupAdvertisement GROUP_ADV;

    public static final String GROUP_ADV_LOCK = "wait for group adv to be created/published";

    public static final String TEST_GROUP_PREFIX = "testGoup";

    public static final String classpath = getClassPath();

    public static final String pathElement[] = MultiplePeersTest.split(classpath);

    public static final Class[] VOID_PARAMETERS_TYPE = new Class[0];

    public static final Object[] VOID_PARAMETERS = new Object[0];

    public static final Class[] initMethodParametersTypes = {String.class, Boolean.class};

    public static final Class[] startMethodParametersTypes = {Boolean.class, String.class};

    public static final String ERRORS_PROPERTY_NAME = "errors";

    public static final String SUCCESS_PROPERTY_NAME = "success";

    public static final String SUCCESS_PROPERTY_VALUE = "true";

    public static final char[] KEYSTORE_PASSWORD = "keystorePass".toCharArray();

    public static final char[] GROUP_PASSWORD = "groupPass".toCharArray();
    
    @BeforeClass
    public static void initTests()
    {
        FileUtil.deleteDirectory(WORKING_DIR);
    }

    @Before
    public void init() throws Exception
    {
        FileUtil.checkDirectoryPath(WORKING_DIR);
    }
    
    @After
    public void cleanUp()
    {
        FileUtil.deleteDirectory(WORKING_DIR);
    }
    
    @Test
    public void testDiscoverPeersInGroup() throws Exception
    {
        this.launchTest(DiscoverPeersInGroup.class.getName(), 2);
    }

    @Test
    public void testBroadcastMessageToGroup() throws Exception
    {
        this.launchTest(BroadcastMessageToGroup.class.getName(), 2);
    }

    private void launchTest(String className, int numberOfPeers) throws Exception
    {
        Object[] testCase = new Object[numberOfPeers];
        Method[] initMethodTestCase = new Method[numberOfPeers];
        Method[] connectMethodTestCase = new Method[numberOfPeers];
        Method[] startMethodTestCase = new Method[numberOfPeers];

        String groupName = TEST_GROUP_PREFIX + UUID.randomUUID().toString();

        for (int i = 0; i < numberOfPeers; i++) {

            PeerClassLoader peerLoader = new PeerClassLoader(pathElement);
            Class discoverTestPeerClass = peerLoader.loadClass(className);

            Constructor constructor = discoverTestPeerClass.getConstructor(VOID_PARAMETERS_TYPE);
            testCase[i] = constructor.newInstance(VOID_PARAMETERS);

            initMethodTestCase[i] = discoverTestPeerClass.getMethod("init", initMethodParametersTypes);
            Object[] initParameters = new Object[] {String.valueOf(i), Boolean.FALSE};
            Boolean inited = (Boolean) initMethodTestCase[i].invoke(testCase[i], initParameters);
            Assert.assertTrue(inited.booleanValue());

            connectMethodTestCase[i] = discoverTestPeerClass.getMethod("connect", VOID_PARAMETERS_TYPE);
            Boolean connected = (Boolean) connectMethodTestCase[i].invoke(testCase[i], VOID_PARAMETERS);
            Assert.assertTrue(connected.booleanValue());

            startMethodTestCase[i] = discoverTestPeerClass.getMethod("start", startMethodParametersTypes);
            Object[] startParameters = new Object[] {(i == 0 ? Boolean.TRUE : Boolean.FALSE), groupName};
            Boolean started = (Boolean) startMethodTestCase[i].invoke(testCase[i], startParameters);
            Assert.assertTrue(started.booleanValue());
        }

        // check if we had errors before waiting.
        checkForErrors();

        System.out.println("Keeping main thread alive for max 2 minutes.");

        synchronized (MAIN_THREAD_LOCK) {
            MAIN_THREAD_LOCK.wait(120000);
            System.out.println("(possibly) A peer finished.");

            checkForErrors();
            checkForSuccess();
        }
    }

    private static String[] split(String classpath)
    {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        String pathElement[] = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreElements(); i++) {
            pathElement[i] = (String) tokenizer.nextElement();
        }
        return pathElement;
    }

    private void checkForErrors()
    {
        String errors = System.getProperty(ERRORS_PROPERTY_NAME);
        System.out.println("Errors: " + errors);
        Assert.assertNull(errors);
    }

    private void checkForSuccess()
    {
        String success = System.getProperty(SUCCESS_PROPERTY_NAME);
        Assert.assertNotNull(success);
        Assert.assertEquals(SUCCESS_PROPERTY_VALUE, success);
    }

    public static String getClassPath()
    {
        String defaultClassPath = System.getProperty("java.class.path");
        String surefireClassPath = System.getProperty("surefire.test.class.path");

        return defaultClassPath + File.pathSeparator + surefireClassPath;
    }
}
