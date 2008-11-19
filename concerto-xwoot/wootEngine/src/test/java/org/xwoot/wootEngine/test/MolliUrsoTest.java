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

import java.util.Vector;

import junit.framework.Assert;

/**
 * TODO: Make a PageManagerTest class for page methods.
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MolliUrsoTest extends AbstractWootEngineTest
{
    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasic() throws Exception
    {
        WootEngine woot = this.createEngine(0);
        WootPage wp = woot.getPageManager().loadPage("page_s0");
        woot.insert(wp, "lineA", 0);
        woot.insert(wp, "lineB", 1);
        woot.insert(wp, "lineC", 2);
        woot.getPageManager().unloadPage(wp);
        Assert.assertEquals("lineA\nlineB\nlineC\n", woot.getPageManager().getPage("page_s0"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testCross() throws Exception
    {
        WootEngine site0 = this.createEngine(0);
        WootEngine site1 = this.createEngine(1);

        WootPage wp1 = site0.getPageManager().loadPage("page");
        WootPage wp2 = site1.getPageManager().loadPage("page");
        WootOp op1 = site0.insert(wp1, "Hello,", 0);
        WootOp op2 = site1.insert(wp2, " World", 0);
        site0.getPageManager().unloadPage(wp1);
        site1.getPageManager().unloadPage(wp2);

        Assert.assertEquals("Hello,\n", site0.getPageManager().getPage("page"));
        Assert.assertEquals(" World\n", site1.getPageManager().getPage("page"));

        Patch patch0 = new Patch();
        patch0.setPageName("page");

        Vector<WootOp> data0 = new Vector<WootOp>();
        data0.addElement(op2);
        patch0.setData(data0);
        site0.deliverPatch(patch0);

        Patch patch1 = new Patch();
        patch1.setPageName("page");

        Vector<WootOp> data1 = new Vector<WootOp>();
        data1.addElement(op1);
        patch1.setData(data1);
        site1.deliverPatch(patch1);

        Assert.assertEquals("Hello,\n World\n", site0.getPageManager().getPage("page"));
        Assert.assertEquals(site0.getPageManager().getPage("page"), site1.getPageManager().getPage("page"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testInsertBeginning() throws Exception
    {
        WootEngine woot = this.createEngine(0);
        WootPage wp = woot.getPageManager().loadPage("page");
        woot.insert(wp, "lineA", 0);
        woot.insert(wp, "lineB", 1);
        woot.insert(wp, "lineC", 2);
        woot.insert(wp, "---", 0);
        woot.getPageManager().unloadPage(wp);
        Assert.assertEquals("---\nlineA\nlineB\nlineC\n", woot.getPageManager().getPage("page"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testSimpleDel() throws Exception
    {
        WootEngine site0 = this.createEngine(0);
        WootPage wp1 = site0.getPageManager().loadPage("page");
        site0.insert(wp1, "Erase me ! :)", 0);
        site0.getPageManager().unloadPage(wp1);
        Assert.assertEquals("Erase me ! :)\n", site0.getPageManager().getPage("page"));
        wp1 = site0.getPageManager().loadPage("page");
        site0.delete(wp1, 0);
        site0.getPageManager().unloadPage(wp1);
        // TODO faire un meilleur getPage pour tester les tombstones
        // assertEquals("Erase me ! :)]", page.toString());
        Assert.assertEquals("", site0.getPageManager().getPage("page"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testTP2() throws Exception
    {
        WootEngine site0 = this.createEngine(0);
        WootEngine site1 = this.createEngine(1);
        WootEngine site2 = this.createEngine(2);
        WootPage wp = site0.getPageManager().loadPage("index");
        WootOp op0 = site0.insert(wp, "a", 0);
        WootOp op1 = site0.insert(wp, "b", 1);
        WootOp op2 = site0.insert(wp, "c", 2);
        site0.getPageManager().unloadPage(wp);
        Assert.assertEquals("a\nb\nc\n", site0.getPageManager().getPage("index"));

        Patch patch = new Patch();
        Vector<WootOp> data = new Vector<WootOp>();
        patch.setPageName("index");
        data.addElement(op0);
        data.addElement(op1);
        data.addElement(op2);
        patch.setData(data);

        site1.deliverPatch(patch);
        site2.deliverPatch(patch);

        Assert.assertEquals(site0.getPageManager().getPage("index"), site1.getPageManager().getPage("index"));
        Assert.assertEquals(site1.getPageManager().getPage("index"), site2.getPageManager().getPage("index"));

        wp = site0.getPageManager().loadPage("index");
        WootPage wp2 = site1.getPageManager().loadPage("index");
        WootPage wp3 = site2.getPageManager().loadPage("index");
        WootOp op3 = site0.insert(wp, "y", 2);
        WootOp op4 = site1.delete(wp2, 2);
        WootOp op5 = site2.insert(wp3, "x", 3);
        site0.getPageManager().unloadPage(wp);
        site1.getPageManager().unloadPage(wp2);
        site2.getPageManager().unloadPage(wp3);

        // crux
        data.clear();
        data.addElement(op5);
        patch.setData(data);
        site1.deliverPatch(patch);

        data.clear();
        data.addElement(op4);
        patch.setData(data);
        site2.deliverPatch(patch);

        Assert.assertEquals(site1.getPageManager().getPage("index"), site2.getPageManager().getPage("index"));

        // op3 descent
        data.clear();
        data.addElement(op3);
        patch.setData(data);
        site1.deliverPatch(patch);

        site2.deliverPatch(patch);

        Assert.assertEquals(site1.getPageManager().getPage("index"), site2.getPageManager().getPage("index"));

        data.clear();
        data.addElement(op4);
        patch.setData(data);
        site0.deliverPatch(patch);

        data.clear();
        data.addElement(op5);
        patch.setData(data);
        site0.deliverPatch(patch);

        Assert.assertEquals(site0.getPageManager().getPage("index"), site1.getPageManager().getPage("index"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testTPUrso() throws Exception
    {
        WootEngine site0 = this.createEngine(0);
        WootEngine site1 = this.createEngine(1);
        WootEngine site2 = this.createEngine(2);

        WootPage wp = site0.getPageManager().loadPage("index");
        WootOp op0 = site0.insert(wp, "b", 0);
        WootOp op1 = site0.insert(wp, "e", 1);
        site0.getPageManager().unloadPage(wp);

        Patch patch = new Patch();
        patch.setPageName("index");

        Vector<WootOp> data = new Vector<WootOp>();
        data.addElement(op1);
        data.addElement(op0);
        patch.setData(data);

        site1.deliverPatch(patch);
        site2.deliverPatch(patch);

        wp = site0.getPageManager().loadPage("index");
        WootPage wp2 = site1.getPageManager().loadPage("index");
        WootOp op2 = site0.insert(wp, "0", 1);
        WootOp op3 = site1.insert(wp2, "1", 1);
        site0.getPageManager().unloadPage(wp);
        site1.getPageManager().unloadPage(wp2);

        data.clear();
        data.addElement(op2);
        patch.setData(data);

        site2.deliverPatch(patch);
        wp2 = site2.getPageManager().loadPage("index");
        WootOp op4 = site2.insert(wp2, "2", 1);
        site2.getPageManager().unloadPage(wp2);

        data.clear();
        data.addElement(op3);
        patch.setData(data);

        // s[2].receive(wbc.ElementAt(3));
        site2.deliverPatch(patch);

        // s[0].receive(wbc.ElementAt(3));
        site0.deliverPatch(patch);

        // s[0].receive(wbc.ElementAt(4));
        data.clear();
        data.addElement(op4);
        patch.setData(data);
        site0.deliverPatch(patch);

        // assertEquals(s[0].getWootString(), s[2].getWootString());
        Assert.assertEquals(site0.getPageManager().getPage("index"), site2.getPageManager().getPage("index"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testUpdateSameLine() throws Exception
    {
        WootEngine site0 = this.createEngine(0);
        WootEngine site1 = this.createEngine(1);

        Vector<WootOp> data = new Vector<WootOp>();
        Patch patch = new Patch();
        patch.setPageName("index");
        WootPage wp = site0.getPageManager().loadPage("index");
        WootOp op0 = site0.insert(wp, "toto titi", 0);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals("toto titi\n", site0.getPageManager().getPage("index"));

        // send op00
        data.addElement(op0);
        patch.setData(data);
        site1.deliverPatch(patch);
        Assert.assertEquals("toto titi\n", site1.getPageManager().getPage("index"));

        // update on site0
        wp = site0.getPageManager().loadPage("index");
        WootOp op00 = site0.delete(wp, 0);
        WootOp op01 = site0.insert(wp, "toto titi tata", 0);
        site0.getPageManager().unloadPage(wp);
        Assert.assertEquals("toto titi tata\n", site0.getPageManager().getPage("index"));

        // update on site1
        WootPage wp2 = site1.getPageManager().loadPage("index");
        WootOp op10 = site1.delete(wp2, 0);
        WootOp op11 = site1.insert(wp2, "toto", 0);
        site1.getPageManager().unloadPage(wp2);

        Assert.assertEquals("toto\n", site1.getPageManager().getPage("index"));

        // send op00 + op01
        data.clear();
        data.addElement(op00);
        data.addElement(op01);
        patch.setData(data);
        site1.deliverPatch(patch);

        // send op10 + op11
        data.clear();
        data.addElement(op10);
        data.addElement(op11);
        patch.setData(data);
        site0.deliverPatch(patch);

        // TODO faire un meilleur getPage
        Assert.assertEquals(site0.getPageManager().getPageToStringInternal("index"), site1.getPageManager().getPageToStringInternal("index"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWaitingQueue() throws Exception
    {
        // used site0 for creating op
        WootEngine site0 = this.createEngine(0);

        Vector<WootOp> data = new Vector<WootOp>();
        WootPage wp = site0.getPageManager().loadPage("index");
        WootOp op1 = site0.insert(wp, "lineA", 0);
        WootOp op2 = site0.insert(wp, "lineB", 1);
        WootOp op3 = site0.insert(wp, "lineC", 2);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals("lineA\nlineB\nlineC\n", site0.getPageManager().getPage("index"));

        // real test : send 2 last op without the first
        // => wootEngine must put this two last op in wating queue
        // after send the first op => woot engine can integrate
        // the 3 op in the good order
        WootEngine site1 = this.createEngine(1);
        Patch patch = new Patch();
        patch.setPageName("index");
        data.addElement(op3);
        data.addElement(op2);
        patch.setData(data);

        site1.deliverPatch(patch);
        Assert.assertEquals("", site1.getPageManager().getPage("index"));

        data.clear();
        data.addElement(op1);
        patch.setData(data);

        site1.deliverPatch(patch);
        Assert.assertEquals("lineA\nlineB\nlineC\n", site1.getPageManager().getPage("index"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWaitingQueue2() throws Exception
    {
        // used site0 for creating op
        WootEngine site0 = this.createEngine(0);

        Vector<WootOp> data = new Vector<WootOp>();
        WootPage wp = site0.getPageManager().loadPage("index");
        WootOp op1 = site0.insert(wp, "lineA", 0);
        WootOp op2 = site0.insert(wp, "lineB", 1);
        WootOp op3 = site0.insert(wp, "lineC", 2);
        site0.getPageManager().unloadPage(wp);

        Assert.assertEquals("lineA\nlineB\nlineC\n", site0.getPageManager().getPage("index"));

        // real test : send 2 last op without the first
        // => wootEngine must put this two last op in wating queue
        // after send the first op => woot engine can integrate
        // the 3 op in the good order
        WootEngine site1 = this.createEngine(1);
        Patch patch = new Patch();
        patch.setPageName("index");
        data.addElement(op3);
        data.addElement(op2);
        data.addElement(op2);
        patch.setData(data);
        site1.deliverPatch(patch);
        Assert.assertEquals("", site1.getPageManager().getPage("index"));

        data.clear();
        data.addElement(op1);
        patch.setData(data);

        site1.deliverPatch(patch);
        Assert.assertEquals("lineA\nlineB\nlineC\n", site1.getPageManager().getPage("index"));
        data.add(op2);
        patch.setData(data);
        site1.deliverPatch(patch);
        Assert.assertEquals("lineA\nlineB\nlineC\n", site1.getPageManager().getPage("index"));
    }
}
