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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.xwootApp.XWoot;
import org.xwoot.xwootApp.core.XWootPage;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PageManagement extends HttpServlet
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

        if (!XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
            System.out.println("Can't access to page management page : xwiki is not connected !");
            request.getRequestDispatcher("/pages/Synchronize.jsp").forward(request, response);

            return;
        }

        System.out.print("Site " + XWootSite.getInstance().getXWootEngine().getPeerId() + " : Page management page -");

        Collection spaces;

        try {
            spaces = ((XWoot) XWootSite.getInstance().getXWootEngine()).getContentManager().getListSpaceId();
        } catch (Exception e1) {
            throw new ServletException(e1);
        }

        request.setAttribute("spaces", spaces);
        request.setAttribute("noPage", Boolean.TRUE);

        if ("printPages".equals(request.getParameter("action"))) {
            String currentSpace = request.getParameter("currentSpace");
            Collection pages;
            try {
                pages =
                    ((XWoot) XWootSite.getInstance().getXWootEngine()).getContentManager().getListPageId(currentSpace);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            Map managedPagesMap = ((XWoot) XWootSite.getInstance().getXWootEngine()).isPagesManaged(pages);

            request.setAttribute("currentSpace", currentSpace);
            request.setAttribute("managedPages", managedPagesMap);
            request.setAttribute("noPage", Boolean.FALSE);
        } else if ("validate".equals(request.getParameter("action"))) {
            System.out.println(" validate -");

            String space = request.getParameter("space");
            List<String> managedPages = new ArrayList<String>();

            for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                String paramName = (String) e.nextElement();

                if (paramName.startsWith("PAGE-")) {
                    String pageName = paramName.substring("PAGE-".length());
                    managedPages.add(pageName);
                }
            }

            try {
                ((XWoot) XWootSite.getInstance().getXWootEngine()).setPageManagement(space, managedPages);
            } catch (Exception e) {
                throw new ServletException(e);
            }

            request.setAttribute("noPage", Boolean.TRUE);
        } else if ("addPage".equals(request.getParameter("action"))) {
            String page = request.getParameter("document");
            System.out.println(" add page [" + page + "]");
            try {
                ((XWoot) XWootSite.getInstance().getXWootEngine()).addPageManagement(new XWootPage(page, null));
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else if ("removePage".equals(request.getParameter("action"))) {
            String page = request.getParameter("document");
            System.out.println(" remove page [" + page + "]");
            try {
                ((XWoot) XWootSite.getInstance().getXWootEngine()).removeManagedPage(new XWootPage(page, null));
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            System.out.println(" no action ! -");
        }

        request.getRequestDispatcher("/pages/PageManagement.jsp").forward(request, response);

        return;
    }
}
