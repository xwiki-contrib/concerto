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

import org.apache.commons.lang.StringUtils;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.antiEntropy.AntiEntropyException;
import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;

import org.xwoot.lpbcast.sender.httpservletlpbcast.HttpServletLpbCast;
import org.xwoot.lpbcast.sender.httpservletlpbcast.HttpServletLpbCastException;

import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;

import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.WootEngineException;

import org.xwoot.xwootApp.XWoot;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWootSite
{
    // singleton instance
    private static XWootSite instance;

    private boolean started = false;

    private XWootAPI xWootEngine;

    public static final String XWIKI_PROPERTIES_FILENAME = "xwiki.properties";

    public static final String XWOOT_PROPERTIES_FILENAME = "xwoot.properties";

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

    private static final String PBCAST_DIR_NAME = "pbCast";

    private static final String WOOTENGINE_DIR_NAME = "wootEngine";

    private static final String TRE_DIR_NAME = "tre";

    private static final String AE_DIR_NAME = "antientropy";

    private static final String XWOOT_DIR_NAME = "xwoot";

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static synchronized XWootSite getInstance()
    {
        if (XWootSite.instance == null) {
            XWootSite.instance = new XWootSite();
        }

        return XWootSite.instance;
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

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public XWootAPI getXWootEngine()
    {
        return this.xWootEngine;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param siteId DOCUMENT ME!
     * @param peerId DOCUMENT ME!
     * @param wikiPropertiesPath DOCUMENT ME!
     * @param workingDirPath DOCUMENT ME!
     * @param messagesRound DOCUMENT ME!
     * @param maxNeighbors DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     * @throws ClockException 
     * @throws WikiContentManagerException 
     * @throws WootEngineException 
     * @throws HttpServletLpbCastException 
     * @throws AntiEntropyException 
     * @throws XWootException 
     * @throws ThomasRuleEngineException 
     */
    public void init(int siteId, String peerId, String workingDirPath, int messagesRound, /* int logDelay, */
    int maxNeighbors, String url, String login, String pwd) throws RuntimeException, ClockException, WikiContentManagerException, WootEngineException, HttpServletLpbCastException, AntiEntropyException, XWootException, ThomasRuleEngineException
    {

        File pbCastDir = new File(workingDirPath + File.separator + PBCAST_DIR_NAME);
        File wootEngineDir = new File(workingDirPath + File.separator + WOOTENGINE_DIR_NAME);
        File wootEngineClockDir = new File(workingDirPath + File.separator + WOOT_CLOCK_DIR_NAME);
        File treDir = new File(workingDirPath + File.separator + TRE_DIR_NAME);
        File aeDir = new File(workingDirPath + File.separator + AE_DIR_NAME);
        File xwootDir = new File(workingDirPath + File.separator + XWOOT_DIR_NAME);

        if (!wootEngineDir.exists() && !wootEngineDir.mkdir()) {
            throw new RuntimeException("Can't create wootEngine directory: " + wootEngineDir);
        }

        if (!wootEngineClockDir.exists() && !wootEngineClockDir.mkdir()) {
            throw new RuntimeException("Can't create wootEngine clocks directory: " + wootEngineClockDir);
        }

        if (!pbCastDir.exists() && !pbCastDir.mkdir()) {
            throw new RuntimeException("Can't create pbCast directory: " + pbCastDir);
        }

        if (!treDir.exists() && !treDir.mkdir()) {
            throw new RuntimeException("Can't create tre directory: " + treDir);
        }

        if (!aeDir.exists() && !aeDir.mkdir()) {
            throw new RuntimeException("Can't create tre directory: " + aeDir);
        }

        if (!xwootDir.exists() && !xwootDir.mkdir()) {
            throw new RuntimeException("Can't create tre directory: " + xwootDir);
        }

        Clock wootEngineClock = new Clock(wootEngineClockDir.toString());
        WikiContentManager wiki = WikiContentManagerFactory.getSwizzleFactory().createWCM(url, login, pwd);
        WootEngine wootEngine = new WootEngine(siteId, wootEngineDir.toString(), wootEngineClock);
        HttpServletLpbCast lpbCast =
            new HttpServletLpbCast(pbCastDir.toString(), messagesRound, maxNeighbors, Integer.valueOf(siteId));

        ThomasRuleEngine tre = new ThomasRuleEngine(siteId, treDir.toString());

        AntiEntropy ae = new AntiEntropy(aeDir.toString());
        this.xWootEngine =
            new XWoot(wiki, wootEngine, lpbCast, xwootDir.toString(), peerId, Integer.valueOf(siteId), tre, ae);

        this.started = true;
        System.out.println("Site " + this.xWootEngine.getXWootPeerId() + " initialisation");
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
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
        Properties p;

        // Update XWiki connection properties.
        p = updateXWikiPropertiesFromRequest(request, xwikiPropertiesPath);
        result += this.validateXWikiProperties(p);
        if (result.equals("")) {
            this.savePropertiesInFile(xwikiPropertiesPath, " -- XWiki XML-RPC connection properties --", p);
        }

        // Update XWoot properties.
        p = updateXWootPropertiesFromRequest(request, xwootPropertiesPath);
        result += this.validateXWootProperties(p);
        if (result.equals("")) {
            this.savePropertiesInFile(xwootPropertiesPath, " -- XWoot properties --", p);
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
     * @param p The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></strong> is returned.
     * @todo Message localization.
     * @todo Make a simple call to the wiki to verify that there is a wiki at that address, and that the
     *       username/password are valid.
     */
    private final String validateXWikiProperties(Properties p)
    {
        String result = "";

        // Check that the XWiki endpoint is a valid URL.
        if (p.get(XWootSite.XWIKI_ENDPOINT) == null) {
            result += "Please enter a non-empty XWiki endpoint URL.\n";
        } else {
            try {
                new URL((String) p.get(XWootSite.XWIKI_ENDPOINT));
            } catch (MalformedURLException e) {
                result += "Please enter a valid XWiki endpoint URL (the given URL is malformed)\n";
            }

        }

        // Check that the username and password are provided.
        if (p.get(XWootSite.XWIKI_USERNAME) == null) {
            result += "Please enter a non-empty username.\n";
        }

        if (p.get(XWootSite.XWIKI_PASSWORD) == null) {
            result += "Please enter a non-empty password.\n";
        }

        return result;
    }

    /**
     * Checks that the XWoot configuration is good.
     * 
     * @param p The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></strong> is returned.
     * @todo Message localization.
     */
    private String validateXWootProperties(Properties p)
    {
        String result = "";

        // Check that the directory for storing data is valid and writable.
        if (p.get(XWootSite.XWOOT_WORKING_DIR) == null) {
            result += "Please enter a non-empty " + XWootSite.XWOOT_WORKING_DIR + " field.\n";
        } else {
            try {
                File f = new File((String) p.get(XWootSite.XWOOT_WORKING_DIR));
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
        if (p.get(XWootSite.XWOOT_SITE_ID) == null) {
            result += "Please enter a non-empty ID.\n";
        } else {
            try {
                int i = Integer.parseInt((String) p.get(XWootSite.XWOOT_SITE_ID));
                if (i <= 0) {
                    result += "Please enter a positive integer for the XWoot ID.\n";
                }

            } catch (NumberFormatException e) {
                result += "Please enter a valid XWoot ID (positive integer value).\n";
            }
        }

        // Check that the XWoot URL is valid.
        if (p.get(XWootSite.XWOOT_SERVER_URL) == null) {
            result += "Please enter a non-empty XWoot address.\n";
        } else {
            try {
                new URL((String) p.get(XWootSite.XWOOT_SERVER_URL));
            } catch (MalformedURLException e) {
                result += "Please enter a valid XWoot address (the given URL is malformed)\n";
            }

        }

        // Check the server name
        if (p.get(XWootSite.XWOOT_SERVER_NAME) == null) {
            result += "Please enter a non-empty server name.\n";
        }

        // Check the refresh period.
        if (p.get(XWootSite.XWOOT_REFRESH_LOG_DELAY) == null) {
            result += "Please enter a non-empty " + XWootSite.XWOOT_REFRESH_LOG_DELAY + " field.\n";
        } else {
            try {
                int i = Integer.parseInt((String) p.get(XWootSite.XWOOT_REFRESH_LOG_DELAY));
                if (i <= 0) {
                    result += "Please enter a positive integer for the refresh period.\n";
                }
            } catch (NumberFormatException e) {
                result += "Please enter a valid refresh period (positive integer value).\n";
            }
        }

        // Check the neighbor list.
        if (p.get(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE) == null) {
            result += "Please enter a non-empty neighbor list size.\n";
        } else {
            try {
                int i = Integer.parseInt((String) p.get(XWootSite.XWOOT_NEIGHBORS_LIST_SIZE));
                if (i <= 0) {
                    result += "Please enter a positive integer value for the neighbor list size.\n";
                }
            } catch (NumberFormatException e) {
                result += "Please enter a valid neighbor list size (positive integer value).\n";
            }
        }

        // Check the number of broadcasting rounds.
        if (p.get(XWootSite.XWOOT_PBCAST_ROUND) == null) {
            result += "Please enter a non-empty number of propagation rounds.\n";
        } else {
            try {
                int i = Integer.parseInt((String) p.get(XWootSite.XWOOT_PBCAST_ROUND));
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
