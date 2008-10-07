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
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author molli+maire
 */
public class RowTest extends AbstractWootEngineTest
{
    /**
     * Creates a new RowTest object. DOCUMENT ME!
     */
    public RowTest()
    {
        super();
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testCBCE()
    {
        Assert.assertTrue(WootRow.RB.compareTo(WootRow.RE) < 0);
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testLocalClock()
    {
        WootRow r1 = new WootRow(new WootId(1, 0), "x");
        WootRow r2 = new WootRow(new WootId(1, 1), "x");

        Assert.assertTrue(r1.compareTo(r2) < 0);
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testSiteId()
    {
        WootRow r1 = new WootRow(new WootId(1, 0), "x");
        WootRow r2 = new WootRow(new WootId(2, 0), "x");

        Assert.assertTrue(r1.compareTo(r2) < 0);
    }
}
