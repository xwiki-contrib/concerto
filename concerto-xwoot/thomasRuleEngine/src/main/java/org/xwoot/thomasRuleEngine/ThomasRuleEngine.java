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

package org.xwoot.thomasRuleEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.thomasRuleEngine.core.EntriesList;
import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpDel;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpNew;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOpSet;

import java.io.File;

import java.util.Calendar;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class ThomasRuleEngine
{
    private int thomasRuleEngineId;

    private EntriesList entriesList;

    private final Log logger = LogFactory.getLog(this.getClass());

    public final static String TREFILENAME = "EntriesListFile";

    public final static String TRESTATEFILENAME = "tre.zip";

    /**
     * Creates a new ThomasRuleEngine object.
     * 
     * @param thomasRuleEngineId DOCUMENT ME!
     * @param WORKINGDIR DOCUMENT ME!
     */
    public ThomasRuleEngine(int thomasRuleEngineId, String workingDir)
    {
        this.thomasRuleEngineId = thomasRuleEngineId;

        File working = new File(workingDir);

        if (!working.exists()) {
            if (!working.mkdir()) {
                throw new RuntimeException("Can't create pages directory: " + working);
            }
        }

        this.entriesList = new EntriesList(workingDir, TREFILENAME + this.thomasRuleEngineId);

        this.logger.info(this.thomasRuleEngineId + " : Thomas rule engine created !");
    }

    public void clearWorkingDir()
    {
        this.entriesList.clearWorkingDir();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     * 
     */
    synchronized public Entry applyOp(ThomasRuleOp op) throws ThomasRuleEngineException 
    {
        if (op == null) {
            return null;
        }

        // execute op
        Entry result = op.execute(this.entriesList.getEntry(op.getId()));

        // add the result of op execution
        if (result != null) {
            this.entriesList.addEntry(result);

            return result;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     * 
     */
    synchronized public ThomasRuleOp getOp(Identifier id, Value value) throws ThomasRuleEngineException 
    {
        Entry e = this.entriesList.getEntry(id);

        // new
        if ((value != null) && ((e == null) || e.isDeleted())) {
            return new ThomasRuleOpNew(id, value, false, this.getTimestamp(), this.getTimestamp());
        } else if (e != null) {
            // set
            if ((value != null) && !e.isDeleted()) {
                if (!value.equals(e.getValue())) {
                    return new ThomasRuleOpSet(id, value, false, e.getTimestampIdCreation(), this.getTimestamp());
                }

                return null;
            }

            // del
            return new ThomasRuleOpDel(id, e.getValue(), true, e.getTimestampIdCreation(), this.getTimestamp());
        }

        // nothing
        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getThomasRuleEngineId()
    {
        return this.thomasRuleEngineId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Timestamp getTimestamp()
    {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();

        return new Timestamp(time, this.getThomasRuleEngineId());
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     */
    synchronized public Value getValue(Identifier id) throws ThomasRuleEngineException 
    {
        Entry e = this.entriesList.getEntry(id);

        if (e != null) {
            if (!e.isDeleted()) {
                return e.getValue();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getWorkingDir()
    {
        return this.entriesList.getWorkingDir();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param thomasRuleEngineId DOCUMENT ME!
     */
    public void setThomasRuleEngineId(int thomasRuleEngineId)
    {
        this.thomasRuleEngineId = thomasRuleEngineId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     * 
     */
    public int size() throws ThomasRuleEngineException
    {
        return this.entriesList.size();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    synchronized public String toString()
    {
        return "Id - " + this.thomasRuleEngineId + " : " + this.entriesList.toString();
    }
}
