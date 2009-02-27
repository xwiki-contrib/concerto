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
import java.net.URI;

import net.jxta.platform.NetworkManager.ConfigMode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Common behavior for jxta tests.
 * <p>
 * Just add tests to subclasses.
 * 
 * @version $Id$
 */
public abstract class AbstractJxtaTestBase
{
    /** Working dir for tests. */
    public static final String WORKING_DIR = FileUtil.getTestsWorkingDirectoryPathForModule("jxta");

    /** Name of test peer. */
    protected static String peerName = "concerto1";

    /** Local repository for test peer. */
    protected static File jxtaHome = new File(WORKING_DIR, peerName);

    /** The test peer. */
    protected static Peer peer;

    /**
     * Initializes the working directory.
     * 
     * @throws Exception if problems occur.
     */
    @BeforeClass
    public static void createAndConnect() throws Exception
    {
        if (peer == null) {
            FileUtil.deleteDirectory(WORKING_DIR);
            FileUtil.checkDirectoryPath(WORKING_DIR);
    
            peer = PeerFactory.createPeer();
            peer.configureNetwork(jxtaHome, ConfigMode.EDGE);
            peer.getManager().setInstanceName(peerName);
    
            // We have not choice if JXTA has a singleton architecture and we can
            // not start multiple peers inside one JVM.
            //peer.getManager().setUseDefaultSeeds(true);

            peer.getManager().getConfigurator().addSeedRendezvous(new URI("tcp://192.18.37.39:9701"));
            peer.getManager().getConfigurator().addSeedRelay(new URI("tcp://192.18.37.39:9701"));
            peer.getManager().getConfigurator().addSeedRendezvous(new URI("http://192.18.37.39:9700"));
            peer.getManager().getConfigurator().addSeedRelay(new URI("http://192.18.37.39:9700"));
            
            peer.getManager().getConfigurator().addSeedRendezvous(new URI("tcp://192.18.37.38:9701"));
            peer.getManager().getConfigurator().addSeedRelay(new URI("tcp://192.18.37.38:9701"));
            peer.getManager().getConfigurator().addSeedRendezvous(new URI("http://192.18.37.38:9700"));
            peer.getManager().getConfigurator().addSeedRelay(new URI("http://192.18.37.38:9700"));
            
            peer.getManager().getConfigurator().addSeedRendezvous(new URI("tcp://192.18.37.36:9701"));
            peer.getManager().getConfigurator().addSeedRelay(new URI("tcp://192.18.37.36:9701"));
            peer.getManager().getConfigurator().addSeedRendezvous(new URI("http://192.18.37.36:9700"));
            peer.getManager().getConfigurator().addSeedRelay(new URI("http://192.18.37.36:9700"));
            
            peer.getManager().getConfigurator().setUseMulticast(false);
    
            // Connect to the network.
            peer.startNetworkAndConnect(null, null);
        }
    }

    /**
     * Stop the network.
     * 
     * @throws Exception if problems occur.
     */
    @AfterClass
    public static void clean() throws Exception
    {
        if (peer != null) {
            peer.stopNetwork();
            FileUtil.deleteDirectory(WORKING_DIR);
        }
    }
}
