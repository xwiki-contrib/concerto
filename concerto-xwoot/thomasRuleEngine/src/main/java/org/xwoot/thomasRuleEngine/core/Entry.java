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

package org.xwoot.thomasRuleEngine.core;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class Entry implements Serializable
{
    /**  */
    private static final long serialVersionUID = 4868709501409852809L;

    private Identifier id;

    private Value value;

    private boolean tombstone;

    private Timestamp timestampModif;

    private Timestamp timestampIdCreation;

    /**
     * Creates a new Entry object.
     * 
     * @param id DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param tombstone DOCUMENT ME!
     * @param timestampCreation DOCUMENT ME!
     * @param timestampModif DOCUMENT ME!
     */
    public Entry(Identifier id, Value value, boolean tombstone, Timestamp timestampCreation, Timestamp timestampModif)
    {
        this.id = id;
        this.value = value;
        this.tombstone = tombstone;
        this.timestampIdCreation = timestampCreation;
        this.timestampModif = timestampModif;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param obj DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final Entry other = (Entry) obj;

        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }

        if (this.timestampIdCreation == null) {
            if (other.timestampIdCreation != null) {
                return false;
            }
        } else if (!this.timestampIdCreation.equals(other.timestampIdCreation)) {
            return false;
        }

        if (this.timestampModif == null) {
            if (other.timestampModif != null) {
                return false;
            }
        } else if (!this.timestampModif.equals(other.timestampModif)) {
            return false;
        }

        if (this.tombstone != other.tombstone) {
            return false;
        }

        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!this.value.equals(other.value)) {
            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Identifier getId()
    {
        return this.id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Timestamp getTimestampIdCreation()
    {
        return this.timestampIdCreation;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Timestamp getTimestampModif()
    {
        return this.timestampModif;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Value getValue()
    {
        return this.value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
        result = (prime * result) + ((this.timestampIdCreation == null) ? 0 : this.timestampIdCreation.hashCode());
        result = (prime * result) + ((this.timestampModif == null) ? 0 : this.timestampModif.hashCode());
        result = (prime * result) + (this.tombstone ? 1231 : 1237);
        result = (prime * result) + ((this.value == null) ? 0 : this.value.hashCode());

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isDeleted()
    {
        return this.tombstone;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param tombstone DOCUMENT ME!
     */
    public void setDeleted(boolean tombstone)
    {
        this.tombstone = tombstone;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     */
    public void setId(Identifier id)
    {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param timestampIdCreation DOCUMENT ME!
     */
    public void setTimestampCreation(Timestamp timestampIdCreation)
    {
        this.timestampIdCreation = timestampIdCreation;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param timestampModif DOCUMENT ME!
     */
    public void setTimestampModif(Timestamp timestampModif)
    {
        this.timestampModif = timestampModif;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param value DOCUMENT ME!
     */
    public void setValue(Value value)
    {
        this.value = value;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return "Entry(" + this.id + "," + this.timestampIdCreation + "," + this.timestampModif + "," + this.value + ","
            + this.tombstone + ")";
    }
}
