/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.xwoot.manager.internal;

import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.phase.LogEnabled;
import org.xwiki.xwoot.manager.XWootManager;

public class DefaultXWootManager extends AbstractLogEnabled implements XWootManager, LogEnabled, Initializable
{
    /** The base address of the XWoot server. Should be configurable... */
    private String wootAddress = "http://localhost:8080/xwootApp";

    /** A HTTP client used to communicate with XWoot. */
    private HttpClient client;

    /**
     * Initializes the HTTP client utility. Called by the component manager when this component is instantiated.
     * 
     * @see Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connectionManager);
        client.getParams().setSoTimeout(2000);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, true));
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#isAvailable()
     */
    public boolean isAvailable()
    {
        String result = call("/information?request=isXWootInitialized");
        return !"failed".equals(result);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#getBootstrapURL()
     */
    public String getBootstrapURL()
    {
        return wootAddress;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#isInitialised()
     */
    public boolean isInitialised()
    {
        String result = call("/information?request=isXWootInitialized");
        return result.indexOf("true") >= 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#isP2PConnected()
     */
    public boolean isP2PConnected()
    {
        String result = call("/information?request=isP2PNetworkConnected");
        return result.indexOf("true") >= 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#connectP2P()
     */
    public void connectP2P()
    {
        call("/synchronize.do?action=p2pnetworkconnection&switch=on");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#disconnectP2P()
     */
    public void disconnectP2P()
    {
        call("/synchronize.do?action=p2pnetworkconnection&switch=off");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#isWikiConnected()
     */
    public boolean isWikiConnected()
    {
        String result = call("/information?request=isWikiConnected");
        return result.indexOf("true") >= 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#connectWiki()
     */
    public void connectWiki()
    {
        call("/synchronize.do?action=cpconnection&switch=on");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#disconnectWiki()
     */
    public void disconnectWiki()
    {
        call("/synchronize.do?action=cpconnection&switch=off");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#isDocumentManaged(String)
     */
    public boolean isDocumentManaged(String documentName)
    {
        String result = call("/information?request=isDocumentManaged&document=" + documentName);
        return result.indexOf("true") >= 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#manageDocument(String)
     */
    public void manageDocument(String documentName)
    {
        call("/pageManagement.do?action=addPage&document=" + documentName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#unmanageDocument(String)
     */
    public void unmanageDocument(String documentName)
    {
        call("/pageManagement.do?action=removePage&document=" + documentName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#listPeers()
     */
    public List<String> listPeers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#synchronize()
     */
    public void synchronize()
    {
        call("/synchronize.do?action=synchronize");
    }

    private String call(String service)
    {
        HttpMethod method = new GetMethod(wootAddress + service);
        try {
            getLogger().debug("Requesting: " + method.getURI());
            if (client.executeMethod(method) < 400) {
                String result = method.getResponseBodyAsString();
                getLogger().debug("Result: " + result);
                return result;
            }
            getLogger().info("Failed call: " + method.getStatusLine());
        } catch (Exception ex) {
            getLogger().warn("Exception occured while calling [" + service + "] on [" + wootAddress + "]", ex);
        } finally {
            // Release the connection, since HTTPClient reuses connections for improved performance
            method.releaseConnection();
        }
        return "failed";
    }
}
