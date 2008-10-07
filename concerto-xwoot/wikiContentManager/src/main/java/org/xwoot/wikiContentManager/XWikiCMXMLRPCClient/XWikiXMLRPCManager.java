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

import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWikiXMLRPCManager implements WikiContentManager
{
    private static String xWikiSeparateChar = ".";

    private static String xWootSeparateChar = "~~";

    /** DOCUMENT ME! */
    public static final String[] MDTAB =
        {"created", "parentId", "title", "modifier", "space", "id", "version", "modified", "creator", "homePage",
        "locks"}; // ,"url"};

    private XWikiClient client;

    private String username;

    private String password;

    private String permToken;

    /**
     * Creates a new XWikiXMLRPCManager object.
     * 
     * @param path DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public XWikiXMLRPCManager(String path) throws Exception
    {
        this.username = null;
        this.password = null;
        this.client = new XWikiClient(path);
        this.client.connect();
    }

    /**
     * Creates a new XWikiXMLRPCManager object.
     * 
     * @param path DOCUMENT ME!
     * @param username DOCUMENT ME!
     * @param password DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public XWikiXMLRPCManager(String path, String username, String password) throws Exception
    {
        this.username = username;
        this.password = password;
        this.client = new XWikiClient(path);
        this.client.connect();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param fieldId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void overwriteField(String pageId, String fieldId, String value) throws Exception
    {
        String token;
        token = this.loginAndGetToken();

        Map<String, String> page = this.client.getPage(token, this.getXWikiPageId(pageId));
        System.out.println(page);
        page.put(fieldId, value);

        try {
            this.client.editPage(page, "", token);
        } catch (Exception e) {
            System.out.println(e
                + "catched => little hack (xwiki diff bug when update) => remove and create page to update");
            this.client.removePage(token, this.getXWikiPageId(pageId));
            this.client.createPage(this.getSpaceNameWithPageId(pageId), this.getPageNameWithPageId(pageId), "", token);
            this.client.editPage(page, "", token);
        } finally {
            this.client.logout(token);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void overwritePageContent(String pageId, String value)
    {
        String token;
        token = this.loginAndGetToken();

        Map<String, String> page = this.client.getPage(token, this.getXWikiPageId(pageId));
        page.put("content", "");

        try {
            this.client.editPage(page, value, token);
        } catch (Exception e) {
            System.out.println(e
                + "catched => little hack (xwiki diff bug when update) => remove and create page to update");
            this.client.removePage(token, this.getXWikiPageId(pageId));
            this.client.createPage(this.getSpaceNameWithPageId(pageId), this.getPageNameWithPageId(pageId), value,
                token);
            this.client.editPage(page, value, token);
        } finally {
            this.client.logout(token);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> createPage(String pageId, String value)
    {
        String token = this.loginAndGetToken();
        this.createSpace(this.getSpaceNameWithPageId(pageId));

        Map<String, String> result =
            this.client.createPage(this.getSpaceNameWithPageId(pageId), this.getPageNameWithPageId(pageId), value,
                token);
        this.client.logout(token);

        return result;
    }

    public void createSpace(String spaceName)
    {
        String token = this.loginAndGetToken();
        this.client.createSpace(token, spaceName);
        this.client.logout(token);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public List<Map> getComments(String pageId)
    {
        throw new RuntimeException("Not yet implemented");
    }

    private byte[] getDigest(String p, String algo) throws NoSuchAlgorithmException
    {
        String page = "";

        if (p != null) {
            page = p;
        }

        MessageDigest md = MessageDigest.getInstance(algo);
        byte[] b = page.getBytes();
        md.update(b);

        return md.digest();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> getFields(String pageId)
    {
        String token = this.loginAndGetToken();
        Map<String, String> result = this.client.getPage(token, this.getXWikiPageId(pageId));

        if (result != null) {
            String resultS = result.get("content").toString();

            if ((resultS == null) || ((resultS.length() == 1) && (resultS.codePointAt(0) == 67))
                || (resultS.length() < 1)) {
                result.put("content", "");
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param field DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String getFieldValue(String pageId, String field) throws Exception
    {
        String token = this.loginAndGetToken();
        String result = (String) this.client.getFieldValue(token, this.getXWikiPageId(pageId), field);
        this.client.logout(token);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Collection getListPageId(String space)
    {
        ArrayList list = this.getListXWikiPageId(space);
        Collection<String> result = new ArrayList<String>();

        if (list == null) {
            return result;
        }

        Iterator i = list.iterator();

        while (i.hasNext()) {
            String currentPage = (String) i.next();
            result.add(this.getPageIdWithXWikiPageId(currentPage));
        }

        return result;
    }

    /* !! the space id are formated by xwiki (with an upercase in first letter) */
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public ArrayList getListSpaceId()
    {
        String token = this.loginAndGetToken();
        ArrayList result = this.client.getListSpaceId(token);
        this.client.logout(token);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public ArrayList getListXWikiPageId(String space)
    {
        String token = this.loginAndGetToken();
        ArrayList result = this.client.getListPageId(token, space);
        this.client.logout(token);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String getMD(String pageId) throws Exception
    {
        String token = this.loginAndGetToken();
        Map<String, String> result = this.client.getPage(token, this.getXWikiPageId(pageId));
        this.client.logout(token);

        return result.toString();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String[] getMDKeyTable()
    {
        return XWikiXMLRPCManager.MDTAB;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public String getPageContent(String pageId)
    {
        String token = this.loginAndGetToken();
        Map<String, String> result = this.client.getPage(token, this.getXWikiPageId(pageId));
        this.client.logout(token);

        if (result != null) {
            String resultS = result.get("content").toString();

            if ((resultS == null) || ((resultS.length() == 1) && (resultS.codePointAt(0) == 67))
                || (resultS.length() < 1)) {
                return "";
            }

            return resultS;
        }

        return "";
    }

    /**
     * DOCUMENT ME!
     * 
     * @param xWikiPageId DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public String getPageIdWithXWikiPageId(String xWikiPageId)
    {
        return xWikiPageId
            .replaceAll("\\" + XWikiXMLRPCManager.xWikiSeparateChar, XWikiXMLRPCManager.xWootSeparateChar);
    }

    private String getPageNameWithPageId(String pageId)
    {
        int l = pageId.lastIndexOf(XWikiXMLRPCManager.xWootSeparateChar);

        if (l == -1) {
            return pageId;
        }

        return pageId.substring(l + XWikiXMLRPCManager.xWootSeparateChar.length(), pageId.length());
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getSiteId()
    {
        return this.client.getServer().hashCode();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getSiteName()
    {
        return this.client.getServer();
    }

    private String getSpaceNameWithPageId(String pageId)
    {
        int l = pageId.lastIndexOf(XWikiXMLRPCManager.xWootSeparateChar);

        if (l == -1) {
            return "";
        }

        return pageId.substring(0, l);
    }

    public String getWikiURL()
    {

        if (this.client.getProperties() != null) {
            return this.client.getProperties().getProperty("xwiki.endpoint");
        }

        return "";

    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public String getXWikiPageId(String pageId)
    {
        return pageId.replaceAll(XWikiXMLRPCManager.xWootSeparateChar, "\\" + XWikiXMLRPCManager.xWikiSeparateChar);
    }

    private String loginAndGetToken()
    {
        if ((this.username != null) && (this.password != null)) {
            return this.client.login(this.username, this.password);
        }

        return this.client.login();
    }

    public void login()
    {
        this.permToken = this.loginAndGetToken();
    }

    public void logout()
    {
        this.client.logout(this.permToken);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param comment DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void overWriteComments(String pageId, List<Map> comment)
    {
        // TODO Auto-generated method stub
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean removePage(String pageId)
    {
        String token = this.loginAndGetToken();
        boolean result = this.client.removePage(token, this.getXWikiPageId(pageId));
        this.client.logout(token);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param comment DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public Map setComment(String pageId, Map comment)
    {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param fields DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public Map<String, String> setFields(String pageId, Map<String, String> fields)
    {
        String token = this.loginAndGetToken();
        Map<String, String> page = this.client.getPage(token, this.getXWikiPageId(pageId));

        if (page == null) {
            page = this.createPage(pageId, "");
        }

        Iterator i = fields.entrySet().iterator();

        while (i.hasNext()) {
            Entry<String, String> e = (Entry<String, String>) i.next();
            page.put(e.getKey(), e.getValue());
        }

        Map<String, String> result = this.client.editPage(page, "", token);
        this.client.logout(token);

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param metaDataId DOCUMENT ME!
     * @param content DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void setFieldValue(String pageId, String metaDataId, String content) throws Exception
    {
        String token = this.loginAndGetToken();
        Map<String, String> page = this.client.getPage(token, this.getXWikiPageId(pageId));

        if (page == null) {
            page = this.createPage(pageId, "");
        }

        page.put(metaDataId, content);
        this.client.editPage(page, "", token);
        this.client.logout(token);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param algo DOCUMENT ME!
     * @param rmd DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws NoSuchAlgorithmException
     * @throws Exception DOCUMENT ME!
     */
    public String setPageContent(String pageId, String value, String algo, byte[] rmd) throws NoSuchAlgorithmException
    {
        String token = this.loginAndGetToken();
        String result = null;
        Map<String, String> page = this.client.getPage(token, this.getXWikiPageId(pageId));

        if (page == null) {
            page = this.createPage(pageId, "");
        }

        byte[] messageDigest = this.getDigest(page.get("content"), algo);

        if (MessageDigest.isEqual(messageDigest, rmd)) {
            page.put("content", value);
            this.client.editPage(page, "", token);
        } else {
            String resultS = page.get("content");

            if ((resultS == null) || ((resultS.length() == 1) && (resultS.codePointAt(0) == 67))
                || (resultS.length() < 1)) {
                result = "";
            } else {
                result = resultS;
            }
        }

        this.client.logout(token);

        return result;
    }

    public void removeSpace(String spaceKey) throws WikiContentManagerException
    {
        String token = this.loginAndGetToken();
        this.client.removeSpace(token, spaceKey);
        this.client.logout(token);
    }
}
