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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * An entry in the {@link EntriesList} that represents an executet {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp
 * ThomasRuleOp}.
 * 
 * @version $Id:$
 */
public class Entry implements Serializable
{
    /** SerialVesrionUID for serialized object. */
    private static final long serialVersionUID = 4868709501409852809L;

    /** The ID of this entry. */
    private Identifier id;

    /** The value of this entry. */
    private Value value;

    /** Marks whether this entry was deleted or not. */
    private boolean tombstone;

    /** The time the {@link ThomasRuleOp} was modified. */
    private Timestamp timestampModif;

    /** The time the {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} was created. */
    private Timestamp timestampIdCreation;

    /**
     * Creates a new Entry object.
     * 
     * @param id the ID of this entry.
     * @param value the value of this entry.
     * @param tombstone whether or not this entry was deleted.
     * @param timestampCreation the time the {@link ThomasRuleOp} was created.
     * @param timestampModif the time the {@link ThomasRuleOp} was modified.
     */
    public Entry(Identifier id, Value value, boolean tombstone, Timestamp timestampCreation, Timestamp timestampModif)
    {
        this.id = id;
        this.value = value;
        this.tombstone = tombstone;
        this.timestampIdCreation = timestampCreation;
        this.timestampModif = timestampModif;
    }

    /** @return the ID of this entry. */
    public Identifier getId()
    {
        return this.id;
    }

    /** @param id the id to set. */
    public void setId(Identifier id)
    {
        this.id = id;
    }

    /** @return the time the {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} was created. */
    public Timestamp getTimestampIdCreation()
    {
        return this.timestampIdCreation;
    }

    /**
     * @param timestampIdCreation the timestampIdCreation to set.
     * @see #getTimestampIdCreation()
     */
    public void setTimestampCreation(Timestamp timestampIdCreation)
    {
        this.timestampIdCreation = timestampIdCreation;
    }

    /** @return the time the {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} was modified. */
    public Timestamp getTimestampModif()
    {
        return this.timestampModif;
    }

    /**
     * @param timestampModif the timestampModif to set.
     * @see #getTimestampModif()
     */
    public void setTimestampModif(Timestamp timestampModif)
    {
        this.timestampModif = timestampModif;
    }

    /** @return the value of this entry. */
    public Value getValue()
    {
        return this.value;
    }

    /** @param value the value to set. */
    public void setValue(Value value)
    {
        this.value = value;
    }

    /** @return whether this entry was deleted or not. */
    public boolean isDeleted()
    {
        return this.tombstone;
    }

    /** @param tombstone mark this entry's deletion status. */
    public void setDeleted(boolean tombstone)
    {
        this.tombstone = tombstone;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String separator = ",";
        return "Entry(" + this.id + separator + this.timestampIdCreation + separator + this.timestampModif + separator
            + this.value + separator + this.tombstone + ")";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Entry)) {
            return false;
        }

        final Entry other = (Entry) obj;

        return new EqualsBuilder().append(this.id, other.id)
            .append(this.timestampIdCreation, other.timestampIdCreation).append(this.timestampModif,
                other.timestampModif).append(this.value, other.value).append(this.tombstone, other.tombstone)
            .isEquals();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.id).append(this.timestampIdCreation).append(this.timestampModif)
            .append(this.tombstone).append(this.value).toHashCode();
    }
}
