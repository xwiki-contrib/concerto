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
 * @author molli
 */
public class WootId implements Comparable, Serializable
{

    private static final long serialVersionUID = -471886787872933727L;

    // id of beginning
    /** DOCUMENT ME! */
    public static final WootId bwid = new WootId(-1, -1);

    // id of end char
    /** DOCUMENT ME! */
    public static final WootId ewid = new WootId(-2, -2);

    private int siteId;

    private int localClock;

    // constructor
    /**
     * Creates a new WootId object.
     * 
     * @param siteId DOCUMENT ME!
     * @param localClock DOCUMENT ME!
     */
    public WootId(int siteId, int localClock)
    {
        this.siteId = siteId;
        this.localClock = localClock;
    }

    // methods
    /**
     * DOCUMENT ME!
     * 
     * @param o DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public int compareTo(Object o)
    {
        WootId id = (WootId) o;

        if (this == WootId.bwid) {
            return -1;
        }

        if (this == WootId.ewid) {
            return 1;
        }

        if (id == WootId.bwid) {
            return 1;
        }

        if (id == WootId.ewid) {
            return -1;
        }

        if (this.siteId == id.siteId) {
            return this.localClock - id.localClock;
        }

        return this.siteId - id.siteId;
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
        // assert o instanceof WootId;
        if (!(o instanceof WootId)) {
            return false;
        }

        WootId wid = (WootId) o;

        return (this.siteId == wid.siteId) && (this.localClock == wid.localClock);
    }

    // getters
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getLocalClock()
    {
        return this.localClock;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getSiteid()
    {
        return this.siteId;
    }

    // setters
    /**
     * DOCUMENT ME!
     * 
     * @param i DOCUMENT ME!
     */
    public void setLocalClock(int i)
    {
        this.localClock = i;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     */
    public void setSiteId(int id)
    {
        this.siteId = id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return "(wid " + this.siteId + "," + this.localClock + ")";
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

}
