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

package org.xwoot.wikiContentManager;

import java.security.NoSuchAlgorithmException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public interface WikiContentManager
{

    /**
     * Some page to String created with createPage : { created=Fri Jul 25 11:00:15 CEST 2008, parentId=, current=false,
     * title=2, modifier=XWiki.Admin, content=, minorVersion=1, language=, space=test, id=test.2, version=2,
     * modified=Fri Jul 25 11:00:51 CEST 2008, creator=XWiki.Admin, homePage=false,
     * url=http://concerto.loria.fr:8080/xwiki -enterprise-web-1.5/bin/view/test/2, translations=[Ljava.lang.Object;@1112783
     * }
     */

    /**
     * Some comment to String : { created=Fri Jul 25 11:00:52 CEST 2008, creator=XWiki.Admin, title=Comment 47,
     * content=New comment for test, url=http://concerto.loria.fr:8080/xwiki-enterprise-web-1.5/bin/view/test/
     * testComments, id=test.testComments?commentId=47, pageId=test.testComments }
     */
    
    /**
    * Some objectSummary to String : {prettyName=XWiki.XWikiRights[0], className=XWiki.XWikiRights, id=0, pageId=Scheduler.WatchListJob2}
    *{prettyName=XWiki.SchedulerJobClass[0], className=XWiki.SchedulerJobClass, id=0, pageId=Scheduler.WatchListJob2}
    **/
    
    /**
     * Some Object to String :
     * {prettyName=XWiki.XWikiRights[0], className=XWiki.XWikiRights, propertyToValueMap={groups=XWiki.XWikiAdminGroup, levels=edit,delete, allow=1}, 
     * id=0, pageId=Scheduler.WatchListJob2}
     * 
     * {prettyName=XWiki.SchedulerJobClass[0], className=XWiki.SchedulerJobClass,propertyToValueMap={contextDatabase=xwiki, 
     * cron=0 0 0 * * ?, contextUser=XWiki.Admin, script=2, contextLang=en, jobDescription=WatchList daily email watchlist job, 
     * status=Normal, jobClass=com.xpn.xwiki.plugin.watchlist.WatchListJob, jobName=WatchList daily notifications}, id=0, pageId=Scheduler.WatchListJob2}
     */

    enum COMMENTMDTABLE
    {
        content, id, pageId;
    }

    enum PAGEMDTABLE
    {
        creator, created, parentId, modifier, title, homePage, id, space;
    }

    /** To get a page */
    final static String PAGE = "page";

    /** To get a comment */
    final static String COMMENT = "comment";

    /** Some String : Hasta la vista, baby ! */
    final static String CONTENT = "content";

    /** Date Format : Fri Jul 25 11:00:15 CEST 2008 */
    final static String CREATED = "created";

    /** Some String : Terminator's dad */
    final static String PARENTID = "parentId";

    /** Some user : XWiki.Admin */
    final static String MODIFIER = "modifier";

    /** The page name : for test.page => page */
    final static String TITLE = "title";

    /** The page id : test.page */
    final static String ID = "id";

    /** Some user : XWiki.Admin */
    final static String CREATOR = "creator";

    /** The space name : for test.page =>test */
    final static String SPACE = "space";

    /** Some int : 2 */
    final static String VERSION = "version";

    /** Date Format : Fri Jul 25 11:00:15 CEST 2008 */
    final static String MODIFIED = "modified";

    /** Boolean : true/false */
    final static String HOMEPAGE = "homePage";

    /** DOCUMENT ME! Lost ?? */
    final static String LOCKS = "locks";

    /**
     * URL page : http://concerto.loria.fr:8080/xwiki-enterprise-web-1.5/bin/view/test/page
     */
    final static String URL = "url";

    /** DOCUMENT ME! Lost ?? */
    final static String CONTENTSTATUS = "contentStatus";

    /** The page id link to a comment : test.page */
    final static String PAGEID = "pageId";

    public void login() throws WikiContentManagerException;

    public void logout() throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param name DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    void overwritePageContent(String name, String value) throws WikiContentManagerException;

    // String[] getMDKeyTable();
    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    Map<String, String> createPage(String pageName, String content) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    List<Map> getComments(String pageId) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    Map<String, String> getFields(String pageId) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    Collection getListPageId(String space) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws SwizzleException
     * @throws Exception DOCUMENT ME!
     */
    Collection getListSpaceId() throws WikiContentManagerException;

    /* creator,space,id,version,modified,homepage,locks,url,contentStatus}; */
    /**
     * DOCUMENT ME!
     * 
     * @param name DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    String getPageContent(String name) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param Name DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    String getWikiURL();

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param comment DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    void overWriteComments(String pageId, List<Map> comment) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    boolean removePage(String pageName) throws WikiContentManagerException;

    void removeSpace(String spaceKey) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param comment DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    Map setComment(String pageId, Map comment) throws WikiContentManagerException;

    // void _setField(String pageId, String fieldId,String value) throws
    // Exception;
    /**
     * DOCUMENT ME!
     * 
     * @param pageId DOCUMENT ME!
     * @param fields DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    Map<String, String> setFields(String pageId, Map<String, String> fields) throws WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param name DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param algo DOCUMENT ME!
     * @param rmd DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws NoSuchAlgorithmException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    String setPageContent(String name, String value, String algo, byte[] rmd) throws NoSuchAlgorithmException,
        WikiContentManagerException;

    /**
     * DOCUMENT ME!
     * 
     * @param spaceKey DOCUMENT ME!
     * @throws Exception
     */
    public void createSpace(String spaceKey) throws WikiContentManagerException;
}
