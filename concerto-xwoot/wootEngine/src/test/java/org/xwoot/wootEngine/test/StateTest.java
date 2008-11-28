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
import org.xwoot.wootEngine.core.WootPage;
import org.xwoot.wootEngine.op.WootOp;

import java.io.File;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import junit.framework.Assert;

/**
 * Test the state transfer.
 * 
 * @version $Id$
 */
public class StateTest extends AbstractWootEngineTest
{
    /**
     * Create a couple of pages on an engine and, from another engine, request and set that state.
     * <p>
     * Result: Both engines will contain the same pages.
     * 
     * @throws Exception if loading/unloading pages or state exchange problems occur.
     */
    @Test
    public void testCreateAndImportState() throws Exception
    {
        // create 10 pages on site 0
        int numberOfPages = 10;
        String[] pagesId = new String[numberOfPages];
        WootPage wp = null;
        for (int i = 0; i < 10; i++) {
            pagesId[i] = this.pageName + i;
            wp = this.site0.getPageManager().loadPage(pagesId[i]);
            this.site0.insert(wp, this.line1 + i, 0);
            this.site0.getPageManager().unloadPage(wp);
        }

        // export state of site 0
        File state = this.site0.getState();

        // import state in site 1
        this.site1.setState(state);

        // tests
        Assert.assertEquals(this.site0.getPageManager().listPages().length, numberOfPages);
        Assert.assertEquals(this.site0.getPageManager().listPages().length, this.site1.getPageManager().listPages().length);

        // Pick a random page from the previously generated.
        int randomPageNumber = new Random().nextInt() % numberOfPages;
        String randomPageName = this.pageName + randomPageNumber;

        // test if the contents of the pages match on both sites.
        Assert.assertEquals(this.site0.getPageManager().getPageInternal(randomPageName), this.site1.getPageManager()
            .getPageInternal(randomPageName));
    }

    /**
     * Make a state exchange and then re-send changes that are already included in the exchanged state.
     * <p>
     * As a result: The duplicate changes will get detected and discarded, both engines having the same content in the
     * end.
     * 
     * @throws Exception if loading/unloading pages or state exchange problems occur.
     */
    @Test
    public void testStateAndPool() throws Exception
    {
        // generate ops with dependencies on site 0
        WootPage wp = this.site0.getPageManager().loadPage(this.pageName);
        WootOp op1 = this.site0.insert(wp, this.line1, 0);
        WootOp op2 = this.site0.insert(wp, this.line2, 1);
        WootOp op3 = this.site0.insert(wp, this.line3, 2);
        this.site0.getPageManager().unloadPage(wp);

        // export state of site 0
        File state = this.site0.getState();

        // import state in site 1
        this.site1.setState(state);

        // tests state
        Assert.assertEquals(this.site0.getPageManager().listPages().length, this.site1.getPageManager().listPages().length);
        Assert.assertEquals(this.site0.getPageManager().getPage(this.pageName), this.site1.getPageManager().getPage(this.pageName));

        // pool simulation
        List<WootOp> data = new Vector<WootOp>();
        data.add(op3);
        data.add(op2);
        Patch patch = new Patch(data, null, this.pageName);

        this.site1.deliverPatch(patch);

        Assert.assertEquals(this.site0.getPageManager().getPageInternal(this.pageName), this.site1.getPageManager().getPageInternal(
            this.pageName));

        data.clear();
        data.add(op1);
        patch.setData(data);

        this.site1.deliverPatch(patch);

        Assert.assertEquals(this.site0.getPageManager().getPageInternal(this.pageName), this.site1.getPageManager().getPageInternal(
            this.pageName));
    }
}
