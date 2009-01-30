/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id$
 *
 */

package org.xwoot.jxta;


import java.io.File;
import java.io.IOException;
import java.util.*;

import net.jxta.discovery.*;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.impl.membership.none.NoneMembershipService;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;


/**
 * P2PFace: Peer-to-Peer protocol interface.
 *
 *          Provides a generic interface to a set of (very JXTA-like)
 *          p2p protocols.  Implementations are used to transparently access 
 *          either JXTA itself or a simulation of JXTA.
 *          
 * @version $Id:$
 */
public interface P2PFace {

	/**
     * Configure the network.
     * 
     * @param jxtaCacheDirectoryPath The location on where to save jxta related information for this peer.
     * @throws IOException if problems occur while initializing.
     **/
    void configureNetwork(File jxtaCacheDirectoryPath) throws IOException;
    
    
    /**
     * Start the network, connect to a RDV and set some parameters.
     *
     * @param jxtaCastListener event listener interested in {@link JxtaCastEvent}s.
     * 
     * @throws IllegalStateException if this is called before {@link #configureNetwork(File)}.
     * @throws PeerGroupException if problems occur while starting the Jxta platform.
     * @throws IOException if problems occur while starting the Jxta platform.
     **/
	void startNetworkAndConnect(JxtaCastEventListener jxtaCastListener) throws IllegalStateException, PeerGroupException, IOException;
	
	
	/**
	 * Stops the JXTA platform and disconnects from the network.
	 */
	void stopNetwork();
	
	
    /** Return my own peer name. */
    String getMyPeerName();


    /** Return advertisement for the default (initial) peer group. */
    PeerGroupAdvertisement getDefaultAdv();


    /** Return advertisement for my peer. */
    PeerAdvertisement getMyPeerAdv();


    /** Return the default (initial) peer group. */
    PeerGroup getDefaultGroup();


    /**
     * Launch a peer group discovery.
     * </p>
     * If this peer did not connect to the network, nothing will happen.
     * 
     * @param targetPeerId  - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events.  May be null,
     *                        if you don't want the notification.
     * @see #isConnectedToNetwork()
     */
    void discoverGroups(String targetPeerId, DiscoveryListener discoListener);

    	
    /**
     * Launch peer discovery, for the joined group.
     * </p>
     * If this peer did not join a group and did not contact(or is not) a group RDV, nothing will happen.
     * 
     * @param targetPeerId  - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events.  May be null,
     *                        if you don't want the notification.
     * @see #isConnectedToGroup()                       
     */
    void discoverPeers(String targetPeerId, DiscoveryListener discoListener);


    /**
     * Launch advertisement discovery, for the specified group.
     * </p>
     * If this peer did not join a group and did not contact(or is not) a group RDV, nothing will happen.
     * 
     * @param targetPeerId  - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events.  May be null,
     *                        if you don't want the notification.
     * @param attribute     - Limit responses to advertisements with this attribute/value pair.
     *                        Set to null to place no limit.
     * @param value         - See 'attribute', above.
     * @see #isConnectedToGroup()
     */
    void discoverAdvertisements(String targetPeerId,
                                       DiscoveryListener discoListener,
                                       String attribute,
                                       String value);


    /**
     * @return PeerGroupAdvertisement objects representing the groups known so far or {@code null} if this peer is not connected to the network.
     *  <b>Note:</b> this doesn't include the default "NetPeerGroup" advertisement.
     * @see #isConnectedToNetwork()
     */
    Enumeration<PeerGroupAdvertisement> getKnownGroups();


    /**
     * @return an enumerator to an array of PeerAdvertisement objects representing the peers known so far for the currently joined group or {@code null} if this peer did not join a group or has not contacted a RDV peer.
     * @see #isConnectedToNetwork()
     */
    Enumeration<PeerAdvertisement> getKnownPeers();


    /** 
     * @return Advertisement objects representing the advs known so far, that were created within the joined peer group. The list can be narrowed to advs matching an attribute/value pair.
     *
     * @param attribute - Limit responses to advertisements with this attribute/value pair.
     *                    Set to null to place no limit.
     * @param value     - See 'attribute', above.
     * 
     * @see #isConnectedToGroup()
     */
    Enumeration<Advertisement> getKnownAdvertisements(String attribute,
                                              String value);

    
    /**
     * @return an enumeration of connected RDV ids or null if this peer is not connected to a group.
     * @see #isConnectedToGroup()
     */
    Enumeration<ID> getConnectedRdvsIDs();
    
    
    /**
     * Create and join a new PeerGroup.  Also publishes the group advertisement.
     * </p>
     * This peer will automatically become a RDV for this group in order to enable communication in the new group.
     * </p>
     * If keystorePassword and identityPassword parameters are not null or empty, the new group will be a secure group using {@link PSEMembershipService}.
     *
     * @param  groupName        Name for the new group.
     * @param  description      Group description.
     * @param  keystorePassword the password of the local keystore.
     * @param  identityPassword the group's password.
     * @return The new peer group if successful, otherwise null.
     * @throws Exception if problems occur.
     */
    PeerGroup createNewGroup(String groupName, String description, char[] keystorePassword, char[] groupPassword) throws Exception;
    
    
    /** Join the specified PeerGroup.
     *  @param  groupAdv      Advertisement of the group to join.
     *  @param  beRendezvous  If true, act as a rendezvous for this group, else the peer will be automatically promoted to a RDV when needed.
     *  @return PeerGroup if we were successfully able to join the group, or
     *          if we had already joined it.
     *          null if we were unable to join the group.
     * @throws PeerGroupException if the group could not be joined.
     * @throws IOException if problems occur publishing the group.
     * @throws ProtocolNotSupportedException if problems occur while authenticating.
     */
    PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, char[] keystorePassword, char[] groupPassword,
                                                boolean beRendezvous) throws PeerGroupException, IOException, ProtocolNotSupportedException;


    /**
     * Leave a peer group.
     * </p>
     * Normally, this would be called after joining a new group to ensure that we are in only one peer group at a one time.
     * </p>
     * If we leave the current peer group, {@link #isConnectedToGroup()} should return false until we join another group.
     * </p>
     * If we try to leave the default peer group, nothing will happen. Also, nothing will happen if we try to leave a group when we are not connected to the network. 
     * 
     * @param oldGroup the group to leave.
     * @throws PeerGroupException if problems occur while leaving the group.
     */
    void leavePeerGroup(PeerGroup oldGroup) throws PeerGroupException;
    

    /**
     * Authenticate membership in a peer group using {@link PSEMembershipService}'s \"StringAuthentication\" method.
     * </p>
     * If both passwords are not provided, the authentication is made using {@link NoneMembershipService} and no authentication data is provided.
     * 
     * @param keystorePassword the password of the local keystore.
     * @param identityPassword the group's password.
     * 
     * @return true if successful, false if the provided passwords were not correct or joining failed.
     * @throws PeerGroupException if problems occurred joining the group.
     * @throws ProtocolNotSupportedException if problems occur authenticating credentials.
     */
    public boolean authenticateMembership(PeerGroup group, char[] keystorePassword, char[] identityPassword) throws PeerGroupException, ProtocolNotSupportedException;


    /**
     * @param peerAdv the peer advertisement of the peer to check.
     * @return true if the peer is a RDV, false otherwise or if it is not from the current joined group.
     */
    public boolean isRendezvous(PeerAdvertisement peerAdv);


	/**
	 * Register a listener for the messages broadcasted inside the joined group.
	 * 
	 * @param listener the listener. 
	 * @see net.jxta.jxtacast.JxtaCast#addJxtaCastEventListener(net.jxta.jxtacast.event.JxtaCastEventListener)
	 */
	public void addJxtaCastEventListener(JxtaCastEventListener listener);
	
	
	/**
	 * Remove a listener that will ignore messages broadcasted inside the joined group.
	 * 
	 * @param listener the listener.
	 * @see net.jxta.jxtacast.JxtaCast#removeJxtaCastEventListener(net.jxta.jxtacast.event.JxtaCastEventListener)
	 */
	public void removeJxtaCastEventListener(JxtaCastEventListener listener);

	
	/**
	 * Unreliably send a message to the whole joined group.
	 * 
	 * @param text the text message to send.
	 * 
	 * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
	 * 
	 * @see net.jxta.jxtacast.JxtaCast#sendChatMsg(java.lang.String)
	 */
	public void sendChatMsg(String text) throws PeerGroupException;
	

	/**
	 * Reliably send a variable-sized file to the whole joined group.
	 * 
	 * @param file the file to send.
	 * @param caption the caption describing the file.
	 * 
	 * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
	 * 
	 * @see net.jxta.jxtacast.JxtaCast#sendFile(java.io.File, java.lang.String)
	 */
	public void sendFile(File file, String caption) throws PeerGroupException;

	
	/**
	 * Reliably send a variable-sized object to the whole joined group.
	 * 
	 * @param object the object to send.
	 * @param caption the caption describing the object.
	 * 
	 * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
	 * 
	 * @see net.jxta.jxtacast.JxtaCast#sendObject(java.lang.Object, java.lang.String)
	 */
	public void sendObject(Object object, String caption) throws PeerGroupException;

	
	/**
	 * Handles RendezVous events in the joined group.
	 * </p>
	 * If it detects a new RDV, it will launch a peer and group discovery to that RDV to get his known peers/groups.
	 * 
	 * @param event the event generated by a RDV.
	 */
	public void rendezvousEvent(RendezvousEvent event);
	
	
	/** @return the currently joined peer group. <b>Note:</b> This will never return the default "netPeerGroup". To get that, use {@link #getDefaultGroup()}. */
	public PeerGroup getCurrentJoinedPeerGroup();
	
	
	/** @return true if this peer has started the jxta platform. */
	public boolean isJxtaStarted();
	
	
	/** @return true if this peer has joined a group, other than the default netPeerGroup. */
	public boolean hasJoinedAGroup();
	
	
	/** @return true if this peer is connected to the network and can start querying it. */
	public boolean isConnectedToNetwork();
	
	
	/** @return true if this peer is connected to the joined group and can start querying it. */
	public boolean isConnectedToGroup();
	
	
	/** @return true if this peer is a RendezVous for the network. */
	public boolean isNetworkRendezVous();
	
	
	/** @return true if this peer is a RendezVous for the joined group, if it has joined a group. */
	public boolean isGroupRendezVous();
	
	
	/** @return true if this peer is connected to a RendezVous of the network or is itself a RendezVous for the network. */
	public boolean isConnectedToNetworkRendezVous();
	
	
	/** @return true if this peer has connected to a group's RendezVous or is itself a RendezVous for the joined group.*/
	public boolean isConnectedToGroupRendezVous();
	
	
	/** @return true if the jxta platform has been configured by calling the {@link #configureNetwork(String)} method and can be started or stopped. */
	public boolean isNetworkConfigured();
}
