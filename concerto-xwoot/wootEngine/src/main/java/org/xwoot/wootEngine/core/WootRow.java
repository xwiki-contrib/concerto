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

package org.xwoot.wootEngine.core;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author nabil
 */
public class WootRow implements Comparable, Serializable
{
    private static final long serialVersionUID = 4769231469557831085L;

    // begin wootRow
    /** DOCUMENT ME! */
    public final static WootRow RB = new WootRow(WootId.bwid, "[", 0);

    // end wootRow
    /** DOCUMENT ME! */
    public final static WootRow RE = new WootRow(WootId.ewid, "]", 0);

    private WootId wootId;

    private boolean visible = true;

    private String value;

    private int degree;

    /**
     * Creates a new WootRow object.
     * 
     * @param wid DOCUMENT ME!
     */
    public WootRow(WootId wid)
    {
        this.wootId = wid;
    }

    /**
     * Creates a new WootRow object.
     * 
     * @param id DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public WootRow(WootId id, String value)
    {
        this(id);
        this.value = value;
    }

    /**
     * Creates a new WootRow object.
     * 
     * @param wootId DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param degree DOCUMENT ME!
     */
    public WootRow(WootId wootId, String value, int degree)
    {
        this(wootId);
        this.value = value;
        this.degree = degree;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int compareTo(Object o)
    {
        WootRow r = (WootRow) o;

        return this.getWootId().compareTo(r.getWootId());
    }

    /**
     * DOCUMENT ME!
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object o)
    {
        // assert o instanceof WootRow;
        if (!(o instanceof WootRow)) {
            return false;
        }

        WootRow wr = (WootRow) o;

        return this.wootId.equals(wr.getWootId());
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getDegree()
    {
        return this.degree;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getValue()
    {
        return this.value;
    }

    // getters
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public WootId getWootId()
    {
        return this.wootId;
    }

    // methods
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isVisible()
    {
        return this.visible;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param degree DOCUMENT ME!
     */
    public void setDegree(int degree)
    {
        this.degree = degree;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param value DOCUMENT ME!
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param visible DOCUMENT ME!
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    // setters
    /**
     * DOCUMENT ME!
     * 
     * @param wootId DOCUMENT ME!
     */
    public void setWid(WootId wootId)
    {
        this.wootId = wootId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return "(wRow " + this.wootId + "." + this.visible + "." + this.value + ")";
    }

    @Override
    public int hashCode()
    {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

}
