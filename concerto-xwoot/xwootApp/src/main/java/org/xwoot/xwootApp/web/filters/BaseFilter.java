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

package org.xwoot.xwootApp.web.filters;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * Filters all requests, and:
 * <ul>
 * <li>Forwards to the startup wizard when the xwoot engine is not initialized</li>
 * <li>Includes other peers in the network whenever a request from such a peer is received</li>
 * <li>Registers a skin in the session when a skin is requested</li>
 * </ul>
 * 
 * @todo Split this into several filters.
 * @todo Add a security filter.
 * @version $Id$
 */
public class BaseFilter implements Filter
{
    /** Logging helper object. */
    private static final Log LOG = LogFactory.getLog(BaseFilter.class);

    /** Serialization helper object. */
    private static final long serialVersionUID = -8050793384094800122L;

    /** {@inheritDoc} */
    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException,
        ServletException
    {
        HttpServletRequest request = (HttpServletRequest) srequest;
        HttpServletResponse response = (HttpServletResponse) sresponse;

        LOG.debug(MessageFormat.format("Received request from {0}@{1} for {2}", request.getRemoteAddr(), request
            .getRemotePort(), request.getRequestURL()));

        XWootSite site = XWootSite.getInstance();
        XWootAPI xwoot = site.getXWootEngine();

        // While the XWoot site is not fully configured, ensure the proper flow.
        if (!XWootSite.getInstance().isStarted()) {
            LOG.debug("Site is not started yet, starting the wizard.");
            if (!"/bootstrap.do".equals(request.getServletPath())) {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrap.do"));
            }
        } else if (!((XWoot3) xwoot).getPeer().isConnectedToNetwork()) {
            LOG.debug("Site is not connected to a network yet, opening network bootstrap.");
            if (!"/bootstrapNetwork.do".equals(request.getServletPath())) {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapNetwork.do"));
            }
        } else if (!((XWoot3) xwoot).getPeer().isConnectedToGroup()) {
            LOG.debug("Site is not connected to a group yet, opening group bootstrap.");
            if (!"/bootstrapGroup.do".equals(request.getServletPath())) {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
            }
        } else if (!(xwoot.isStateComputed())) {
            LOG.debug("Site does not have a state yet, opening stateManagement.");
            if (!"/stateManagement.do".equals(request.getServletPath())) {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/stateManagement.do"));
            }
        }
        
        // Add a header to inform about the presence of the xwoot service.
        response.addHeader("XWOOT_SERVICE", "xwoot service");

        LOG.debug("Base Filter applied");

        // Let the request be further processed.
        chain.doFilter(request, response);
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#destroy()
     */
    public void destroy()
    {
    }
}
