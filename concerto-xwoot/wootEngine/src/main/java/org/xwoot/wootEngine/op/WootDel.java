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
 * Provides the Woot operation "Delete". It is able to delete a {@link WootRow} from a {@link WootPage} by setting the
 * {@link WootRow#visible} field to false.
 * 
 * @version $Id$
 */
public class WootDel extends AbstractWootOp implements Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -780930699257733045L;

    /** The id of the {@link WootRow} to delete. */
    private WootId idOfRowToDelete;

    /**
     * Creates a new WootDel object.
     * 
     * @param idOfRowToDelete the id of the row that will be deleted.
     */
    public WootDel(WootId idOfRowToDelete)
    {
        this.idOfRowToDelete = idOfRowToDelete;
    }

    /*
     * public void execute(WootPage page) { //assert getWootPage().contains(r); //int index = getWootPage().indexOf(r);
     * //getWootPage().elementAt(index + 1).setVisible(false); int index = page.indexOf(r); page.elementAt(index +
     * 1).setVisible(false); }
     */
    /** {@inheritDoc} */
    @Override
    public void execute(WootPage page)
    {
        int indexOfRowToDelete = page.indexOfId(idOfRowToDelete);
        page.elementAt(indexOfRowToDelete).setVisible(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canExecute(WootPage page)
    {
        return page.containsById(idOfRowToDelete);
    }

    /** {@inheritDoc} */
    public Integer getAffectedRowIndexes(WootPage page)
    {
        int affectedRowIndex = page.indexOfId(idOfRowToDelete); 
        return (affectedRowIndex < 0 ? null : new Integer(affectedRowIndex));
    }

    /**
     * @return The id of the {@link WootRow} to delete.
     */
    public WootId getIdOfRowToDelete()
    {
        return this.idOfRowToDelete;
    }

    /**
     * @param idOfRowToDelete the idOfRowToDelete to set.
     * @see #getIdOfRowToDelete()
     */
    public void setIdOfRowToDelete(WootId idOfRowToDelete)
    {
        this.idOfRowToDelete = idOfRowToDelete;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return super.toString() + " delete(" + this.idOfRowToDelete + ")";
    }
}
