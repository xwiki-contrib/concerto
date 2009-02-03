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

import junit.framework.Assert;

import org.junit.Test;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;

/**
 * Test the network.
 *
 * @version $Id:$
 */
public class NetworkTest extends AbstractJxtaTestBase
{
    /**
     * Can't start network before we configure our network and local settings.
     * 
     * @throws Exception expected to throw an IllegalStateException.
     */
    @Test(expected=IllegalStateException.class)
    public void testStartNetwork() throws Exception
    {
        Peer aPeer = PeerFactory.createPeer();
        aPeer.startNetworkAndConnect(null);
    }
    
    /**
     * Connect to our custom rdv.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testConnectToNetwork() throws Exception
    {
        Assert.assertTrue(peer.isConnectedToNetwork());
    }
}
