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

package org.xwoot.wootEngine.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;

import java.util.List;
import java.util.Vector;

/**
 * Stress and performance tests.
 * 
 * @version $Id:$
 */
public class PerformanceTest extends AbstractWootEngineTest
{
    /**
     * Do 1000 insert operations between the default first row and the previous row inserted by the previous operation.
     * Basically, you will always be inserting on the first position.
     * <p>
     * Normally this takes about 2.4-2.5 seconds but 4 seconds should be enough on other machines too.
     * 
     * @throws Exception if problems loading/unloading pages occur.
     */
    @Test(timeout = 4000)
    public void testFlood() throws Exception
    {
        String line =
            "---------------FLOOD---------------|" + "---------------FLOOD---------------|---------------"
                + "FLOOD---------------|---------------FLOOD---------------";

        // Add a first line between the default first and last woot row.
        WootId firstRowId = new WootId(site0.getWootEngineId(), 0);
        WootIns op0 = new WootIns(new WootRow(firstRowId, line), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setPageName(pageName);
        op0.setOpId(firstRowId);

        List<WootOp> data = new Vector<WootOp>();
        data.add(op0);

        // do 1000 insert operations on the first position, relative to the previous inserted row.
        for (int i = 0; i < 1000; i++) {
            WootId previouslyAddedRowId = new WootId(site0.getWootEngineId(), i);
            WootId newRowId = new WootId(site0.getWootEngineId(), i + 1);
            WootIns op = new WootIns(new WootRow(newRowId, line), WootId.FIRST_WOOT_ID, previouslyAddedRowId);
            op.setPageName(pageName);
            op.setOpId(newRowId);
            // woot.ins("index", line, 0).toString();
            data.add(op);
        }

        Patch patch = new Patch(data, null, pageName);

        Log log = LogFactory.getLog(this.getClass());
        log.debug("Started time-consuming operation...");

        long start = System.currentTimeMillis();
        site0.deliverPatch(patch);

        // Get elapsed time in milliseconds
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;

        log.debug("Finished in: " + elapsedTimeSec + " seconds.");
    }
}
