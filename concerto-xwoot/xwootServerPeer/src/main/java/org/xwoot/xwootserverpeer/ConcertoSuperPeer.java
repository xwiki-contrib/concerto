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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.exception.JxtaException;
import net.jxta.platform.NetworkConfigurator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Implements a Super peer that will ensure connectivity and for a network.
 * <p>
 * This peer does nothing except route communication, log events and ensures network existence.
 * 
 * @version $Id$
 */
public class ConcertoSuperPeer
{
    /** Get help. */
    public static final String HELP_PARAMETER = "h";

    /** Location of a list of seeding rdvs. */
    public static final String RDV_SEEDING_URI_PARAMETER = "rdvSeedingUri";

    /** Location of a list of seeding relays. */
    public static final String RELAY_SEEDING_URI_PARAMETER = "relaySeedingUri";

    /** Relay mode for this peer. */
    public static final String MODE_RELAY_PARAMETER = "relay";

    /** Rendezvous mode for this peer. */
    public static final String MODE_RENDEZVOUS_PARAMETER = "rendezvous";

    /** Comma separated list of rdv seeds. */
    public static final String RDV_SEEDS_PARAMETER = "rdvSeeds";

    /** Comma separated list of relay seeds. */
    public static final String RELAY_SEEDS_PARAMETER = "relaySeeds";

    /** Name of this peer. */
    public static final String PEER_NAME_PARAMETER = "name";

    /** Location where to store the jxta cache directory. */
    public static final String HOME_PARAMETER = "home";

    /** If to use TCP for communication. */
    public static final String USE_TCP_PARAMETER = "useTcp";

    /** The TCP port to use. */
    public static final String TCP_PORT_PARAMETER = "tcpPort";

    /** If to use TCP Incoming connections. */
    public static final String TCP_INCOMMING_PARAMETER = "useTcpIncomming";

    /** If to use TCP Outgoing connections. */
    public static final String TCP_OUTGOING_PARAMETER = "useTcpOutgoing";

    /** If to use HTTP for communication. */
    public static final String USE_HTTP_PARAMETER = "useHttp";

    /** The HTTP port to use. */
    public static final String HTTP_PORT_PARAMETER = "httpPort";

    /** If to use HTTP Incoming connections. */
    public static final String HTTP_INCOMMING_PARAMETER = "useHttpIncomming";

    /** If to use HTTP Outgoing connections. */
    public static final String HTTP_OUTGOING_PARAMETER = "useHttpOutgoing";

    /** The external ip to use for this peer. */
    public static final String EXTERNAL_IP_PARAMETER = "externalIp";

    /** If to use only the external ip in advertisements. */
    public static final String ONLY_EXTERNAL_IP_PARAMETER = "useOnlyExternalIp";
    
    /** To clean any existing configuration from the home directory. */
    public static final String CLEAN_EXISTING_CONFIG_PARAMETER = "clean";
    
    /** To enable multicast for LAN communication. */
    public static final String USE_MULTICAST_PARAMETER = "useMulticast";

    /** Name of this peer. */
    private String peerName;

    /** Location where to store the jxta cache directory. */
    private String homePath;

    /** Location of a list of seeding rdvs. */
    private URI rdvSeedingUri;

    /** Location of a list of seeding relays. */
    private URI relaySeedingUri;

    /** list of rdv seeds. */
    private String[] rdvSeeds;

    /** list of relay seeds. */
    private String[] relaySeeds;

    /** Rendezvous mode for this peer. */
    private boolean modeRendezvous;

    /** Relay mode for this peer. */
    private boolean modeRelay;

    /** The TCP port to use. */
    private int tcpPort = 0;

    /** The HTTP port to use. */
    private int httpPort = 0;

    /** If to use TCP for communication. */
    private boolean useTcp;

    /** If to use TCP Incoming connections. */
    private boolean useTcpIncomming;

    /** If to use TCP Outgoing connections. */
    private boolean useTcpOutgoing;

    /** If to use HTTP for communication. */
    private boolean useHttp;

    /** If to use HTTP Incoming connections. */
    private boolean useHttpIncomming;

    /** If to use HTTP Outgoing connections. */
    private boolean useHttpOutgoing;

    /** If to use only the external ip in advertisements. */
    private boolean useOnlyExternalIp;

    /** The external ip to use for this peer. */
    private String externalIp;
    
    /** To clean any existing configuration from the home directory. */
    private boolean clean;
    
    /** To enable multicast for LAN communication. */
    private boolean useMulticast;

    /** This super peer instance. */
    private SuperPeer superPeer;

    /** Options for command line. */
    private Options options;
    
    private HelpFormatter formatter;  

    /**
     * Constructor.
     */
    public ConcertoSuperPeer()
    {
        this.options = new Options();
        this.addOptions(options);
        formatter = new HelpFormatter();
    }

    /**
     * Main method.
     * 
     * @param args main method arguments.
     */
    public static void main(String[] args)
    {
        ConcertoSuperPeer concertoSuperPeer = new ConcertoSuperPeer();

        try {
            concertoSuperPeer.parseArgs(args);
        } catch (ParseException e) {
            System.err.println("Error parsing parameters: " + e.getMessage());
            concertoSuperPeer.showUsage();
            System.exit(-1);
        }

        try {
            concertoSuperPeer.configureNetwork();
        } catch (JxtaException e) {
            System.err.println("Error configuring the peer's network settings: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            concertoSuperPeer.getSuperPeer().startNetwork();
        } catch (JxtaException e) {
            System.err.println("Error starting the network on this peer: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        synchronized (concertoSuperPeer) {

            try {
                concertoSuperPeer.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        BasicParser parser = new BasicParser();
        CommandLine cl = parser.parse(this.options, args);

        if (cl.hasOption(HELP_PARAMETER)) {
            showUsage();
        } else {
            String rdvSeedingUriString = cl.getOptionValue(RDV_SEEDING_URI_PARAMETER);
            String relaySeedingUriString = cl.getOptionValue(RELAY_SEEDING_URI_PARAMETER);
            this.modeRendezvous = (cl.hasOption(MODE_RENDEZVOUS_PARAMETER) != this.modeRendezvous ? cl.hasOption(MODE_RENDEZVOUS_PARAMETER) : this.modeRendezvous);
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
            
            this.clean = cl.hasOption(CLEAN_EXISTING_CONFIG_PARAMETER);
            this.useMulticast = cl.hasOption(USE_MULTICAST_PARAMETER);

            if (tcpPortString != null) {
                try {
                    this.tcpPort = Integer.parseInt(tcpPortString);
                } catch (NumberFormatException e) {
                    showUsage();
                    throw new ParseException("Invalid TCP port given.");
                }
            }

            if (httpPortString != null) {
                try {
                    this.httpPort = Integer.parseInt(httpPortString);
                } catch (NumberFormatException e) {
                    showUsage();
                    throw new ParseException("Invalid HTTP port given.");
                }
            }

            if (!modeRendezvous && !modeRelay) {
                showUsage();
                throw new ParseException("This peer must run in relay, rendezvous or both modes.");
            }

            String delimiter = ",";
            if (rdvSeedsString != null) {
                this.rdvSeeds = rdvSeedsString.split(delimiter);
            }

            if (relaySeedsString != null) {
                this.relaySeeds = relaySeedsString.split(delimiter);
            }

            if (rdvSeedingUriString != null) {
                try {
                    this.rdvSeedingUri = new URI(rdvSeedingUriString);
                } catch (URISyntaxException e) {
                    showUsage();
                    throw new ParseException("Invalid Rendezvous seeding URI given.");
                }
            }

            if (relaySeedsString != null) {
                try {
                    this.relaySeedingUri = new URI(relaySeedingUriString);
                } catch (URISyntaxException e) {
                    showUsage();
                    throw new ParseException("Invalid Relay seeding URI given.");
                }
            }

        }
    }

    /**
     * Add the options for the command line.
     * 
     * @param options the options object to populate.
     */
    public void addOptions(Options options)
    {
        options.addOption(HELP_PARAMETER, false, "Prints the usage for this application.");
        options.addOption(RDV_SEEDING_URI_PARAMETER, true,
            "The RDV seeding location where to get RDV seeds for the network.");
        options.addOption(RELAY_SEEDING_URI_PARAMETER, true,
            "The Relay seeding location where to get Relay seeds for the network.");
        options.addOption(MODE_RENDEZVOUS_PARAMETER, false, "Run this super peer in rendezvous mode.");
        options.addOption(MODE_RELAY_PARAMETER, false, "Run this super peer in relay mode.");
        options.addOption(RDV_SEEDS_PARAMETER, true, "A comma separated list of RDV seeds to use for the network.");
        options.addOption(RELAY_SEEDS_PARAMETER, true, "A comma separated list of Relay seeds to use for the network.");
        options.addOption(PEER_NAME_PARAMETER, true, "The name of this peer. Default is " + Peer.DEFAULT_PEER_NAME);
        options.addOption(HOME_PARAMETER, true,
            "The absolute location on drive where to store the jxta cache directory. Default is a directory named "
                + Peer.DEFAULT_DIR_NAME + " created in the current directory.");

        options.addOption(USE_TCP_PARAMETER, false, "To use TCP protocol for communication.");
        options.addOption(TCP_PORT_PARAMETER, true, "The port to use for TCP communication.");
        options.addOption(TCP_INCOMMING_PARAMETER, false, "To use TCP incomming connections for TCP communication.");
        options.addOption(TCP_OUTGOING_PARAMETER, false, "To use TCP outgoing connections for TCP communication.");

        options.addOption(USE_HTTP_PARAMETER, false, "To use HTTP protocol for communication.");
        options.addOption(HTTP_PORT_PARAMETER, true, "The port to use for HTTP communication.");
        options.addOption(HTTP_INCOMMING_PARAMETER, false, "To use HTTP incomming connections for HTTP communication.");
        options.addOption(HTTP_OUTGOING_PARAMETER, false, "To use HTTP outgoing connections for HTTP communication.");

        options.addOption(EXTERNAL_IP_PARAMETER, true,
            "An un-firewalled/un-NAT-ed IP address or DNS to use for communication instead of the local one.");
        options.addOption(ONLY_EXTERNAL_IP_PARAMETER, false, "To use HTTP protocol for communication.");
        
        options.addOption(CLEAN_EXISTING_CONFIG_PARAMETER, false, "To clean any existing configuration from the home directory.");
        
        options.addOption(USE_MULTICAST_PARAMETER, false, "To enable multicast for LAN communication.");
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

        if (this.clean) {            
            String theName = (this.peerName != null ? this.peerName : Peer.DEFAULT_PEER_NAME);
            File homeLocation = (this.homePath != null ? new File(this.homePath, theName) : new File(Peer.DEFAULT_DIR_NAME, theName));
            
            FileUtil.deleteDirectory(homeLocation);
        }
            
        this.superPeer = new JxtaSuperPeer(peerName, this.homePath, mode);

        NetworkConfigurator networkConfigurator = this.superPeer.getConfigurator();

        if (this.rdvSeedingUri != null) {
            networkConfigurator.addRdvSeedingURI(rdvSeedingUri);
        }

        if (this.relaySeedingUri != null) {
            networkConfigurator.addRelaySeedingURI(relaySeedingUri);
        }
        
        if (this.relaySeeds != null) {
            for (String seed : relaySeeds) {
                try {
                    networkConfigurator.addSeedRelay(new URI(seed));
                } catch (URISyntaxException e) {
                    System.err.println("Skipping invalid relay seed: " + seed);
                    e.printStackTrace();
                }
            }
        }
        
        if (this.rdvSeeds != null) {
            for (String seed : rdvSeeds) {
                try {
                    networkConfigurator.addSeedRendezvous(new URI(seed));
                } catch (URISyntaxException e) {
                    System.err.println("Skipping invalid rendezvous seed: " + seed);
                    e.printStackTrace();
                }
            }
        }

        String addressPortSeparator = ":";
        if (useTcp) {
            networkConfigurator.setTcpEnabled(true);
            networkConfigurator.setTcpIncoming(this.useTcpIncomming);
            networkConfigurator.setTcpOutgoing(this.useTcpOutgoing);
            networkConfigurator.setTcpPort(tcpPort);
            if (this.externalIp != null) {
                // disable dynamic ports because we use a fixed ip:port combination now.
                networkConfigurator.setTcpStartPort(-1);
                networkConfigurator.setTcpEndPort(-1);
                
                String tcpPublicAddress = this.externalIp;
                if (!tcpPublicAddress.contains(addressPortSeparator)) {
                    tcpPublicAddress += addressPortSeparator + String.valueOf(tcpPort);
                } else {

                }
                System.out.println("TCP public address: " + tcpPublicAddress);
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
                if (!httpPublicAddress.contains(addressPortSeparator)) {
                    httpPublicAddress += addressPortSeparator + String.valueOf(httpPort);
                } else {

                }
                System.out.println("HTTP public address: " + httpPublicAddress);
                networkConfigurator.setHttpPublicAddress(httpPublicAddress, this.useOnlyExternalIp);
            }
        }
        
        if (this.useMulticast) {
            this.superPeer.getManager().setUseDefaultSeeds(true);
        }
    }

    /**
     * @return the superPeer
     */
    public SuperPeer getSuperPeer()
    {
        return this.superPeer;
    }
    
    public void showUsage()
    {
        this.formatter.printHelp("Available parameters", this.options);
    }

}
