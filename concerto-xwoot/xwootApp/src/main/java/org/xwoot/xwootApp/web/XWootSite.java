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

package org.xwoot.xwootApp.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.jxta.exception.JxtaException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.XWootContentProviderException;
import org.xwoot.XWootContentProviderFactory;
import org.xwoot.XWootContentProviderInterface;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.antiEntropy.AntiEntropyException;
import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;
import org.xwoot.jxta.Peer;
import org.xwoot.jxta.PeerFactory;
import org.xwoot.jxta.NetworkManager.ConfigMode;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.WootEngineException;
import org.xwoot.xwootApp.AutoSynchronizationThread;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootUtil.FileUtil;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWootSite
{
    /** Logging helper object. */
    private static final Log LOG = LogFactory.getLog(XWootSite.class);

    // singleton instance
    private static XWootSite instance = new XWootSite();

    private boolean started = false;

    private XWootAPI XWootEngine;

    public static final String XWIKI_PROPERTIES_FILENAME = "xwiki.properties";

    public static final String XWOOT_PROPERTIES_FILENAME = "xwoot.properties";

    public static final String CONTENT_MANAGER_PROPERTIES_FILENAME = "xwoot-content-provider.properties";

    public static final String XWIKI_ENDPOINT = "xwiki_endpoint";

    public static final String XWIKI_USERNAME = "xwiki_username";

    public static final String XWIKI_PASSWORD = "xwiki_password";

    public static final String XWOOT_WORKING_DIR = "xwoot_working_dir";

    public static final String XWOOT_SITE_ID = "xwoot_site_id";

    public static final String XWOOT_SERVER_URL = "xwoot_server_url";

    public static final String XWOOT_SERVER_NAME = "xwoot_server_name";

    public static final String XWOOT_REFRESH_LOG_DELAY = "xwoot_refresh_log_delay";

    public static final String XWOOT_NEIGHBORS_LIST_SIZE = "xwoot_neighbors_list_size";

    public static final String XWOOT_PBCAST_ROUND = "xwoot_pbcast_round";

    private static final String WOOT_CLOCK_DIR_NAME = "wootclock";

    private static final String JXTA_DIR_NAME = "jxta";

    private static final String WOOTENGINE_DIR_NAME = "wootEngine";

    private static final String TRE_DIR_NAME = "tre";

    private static final String AE_DIR_NAME = "antientropy";

    private static final String XWOOT_DIR_NAME = "xwoot";

    private static final String CONTENT_PROVIDER_DIR_NAME = "contentProvider";

    private AutoSynchronizationThread autoSynchronizationThread;

    // FIXME: 60 seconds for now. Read this from a properties file.
    private static final int AUTO_SYNCHRONIZE_INTERVAL = 60000;

    /** @return the singleton instance. */
    public static XWootSite getInstance()
    {
        return instance;
    }

    public Properties getProperties(String path)
    {
        Properties p = new Properties();
        try {
            FileInputStream fis = new FileInputStream(path);
            p.load(fis);
            fis.close();
        } catch (IOException ex) {
            // Cannot load properties, return empty properties.
        }
        return p;
    }

    /** @return the XWoot engine managed by this XWoot site. */
    public XWootAPI getXWootEngine()
    {
        return this.XWootEngine;
    }

    public AutoSynchronizationThread getAutoSynchronizationThread()
    {
        return this.autoSynchronizationThread;
    }

    /**
     * @param siteName
     * @param workingDirPath
     * @param contentProviderXmlRpcUrl
     * @param contentProviderLogin
     * @param contentProviderPassword
     * @param contenProviderPropertiesFilePath
     * @throws RuntimeException
     * @throws ClockException
     * @throws WikiContentManagerException
     * @throws WootEngineException
     * @throws JxtaException
     * @throws AntiEntropyException
     * @throws XWootException
     * @throws ThomasRuleEngineException
     * @throws XWootContentProviderException
     */
    public void init(String siteName, String workingDirPath, String contentProviderXmlRpcUrl,
        String contentProviderLogin, String contentProviderPassword, String contenProviderPropertiesFilePath)
        throws RuntimeException, ClockException, WootEngineException, JxtaException, AntiEntropyException,
        XWootException, ThomasRuleEngineException, XWootContentProviderException
    {
        // Module directories.
        File jxtaDir = new File(workingDirPath, JXTA_DIR_NAME);
        File wootEngineDir = new File(workingDirPath, WOOTENGINE_DIR_NAME);
        File wootEngineClockDir = new File(workingDirPath, WOOT_CLOCK_DIR_NAME);
        File treDir = new File(workingDirPath, TRE_DIR_NAME);
        File aeDir = new File(workingDirPath, AE_DIR_NAME);
        File xwootDir = new File(workingDirPath, XWOOT_DIR_NAME);
        File contentProviderDir = new File(workingDirPath, CONTENT_PROVIDER_DIR_NAME);

        try {
            // Check and/or create the working dir.
            FileUtil.checkDirectoryPath(workingDirPath);

            // Do the same for all the components.
            FileUtil.checkDirectoryPath(jxtaDir.toString());
            FileUtil.checkDirectoryPath(wootEngineDir.toString());
            FileUtil.checkDirectoryPath(wootEngineClockDir.toString());
            FileUtil.checkDirectoryPath(treDir.toString());
            FileUtil.checkDirectoryPath(aeDir.toString());
            FileUtil.checkDirectoryPath(xwootDir.toString());
        } catch (Exception e) {
            throw new RuntimeException("The provided working directory is not usable.", e);
        }

        // Init modules.
        Clock wootEngineClock = new Clock(wootEngineClockDir.toString());

        AntiEntropy ae = new AntiEntropy(aeDir.toString());

        Peer peer = PeerFactory.createPeer();
        // FIXME: Use a properties file or something similar to store the current group, it's password, the keystore
        // password in order to automatically start communicating after a reboot.
        // FIXME: This behavior opens a possible security hole. The group's password is no longer protected by the
        // keystore. Find a solution.
        peer.configureNetwork(siteName, jxtaDir, ConfigMode.EDGE);
        String peerName = peer.getMyPeerName();
        String peerId = peer.getMyPeerID().getUniqueValue().toString();

        // TODO better properties management
        Properties contentProviderProperties = this.getProperties(contenProviderPropertiesFilePath);
        LOG.debug(contentProviderProperties);

        String dbLocation = new File(contentProviderDir, peerName).toString();
        XWootContentProviderInterface xwiki =
            XWootContentProviderFactory.getXWootContentProvider(contentProviderXmlRpcUrl, dbLocation, true,
                contentProviderProperties);
        // WikiContentManager wiki = WikiContentManagerFactory.getSwizzleFactory().createWCM(url, login, pwd);

        // FIXME: use peerId for wootEngine and for TreEngine as well.
        WootEngine wootEngine = new WootEngine(peerId, wootEngineDir.toString(), wootEngineClock);

        ThomasRuleEngine tre = new ThomasRuleEngine(peerId, treDir.toString());

        this.XWootEngine = new XWoot3(xwiki, wootEngine, peer, xwootDir.toString(), tre, ae);

        // FIXME: read the interval from the properties file.
        this.autoSynchronizationThread = new AutoSynchronizationThread(this.XWootEngine, AUTO_SYNCHRONIZE_INTERVAL);

        // Mark as started.
        this.started = true;
        LOG.debug("Site " + this.XWootEngine.getXWootPeerId() + " initialisation");
    }

    /** @return true if this instance is initialized. */
    public boolean isStarted()
    {
        return this.started;
    }

    public void savePropertiesInFile(String path, String comments, Properties p) throws IOException
    {
        File f = new File(path);
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        p.store(fos, comments);
        fos.flush();
        fos.close();
    }

    public String updatePropertiesFiles(HttpServletRequest request, String xwikiPropertiesPath,
        String xwootPropertiesPath) throws IOException
    {
        String result = "";
        Properties properties;

        // Update XWiki connection properties.
        properties = updateXWikiPropertiesFromRequest(request, xwikiPropertiesPath);
        result += this.validateXWikiProperties(properties);
        if (result.equals("")) {
            this.savePropertiesInFile(xwikiPropertiesPath, " -- XWiki XML-RPC connection properties --", properties);
        }

        // Update XWoot properties.
        properties = updateXWootPropertiesFromRequest(request, xwootPropertiesPath);
        result += this.validateXWootProperties(properties);
        if (result.equals("")) {
            this.savePropertiesInFile(xwootPropertiesPath, " -- XWoot properties --", properties);
        }
        return result;
    }

    public Properties updateXWikiPropertiesFromRequest(ServletRequest request, String xwikiPropertiesPath)
    {
        Properties p = getProperties(xwikiPropertiesPath);
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWIKI_ENDPOINT))) {
            p.put(XWootSite.XWIKI_ENDPOINT, request.getParameter(XWootSite.XWIKI_ENDPOINT));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWIKI_USERNAME))) {
            p.put(XWootSite.XWIKI_USERNAME, request.getParameter(XWootSite.XWIKI_USERNAME));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWIKI_PASSWORD))) {
            p.put(XWootSite.XWIKI_PASSWORD, request.getParameter(XWootSite.XWIKI_PASSWORD));
        }
        return p;
    }

    public Properties updateXWootPropertiesFromRequest(ServletRequest request, String xwootPropertiesPath)
    {
        Properties p = getProperties(xwootPropertiesPath);
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_WORKING_DIR))) {
            p.put(XWootSite.XWOOT_WORKING_DIR, request.getParameter(XWootSite.XWOOT_WORKING_DIR));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_SITE_ID))) {
            p.put(XWootSite.XWOOT_SITE_ID, request.getParameter(XWootSite.XWOOT_SITE_ID));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_SERVER_URL))) {
            p.put(XWootSite.XWOOT_SERVER_URL, request.getParameter(XWootSite.XWOOT_SERVER_URL));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_SERVER_NAME))) {
            p.put(XWootSite.XWOOT_SERVER_NAME, request.getParameter(XWootSite.XWOOT_SERVER_NAME));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_REFRESH_LOG_DELAY))) {
            p.put(XWootSite.XWOOT_REFRESH_LOG_DELAY, request.getParameter(XWootSite.XWOOT_REFRESH_LOG_DELAY));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE))) {
            p.put(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE, request.getParameter(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE));
        }
        if (!StringUtils.isEmpty(request.getParameter(XWootSite.XWOOT_PBCAST_ROUND))) {
            p.put(XWootSite.XWOOT_PBCAST_ROUND, request.getParameter(XWootSite.XWOOT_PBCAST_ROUND));
        }
        return p;
    }

    /**
     * Checks that the XWiki connection configuration is good: the connection URL is a valid URL, and the username and
     * password are provided.
     * 
     * @param properties The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></strong> is returned.
     * @todo Message localization.
     * @todo Make a simple call to the wiki to verify that there is a wiki at that address, and that the
     *       username/password are valid.
     */
    private final String validateXWikiProperties(Properties properties)
    {
        String result = "";

        // Check that the XWiki endpoint is a valid URL.
        if (properties.get(XWootSite.XWIKI_ENDPOINT) == null) {
            result += "Please enter a non-empty XWiki endpoint URL.\n";
        } else {
            try {
                new URL((String) properties.get(XWootSite.XWIKI_ENDPOINT));
            } catch (MalformedURLException e) {
                result += "Please enter a valid XWiki endpoint URL (the given URL is malformed)\n";
            }

        }

        // Check that the username and password are provided.
        if (properties.get(XWootSite.XWIKI_USERNAME) == null) {
            result += "Please enter a non-empty username.\n";
        }

        if (properties.get(XWootSite.XWIKI_PASSWORD) == null) {
            result += "Please enter a non-empty password.\n";
        }

        return result;
    }

    /**
     * Checks that the XWoot configuration is good.
     * 
     * @param properties The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></string> is returned.
     * @todo Message localization.
     */
    private String validateXWootProperties(Properties properties)
    {
        String result = "";

        // Check that the directory for storing data is valid and writable.
        if (properties.get(XWootSite.XWOOT_WORKING_DIR) == null) {
            result += "Please enter a non-empty " + XWootSite.XWOOT_WORKING_DIR + " field.\n";
        } else {
            try {
                File f = new File((String) properties.get(XWootSite.XWOOT_WORKING_DIR));
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        result +=
                            "The provided directory does not exist and cannot be created. Please enter a writable serialization folder.\n";
                    }
                } else if (!f.canRead() || !f.canWrite()) {
                    result += "Please enter a writable serialization folder.\n";
                }
            } catch (Exception ex) {
                result += "The provided directory cannot be accessed. Please enter a writable serialization folder.\n";
            }
        }

        // Check that the site ID is a valid positive integer.
        if (properties.get(XWootSite.XWOOT_SITE_ID) == null) {
            result += "Please enter a non-empty ID.\n";
        } else {
            try {
                int i = Integer.parseInt((String) properties.get(XWootSite.XWOOT_SITE_ID));
                if (i <= 0) {
                    result += "Please enter a positive integer for the XWoot ID.\n";
                }

            } catch (NumberFormatException e) {
                result += "Please enter a valid XWoot ID (positive integer value).\n";
            }
        }

        // Check that the XWoot URL is valid.
        if (properties.get(XWootSite.XWOOT_SERVER_URL) == null) {
            result += "Please enter a non-empty XWoot address.\n";
        } else {
            try {
                new URL((String) properties.get(XWootSite.XWOOT_SERVER_URL));
            } catch (MalformedURLException e) {
                result += "Please enter a valid XWoot address (the given URL is malformed)\n";
            }

        }

        // Check the server name
        if (properties.get(XWootSite.XWOOT_SERVER_NAME) == null) {
            result += "Please enter a non-empty server name.\n";
        }

        // Check the refresh period.
        if (properties.get(XWootSite.XWOOT_REFRESH_LOG_DELAY) == null) {
            result += "Please enter a non-empty " + XWootSite.XWOOT_REFRESH_LOG_DELAY + " field.\n";
        } else {
            try {
                int i = Integer.parseInt((String) properties.get(XWootSite.XWOOT_REFRESH_LOG_DELAY));
                if (i <= 0) {
                    result += "Please enter a positive integer for the refresh period.\n";
                }
            } catch (NumberFormatException e) {
                result += "Please enter a valid refresh period (positive integer value).\n";
            }
        }

        // Check the neighbor list.
        if (properties.get(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE) == null) {
            result += "Please enter a non-empty neighbor list size.\n";
        } else {
            try {
                int i = Integer.parseInt((String) properties.get(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE));
                if (i <= 0) {
                    result += "Please enter a positive integer value for the neighbor list size.\n";
                }
            } catch (NumberFormatException e) {
                result += "Please enter a valid neighbor list size (positive integer value).\n";
            }
        }

        // Check the number of broadcasting rounds.
        if (properties.get(XWootSite.XWOOT_PBCAST_ROUND) == null) {
            result += "Please enter a non-empty number of propagation rounds.\n";
        } else {
            try {
                int i = Integer.parseInt((String) properties.get(XWootSite.XWOOT_PBCAST_ROUND));
                if (i <= 0) {
                    result += "Please enter a positive integer value for propagation rounds.\n";
                }
            } catch (NumberFormatException e) {
                result += "Please enter a valid number of propagation rounds (positive integer value).\n";
            }
        }

        return result;
    }
}
