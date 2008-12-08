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

package org.xwoot.thomasRuleEngine.op;

import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;

/**
 * Entry Creation operation.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc677">RFC677 - The Maintenance of Duplicate Databases</a>
 * @version $Id:$
 */
public class ThomasRuleOpNew extends AbstractThomasRuleOp
{
    /** Unique ID used in the serialization process. */
    private static final long serialVersionUID = -8511654120302474839L;

    /**
     * Creates a new ThomasRuleOpNew object.
     * 
     * @param id the id of the affected entry.
     * @param value the new value of the affected entry.
     * @param isDeleted the new deletion status of the affected entry.
     * @param timestampIdCreation when the affected entry was created.
     * @param timestampModif when this operation affected the entry.
     */
    public ThomasRuleOpNew(Identifier id, Value value, boolean isDeleted, Timestamp timestampIdCreation,
        Timestamp timestampModif)
    {
        super(id, value, isDeleted, timestampIdCreation, timestampModif);
    }

    /**
     * {@inheritDoc}
     * 
     * @return If this operation's timestamps are not valid when compared to the entry, null is returned.
     *         <p>
     *         Otherwise, an new entry will be returned having the {@link Entry#isDeleted()} set to false and the rest
     *         of it's data the data of this operation.
     */
    @Override
    public Entry execute(Entry from)
    {
        if (!this.isOpTimestampsValid(from)) {
            return null;
        }

        return new Entry(this.getId(), this.getValue(), false, this.getTimestampIdCreation(), this.getTimestampModif());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "New" + super.toString();
    }
}
