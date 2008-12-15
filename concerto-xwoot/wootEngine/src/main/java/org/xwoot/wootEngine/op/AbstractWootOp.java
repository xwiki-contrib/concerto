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

import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootContent;

import java.io.Serializable;

/**
 * Abstract Woot Operation.
 * 
 * @version $Id$
 */
public abstract class AbstractWootOp implements WootOp, Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -4534820701699896979L;

    /** The associated WootId of this operation. */
    private WootId opId;

    /** The id of the content on which this operation will be applied. */
    private ContentId contentId;

    /** {@inheritDoc} */
    public abstract void execute(WootContent content);

    /** {@inheritDoc} */
    public abstract boolean canExecute(WootContent content);

    /** {@inheritDoc} */
    public abstract Object getAffectedRowIndexes(WootContent content);

    /** {@inheritDoc} */
    public WootId getOpId()
    {
        return this.opId;
    }

    /** {@inheritDoc} */
    public void setOpId(WootId opId)
    {
        this.opId = opId;
    }

    /** {@inheritDoc} */
    public ContentId getContentId()
    {
        return this.contentId;
    }

    /** {@inheritDoc} */
    public void setContentId(ContentId contentId)
    {
        this.contentId = contentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "siteId: " + (this.opId != null ? String.valueOf(this.opId.getSiteid()) : "null") + " opid: "
            + this.opId + " contentId: " + this.contentId;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        WootOp other = (WootOp) obj;

        return (this.opId.equals(other.getOpId()) && this.contentId.equals(this.getContentId()));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (this.opId == null ? 0 : this.opId.hashCode());
        hash = 31 * hash + (this.contentId == null ? 0 : this.contentId.hashCode());
        return hash;
    }
}
