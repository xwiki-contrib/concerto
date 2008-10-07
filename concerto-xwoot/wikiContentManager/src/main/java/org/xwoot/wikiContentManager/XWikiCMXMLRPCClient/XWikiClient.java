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

package org.xwoot.wikiContentManager.XWikiCMXMLRPCClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.List;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWikiClient
{
    private static final String SEPARATECHAR = ".";

    private String server;

    private XmlRpcClient client;

    private Properties properties;

    /**
     * Creates a new XWikiClient object.
     * 
     * @param path DOCUMENT ME!
     */
    public XWikiClient(String path)
    {
        this.loadProperties(path);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    public void connect() throws Exception
    {
        if (this.client != null) {
            return;
        }

        // XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        if (this.getProperties() != null) {
            this.setServer(this.getProperties().getProperty("xwiki.endpoint"));
        } else {
            return;
        }

        XmlRpcClientConfigImpl clientConfig = new XmlRpcClientConfigImpl();
        clientConfig.setServerURL(new URL("http://" + this.getServer() + "/xmlrpc/confluence"));

        this.client = new XmlRpcClient();
        this.client.setConfig(clientConfig);

        // config.setServerURL(new URL("http://" + getServer() +
        // "/xmlrpc/confluence"));

        // this.client.setConfig(config);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @param pageId DOCUMENT ME!
     * @param token DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> createPage(String space, String pageId, String content, String token)
    {
        System.out.println("createPage " + space + XWikiClient.SEPARATECHAR + pageId);

        if (!this.existPage(token, space + XWikiClient.SEPARATECHAR + pageId)) {
            Map<String, String> page = new Hashtable<String, String>();
            page.put("space", space);
            page.put("title", pageId);
            page.put("content", content);

            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(page);

            Map<String, String> result;
            try {
                result = (Map<String, String>) this.rpcCall("storePage", params);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return result;
        }

        return null;

        // System.out.println( "Successfully created page: " + page);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @param pageId DOCUMENT ME!
     * @param token DOCUMENT ME!
     * @param title DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, Object> createPage2(String space, String pageId, String token, String title) throws Exception
    {
        System.out.println("createPage " + space + XWikiClient.SEPARATECHAR + pageId);

        if (!this.existPage(token, space + XWikiClient.SEPARATECHAR + pageId)) {
            Map<String, Object> page = new Hashtable<String, Object>();
            page.put("space", space);
            page.put("title", title);
            page.put("content", "");

            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(page);

            Map<String, Object> result = (Map<String, Object>) this.rpcCall("storePage", params);

            return result;
        }

        return null;

        // System.out.println( "Successfully created page: " + page);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param spaceName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, Object> createSpace(String token, String spaceName)
    {
        if (!this.existSpace(token, spaceName)) {
            Map<String, Object> space = new Hashtable<String, Object>();
            space.put("name", spaceName);
            space.put("key", spaceName);
            space.put("description", spaceName);

            List<Object> params = new ArrayList<Object>();

            params.add(token);
            params.add(space);

            Map<String, Object> result;
            try {
                result = (Map<String, Object>) this.rpcCall("addSpace", params);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            System.out.println("Create space : " + spaceName);

            return result;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @param newContent DOCUMENT ME!
     * @param token DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> editPage(Map<String, String> page, String newContent, String token)
    {
        Integer version = Integer.valueOf(page.get("version"));
        Integer newVersion = Integer.valueOf(version.intValue() + 1);

        Map<String, String> editedPage = new Hashtable<String, String>();

        editedPage.put("id", page.get("id"));
        editedPage.put("space", page.get("space"));
        editedPage.put("title", "Yogourt");
        editedPage.put("content", page.get("content") + newContent);
        editedPage.put("version", newVersion.toString());

        List<Object> params = new ArrayList<Object>();
        params.add(token);
        params.add(editedPage);

        try {
            return (Map<String, String>) this.rpcCall("storePage", params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean existPage(String token, String pageId)
    {
        List<Object> params = new ArrayList<Object>();
        params.add(token);
        params.add(pageId);

        try {
            this.rpcCall("getPage", params);
        } catch (Exception e) {
            System.out.println(e + "=>catched : the page " + pageId + " don't exist !");

            return false;
        }

        return true;
    }

    private boolean existSpace(String token, String spaceName)
    {
        ArrayList<String> temp = this.getListSpaceId(token);

        return temp.contains(spaceName);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param pageId DOCUMENT ME!
     * @param field DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Object getFieldValue(String token, String pageId, String field) throws Exception
    {
        if (this.existPage(token, pageId)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(pageId);

            Map<String, Object> page = (Map<String, Object>) this.rpcCall("getPage", params);

            return page.get(field);
        }

        throw new Exception("Page doesn't exist");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param spaceName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    public ArrayList getListPageId(String token, String spaceName)
    {
        if (this.existSpace(token, spaceName)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(spaceName);

            List<Hashtable> pages;
            try {
                pages = (List<Hashtable>) this.rpcCall("getPages", params);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            ArrayList result = new ArrayList();

            for (int i = 0; i < pages.size(); i++) {
                result.add(((Map<String, Object>) pages.get(i)).get("id"));
            }

            return result;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param spaceName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    public ArrayList getListPageName(String token, String spaceName) throws Exception
    {
        if (this.existSpace(token, spaceName)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(spaceName);

            List<Hashtable> pages = (List<Hashtable>) this.rpcCall("getPages", params);

            ArrayList result = new ArrayList();

            for (int i = 0; i < pages.size(); i++) {
                result.add(((Map<String, Object>) pages.get(i)).get("title"));
            }

            return result;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    public ArrayList getListSpaceId(String token)
    {
        List<Object> params = new ArrayList<Object>();
        params.add(token);

        List<Hashtable> spaces;
        try {
            spaces = (List<Hashtable>) this.rpcCall("getSpaces", params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        ArrayList result = new ArrayList();

        for (int i = 0; i < spaces.size(); i++) {
            result.add(((Map<String, Object>) spaces.get(i)).get("key"));
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getPage(String token, String pageId)
    {
        if (this.existPage(token, pageId)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(pageId);

            Map<String, String> page;
            try {
                page = (Map<String, String>) this.rpcCall("getPage", params);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return page;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Properties getProperties()
    {
        return this.properties;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getServer()
    {
        return this.server;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param spaceName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map getSpace(String token, String spaceName) throws Exception
    {
        if (this.existSpace(token, spaceName)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(spaceName);

            return (Map) this.rpcCall("getSpace", params);
        }

        return null;
    }

    private void loadProperties(String path)
    {
        if (this.properties == null) {
            this.properties = new Properties();

            try {
                File file = new File(path);
                System.out.println(file);
                this.properties.load(new FileInputStream(file)); // "XWiki-XMLRPC-Example.properties"
                // ));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            return;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String login()
    {
        List<Object> params = new ArrayList<Object>();
        params.add(this.getProperties().getProperty("xwiki.username"));
        params.add(this.getProperties().getProperty("xwiki.password"));

        String token;
        try {
            token = (String) this.rpcCall("login", params);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        // if ( token != null )
        // System.out.println( "Successfully logged in: token = " + token );
        return token;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param username DOCUMENT ME!
     * @param password DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String login(String username, String password)
    {
        List<Object> params = new ArrayList<Object>();
        params.add(username);
        params.add(password);

        String token;
        try {
            token = (String) this.rpcCall("login", params);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        // if ( token != null )
        // System.out.println( "Successfully logged in: token = " + token );

    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void logout(String token)
    {
        List<Object> params = new ArrayList<Object>();
        params.add(token);

        try {
            this.rpcCall("logout", params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println( "Successfully logged out");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean removePage(String token, String pageId)
    {
        if (this.existPage(token, pageId)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(pageId);

            try {
                return ((Boolean) this.rpcCall("removePage", params)).booleanValue();
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param token DOCUMENT ME!
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean removeSpace(String token, String spaceId)
    {
        if (this.existSpace(token, spaceId)) {
            List<Object> params = new ArrayList<Object>();
            params.add(token);
            params.add(spaceId);

            try {
                return ((Boolean) this.rpcCall("removeSpace", params)).booleanValue();
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    protected Object rpcCall(String rpc, List<Object> params) throws Exception
    {
        this.connect();

        return this.client.execute("confluence1." + rpc, params);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param server DOCUMENT ME!
     */
    public void setServer(String server)
    {
        this.server = server;
    }
}
