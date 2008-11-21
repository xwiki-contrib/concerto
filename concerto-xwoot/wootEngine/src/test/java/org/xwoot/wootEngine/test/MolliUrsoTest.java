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

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

/**
 * Tests for the WootEngine class.
 * 
 * @version $Id:$
 */
public class MolliUrsoTest extends AbstractWootEngineTest
{
    /**
     * Test inserting three lines in a page.
     * 
     * @throws Exception if loading/unloading problems occur.
     */
    @Test
    public void testBasic() throws Exception
    {
        WootPage wootPage = site0.getPageManager().loadPage(pageName);

        site0.insert(wootPage, line1, 0);
        site0.insert(wootPage, line2, 1);
        site0.insert(wootPage, line3, 2);

        site0.getPageManager().unloadPage(wootPage);

        Assert.assertEquals(wrapStartEndMarkers(line1 + line2 + line3), site0.getPageManager()
            .getPageInternal(pageName));
    }

    /**
     * If 2 WootEngines modify the same line in the same page.
     * <p>
     * After applying the patches with insert operations, on each engine the page contains both the lines, in the same
     * order.
     * 
     * @throws Exception if problems loading/unloading the pages occur.
     */
    @Test
    public void testCross() throws Exception
    {
        WootPage wp1 = site0.getPageManager().loadPage(pageName);
        WootPage wp2 = site1.getPageManager().loadPage(pageName);

        WootOp op1 = site0.insert(wp1, line1, 0);
        WootOp op2 = site1.insert(wp2, line2, 0);
        site0.getPageManager().unloadPage(wp1);
        site1.getPageManager().unloadPage(wp2);

        Assert.assertEquals(addEndLine(line1), site0.getPageManager().getPage(pageName));
        Assert.assertEquals(addEndLine(line2), site1.getPageManager().getPage(pageName));

        // operation 2
        List<WootOp> data0 = new Vector<WootOp>();
        data0.add(op2);
        Patch patch0 = new Patch(data0, null, pageName);

        // operation 1
        List<WootOp> data1 = new Vector<WootOp>();
        data1.add(op1);
        Patch patch1 = new Patch(data1, null, pageName);

        site0.deliverPatch(patch0);
        site1.deliverPatch(patch1);

        Assert.assertEquals(wrapStartEndMarkers(line1 + line2), site0.getPageManager().getPageInternal(pageName));
        Assert.assertEquals(site0.getPageManager().getPageInternal(pageName), site1.getPageManager().getPageInternal(
            pageName));
    }

    /**
     * A page containing 3 lines gets an insert operation at position 0.
     * <p>
     * The result will have on position 0 the newly inserted line and all the other lines will be shifted below by one
     * position.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testInsertBeginning() throws Exception
    {
        WootPage wp = site0.getPageManager().loadPage(pageName);
        site0.insert(wp, line1, 0);
        site0.insert(wp, line2, 1);
        site0.insert(wp, line3, 2);
        site0.insert(wp, line4, 0);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals(wrapStartEndMarkers(line4 + line1 + line2 + line3), site0.getPageManager().getPageInternal(
            pageName));
    }

    /**
     * Delete the only line in a page.
     * <p>
     * As a result, the page should contain only the start and end row.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testSimpleDel() throws Exception
    {
        WootPage wp1 = site0.getPageManager().loadPage(pageName);
        site0.insert(wp1, line1, 0);
        site0.getPageManager().unloadPage(wp1);
        Assert.assertEquals(wrapStartEndMarkers(line1), site0.getPageManager().getPageInternal(pageName));

        wp1 = site0.getPageManager().loadPage(pageName);
        site0.delete(wp1, 0);
        site0.getPageManager().unloadPage(wp1);
        Assert.assertEquals(emptyPageContent, site0.getPageManager().getPageInternalVisible(pageName));
    }

    /**
     * Make concurrent changes on 3 engines.
     * <p>
     * In the end, all 3 engines have the same content.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testTP2() throws Exception
    {
        WootPage wp = site0.getPageManager().loadPage(pageName);
        WootOp op0 = site0.insert(wp, line1, 0);
        WootOp op1 = site0.insert(wp, line2, 1);
        WootOp op2 = site0.insert(wp, line3, 2);
        site0.getPageManager().unloadPage(wp);

        /* site0: line1 line2 line3 */

        // List<WootOp> data = Arrays.asList(new WootOp[] {op0, op1, op2});
        Patch patch = new Patch(Arrays.asList(new WootOp[] {op0, op1, op2}), null, pageName);

        site1.deliverPatch(patch);
        site2.deliverPatch(patch);

        /*
         * site0: line1 line2 line3 site1: line1 line2 line3 site2: line1 line2 line3
         */
        Assert.assertEquals(site0.getPageManager().getPage(pageName), site1.getPageManager().getPage(pageName));
        Assert.assertEquals(site1.getPageManager().getPage(pageName), site2.getPageManager().getPage(pageName));

        wp = site0.getPageManager().loadPage(pageName);
        WootPage wp2 = site1.getPageManager().loadPage(pageName);
        WootPage wp3 = site2.getPageManager().loadPage(pageName);
        WootOp op3 = site0.insert(wp, line4, 2);
        WootOp op4 = site1.delete(wp2, 2);
        WootOp op5 = site2.insert(wp3, line5, 3);
        site0.getPageManager().unloadPage(wp);
        site1.getPageManager().unloadPage(wp2);
        site2.getPageManager().unloadPage(wp3);

        // crux
        // data = Arrays.asList(new WootOp[] {op5});
        site1.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op5}), null, pageName));

        // data = Arrays.asList(new WootOp[] {op4});
        site2.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op4}), null, pageName));

        /*
         * site1: line1 line2 {deleted line3} line5 site2: line1 line2 {deleted line3} line5
         */
        Assert.assertEquals(site1.getPageManager().getPage(pageName), site2.getPageManager().getPage(pageName));

        // op3 descent
        // data = Arrays.asList(new WootOp[] {op3});
        patch.setData(Arrays.asList(new WootOp[] {op3}));

        site1.deliverPatch(patch);
        site2.deliverPatch(patch);

        /*
         * site1: line1 line2 {deleted line3} line4 line5 site2: line1 line2 {deleted line3} line4 line5
         */
        Assert.assertEquals(site1.getPageManager().getPage(pageName), site2.getPageManager().getPage(pageName));

        // data = Arrays.asList(new WootOp[] {op4});
        site0.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op4}), null, pageName));

        // data = Arrays.asList(new WootOp[] {op5});
        site0.deliverPatch(new Patch(Arrays.asList(new WootOp[] {op5}), null, pageName));

        /*
         * site0: line1 line2 {deleted line3} line4 line5 site1: line1 line2 {deleted line3} line4 line5
         */
        Assert.assertEquals(site0.getPageManager().getPage(pageName), site1.getPageManager().getPage(pageName));
    }

    /**
     * Test inserting more times on the same position.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testTPUrso() throws Exception
    {
        WootPage wp = site0.getPageManager().loadPage(pageName);
        WootOp op0 = site0.insert(wp, line1, 0);
        WootOp op1 = site0.insert(wp, line2, 1);
        site0.getPageManager().unloadPage(wp);

        List<WootOp> data = Arrays.asList(new WootOp[] {op0, op1});
        Patch patch = new Patch(data, null, pageName);

        site1.deliverPatch(patch);
        site2.deliverPatch(patch);
        /*
         * SITE0: [line1line2] SITE1: [line1line2] SITE2: [line1line2]
         */

        wp = site0.getPageManager().loadPage(pageName);
        WootPage wp2 = site1.getPageManager().loadPage(pageName);
        WootOp op2 = site0.insert(wp, line3, 1);
        WootOp op3 = site1.insert(wp2, line4, 1);
        site0.getPageManager().unloadPage(wp);
        site1.getPageManager().unloadPage(wp2);

        data = Arrays.asList(new WootOp[] {op2});
        patch.setData(data);

        site2.deliverPatch(patch);
        /*
         * SITE0: [line1line3line2] SITE1: [line1line4line2] SITE2: [line1line3line2]
         */

        wp2 = site2.getPageManager().loadPage(pageName);
        WootOp op4 = site2.insert(wp2, line5, 1);
        site2.getPageManager().unloadPage(wp2);

        data = Arrays.asList(new WootOp[] {op3});
        patch.setData(data);

        site2.deliverPatch(patch);
        site0.deliverPatch(patch);
        /*
         * SITE0: [line1line3line4line2] SITE1: [line1line4line2] SITE2: [line1line5line3line4line2]
         */

        data = Arrays.asList(new WootOp[] {op4});
        patch.setData(data);

        site0.deliverPatch(patch);
        /*
         * SITE0: [line1line5line3line4line2] SITE1: [line1line4line2] SITE2: [line1line5line3line4line2]
         */

        Assert.assertEquals(site0.getPageManager().getPage(pageName), site2.getPageManager().getPage(pageName));
    }

    /**
     * Replace the same line in each engine with different content.
     * <p>
     * As a result, the in both engines will have both new lines, one after another.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testUpdateSameLine() throws Exception
    {
        WootPage wp = site0.getPageManager().loadPage(pageName);
        WootOp op0 = site0.insert(wp, line1, 0);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals(addEndLine(line1), site0.getPageManager().getPage(pageName));

        // send op00
        List<WootOp> data = new Vector<WootOp>();
        data.add(op0);
        Patch patch = new Patch(data, null, pageName);
        site1.deliverPatch(patch);
        Assert.assertEquals(addEndLine(line1), site1.getPageManager().getPage(pageName));

        // update on site0
        wp = site0.getPageManager().loadPage(pageName);
        WootOp op00 = site0.delete(wp, 0);
        WootOp op01 = site0.insert(wp, line2, 0);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals(addEndLine(line2), site0.getPageManager().getPage(pageName));

        // update on site1
        WootPage wp2 = site1.getPageManager().loadPage(pageName);
        WootOp op10 = site1.delete(wp2, 0);
        WootOp op11 = site1.insert(wp2, line3, 0);
        site1.getPageManager().unloadPage(wp2);

        Assert.assertEquals(addEndLine(line3), site1.getPageManager().getPage(pageName));

        // send op00 + op01
        data.clear();
        data.add(op00);
        data.add(op01);
        patch.setData(data);
        site1.deliverPatch(patch);
        /*
         * Site0: [line2] Site1: [line2line3]
         */

        // send op10 + op11
        data.clear();
        data.add(op10);
        data.add(op11);
        patch.setData(data);
        site0.deliverPatch(patch);
        /*
         * Site0: [line2line3] Site1: [line2line3]
         */

        Assert.assertEquals(site0.getPageManager().getPageInternal(pageName), site1.getPageManager().getPageInternal(
            pageName));
    }

    /**
     * 3 insert operations are sent/received in the wrong order. First op 3 and op 2, then the last one.
     * <p>
     * The result must contain all 3 of them in the right order.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testWaitingQueue() throws Exception
    {
        WootPage wp = site0.getPageManager().loadPage(pageName);
        WootOp op1 = site0.insert(wp, line1, 0);
        WootOp op2 = site0.insert(wp, line2, 1);
        WootOp op3 = site0.insert(wp, line3, 2);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals(wrapStartEndMarkers(line1 + line2 + line3), site0.getPageManager()
            .getPageInternal(pageName));

        // real test : send 2 last op without the first
        // => wootEngine must put this two last op in wating queue
        // after send the first op => woot engine can integrate
        // the 3 op in the good order
        WootEngine site1 = this.createEngine(1);

        List<WootOp> data = new Vector<WootOp>();
        data.add(op3);
        data.add(op2);
        Patch patch = new Patch(data, null, pageName);

        site1.deliverPatch(patch);
        Assert.assertEquals(emptyPageContent, site1.getPageManager().getPageInternal(pageName));

        data.clear();
        data.add(op1);
        patch.setData(data);

        site1.deliverPatch(patch);
        Assert.assertEquals(wrapStartEndMarkers(line1 + line2 + line3), site1.getPageManager()
            .getPageInternal(pageName));
    }

    /**
     * Same as {@link #testWaitingQueue()} but this time operation 2 is repeated in the first patch and after all the
     * operations are applied it is sent one more time.
     * <p>
     * The result must be the same and the duplicated operation must be applied only once.
     * 
     * @throws Exception if problems loading/unloading the page occur.
     */
    @Test
    public void testWaitingQueue2() throws Exception
    {
        // used site0 for creating op
        WootEngine site0 = this.createEngine(0);

        WootPage wp = site0.getPageManager().loadPage(pageName);
        WootOp op1 = site0.insert(wp, line1, 0);
        WootOp op2 = site0.insert(wp, line2, 1);
        WootOp op3 = site0.insert(wp, line3, 2);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals(wrapStartEndMarkers(line1 + line2 + line3), site0.getPageManager()
            .getPageInternal(pageName));

        // real test : send 2 last op without the first
        // => wootEngine must put this two last op in wating queue
        // after send the first op => woot engine can integrate
        // the 3 op in the good order
        WootEngine site1 = this.createEngine(1);

        List<WootOp> data = new Vector<WootOp>();
        data.add(op3);
        data.add(op2);
        data.add(op2);
        Patch patch = new Patch(data, null, pageName);
        site1.deliverPatch(patch);

        Assert.assertEquals(emptyPageContent, site1.getPageManager().getPageInternal(pageName));

        data.clear();
        data.add(op1);
        patch.setData(data);

        site1.deliverPatch(patch);
        Assert.assertEquals(wrapStartEndMarkers(line1 + line2 + line3), site1.getPageManager()
            .getPageInternal(pageName));

        data.add(op2);
        patch.setData(data);
        site1.deliverPatch(patch);

        Assert.assertEquals(wrapStartEndMarkers(line1 + line2 + line3), site1.getPageManager()
            .getPageInternal(pageName));
    }
}
