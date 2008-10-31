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

package org.xwoot.iwoot.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

/**
 * DOCUMENT ME!
 * 
 * @version $Id: Bootstrap.java 13483 2008-10-10 11:31:04Z jmaire $
 */
public class InitFilter implements Filter
{
    private static final long serialVersionUID = -7533824334342866689L;

    /** The filter configuration, used for accessing the servlet context. */
    private FilterConfig config = null;

    /**
     * {@inheritDoc}
     * 
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException,
    ServletException
    {
        // Verifying initialization
        if (srequest.getParameter("init") != null) {
            // Let the request be further processed.
            chain.doFilter(srequest, sresponse);
            return;

        }

        HttpServletRequest request = (HttpServletRequest) srequest;
        HttpServletResponse response = (HttpServletResponse) sresponse;

        System.out.println("Bootstrap IWoot !");

        RestApplication appli=(RestApplication) this.config.getServletContext().getAttribute("com.noelios.restlet.ext.servlet.ServerServlet.application");


        if (appli!=null && appli.getIwoot()==null) {
            try {   
                WikiContentManager WCM = WikiContentManagerFactory.getMockFactory().createWCM();
                String pageId="test.page0";
                String pageContent="Content of existing page";
                WCM.createPage(pageId, pageContent);

                XWootClientAPI xwootClient=XWootClientFactory.getMockFactory().createXWootClient();

                IWoot iwoot = new IWoot(xwootClient, WCM, Integer.valueOf(1));

                appli.setIwoot(iwoot);
            } catch (XWootClientException e1) {
                throw new ServletException(e1);
            } catch (WikiContentManagerException e) {
                throw new ServletException(e);
            }

            request.getSession().setAttribute("init",Boolean.valueOf(true));
        }
        // Let the request be further processed.
        chain.doFilter(request, response);
        return;  
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#destroy()
     */
    public void destroy()
    {
        this.config = null;
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.config=filterConfig;
    }

    private Properties loadProperties(String path) throws ServletException{
        // loadproperties
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(path));
        } catch (FileNotFoundException e) {
           throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
        return props;
    }
}
