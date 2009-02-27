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

import net.jxta.protocol.PipeAdvertisement;

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
public class Synchronize extends HttpServlet
{
    private static final long serialVersionUID = -3758874922535817475L;

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        System.out.print("Site " + XWootSite.getInstance().getXWootEngine().getXWootPeerId() + " : Synchronize page -");
        
        XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();
/*
        // test
        if (request.getParameter("test") != null) {

            if (!XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                System.out.println("Neighbor not connected");
                response.getWriter().print("Neighbor not connected");
                response.setHeader("connected", "false");
            } else {
                response.setHeader("connected", "true");
                System.out.println("Neighbor " + XWootSite.getInstance().getXWootEngine().getXWootPeerId()
                    + " is connected.");
            }
            return;
        }
        // add neighbor
        else if ("addNeighbor".equals(request.getParameter("action"))) {
            String neighbor = request.getParameter("neighbor");
            if (neighbor != null && !neighbor.trim().equals("")) {
                try {
                    System.out.println(" receive neighbour : " + neighbor + " -");
                    if (XWootSite.getInstance().getXWootEngine().addNeighbour(neighbor)) {
                        XWootSite.getInstance().getXWootEngine().doAntiEntropy(neighbor);
                    }
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            } else {
                System.out.println(" want to add a neighbor");
                request.setAttribute("action", "addneighbor");
            }
        }

        // remove neighbor
        else if ("removeNeighbor".equals(request.getParameter("action"))) {
            String neighbor = request.getParameter("neighbor");
            System.out.println(" remove neighbour : " + neighbor + " -");
            try {
                XWootSite.getInstance().getXWootEngine().removeNeighbor(NetUtil.normalize(neighbor));
            } catch (URISyntaxException e) {
                throw new ServletException(e);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
*/
        // else if ("true".equals(request.getParameter("addneighbor"))) {
        // System.out.println(" want to add a neighbor");
        // request.setAttribute("addneighbor", Boolean.TRUE);
        // }

        // page management
       /* else if ("pageManagement".equals(request.getParameter("action"))
            && XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
            System.out.print("pageManagement -- ");
            try {
                if ("all".equals(request.getParameter(("val")))) {
                    System.out.println(" add all pages -");

                    ((XWoot) XWootSite.getInstance().getXWootEngine()).addAllPageManagement();

                } else if ("remove".equals(request.getParameter("val"))) {
                    System.out.println(" remove all pages -");
                    ((XWoot) XWootSite.getInstance().getXWootEngine()).removeAllManagedPages();
                } else if ("custom".equals(request.getParameter("val"))) {
                    this.customPageManagement(request);
                }
            } catch (WikiContentManagerException e) {
                throw new ServletException(e);
            } catch (XWootException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }*/

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
                String mode = request.getParameter("switch");
                if (StringUtils.equals(mode, "on")
                    && !XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                    XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
                } else if (StringUtils.equals(mode, "off")
                    && XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                    XWootSite.getInstance().getXWootEngine().disconnectFromP2PNetwork();
                } else {
                    if (XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                        XWootSite.getInstance().getXWootEngine().disconnectFromP2PNetwork();
                    } else {
                        XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
                    }
                }
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
            neighbors = XWootSite.getInstance().getXWootEngine().getNeighborsList();
        } catch (Exception e) {
            // remove this with new xwootAPI addapted to XWoot3.
        }
        
        if (neighbors != null) {
            HashMap<PipeAdvertisement, Boolean> result = new HashMap<PipeAdvertisement, Boolean>();
            for (PipeAdvertisement n : neighbors) {
                if ( ((XWoot3)xwootEngine).getPeer().getMyDirectCommunicationPipeAdvertisement().equals(n)) {
                    // skip out pipe adv.
                    continue;
                }
                
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
            /* NetUtil.READ_TIME_OUT=temp; */
            /*Collection pages = ((XWoot2) XWootSite.getInstance().getXWootEngine()).getListOfManagedPages();*/
           /* request.setAttribute("nopage", Boolean.valueOf(pages.size() == 0));*/
            request.setAttribute("noneighbor", Boolean.valueOf(neighbors.size() == 0));
            request.setAttribute("neighbors", result);
        } else {
            request.setAttribute("noneighbor", true);
        }
            
      /*  request.setAttribute("pages", pages);*/
        request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.setAttribute("p2pconnection", Boolean.valueOf(XWootSite.getInstance().getXWootEngine()
            .isConnectedToP2PNetwork()));
        request.setAttribute("cpconnection", Boolean.valueOf(XWootSite.getInstance().getXWootEngine()
            .isContentManagerConnected()));
        request.getRequestDispatcher("/pages/Synchronize.jsp").forward(request, response);


        return;
    }

  /*  private void customPageManagement(HttpServletRequest request) throws WikiContentManagerException, XWootException
    {
        System.out.print("Site " + XWootSite.getInstance().getXWootEngine().getXWootPeerId()
            + " : Page management page -");

        if (!XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
            return;
        }

        Collection spaces = ((XWoot) XWootSite.getInstance().getXWootEngine()).getContentManager().getListSpaceId();
        request.setAttribute("customPage", Boolean.TRUE);
        request.setAttribute("spaces", spaces);
        request.setAttribute("noPageChoice", Boolean.TRUE);

        if ("printPages".equals(request.getParameter("actionManagement"))) {

            String currentSpace = request.getParameter("currentSpace");
            Collection pages =
                ((XWoot) XWootSite.getInstance().getXWootEngine()).getContentManager().getListPageId(currentSpace);
            Map managedPagesMap = ((XWoot) XWootSite.getInstance().getXWootEngine()).isPagesManaged(pages);

            request.setAttribute("currentSpace", currentSpace);
            request.setAttribute("managedPages", managedPagesMap);
            request.setAttribute("noPageChoice", Boolean.FALSE);
        } else if ("validate".equals(request.getParameter("actionManagement"))) {
            System.out.println(" validate -");

            String space = request.getParameter("space");
            List<String> managedPages = new ArrayList<String>();

            for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                String paramName = (String) e.nextElement();

                if (paramName.startsWith("PAGE-")) {
                    String pageName = paramName.substring(5);
                    managedPages.add(pageName);
                }
            }

            ((XWoot) XWootSite.getInstance().getXWootEngine()).setPageManagement(space, managedPages);

            request.setAttribute("noPageChoice", Boolean.TRUE);
        } else {
            System.out.println(" no action ! -");
        }
    }*/
}
