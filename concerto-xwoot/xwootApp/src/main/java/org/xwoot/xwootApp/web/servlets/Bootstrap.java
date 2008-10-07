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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @version $Id$
 */
public class Bootstrap extends HttpServlet
{
    private static final long serialVersionUID = -7533824334342866689L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        try {
            if (XWootSite.getInstance().isStarted()) {
                System.out.println("Site: " + XWootSite.getInstance().getXWootEngine().getPeerId()
                    + " Bootstrap - instance already started");
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/synchronize.do"));
                return;
            }

            String errors = "";
            String xwikiPropertiesFile =
                request.getSession().getServletContext().getRealPath(XWootSite.XWIKI_PROPERTIES_FILENAME);
            String xwootPropertiesFile =
                request.getSession().getServletContext().getRealPath(XWootSite.XWOOT_PROPERTIES_FILENAME);

            if (request.getParameter("update") != null) {
                errors =
                    XWootSite.getInstance().updatePropertiesFiles(request, xwikiPropertiesFile, xwootPropertiesFile);

                // Start the XWoot server if the properties were correctly
                // saved.
                if (StringUtils.isBlank(errors)) {
                    Properties p_xwiki = XWootSite.getInstance().getProperties(xwikiPropertiesFile);
                    Properties p_xwoot = XWootSite.getInstance().getProperties(xwootPropertiesFile);
                    XWootSite.getInstance().init(Integer.parseInt((String) p_xwoot.get(XWootSite.XWOOT_SITE_ID)),
                        (String) p_xwoot.get(XWootSite.XWOOT_SERVER_URL),
                        (String) p_xwoot.get(XWootSite.XWOOT_WORKING_DIR),
                        Integer.parseInt((String) p_xwoot.get(XWootSite.XWOOT_PBCAST_ROUND)),
                        Integer.parseInt((String) p_xwoot.get(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE)),
                        (String) p_xwiki.get(XWootSite.XWIKI_ENDPOINT), (String) p_xwiki.get(XWootSite.XWIKI_USERNAME),
                        (String) p_xwiki.get(XWootSite.XWIKI_PASSWORD));

                    System.out.println("Site :" + XWootSite.getInstance().getXWootEngine().getPeerId()
                        + " Bootstrap - starting instance -");
                    response
                        .sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapNetwork.do"));
                    return;
                }

                // There are errors, display the bootstrap page again.
                errors = errors.replaceAll("\n", "<br/>");
                request.setAttribute("errors", errors);
            }

            if (!StringUtils.isBlank(xwikiPropertiesFile) && !StringUtils.isBlank(xwootPropertiesFile)) {
                Properties p_xwiki =
                    XWootSite.getInstance().updateXWikiPropertiesFromRequest(request, xwikiPropertiesFile);
                Properties p_xwoot =
                    XWootSite.getInstance().updateXWootPropertiesFromRequest(request, xwootPropertiesFile);
                if (StringUtils.isBlank(p_xwoot.getProperty(XWootSite.XWOOT_SITE_ID))) {
                    p_xwoot.put(XWootSite.XWOOT_SITE_ID, new Integer(RandomUtils.nextInt(1000000) + 1000000));
                }
                request.setAttribute("xwiki_properties", p_xwiki);
                request.setAttribute("xwoot_properties", p_xwoot);
            }

            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            System.out.println("EXCEPTION catched !!");
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        }
    }
}
