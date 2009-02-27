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

package org.xwoot.jxta.test.multiplePeers;

import java.io.File;
import java.util.Enumeration;

import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.xwoot.jxta.DirectMessageReceiver;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.test.AbstractJxtaTestBase;
import org.xwoot.jxta.test.MultiplePeersTest;
import org.xwoot.xwootUtil.FileUtil;

/**
 * TODO DOCUMENT ME!
 *
 * @version $Id:$
 */
public abstract class AbstractMultiplePeersTestCase implements MultiplePeersTestCase, JxtaCastEventListener, DirectMessageReceiver
{
    protected String peerName;
    protected Boolean networkCreator;
    protected Boolean groupCreator;
    protected Peer peer;
    protected String groupName;
    
    /** {@inheritDoc} **/
    public Boolean init(String peerName, Boolean networkCreator)
    {
        this.peerName = peerName;
        System.out.println(this.peerName + " : Init requested (" + peerName + ", " + networkCreator + ").");
        
        this.networkCreator = networkCreator;
        
        this.peer = PeerFactory.createPeer();
        
        File peerHome = new File(AbstractJxtaTestBase.WORKING_DIR, peerName);
        FileUtil.deleteDirectory(peerHome);
        
        try {
            FileUtil.checkDirectoryPath(peerHome.getPath());
            this.peer.configureNetwork(peerHome, (networkCreator ? ConfigMode.RENDEZVOUS_RELAY : ConfigMode.EDGE));
            this.peer.setMyPeerName(peerName);
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /** {@inheritDoc} **/
    public Boolean connect()
    {
        System.out.println(this.peerName + " : Connect requested");
        
        try {
            this.peer.startNetworkAndConnect(this, this);
        } catch (Exception e) {
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }

    /** {@inheritDoc} **/
    public Boolean start(Boolean groupCreator, String groupName)
    {
        System.out.println(this.peerName + " : Start requested (" + groupCreator + ", " + groupName + ").");
        
        this.groupCreator = groupCreator;
        this.groupName = groupName;
        
        new Thread(this, this.getClass().getName() + "Peer: " + this.peerName).start();
        
        return Boolean.TRUE;
    }
    
    /** {@inheritDoc} **/
    public void fail(String message)
    {
        System.out.println(this.peerName + " : Thread Failed. Stopping.");
        System.setProperty("errors", message);
        
        synchronized (MultiplePeersTest.MAIN_THREAD_LOCK) {
            // notify main thread not to wait for this thread anymore.
            MultiplePeersTest.MAIN_THREAD_LOCK.notifyAll();
        }
        
        throw new RuntimeException(message);
    }
    
    public void pass()
    {
        System.out.println(this.peerName + " : Thread stopped. The network will stop as well.");
        System.setProperty("success", "true");
        synchronized (MultiplePeersTest.MAIN_THREAD_LOCK) {
            // notify main thread not to wait for this thread anymore.
            MultiplePeersTest.MAIN_THREAD_LOCK.notifyAll();
        }
    }
    
    public PeerGroupAdvertisement searchForGroup(String groupName)
    {
        PeerGroupAdvertisement result = null;
        
        do {
            System.out.println(this.peerName + " : Searching for the group to join.");
            
            Enumeration<PeerGroupAdvertisement> groups = this.peer.getKnownGroups();
            while (groups.hasMoreElements()) {
                PeerGroupAdvertisement groupAdv = (PeerGroupAdvertisement) groups.nextElement();
                System.out.println(this.peerName + " : Trying group: " + groupAdv.getName());
                if (groupAdv.getName().equals(groupName)) {
                    System.out.println(this.peerName + " : Group Found!");
                    result = groupAdv;
                    break;
                }
            }
            
            if (result != null) {
                break;
            }
            
            System.out.println(this.peerName + " : Group Not found yet.");
            
            synchronized (MultiplePeersTest.GROUP_ADV_LOCK) {
                try {
                    System.out.println(this.peerName + " : waiting.");
                    MultiplePeersTest.GROUP_ADV_LOCK.wait(10000);
                    System.out.println(this.peerName + " : 10 seconds passed?.");
                } catch (InterruptedException ignore) {
                }
            }
        } while (result == null);
        
        return result;
    }
}
