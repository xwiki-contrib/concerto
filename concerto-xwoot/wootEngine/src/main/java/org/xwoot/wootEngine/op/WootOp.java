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
public abstract class WootOp implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4534820701699896979L;

    private WootId opid;

    private String pageName;

    /*
     * public int compareTo(Object o){ return opid.compareTo(((WootOp)o).getOpid()); }
     */
    /**
     * DOCUMENT ME!
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object o)
    {
        // assert o instanceof WootOp;
        if (!(o instanceof WootOp)) {
            return false;
        }

        return this.getOpid().equals(((WootOp) o).getOpid());
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     */
    abstract public void execute(WootPage page);

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootId getOpid()
    {
        return this.opid;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getPageName()
    {
        return this.pageName;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param page DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    abstract public boolean precond(WootPage page);

    /**
     * DOCUMENT ME!
     * 
     * @param opid DOCUMENT ME!
     */
    public void setOpid(WootId opid)
    {
        this.opid = opid;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pageName DOCUMENT ME!
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    /*
     * public WootPage getWootPage() { return wootPage; } public void setWootPage(WootPage wootPage) { this.wootPage =
     * wootPage; }
     */
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        String s = " siteId: " + this.opid.getSiteid() + " opid: " + this.opid;

        return s;
    }

    @Override
    public int hashCode()
    {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

}
