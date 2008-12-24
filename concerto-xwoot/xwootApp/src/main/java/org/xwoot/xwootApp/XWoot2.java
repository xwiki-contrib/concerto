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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import jlibdiff.Diff;
import jlibdiff.Hunk;
import jlibdiff.HunkAdd;
import jlibdiff.HunkChange;
import jlibdiff.HunkDel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.MockXWootContentProvider;
import org.xwoot.XWootContentProviderException;
import org.xwoot.XWootId;
import org.xwoot.XWootObject;
import org.xwoot.XWootObjectField;
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
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;

import org.xwoot.wikiContentManager.WikiContentManager;

import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.WootEngineException;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootApp.core.LastModifiedContentIdMap;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.XWootObjectValue;
import org.xwoot.xwootUtil.FileUtil;

import java.net.URL;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWoot2 implements XWootAPI
{
    private MockXWootContentProvider contentManager;

    private WootEngine wootEngine;

    private LpbCastAPI sender;

    private ThomasRuleEngine tre;

    private AntiEntropy antiEntropy;

    private final Log logger = LogFactory.getLog(this.getClass());

    private Integer siteId;

    private boolean p2Pconnected;

    private boolean contentManagerConnected;

    private String contentManagerURL;

    private String peerId;

    private String stateDir;

    public static final String STATEFILENAME = "state.zip";

    private String workingDir;

    /**
     * A content id list. ContentManager adds an id when a content change occurs. (see {@link LastModifiedContentIdMap}
     * )
     */
    private LastModifiedContentIdMap lastModifiedContentIdMap;

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
     * @throws WootEngineException
     * @throws XWootContentProviderException
     */
    public XWoot2(WikiContentManager contentManager, WootEngine wootEngine, LpbCastAPI sender, String workingDir,
        String peerId, Integer siteId, ThomasRuleEngine tre, AntiEntropy ae) throws XWootException
    {
        this.lastModifiedContentIdMap = new LastModifiedContentIdMap(workingDir);
        this.workingDir = workingDir;
        this.stateDir = workingDir + File.separator + "stateDir";
        this.createWorkingDir();

        try {
            this.contentManager = new MockXWootContentProvider();
        } catch (XWootContentProviderException e) {
            throw new XWootException(e);
        }
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

    public void clearWorkingDir()
    {
        File stateDirFile = new File(this.stateDir);

        if (stateDirFile.exists()) {
            FileUtil.deleteDirectory(stateDirFile);
        }
      
    }
    
    public void clearBaseDir() throws XWootException
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

        File stateDirFile = new File(this.stateDir);

        if (!stateDirFile.exists() && !stateDirFile.mkdir()) {
            throw new XWootException("Can't create pages directory: " + stateDirFile);
        }
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
                            this.treatePatch((Patch) mess.getContent());
                        }
                    } catch (AntiEntropyException e) {
                        throw new XWootException(this.siteId + " : Problem to send a message", e);
                    }
                }
                break;

            case LpbCastAPI.LOG_AND_GOSSIP_OBJECT:

                try {
                    if (!this.antiEntropy.getLog().existInLog(message.getId())) {
                        this.logger.info(this.siteId + " : received message : integration and logging.");
                        this.antiEntropy.logMessage(message.getId(), message);
                        this.treatePatch((Patch) message.getContent());
                    } else {
                        this.logger.info(this.siteId + " : received message : already in log.");
                    }
                } catch (AntiEntropyException e) {
                    throw new XWootException(this.siteId + " : Problem to log message", e);
                }

                message.setRound(message.getRound() - 1);
                message.setOriginalPeerId(this.getXWootPeerId());

                if (message.getRound() > 0) {
                    this.logger.info(this.siteId + " : Received message : round >0 ; gossip message.");
                    try {
                        this.sender.gossip(message);
                    } catch (SenderException e) {
                        throw new XWootException(this.siteId + " : Problem to gossip message", e);
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
                    throw new XWootException(this.siteId + " : Problem to do antiEntropy \n", e);
                }
                Message toSend;

                toSend = this.sender.getNewMessage(this.getXWootPeerId(), content, LpbCastAPI.LOG_OBJECT, 0);

                this.logger
                    .debug(this.siteId
                        + " : New message -- content : patches : result of diff beetween given log and local log -- Action : LOG_OBJECT -- round : 0");
                try {
                    this.sender.sendTo(message.getOriginalPeerId(), toSend);
                } catch (SenderException e) {
                    throw new XWootException(this.siteId + " : Problem to send a Message \n", e);
                }

                break;

            default:
                break;
        }
        if (randNeighbor != null && !this.getNeighborsList().contains(randNeighbor) && this.addNeighbour(randNeighbor)) {
            this.doAntiEntropy(randNeighbor);
        }
    }

    private void treatePatch(Patch patch) throws XWootException
    {
        XWootId xWootId =
            new XWootId(patch.getPageId(), patch.getTimestamp(), patch.getVersion(), patch.getMinorVersion());

        this.lastModifiedContentIdMap.add(xWootId, patch.getObjectId());

        if (patch.getMDelements() != null) {
            try {
                for (Object tre_op : patch.getMDelements()) {
                    this.getTre().applyOp((ThomasRuleOp) tre_op);
                }
            } catch (ThomasRuleEngineException e) {
                throw new XWootException("Problem with ThomasRuleEngine");
            }
        }

        try {
            this.getWootEngine().deliverPatch(patch);
        } catch (WootEngineException e) {
            throw new XWootException("Problem with WootEngine");
        }
        this.synchronizeFromModelToXWiki();
    }

    private void sendNewPatch(Patch newPatch) throws XWootException
    {
        Message message = null;
        message =
            this.sender.getNewMessage(this.getXWootPeerId(), newPatch, LpbCastAPI.LOG_AND_GOSSIP_OBJECT, this.sender
                .getRound());
        if (message != null) {
            try {
                this.sender.gossip(message);
                this.getAntiEntropy().logMessage(message.getId(), message);
            } catch (SenderException e) {
                throw new XWootException("Can't send new Message ", e);
            } catch (AntiEntropyException e) {
                throw new XWootException("Can't send new Message ", e);
            }
        }
    }

    private synchronized void synchronizeFromXWikiToModel(boolean inCopy) throws XWootException
    {
        try {
            List<XWootId> xwootIds = this.contentManager.getModifiedPagesIds();

            while (xwootIds != null && xwootIds.size() > 0) {
                for (XWootId id : xwootIds) {
                    List<XWootObject> objects = this.contentManager.getModifiedEntities(id);
                    for (XWootObject newObject : objects) {
                        Patch newPatch = this.synchronizeObjectFromXWikiToModel(newObject, id, inCopy);
                        if (inCopy) {
                            this.wootEngine.deliverPatch(newPatch);
                        }
                        this.contentManager.clearModification(id);
                        this.sendNewPatch(newPatch);
                    }
                }
                xwootIds = this.contentManager.getModifiedPagesIds();
            }
        } catch (XWootContentProviderException e) {
            throw new XWootException(e);

        } catch (WootEngineException e) {
            throw new XWootException(e);
        }
    }

    private synchronized Patch synchronizeObjectFromXWikiToModel(XWootObject newObject, XWootId id, boolean inCopy)
        throws XWootException, WootEngineException
    {
        List<ThomasRuleOp> treOps = new ArrayList<ThomasRuleOp>();
        String objectId = newObject.getGuid();
        // TRE content
        ThomasRuleOp tre_op = this.synchronizeWithTRE(newObject);
        if (tre_op == null) {
            throw new XWootException("Synchronization problem !");
        }
        treOps.add(tre_op);
        List<WootOp> wootOps = null;

        if (newObject.hasWootableFields()) {
            String pageName = newObject.getPageId();
            for (XWootObjectField f : newObject.getFields()) {
                String fieldId = f.getName();
                if (f.isWootable()) {
                    String oldContent = "";
                    if (inCopy) {
                        oldContent =
                            this.getWootEngine().getContentManager().getCopyContent(pageName, objectId, fieldId);
                    } else {
                        oldContent = this.getWootEngine().getContentManager().getContent(pageName, objectId, fieldId);
                    }
                    wootOps =
                        this.synchronizeWithWootEngine(pageName, objectId, fieldId, oldContent, (String) f.getValue(),
                            inCopy);
                }
            }
        }
        Patch newPatch =
            new Patch(wootOps, treOps, id.getPageId(), objectId, id.getTimestamp(), id.getVersion(), id
                .getMinorVersion());
        return newPatch;
    }

    private void synchronizeFromModelToXWiki() throws XWootException
    {
        Map<XWootId, Set<String>> currentList = this.lastModifiedContentIdMap.getCurrentMap();
        for (XWootId xwid : currentList.keySet()) {
            for (String objectId : currentList.get(xwid)) {
                XWootObjectValue obj_tre = null;
                String pageName = xwid.getPageId();
                XWootObjectIdentifier id_tre = new XWootObjectIdentifier(pageName, objectId);
                try {
                    obj_tre = (XWootObjectValue) this.tre.getValue(id_tre);
                    if (obj_tre == null) {
                        throw new XWootException(
                            "Problem with last modified content id list -- An id is in the list but not in the Thomas Rule Engine model.");
                    }
                    XWootObject xwootObject = (XWootObject) obj_tre.get();
                    for (XWootObjectField f : xwootObject.getFields()) {
                        String fieldId = f.getName();
                        if (f.isWootable()) {
                            String content =
                                this.wootEngine.getContentManager().getContent(pageName, objectId, fieldId);
                            obj_tre.setObjectField(new XWootObjectField(fieldId, content, true));
                        }
                    }
                    this.synchronizeObjectFromModelToXWiki(xwootObject);
                    this.lastModifiedContentIdMap.remove(xwid,objectId);         
                } catch (ThomasRuleEngineException e) {
                    throw new XWootException("Problem with Thomas Rule Engine", e);
                } catch (WootEngineException e) {
                    throw new XWootException("Problem with WootEngine", e);
                }
            }
           
        }

    }

    private synchronized void synchronizeObjectFromModelToXWiki(XWootObject o) throws XWootException,
        WootEngineException
    {
        this.logger.info(this.siteId + " : apply content modif");

        if (this.isContentManagerConnected()) {
            if (!this.contentManager.store(o)) {
                this.logger.info(this.siteId + " : some no consummed datas for id : " + o.getPageId() + "."
                    + o.getGuid());
                this.synchronizeFromXWikiToModel(true);
                this.synchronizeObjectFromModelToXWiki(o);
            }

            for (XWootObjectField f : o.getFields()) {
                if (f.isWootable()) {
                    this.getWootEngine().getContentManager().copyWootContent(o.getPageId(), o.getGuid(), f.getName());
                }
            }
        }

    }

    private synchronized List<WootOp> synchronizeWithWootEngine(String pageName, String objectId, String fieldId,
        String oldPage, String newPage, boolean inCopy) throws XWootException
    {
        BufferedReader oldContent = new BufferedReader(new StringReader(oldPage));
        BufferedReader newContent = new BufferedReader(new StringReader(newPage));

        Diff d = new Diff();
        try {
            d.diff(oldContent, newContent);
        } catch (IOException e) {
            this.logger.error(this.siteId + " : Problem with diff when synchronizing content", e);
        }

        List l = d.getHunks();
        ListIterator lIt = l.listIterator();
        List<WootOp> data = new ArrayList<WootOp>();

        if (lIt.hasNext()) {
            try {
                this.wootEngine.loadClock();
            } catch (ClockException e) {
                throw new XWootException(this.siteId + " : Problem when synchronizing content", e);
            }
            WootContent page = null;
            try {
                if (inCopy) {
                    page = this.wootEngine.getContentManager().loadWootContentCopy(pageName, objectId, fieldId);
                } else {
                    page = this.wootEngine.getContentManager().loadWootContent(pageName, objectId, fieldId);
                }
            } catch (WootEngineException e) {
                throw new XWootException(this.siteId + " : Problem when synchronizing content", e);

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
                            ins = this.getWootEngine().insert(page, line, (pos + i));
                        } catch (WootEngineException e) {
                            throw new XWootException(this.siteId + " : Problem when synchronizing content", e);
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
                            del = this.getWootEngine().delete(page, pos);
                        } catch (WootEngineException e) {
                            throw new XWootException(this.siteId + " : Problem when synchronizing content", e);
                        }
                        data.add(del);
                    }
                } else if (hunk instanceof HunkChange) {
                    throw new XWootException("HunkChange might not be detected, check the jlibdiff configuration");
                }
            } while (lIt.hasNext());
            try {
                this.wootEngine.unloadClock();
            } catch (ClockException e) {
                this.logger.error(this.siteId + " : Problem when synchronizing content", e);
            }
            try {
                this.wootEngine.getContentManager().unloadWootContent(page);
            } catch (WootEngineException e) {
                this.logger.error(this.siteId + " : Problem when synchronizing content", e);
            }
        }

        if (!data.isEmpty()) {
            this.logger.info(this.siteId + " : " + data.size() + " operation(s) applicated to content model\n\n");
        } else {
            this.logger.info(this.siteId + " : Synchronize page content :" + pageName + " -- no diff.\n\n");
        }

        return data;
    }

    private ThomasRuleOp synchronizeWithTRE(XWootObject o) throws XWootException
    {
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier(o.getPageId(), o.getGuid());
        try {
            XWootObjectValue tre_value = (XWootObjectValue) this.tre.getValue(tre_id);

            if (tre_value == null) {
                tre_value = new XWootObjectValue();
            }

            if (o.isNewlyCreated()) {
                tre_value.setObject(o);
            } else {
                for (XWootObjectField f : o.getFields()) {
                    tre_value.setObjectField(f);
                }
            }

            ThomasRuleOp op = this.tre.getOp(tre_id, tre_value);
            this.tre.applyOp(op);
            return op;

        } catch (ThomasRuleEngineException e) {
            throw new XWootException(e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws XWootException
     * @throws WootEngineException
     */
    public synchronized void synchronize() throws XWootException
    {
        if (!this.isContentManagerConnected()) {
            return;
        }
        this.logger.info(this.siteId + " : Starting the synchronisation of each managed pages");
        try {
            this.contentManager.login("Admin", "admin");

            this.synchronizeFromXWikiToModel(false);
            this.synchronizeFromModelToXWiki();
        } catch (XWootContentProviderException e) {
            throw new XWootException(e);
        }
        this.logger.info(this.siteId + " : Synchronising OK.");
    }

    public boolean joinNetwork(String neighborURL) throws XWootException
    {
        File s = null;
        if (this.isStateComputed()) {
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
            this.importState(s);
            return true;
        }
        return false;
    }

    public boolean createNetwork() throws XWootException
    {
        this.clearWorkingDir();
        try {
            this.wootEngine.clearWorkingDir();
        } catch (WootEngineException e) {
            this.logger.error(this.peerId + " : Problem when clearing wootEngine dir\n", e);
            throw new XWootException(this.peerId + " : Problem when clearing wootEngine dir\n", e);
        }
        this.tre.clearWorkingDir();
        this.antiEntropy.clearWorkingDir();
        // try {
        this.sender.clearWorkingDir();
        // } catch (SenderException e) {
        // this.logger.error(this.peerId+" : Problem when clearing sender dir\n",e);
        // throw new XWootException(this.peerId+" : Problem when clearing  sender dir\n",e);
        // }
       
        this.logger.info(this.siteId + " : all datas clears");
        if (!this.isContentManagerConnected()) {
            this.contentManagerConnected = true;
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param state DOCUMENT ME!
     * @throws XWootException
     */
    private void setState(File s) throws XWootException
    {
        this.logger.debug(this.siteId + " : receive state and apply");

        if (s == null) {
            return;
        }
        System.out.println("Zip file : " + s);
        try {
            ZipFile state = new ZipFile(s);
            FileUtil.unzipInDirectory(state, this.stateDir);
            File[] states = new File[2];
            String[] l = new File(this.stateDir).list();

            if (l.length != 3) {
                return;
            }

            for (int i = 0; i < l.length; i++) {
                int j = -1;
                if (l[i].equals(WootEngine.STATE_FILE_NAME)) {
                    j = 0;
                } else if (l[i].equals(ThomasRuleEngine.TRE_STATE_FILE_NAME)) {
                    j = 1;
                }

                if (j != -1)
                    states[j] = new File(this.stateDir + File.separatorChar + l[i]);
            }

            this.getWootEngine().setState(states[0]);

            ZipFile mDState = new ZipFile(states[1]);

            // delete all existing pages
            File mDFile = new File(this.tre.getWorkingDir());
            /*
             * FileUtil.deleteDirectory(mDFile); mDFile.mkdirs();
             */
            FileUtil.unzipInDirectory(mDState, mDFile.getAbsolutePath());

            if (!this.isStateComputed()) {
                File temp = new File(this.stateDir + File.separatorChar + STATEFILENAME);
                s.renameTo(temp);
            }

            return;
        } catch (WootEngineException e) {
            this.logger.error(this.peerId + " : Problem to get woot engine state \n", e);
            throw new XWootException(this.peerId + " : Problem to get woot engine state \n", e);
        } catch (IOException e) {
            this.logger.error(this.peerId + " : Problem with files when computing state \n", e);
            throw new XWootException(this.peerId + " : Problem with files when computing state \n", e);
        }
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
            this.logger.error(this.peerId + " : Can't initialize woot Storage : Problem to get neighbors list.\n", e);
            throw new XWootException(this.peerId
                + " : Can't initialize woot Storage : Problem to get neighbors list.\n", e);
        }

        // initialization of the state directory
        File stateD = new File(this.stateDir);
        FileUtil.deleteDirectory(stateD);
        stateD.mkdir();

        this.synchronize();
        this.logger.debug(this.siteId + " : create state");

        // get WOOT state
        File wootState;
        try {
            wootState = this.getWootEngine().getState();
        } catch (WootEngineException e) {
            this.logger.error(this.peerId + " : Problem to get woot engine state \n", e);
            throw new XWootException(this.peerId + " : Problem to get woot engine state \n", e);
        }

        File copy0 = new File(this.stateDir + File.separatorChar + WootEngine.STATE_FILE_NAME);
        wootState.renameTo(copy0);

        try {
            // get TRE state
            FileUtil.zipDirectory(this.tre.getWorkingDir(), this.stateDir + File.separatorChar
                + ThomasRuleEngine.TRE_STATE_FILE_NAME);

            File r = new File(FileUtil.zipDirectory(this.stateDir, File.createTempFile("state", ".zip").getPath()));
            File result = new File(this.stateDir + File.separatorChar + STATEFILENAME);
            r.renameTo(result);

            return result;
        } catch (IOException e) {
            this.logger.error(this.peerId + " : Problem to zip xwoot datas \n", e);
            throw new XWootException(this.peerId + " : Problem to zip xwoot datas \n", e);
        }

    }

    public File getState()
    {
        if (this.isStateComputed()) {
            return new File(this.getStateFilePath());
        }
        return null;
    }

    public boolean isStateComputed()
    {
        File result = new File(this.getStateFilePath());
        return result.exists();
    }

    public boolean importState(File wst) throws XWootException
    {
        if (!this.isContentManagerConnected()) {
            this.contentManagerConnected = true;
        }
        File state = new File(this.getStateFilePath());
        if (wst != null && !wst.equals(state)) {
            try {
                FileUtil.copyFile(wst.toString(), state.toString());
            } catch (IOException e) {
                this.logger.error(this.peerId + " : Problem when copying state file ", e);
                throw new XWootException(this.peerId + " : Problem when copying state file ", e);
            }
        }
        this.setState(wst);
        this.synchronize();
        return true;
    }

    public File askState(String from, String to) throws XWootException
    {

        try {
            System.out.println(this.getXWootPeerId() + " Ask state to " + NetUtil.normalize(to));
            URL getNeighborState =
                new URL(NetUtil.normalize(to) + HttpServletLpbCast.SEND_STATE_SERVLET + "?neighbor=" + from
                    + "&file=stateFile");
            getNeighborState.openConnection().addRequestProperty("file", "stateFile");

            File state = NetUtil.getFileViaHTTPRequest(getNeighborState);

            return state;
        } catch (IOException e) {
            this.logger.error(this.peerId + " : Problem to get file via http request \n", e);
            throw new XWootException(this.peerId + " : Problem to get file via http request \n", e);
        } catch (URISyntaxException e) {
            this.logger.error(this.peerId + " : Problem to get file via http request \n", e);
            throw new XWootException(this.peerId + " : Problem to get file via http request \n", e);
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
     */
    synchronized public void doAntiEntropy(String neighborURL) throws XWootException
    {
        if (!this.isConnectedToP2PNetwork())
            return;
        this.logger.info(this.siteId + " : Asking antiEntropy with : " + neighborURL);

        Message message;
        try {
            message =
                this.sender.getNewMessage(this.getXWootPeerId(), this.antiEntropy.getMessageIdsForAskAntiEntropy(),
                    LpbCastAPI.ANTI_ENTROPY, 0);
        } catch (AntiEntropyException e) {
            this.logger.error(this.peerId + " : Problem to get content for antiEntropy \n", e);
            throw new XWootException(this.peerId + " : Problem to get content for antiEntropy \n", e);
        }
        this.logger
            .debug(this.siteId + " : New message -- content : log patches -- Action : ANTI_ENTROPY -- round : 0");
        try {

            this.sender.sendTo(neighborURL, message);
        } catch (SenderException e) {
            this.logger.error(this.peerId + " : Problem to send message\n", e);
            throw new XWootException(this.peerId + " : Problem to send message\n", e);
        }
    }

    public void connectToContentManager() throws XWootException
    {
        if (!this.isContentManagerConnected()) {
            if (this.isConnectedToP2PNetwork()) {
                this.doAntiEntropyWithAllNeighbors();
            }
            this.contentManagerConnected = true;
            this.synchronize();
        }
    }

    public boolean isContentManagerConnected()
    {
        return this.contentManagerConnected;
    }

    public void disconnectFromContentManager() throws XWootException
    {
        if (this.isContentManagerConnected()) {
            if (this.isConnectedToP2PNetwork()) {
                this.doAntiEntropyWithAllNeighbors();
            }
            this.synchronize();
            this.contentManagerConnected = false;
        }
    }

    public void reconnectToP2PNetwork() throws XWootException
    {
        if (!this.isConnectedToP2PNetwork()) {
            if (this.isContentManagerConnected()) {
                this.synchronize();
            }
            this.sender.connectSender();
            this.p2Pconnected = true;
            this.doAntiEntropyWithAllNeighbors();
        }
    }

    public boolean isConnectedToP2PNetwork()
    {
        return this.p2Pconnected;
    }

    public void disconnectFromP2PNetwork() throws XWootException
    {
        if (this.isConnectedToP2PNetwork()) {
            if (this.isContentManagerConnected()) {
                this.synchronize();
            }
            this.doAntiEntropyWithAllNeighbors();

            this.sender.disconnectSender();
            this.p2Pconnected = false;
        }
    }

    public void removeNeighbor(String neighborURL) throws XWootException
    {
        try {
            this.sender.removeNeighbor(neighborURL);
        } catch (SenderException e) {
            this.logger.error(this.peerId + " : Problem to remove neighbor \n", e);
            throw new XWootException(this.peerId + " : Problem to remove neighbor \n", e);
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
            this.logger.error(this.peerId + " : Problem to get neigbors list \n", e);
            throw new XWootException(this.peerId + " : Problem to get neigbors list \n", e);
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
    public MockXWootContentProvider getContentManager()
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
     * @return the lastModifiedContentIdMap
     */
    public LastModifiedContentIdMap getLastModifiedContentIdMap()
    {
        return this.lastModifiedContentIdMap;
    }

    public List<String> getLastPages(String id) throws XWootException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
