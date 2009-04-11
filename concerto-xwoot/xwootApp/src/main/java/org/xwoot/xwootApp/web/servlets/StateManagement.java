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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class StateManagement extends HttpServlet
{
    private enum StateAction
    {
        UPLOAD, CREATE, RETRIEVE, REIMPORT_EXISTING
    }

    private File temp;

    private static final long serialVersionUID = -3758874922535817475L;

    /**
     * {@inheritDoc}
     * 
     * @see HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        Map<String, FileItem> requestParameters = parseMultipartParameters(request);

        XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();
        
        StateAction action = null;
        if (request.getParameter("action_upload") != null || requestParameters.containsKey("action_upload")) {
            action = StateAction.UPLOAD;
        } else if (request.getParameter("action_create") != null || requestParameters.containsKey("action_create")) {
            action = StateAction.CREATE;
        } else if (request.getParameter("action_retrieve") != null || requestParameters.containsKey("action_retrieve")) {
            action = StateAction.RETRIEVE;
        }
        System.out.println("Performing action: " + action);
        
        boolean uploadSuccessful = false;
        
        // The result of the action
        String currentState = null;
        String exceptionMessage = null;
        
//        // Check if this should be auto-join and this xwoot node already has the state of this group.
//        if (action == null) {
//            // If we have the state of this group.
//            if (xwootEngine.isStateComputed()) {
//                // Try to import it and replace the current internal model (if any).
//                try {
//                    xwootEngine.importState(xwootEngine.getState());
//                    action = StateAction.REIMPORT_EXISTING;
//                } catch (Exception e) {
//                    exceptionMessage = e.getMessage();
//                    this.log("Failed to import the existing state for this group.", e);
//                }
//            }
//        }

        /*
        // The peer specified as the network contact point.
        String neighbor = (String) request.getSession().getAttribute("neighbor");
        if (StringUtils.isBlank(neighbor)) {
            neighbor = null;
            System.out.println("No neighbor provided");
        } else {
            System.out.println("Neighbor: " + neighbor);
        }*/

        try {
            if (action == StateAction.UPLOAD) {
                this.log("Want to upload state file... ");
                if (requestParameters.containsKey("statefile")) {
                    this.temp = File.createTempFile("uploadedState", ".zip");
                    FileItem fileToUpload = requestParameters.get("statefile");
                    
                    if (!((XWoot3)xwootEngine).isGroupCreator() && 
                        !fileToUpload.getName().equals(((XWoot3)xwootEngine).getStateFileName())) {
                        exceptionMessage = "A state for a different group was provided. Please upload the state of this group if you already have it or ask a new one instead.";
                    } else if (this.upload(fileToUpload)) {
                        uploadSuccessful = true;
                        try {
                            XWootSite.getInstance().getXWootEngine().importState(this.temp);
                        } catch (Exception e) {
                            exceptionMessage = e.getMessage();
                            this.log("Problems with the uloaded state.", e);
                        }
                    }
                    
                    // remove the temporary uploaded file because it was copied to the right place by importState
                    this.temp.delete();
                }
            } else if (action == StateAction.RETRIEVE) {
                this.getServletContext().log("Retrieving state from group.");
                File newStateFile = ((XWoot3) xwootEngine).askStateToGroup();
                xwootEngine.importState(newStateFile);
            } else if (action == StateAction.CREATE) {
                this.log("Want to compute state");
                XWootSite.getInstance().getXWootEngine().connectToContentManager();
                XWootSite.getInstance().getXWootEngine().computeState();
            }
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
            this.log("Problem with state: " + e.getMessage(), e);
        }

        // If we performed an action or the state already exists.
        if (action != null || XWootSite.getInstance().getXWootEngine().isStateComputed()) {
            // If successful.
            if (exceptionMessage == null && XWootSite.getInstance().getXWootEngine().isStateComputed()) {
                
                if (!xwootEngine.isContentManagerConnected()) {
                    try {
                        xwootEngine.connectToContentManager();
                    } catch (XWootException e) {
                        this.log("Failed to connect to content manager. Please retry from the Synchronize page.", e);
                    }
                }
                
                try {
                    // Synchronize with the Content Provider.
                    xwootEngine.synchronize();
                } catch (XWootException e) {
                    this.log("Failed to synchronize before anti-entropy request with all neighbors.");
                }
                
                // Start the auto-synchronization thread
                XWootSite.getInstance().getAutoSynchronizationThread().startThread();
                
                // Do initial anti-entropy with all neighbors.
                try {
                    xwootEngine.doAntiEntropyWithAllNeighbors();
                } catch (XWootException e) {
                    this.log("Failed to launch initial anti-entropy request with all neighbors.");
                }
                
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/synchronize.do"));
                return;
            }
            
            // Otherwise
            switch (action) {
                case UPLOAD:
                    if (uploadSuccessful) {
                        currentState = "Problem with state: can't import selected file." + "\n(Details: "+exceptionMessage+")";
                    } else {
                        currentState = "Problem with state: please select a state file to upload." + (exceptionMessage != null ? "\n(Details: "+exceptionMessage+")" : "");
                    }
                    break;
                case CREATE:
                    currentState = "Problem with state: can't compute a new state." + "\n(Details: "+exceptionMessage+")";;
                    break;
                case RETRIEVE:
                    currentState = "Problem with state: can't import the state from the given peer." + "\n(Details: "+exceptionMessage+")";;
                    break;
                case REIMPORT_EXISTING:
                    currentState = "Failed to import the existing state for this group. Please get the group's state again." + " (Details: " + exceptionMessage + ")";
                default:
                    // First request, there's no error yet
                    break;
            }
        }

        request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.setAttribute("errors", currentState);
        request.setAttribute("groupCreator", ((XWoot3)xwootEngine).isGroupCreator());

        request.getRequestDispatcher("/pages/StateManagement.jsp").forward(request, response);
        return;
    }

    @SuppressWarnings("unchecked")
    private Map<String, FileItem> parseMultipartParameters(HttpServletRequest request) throws ServletException
    {
        Map<String, FileItem> result = new HashMap<String, FileItem>();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        System.out.println("Multipart : " + isMultipart);
        if (isMultipart) {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            List<FileItem> items;
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException e) {
                throw new ServletException(e);
            }

            for (FileItem i : items) {
                result.put(i.getFieldName(), i);
            }
        }
        return result;
    }

    private boolean upload(FileItem item) throws ServletException
    {
        System.out.println(item.toString());
        if (item.getSize() == 0) {
            return false;
        }
        try {
            item.write(this.temp);
            return true;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
