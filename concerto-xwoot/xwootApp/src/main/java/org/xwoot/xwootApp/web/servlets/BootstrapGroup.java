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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.commons.lang.StringUtils;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class BootstrapGroup extends HttpServlet
{
    private static final long serialVersionUID = -3758874922535817475L;

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        this.getServletContext().log("BootstrapGroup opened.");
        
        /*request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.getSession().removeAttribute("neighbor");
        request.getSession().removeAttribute("join");*/
        
        String errors = "";
        
        XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();
        
        if (!xwootEngine.isConnectedToP2PNetwork()) {
            this.getServletContext().log("Please connect to a network first.");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapNetwork.do"));
            return;
        }
        /*
        NetworkManager networkManager = ((XWoot3) xwootEngine).getPeer().getManager();
        NetworkConfigurator networkConfig = networkManager.getConfigurator();*/
        
        String groupChoice = request.getParameter("groupChoice");
        
        if (StringUtils.equals(groupChoice, "Create")) {
            this.getServletContext().log("Create group requested.");
            
            String groupName = request.getParameter("groupName");
            String groupDescription = request.getParameter("groupDescription");
            String privateGroup = request.getParameter("privateGroup");
            String groupPassword = request.getParameter("createGroupPassword");
            String groupPasswordRetyped = request.getParameter("createGroupPasswordRetyped");
            String keystorePassword = request.getParameter("createKeystorePassword");
            
            try {
                boolean privateGroupBoolean = Boolean.parseBoolean(privateGroup);
                if (privateGroupBoolean) {
                    if (groupPassword == null || groupPassword.length() == 0) {
                        throw new IllegalArgumentException("A password must be set for a private group.");
                    }
                    if (!(groupPassword.equals(groupPasswordRetyped))) {
                        throw new IllegalArgumentException("Passwords do not match.");
                    }
                }
                
                //boolean beRendezVousBoolean = Boolean.parseBoolean(beRendezVous);
                ((XWoot3) xwootEngine).createNewGroup(groupName, groupDescription, keystorePassword.toCharArray(), groupPassword.toCharArray());
            } catch (Exception e) {
                errors += "Can't create group:" + e.getMessage() + "\n";
            }
            
        } else if (StringUtils.equals(groupChoice, "Join")) {
            this.getServletContext().log("Join group requested.");
            //
            String groupPassword = request.getParameter("joinGroupPassword");
            String keystorePassword = request.getParameter("joinGroupKeystorePassword");
            String beRendezVousString = request.getParameter("beRendezVous");
            
            String groupID = request.getParameter("groupID");
            if (groupID == null || groupID.length() == 0) {
                errors += "Please select a group to join first.";
            } else {
                try {
                    Collection groups = ((XWoot3) xwootEngine).getGroups();
                    boolean found = false;
                    for(Object group : groups) {
                        PeerGroupAdvertisement aGroupAdv = (PeerGroupAdvertisement) group;
                        if (aGroupAdv.getPeerGroupID().toString().equals(groupID)) {
                            this.log("Joining group described by this adv:\n" + aGroupAdv);
                            this.log("Using group password: " + groupPassword + " (" + Arrays.toString(groupPassword.toCharArray()) + ")");
                            this.log("Using keystore password: " + keystorePassword + " (" + Arrays.toString(keystorePassword.toCharArray()) + ")");
                            
                            Boolean beRendezVous = Boolean.parseBoolean(beRendezVousString);
                            
                            ((XWoot3) xwootEngine).joinGroup(aGroupAdv, keystorePassword.toCharArray(), groupPassword.toCharArray(), beRendezVous);
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        errors += "Invalid group selected or it has expired.";
                    }
                } catch (Exception e) {
                    String message = null;
                    if (e.getMessage() == null) {
                        message = e.getClass().getName();
                    } else {
                        message = e.getMessage();
                    }
                    
                    errors += "Can't join group: " + message + ".";
                }
            }
        }
        
        // If no errors were encountered and successfuly joined/created a network, go to next step.
        if (errors.length() == 0 && (StringUtils.equals(groupChoice, "Create") || StringUtils.equals(groupChoice, "Join"))) {
            this.getServletContext().log("No errors occured.");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
            return;
        } else {
            this.getServletContext().log("Errors occured or invalid group choice.");
        }
        
        try {
            request.setAttribute("groups", ((XWoot3) xwootEngine).getGroups());
            this.getServletContext().log("Available groups: " + request.getAttribute("groups"));
        } catch (Exception e) {
            request.setAttribute("groups", new ArrayList());
            errors += "Failed to list groups: " + e.getMessage();
        }

        // If any.
        request.setAttribute("errors", errors);
        
        // No button clicked yet or an error occurred. Display the network boostrap page.
        request.getRequestDispatcher("/pages/BootstrapGroup.jsp").forward(request, response);
        return;

    }
}
