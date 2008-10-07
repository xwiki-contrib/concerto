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

package org.xwoot.wootEngine.op;

import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootPage;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author molli
 */
public class WootDel extends WootOp implements Serializable
{
    private static final long serialVersionUID = -780930699257733045L;

    // private WootRow r;
    private WootId rId;

    private int indexRow;

    /**
     * Creates a new WootDel object.
     * 
     * @param rowId DOCUMENT ME!
     */
    public WootDel( /* WootPage page, */
    WootId rowId)
    {
        // this.setWootPage(wootPage);
        // this.r = getWootPage().elementAt(getWootPage().indexOfId(rowId));
        // this.r = page.elementAt(page.indexOfId(rowId));
        this.rId = rowId;
    }

    /*
     * public void execute(WootPage page) { //assert getWootPage().contains(r); //int index = getWootPage().indexOf(r);
     * //getWootPage().elementAt(index + 1).setVisible(false); int index = page.indexOf(r); page.elementAt(index +
     * 1).setVisible(false); }
     */
    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     */
    @Override
    public void execute(WootPage page)
    {
        page.elementAt(this.indexRow).setVisible(false);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getIndexRow()
    {
        return this.indexRow;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean precond(WootPage page)
    {
        // return getWootPage().contains(r);
        // return page.contains(r);
        return page.containsById(this.rId);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int precond_v2(WootPage page)
    {
        // return getWootPage().contains(r);
        return page.indexOfId(this.rId);

        // return page.contains(r);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param indexRow DOCUMENT ME!
     */
    public void setIndexRow(int indexRow)
    {
        this.indexRow = indexRow;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return super.toString() + "del(" + this.rId + ")";
    }
}
