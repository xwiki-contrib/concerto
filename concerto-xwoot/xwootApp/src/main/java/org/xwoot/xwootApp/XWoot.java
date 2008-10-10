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

package org.xwoot.xwootApp;

//Harg ! Coupling between patch and XWoot ...
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.zip.ZipFile;

import jlibdiff.Diff;
import jlibdiff.Hunk;
import jlibdiff.HunkAdd;
import jlibdiff.HunkChange;
import jlibdiff.HunkDel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.antiEntropy.AntiEntropyException;
import org.xwoot.clockEngine.ClockException;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.sender.LpbCastAPI;
import org.xwoot.lpbcast.sender.SenderException;
import org.xwoot.lpbcast.sender.httpservletlpbcast.HttpServletLpbCast;
import org.xwoot.lpbcast.util.NetUtil;

import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;

import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManager.PAGEMDTABLE;

import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.WootEngineException;
import org.xwoot.wootEngine.core.WootPage;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootApp.core.XWootPage;
import org.xwoot.xwootApp.core.tre.CommentsValue;
import org.xwoot.xwootApp.core.tre.MDIdentifier;
import org.xwoot.xwootApp.core.tre.MDValue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWoot implements XWootAPI
{

    private WikiContentManager contentManager;

    private WootEngine wootEngine;

    private LpbCastAPI sender;

    private ThomasRuleEngine tre;

    private String lastVuePagesDir;

    private Integer siteId;

    private AntiEntropy antiEntropy;

    private final Log logger = LogFactory.getLog(this.getClass());

    private boolean p2Pconnected;

    private boolean contentManagerConnected;

    private String contentManagerURL;

    private String peerId;

    private String stateDir;

    public static final String STATEFILENAME = "state.zip";

    public static final String XWOOTSTATEFILENAME = "xwoot.zip";

    private static final String PAGELISTFILEFORSTATE = "list.txt";

    private String workingDir;

    /**
     * Creates a new XWoot object.
     * 
     * @param contentManager DOCUMENT ME!
     * @param wootEngine DOCUMENT ME!
     * @param sender DOCUMENT ME!
     * @param clock DOCUMENT ME!
     * @param siteUrl DOCUMENT ME!
     * @param siteId DOCUMENT ME!
     * @param tre DOCUMENT ME!
     * @param ae TODO
     * @param WORKINGDIR DOCUMENT ME!
     * @throws XWootException 
     */
    public XWoot(WikiContentManager contentManager, WootEngine wootEngine, LpbCastAPI sender, String workingDir,
        String peerId, Integer siteId, ThomasRuleEngine tre, AntiEntropy ae) throws XWootException 
        {

        this.workingDir = workingDir;
        this.lastVuePagesDir = workingDir + File.separator + "lastVuePages";
        this.stateDir = workingDir + File.separator + "stateDir";
        this.createWorkingDir();

        this.contentManager = contentManager;
        this.contentManagerURL = contentManager.getWikiURL();
        this.wootEngine = wootEngine;
        this.sender = sender;
        this.tre = tre;
        this.siteId = siteId;
        this.peerId = peerId;
        this.logger.info(this.siteId + " : AntiEntropy component created.");
        this.antiEntropy = ae;
        this.logger.info(this.siteId + " : XWoot engine created. XWoot working directory : " + workingDir + "\n\n");
        this.p2Pconnected = false;
        this.contentManagerConnected = false;
        if (this.sender.isSenderConnected()) {
            this.sender.disconnectSender();
        }
        }

    public void clearWorkingDir() throws XWootException
    {
        File f = new File(this.workingDir);
        System.out.println("=>" + this.workingDir);
        if (f.exists()) {
            this.logger.info(this.siteId + " Delete working dir xwoot : " + f.toString());
            FileUtil.deleteDirectory(f);
        }
        this.createWorkingDir();
    }

    private void createWorkingDir() throws XWootException
    {
        File working = new File(this.workingDir);

        if (!working.exists() && !working.mkdir()) {
            throw new XWootException("Can't create xwoot directory: " + working);
        }

        if (!working.isDirectory()) {
            throw new RuntimeException(working + " is not a directory");
        } else if (!working.canWrite()) {
            throw new XWootException("Can't write in directory: " + working);
        }

        File lastVuePages = new File(this.lastVuePagesDir);

        if (!lastVuePages.exists() && !lastVuePages.mkdir()) {
            throw new XWootException("Can't create pages directory: " + lastVuePages);
        }

        File stateDirFile = new File(this.stateDir);

        if (!stateDirFile.exists() && !stateDirFile.mkdir()) {
            throw new XWootException("Can't create pages directory: " + stateDirFile);
        }
    }

    private void overwriteCPPages(Collection list)
    {
        if (!this.isContentManagerConnected()) {
            return;
        }
        this.logger.info(this.siteId + " : Starting the overwrite of each managed pages");
        Iterator i = list.iterator();
        while (i.hasNext()) {
            // for each page
            String pageName = (String) i.next();

            this.logger.info(this.siteId + " : overwrite page : " + pageName + " ...");
            // overwrite content
            String wootContent=null;
            try {
                wootContent = this.getWootEngine().getPage(pageName);
            } catch (WootEngineException e) {
                this.logger.error("Problem when loading woot page -- "+pageName+"\n"+e);
            }

            if (wootContent!=null){
                try {
                    this.contentManager.overwritePageContent(pageName, wootContent);
                } catch (WikiContentManagerException e) {
                    this.logger.error("Problem when overwritting page content -- "+pageName+" \n"+e);
                }

                // overwrite fields
                Map<String, String> fields=null;
                try {
                    fields = this.contentManager.getFields(pageName);
                } catch (WikiContentManagerException e) {
                    this.logger.error("Problem when overwritting page MD  -- "+pageName+" \n"+e);
                }
                if (fields != null) {
                    for (PAGEMDTABLE pageMd : PAGEMDTABLE.values()) {
                        Value v=null;
                        try {
                            v = this.tre.getValue(new MDIdentifier(pageName, String.valueOf(pageMd)));
                        } catch (ThomasRuleEngineException e) {
                            this.logger.error("Problem when getting tre value for page : "+pageName+"\n"+e);
                        } 
                        if (v != null) {
                            fields.put(String.valueOf(pageMd),(String)v.get());
                        }
                    }
                    try {
                        this.contentManager.setFields(pageName, fields);
                    } catch (WikiContentManagerException e) {
                        this.logger.error("Problem when overwritting page fields -- "+pageName+" \n"+e);
                    }
                }

                // overwrite comments
                CommentsValue value=null;
                try {
                    value = (CommentsValue) this.tre.getValue(new MDIdentifier(pageName, WikiContentManager.COMMENT));
                } catch (ThomasRuleEngineException e) {
                    this.logger.error("Problem when getting tre comment for page : "+pageName+"\n"+e);
                } 
                if (value != null) {
                    List<Map> comments = (List<Map>) value.get();
                    try {
                        this.contentManager.overWriteComments(pageName, comments);
                    } catch (WikiContentManagerException e) {
                        this.logger.error("Problem when overwritting page comments -- "+pageName+" \n"+e);
                    }
                }
                this.logger.info(this.siteId + " : overwrite page : " + pageName + " OK");
            }
        }

        this.logger.info(this.siteId + " : Synchronising OK.");
    }

    // private void removeAllXWikiPages() throws WikiContentManagerException{
    // Collection spaces=this.xwiki.getListSpaceId();
    // Iterator i=spaces.iterator();
    // this.logger.info(this.siteId + "Remove all contentManager pages ...");
    // while (i.hasNext()) {
    // String space=(String)i.next();
    // if (!space.equals("XWiki")){
    // this.logger.info(this.siteId + "Remove space : "+space);
    // Collection pages=this.xwiki.getListPageId(space);
    // Iterator j=pages.iterator();
    // while(j.hasNext()){
    // String pageId=(String)j.next();
    // boolean b=this.xwiki.removePage(pageId);
    // this.logger.info(this.siteId + "Remove page : "+pageId+" "+b);
    // }
    // this.xwiki.removeSpace(space);
    // }
    // }
    // this.logger.info(this.siteId + "Remove all contentManager pages OK.");
    // }

    private void treatePatch(Patch patch)
    {

        this.logger.info(this.siteId + " : reception d'un patch -- " + patch.toString());

        try {
            String pageName = patch.getPageName();
            this.logger.info(this.siteId + " : for page : " + pageName);
            // apply receiving patch to woot engine
            this.getWootEngine().deliverPatch(patch);
            this.contentManager.login();
            // treate Content data

            this.treatePatch_PageContent(pageName);

            // treate MD

            this.treatePatch_PageMD(pageName, patch.getMDelements());
            this.contentManager.logout();
        } catch (Exception e) {
            this.logger.error(e.getMessage());
        }
    }

    private Patch sendNewPatch(List<WootOp> contentData, List<ThomasRuleOp> mDContentData, String pageName) throws XWootException 
    {
        // send the new patch
        Patch newPatch = this.getWootEngine().createPatch(contentData, mDContentData, pageName, this.siteId.intValue());
        Message message=null;
        message = this.sender.getNewMessage(this.getXWootPeerId(), newPatch, LpbCastAPI.LOG_AND_GOSSIP_OBJECT, this.sender
            .getRound()); 
        if (message!=null){
            try {
                this.sender.gossip(this.getXWootPeerId(), message);
                this.getAntiEntropy().logMessage(message.getId(), message);
            } catch (SenderException e) {
                throw new XWootException("Can't send new Message "+e);  
            } catch (AntiEntropyException e) {
                throw new XWootException("Can't send new Message "+e);    
            }
        }
        return newPatch;
    }

    private void treatePatch_PageContent(String pageName)
    {
        XWootPage page = null;

        try {
            this.logger.info(this.siteId + " : apply content modif");

            // get the concerning page
            page = new XWootPage(pageName, "");
            page.loadPage(this.lastVuePagesDir);

            // get the last vue of this page
            String lastVuePage = page.getContent();

            // get digest of last vue page
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(lastVuePage.getBytes());

            byte[] lastVuePageDigest = md.digest();

            // get the woot engine page
            String modelPage = this.getWootEngine().getPage(pageName);

            if (this.isContentManagerConnected()) {

                // set contentManager page
                // *************************************************//
                // critical access for execution time (XMLRPC call) //
                // *************************************************//
                String newPage = this.contentManager.setPageContent(pageName, modelPage, "MD5", lastVuePageDigest);

                // if contentManager page have change before patch synchronize
                // new contentManager
                // page
                if (newPage != null) {
                    this.logger.info(this.siteId + " : contentManager page != last vue page");
                    // get the patch of the synchronization between woot engine
                    // page
                    // and contentManager page
                    page.setContent(lastVuePage);
                    this.logger.debug(this.siteId + " : - lastVuePage : " + lastVuePage + "   newPage : " + newPage);

                    // make diff on woot page copy (last parameter = true) !!
                    List<WootOp> dataContent =
                        this.synchronizePageContent(pageName, lastVuePage, newPage, true);

                    // save the last vue page with the contentManager page
                    page.setContent(newPage);
                    this.getWootEngine().copyPage(pageName);
                    page.unloadPage(this.lastVuePagesDir);
                    // send the new patch

                    // **********************************************************
                    // *//
                    // critical access for execution time/memory (recursive
                    // call) //
                    // **********************************************************
                    // *//
                    Patch newPatch = this.sendNewPatch(dataContent, null, pageName);
                    this.wootEngine.deliverPatch(newPatch);
                    this.treatePatch_PageContent(pageName);

                } else {
                    this.logger.info(this.siteId + " : contentManager page have no change");
                    // save the last vue page with the woot engine page
                    page.setContent(modelPage);
                    this.getWootEngine().copyPage(pageName);
                    page.unloadPage(this.lastVuePagesDir);
                }
            }

        } catch (Exception e) {
            if (page != null) {
                try {
                    page.unloadPage(pageName);
                }  catch (XWootException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            this.logger.error(e.getMessage());
        }
    }

    private void treatePatch_PageMD(String pageName, List list) throws XWootException
    {
        this.logger.info(this.siteId + " : apply metaData modif");

        if ((list == null) || list.isEmpty() || !(list.get(0)!=null && list.get(0) instanceof ThomasRuleOp)) {
            return;
        }

        // Update TRE
        Map<String, String> fields = new HashMap<String, String>();
        List<Map> comments = null;
        Collection entries = new ArrayList<Entry>();
        for (int i = 0; i < list.size(); i++) {
            Entry e=null;
            try {
                e = this.tre.applyOp((ThomasRuleOp) list.get(i));
            } catch (ThomasRuleEngineException e1) {
                this.logger.error("Problem to apply op"+e1);
            } 
            if (e!=null){
                entries.add(e);
            }
        }

        // In case of contentManager is connected, update contentManager page
        if (this.isContentManagerConnected()) {
            try {
                fields = this.contentManager.getFields(pageName);
            } catch (WikiContentManagerException e1) {
                this.logger.error("Error when getting fields for page "+pageName+"\n"+e1);
            }

            if (fields == null) {
                try {
                    fields = this.contentManager.createPage(pageName, "");
                } catch (WikiContentManagerException e) {
                    this.logger.error("Problem with page : " + pageName + " -- can't get it, can't create it."+e);
                    throw new XWootException("Problem with page : " + pageName + " -- can't get it, can't create it."+e);
                }

            }

            this.logger.debug(this.siteId + " : fields of contentManager page : " + fields);

            Iterator i = entries.iterator();
            while (i.hasNext()) {
                Entry e = (Entry) i.next();

                if (e != null) {
                    Value val = e.getValue();
                    if (val != null) {
                        if (val instanceof MDValue) {
                            fields.put(((MDIdentifier) e.getId()).getMetaDataId(), (String)((MDValue) e.getValue()).get());
                            this.logger.debug(this.siteId + " : set field -- "
                                + ((MDIdentifier) e.getId()).getMetaDataId() + "," + ((MDValue) e.getValue()).get());
                        } else if (val instanceof CommentsValue) {
                            if (comments == null) {
                                comments = (List<Map>) ((CommentsValue) e.getValue()).get();
                            } else {
                                throw new XWootException("One comments op per page !");
                            }
                        } else {
                            throw new ClassCastException("Bad given type : " + val.getClass());
                        }
                    }
                }
            }

            try {
                this.contentManager.setFields(pageName, fields);
            } catch (WikiContentManagerException e) {
                this.logger.error("Problem when setting page "+pageName+"\n"+e);
            }

            if (comments != null) {
                try {
                    this.contentManager.overWriteComments(pageName, comments);
                } catch (WikiContentManagerException e) {
                    this.logger.error("Problem when overwritting comments "+pageName+"\n"+e);
                }
                this.logger.debug(this.siteId + " : set comments : " + comments);
            }
        }
    }

    private synchronized void synchronizePage(String pageName) throws XWootException 
    {
        if (!this.isContentManagerConnected()) {
            return;
        }
        this.logger.trace(this.siteId + " : Synchronising page : " + pageName);

        // get the woot engine content
        String wootContent=null;
        try {
            wootContent = this.getWootEngine().getPage(pageName);
        } catch (WootEngineException e) {
            this.logger.error("Can't synchronize page : "+pageName+"\n"+e);
            return;
        }

        // get the contentManager content

        Map<String, String> fields=null;
        try {
            fields = this.contentManager.getFields(pageName);
        } catch (WikiContentManagerException e) {
            this.logger.error("Can't synchronize page : "+pageName+"\n"+e);
            return;
        }
        String contentManagerContent = "";

        // get page content
        if (fields != null) {
            contentManagerContent = fields.get("content");
        }

        this.logger.debug(this.siteId + " : " + wootContent + " vs " + contentManagerContent);
        this.logger.debug(this.siteId + " : fields : " + fields);

        // synchronize wootengine with contentManager content
        List<WootOp> dataContent = this.synchronizePageContent(pageName, wootContent, contentManagerContent, false);

        // save the last vue contentManager page
        if (!dataContent.isEmpty()) {
            this.logger.debug(this.siteId + " : add page to model");

            XWootPage page = new XWootPage(pageName, "");
            page.setContent(contentManagerContent);
            page.unloadPage(this.lastVuePagesDir);
            try {
                this.getWootEngine().copyPage(pageName);
            } catch (WootEngineException e) {
                this.logger.error("Can't synchronize page : "+pageName+"\n"+e);
                return;
            } 
        }

        // synchronize MD contents
        List<ThomasRuleOp> mdOp = this.synchronizePageMD(pageName, fields);
        ThomasRuleOp o;
        try {
            o = this.synchronizePageComments(pageName, this.contentManager.getComments(pageName));
            if (o != null){
                mdOp.add(o);
            }

        } catch (WikiContentManagerException e) {
            this.logger.error("Problem when getting comments for page "+pageName+"\n"+e);
        }

        // CreatePatch if necessary
        if (!dataContent.isEmpty() || !mdOp.isEmpty()) {
            this.logger.debug(this.siteId + " : some change to send ; create patch");
            this.sendNewPatch(dataContent, mdOp, pageName);
        }

    }

    private synchronized ThomasRuleOp synchronizePageComments(String pageName, List<Map> comments)
    {
        CommentsValue value = null;
        Identifier id = new MDIdentifier(pageName, WikiContentManager.COMMENT);

        if (comments != null) {
            value = new CommentsValue(comments);
        }

        ThomasRuleOp op=null;
        try {
            op = this.tre.getOp(id, value);
            this.tre.applyOp(op);
        } catch (ThomasRuleEngineException e) {
            this.logger.error("Can't apply comment op ");
        } 


        if (op == null) {
            this.logger.info(this.siteId + " : no comment to synchronize\n\n");
        } else {
            this.logger.info(this.siteId + " : comments changed ; synchronizing");
        }

        return op;
    }

    private synchronized List<ThomasRuleOp> synchronizePageMD(String pageName, Map<String, String> fields)
    {

        List<ThomasRuleOp> result = new ArrayList<ThomasRuleOp>();
        String value = null;
        if (fields != null) {
            for (PAGEMDTABLE pageMd : PAGEMDTABLE.values()) {
                value = fields.get(pageMd.toString());
                if (value != null) {
                    ThomasRuleOp op=null;
                    try {
                        op = this.tre.getOp(new MDIdentifier(pageName, pageMd.toString()), new MDValue(value));
                        if (op != null) {
                            this.tre.applyOp(op);
                            result.add(op);
                        }
                    } catch (ThomasRuleEngineException e) {
                        this.logger.error(this.siteId + " : problem to get an op \n"+e);
                    } 
                }
            }
        }

        this.logger.info(this.siteId + " : " + result.size()
            + " operation(s) applicated to meta data model for page fields");

        return result;
    }

    private synchronized List<WootOp> synchronizePageContent(String pageName, String oldPage,
        String newPage, boolean inCopy) throws XWootException
        {
        BufferedReader oldContent = new BufferedReader(new StringReader(oldPage));
        BufferedReader newContent = new BufferedReader(new StringReader(newPage));

        Diff d = new Diff();
        try {
            d.diff(oldContent, newContent);
        } catch (IOException e) {
            this.logger.error(this.siteId + " : Problem with diff when synchronizing content"+e);
        }

        List l = d.getHunks();
        ListIterator lIt = l.listIterator();
        List<WootOp> data = new ArrayList<WootOp>();

        if (lIt.hasNext()) {
            try {
                this.wootEngine.loadClock();
            } catch (ClockException e) {
                throw new XWootException(this.siteId+" : Problem when synchronizing content"+e);
            }
            WootPage page = null;
            try {
                if (inCopy) {
                    page = this.wootEngine.loadCopy(pageName);
                } else {
                    page = this.wootEngine.loadPage(pageName);
                }
            }
            catch (WootEngineException e) {
                throw new XWootException(this.siteId+" : Problem when synchronizing content"+e);

            } 
            do {
                Hunk hunk = (Hunk) lIt.next();

                if (hunk instanceof HunkAdd) {
                    HunkAdd ha = (HunkAdd) hunk;
                    Iterator it = ha.getNewContent().iterator();
                    int pos = ha.getLD2();
                    int i = -1;

                    while (it.hasNext()) {
                        String line = (String) it.next();
                        WootOp ins = null;
                        try {
                            ins = this.getWootEngine().ins(page, line, (pos + i));
                        } catch (WootEngineException e) {
                            throw new XWootException(this.siteId+" : Problem when synchronizing content"+e);
                        }
                        data.add(ins);
                        i++;
                    }
                } else if (hunk instanceof HunkDel) {
                    HunkDel hDel = ((HunkDel) hunk);
                    int nbOfLine = hDel.getLF1() - hDel.getLD1() + 1;
                    int pos = hDel.getLD2() - 1;

                    for (int i = 0; i < nbOfLine; i++) {
                        WootOp del = null;
                        try {
                            del = this.getWootEngine().del(page, pos);
                        } catch (WootEngineException e) {
                            throw new XWootException(this.siteId+" : Problem when synchronizing content"+e); 
                        }
                        data.add(del);
                    }
                } else if (hunk instanceof HunkChange) {
                    throw new RuntimeException("HunkChange might not be detected, check the jlibdiff configuration");
                }
            } while (lIt.hasNext());
            try {
                this.wootEngine.unloadClock();
            } catch (ClockException e) {
                this.logger.error(this.siteId+" : Problem when synchronizing content"+e);
            }
            try {
                this.wootEngine.unloadPage(page);
            } catch (WootEngineException e) {
                this.logger.error(this.siteId+" : Problem when synchronizing content"+e);
            } 
        }

        if (!data.isEmpty()) {
            this.logger.info(this.siteId + " : " + data.size() + " operation(s) applicated to content model\n\n");
        } else {
            this.logger.info(this.siteId + " : Synchronize page content :" + pageName + " -- no diff.\n\n");
        }

        return data;
        }

    /**
     * DOCUMENT ME!
     * @throws XWootException 
     * 
     */
    public synchronized void synchronizePages() throws XWootException
    {
        if (!this.isContentManagerConnected()) {
            return;
        }
        this.logger.info(this.siteId + " : Starting the synchronisation of each managed pages");

        Collection listPages=null;
        listPages = XWootPage.getManagedPageNames(this.lastVuePagesDir);
        Iterator i = listPages.iterator();
        try {
            this.contentManager.login();
        } catch (WikiContentManagerException e) {
            throw new XWootException("Problem when logging to content manager");
        }
        // for each page
        while (i.hasNext()) {
            String pageName = (String) i.next();
            this.synchronizePage(pageName);
        }
        try {
            this.contentManager.logout();
        } catch (WikiContentManagerException e) {
            throw new XWootException("Problem when loggout to content manager");
        }
        this.logger.info(this.siteId + " : Synchronising OK.");
    }

    /**
     * DOCUMENT ME!
     * 
     * @param receivedMessage DOCUMENT ME!
     * @throws XWootException 
     */
    public synchronized void receivePatch(Message message) throws XWootException 
    {
        if (!this.isConnectedToP2PNetwork()) {
            return;
        }  
        String randNeighbor = (String) message.getRandNeighbor();
        this.logger.info(this.siteId + " : received message...");
        switch (message.getAction()) {
            case LpbCastAPI.LOG_OBJECT:
                this.logger.info(this.siteId + " : Integrate antientropy messages\n\n");

                Collection contents = (Collection) message.getContent();

                for (Iterator iter = contents.iterator(); iter.hasNext();) {
                    Message mess = (Message) iter.next();
                    try {
                        if (!this.antiEntropy.getLog().existInLog(mess.getId())) {
                            this.antiEntropy.logMessage(mess.getId(), mess);
                            this.treatePatch((Patch)mess.getContent());
                        }
                    } catch (AntiEntropyException e) {
                        throw new XWootException(this.siteId+" : Problem to send a message"+e);
                    } 
                }
                break;

            case LpbCastAPI.LOG_AND_GOSSIP_OBJECT:

                try {
                    if (!this.antiEntropy.getLog().existInLog(message.getId())) {
                        this.logger.info(this.siteId + " : received message : integration and logging.");
                        this.antiEntropy.logMessage(message.getId(), message);
                        this.treatePatch((Patch)message.getContent());
                    } else {
                        this.logger.info(this.siteId + " : received message : already in log.");
                    }
                } catch (AntiEntropyException e) {
                    throw new XWootException(this.siteId+" : Problem to log message"+e);
                } 

                message.setRound(message.getRound() - 1);
                message.setOriginalPeerId(this.getXWootPeerId());

                if (message.getRound() > 0) {
                    this.logger.info(this.siteId + " : Received message : round >0 ; gossip message.");
                    try {
                        this.sender.gossip(this.getXWootPeerId(), message);
                    } catch (SenderException e) {
                        throw new XWootException(this.siteId+" : Problem to gossip message"+e);
                    } 
                } else {
                    this.logger.info(this.siteId + " : Received message : round=0 ; stop gossip message.");
                }

                break;

            case LpbCastAPI.ANTI_ENTROPY:
                // send diff with local log
                this.logger.info(this.siteId + " : Message ask antientropy diff -- sending it.");

                Collection content;
                try {
                    content = this.antiEntropy.answerAntiEntropy(message.getContent());
                } catch (AntiEntropyException e) {
                    throw new XWootException(this.siteId+" : Problem to do antiEntropy \n"+e);   
                }
                Message toSend;

                toSend = this.sender.getNewMessage(this.getXWootPeerId(), content, LpbCastAPI.LOG_OBJECT, 0);

                this.logger
                .debug(this.siteId
                    + " : New message -- content : patches : result of diff beetween given log and local log -- Action : LOG_OBJECT -- round : 0");
                try {
                    this.sender.sendTo(message.getOriginalPeerId(), toSend);
                } catch (SenderException e) {
                    throw new XWootException(this.siteId+" : Problem to send a Message \n"+e);
                }

                break;

            default:
                break;
        }
        if (randNeighbor != null && !this.getNeighborsList().contains(randNeighbor) && this.addNeighbour(randNeighbor)) {
            this.doAntiEntropy(randNeighbor);
        }
    }

    public boolean isConnectedToP2PNetwork()
    {
        return this.p2Pconnected;
    }

    public boolean isContentManagerConnected()
    {
        return this.contentManagerConnected;
    }

    public boolean joinNetwork(String neighborURL) throws XWootException
    {
        File s = null;
        if (this.isWootStorageComputed()) {
            s = new File(this.getStateFilePath());
        }
        if (!this.isConnectedToP2PNetwork()) {
            this.p2Pconnected = true;
        }
        if (!this.isContentManagerConnected()) {
            this.contentManagerConnected = true;
        }

        if (this.getNeighborsList().contains(neighborURL) || this.addNeighbour(neighborURL)) {
            if (s == null) {
                s = this.askState(this.getXWootPeerId(), neighborURL);
                if (s == null) {
                    this.logger.warn(this.siteId + " : problem to get state of neighbor : " + neighborURL);
                    return false;
                }
            }
            this.importWootStorage(s);
            return true;
        }
        return false;
    }

    public File askState(String from, String to) throws XWootException
    {

        try {
            System.out.println(this.getXWootPeerId() + " Ask state to " + NetUtil.normalize(to));
            URL getNeighborState =
                new URL(NetUtil.normalize(to) + HttpServletLpbCast.SENDSTATECONTEXT + "?neighbor=" + from
                    + "&file=stateFile");
            getNeighborState.openConnection().addRequestProperty("file", "stateFile");

            File state = NetUtil.getFileViaHTTPRequest(getNeighborState);

            return state;
        } catch (IOException e) {
            this.logger.error(this.peerId+" : Problem to get file via http request \n"+e);
            throw new XWootException(this.peerId+" : Problem to get file via http request \n"+e);
        } catch (URISyntaxException e) {
            this.logger.error(this.peerId+" : Problem to get file via http request \n"+e);
            throw new XWootException(this.peerId+" : Problem to get file via http request \n"+e);
        }

    }

    public Message[] askAE(String from, String to) throws XWootException 
    {
        try {
            System.out.println(this.getXWootPeerId() + " Ask antiEntropy to " + NetUtil.normalize(to));
            URL getNeighborAE =
                new URL(NetUtil.normalize(to) + HttpServletLpbCast.SENDAEDIFFCONTEXT + "?neighbor=" + from
                    + "&file=stateFile");
            getNeighborAE.openConnection().addRequestProperty("file", "stateFile");
            //Message[] log=(Message[]) this.getAntiEntropy().getContentForAskAntiEntropy();
            //TODO send log and receive diff 
            return null;
            //NetUtil.sendObjectViaHTTPRequest(getNeighborAE, log);
        }catch (IOException e) {
            this.logger.error(this.peerId+" : Problem to get file via http request \n"+e);
            throw new XWootException(this.peerId+" : Problem to get file via http request \n"+e);
        } catch (URISyntaxException e) {
            this.logger.error(this.peerId+" : Problem to get file via http request \n"+e);
            throw new XWootException(this.peerId+" : Problem to get file via http request \n"+e);
        } 
    }

    public boolean createNetwork() throws XWootException
    {
        try {
            this.wootEngine.clearWorkingDir();
        } catch (WootEngineException e) {
            this.logger.error(this.peerId+" : Problem when clearing wootEngine dir\n"+e);
            throw new XWootException(this.peerId+" : Problem when clearing wootEngine dir\n"+e);
        }
        this.tre.clearWorkingDir();
        this.antiEntropy.clearWorkingDir();
        try {
            this.sender.clearWorkingDir();
        } catch (SenderException e) {
            this.logger.error(this.peerId+" : Problem when clearing sender dir\n"+e);
            throw new XWootException(this.peerId+" : Problem when clearing  sender dir\n"+e);
        }
        this.clearWorkingDir();
        this.logger.info(this.siteId + " : all datas clears");
        if (!this.isContentManagerConnected()) {
            this.contentManagerConnected = true;
        }
        return true;
    }

    public void connectToContentManager() throws XWootException
    {
        if (!this.isContentManagerConnected()) {
            if (this.isConnectedToP2PNetwork()) {
                this.doAntiEntropyWithAllNeighbors();
            }
            this.contentManagerConnected = true;
            this.synchronizePages();
        }
    }

    public void disconnectFromContentManager() throws XWootException
    {
        if (this.isContentManagerConnected()) {
            if (this.isConnectedToP2PNetwork()) {
                this.doAntiEntropyWithAllNeighbors();
            }
            this.synchronizePages();
            this.contentManagerConnected = false;
        }
    }

    public void reconnectToP2PNetwork() throws XWootException
    {
        if (!this.isConnectedToP2PNetwork()) {
            if (this.isContentManagerConnected()) {
                this.synchronizePages();
            }
            this.sender.connectSender();
            this.p2Pconnected = true;
            this.doAntiEntropyWithAllNeighbors();
        }
    }

    public void disconnectFromP2PNetwork() throws XWootException
    {
        if (this.isConnectedToP2PNetwork()) {
            if (this.isContentManagerConnected()) {
                this.synchronizePages();
            }
            this.doAntiEntropyWithAllNeighbors();

            this.sender.disconnectSender();
            this.p2Pconnected = false;
        }
    }

    public void doAntiEntropyWithAllNeighbors() throws XWootException
    {
        if (this.isConnectedToP2PNetwork()) {
            Collection c = this.getNeighborsList();
            if ((c == null) || c.isEmpty()) {
                return;
            }

            Iterator i = c.iterator();
            while (i.hasNext()) {
                this.doAntiEntropy((String) i.next());
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @throws XWootException 
     * 
     */
    synchronized public void doAntiEntropy(String neighborURL) throws XWootException 
    {
        if (!this.isConnectedToP2PNetwork())
            return;
        this.logger.info(this.siteId + " : Asking antiEntropy with : " + neighborURL);

        Message message;
        try {
            message = this.sender.getNewMessage(this.getXWootPeerId(), this.antiEntropy.getContentForAskAntiEntropy(),
                LpbCastAPI.ANTI_ENTROPY, 0);
        } catch (AntiEntropyException e) {
            this.logger.error(this.peerId+" : Problem to get content for antiEntropy \n"+e);
            throw new XWootException(this.peerId+" : Problem to get content for antiEntropy \n"+e);
        }
        this.logger
        .debug(this.siteId + " : New message -- content : log patches -- Action : ANTI_ENTROPY -- round : 0");
        try {

            this.sender.sendTo(neighborURL, message);
        } catch (SenderException e) {
            this.logger.error(this.peerId+" : Problem to send message\n"+e);
            throw new XWootException(this.peerId+" : Problem to send message\n"+e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public AntiEntropy getAntiEntropy()
    {
        return this.antiEntropy;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public File computeState() throws XWootException
    {
        if (!this.contentManagerConnected) {
            throw new XWootException("Can't initialize woot Storage : contentManager is not connected.");
        }

        try {
            if (this.isConnectedToP2PNetwork() && this.sender.getNeighborsList().size() > 0) {
                throw new XWootException(
                "Can't initialize woot Storage : neighbors list is not empty (This operation must be done at startup).");
            }
        } catch (SenderException e) {
            this.logger.error(this.peerId+
                " : Can't initialize woot Storage : Problem to get neighbors list.\n"+e);
            throw new XWootException(this.peerId+
                " : Can't initialize woot Storage : Problem to get neighbors list.\n"+e);
        }

        // initialization of the state directory
        File stateD = new File(this.stateDir);
        FileUtil.deleteDirectory(stateD);
        stateD.mkdir();

        // get page list
        /*********************************************/
        /** TODO WARNING BE CARREFUL ! **/
        /* this.addAllPageManagement(); */
        Collection l= new ArrayList<String>();
        try {
            l = this.contentManager.getListPageId("test");
        } catch (WikiContentManagerException e) {
            //void
        }
        l.add("Main.WebHome");
        //        if (l==null){
        //            this.contentManager.createSpace("test");
        //            this.contentManager.createPage("test.WebHome", "");
        //            this.contentManager.overwritePageContent("test.WebHome", "This page have been created to test XWoot application");
        //            l = this.contentManager.getListPageId("test");
        //        }
        /*********************************************/ 

        Iterator i = l.iterator();
        while (i.hasNext()) {
            String page = (String) i.next();
            XWootPage xWootPage = new XWootPage(page, null);

            if (!this.isPageManaged(xWootPage)) {
                this.addPageManagement(xWootPage);
            }
        } 


        this.synchronizePages();
        this.logger.debug(this.siteId + " : create state");

        // get WOOT state
        File wootState;
        try {
            wootState = this.getWootEngine().getState();
        } catch (WootEngineException e) {
            this.logger.error(this.peerId+" : Problem to get woot engine state \n"+e);
            throw new XWootException(this.peerId+" : Problem to get woot engine state \n"+e);
        }

        File copy0 = new File(this.stateDir + File.separatorChar + WootEngine.STATEFILENAME);
        wootState.renameTo(copy0);


        try {
            // get XWOOT state
            FileUtil.zipDirectory(this.lastVuePagesDir,this.stateDir + File.separatorChar + XWOOTSTATEFILENAME);
            // get TRE state
            FileUtil.zipDirectory(this.tre.getWorkingDir(),this.stateDir + File.separatorChar + ThomasRuleEngine.TRESTATEFILENAME);
            // create page list file
            File pageListFile = new File(this.stateDir + File.separator + PAGELISTFILEFORSTATE);
            XStream xstream = new XStream();
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(pageListFile));
            PrintWriter output = new PrintWriter(osw);
            output.print(xstream.toXML(l));
            output.flush();
            output.close();

            File r = new File(FileUtil.zipDirectory(this.stateDir,File.createTempFile("state",".zip").getPath()));
            File result=new File(this.stateDir + File.separatorChar + STATEFILENAME);
            r.renameTo(result);

            return result;
        } catch (IOException e) {
            this.logger.error(this.peerId+" : Problem to zip xwoot datas \n"+e);
            throw new XWootException(this.peerId+" : Problem to zip xwoot datas \n"+e);
        }




    }

    /**
     * DOCUMENT ME!
     * 
     * @param state DOCUMENT ME!
     * @throws XWootException 
     * 
     */
    @SuppressWarnings("cast")
    public Collection setWootStorage(File s) throws XWootException 
    {
        this.logger.debug(this.siteId + " : receive state and apply");

        if (s == null) {
            return null;
        }
        System.out.println("Zip file : " + s);
        try {
            ZipFile state = new ZipFile(s);
            FileUtil.unzipInDirectory(state, this.stateDir);
            File[] states = new File[4];
            String[] l = new File(this.stateDir).list();

            if (l.length < 3 || l.length > 5) {
                return null;
            }

            for (int i = 0; i < l.length; i++) {
                int j = -1;
                if (l[i].equals(WootEngine.STATEFILENAME)) {
                    j = 0;
                } else if (l[i].equals(XWOOTSTATEFILENAME)) {
                    j = 1;
                } else if (l[i].equals(ThomasRuleEngine.TRESTATEFILENAME)) {
                    j = 2;
                } else if (l[i].equals(PAGELISTFILEFORSTATE)) {
                    j = 3;
                }

                if (j != -1)
                    states[j] = new File(this.stateDir + File.separatorChar + l[i]);
            }


            if (this.getWootEngine().setState(states[0])) {
                if ((states.length >= 2) && (states[1] != null) && (states[1] instanceof File)) {
                    // MD gestion
                    if ((states.length >= 3) && (states[2] != null) && (states[2] instanceof File)) {
                        ZipFile mDState = new ZipFile(states[2]);

                        // delete all existing pages
                        File mDFile = new File(this.tre.getWorkingDir());
                        /*
                         * FileUtil.deleteDirectory(mDFile); mDFile.mkdirs();
                         */
                        FileUtil.unzipInDirectory(mDState, mDFile.getAbsolutePath());
                    }

                    ZipFile xWootState = new ZipFile(states[1]);

                    // delete all existing pages
                    File lastVuePagesFile = new File(this.lastVuePagesDir);
                    /*
                     * FileUtil.deleteDirectory(lastVuePagesFile); lastVuePagesFile.mkdirs();
                     */
                    FileUtil.unzipInDirectory(xWootState, lastVuePagesFile.getAbsolutePath());
                    System.out.println("Receive xWoot state");
                    // this.overwriteCPPages();
                    xWootState = new ZipFile(states[1]);

                    if (!this.isWootStorageComputed()) {
                        File temp = new File(this.stateDir + File.separatorChar + STATEFILENAME);
                        s.renameTo(temp);
                    }
                    XStream xstream = new XStream(new DomDriver());
                    Collection list =
                        (Collection) xstream.fromXML(new FileInputStream(this.stateDir + File.separatorChar
                            + PAGELISTFILEFORSTATE));

                    return list;

                }
                this.logger.warn(this.siteId + " : Woot state file without XWoot state file ! Maybe files empty ...");
                return null;
            }
        } catch (WootEngineException e) {
            this.logger.error(this.peerId+" : Problem to get woot engine state \n"+e);
            throw new XWootException(this.peerId+" : Problem to get woot engine state \n"+e);
        } catch (IOException e) {
            this.logger.error(this.peerId+" : Problem with files when computing state \n"+e);
            throw new XWootException(this.peerId+" : Problem with files when computing state \n"+e);
        }
        return null;

    }

    public boolean isWootStorageComputed()
    {
        File result = new File(this.getStateFilePath());
        return result.exists();
    }

    public void initialiseWootStorage() throws XWootException
    {
        this.computeState();
    }

    public File exportWootStorage()
    {
        if (this.isWootStorageComputed()) {
            return new File(this.getStateFilePath());
        }
        return null;
    }

    public boolean importWootStorage(File wst) throws XWootException
    {
        if (!this.isContentManagerConnected()) {
            this.contentManagerConnected = true;
        }
        File state = new File(this.getStateFilePath());
        if (wst!=null && !wst.equals(state)) {
            try {
                FileUtil.copyFile(wst.toString(), state.toString());
            } catch (IOException e) {
                this.logger.error(this.peerId+" : Problem when copying state file "+e);
                throw new XWootException(this.peerId+" : Problem when copying state file "+e);
            }
        }
        Collection list =this.setWootStorage(wst);
        this.overwriteCPPages(list);
        this.synchronizePages();
        return true;
    }


    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws XWootException 
     */
    public Collection getListOfManagedPages() throws XWootException
    {
        List<String> result = new ArrayList<String>();
        Collection temp = XWootPage.getManagedPageNames(this.lastVuePagesDir);
        Iterator i = temp.iterator();

        while (i.hasNext()) {
            String current = (String) i.next();
            result.add(current);
        }

        return result;
    }

    public void removeNeighbor(String neighborURL) throws XWootException 
    {
        try {
            this.sender.removeNeighbor(neighborURL);
        } catch (SenderException e) {
            this.logger.error(this.peerId+" : Problem to remove neighbor \n"+e);
            throw new XWootException(this.peerId+" : Problem to remove neighbor \n"+e);
        }
    }

    public boolean addNeighbour(String neighborURL)
    {
        return this.getSender().addNeighbor(this.getXWootPeerId(), neighborURL);

    }

    public boolean forceAddNeighbour(String neighborURL)
    {
        return this.getSender().addNeighbor(null, neighborURL);

    }

    public Collection<String> getNeighborsList() throws XWootException 
    {
        try {
            return this.sender.getNeighborsList();
        } catch (SenderException e) {
            this.logger.error(this.peerId+" : Problem to get neigbors list \n"+e);
            throw new XWootException(this.peerId+" : Problem to get neigbors list \n"+e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public LpbCastAPI getSender()
    {
        return this.sender;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public ThomasRuleEngine getTre()
    {
        return this.tre;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootEngine getWootEngine()
    {
        return this.wootEngine;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WikiContentManager getContentManager()
    {
        if (!this.contentManagerConnected) {
            return null;
        }
        return this.contentManager;
    }

    public String getContentManagerURL()
    {
        return this.contentManagerURL;
    }

    public String getXWootPeerId()
    {
        return this.peerId;
    }

    public String getStateFilePath()
    {
        return this.stateDir + File.separatorChar + STATEFILENAME;
    }

    /**
     * DOCUMENT ME!
     * @throws XWootException 
     * 
     */
    public void addAllPageManagement() throws XWootException 
    {
        if (!this.contentManagerConnected) {
            return;
        }
        Collection spaces;
        try {
            spaces = this.contentManager.getListSpaceId();
        } catch (WikiContentManagerException e) {
            this.logger.error(this.peerId+" : Problem to get space list \n"+e);
            throw new XWootException(this.peerId+" : Problem to get space list \n"+e);
        }

        Iterator i = spaces.iterator();

        // for each space
        while (i.hasNext()) {
            String space = (String) i.next();
            try {
                Collection pages = this.contentManager.getListPageId(space);

                if (pages != null) {
                    Iterator j = pages.iterator();

                    while (j.hasNext()) {
                        String page = (String) j.next();
                        XWootPage xWootPage = new XWootPage(page, null);

                        if (!this.isPageManaged(xWootPage)) {
                            this.addPageManagement(xWootPage);
                        }
                    }
                }
            } catch (WikiContentManagerException e) {
                this.logger.error(this.peerId+" : Problem to get page list for space "+space+" \n"+e);
                throw new XWootException(this.peerId+" : Problem to get page list for space "+space+" \n"+e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     * @throws XWootException 
     * 
     */
    public void removeAllManagedPages() throws XWootException
    {
        Collection managedPages = XWootPage.getManagedPageNames(this.lastVuePagesDir);
        Iterator i = managedPages.iterator();

        while (i.hasNext()) {
            String page = (String) i.next();
            XWootPage xWootPage = new XWootPage(page, null);

            if (xWootPage.existPage(this.lastVuePagesDir)) {
                this.removeManagedPage(xWootPage);
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean isPageManaged(XWootPage page)
    {
        return page.existPage(this.lastVuePagesDir);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param contentManagerPages DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws XWootException 
     */
    public HashMap isPagesManaged(Collection contentManagerPages) throws XWootException 
    {
        Collection managedPages = XWootPage.getManagedPageNames(this.lastVuePagesDir);
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();

        if (contentManagerPages == null) {
            return result;
        }

        Iterator<String> i = ((Collection<String>) contentManagerPages).iterator();

        while (i.hasNext()) {
            String current = i.next();
            result.put(current, Boolean.valueOf((managedPages.contains(current))));
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @throws XWootException 
     * 
     */
    public void addPageManagement(XWootPage page) throws XWootException 
    {
        page.createPage(this.lastVuePagesDir);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param space DOCUMENT ME!
     * @param managedPages DOCUMENT ME!
     * @throws XWootException 
     */
    public void setPageManagement(String space, List<String> managedPages) throws XWootException 
    {
        if (!this.contentManagerConnected) {
            return;
        }
        try{
            Collection pages = this.contentManager.getListPageId(space);

            if (pages != null) {

                Iterator i = pages.iterator();

                while (i.hasNext()) {
                    String currentPage = (String) i.next();
                    XWootPage page = new XWootPage(currentPage, null);

                    if (!managedPages.contains(currentPage) && page.existPage(this.lastVuePagesDir)) {
                        this.removeManagedPage(page);
                    } else if (managedPages.contains(currentPage) && !page.existPage(this.lastVuePagesDir)) {
                        this.addPageManagement(page);
                    }
                }

            }
        } catch (WikiContentManagerException e) {
            this.logger.error(this.peerId+" : Problem to get page list for space "+space+" \n"+e);
            throw new XWootException(this.peerId+" : Problem to get page list for space "+space+" \n"+e);
        }

    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean removeManagedPage(XWootPage page)
    {
        File file = new File(this.lastVuePagesDir + File.separator + page.getFileName());
        return file.delete();
    }
}
