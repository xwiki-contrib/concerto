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
import java.io.Serializable;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.exception.JxtaException;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;

/**
 * Peer: Peer-to-Peer protocol interface. Provides a generic interface to a set of (very JXTA-like) p2p protocols.
 * Implementations are used to transparently access either JXTA itself or a simulation of JXTA.
 * 
 * @version $Id$
 */
public interface Peer
{

    /**
     * Configure the network.
     * 
     * @param jxtaCacheDirectoryPath The location on where to save jxta related information for this peer.
     * @param mode The mode in which this peer will be running.
     * @see ConfigMode
     * @throws IOException if problems occur while initializing.
     **/
    void configureNetwork(File jxtaCacheDirectoryPath, ConfigMode mode) throws IOException;

    /**
     * @return the NetworkManager instance created by {@link #configureNetwork(File)} that allows tweaking the peer's
     *         configuration.
     **/
    NetworkManager getManager();

    /**
     * Start the network, connect to a RDV and set some parameters.
     * 
     * @param jxtaCastListener event listener interested in {@link JxtaCastEvent}s.
     * @throws IllegalStateException if this is called before {@link #configureNetwork(File)}.
     * @throws PeerGroupException if problems occur while starting the Jxta platform.
     * @throws IOException if problems occur while starting the Jxta platform.
     **/
    void startNetworkAndConnect(JxtaCastEventListener jxtaCastListener) throws IllegalStateException,
        PeerGroupException, IOException;

    /**
     * Stops the JXTA platform and disconnects from the network.
     */
    void stopNetwork();

    /** @return my own peer name. */
    String getMyPeerName();

    /** @return advertisement for the default (initial) peer group. */
    PeerGroupAdvertisement getDefaultAdv();

    /** @return advertisement for my peer. */
    PeerAdvertisement getMyPeerAdv();
    
    /** @return the pipe name prefix used for the JxtaCast back channel input pipe. */
    public String getBackChannelPipeNamePrefix();
    
    /** @return the pipe advertisement used by this peer for the JxtaCast back channel input pipe. */
    public PipeAdvertisement getMyBackChannelPipeAdvertisement();
    
    /** @return the pipe name used by this peer for the JxtaCast back channel input pipe. */
    public String getMyBackChannelPipeName();
    
    /** @return the {@link JxtaCast} instance associated with this peer. */
    public JxtaCast getJxtaCastInstance();

    /** @return the default (initial) peer group. */
    PeerGroup getDefaultGroup();

    /**
     * Launch a peer group discovery.
     * <p>
     * If this peer did not connect to the network, nothing will happen.
     * 
     * @param targetPeerId - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events. May be null, if you don't want the notification.
     * @see #isConnectedToNetwork()
     */
    void discoverGroups(String targetPeerId, DiscoveryListener discoListener);

    /**
     * Launch peer discovery, for the joined group.
     * <p>
     * If this peer did not join a group and did not contact(or is not) a group RDV, nothing will happen.
     * 
     * @param targetPeerId - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events. May be null, if you don't want the notification.
     * @see #isConnectedToGroup()
     */
    void discoverPeers(String targetPeerId, DiscoveryListener discoListener);

    /**
     * Launch advertisement discovery, for the specified group.
     * <p>
     * If this peer did not join a group and did not contact(or is not) a group RDV, nothing will happen.
     * 
     * @param targetPeerId - limit to responses from this peer, or null for no limit.
     * @param discoListener - listener for discovery events. May be null, if you don't want the notification.
     * @param attribute - Limit responses to advertisements with this attribute/value pair. Set to null to place no
     *            limit.
     * @param value - See 'attribute', above.
     * @see #isConnectedToGroup()
     */
    void discoverAdvertisements(String targetPeerId, DiscoveryListener discoListener, String attribute, String value);

    /**
     * @return PeerGroupAdvertisement objects representing the groups known so far or {@code null} if this peer is not
     *         connected to the network. <b>Note:</b> this doesn't include the default "NetPeerGroup" advertisement.
     * @see #isConnectedToNetwork()
     */
    Enumeration<PeerGroupAdvertisement> getKnownGroups();

    /**
     * @return an enumerator to an array of PeerAdvertisement objects representing the peers known so far for the
     *         currently joined group or {@code null} if this peer did not join a group or has not contacted a RDV peer.
     * @see #isConnectedToNetwork()
     */
    Enumeration<PeerAdvertisement> getKnownPeers();

    /**
     * @return Advertisement objects representing the advs known so far, that were created within the joined peer group.
     *         The list can be narrowed to advs matching an attribute/value pair.
     * @param attribute - Limit responses to advertisements with this attribute/value pair. Set to null to place no
     *            limit.
     * @param value - See 'attribute', above.
     * @see #isConnectedToGroup()
     */
    Enumeration<Advertisement> getKnownAdvertisements(String attribute, String value);

    /**
     * @return an enumeration of connected RDV ids or null if this peer is not connected to a group.
     * @see #isConnectedToGroup()
     */
    Enumeration<ID> getConnectedRdvsIDs();

    /**
     * Create and join a new PeerGroup. Also publishes the group advertisement.
     * <p>
     * This peer will automatically become a RDV for this group in order to enable communication in the new group.
     * <p>
     * If keystorePassword and identityPassword parameters are not null or empty, the new group will be a secure group
     * using {@link net.jxta.impl.membership.pse.PSEMembershipService PSEMembershipService}.
     * 
     * @param groupName Name for the new group.
     * @param description Group description.
     * @param keystorePassword the password of the local keystore.
     * @param groupPassword the group's password.
     * @return The new peer group if successful, otherwise null.
     * @throws Exception if problems occur.
     */
    PeerGroup createNewGroup(String groupName, String description, char[] keystorePassword, char[] groupPassword)
        throws Exception;

    /**
     * Convenience method for creating a public peer group.
     * 
     * @see #createNewGroup(String, String, char[], char[])
     * @param groupName Name for the new group.
     * @param description Group description.
     * @return The new peer group if successful, otherwise null.
     * @throws Exception if problems occur.
     */
    PeerGroup createNewGroup(String groupName, String description) throws Exception;

    /**
     * Join the specified PeerGroup.
     * <p>
     * Upon join, the peer will automatically become a RDV for this group in order to enable communication. If {@code
     * beRendezvous} was not set to true, the peer will be demoted back to a normal Edge peer when the network can
     * support this but will also be promoted back to RDV when the network will need it.
     * <p>
     * If keystorePassword and identityPassword parameters are not null or empty, the new group will be a secure group
     * using  {@link NetworkManager.. {@link net.jxta.impl.membership.pse.PSEMembershipService PSEMPSEMembershipService}, else it will be a public group and use {@link net.jxta.impl.membership.none.NoneMembershipService NoneMembershipService}.
     * 
     * @param groupAdv Advertisement of the group to join.
     * @param keystorePassword The local keystore password.
     * @param groupPassword The group's password required for joining.
     * @param beRendezvous If true, act as a rendezvous for this group, else the peer will be automatically promoted to
     *            a RDV or demoted back to an EDGE peer when needed.
     * @return PeerGroup if we were successfully able to join the group, or if we had already joined it. null if we were
     *         unable to join the group. 
     * @throws PeerGroupException if the group could not be joined.
     * @throws IOException if problems occur publishing the group.
     * @throws ProtocolNotSupportedException if problems occur while authenticating.
     */
    PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, char[] keystorePassword, char[] groupPassword,
        boolean beRendezvous) throws PeerGroupException, IOException, ProtocolNotSupportedException;

    /**
     * Convenience method for joining a public group.
     * 
     * @see #joinPeerGroup(PeerGroupAdvertisement, char[], char[], boolean)
     * @param groupAdv Advertisement of the group to join.
     * @param beRendezvous If true, act as a rendezvous for this group, else the peer will be automatically promoted to
     *            a RDV when needed.
     * @return PeerGroup if we were successfully able to join the group, or if we had already joined it. null if we were
     *         unable to join the group.
     * @throws PeerGroupException if the group could not be joined.
     * @throws IOException if problems occur publishing the group.
     * @throws ProtocolNotSupportedException if problems occur while authenticating.
     */
    PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, boolean beRendezvous) throws PeerGroupException,
        IOException, ProtocolNotSupportedException;

    /**
     * Leave a peer group.
     * <p>
     * Normally, this would be called after joining a new group to ensure that we are in only one peer group at a one
     * time.
     * <p>
     * If we leave the current peer group, {@link #isConnectedToGroup()} should return false until we join another
     * group.
     * <p>
     * If we try to leave the default peer group, nothing will happen. Also, nothing will happen if we try to leave a
     * group when we are not connected to the network.
     * 
     * @param oldGroup the group to leave.
     * @throws PeerGroupException if problems occur while leaving the group.
     */
    void leavePeerGroup(PeerGroup oldGroup) throws PeerGroupException;
    
    /**
     * Convenince method to leave the joined peer group.
     * 
     * @see #leavePeerGroup(PeerGroup)
     * 
     * @throws PeerGroupException if problems occur while leaving the group.
     */
    void leavePeerGroup() throws PeerGroupException;

    /**
     * @param peerAdv the peer advertisement of the peer to check.
     * @return true if the peer is a RDV, false otherwise or if it is not from the current joined group.
     */
    boolean isRendezvous(PeerAdvertisement peerAdv);

    /**
     * Register a listener for the messages broadcasted inside the joined group.
     * 
     * @param listener the listener.
     * @see net.jxta.jxtacast.JxtaCast#addJxtaCastEventListener(net.jxta.jxtacast.event.JxtaCastEventListener)
     */
    void addJxtaCastEventListener(JxtaCastEventListener listener);

    /**
     * Remove a listener that will ignore messages broadcasted inside the joined group.
     * 
     * @param listener the listener.
     * @see net.jxta.jxtacast.JxtaCast#removeJxtaCastEventListener(net.jxta.jxtacast.event.JxtaCastEventListener)
     */
    void removeJxtaCastEventListener(JxtaCastEventListener listener);

    /**
     * Unreliably send a message to the whole joined group.
     * 
     * @param text the text message to send.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @see net.jxta.jxtacast.JxtaCast#sendChatMsg(java.lang.String)
     */
    void sendChatMsg(String text) throws PeerGroupException;

    /**
     * Reliably send a variable-sized file to the whole joined group.
     * 
     * @param file the file to send.
     * @param caption the caption describing the file.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @see net.jxta.jxtacast.JxtaCast#sendFile(java.io.File, java.lang.String)
     */
    void sendFile(File file, String caption) throws PeerGroupException;

    /**
     * Reliably send a variable-sized object to the whole joined group.
     * 
     * @param object the object to send.
     * @param caption the caption describing the object.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @throws IllegalArgumentException if the object does not implement {@link Serializable}.
     * @see net.jxta.jxtacast.JxtaCast#sendObject(java.lang.Object, java.lang.String)
     */
    void sendObject(Object object, String caption) throws PeerGroupException;
    
    /**
     * Send a variable-sized object to a peer.
     * 
     * @param object the object to send.
     * @param caption the caption describing the object.
     * @param pipeAdv the pipe advertisement where the destination peer listens for messages.
     * @return true if successfuly sent. false is returned if the message could not be sent due to network over-load but you can try to send it again later.
     * @throws PeerGroupException if this peer has not yet joined a group other than NetPeerGroup and contacted its RDV.
     * @throws IllegalArgumentException if the object does not implement {@link Serializable}.
     * @see net.jxta.jxtacast.JxtaCast#sendObject(java.lang.Object, java.lang.String)
     */
    public boolean sendObject(Object object, String caption, PipeAdvertisement pipeAdv) throws JxtaException;

    /**
     * Handles RendezVous events in the joined group.
     * <p>
     * If it detects a new RDV, it will launch a peer and group discovery to that RDV to get his known peers/groups.
     * 
     * @param event the event generated by a RDV.
     */
    void rendezvousEvent(RendezvousEvent event);

    /**
     * @return the currently joined peer group. <b>Note:</b> This will never return the default "netPeerGroup". To get
     *         that, use {@link #getDefaultGroup()}.
     */
    PeerGroup getCurrentJoinedPeerGroup();

    /** @return true if this peer has started the jxta platform. */
    boolean isJxtaStarted();

    /** @return true if this peer has joined a group, other than the default netPeerGroup. */
    boolean hasJoinedAGroup();

    /** @return true if this peer is connected to the network and can start querying it. */
    boolean isConnectedToNetwork();

    /** @return true if this peer is connected to the joined group and can start querying it. */
    boolean isConnectedToGroup();

    /** @return true if this peer is a RendezVous for the network. */
    boolean isNetworkRendezVous();

    /** @return true if this peer is a RendezVous for the joined group, if it has joined a group. */
    boolean isGroupRendezVous();

    /**
     * @return true if this peer is connected to a RendezVous of the network or is itself a RendezVous for the network.
     **/
    boolean isConnectedToNetworkRendezVous();

    /**
     * @return true if this peer has connected to a group's RendezVous or is itself a RendezVous for the joined group.
     */
    boolean isConnectedToGroupRendezVous();

    /**
     * @return true if the jxta platform has been configured by calling the {@link #configureNetwork(String)} method and
     *         can be started or stopped.
     */
    boolean isNetworkConfigured();
}
