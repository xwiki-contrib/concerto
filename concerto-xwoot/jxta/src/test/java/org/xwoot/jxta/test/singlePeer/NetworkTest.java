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

package org.xwoot.jxta.test.singlePeer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;

/**
 * Test the network.
 *
 * @version $Id$
 */
public class NetworkTest extends AbstractSinglePeerTestBase
{
    @Before
    public void init() throws Exception
    {
        if (!PEER.isConnectedToNetwork()) {
            PEER.startNetworkAndConnect(null, null);
        }
    }
    
    @After
    public void destroy() throws Exception
    {
        PEER.leavePeerGroup();
    }
    
    /**
     * Can't start network before we configure our network and local settings.
     * 
     * @throws Exception expected to throw an IllegalStateException.
     */
    @Test(expected=IllegalStateException.class)
    public void testStartNetwork() throws Exception
    {
        Peer aPeer = PeerFactory.createPeer();
        try {
            aPeer.startNetworkAndConnect(null, null);
        } finally {
            if (aPeer != null) {
                aPeer.stopNetwork();
            }
        }
    }
    
    /**
     * Connect to our custom rdv.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testConnectToNetwork() throws Exception
    {
        Assert.assertTrue(PEER.isConnectedToNetwork());
    }
    
    
    /**
     * Connect to a network, disconnect and then reconnect.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testReConnectToNetwork() throws Exception
    {
        //System.out.println("Connection test.");
        Assert.assertTrue(PEER.isConnectedToNetwork());
        
        //System.out.println("STOP");
        PEER.stopNetwork();
//        System.out.println("Connection test.");
//        System.out.println("Status: ");
//        System.out.println("jxta.isNetworkConfigured() : " + peer.isNetworkConfigured());
//        System.out.println("jxta.isJxtaStarted() : " + peer.isJxtaStarted()); 
//        System.out.println("jxta.isConnectedToNetworkRendezVous() : " + peer.isConnectedToNetworkRendezVous());
//        System.out.println("jxta.isConnectedToNetwork() : " + peer.isConnectedToNetwork());
//        System.out.println("jxta.hasJoinedAGroup() : " + peer.hasJoinedAGroup());
//        System.out.println("jxta.isGroupRendezVous() : " + peer.isGroupRendezVous());
//        System.out.println("jxta.isConnectedToGroupRendezVous() : " + peer.isConnectedToGroupRendezVous());
//        System.out.println("jxta.isConnectedToGroup() : " + peer.isConnectedToGroup());
        Assert.assertFalse(PEER.isConnectedToNetwork());
        
//        System.out.println("START");
        PEER.startNetworkAndConnect(null, null);
//        System.out.println("Connection test.");
        Assert.assertTrue(PEER.isConnectedToNetwork());
    }
    
    /**
     * Fail to connect because seed did not respond or no seed was provided.
     * Result: a {@link JxtaException} will be thrown.
     * 
     * @throws Exception if problems occur.
     */
    /*FIXME: jxta 2.5 bug affects this too. Try 2.6-snapshot
     * @Test(expected = JxtaException.class)
    public void testFailConnectToNetwork() throws Exception
    {
        Assert.assertTrue(peer.isConnectedToNetwork());
        
        // disconnect.
        peer.stopNetwork();
        Assert.assertFalse(peer.isConnectedToNetwork());
        
        // remove any seeds.
        NetworkConfigurator networkConfig = peer.getManager().getConfigurator();
        networkConfig.clearRelaySeedingURIs();
        networkConfig.clearRelaySeeds();
        networkConfig.clearRendezvousSeedingURIs();
        networkConfig.clearRendezvousSeeds();
        
        peer.getManager().setUseDefaultSeeds(false);
        
        try {
            // try to connect to nothing and fail.
            peer.startNetworkAndConnect(null, null);
        } catch(Exception e) {
            Assert.assertFalse(peer.isJxtaStarted());
            Assert.assertFalse(peer.isConnectedToNetwork());
            throw e;
        }
    }*/
}
