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

package org.xwoot.jxta;

import java.security.cert.X509Certificate;
import java.util.*;
import java.io.File;
import java.io.IOException;

import javax.crypto.EncryptedPrivateKeyInfo;

import net.jxta.discovery.*;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.none.NoneMembershipService;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.PSEUtils;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.impl.protocol.PSEConfigAdv;
import net.jxta.jxtacast.JxtaCast;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.*;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.*;
import net.jxta.rendezvous.*;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.XMLDocument;

/**
 * Implementation handling the gory details of JXTA. 
 *              
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class JxtaPeer implements Peer, RendezvousListener {

    protected PeerGroup rootGroup;
    protected PeerGroup currentJoinedGroup;
    //protected Vector<PeerGroup> joinedGroups;  // Holds PeerGroup objects that we've joined.
    protected NetworkManager manager;
    protected JxtaCast jc;
    protected Credential groupCredential;
    
    /** The pipe name to be used when broadcasting messages. Interested peers will look for this. */
	public static final String PIPE_ADVERTISEMENT_NAME = "ConcertoMessageBroadcast";
	
//    /** Constructor - Starts JXTA.
//     */
//    public JxtaPeer() {
//        //joinedGroups = new Vector<PeerGroup>(20);
//    }
    
	/** {@inheritDoc} **/
	public void configureNetwork(File jxtaCacheDirectoryPath, ConfigMode mode) throws IOException
	{
	    if (jxtaCacheDirectoryPath == null) {
            jxtaCacheDirectoryPath = new File(new File(".cache"), "ConcertoPeer"
                    + UUID.randomUUID().toString());
        }
        
        manager = new NetworkManager(mode, "ConcertoPeer",
            jxtaCacheDirectoryPath.toURI());

        // Use JXTA default relay/rendezvous servers for now.
        // manager.setUseDefaultSeeds(true);
        //manager.getConfigurator().addSeedRelay(URI.create("tcp://192.18.37.39:9701"));
        //manager.getConfigurator().addSeedRendezvous(URI.create("tcp://192.18.37.39:9701"));
        
        // FIXME: Leave such configurations to be made from outside
        // after calling this method but before calling startNetworkAndConnect.
        //NetworkConfigurator config = manager.getConfigurator();
        //manager.getConfigurator().setUseMulticast(false);
        
        System.out.println("Infrastructure ID: " + manager.getInfrastructureID());
        System.out.println("Peer ID: " + manager.getPeerID());
	}
    
    
    /** {@inheritDoc} */
    public NetworkManager getManager()
    {
        return this.manager;
    }

    
    /** {@inheritDoc} **/
	public void startNetworkAndConnect(JxtaCastEventListener jxtaCastListener) throws IllegalStateException,
			PeerGroupException, IOException {
		if (!this.isNetworkConfigured()) {
			throw new IllegalStateException(
					"The manager has not yet been instantiated and configured. Call configureNetwork() first.");
		}
		
		if (this.isConnectedToNetwork()) {
			System.out.println("Already connected to the network.");
			return;
		}

		// Start the network.
		this.manager.startNetwork();

		// Get the NetPeerGroup and use this for now.
		this.rootGroup = manager.getNetPeerGroup();
		
		//this.joinedGroups.add(this.rootGroup);
		this.currentJoinedGroup = this.rootGroup;

		// Contribute to the network's conectivity.
		this.rootGroup.getRendezVousService().setAutoStart(true);

		// Connect to the Network entry-point (Rendezvous).
		if (!manager.waitForRendezvousConnection(120000)) {
			System.err
					.println("Unable to connect to rendezvous server. Stoping.");
			this.stopNetwork();
			return;
		}

		// Register ourselves to detect new RDVs that broadcast their presence.
		this.rootGroup.getRendezVousService().addListener(this);
		
//		// Init JxtaCast with the rootGroup.
//		this.jc = new JxtaCast(this.getMyPeerAdv(), this.rootGroup, PIPE_ADVERTISEMENT_NAME);
//		this.jc.addJxtaCastEventListener(jxtaCastListener);
//		JxtaCast.logEnabled = true;
		
		// Do a discovery for available groups.
		discoverGroups(null, null);
	}
	
	
	/** {@inheritDoc} **/
	public void stopNetwork() {
		if (this.isNetworkConfigured()) {
			// Try to leave the current group nicely.
			try {
				this.leavePeerGroup(currentJoinedGroup);
			} catch (Exception e) {
				// ignore, we are shutting down anyway.
			}
			
			manager.stopNetwork();
			this.rootGroup = null;
			this.currentJoinedGroup = null;
		}
	}

	
	/** {@inheritDoc} **/
    public PeerGroupAdvertisement getDefaultAdv() {
        return rootGroup.getPeerGroupAdvertisement();
    }

    
    /** {@inheritDoc} **/
    public PeerGroup getDefaultGroup() {
        return rootGroup;
    }
    
    
    /** {@inheritDoc} **/
    public String getMyPeerName() {
        return this.getMyPeerAdv().getName();
    }
    
    
    /** {@inheritDoc} **/
    public PeerAdvertisement getMyPeerAdv() {
        return rootGroup.getPeerAdvertisement();
    }


    /** {@inheritDoc} **/
    public void discoverGroups(String targetPeerId, DiscoveryListener discoListener) {

    	if (!this.isConnectedToNetwork()) {
    		return;
    	}
    	
        DiscoveryService disco = rootGroup.getDiscoveryService();
		
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.GROUP,
                                             null,
                                             null,
                                             discoListener);
        thread.start();
    }


    /** {@inheritDoc} **/
    public void discoverPeers(String targetPeerId,
                              //PeerGroupAdvertisement group,
                              DiscoveryListener discoListener) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.)
        //
//        PeerGroup pg = findJoinedGroup(group);
//        if (pg == null)
//            return;
    	
    	if (!this.isConnectedToGroup()) {
    		return;
    	}

        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();
        /*
        Enumeration<PeerAdvertisement> peers = getKnownPeers(group);
        while (peers.hasMoreElements()) {
        	try {
				disco.flushAdvertisement(peers.nextElement());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        */
        
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.PEER,
                                             null,
                                             null,
                                             discoListener);
        thread.start();
    }


    /** {@inheritDoc} **/
    public void discoverAdvertisements(String targetPeerId,
                                       //PeerGroupAdvertisement group,
                                       DiscoveryListener discoListener,
                                       String attribute,
                                       String value) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.)
        //
//        PeerGroup pg = findJoinedGroup(group);
//        if (pg == null)
//            return;
    	
    	if (!this.isConnectedToGroup()) {
    		return;
    	}

        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();
        DiscoThread thread = new DiscoThread(disco,
                                             targetPeerId,
                                             DiscoveryService.ADV,
                                             attribute,
                                             value,
                                             discoListener);
        thread.start();
    }


    /** {@inheritDoc} **/
	@SuppressWarnings("unchecked")
	public Enumeration<PeerGroupAdvertisement> getKnownGroups() {

		if (!this.isConnectedToNetwork()) {
    		System.out.println("Warning: Not conencted to network.");
    		return null;
    	}
    	
        Enumeration en = null;
        DiscoveryService disco = rootGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.GROUP, null, null);
        } catch (Exception e) {
            System.err.println("Failed to get local group advertisements.\n");
            e.printStackTrace();
        }
        
        // Look for new groups to add to the local repository.
        discoverGroups(null, null);

        return (Enumeration<PeerGroupAdvertisement>) en;
    }
    

    /** {@inheritDoc} **/
    @SuppressWarnings("unchecked")
	public Enumeration<PeerAdvertisement> getKnownPeers(/*PeerGroupAdvertisement group*/) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.
        //
        /*PeerGroup pg = findJoinedGroup(group);
        if (pg == null)
            return null;*/
    	
    	if (!this.isConnectedToNetwork()) {
    		System.out.println("Warning: Not conencted to network.");
    		return null;
    	}

        Enumeration en = null;
        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.PEER, null, null);
        } catch (Exception e) {
            System.err.println("Failed to get locally stored known peers.\n");
            e.printStackTrace();
        }
        
        discoverPeers(null, null);

        return (Enumeration<PeerAdvertisement>) en;
    }


    /** {@inheritDoc} **/
    public Enumeration<Advertisement> getKnownAdvertisements(/*PeerGroupAdvertisement group,
                                              */String attribute,
                                              String value) {

        // Find the PeerGroup for this adv.  If we haven't joined the group,
        // we can't do the discovery.  (We get the DiscoveryService object from the
        // PeerGroup.
        //
//        PeerGroup pg = findJoinedGroup(group);
//        if (pg == null)
//            return null;
    	
    	if (!this.isConnectedToNetwork()) {
    		System.out.println("Warning: Not conencted to network.");
    		return null;
    	}

        Enumeration<Advertisement> en = null;
        DiscoveryService disco = this.currentJoinedGroup.getDiscoveryService();

        try {
            en = disco.getLocalAdvertisements(DiscoveryService.ADV, attribute, value);
        } catch (Exception e) {
            System.err.println("Failed to get locally stored known advertisements.\n");
            e.printStackTrace();
        }
        
        discoverAdvertisements(null, null, attribute, value);

        return en;
    }
    
    
    /** {@inheritDoc} **/
    public Enumeration<ID> getConnectedRdvsIDs() {
    	if (!this.isConnectedToGroup()) {
    		return null;
    	}
    	
    	return this.rootGroup.getRendezVousService().getConnectedRendezVous();
    }


    /** {@inheritDoc} **/
    public PeerGroup createNewGroup(String groupName, String description, char[] keystorePassword, char[] groupPassword) throws Exception {

    	DiscoveryService disco = rootGroup.getDiscoveryService();
    	
        PeerGroup pg;               // new peer group
        //PeerGroupAdvertisement adv; // advertisement for the new peer group
        
        // Create a new all purpose peergroup.
        ModuleImplAdvertisement newGroupImpl = null;
        
        // If a keystorePassword and groupPassword has been specified, set the membershipService to PSEMembershipService.
        if (keystorePassword != null && keystorePassword.length != 0 && groupPassword != null && groupPassword.length != 0) {
	       
        	newGroupImpl = buildNewGroupImplAdvertisementWithPSE(rootGroup, groupName);
       
	        //System.out.println("ALTERED PEER GROUP IMPL ADV CONTAINING PSEMEMBERSHIPSERVICE, NO PSECONFIG:\n" + newGroupImpl);
	        
	        // Advertise this altered module impl adv
	        disco.remotePublish(newGroupImpl);
	        disco.publish(newGroupImpl);
	        
	        // Generate self-signed certificate and encrypt the private key from this certificate.
	        PSEUtils.IssuerInfo groupAuthenticationData = PSEUtils.genCert(manager.getInstanceName(), null);
	        EncryptedPrivateKeyInfo encryptedGroupPrivateKey = PSEUtils.pkcs5_Encrypt_pbePrivateKey(
	        		groupPassword, groupAuthenticationData.issuerPkey, 1000);

	        // Build PeerGroupAdvertisement for the new group with PSE authentication data in it.
	        X509Certificate[] certificateChain = { groupAuthenticationData.cert };
	        
	        PeerGroupAdvertisement newGroupAdv = buildGroupAdvWithPSE(
	        		groupName, description, newGroupImpl, certificateChain, encryptedGroupPrivateKey);
	  
	        // Publish it.
	        disco.publish(newGroupAdv);
	        disco.remotePublish(newGroupAdv);
	  
	        // create a group from it.
	        // rootGroup.loadModule(newGroupImpl.getID(), newGroupImpl);
	        pg = rootGroup.newGroup(newGroupAdv);
        } else {
        	// Create a public group.
        	newGroupImpl = rootGroup.getAllPurposePeerGroupImplAdvertisement();
        	pg = rootGroup.newGroup(null,         // Assign new group ID
        							newGroupImpl, // The implem. adv
        							groupName,    // The name
        							description); // Helpful descr.
        }
        
        
        //System.out.println("NEW PEER GROUP ADV:\n" + pg.getPeerGroupAdvertisement());
 
        // We join the new group as well.
        if (!authenticateMembership(pg, keystorePassword, groupPassword)) {
        	throw new Exception("Authentication failed for the new group!");
        }

        // Become rdv for this new group. Peers will not be able to communicate if there is no rdv in this group.
        pg.getRendezVousService().startRendezVous();

  /*      System.out.println("Connected RDVs: ");
        Enumeration<ID> rdvs = pg.getRendezVousService().getConnectedRendezVous();
        while (rdvs.hasMoreElements()) {
        	System.out.println("Rdv: " + rdvs.nextElement());
        }
        
        System.out.println("Connected Peers: ");
        Enumeration<ID> peers = pg.getRendezVousService().getConnectedPeers();
        while (peers.hasMoreElements()) {
        	System.out.println("Peer: " + peers.nextElement());
        }
        
        System.out.println("Connected to RDV?" + pg.getRendezVousService().isConnectedToRendezVous());
        System.out.println("Is RDV?" + pg.getRendezVousService().isRendezVous());
     */
        
        // Not sure how much of this is needed; this might be overkill.
        disco.remotePublish(pg.getPeerGroupAdvertisement());
        disco.publish(pg.getPeerGroupAdvertisement());

        // Add the new group to our list of joined groups.
        //joinedGroups.add(pg);
        
        // If we were previously a member of a group, we have to leave it now.
        if (this.isConnectedToGroup()) {
        	this.leavePeerGroup(this.currentJoinedGroup);
        }
        
        // Set the new group as the current joined group.
        this.currentJoinedGroup = pg;
        
        // Init JxtaCast if null.
        if (jc == null ) {
        	jc = new JxtaCast(currentJoinedGroup.getPeerAdvertisement(), currentJoinedGroup, PIPE_ADVERTISEMENT_NAME);
        	JxtaCast.logEnabled = true;
        }
        
        // Set as JxtaCast peer group.
        jc.setPeerGroup(pg);

        return pg;
    }
    
    
    /** {@inheritDoc} **/
    public PeerGroup createNewGroup(String groupName, String description) throws Exception {
        return this.createNewGroup(groupName, description, null, null);
    }
    
    
    /** {@inheritDoc} **/
    public synchronized PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, char[] keystorePassword, char[] groupPassword,
                                                boolean beRendezvous) throws PeerGroupException, IOException, ProtocolNotSupportedException {

        // See if it's a group we've already joined.
        if (this.currentJoinedGroup != null && groupAdv.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
        	System.out.println("Already joined.");
        	return this.currentJoinedGroup;
        }
        	
    	// Join the group.  This is done by creating a PeerGroup object for
        // the group and initializing it with the group advertisement.
        //
        PeerGroup newGroup = null;
    	try {
    		newGroup = rootGroup.newGroup(groupAdv);
    	} catch (PeerGroupException e) {
    		System.err.println("Failed to get the group from pgadv.");
    		e.printStackTrace();
    		throw e;
    	}
    	
        if (!authenticateMembership(newGroup, keystorePassword, groupPassword)) {
        	throw new PeerGroupException("Authentication failed for joining the group.");
        }

        // TODO: maybe change this to a waitForRendezVous check for the group.
        
        // Make this peer a RDV for the group in order to enable immediate communication.
        newGroup.getRendezVousService().startRendezVous();
        
        // If this peer is not intended to be a full-time RDV, let jxta determine when to demote it back to a simple EDGE. (when the network can support this)
        if (!beRendezvous) {
        	newGroup.getRendezVousService().setAutoStart(true);
        }
        
        /*
        // We'll be a rendezvous in the new group, if explicitly requested.
        if (beRendezvous) {
            newGroup.getRendezVousService().startRendezVous();
        } else {
        	
        	// immediately intensively check if there is a need for RDVs in this group.
        	newGroup.getRendezVousService().setAutoStart(true, 10);
        	
        	// Reset the auto-start check to it's default value after one minute of intensive checking.
        	final PeerGroup theGroup = newGroup;
        	new Timer().schedule(new TimerTask(){

				@Override
				public void run() {
					System.out.println("Reset autostart check.");
					theGroup.getRendezVousService().setAutoStart(true);
					
				}
        		
        	}, 1 * 60 * 1000L);
        	
        	
        }
        
        */
        
        // Leave the old peer group.
        this.leavePeerGroup(currentJoinedGroup);

        // Advertise that we've joined this group.
        DiscoveryService disco = newGroup.getDiscoveryService();
        
        // Publish our advertisements.  Is all of this really needed?
        disco.publish(newGroup.getPeerGroupAdvertisement());

        // Add the new group to our list of joined groups.
        //joinedGroups.add(newGroup);
        
        // Set this group as the current one
        currentJoinedGroup = newGroup;
        
 /*       
        System.out.println("Connected RDVs: ");
        Enumeration<ID> rdvs = newGroup.getRendezVousService().getConnectedRendezVous();
        while (rdvs.hasMoreElements()) {
        	System.out.println("Rdv: " + rdvs.nextElement());
        }
        
        System.out.println("Connected Peers: ");
        Enumeration<ID> peers = newGroup.getRendezVousService().getConnectedPeers();
        while (peers.hasMoreElements()) {
        	System.out.println("Peer: " + peers.nextElement());
        }
        
        System.out.println("Connected to RDV?" + newGroup.getRendezVousService().isConnectedToRendezVous());
        System.out.println("Is RDV?" + newGroup.getRendezVousService().isRendezVous());
    */
        
        // Init JxtaCast if null.
        if (jc == null ) {
        	jc = new JxtaCast(currentJoinedGroup.getPeerAdvertisement(), currentJoinedGroup, PIPE_ADVERTISEMENT_NAME);
        	JxtaCast.logEnabled = true;
        }
        
        // Set the group as JxtaCast's group.
        jc.setPeerGroup(newGroup);
        
        // Update local cache with peers and their private pipe advertisements from this group.
        discoverPeers(null, /*groupAdv, */null);
        discoverAdvertisements(null, null, "Name", jc.getBackChannelPipePrefix() + "*");

        return newGroup;
    }
    
    /** {@inheritDoc} */
    public PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, boolean beRendezvous) throws PeerGroupException,
        IOException, ProtocolNotSupportedException
    {
        return joinPeerGroup(groupAdv, null, null, beRendezvous);
    }
    
    /** {@inheritDoc} **/
    public void leavePeerGroup(PeerGroup oldGroup) throws PeerGroupException {

    	// If not connected to the network or group is null there is nothing to leave from.
        if (!this.isConnectedToNetwork() || oldGroup == null) {
        	return;
        }
        
        // See if it's the default group. Don`t think you can leave that.
        if (oldGroup.getPeerGroupID().equals(this.rootGroup.getPeerGroupID())) {
        	return;
        }
        
        MembershipService oldGroupMembershipService = oldGroup.getMembershipService();
        oldGroupMembershipService.resign();
    	
    	// See if it was the current joined group.
        if (oldGroup.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
        	this.currentJoinedGroup = null;
        }
    }
    
    
    /** {@inheritDoc} **/
    public void leavePeerGroup() throws PeerGroupException {
        leavePeerGroup(this.currentJoinedGroup);
    }
    

    /**
     * Build a Module Implementation Advertisement suitable for the PSE Sample
     * Peer Group. The <tt>ModuleImplAdvertisement</tt> is built using the
     * result of <tt>base.getAllPurposePeerGroupImplAdvertisement()</tt> to
     * ensure that the result will be appropriate for running as a child
     * peer group of <tt>base</tt>.
     * <p/>
     * <p/>The default advertisement is modified to use the PSE Membership
     * Service as it's membership service replacing whatever membership
     * service was originally specified (except if it already is PSE of course).
     * <p/>
     * <p/>The Module Spec ID of the ModuleImplAdvertisement is set to a new and
     * random value in order not to collide with the base group's
     * ModuleImplAdvertisement.
     *
     * @param base The Peer Group from which we will retrieve the default
     *             Module Implementation Advertisement.
     * @return The Module Implementation Advertisement for the PSE Sample
     *         Peer Group.
     */
    @SuppressWarnings("unchecked")
	static ModuleImplAdvertisement buildNewGroupImplAdvertisementWithPSE(PeerGroup base, String newGroupName) {
        ModuleImplAdvertisement newGroupImpl;

        try {
            newGroupImpl = base.getAllPurposePeerGroupImplAdvertisement();
        } catch (Exception unlikely) {
            // getAllPurposePeerGroupImplAdvertisement() doesn't really throw exceptions.
            throw new IllegalStateException("Could not get All Purpose Peer Group Impl Advertisement.");
        }

        newGroupImpl.setDescription(newGroupName + " Peer Group Implementation");
        newGroupImpl.setModuleSpecID(IDFactory.newModuleSpecID(PeerGroup.peerGroupClassID));

        // FIXME bondolo Use something else to edit the params.
        StdPeerGroupParamAdv params = new StdPeerGroupParamAdv(newGroupImpl.getParam());

        Map<ModuleClassID, Object> newGroupServices = params.getServices();

        ModuleImplAdvertisement baseGroupMembershipModuleAdv = (ModuleImplAdvertisement) newGroupServices.get(PeerGroup.membershipClassID);

        newGroupServices.remove(PeerGroup.membershipClassID);

        // The ModuleImplAdvertisement of the PSEMembershipService we want to set to the group.
        ModuleImplAdvertisement pseMembershipServiceImplAdv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(
                ModuleImplAdvertisement.getAdvertisementType());

        pseMembershipServiceImplAdv.setModuleSpecID(PSEMembershipService.pseMembershipSpecID);
        pseMembershipServiceImplAdv.setCompat(baseGroupMembershipModuleAdv.getCompat());
        pseMembershipServiceImplAdv.setCode(PSEMembershipService.class.getName());
        pseMembershipServiceImplAdv.setUri(baseGroupMembershipModuleAdv.getUri());
        pseMembershipServiceImplAdv.setProvider(baseGroupMembershipModuleAdv.getProvider());
        pseMembershipServiceImplAdv.setDescription("PSE Membership Service");

        // Add our selected membership service to the peer group service as the
        // group's default membership service.
        newGroupServices.put(PeerGroup.membershipClassID, pseMembershipServiceImplAdv);

        // Save the group impl parameters
        newGroupImpl.setParam((Element) params.getDocument(MimeMediaType.XMLUTF8));

        return newGroupImpl;
    }
    
    
    /**
     * Build the Peer Group Advertisement for the PSE Sample Peer Group.
     * <p/>
     * <p/>The Peer Group Advertisement will be generated to contain an
     * invitation certificate chain and encrypted private key. Peers which
     * know the password for the Peer Group Root Certificate Key can generate
     * their own invitation otherwise peers must get an invitation from
     * another group member.
     * <p/>
     * <p/>The invitation certificate chain appears in two forms:
     * <ul>
     * <li>Self Invitation : PSE Sample Group Root Certificate + Encrypted Private Key</li>
     * <li>Regular Invitation :
     * <ul>
     * <li>Invitation Certificate + Encrpyted Private Key</li>
     * <li>Peer Group Member Certificate</li>
     * <li>Peer Group Administrator Certificate</li>
     * <li>PSE Sample Group Root Certificate</li>
     * </ul></li>
     * </ul>
     * <p/>
     * <p/>Invitations are provided to prospective peer group members. You can
     * use a unique invitation for each prospective member or a single
     * static invitation for every prospective member. If you use a static
     * invitation certificate keep in mind that every copy will use the same
     * shared password and thus the invitation will provide only very limited
     * security.
     * <p/>
     * <p/>In some applications the invitation password will be built in to the
     * application and the human user will never have to know of it's use.
     * This can be useful if you wish your PSE Peer Group used only by a single
     * application.
     *
     * @param pseImpl              The Module Impl Advertisement which the Peer Group
     *                             Advertisement will reference for its Module Spec ID.
     * @param certificateChain  The certificate chain which comprises the
     *                             PeerGroup Invitation.
     * @param encryptedGroupPrivateKey The private key of the invitation.
     * @return The Peer Group Advertisement.
     */
    @SuppressWarnings("unchecked")
	static PeerGroupAdvertisement buildGroupAdvWithPSE(String groupName, String description, 
    		ModuleImplAdvertisement pseImpl, X509Certificate[] certificateChain, EncryptedPrivateKeyInfo encryptedGroupPrivateKey) {
    	
    	// TODO:The invitation based group join should be investigated deeper. It could provide a nicer solution than the
    	// simple password based authentication.
    	
        PeerGroupAdvertisement newPGAdv = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());

        newPGAdv.setPeerGroupID(IDFactory.newPeerGroupID());
        newPGAdv.setModuleSpecID(pseImpl.getModuleSpecID());
        newPGAdv.setName(groupName);
        newPGAdv.setDescription(description);

        PSEConfigAdv pseConfAdv = (PSEConfigAdv) AdvertisementFactory.newAdvertisement(PSEConfigAdv.getAdvertisementType());

        pseConfAdv.setCertificateChain(certificateChain);
        pseConfAdv.setEncryptedPrivateKey(encryptedGroupPrivateKey, certificateChain[0].getPublicKey().getAlgorithm());

        XMLDocument pseDoc = (XMLDocument) pseConfAdv.getDocument(MimeMediaType.XMLUTF8);

        newPGAdv.putServiceParam(PeerGroup.membershipClassID, pseDoc);

        return newPGAdv;
    }
    

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
    protected boolean authenticateMembership(PeerGroup group, char[] keystorePassword, char[] identityPassword) throws PeerGroupException, ProtocolNotSupportedException {
    	// FIXME: make authentication based on the actual membershipService of the group, not by the provided passwords.
    	
        // Get the MembershipService from the peer group.
        MembershipService membership = group.getMembershipService();
        
        System.out.println("Current Membership service: " + membership);
    	
        //StructuredDocument creds = null;
        Authenticator memberAuthenticator = null;
        
        String authenticationMethod = null;
        
        if (keystorePassword != null && identityPassword != null) {
        	authenticationMethod = "StringAuthentication";
        }
        
        try {

        	// Generate the credentials for the Peer Group.
	        AuthenticationCredential authCred = 
	            new AuthenticationCredential(group, authenticationMethod, null);
	
	        // Get the Authenticator from the Authentication creds.
	        memberAuthenticator = membership.apply(authCred);
        
        } catch (ProtocolNotSupportedException noAuthenticator) {
        	System.err.println("Could not create authenticator: " + noAuthenticator.getMessage());
        	return false;
        }

        if (authenticationMethod != null && authenticationMethod.equals("StringAuthentication")) {
	        ID identity = group.getPeerID();
	        
	        ((StringAuthenticator) memberAuthenticator).setAuth1_KeyStorePassword(keystorePassword);
	        ((StringAuthenticator) memberAuthenticator).setAuth2Identity(identity);
	        ((StringAuthenticator) memberAuthenticator).setAuth3_IdentityPassword(identityPassword);
        }
        
        // Check if everything is okay to join the group.
        if (memberAuthenticator.isReadyForJoin()) {
        	try {
        		this.groupCredential = membership.join(memberAuthenticator);
        		System.out.println("Member authentication successful.");
        	} catch (PeerGroupException failed) {
        		System.err.println("Member authentication failed: " + failed.getMessage());
        		failed.printStackTrace();
        		return false;
        	}
        }
        else {
        	System.err.println("Can't join the group yet. Authentication data incorrent or incomplete.");
            return false;
        }

        return true;
    }


    /** {@inheritDoc} **/
    public boolean isRendezvous(PeerAdvertisement peerAdv) {

        // If this peer is not from the currentJoinedGroup, bail. 
    	if (peerAdv.getPeerGroupID().equals(this.currentJoinedGroup.getPeerGroupID())) {
    		return false;
    	}
    	
    	// Find the PeerGroup object for this group.
        /*PeerGroup group = findJoinedGroup(peerAdv.getPeerGroupID());
        if (group == null)
            return false;
        */

        // Are we checking for our own peer?  If so, we can just ask the
        // PeerGroup object if we are a rendezvous.
        if (peerAdv.getPeerID().equals(rootGroup.getPeerAdvertisement().getPeerID())) {
            return this.isGroupRendezVous();
        }

        
        // Get the RendezVousService from the PeerGroup.
        /*RendezVousService rdv = (RendezVousService)group.getRendezVousService();
        if (rdv == null)
            return false;*/

        // Get a list of the connected rendezvous peers for this group, and
        // search it for the requested peer.
        //
        PeerID peerID = null;
        Enumeration<ID> rdvs = null;
        rdvs = this.getConnectedRdvsIDs();
        while (rdvs.hasMoreElements()) {
            try {
                peerID = (PeerID)rdvs.nextElement();
                if (peerID.equals(peerAdv.getPeerID()))
                    return true;
            } catch (Exception e) {}
        }

        // Didn't find it, the peer isn't a rendezvous.
        return false;
    }

//
//    /** Search our array of joined groups for the requested group.
//     *  @return PeerGroup, or null if not found.
//     */
//    protected PeerGroup findJoinedGroup(PeerGroupAdvertisement groupAdv) {
//        return findJoinedGroup(groupAdv.getPeerGroupID());
//    }
//
//
//    /** Search our array of joined groups for the requested group.
//     *  @return PeerGroup, or null if not found.
//     */
//    protected PeerGroup findJoinedGroup(PeerGroupID groupID) {
//
//        PeerGroup group = null;
//
//        // Step thru the groups we've created, looking for one that has the
//        // same peergroup ID as the requested group.
//        //
//        Enumeration<PeerGroup> myGroups = joinedGroups.elements();
//        while (myGroups.hasMoreElements()) {
//            group = (PeerGroup)myGroups.nextElement();
//
//            // If these match, we found it.
//            if (group.getPeerGroupID().equals(groupID))
//                return group;
//        }
//
//        // Didn't find it.
//        return null;
//    }


    /** {@inheritDoc} **/
	public void addJxtaCastEventListener(JxtaCastEventListener listener) {
		if (!this.isConnectedToGroup()) {
			throw new IllegalStateException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.addJxtaCastEventListener(listener);
	}
	
	/** {@inheritDoc} **/
	public void removeJxtaCastEventListener(JxtaCastEventListener listener) {
		if (!this.isConnectedToGroup()) {
			throw new IllegalStateException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.removeJxtaCastEventListener(listener);
	}

	/** {@inheritDoc} **/
	public void sendChatMsg(String text) throws PeerGroupException {
		if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendChatMsg(text);
	}

	/** {@inheritDoc} **/
	public void sendFile(File file, String caption) throws PeerGroupException {
		if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendFile(file, caption);
	}

	/** {@inheritDoc} **/
	public void sendObject(Object object, String caption) throws PeerGroupException {
		if (!this.isConnectedToGroup()) {
			throw new PeerGroupException("The peer has not yet joined a group and contacted a RDV peer.");
		}
		
		jc.sendObject(object, caption);
	}

	/** {@inheritDoc} **/
	public void rendezvousEvent(RendezvousEvent event) {
		if (event.getType() == RendezvousEvent.RDVCONNECT    ||
        event.getType() == RendezvousEvent.RDVRECONNECT  ||
        event.getType() == RendezvousEvent.RDVDISCONNECT ||
        event.getType() == RendezvousEvent.RDVFAILED) {
	
	        // If we've connected to a new rdv or just disconencted from one.  Launch discovery,
	        // so we can see any peers and groups this rdv knows or used to know.
	        if (event.getType() == RendezvousEvent.RDVCONNECT || event.getType() == RendezvousEvent.RDVDISCONNECT) {
	            this.discoverGroups(event.getPeer(), null);
	            
	            this.discoverPeers(event.getPeer(), null);
	        }
		}
		
	}
	
	/** {@inheritDoc} **/
	public PeerGroup getCurrentJoinedPeerGroup() {
		return this.currentJoinedGroup;
	}
	
	/** {@inheritDoc} **/
	public boolean isJxtaStarted() {
		return this.rootGroup != null;
	}
	
	/** {@inheritDoc} **/
	public boolean hasJoinedAGroup() {
		return this.currentJoinedGroup != null && !this.currentJoinedGroup.equals(this.rootGroup);
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToNetwork() {
		return this.isNetworkRendezVous() || this.isConnectedToNetworkRendezVous(); 
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToGroup() {
		return this.isGroupRendezVous() || this.isConnectedToGroupRendezVous(); 
	}
	
	/** {@inheritDoc} **/
	public boolean isNetworkRendezVous() {
		return this.isJxtaStarted() && this.rootGroup.getRendezVousService().isRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isGroupRendezVous() {
		return this.hasJoinedAGroup() && this.currentJoinedGroup.getRendezVousService().isRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToNetworkRendezVous() {
		return this.isJxtaStarted() && this.rootGroup.getRendezVousService().isConnectedToRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isConnectedToGroupRendezVous() {
		return this.hasJoinedAGroup() && this.currentJoinedGroup.getRendezVousService().isConnectedToRendezVous();
	}
	
	/** {@inheritDoc} **/
	public boolean isNetworkConfigured() {
		return this.manager != null;
	}
}
