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

import java.io.IOException;
import java.net.URI;

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
        request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.getSession().removeAttribute("neighbor");
        request.getSession().removeAttribute("join");

        String errors = "";

        NetworkManager networkManager = ((XWoot3) xwootEngine).getPeer().getManager();
        NetworkConfigurator networkConfig = networkManager.getConfigurator();

        String networkChoice = request.getParameter("networkChoice");

        if (networkChoice == null) {
            // No button clicked yet.
            request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
            return;
        }

        // Common settings.
        if (CREATE_BUTTON.equals(networkChoice) || JOIN_BUTTON.equals(networkChoice)) {
            boolean useExternalIp = TRUE.equals(request.getParameter("useExternalIp"));
            String externalIp = request.getParameter("externalIp");
            boolean useOnlyExternalIp = TRUE.equals(request.getParameter("useOnlyExternalIp"));

            boolean useTcp = TRUE.equals(request.getParameter("useTcp"));
            String tcpPort = request.getParameter("tcpPort");

            boolean useHttp = "true".equals(request.getParameter("useHttp"));
            String httpPort = request.getParameter("httpPort");

            boolean useMulticast = "true".equals(request.getParameter("useMulticast"));

            try {
                // Disconnect from any connected network.
                if (xwootEngine.isConnectedToP2PNetwork()) {
                    xwootEngine.disconnectFromP2PNetwork();
                }

                networkConfig.setTcpEnabled(useTcp);

                if (useTcp) {
                    this.log("Using TCP");

                    networkConfig.setTcpIncoming(true);
                    networkConfig.setTcpOutgoing(true);
                    networkConfig.setTcpPort(Integer.parseInt(tcpPort));

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
                    networkConfig.setHttpPort(Integer.parseInt(httpPort));

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
            } catch (Exception e) {
                // FIXME: handle errors.
            }
        }

        if (CREATE_BUTTON.equals(networkChoice)) {
            this.getServletContext().log("Create network requested.");

            try {
                xwootEngine.createNetwork();
                // request.getSession().setAttribute("join", Boolean.valueOf(false));
                // response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
            } catch (Exception e) {
                errors += "Can't create network:" + e.getMessage() + "\n";
            }

        } else if (JOIN_BUTTON.equals(networkChoice)) {
            this.getServletContext().log("Join network requested.");

            String useNetwork = request.getParameter("useNetwork");
            String rdvSeedingUri = request.getParameter("rdvSeedingUri");
            String relaySeedingUri = request.getParameter("relaySeedingUri");
            String rdvSeeds = request.getParameter("rdvSeeds");
            String relaySeeds = request.getParameter("relaySeeds");
            boolean beRendezVous = TRUE.equals(request.getParameter("beRendezVous"));
            boolean beRelay = TRUE.equals(request.getParameter("beRelay"));

            String[] rdvSeedsList = rdvSeeds.split("\\s*,\\s*");
            String[] relaySeedsList = relaySeeds.split("\\s*,\\s*");

            if (useNetwork != null) {
                if (useNetwork.equals("custom") && (rdvSeedingUri == null && rdvSeeds == null)) {
                    errors += "Can not join custom network. No rdvSeedingUri or rdvSeeds supplied.\n";
                } else {
                    try {
                        if (xwootEngine.isConnectedToP2PNetwork()) {
                            xwootEngine.disconnectFromP2PNetwork();
                        }

                        // Clean any previously entered seeds and seedingUris.
                        networkConfig.clearRendezvousSeeds();
                        networkConfig.clearRendezvousSeedingURIs();
                        networkConfig.clearRelaySeeds();
                        networkConfig.clearRelaySeedingURIs();

                        if (useNetwork.equals("concerto")) {
                            networkConfig.addRdvSeedingURI("http://jxta.concerto.com/rendezvousList.do");
                            networkConfig.addRelaySeedingURI("http://jxta.concerto.com/relayList.do");

                        } else if (useNetwork.equals("publicJxta")) {
                            networkManager.setUseDefaultSeeds(true);
                            networkConfig.addRdvSeedingURI("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi");
                            networkConfig.addRelaySeedingURI("http://rdv.jxtahosts.net/cgi-bin/relays.cgi");

                        } else if (useNetwork.equals("custom")) {
                            networkConfig.addRdvSeedingURI(rdvSeedingUri);
                            networkConfig.addRelaySeedingURI(relaySeedingUri);

                            // Rdv Seeds.
                            for (String rdvSeed : rdvSeedsList) {
                                try {
                                    URI seedUri = new URI(rdvSeed);
                                    if (seedUri.getPort() < 1) {
                                        throw new IllegalArgumentException(rdvSeed + " is an invalid RDV seed.\n");
                                    }
                                    networkConfig.addSeedRendezvous(seedUri);
                                } catch (Exception e) {
                                    // ignore invalid entry.
                                    continue;
                                }
                            }

                            // Relay Seeds.
                            for (String relaySeed : relaySeedsList) {
                                try {
                                    URI seedUri = new URI(relaySeed);
                                    if (seedUri.getPort() < 1) {
                                        throw new IllegalArgumentException(relaySeed + " is an invalid Relay seed.\n");
                                    }
                                    networkConfig.addSeedRendezvous(seedUri);
                                } catch (Exception e) {
                                    // ignore invalid entry.
                                    continue;
                                }
                            }
                        }

                        ConfigMode mode = null;
                        if (beRendezVous && beRelay) {
                            mode = ConfigMode.RENDEZVOUS_RELAY;
                        } else if (beRendezVous) {
                            mode = ConfigMode.RENDEZVOUS;
                        } else if (beRelay) {
                            mode = ConfigMode.RELAY;
                        }

                        this.log("Setting this peer to " + mode + " mode.");

                        if (mode != null) {
                            networkManager.setMode(mode);
                        }

                        xwootEngine.joinNetwork(null);

                        // Catch silent exceptions jxta is not throwing but just warning about.
                        if (!xwootEngine.isConnectedToP2PNetwork()) {
                            errors += "Can't join network. Failed to contact a RendezVous peer for the given network.";
                        }
                    } catch (Exception e) {
                        // If exceptions come along the way or if joinNetwork() fails.
                        errors += "Can't join network:" + e.getMessage() + "\n";
                    }

                }
            } else {
                errors += "Can't join network. No network sepecified.";
            }
        }

        // If no errors were encountered and successfully joined/created a network, go to next step.
        if (errors.length() == 0) {
            this.getServletContext().log("No errors occured.");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
            return;
        } else {
            this.getServletContext().log("Errors occured.");
        }

        // If any.
        request.setAttribute("errors", errors);

        // No button clicked yet or an error occurred. Display the network boostrap page.
        request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
        return;

    }
}
