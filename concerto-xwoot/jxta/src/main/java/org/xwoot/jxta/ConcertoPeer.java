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

import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.jxtacast.event.JxtaCastEvent;
import net.jxta.jxtacast.event.JxtaCastEventListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Proof of Concept TUI application using the JXTA platform trough the implementation
 * {@link JxtaP2PFace} of the interface {@link P2PFace}.
 * 
 * @version $Id:$
 */
public class ConcertoPeer implements JxtaCastEventListener {

	P2PFace jxta;

	public ConcertoPeer() {
		jxta = new JxtaP2PFace();
	}
	
	public void start() throws Exception {
		jxta.configureNetwork(null);
		jxta.startNetworkAndConnect(this);
	}

	/**
	 * main
	 * 
	 * @param args
	 *            command line args
	 */
	public static void main(String args[]) {
		ConcertoPeer peer = new ConcertoPeer();

		try {
			peer.start();
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			int option = 0;
			while (option <= 0 || option > 4 ) {
				System.out.println("> ");
	
				System.out.println("Please select an action for this peer:");
				System.out.println("1. Join a group.");
				System.out.println("2. Create a public group.");
				System.out.println("3. Create a private group.");
				System.out.println("4. List known peers in the current group.");
				System.out.println("5. Send a small object to a group.");
				System.out.println("6. Show jxta status.");
	
				BufferedReader bis = new BufferedReader(new InputStreamReader(
						System.in));
	
				try {
					option = Integer.parseInt(bis.readLine());
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				if (option == 1) {
					// Start looking for messages and receiving them.
					peer.joinAGroup();
				} else if (option == 2) {
					peer.createAPublicGroup();
				} else if (option == 3) {
					peer.createAPrivateGroup();
				} else if (option == 4) {
					peer.listKnownPeersinCurrentGroup();
				} else if (option == 5) {
					// Send a small message.
					peer.sendSmallObjectToCurrentGroup();
				} else if (option == 6) {
					peer.getStatus();
				} else {
					option = 0;
					System.out.println("Invalid option. Try again.");
				}
			}
		}
	}
	
	public void joinAGroup() {
		Enumeration<PeerGroupAdvertisement> en = this.jxta.getKnownGroups();
		
		List<PeerGroupAdvertisement> list = new ArrayList<PeerGroupAdvertisement>();
		
		System.out.println("Available groups:");
		while (en.hasMoreElements()) {
			PeerGroupAdvertisement adv = en.nextElement();
			list.add(adv);
			System.out.println(list.size() - 1 + ": " + adv.getName());
		}
		
		int selection = -1;
		System.out.println("Select: ");
		BufferedReader bis = new BufferedReader(new InputStreamReader(
				System.in));

		try {
			selection = Integer.parseInt(bis.readLine());
		} catch (Exception e) {
			System.out.println("Bad Value, canceled.");
			selection = -1;
			return;
		}
		
		try {
			char[] keystorePassword = null;
			char[] groupPassword = null;
			
			PeerGroup selectedGroup = jxta.getDefaultGroup().newGroup(list.get(selection));
			if (selectedGroup.getMembershipService() instanceof PSEMembershipService) {
				System.out.println("This group is a private group and requires authentication.");
				
				System.out.println("Please enter the keystore password: ");
				keystorePassword = bis.readLine().toCharArray();
				
				System.out.println("Please enter the group's password: ");
				groupPassword = bis.readLine().toCharArray();
			}
			
			jxta.joinPeerGroup(list.get(selection), keystorePassword, groupPassword, false);
		} catch (Exception e) {
			System.out.println("Failed to join the group.");
			e.printStackTrace();
			return;
		}
	}
	
	public void createAPublicGroup() {
		System.out.println("Please enter a group name: ");
		BufferedReader bis = new BufferedReader(new InputStreamReader(
				System.in));
		
		String groupName = null;
		try {
			groupName = bis.readLine();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			jxta.createNewGroup(groupName, "A new test group", null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createAPrivateGroup() {
		String groupName = null;
		char[] keystorePassword = null;
		char[] groupPassword = null;

		try {
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					System.in));

			System.out.println("Please enter a group name: ");
			groupName = bis.readLine();

			System.out.println("Please enter the keystore password: ");
			keystorePassword = bis.readLine().toCharArray();

			System.out.println("Please enter the group's password: ");
			groupPassword = bis.readLine().toCharArray();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			jxta.createNewGroup(groupName, "A new test group", keystorePassword, groupPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void listKnownPeersinCurrentGroup() {
		System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup().getPeerGroupName());
		
		Enumeration<PeerAdvertisement> peers = jxta.getKnownPeers();
		if (peers != null) {
			while(peers.hasMoreElements()) {
				PeerAdvertisement peer = peers.nextElement();
				System.out.println(" " + peer.getName() + "(" + peer.getID() + ")");
			}
		}
	}
	
	
	public void getStatus() {
		
		System.out.println("Status: ");
		System.out.println("jxta.isNetworkConfigured() : " + jxta.isNetworkConfigured());
		System.out.println("jxta.isJxtaStarted() : " + jxta.isJxtaStarted()); 
		System.out.println("jxta.isConnectedToNetworkRendezVous() : " + jxta.isConnectedToNetworkRendezVous());
		System.out.println("jxta.isConnectedToNetwork() : " + jxta.isConnectedToNetwork());
		System.out.println("jxta.hasJoinedAGroup() : " + jxta.hasJoinedAGroup());
		System.out.println("jxta.isGroupRendezVous() : " + jxta.isGroupRendezVous());
		System.out.println("jxta.isConnectedToGroupRendezVous() : " + jxta.isConnectedToGroupRendezVous());
		System.out.println("jxta.isConnectedToGroup() : " + jxta.isConnectedToGroup());

	}
	
	
	public void sendSmallObjectToCurrentGroup() {
		System.out.println("Current group: " + jxta.getCurrentJoinedPeerGroup().getPeerGroupName());
		
		String smallObject = "Small object transfer";
		
		try {
			jxta.sendObject(smallObject, "small object");
		} catch (PeerGroupException e) {
			e.printStackTrace();
		}
	}

	public void jxtaCastProgress(JxtaCastEvent e) {
		if (e.percentDone == 100) {
			System.out.println("[CONCERTO] Transer complete!");
			if (e.transType == JxtaCastEvent.RECV) {
				System.out.println("[CONCERTO] Received: " + e.transferedData.getClass());
				if (e.transferedData instanceof String) {
					String data = (String) e.transferedData;
					System.out.println("Object size: " + data.getBytes().length);
				}
			}
		}
	}
}
