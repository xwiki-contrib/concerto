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
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.protocol.PeerGroupAdvertisement;

import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * Servlet handling group management.
 * 
 * @version $Id$
 */
public class BootstrapGroup extends HttpServlet
{
    /** The request attribute trough which to send the list of groups to the jsp. */
    private static final String AVAILABLE_GROUPS_ATTRIBUTE = "groups";
    
    /** The value of the join group button. */
    private static final String JOIN_BUTTON = "Join";
    
    /** The value of the create group button. */
    private static final String CREATE_BUTTON = "Create";
    
    /** The value of a checked checkbox. */
    private static final String TRUE = "true";
    
    /** The default keystore password for jxta's authentication protocol. */
    private static final char[] KEYSTORE_PASSWORD = "concerto".toCharArray();
    
    /** Used for serialization. */
    private static final long serialVersionUID = -3758874922535817475L;

    /** {@inheritDoc} */
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
        
        String groupChoice = request.getParameter("groupChoice");
        
        if (CREATE_BUTTON.equals(groupChoice)) {
            this.getServletContext().log("Create group requested.");
            
            String groupName = request.getParameter("groupName");
            String groupDescription = request.getParameter("groupDescription");
            boolean isPrivateGroup = TRUE.equals(request.getParameter("isPrivateGroup"));
            String groupPassword = request.getParameter("createGroupPassword");
            String groupPasswordRetyped = request.getParameter("createGroupPasswordRetyped");
            //String keystorePassword = request.getParameter("createKeystorePassword");
            
            try {
                if (isPrivateGroup) {
                    if (groupPassword == null || groupPassword.length() == 0) {
                        throw new IllegalArgumentException("A password must be set for a private group.");
                    }
                    if (!(groupPassword.equals(groupPasswordRetyped))) {
                        throw new IllegalArgumentException("Passwords do not match.");
                    }
                }
                
                ((XWoot3) xwootEngine).createNewGroup(groupName, groupDescription, KEYSTORE_PASSWORD/*keystorePassword.toCharArray()*/, groupPassword.toCharArray());
            } catch (Exception e) {
                errors += "Can't create group:" + e.getMessage() + "\n";
            }
            
        } else if (JOIN_BUTTON.equals(groupChoice)) {
            this.getServletContext().log("Join group requested.");
            //
            String groupPassword = request.getParameter("joinGroupPassword");
            //String keystorePassword = request.getParameter("joinGroupKeystorePassword");
            
            boolean beRendezVous = TRUE.equals(request.getParameter("beRendezVous"));
            
            String groupID = request.getParameter("groupID");
            if (groupID == null || groupID.length() == 0) {
                errors += "Please select a group to join first.";
            } else {
                try {
                    Collection groups = ((XWoot3) xwootEngine).getGroups();
                    boolean found = false;
                    for (Object group : groups) {
                        PeerGroupAdvertisement aGroupAdv = (PeerGroupAdvertisement) group;
                        if (aGroupAdv.getPeerGroupID().toString().equals(groupID)) {
                            this.log("Joining group described by this adv:\n" + aGroupAdv);
                            
                            ((XWoot3) xwootEngine).joinGroup(aGroupAdv, KEYSTORE_PASSWORD/*keystorePassword.toCharArray()*/, groupPassword.toCharArray(), beRendezVous);
                            
                            // Save the group advertisement to be able to rejoin after a reboot.
                            // FIXME: save group and keystore password then implement the deletion of the current group
                            // advertisement file when explicitly leaving a group from the web UI.
                            // try {
                            // PersistencyUtil.saveObjectToXml(aGroupAdv, xwootEngine.getWorkingDir() + File.separator
                            // + "currentGroupAdvertisement.xml");
                            // } catch (Exception e) {
                            // this.log("Failed to save the group advertisement. "
                            // + "Auto-join will not be available on next restart.", e);
                            // }
                            
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
        
        // FIXME: try to auto-join a group if a group advertisement file is found.
        
        // If no errors were encountered and successfully joined/created a network, go to next step.
        if (errors.length() == 0 && (CREATE_BUTTON.equals(groupChoice) || JOIN_BUTTON.equals(groupChoice))) {
            this.getServletContext().log("No errors occured.");
            
            // Stop the autosynch thread if it is running.
            XWootSite.getInstance().getAutoSynchronizationThread().stopThread();
            
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
            return;
        } else {
            this.getServletContext().log("Errors occured or invalid group choice.");
        }
        
        try {
            request.setAttribute(AVAILABLE_GROUPS_ATTRIBUTE, ((XWoot3) xwootEngine).getGroups());
            this.getServletContext().log("Available groups: " + request.getAttribute(AVAILABLE_GROUPS_ATTRIBUTE));
        } catch (Exception e) {
            request.setAttribute(AVAILABLE_GROUPS_ATTRIBUTE, new ArrayList());
            errors += "Failed to list groups: " + e.getMessage();
        }

        // If any.
        request.setAttribute("errors", errors);
        
        // No button clicked yet or an error occurred. Display the network boostrap page.
        request.getRequestDispatcher("/pages/BootstrapGroup.jsp").forward(request, response);
        return;

    }
}
