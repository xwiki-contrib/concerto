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

import org.junit.Test;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;

import java.util.Vector;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PerformanceTests extends AbstractWootEngineTest
{
    /**
     * Creates a new PerformanceTests object. DOCUMENT ME!
     */
    public PerformanceTests()
    {
        super();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testFlood() throws Exception
    {
        WootEngine woot = this.createEngine(0);

        String line =
            "---------------FLOOD---------------|" + "---------------FLOOD---------------|---------------"
                + "FLOOD---------------|---------------FLOOD---------------";

        // Get current time
        Vector<WootOp> data = new Vector<WootOp>();

        WootIns op0 = new WootIns(new WootRow(new WootId(0, 0), line), new WootId(-1, -1), new WootId(-2, -2));
        op0.setPageName("0");
        op0.setOpid(new WootId(0, 0));
        data.add(op0);

        for (int i = 0; i < 1000; i++) {
            WootIns op = new WootIns(new WootRow(new WootId(0, i + 1), line), new WootId(-1, -1), new WootId(0, i));
            op.setPageName("0");
            op.setOpid(new WootId(0, i + 1));
            // woot.ins("index", line, 0).toString();
            data.add(op);
        }

        Patch p = new Patch();

        p.setData(data);
        p.setPageName("0");

        long start = System.currentTimeMillis();
        woot.deliverPatch(p);

        // Get elapsed time in milliseconds
        long elapsedTimeMillis = System.currentTimeMillis() - start;
        float elapsedTimeSec = elapsedTimeMillis / 1000F;
        System.out.println("Finished at: " + elapsedTimeSec + "Millis");
    }
}
