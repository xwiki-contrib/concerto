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
import org.xwoot.wootEngine.core.WootPage;
import org.xwoot.wootEngine.op.WootOp;

import java.io.File;

import java.util.Vector;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class StateTest extends AbstractWootEngineTest
{
    /**
     * Creates a new StateTest object.
     */
    public StateTest()
    {
        super();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testCreateAndImportState() throws Exception
    {
        WootEngine woot0 = this.createEngine(0);
        WootEngine woot1 = this.createEngine(1);

        // create 10 pages on site 0
        int nbPages = 10;
        String[] pagesId = new String[nbPages];
        WootPage wp = null;
        for (int i = 0; i < 10; i++) {
            pagesId[i] = "page" + i;
            wp = woot0.loadPage(pagesId[i]);
            woot0.ins(wp, "" + i, 0);
            woot0.unloadPage(wp);
        }

        // export state of site 0
        File state = woot0.getState();

        // import state in site 1
        woot1.setState(state);

        // tests
        Assert.assertEquals(woot0.listPages().length, nbPages);
        Assert.assertEquals(woot0.listPages().length, woot1.listPages().length);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testStateAndPool() throws Exception
    {
        // create 2 sites
        WootEngine site0 = this.createEngine(0);
        WootEngine site1 = this.createEngine(1);

        // generate ops with dependencies on site 0
        Vector<WootOp> data = new Vector<WootOp>();
        WootPage wp = site0.loadPage("index");
        WootOp op1 = site0.ins(wp, "lineA", 0);
        WootOp op2 = site0.ins(wp, "lineB", 1);
        WootOp op3 = site0.ins(wp, "lineC", 2);
        site0.unloadPage(wp);

        // export state of site 0
        File state =  site0.getState();

        // import state in site 1
        site1.setState(state);

        // tests state
        Assert.assertEquals(site0.listPages().length, site1.listPages().length);
        Assert.assertEquals(site0.getPage("index"), site1.getPage("index"));

        // pool simulation
        Patch patch = new Patch();
        patch.setPageName("index");
        data.addElement(op3);
        data.addElement(op2);
        patch.setData(data);

        site1.deliverPatch(patch);
        // assertEquals("lineA\nlineB\nlineC\n", site1.getPage("index"));
        data.clear();
        data.addElement(op1);
        patch.setData(data);

        site1.deliverPatch(patch);
        Assert.assertEquals("lineA\nlineB\nlineC\n", site1.getPage("index"));
    }
}
