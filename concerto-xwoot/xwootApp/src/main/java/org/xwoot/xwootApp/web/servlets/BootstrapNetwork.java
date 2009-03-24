/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.xwootApp.web.servlets;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.platform.NetworkConfigurator;

import org.xwoot.jxta.NetworkManager;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * Servlet handling network setup.
 * 
 * @version $Id$
 */
public class BootstrapNetwork extends HttpServlet
{
    /** Join custom network option. */
    private static final String CUSTOM_NETWORK = "custom";

    /** Join public jxta network option. */
    private static final String PUBLIC_JXTA_NETWORK = "publicJxta";

    /** Join concerto network option. */
    private static final String CONCERTO_NETWORK = "concerto";

    /** Used for serialization. */
    private static final long serialVersionUID = -3758874922535817475L;

    /** The value of the join network button. */
    private static final String JOIN_BUTTON = "Join";

    /** The value of the create network button. */
    private static final String CREATE_BUTTON = "Create";

    /** The value of a checked checkbox. */
    private static final String TRUE = "true";

    /** The XWootEngine instance to manage. */
    private XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();

    /** {@inheritDoc} */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        /*request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.getSession().removeAttribute("neighbor");
        request.getSession().removeAttribute("join");*/

        String errors = "";
        
        NetworkManager networkManager = ((XWoot3) xwootEngine).getPeer().getManager();
        NetworkConfigurator networkConfig = null;

        String networkChoice = request.getParameter("networkChoice");

        if (networkChoice == null) {
            // No button clicked yet.
            request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
            return;
        }
        
        errors += this.validateCommonFormFieldsFromRequest(request);
        
        // If we detect errors at this point, don`t go any further.
        if (errors != null && errors.length() != 0) {
            errors = errors.replaceAll("\n", "<br />");
            request.setAttribute("errors", errors);

            request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
            return;
        }

        // Common settings.
        if (CREATE_BUTTON.equals(networkChoice) || JOIN_BUTTON.equals(networkChoice)) {
            
            // Disconnect from any connected network.
            if (xwootEngine.isConnectedToP2PNetwork()) {
                try {
                    xwootEngine.disconnectFromP2PNetwork();
                } catch (Exception e) {
                    // TODO: remove the exception throwing of disconnectFromP2PNetwork.
                    // This should never happen.
                    this.log("Failed to disconnect from existing network.", e);
                }
            }
            
            // Clear previous locally cached configuration.
            File platformConfig = new File(networkManager.getConfigurator().getHome(), "PlatformConfig");
            if (platformConfig.exists()) {
                platformConfig.delete();
            }
            
            // Initialize the proper peer mode.
            ConfigMode mode = null;
            if (JOIN_BUTTON.equals(networkChoice)) {
                boolean beRendezVous = TRUE.equals(request.getParameter("beRendezVous"));
                boolean beRelay = TRUE.equals(request.getParameter("beRelay"));
                
                if (beRendezVous && beRelay) {
                    mode = ConfigMode.RENDEZVOUS_RELAY;
                } else if (beRendezVous) {
                    mode = ConfigMode.RENDEZVOUS;
                } else if (beRelay) {
                    mode = ConfigMode.RELAY;
                }
            } else if (CREATE_BUTTON.equals(networkChoice)) {
                mode = ConfigMode.RENDEZVOUS_RELAY;
            }
            
            if (mode != null) {
                this.log("Setting this peer to " + mode + " mode.");
                networkManager.setMode(mode);
            } // else mode remains unchanged as EDGE.
            
            // Get the now updated networkConfig or the old one if the mode remained the same.
            networkConfig = networkManager.getConfigurator();
            
            
            // Continue with common settings.
            
            boolean useExternalIp = TRUE.equals(request.getParameter("useExternalIp"));
            String externalIp = request.getParameter("externalIp");
            boolean useOnlyExternalIp = TRUE.equals(request.getParameter("useOnlyExternalIp"));

            boolean useTcp = TRUE.equals(request.getParameter("useTcp"));
            int tcpPort = Integer.parseInt(request.getParameter("tcpPort"));

            boolean useHttp = TRUE.equals(request.getParameter("useHttp"));
            int httpPort = Integer.parseInt(request.getParameter("httpPort"));

            boolean useMulticast = TRUE.equals(request.getParameter("useMulticast"));

            networkConfig.setTcpEnabled(useTcp);

            if (useTcp) {
                this.log("Using TCP");

                networkConfig.setTcpIncoming(true);
                networkConfig.setTcpOutgoing(true);
                networkConfig.setTcpPort(tcpPort);

                String tcpPublicAddress = externalIp;
                if (useExternalIp) {
                    // disable dynamic ports because we use a fixed ip:port combination now.
                    networkConfig.setTcpStartPort(-1);
                    networkConfig.setTcpEndPort(-1);

                    if (!tcpPublicAddress.contains(":")) {
                        tcpPublicAddress += ":" + tcpPort;
                    }
                    networkConfig.setTcpPublicAddress(tcpPublicAddress, useOnlyExternalIp);
                    this.log("Using TCP External IP : " + tcpPublicAddress + " exclusively? " + useOnlyExternalIp);
                }
            }

            networkConfig.setHttpEnabled(useHttp);

            if (useHttp) {
                this.log("Using HTTP");

                networkConfig.setHttpIncoming(true);
                networkConfig.setHttpOutgoing(true);
                networkConfig.setHttpPort(httpPort);

                String httpPublicAddress = externalIp;
                if (useExternalIp) {
                    if (!httpPublicAddress.contains(":")) {
                        httpPublicAddress += ":" + httpPort;
                    }
                    networkConfig.setHttpPublicAddress(httpPublicAddress, useOnlyExternalIp);
                    this
                        .log("Using HTTP External IP : " + httpPublicAddress + " exclusively? " + useOnlyExternalIp);
                }
            }

            networkConfig.setUseMulticast(useMulticast);
            this.log("Using Multicast? " + useMulticast);
            
        }

        // Create network settings.
        if (CREATE_BUTTON.equals(networkChoice)) {
            this.getServletContext().log("Create network requested.");

            try {
                // Can`t use this because setmode overrides our settings. 
                //xwootEngine.createNetwork();
                
                networkConfig.clearRelaySeedingURIs();
                networkConfig.clearRelaySeeds();
                networkConfig.clearRendezvousSeedingURIs();
                networkConfig.clearRendezvousSeeds();
                
                networkManager.setUseDefaultSeeds(false);
                
                ((XWoot3) xwootEngine).getPeer().startNetworkAndConnect((XWoot3) xwootEngine, (XWoot3) xwootEngine);
                // request.getSession().setAttribute("join", Boolean.valueOf(false));
                // response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
            } catch (Exception e) {
                errors += "Can't create network:" + e.getMessage() + "\n";
            }

        // Join Network settings
        } else if (JOIN_BUTTON.equals(networkChoice)) {
            this.getServletContext().log("Join network requested.");
            
            errors += this.validateJoinFormFieldsFromRequest(request);
            if (errors == null || errors.length() == 0) {

                String useNetwork = request.getParameter("useNetwork");
                String rdvSeedingUriString = request.getParameter("rdvSeedingUri");
                String relaySeedingUriString = request.getParameter("relaySeedingUri");
                String rdvSeeds = request.getParameter("rdvSeeds");
                String relaySeeds = request.getParameter("relaySeeds");
    
                String[] rdvSeedsList = rdvSeeds.split("\\s*,\\s*");
                String[] relaySeedsList = relaySeeds.split("\\s*,\\s*");
            
                try {
                    // Clean any previously entered seeds and seedingUris.
                    networkConfig.clearRendezvousSeeds();
                    networkConfig.clearRendezvousSeedingURIs();
                    networkConfig.clearRelaySeeds();
                    networkConfig.clearRelaySeedingURIs();

                    if (CONCERTO_NETWORK.equals(useNetwork)) {
                        networkConfig.addRdvSeedingURI("http://jxta.concerto.com/rendezvousList.do");
                        networkConfig.addRelaySeedingURI("http://jxta.concerto.com/relayList.do");

                    } else if (PUBLIC_JXTA_NETWORK.equals(useNetwork)) {
                        networkManager.setUseDefaultSeeds(true);
                        networkConfig.addRdvSeedingURI("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi");
                        networkConfig.addRelaySeedingURI("http://rdv.jxtahosts.net/cgi-bin/relays.cgi");

                    } else if (CUSTOM_NETWORK.equals(useNetwork)) {
                        // Seeding URIs
                        if (rdvSeedingUriString != null && rdvSeedingUriString.trim().length() > 0) {
                            networkConfig.addRdvSeedingURI(URI.create(rdvSeedingUriString));
                        }
                        
                        if (relaySeedingUriString != null && relaySeedingUriString.trim().length() > 0) {
                            networkConfig.addRelaySeedingURI(URI.create(relaySeedingUriString));
                        }

                        // Rdv Seeds.
                        for (String rdvSeed : rdvSeedsList) {
                            if (rdvSeed.trim().length() != 0) {
                                networkConfig.addSeedRendezvous(URI.create(rdvSeed));
                            }
                        }

                        // Relay Seeds.
                        for (String relaySeed : relaySeedsList) {
                            if (relaySeed.trim().length() != 0) {
                                networkConfig.addSeedRelay(URI.create(relaySeed));
                            }
                        }
                        
                    }

                    xwootEngine.joinNetwork(null);

                    // Catch silent exceptions jxta is not throwing but just warning about.
                    if (!xwootEngine.isConnectedToP2PNetwork()) {
                        errors += "Can't join network. Failed to contact a RendezVous peer for the given network.";
                    }
                } catch (Exception e) {
                    // If exceptions come along the way or if joinNetwork() fails.
                    errors += "Can't join network: " + e.getMessage() + "\n";
                }

            }
        }

        // If no errors were encountered and successfully joined/created a network, go to next step.
        if (errors.length() == 0) {
            this.getServletContext().log("No errors occured.");
            
            // Stop the autosynch thread if it is running.
            XWootSite.getInstance().getAutoSynchronizationThread().stopThread();
            
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
            return;
        } else {
            this.getServletContext().log("Errors occured.");
        }

        // If any.
        errors = errors.replaceAll("\n", "<br/>");
        request.setAttribute("errors", errors);

        // No button clicked yet or an error occurred. Display the network boostrap page.
        request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
        return;

    }
    
    public String validateCommonFormFieldsFromRequest(HttpServletRequest request)
    {
        String errors = "";
        
        boolean useExternalIp = TRUE.equals(request.getParameter("useExternalIp"));
        String externalIp = request.getParameter("externalIp");

        boolean useTcp = TRUE.equals(request.getParameter("useTcp"));
        String tcpPortString = request.getParameter("tcpPort");

        boolean useHttp = TRUE.equals(request.getParameter("useHttp"));
        String httpPortString = request.getParameter("httpPort");
        
        if (useExternalIp) {
            if (externalIp == null || externalIp.trim().length() == 0) {
                errors += "No external IP provided.";
            }
        }
        
        if (!useTcp && !useHttp) {
            errors += "At least one communication method (TCP and/or HTTP) must be chosen.";
        } else {
            if (useTcp) {
                if (tcpPortString != null && tcpPortString.trim().length() != 0) {
                    try {
                        int port = Integer.parseInt(tcpPortString); 
                        if (port <= 0) {
                            errors += "TCP port number must be greater than 0.";
                        }
                        // TODO: check if port is busy/usable.
                    } catch (NumberFormatException e) {
                        errors += "Invalid TCP port.\n";
                    }
                } else {
                    errors += "No TCP port provided.\n";
                }
            }
            
            if (useHttp) {
                if (httpPortString != null && httpPortString.trim().length() != 0) {
                    try {
                        int port = Integer.parseInt(httpPortString); 
                        if (port <= 0) {
                            errors += "HTTP port number must be greater than 0.";
                        }
                        // TODO: check if port is busy/usable.
                    } catch (NumberFormatException e) {
                        errors += "Invalid HTTP port.\n";
                    }
                } else {
                    errors += "No HTTP port provided.\n";
                }
            }
        }
        
        return errors;
    }
    
    public String validateJoinFormFieldsFromRequest(HttpServletRequest request)
    {
        String errors = "";
        
        String useNetwork = request.getParameter("useNetwork");
        String rdvSeedingUri = request.getParameter("rdvSeedingUri");
        String relaySeedingUri = request.getParameter("relaySeedingUri");
        String rdvSeeds = request.getParameter("rdvSeeds");
        String relaySeeds = request.getParameter("relaySeeds");

        String[] rdvSeedsList = rdvSeeds.split(",");
        String[] relaySeedsList = relaySeeds.split(",");
        
        if (!CONCERTO_NETWORK.equals(useNetwork) && !PUBLIC_JXTA_NETWORK.equals(useNetwork) && !CUSTOM_NETWORK.equals(useNetwork)) {
            return "No network specified.\n";
        }
        
        if (rdvSeedingUri != null && rdvSeedingUri.trim().length() > 0) {
            try {
                URI uri = new URI(rdvSeedingUri);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                if (host == null || scheme == null) {
                    errors += rdvSeedingUri + " is not a valid location for retrieving rendezvous seeds.\n";
                }
            } catch (URISyntaxException e) {
                errors += rdvSeedingUri + " is not a valid location for retrieving rendezvous seeds.\n";
            }
        } else {
            // no rdv seeding uri provided.
            if (rdvSeedsList.length == 0 || (rdvSeedsList.length == 1 && rdvSeedsList[0].length() == 0)) {
                errors += "Must specify at least one RendezVous seed or RendezVous seeding URI.\n";
            }
        }
        
        if (relaySeedingUri != null && relaySeedingUri.trim().length() > 0) {
            try {
                URI uri = new URI(relaySeedingUri);
                String scheme = uri.getScheme();
                String host = uri.getHost();
                if (host == null || scheme == null) {
                    errors += rdvSeedingUri + " is not a valid location for retrieving relay seeds.\n";
                }
            } catch (URISyntaxException e) {
                errors += relaySeedingUri + " is not a valid location for retrieving relay seeds.\n";
            }
        }
     
        for (String rdvSeed : rdvSeedsList) {
            if (rdvSeed != null && rdvSeed.trim().length() != 0) {
                try {
                    URI seedUri = new URI(rdvSeed);
                    String scheme = seedUri.getScheme();
                    String host = seedUri.getHost();
                    if (host == null || scheme == null) {
                        errors += rdvSeed + " is not a valid RendezVous seed.\n";
                    } else if (seedUri.getPort() < 1) {
                        errors += rdvSeed + " has no port specified.\n";
                    }
                } catch (Exception e) {
                    errors += rdvSeed + " is not a valid RendezVous seed.";
                }
            }
        }
    
        for (String relaySeed : relaySeedsList) {
            if (relaySeed != null && relaySeed.trim().length() != 0) {
                try {
                    URI seedUri = new URI(relaySeed);
                    String scheme = seedUri.getScheme();
                    String host = seedUri.getHost();
                    if (host == null || scheme == null) {
                        errors += relaySeed + " is not a valid Relay seed.\n";
                    } else if (seedUri.getPort() < 1) {
                        errors += relaySeed + " has no port specified.\n";
                    }
                } catch (Exception e) {
                    errors += relaySeed + " is not a valid Relay seed.\n";
                }
            }
        }
        
        return errors;
    }
}
