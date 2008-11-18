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
 * Handles the content of a row of text.
 * 
 * @version $Id:$
 */
@SuppressWarnings("unchecked")
public class WootRow implements Comparable, Serializable
{
    /** The first row in a page's content. */
    public static final WootRow FIRST_WOOT_ROW = new WootRow(WootId.FIRST_WOOT_ID, "[", 0);

    /** The last row in a page's content. */
    public static final WootRow LAST_WOOT_ROW = new WootRow(WootId.LAST_WOOT_ID, "]", 0);
    
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = 4769231469557831085L;

    /** The ID of this row. */
    private WootId wootId;

    /** If this row is visible or not. */
    private boolean visible;

    /** The text content of this row. */
    private String content;

    /** The degree of this row. */
    private int degree;

    /**
     * Equivalent to {@link WootRow#WootRow(WootId, String, int) WootRow(wootId, null, 0)}.
     * 
     * @param wootId the ID of this row.
     */
    public WootRow(WootId wootId)
    {
        this(wootId, null, 0);
    }

    /**
     * Equivalent to {@link WootRow#WootRow(WootId, String, int) WootRow(wootId, value, 0)}.
     * 
     * @param wootId the ID of this row.
     * @param value the content of this row.
     */
    public WootRow(WootId wootId, String value)
    {
        this(wootId, value, 0);
    }

    /**
     * Creates a new WootRow object.
     * 
     * @param wootId the ID of this row.
     * @param value the content of this row.
     * @param degree the degree of this row.
     */
    public WootRow(WootId wootId, String value, int degree)
    {
        this.wootId = wootId;
        this.content = value;
        this.degree = degree;
        
        this.visible = true;
    }

    /**
     * @return true if the current WootRow is visible, false otherwise.
     */
    public boolean isVisible()
    {
        return this.visible;
    }

    /**
     * @param visible the visible boolean value to set.
     * @see #isVisible()
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * @return the degree of this row.
     */
    public int getDegree()
    {
        return this.degree;
    }

    /**
     * @param degree the degree to set.
     * @see #getDegree()
     */
    public void setDegree(int degree)
    {
        this.degree = degree;
    }

    /**
     * @return the text content of this row.
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * @param content the value to set.
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * @return the ID of this row.
     */
    public WootId getWootId()
    {
        return this.wootId;
    }

    /**
     * @param wootId to set.
     * @see #getWootId()
     */
    public void setWid(WootId wootId)
    {
        this.wootId = wootId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String separator = ", ";
        return "(wRow " + this.wootId + separator + this.visible + separator + this.content + ")";
    }

    /** {@inheritDoc} */
    public int compareTo(Object o)
    {
        WootRow other = (WootRow) o;

        return this.getWootId().compareTo(other.getWootId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if ((o == null) || (o.getClass() != this.getClass())) {
            return false;
        }

        WootRow other = (WootRow) o;

        return this.wootId.equals(other.getWootId());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return this.wootId.hashCode();
    }

}
