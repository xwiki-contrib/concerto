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
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class ThomasRuleOpDel extends ThomasRuleOp
{
    /**  */
    private static final long serialVersionUID = 1645747491343982017L;

    /**
     * Creates a new ThomasRuleOpDel object.
     * 
     * @param id DOCUMENT ME!
     * @param val DOCUMENT ME!
     * @param isDeleted DOCUMENT ME!
     * @param timestampIdCreation DOCUMENT ME!
     * @param timestampModif DOCUMENT ME!
     */
    public ThomasRuleOpDel(Identifier id, Value val, boolean isDeleted, Timestamp timestampIdCreation,
        Timestamp timestampModif)
    {
        super(id, val, isDeleted, timestampIdCreation, timestampModif);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param e DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public Entry execute(Entry e)
    {
        if (e == null) {
            return (new ThomasRuleOpNew(this.getId(), this.getVal(), this.isDeleted(), this.getTimestampIdCreation(),
                this.getTimestampModif())).execute(null);
        } else if (!this.isOpTimestampsValid(e)) {
            return null;
        }

        return new Entry(this.getId(), this.getVal(), true, this.getTimestampIdCreation(), this.getTimestampModif());
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return " DelOp(" + this.getId() + "," + this.getVal() + "," + this.isDeleted() + ","
            + this.getTimestampIdCreation() + "," + this.getTimestampModif() + ")";
    }
}
