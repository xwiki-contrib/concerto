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
import org.xwoot.wootEngine.core.WootRow;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author molli
 */
public class WootIns extends WootOp implements Serializable
{
    private static final long serialVersionUID = -4385801023236126147L;

    private WootRow r;

    // private WootRow rp = null;
    // private WootRow rn = null;
    private WootId idRp = null;

    private WootId idRn = null;

    // private int degree;
    /**
     * Creates a new WootIns object.
     * 
     * @param r DOCUMENT ME!
     * @param idP DOCUMENT ME!
     * @param idN DOCUMENT ME!
     */
    public WootIns(WootRow r, WootId idP, WootId idN)
    {
        this.idRp = idP;
        this.idRn = idN;
        this.r = r;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param ip DOCUMENT ME!
     * @param in DOCUMENT ME!
     * @param page DOCUMENT ME!
     */
    public void execute(int ip, int in, WootPage page)
    {
        int iprev = ip;
        int inext = in;
        WootId id = this.r.getWootId();

        while (iprev < (inext - 1)) {
            int degree = page.elementAt(iprev + 1).getDegree();

            for (int i = iprev + 2; i < inext; ++i) {
                int d = page.elementAt(i).getDegree();

                if (d < degree) {
                    degree = d;
                }
            }

            for (int i = iprev + 1; i < inext; ++i) {
                if (page.elementAt(i).getDegree() == degree) {
                    int c = page.elementAt(i).getWootId().compareTo(id);

                    if (c < 0) {
                        iprev = i;
                    } else {
                        inext = i;
                    }
                }
            }
        }

        page.insert(this.r, iprev);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     */
    @Override
    public void execute(WootPage page)
    {
        // execute(page.indexOfId(this.getIdRp()),
        // page.indexOfId(this.getIdRn()), page);
        int iprev = page.indexOfId(this.idRp);
        this.execute(iprev, page.indexOfIdFrom(iprev, this.idRn), page);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootId getIdRn()
    {
        return this.idRn;
    }

    /*
     * public WootRow getPreviousRow() { return rp; } public void setPreviousRow(WootRow rp) { this.rp = rp; } public
     * WootRow getNextRow() { return rn; } public void setNextRow(WootRow rn) { this.rn = rn; }
     */
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootId getIdRp()
    {
        return this.idRp;
    }

    /*
     * public int getDegree() { return degree; } public void setDegree(int degree) { this.degree = degree; }
     */
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootRow getNewRow()
    {
        return this.r;
    }

    // List sub = getWootPage().subSeq(getPreviousRow(), getNextRow());
    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean precond(WootPage page)
    {
        // boolean b = (page.containsById(getNextRow().getWootId()) &&
        // page.containsById(getPreviousRow().getWootId()) &&
        // !(page.containsById(getNewRow().getWootId())));
        return (page.containsById(this.idRp) && page.containsById(this.idRn) && !(page.containsById(this.r.getWootId())));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int[] precond_v2(WootPage page)
    {
        // boolean b = (page.containsById(getNextRow().getWootId()) &&
        // page.containsById(getPreviousRow().getWootId()) &&
        // !(page.containsById(getNewRow().getWootId())));
        int p;

        // boolean b = (page.containsById(getNextRow().getWootId()) &&
        // page.containsById(getPreviousRow().getWootId()) &&
        // !(page.containsById(getNewRow().getWootId())));
        int n;
        p = page.indexOfId(this.getIdRp());

        if (p < 0) {
            return null;
        }

        n = page.indexOfIdFrom(p, this.getIdRn());

        if (n < 0) {
            return null;
        }

        // row already exist
        // if (page.indexOfId(getNewRow().getWootId()) >= 0) {
        // return null;
        // } else {
        int[] indexs = new int[2];
        indexs[0] = p;
        indexs[1] = n;

        return indexs;

        // }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param idRn DOCUMENT ME!
     */
    public void setIdRn(WootId idRn)
    {
        this.idRn = idRn;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param idRp DOCUMENT ME!
     */
    public void setIdRp(WootId idRp)
    {
        this.idRp = idRp;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param r DOCUMENT ME!
     */
    public void setNewRow(WootRow r)
    {
        this.r = r;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return super.toString() + "ins(" + this.getNewRow() + "," + this.getIdRp() + "," + this.getIdRn() + ")";
    }
}
