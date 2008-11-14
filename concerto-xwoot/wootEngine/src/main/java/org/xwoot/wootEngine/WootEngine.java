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

import org.apache.commons.logging.LogFactory;

import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;

import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootPage;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootDel;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Manages the internal Woot state, applies patches and Woot operations.
 * 
 * @version $Id:$
 */
public class WootEngine extends LoggedWootExceptionThrower
{
    /** The name of the output file containing the zipped state. */
    public static final String STATE_FILE_NAME = "woot.zip";

    /** The directory where the WootEngine stores it's data. */
    private String workingDirPath;

    /** An internal {@link Clock} engine required by the Woot algorithm. */
    private Clock opLocalClock;

    /**
     * A waiting queue for WootOp elements originating from patches and that were destined for another page than the
     * page of the patch.
     */
    private Pool waitingQueue;

    /** Handles WootPages for the internal WootEngine model. */
    private PageManager pageManager;

    /**
     * Creates a new WootEngine object.
     * 
     * @param siteId Unique identifier of the wanted component.
     * @param workingDir Directory with read/write access to serialize content.
     * @param opClock {@link Clock} engine component instance.
     * @throws WootEngineException if problems related to directory access occur.
     */
    public WootEngine(int siteId, String workingDir, Clock opClock) throws WootEngineException
    {
        this.wootEngineId = siteId;
        this.logger = LogFactory.getLog(this.getClass());

        this.setWorkingDir(workingDir);
        this.createWorkingDir();

        this.setOpLocalClock(opClock);
        this.setWaitingQueue(new Pool(workingDir));
        this.setPageManager(new PageManager(siteId, workingDir));

        this.logger.info(this.wootEngineId + " - WootEngine created.");
    }

    /**
     * Create the working directories and init the pagesDir field.
     * 
     * @throws WootEngineException if file access problems occur.
     * @see FileUtil#checkDirectoryPath(String)
     */
    private void createWorkingDir() throws WootEngineException
    {
        try {
            FileUtil.checkDirectoryPath(workingDirPath);
        } catch (Exception e) {
            this.throwLoggedException("Problems creating workingDir.", e);
        }
    }

    /**
     * Deletes and reinitializes the contents of the working dir.
     * 
     * @throws WootEngineException if problems occur while recreating the working directory's structure.
     * @see #createWorkingDir()
     */
    public void clearWorkingDir() throws WootEngineException
    {
        File dir = new File(this.workingDirPath);
        if (dir.exists()) {
            FileUtil.deleteDirectory(dir);
        }

        this.createWorkingDir();

        this.getPageManager().clearWorkingDir();
        this.getWaitingQueue().initializePool(true);
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
     * Delete from the WootPage an atomic value at a given position.
     * 
     * @param page the page from which to delete.
     * @param position the position (WootRow) to delete. (starting from 0)
     * @return the corresponding Woot operation that has been applied.
     * @throws WootEngineException if the given position is invalid or if problems occurred with the operations Clock.
     */
    public WootDel del(WootPage page, int position) throws WootEngineException
    {
        if ((position >= 0) && (position < page.size())) {
            // FIXME: position + 1 ?
            int deleteIndex = page.indexOfVisible(position + 1);
            WootRow deleteRow = page.elementAt(deleteIndex);

            if (!deleteRow.equals(WootRow.RE)) {
                WootDel deleteOperation = new WootDel(deleteRow.getWootId());

                int currentClockValue;
                try {
                    currentClockValue = this.getOpLocalClock().getValue();
                    deleteOperation.setOpid(new WootId(this.wootEngineId, currentClockValue));

                    this.getOpLocalClock().tick();
                } catch (ClockException e) {
                    this.throwLoggedException("Problem with the clock.", e);
                }

                deleteOperation.setPageName(page.getPageName());
                deleteOperation.setIndexRow(deleteIndex);
                deleteOperation.execute(page);

                this.logger.debug(this.wootEngineId + " Operation executed : " + deleteOperation.toString());

                return deleteOperation;
            }
        }

        this.throwLoggedException("Invalid delete position " + position + " for the page " + page.getPageName());

        // never reachable
        return null;
    }

    /**
     * Inserts in the WootPage a String value at a given position.
     * 
     * @param page the page in which to insert.
     * @param value the value to insert.
     * @param position the position (WootRow) where to insert. (starting from 0)
     * @return the corresponding Woot operation that has been applied.
     * @throws WootEngineException if the given position is invalid or if problems occurred with the operations Clock.
     */
    public WootIns ins(WootPage page, String value, int position) throws WootEngineException
    {
        this.logger.debug(this.wootEngineId + " - Direct insertion in " + page.getPageName() + ", value : " + value
            + ", position : " + position);

        if ((position >= 0) && (position <= page.size())) {
            int insertIndex = page.indexOfVisible(position);
            /*
             * FIXME: If position > page rows => insertion should be done at the last row, not the first. (Insertion at
             * the end of the page.) TODO: Check this.
             */
            WootRow rowBeforeInsert = (insertIndex != -1) ? page.elementAt(insertIndex) : page.elementAt(0);

            if (!rowBeforeInsert.equals(WootRow.RE)) {
                int indexAfterInsert = page.indexOfVisibleNext(insertIndex);
                // FIXME: Check if it's better to use page.size() instead of page.size() + 1.
                WootRow rowAfterInsert =
                    (indexAfterInsert != -1) ? page.elementAt(indexAfterInsert) : page.elementAt(page.size() + 1);

                int degreeC = 1;
                degreeC +=
                    ((rowBeforeInsert.getDegree() >= rowAfterInsert.getDegree()) ? rowBeforeInsert.getDegree()
                        : rowAfterInsert.getDegree());

                try {
                    int currentClockValue = this.getOpLocalClock().getValue();
                    WootRow newRowToInsert =
                        new WootRow(new WootId(this.wootEngineId, currentClockValue), value, degreeC);

                    WootIns insertOperation =
                        new WootIns(newRowToInsert, rowBeforeInsert.getWootId(), rowAfterInsert.getWootId());
                    // FIXME: Reuse the wootId from the row for the operation?
                    insertOperation.setOpid(new WootId(this.wootEngineId, currentClockValue));
                    insertOperation.setPageName(page.getPageName());

                    this.getOpLocalClock().tick();

                    insertOperation.execute(page);
                    this.logger.debug(this.wootEngineId + " - Operation executed :  " + insertOperation.toString());

                    return insertOperation;
                } catch (ClockException e) {
                    this.throwLoggedException("Problems with the clock.", e);
                }
            }
        }

        this.throwLoggedException("Invalid insert position " + position + " for page " + page.getPageName());

        // never reachable.
        return null;
    }

    /**
     * Applies a given Woot Operation on a page.
     * 
     * @param operation the operation to apply.
     * @param page the page on which to apply the operation.
     * @return true if the operation has been applied, false otherwise or if the operation was not indented for the
     *         specified page.
     */
    private boolean executeOp(WootOp operation, WootPage page)
    {
        // FIXME: Rewrite this section after refactoring wootEndigne.op package.
        if (!operation.getPageName().equals(page.getPageName())) {
            return false;
        }

        synchronized (page) {
            if (operation instanceof WootIns) {

                WootIns ins = (WootIns) operation;
                int[] indexs = new int[2];
                indexs = ins.precond_v2(page);

                // In case of an op reception after a setState containing this op
                if (page.indexOfId(ins.getNewRow().getWootId()) >= 0) {

                    this.logger.debug(this.wootEngineId
                        + " - Operation not executed because it was already executed during a state transfert. -- "
                        + ins.getNewRow().getWootId());

                    return true;
                } else if (indexs != null) {
                    // execute the operation
                    ins.execute(indexs[0], indexs[1], page);

                    this.logger.debug(this.wootEngineId + " - Operation executed  (" + operation.getPageName()
                        + "  -  " + page.getPageName() + ")  : " + operation.toString());

                    return true;
                }
            } else if (operation instanceof WootDel) {

                WootDel deleteOperation = (WootDel) operation;
                int deleteRowIndex = deleteOperation.precond_v2(page);

                if (deleteRowIndex >= 0) {

                    deleteOperation.setIndexRow(deleteRowIndex);
                    deleteOperation.execute(page);

                    this.logger.debug(this.wootEngineId + " - Operation executed (" + operation.getPageName() + " - "
                        + page.getPageName() + ") : " + operation.toString());

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Applies a received patch.
     * <p>
     * If the page this patch for does not exist in the model, it will be created and added.
     * <p>
     * If operations in this patch can not be applied, they will be added to a waiting queue.
     * 
     * @param patch the patch to apply.
     * @throws WootEngineException if problems accessing or creating the page or working with the waiting queue occur.
     */
    public synchronized void deliverPatch(Patch patch) throws WootEngineException
    {
        this.logger.info(this.wootEngineId + " - Reception of a new patch for page : " + patch.getPageName());
        this.logger.debug(this.wootEngineId + " - Patch contents : " + patch.toString());

        String pageName = patch.getPageName();

        WootPage page = null;

        if (!this.getPageManager().pageExists(pageName)) {
            page = this.getPageManager().createPage(pageName);
        } else {
            page = this.getPageManager().loadPage(pageName);
        }

        this.logger.debug(this.wootEngineId + " - Execution of patch operations...");

        this.getWaitingQueue().loadPool();
        for (Object obj : patch.getData()) {
            WootOp op = (WootOp) obj;

            if (!this.executeOp(op, page)) {
                this.logger.debug(this.wootEngineId + " - apending to waiting queue : " + op.toString());
                this.getWaitingQueue().getContent().add(op);
            }
        }
        this.waitingQueueExec(page);
        this.getWaitingQueue().unLoadPool();
        this.getPageManager().unloadPage(page);
    }

    /**
     * This method is called to check if waiting operations can be applied.
     * <p>
     * The operations that do get executed will get removed from the waiting queue.
     * 
     * @param page the page name on which the operations have to be applied.
     * @throws WootEngineException if problems saving the pool's state occur.
     */
    private void waitingQueueExec(WootPage page) throws WootEngineException
    {
        this.logger.debug(this.wootEngineId + " - Waiting queue execution.");

        int i = 0;

        while (i < this.getWaitingQueue().getContent().size()) {
            WootOp operation = this.getWaitingQueue().get(i);

            if (this.executeOp(operation, page)) {
                this.getWaitingQueue().remove(i);
                // rewind
                i = 0;
                this.getWaitingQueue().storePool();

                this.logger.debug(this.wootEngineId + " - Operation executed : " + operation.toString());
            } else {
                i++;

                this.logger.debug(this.wootEngineId + " - Operation not executed :" + operation.toString());
            }
        }
    }

    /**
     * Computes the current state of the WootEngine as a zip archieve containing WootPages.
     * 
     * @return the location of the zip file generated. Note: The file is temporary and it is stored in the temporary
     *         directory.
     * @throws WootEngineException if problems occur in the zipping process.
     * @see FileUtil#zipDirectory(String)
     */
    public synchronized File getState() throws WootEngineException
    {
        File pagesDir = new File(this.getPageManager().getPagesDirPath());

        String stateFilePath = null;
        try {
            stateFilePath = FileUtil.zipDirectory(pagesDir.getAbsolutePath());
        } catch (IOException e) {
            this.throwLoggedException("Problems zipping the current state.", e);
        }

        if (stateFilePath == null) {
            return null;
        }

        return new File(stateFilePath);
    }

    /**
     * Replaces the current state with the one provided.
     * 
     * @param zippedStateFile the location of a zipped state that will replace the current one.
     * @return true if the process was successfully executed, false otherwise.
     * @throws WootEngineException if unzipping or I/O problems occur.
     * @see #getState()
     */
    public synchronized boolean setState(File zippedStateFile) throws WootEngineException
    {
        // FIXME: Implement fail-safe method, transaction style. If setState fails for the new state, setState should be
        // called for a backup state, previously saved.
        if (zippedStateFile != null) {

            try {
                // delete all existing pages
                File pagesDir = new File(this.getPageManager().getPagesDirPath());
                /*
                 * FIXME: actually remove current content and remake directory structure.
                 * FileUtil.deleteDirectory(pagesDir); createWorkingDir();
                 */

                FileUtil.unzipInDirectory(zippedStateFile.toString(), pagesDir.getAbsolutePath());

                this.logger.info(this.getWootEngineId() + " - Received WootEngine state.");

                return true;
            } catch (Exception e) {
                this.throwLoggedException("Problems unziping the state file " + zippedStateFile.toString(), e);
            }
        }

        return false;
    }

    /**
     * @return a waiting queue for WootOp elements originating from patches and that were destined for another page than
     *         the page of the patch.
     */
    private Pool getWaitingQueue()
    {
        return this.waitingQueue;
    }

    /**
     * @param waitingQueue the Pool object to set.
     */
    private void setWaitingQueue(Pool waitingQueue)
    {
        this.waitingQueue = waitingQueue;
    }

    /**
     * @return the directory where the WootEngine stores it's data.
     */
    public String getWorkingDir()
    {
        return this.workingDirPath;
    }

    /**
     * @param workDirectory the workDirectory to set.
     */
    private void setWorkingDir(String workDirectory)
    {
        this.workingDirPath = workDirectory;
    }

    /**
     * @return the associated {@link Clock} object.
     */
    private Clock getOpLocalClock()
    {
        return this.opLocalClock;
    }

    /**
     * @param opLocalClock the {@link Clock} object that will become associated to this instance and will be used with
     *            the {@link WootOp} operations.
     */
    private void setOpLocalClock(Clock opLocalClock)
    {
        this.opLocalClock = opLocalClock;
    }

    /**
     * Loads the clock from file.
     * 
     * @throws ClockException if problems occur.
     * @see Clock#load()
     */
    public synchronized void loadClock() throws ClockException
    {
        this.opLocalClock = this.opLocalClock.load();
    }

    /**
     * Serializes the clock to file.
     * 
     * @throws ClockException if problems occur.
     * @see Clock#store()
     */
    public synchronized void unloadClock() throws ClockException
    {
        this.opLocalClock.store();
    }

    /**
     * @return the PageManager instance responsible for WootPages handling.
     * @see WootPage
     */
    public PageManager getPageManager()
    {
        return pageManager;
    }

    /**
     * @param pageManager the pageManager to set
     */
    public void setPageManager(PageManager pageManager)
    {
        this.pageManager = pageManager;
    }

}
