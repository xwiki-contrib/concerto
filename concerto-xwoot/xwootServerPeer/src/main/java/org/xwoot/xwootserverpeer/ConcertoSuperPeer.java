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

package org.xwoot.xwootserverpeer;

import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.exception.JxtaException;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager.ConfigMode;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xwoot.jxta.Peer;

/**
 * TODO DOCUMENT ME!
 *
 * @version $Id:$
 */
public class ConcertoSuperPeer
{
    public static final String HELP_PARAMETER = "h";
    public static final String RDV_SEEDING_URI_PARAMETER = "rdvSeedingUri";
    public static final String RELAY_SEEDING_URI_PARAMETER = "rdvSeedingUri";
    public static final String MODE_RELAY_PARAMETER = "relay";
    public static final String MODE_RENDEZVOUS_PARAMETER = "rendezvous";
    public static final String RDV_SEEDS_PARAMETER = "rdvSeeds";
    public static final String RELAY_SEEDS_PARAMETER = "relaySeeds";
    public static final String PEER_NAME_PARAMETER = "name";
    public static final String HOME_PARAMETER = "home";
    
    public static final String USE_TCP_PARAMETER = "useTcp";
    public static final String TCP_PORT_PARAMETER = "tcpPort";
    public static final String TCP_INCOMMING_PARAMETER = "useTcpIncomming";
    public static final String TCP_OUTGOING_PARAMETER = "useTcpOutgoing";
    
    public static final String USE_HTTP_PARAMETER = "useHttp";
    public static final String HTTP_PORT_PARAMETER = "httpPort";
    public static final String HTTP_INCOMMING_PARAMETER = "useHttpIncomming";
    public static final String HTTP_OUTGOING_PARAMETER = "useHttpOutgoing";
    
    public static final String EXTERNAL_IP_PARAMETER = "externalIp";
    public static final String ONLY_EXTERNAL_IP_PARAMETER = "useOnlyExternalIp";
    
    String peerName;
    String homePath;
    
    URI rdvSeedingUri;
    URI relaySeedingUri;
    
    String[] rdvSeeds;
    String[] relaySeeds;
    
    boolean modeRendezvous;
    boolean modeRelay;
    
    int tcpPort;
    int httpPort;
    
    boolean useTcp = true;
    boolean useTcpIncomming = true;
    boolean useTcpOutgoing = true;
    
    boolean useHttp = true;
    boolean useHttpIncomming = true;
    boolean useHttpOutgoing = true;
    
    boolean useOnlyExternalIp;
    String externalIp;
    
    
    SuperPeer superPeer;
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ConcertoSuperPeer concertoSuperPeer = new ConcertoSuperPeer();
        
        try {
            concertoSuperPeer.parseArgs(args);
        } catch (ParseException e) {
            System.err.println("Error parsing parameters: " + e.getMessage());
            System.exit(-1);
        }
        
        try {
            concertoSuperPeer.configureNetwork();
        } catch (JxtaException e) {
            System.err.println("Error configuring the peer's network settings: " + e.getMessage());
            System.exit(-1);
        }
        
        try {
            concertoSuperPeer.getSuperPeer().startNetwork();
        } catch (JxtaException e) {
            System.err.println("Error starting the network on this peer: " + e.getMessage());
            System.exit(-1);
        }
    }
    
    /**
     * Parse the command line arguments.
     * 
     * @param args the arguments to parse.
     * @throws ParseException if problems occur while parsing.
     */
    public void parseArgs(String[] args) throws ParseException
    {
        Options opt = new Options(); 
        
        opt.addOption(HELP_PARAMETER, false, "Prints the usage for this application.");
        opt.addOption(RDV_SEEDING_URI_PARAMETER, true, "The RDV seeding location where to get RDV seeds for the network.");
        opt.addOption(RELAY_SEEDING_URI_PARAMETER, true, "The Relay seeding location where to get Relay seeds for the network.");
        opt.addOption(MODE_RENDEZVOUS_PARAMETER, false, "Run this super peer in rendezvous mode.");
        opt.addOption(MODE_RELAY_PARAMETER, false, "Run this super peer in relay mode.");
        opt.addOption(RDV_SEEDS_PARAMETER, true, "A comma separated list of RDV seeds to use for the network.");
        opt.addOption(RELAY_SEEDS_PARAMETER, true, "A comma separated list of Relay seeds to use for the network.");
        opt.addOption(PEER_NAME_PARAMETER, true, "The name of this peer. Default is " + Peer.DEFAULT_PEER_NAME);
        opt.addOption(HOME_PARAMETER, true, "The absolute location on drive where to store the jxta cache directory. Default is a directory named " + Peer.DEFAULT_DIR_NAME + " created in the current directory.");
        
        opt.addOption(USE_TCP_PARAMETER, false, "To use TCP protocol for communication.");
        opt.addOption(TCP_PORT_PARAMETER, true, "The port to use for TCP communication.");
        opt.addOption(TCP_INCOMMING_PARAMETER, true, "To use TCP incomming connections for TCP communication.");
        opt.addOption(TCP_OUTGOING_PARAMETER, true, "To use TCP outgoing connections for TCP communication.");
        
        opt.addOption(USE_HTTP_PARAMETER, false, "To use HTTP protocol for communication.");
        opt.addOption(HTTP_PORT_PARAMETER, true, "The port to use for HTTP communication.");
        opt.addOption(HTTP_INCOMMING_PARAMETER, true, "To use HTTP incomming connections for HTTP communication.");
        opt.addOption(HTTP_OUTGOING_PARAMETER, true, "To use HTTP outgoing connections for HTTP communication.");
        
        opt.addOption(EXTERNAL_IP_PARAMETER, true, "An un-firewalled/un-NAT-ed IP address or DNS to use for communication instead of the local one.");
        opt.addOption(ONLY_EXTERNAL_IP_PARAMETER, false, "To use HTTP protocol for communication.");
        
        
        BasicParser parser = new BasicParser();
        CommandLine cl = parser.parse(opt, args);
        
        HelpFormatter f = new HelpFormatter();
        
        if ( cl.hasOption(HELP_PARAMETER) ) {
            f.printHelp("Available parameters", opt);
        }
        else {
            String rdvSeedingUriString = cl.getOptionValue(RDV_SEEDING_URI_PARAMETER);
            String relaySeedingUriString = cl.getOptionValue(RELAY_SEEDING_URI_PARAMETER);
            this.modeRendezvous = cl.hasOption(MODE_RENDEZVOUS_PARAMETER);
            this.modeRelay = cl.hasOption(MODE_RELAY_PARAMETER);
            this.peerName = cl.getOptionValue(PEER_NAME_PARAMETER);
            this.homePath = cl.getOptionValue(HOME_PARAMETER);
            String rdvSeedsString = cl.getOptionValue(RDV_SEEDS_PARAMETER);
            String relaySeedsString = cl.getOptionValue(RELAY_SEEDS_PARAMETER);
            
            this.useTcp = cl.hasOption(USE_TCP_PARAMETER);
            String tcpPortString = cl.getOptionValue(TCP_PORT_PARAMETER);
            this.useTcpIncomming = cl.hasOption(TCP_INCOMMING_PARAMETER);
            this.useTcpOutgoing = cl.hasOption(TCP_OUTGOING_PARAMETER);
            
            this.useHttp = cl.hasOption(USE_HTTP_PARAMETER);
            String httpPortString = cl.getOptionValue(HTTP_PORT_PARAMETER);
            this.useHttpIncomming = cl.hasOption(HTTP_INCOMMING_PARAMETER);
            this.useHttpOutgoing = cl.hasOption(HTTP_OUTGOING_PARAMETER);
            
            this.externalIp = cl.getOptionValue(EXTERNAL_IP_PARAMETER);
            this.useOnlyExternalIp = cl.hasOption(ONLY_EXTERNAL_IP_PARAMETER);
            
            if (tcpPortString != null) {
                try {
                    this.tcpPort = Integer.parseInt(tcpPortString);
                } catch (NumberFormatException e) {
                    f.printHelp("Available parameters", opt);
                    throw new ParseException("Invalid TCP port given.");
                }
            }
            
            if (httpPortString != null) {
                try {
                    this.httpPort = Integer.parseInt(httpPortString);
                } catch (NumberFormatException e) {
                    f.printHelp("Available parameters", opt);
                    throw new ParseException("Invalid HTTP port given.");
                }
            }
            
            if (!modeRendezvous && !modeRelay) {
                f.printHelp("Available parameters", opt);
                throw new ParseException("This peer must run in relay, rendezvous or both modes.");
            }
            
            if (rdvSeedsString != null) {
                this.rdvSeeds = rdvSeedsString.split(",");
            }
            
            if (relaySeedsString != null) {
                this.relaySeeds = relaySeedsString.split(",");
            }
            
            if (rdvSeedingUri != null) {
                try {
                    this.rdvSeedingUri = new URI(rdvSeedingUriString);
                } catch (URISyntaxException e) {
                    f.printHelp("Available parameters", opt);
                    throw new ParseException("Invalid Rendezvous seeding URI given.");
                }
            }
            
            if (relaySeedsString != null) {
                try {
                    this.relaySeedingUri = new URI(relaySeedingUriString);
                } catch (URISyntaxException e) {
                    f.printHelp("Available parameters", opt);
                    throw new ParseException("Invalid Relay seeding URI given.");
                }
            }
            
            
        }
    }
    
    /**
     * Configure this peer's network settings.
     * 
     * @throws JxtaException if problems occur while configuring the peer.
     */
    public void configureNetwork() throws JxtaException
    {
        ConfigMode mode = null;
        if (modeRendezvous && modeRelay) {
            mode = ConfigMode.RENDEZVOUS_RELAY;
        } else if (!modeRelay) {
            mode = ConfigMode.RENDEZVOUS;
        } else {
            mode = ConfigMode.RELAY;
        }
        
        this.superPeer = new JxtaSuperPeer(peerName, this.homePath, mode);
        
        NetworkConfigurator networkConfigurator = this.superPeer.getConfigurator();
        
        if (this.rdvSeedingUri != null) {
            networkConfigurator.addRdvSeedingURI(rdvSeedingUri);
        }
        
        if (this.relaySeedingUri != null) {
            networkConfigurator.addRelaySeedingURI(relaySeedingUri);
        }
        
        if (useTcp) {
            networkConfigurator.setTcpEnabled(true);
            networkConfigurator.setTcpIncoming(this.useTcpIncomming);
            networkConfigurator.setTcpOutgoing(this.useTcpOutgoing);
            networkConfigurator.setTcpPort(tcpPort);
            if (this.externalIp != null) {
                String tcpPublicAddress = this.externalIp;
                if (!tcpPublicAddress.contains(":")) {
                    tcpPublicAddress += ":" + String.valueOf(tcpPort);
                } else {
                    
                }
                networkConfigurator.setTcpPublicAddress(tcpPublicAddress, this.useOnlyExternalIp);
            }
        }
        
        if (useHttp) {
            networkConfigurator.setHttpEnabled(true);
            networkConfigurator.setHttpIncoming(this.useHttpIncomming);
            networkConfigurator.setHttpOutgoing(this.useHttpOutgoing);
            networkConfigurator.setHttpPort(httpPort);
            if (this.externalIp != null) {
                String httpPublicAddress = this.externalIp;
                if (!httpPublicAddress.contains(":")) {
                    httpPublicAddress += ":" + String.valueOf(tcpPort);
                } else {
                    
                }
                networkConfigurator.setHttpPublicAddress(httpPublicAddress, this.useOnlyExternalIp);
            }
        }
    }

    /**
     * @return the superPeer
     */
    public SuperPeer getSuperPeer()
    {
        return this.superPeer;
    }

}
