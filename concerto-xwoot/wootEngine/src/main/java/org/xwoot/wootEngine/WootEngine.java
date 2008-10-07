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

package org.xwoot.wootEngine;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;

import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootPage;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootDel;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;

import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class WootEngine
{
    private int wootEngineId;

    private String workingDir;

    private Clock opLocalClock;

    private Pool waitingQueue;

    private final Log logger = LogFactory.getLog(this.getClass());

    public final static String STATEFILENAME = "woot.zip";

    /**
     * Creates a new WootEngine object.
     * 
     * @param siteId Unique identifier of the wanted component
     * @param WORKINGDIR Directory with read/write access to serialize content
     * @param opClock Clock engine component instance
     * @throws Exception : serialization exceptions (given directory must exist and have read/write access)
     */
    public WootEngine(int siteId, String workingDir, Clock opClock) throws Exception
    {
        this.wootEngineId = siteId;
        this.setWorkingDir(workingDir);
        this.createWorkingDir();
        this.setOpLocalClock(opClock);
        this.setWaitingQueue(new Pool(workingDir + File.separator + "pool"));
        this.logger.info(this.wootEngineId + " WootEngine created.");
    }

    synchronized public void loadClock() throws ClockException
    {
        this.opLocalClock = this.opLocalClock.load();
    }

    synchronized public void unloadClock() throws ClockException
    {
        this.opLocalClock.store();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws Exception DOCUMENT ME!
     */
    public void copyPage(String pageName) throws UnsupportedEncodingException, IOException
    {
        if (this.pageExist(pageName)) {
            WootPage currentPage = this.loadPage(pageName);
            currentPage.setSavedPage(true);
            this.unloadPage(currentPage);
        }
    }

    /**
     * To create a new page in model.
     * 
     * @param pageName the new page to create
     * @return the page name of the created page
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws Exception : the given page name must not be empty
     */
    public WootPage createPage(String pageName) throws UnsupportedEncodingException, IOException
    {
        if ((pageName == null) || pageName.equals("")) {
            this.logger.error(this.getWootEngineId() + " Please enter a non-empty page name !");
            throw new IOException(this.getWootEngineId() + " Please enter a non-empty page name !");
        }

        if (this.pageExist(pageName)) {
            this.logger.error(this.getWootEngineId() + " This page already exist !");
            throw new IOException(this.getWootEngineId() + " This page already exist !");
        }

        WootPage wootPage = new WootPage(true);
        wootPage.setPageName(pageName);

        XStream xstream = new XStream();
        this.logger.debug(this.getWootEngineId() + " Create woot page : " + wootPage.getFileName());

        PrintWriter pw =
            new PrintWriter(new FileOutputStream(this.getWorkingDir() + File.separator + "pages" + File.separator
                + wootPage.getFileName()));
        pw.print(xstream.toXML(wootPage));
        pw.flush();
        pw.close();

        return wootPage;
    }

    public void clearWorkingDir() throws Exception
    {
        File f = new File(this.workingDir);
        if (f.exists()) {
            FileUtil.deleteDirectory(f);
        }
        this.createWorkingDir();
        this.waitingQueue.initializeLog(true);
    }

    private void createWorkingDir() throws Exception
    {
        File working = new File(this.workingDir);

        if (!working.exists()) {
            if (!working.mkdir()) {
                throw new Exception("Can't create main directory: " + working);
            }
        }

        File dirPages = new File(this.getWorkingDir() + File.separator + "pages");

        if (!dirPages.exists()) {
            if (!dirPages.mkdir()) {
                throw new Exception("Can't create pages directory: " + dirPages);
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param data DOCUMENT ME!
     * @param mdData DOCUMENT ME!
     * @param wootEnginePageId DOCUMENT ME!
     * @param siteId DOCUMENT ME!
     * @param clockValue DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public Patch createPatch(List data, List mdData, String wootEnginePageId, int siteId)
    {
        Patch result = new Patch();
        result.setData(data);
        result.setPageName(wootEnginePageId);
        result.setMDelements(mdData);

        return result;
    }

    // /**
    // * DOCUMENT ME!
    // *
    // * @param pageName DOCUMENT ME!
    // * @param line DOCUMENT ME!
    // * @param pos DOCUMENT ME!
    // *
    // * @return DOCUMENT ME!
    // *
    // * @throws Exception DOCUMENT ME!
    // */
    // public synchronized WootIns ins(String pageName, String line, int pos)
    // throws Exception {
    // if (!this.pageExist(pageName)) {
    // this.createPage(pageName);
    // }
    //
    // WootPage page = this.loadPage(pageName);
    // this.logger.debug(this.wootEngineId + " Direct insertion in " + pageName
    // + ", value : " + line + ", position : " + pos);
    //
    // WootIns result = this.ins(page, line, pos);
    // this.storePage(page);
    // this.unloadPage(page);
    //
    // return result;
    // }
    //
    // /**
    // * DOCUMENT ME!
    // *
    // * @param pageName DOCUMENT ME!
    // * @param pos DOCUMENT ME!
    // *
    // * @return DOCUMENT ME!
    // *
    // * @throws Exception DOCUMENT ME!
    // */
    // public synchronized WootDel del(String pageName, int pos) throws
    // Exception {
    // if (!this.pageExist(pageName)) {
    // this.createPage(pageName);
    // }
    //
    // WootPage page = this.loadPage(pageName);
    //
    // this.logger.debug(this.wootEngineId + "Direct suppression in pageName : "
    // + pageName + ", value : " + page.getRows().get(pos + 1).getValue() +
    // ", position : " + pos);
    //
    // WootDel result = this.del(page, pos);
    // this.storePage(page);
    // this.unloadPage(page);
    //
    // return result;
    // }

    /**
     * To delete an atomic value at a given position to a given page The method return the Woot operation
     * 
     * @param page : apply the deletion to this page (WootPage format)
     * @param pos : apply the deletion at this position (first position is 0)
     * @return : the corresponding Woot operation
     * @throws Exception : require ((pos req 0) && (pos lt page.size()))
     */
    public WootDel del(WootPage page, int pos) throws Exception
    {
        if ((pos >= 0) && (pos < page.size())) {
            int idxV = page.indexOfVisible(pos + 1);
            WootRow wr = page.elementAt(idxV);

            if (!wr.equals(WootRow.RE)) {
                WootDel del = new WootDel(wr.getWootId());
                int temp = this.getOpLocalClock().getValue();
                del.setOpid(new WootId(this.wootEngineId, temp));
                this.getOpLocalClock().setValue(temp + 1);
                del.setPageName(page.getPageName());
                del.setIndexRow(idxV);
                del.execute(page);
                this.logger.debug(this.wootEngineId + " Operation executed : deletion -- " + del);

                return del;
            }

            this.logger.error(this.getWootEngineId() + " - page : " + page.getPageName()
                + ":impossible deletion position " + pos);
            throw new Exception(this.getWootEngineId() + " - page : " + page.getPageName()
                + ":impossible deletion position " + pos);
        }

        this.logger.error(this.getWootEngineId() + " - page : " + page.getPageName() + ":impossible deletion position "
            + pos);
        throw new Exception(this.getWootEngineId() + " - page : " + page.getPageName()
            + ":impossible deletion position " + pos);
    }

    /**
     * To ins a given String value at a given position to a given page The method return the Woot operation
     * 
     * @param page : apply the op to this page (WootPage format)
     * @param alpha : the String value to insert
     * @param pos : apply the operation at this position (first position is 0)
     * @return : the corresponding Woot operation
     * @throws Exception : require ((pos leq page.size()) && (pos req 0))
     */
    public WootIns ins(WootPage page, String alpha, int pos) throws Exception
    {
        this.logger.debug(this.wootEngineId + " Direct insertion in " + page.getPageName() + ", value : " + alpha
            + ", position : " + pos);
        if ((pos <= page.size()) && (pos >= 0)) {
            int indexP = page.indexOfVisible(pos);
            WootRow rp = (indexP != -1) ? page.elementAt(indexP) : page.elementAt(0);

            if (!rp.equals(WootRow.RE)) {
                int indexN = page.indexOfVisibleNext(indexP);
                WootRow rn = (indexN != -1) ? page.elementAt(indexN) : page.elementAt(page.size() + 1);
                int deg_c = 1;
                deg_c += ((rp.getDegree() >= rn.getDegree()) ? rp.getDegree() : rn.getDegree());
                int temp = this.getOpLocalClock().getValue();
                WootRow r = new WootRow(new WootId(this.wootEngineId, temp), alpha, deg_c);
                WootOp ins = new WootIns(r, rp.getWootId(), rn.getWootId());
                ins.setOpid(new WootId(this.wootEngineId, temp));
                ins.setPageName(page.getPageName());
                this.getOpLocalClock().setValue(temp + 1);
                ins.execute(page);
                this.logger.debug(this.wootEngineId + " Operation executed : insertion -- " + ins);

                return (WootIns) ins;
            }

            this.logger.error(this.getWootEngineId() + " - page : " + page.getPageName()
                + ":impossible insertion position " + pos);
            throw new Exception(this.getWootEngineId() + " - page : " + page.getPageName()
                + ":impossible insertion position " + pos);
        }

        this.logger.error(this.getWootEngineId() + " - page : " + page.getPageName()
            + ":impossible insertion position " + pos);
        throw new Exception("Site " + this.getWootEngineId() + " - page : " + page.getPageName()
            + ": Il est inmpossible d'ins\u00E9rer \u00E0 la position " + pos);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param object DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    synchronized public void deliverPatch(Patch p) throws Exception
    {
        this.logger.info(this.wootEngineId + " Reception of a new patch for page : " + p.getPageName());
        this.logger.debug(this.wootEngineId + " patch : " + p.toString());

        String pageName = p.getPageName();

        if (!this.pageExist(pageName)) {
            this.createPage(pageName);
        }

        WootPage page = this.loadPage(pageName);
        this.logger.debug("Execution of patch operations...");
        this.getWaitingQueue().loadPool();
        for (Object obj : p.getData()) {
            WootOp op = (WootOp) obj;

            if (!this.executeOp(op, page)) {
                this.logger.debug(this.wootEngineId + "appenning to waiting queue : " + op.toString());

                this.getWaitingQueue().getContent().add(op);

            }
        }
        this.waitingQueueExec(page);
        this.getWaitingQueue().unLoadPool();
        this.unloadPage(page);
    }

    /**
     * Call this method to apply a given Woot Operation to a page
     * 
     * @param op : the operation to apply
     * @param page : apply the op to this page
     * @return true if op has been applied
     */
    private boolean executeOp(WootOp op, WootPage page)
    {
        if (!op.getPageName().equals(page.getPageName())) {
            return false;
        }
        synchronized (page) {
            if (op instanceof WootIns) {
                WootIns ins = (WootIns) op;
                int[] indexs = new int[2];
                indexs = ins.precond_v2(page);

                // in case of an op reception after a setState containing
                // this op
                if (page.indexOfId(ins.getNewRow().getWootId()) >= 0) {
                    this.logger.debug(this.wootEngineId
                        + " Operation not executed (this op was executed during a state transfert)"+" -- "+ins.getNewRow().getWootId());
                    return true;
                }
                // execution of the op
                else if (indexs != null) {
                    this.logger.debug(this.wootEngineId + " Operation executed (" + op.getPageName() + " - "
                        + page.getPageName() + " ) : insertion -- " + op.toString());
                    ins.execute(indexs[0], indexs[1], page);

                    return true;
                }
            } else if (op instanceof WootDel) { // del

                WootDel del = (WootDel) op;
                int idx = del.precond_v2(page);

                if (idx >= 0) {
                    this.logger.debug(this.wootEngineId + " Operation executed : deletion -- " + op.toString());
                    del.setIndexRow(idx);
                    del.execute(page);

                    return true;
                }
            }
        }
        return false;
    }

    private Clock getOpLocalClock()
    {
        return this.opLocalClock;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    public String getPage(String pageName) throws IOException
    {
        if (!this.pageExist(pageName)) {
            return "";
        }

        return this.loadPage(pageName).toHumanString();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String getPageContentModifications(String pageName) throws Exception
    {
        WootPage page = this.loadPage(pageName);

        if (page == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer("");

        for (int i = 1; i < (page.getRows().size() - 1); i++) {
            WootRow row = page.getRows().get(i);

            if (row.isVisible()) {
                sb.append("<p class=\"visibleLine\">" + row.getValue() + "</p>\n");
            } else {
                sb.append("<p class=\"invisibleLine\">" + row.getValue() + "</p>\n");
            }
        }

        return sb.toString();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String getPageToStringInternal(String pageName) throws Exception
    {
        if (!this.pageExist(pageName)) {
            return null;
        }

        return this.loadPage(pageName).toStringInternal();
    }

    // receiver interface
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    public synchronized File getState() throws IOException
    {
        // delete all existing pages
        File pagesDir = new File(this.workingDir + File.separator + "pages");
        String stateFilePath = FileUtil.zipDirectory(pagesDir.getAbsolutePath());

        if (stateFilePath == null) {
            return null;
        }

        return new File(stateFilePath);
    }

    private Pool getWaitingQueue()
    {
        return this.waitingQueue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getWootEngineId()
    {
        return this.wootEngineId;
    }

    private String getWorkingDir()
    {
        return this.workingDir;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public String[] listPages() throws Exception
    {
        File dir = new File(this.workingDir + File.separator + "pages" + File.separator);
        String[] res = dir.list();

        if (res != null) {
            for (int i = 0; i < res.length; i++) {
                res[i] = FileUtil.getDecodedFileName(res[i]);
            }
        }

        return res;
    }

    /**
     * To get the WootPage object corresponding to a serialized page
     * 
     * @param pageId : the id of the wanted page
     * @return the wanted WootPage
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws Exception : serialization exceptions && when page don't exist
     */
    public synchronized WootPage loadPage(String pageId) throws UnsupportedEncodingException, IOException
    {
        if (!this.pageExist(pageId)) {
            if (!this.pageExist(pageId)) {
                return this.createPage(pageId);
            }

        }

        String filename = FileUtil.getEncodedFileName(pageId);

        XStream xstream = new XStream(new DomDriver());

        return (WootPage) xstream.fromXML(new FileInputStream(this.getWorkingDir() + File.separator + "pages"
            + File.separator + filename));
    }

    /**
     * To get the WootPage object corresponding to a serialized page
     * 
     * @param pageId : the id of the wanted page
     * @return the wanted WootPage
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws Exception : serialization exceptions && when page don't exist
     */
    public synchronized WootPage loadCopy(String pageId) throws UnsupportedEncodingException, IOException
    {
        WootPage result = null;
        String savePageName = pageId + WootPage.SAVEDFILEEXTENSION;

        if (!this.pageExist(savePageName)) {
            if (!this.pageExist(savePageName)) {
                result=this.createPage(savePageName);
                result.setPageName(pageId);
                result.setSavedPage(true);
                return result;
                
            }

        }

        String filename = FileUtil.getEncodedFileName(savePageName);

        XStream xstream = new XStream(new DomDriver());

        result =
            (WootPage) xstream.fromXML(new FileInputStream(this.getWorkingDir() + File.separator + "pages"
                + File.separator + filename));
        result.setSavedPage(true);
        result.setPageName(pageId);
        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws UnsupportedEncodingException
     * @throws Exception DOCUMENT ME!
     */
    public boolean pageExist(String pageName) throws UnsupportedEncodingException
    {
        String filename = FileUtil.getEncodedFileName(pageName);
        File f = new File(this.workingDir + File.separator + "pages" + File.separator + filename);

        return f.exists();
    }

    private void setOpLocalClock(Clock opLocalClock)
    {
        this.opLocalClock = opLocalClock;
    }

    // state
    /**
     * DOCUMENT ME!
     * 
     * @param object DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     * @throws ZipException
     * @throws Exception DOCUMENT ME!
     */
    public synchronized boolean setState(File object) throws ZipException, IOException
    {
        if ((object != null) ) {
            ZipFile state = new ZipFile(object);

            // delete all existing pages
            File pagesDir = new File(this.workingDir + File.separator + "pages");
            /*
             * FileUtil.deleteDirectory(pagesDir); pagesDir.mkdirs();
             */
            FileUtil.unzipInDirectory(state, pagesDir.getAbsolutePath());
            this.logger.info(this.getWootEngineId() + " Receive WootEngine state");

            return true;
        }

        return false;
    }

    private void setWaitingQueue(Pool waitingQueue)
    {
        this.waitingQueue = waitingQueue;
    }

    private void setWorkingDir(String workDirectory)
    {
        this.workingDir = workDirectory;
    }

    /**
     * To serialize a WootPage object
     * 
     * @param wootPage : the object to serialize
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws Exception : serialization exceptions && when page don't exist
     */
    private void storePage(WootPage wootPage) throws FileNotFoundException, UnsupportedEncodingException
    {
        XStream xstream = new XStream(new DomDriver());

        OutputStreamWriter osw =
            new OutputStreamWriter(new FileOutputStream(this.getWorkingDir() + File.separator + "pages"
                + File.separator + wootPage.getFileName()), Charset.forName(System.getProperty("file.encoding")));
        PrintWriter output = new PrintWriter(osw);

        output.print(xstream.toXML(wootPage));
        output.flush();
        output.close();
    }

    /**
     * To serialize a WootPage object and run finalization
     * 
     * @param wootPage : the object to serialize
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws Exception
     * @throws Exception : serialization exceptions && when page don't exist
     */
    public synchronized void unloadPage(WootPage wootPage) throws FileNotFoundException, UnsupportedEncodingException
    {
        this.storePage(wootPage);
        System.runFinalization();
        System.gc();
    }

    /**
     * This method is called to check if waiting operations can be applied
     * 
     * @param page : the page name on which the operations have to be applied
     */
    private void waitingQueueExec(WootPage page)
    {
        this.logger.debug(this.wootEngineId + " waiting queue execution.");
      

        int i = 0;

        while (i < this.getWaitingQueue().getContent().size()) {
            WootOp op = this.getWaitingQueue().get(i);

            if (this.executeOp(op, page)) {
                this.getWaitingQueue().remove(i);
                i = 0; // rewind
                this.getWaitingQueue().storePool();
                this.logger.debug(this.wootEngineId + " Operation executed : " + op.toString());
            } else {
                this.logger.debug(this.wootEngineId + " Operation not executed :" + op.toString());
                i++;
            }
        }
    }
}
