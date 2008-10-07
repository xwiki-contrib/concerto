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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class BootstrapNetwork extends HttpServlet
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
        request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.getSession().removeAttribute("neighbor");
        request.getSession().removeAttribute("join");

        if (request.getParameter("choice_join") != null) {
            String neighbor = request.getParameter("neighbor");
            if (!StringUtils.isBlank(neighbor)) {
                try {
                    if (!XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                        XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
                    }
                    if (XWootSite.getInstance().getXWootEngine().getNeighborsList().contains(neighbor)
                        || XWootSite.getInstance().getXWootEngine().addNeighbour(neighbor)) {
                        request.getSession().setAttribute("join", Boolean.valueOf(true));
                        request.getSession().setAttribute("neighbor", neighbor);
                        response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
                            + "/stateManagement.do"));
                        return;
                    }
                } catch (Exception e) {
                    throw new ServletException(e);
                }
                System.out.println("can't add this neighbor");

            } else {
                request.setAttribute("errors", "Invalid peer name.");
            }
        } else if (request.getParameter("choice_create") != null) {
            System.out.println("Create");
            try {
                if (XWootSite.getInstance().getXWootEngine().createNetwork()) {
                    request.getSession().setAttribute("join", Boolean.valueOf(false));
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
                    return;
                }
            } catch (Exception e) {
                new ServletException(e);
            }
        }

        // No button clicked yet, display the network boostrap page.
        request.getRequestDispatcher("/pages/BootstrapNetwork.jsp").forward(request, response);
        return;

    }
}
