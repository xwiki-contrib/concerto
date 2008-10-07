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

import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class ThomasRuleOp implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 3952443924428463564L;

    private Identifier id;

    private Timestamp timestampModif;

    private Timestamp timestampIdCreation;

    private Value val;

    private boolean isDeleted;

    /**
     * Creates a new ThomasRuleOp object.
     * 
     * @param id DOCUMENT ME!
     * @param val DOCUMENT ME!
     * @param isDeleted DOCUMENT ME!
     * @param timestampIdCreation DOCUMENT ME!
     * @param timestampModif DOCUMENT ME!
     */
    public ThomasRuleOp(Identifier id, Value val, boolean isDeleted, Timestamp timestampIdCreation,
        Timestamp timestampModif)
    {
        this.id = id;
        this.val = val;
        this.isDeleted = isDeleted;
        this.timestampIdCreation = timestampIdCreation;
        this.timestampModif = timestampModif;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param from DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public abstract Entry execute(Entry from);

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
    public Value getVal()
    {
        return this.val;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isDeleted()
    {
        return this.isDeleted;
    }

    protected boolean isOpTimestampsValid(Entry e)
    {
        if (e != null) {
            // existing TimestampIdCreation > : nothing to do
            if (e.getTimestampIdCreation().compareTo(this.getTimestampIdCreation()) >= 1) {
                return false;
            }
            // existing TimestampIdCreation == :
            else if (e.getTimestampIdCreation().compareTo(this.getTimestampIdCreation()) == 0) {
                // nothing to do when existing entry modif timestamp > given om
                // modif timestamp
                if (e.getTimestampModif().compareTo(this.getTimestampModif()) > -1) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public abstract String toString();
}
