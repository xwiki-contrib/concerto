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
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.document.AdvertisementFactory;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.commons.lang.StringUtils;
import org.xwoot.jxta.JxtaPeer;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class Synchronize extends HttpServlet
{
    private static final long serialVersionUID = -3758874922535817475L;

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        System.out.print("Site " + XWootSite.getInstance().getXWootEngine().getXWootPeerId() + " : Synchronize page -");
        
        XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();

        // synchronize
        if ("synchronize".equals(request.getParameter("action"))
            && XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
            this.log("Synchronization requested.");
            try {
                XWootSite.getInstance().getXWootEngine().synchronize();
            } catch (Exception e) {
                this.log("Error while synchronizing.\n", e);
                
                // FIXME: bring back the "errors" mechanism for this page as well instead of throwing servlet exceptions.
                throw new ServletException(e);
            }
        }

        // anti entropy
        else if ("antiEntropy".equals(request.getParameter("action"))
            && XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
            String neighbor = request.getParameter("neighbor");
            try {
                XWootSite.getInstance().getXWootEngine().doAntiEntropy(neighbor);
            } catch (Exception e) {
                this.log("Problems while doing anti-entropy with " + neighbor, e);
                
                //FIXME: bring back the "errors" mechanism for this page as well instead of throwing servlet exceptions.
                throw new ServletException(e);
            }
        }

        // p2p connection
        else if ("p2pnetworkconnection".equals(request.getParameter("action"))) {
            this.log("P2P connection gestion ...");
            try {
//                String mode = request.getParameter("switch");
//                if ("on".equals(mode)
//                    && !XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
//                    XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
//                } else if ("off".equals(mode)
//                    && XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
//                    XWootSite.getInstance().getXWootEngine().disconnectFromP2PNetwork();
//                } else {
                    if (XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                        XWootSite.getInstance().getXWootEngine().disconnectFromP2PNetwork();
                        
                        // Stop auto-synchronization. We don't need redundant patches.
                        XWootSite.getInstance().getAutoSynchronizationThread().stopThread();
                    } else {
                        XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
                        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
                        return;
                    }
//                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        // cp connection
        else if ("cpconnection".equals(request.getParameter("action"))) {
            this.log("Content Provider connection gestion ...");
            try {
                if (XWootSite.getInstance().getXWootEngine().isConnectedToP2PGroup()){
                    XWootSite.getInstance().getXWootEngine().doAntiEntropyWithAllNeighbors();
                } 
                String mode = request.getParameter("switch");
                if (StringUtils.equals(mode, "on")
                    && !XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
                    XWootSite.getInstance().getXWootEngine().connectToContentManager();
                } else if (StringUtils.equals(mode, "off")
                    && XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
                    XWootSite.getInstance().getXWootEngine().disconnectFromContentManager();
                } else {
                    if (XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
                        XWootSite.getInstance().getXWootEngine().disconnectFromContentManager();
                    } else {
                        XWootSite.getInstance().getXWootEngine().connectToContentManager();  
                    }
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        else {
            this.log(" no action ! -");
        }

        // view neighbors list
        Collection<PipeAdvertisement> neighbors = null;
        try {
            neighbors = xwootEngine.getNeighborsList();
        } catch (Exception e) {
            // remove this with new xwootAPI adapted to XWoot3.
        }
        
        if (neighbors != null) {
            HashMap<PipeAdvertisement, Boolean> result = new HashMap<PipeAdvertisement, Boolean>();
            for (PipeAdvertisement n : neighbors) {
                
                // send to the UI a lighter, copy version having a human-readable name.
                PipeAdvertisement original = n;
                n = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
                n.setPipeID(original.getPipeID());
                n.setName(JxtaPeer.getPeerNameFromBackChannelPipeName(original.getName()));
                n.setType(original.getType());
                
                if (!XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                    this.log(n + " Site " + n + " is not connected because we are disconnected.");
                    result.put(n, Boolean.FALSE);
                } else {
                    //TODO: implement a ping mechanism.
                    /*URL to = new URL(n + "/synchronize.do?test=true");
                    try {
                        HttpURLConnection init = (HttpURLConnection) to.openConnection();
                        result.put(n, Boolean.valueOf(init.getResponseMessage().contains("OK")));
                        init.disconnect();
                    } catch (Exception e) {
                        System.out.println(n + " Neighbor " + n + " is not connected");
                        result.put(n, Boolean.FALSE);
                    }*/
                    result.put(n, Boolean.TRUE);
                }
            }
            request.setAttribute("noneighbor", Boolean.valueOf(neighbors.size() == 0));
            request.setAttribute("neighbors", result);
        } else {
            request.setAttribute("noneighbor", true);
        }
            
        request.setAttribute("content_provider", XWootSite.getInstance().getXWootEngine().getContentProvider());
        request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.setAttribute("p2pconnection", Boolean.valueOf(XWootSite.getInstance().getXWootEngine()
            .isConnectedToP2PNetwork()));
        request.setAttribute("cpconnection", Boolean.valueOf(XWootSite.getInstance().getXWootEngine()
            .isContentManagerConnected()));
        request.getRequestDispatcher("/pages/Synchronize.jsp").forward(request, response);


        return;
    }
}
