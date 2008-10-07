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

package org.xwoot.lpbcast.message;

import java.io.Serializable;

import org.xwoot.lpbcast.util.Guid;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class Message implements Serializable
{
    private static final long serialVersionUID = -8107239172395652489L;

    private Object id;

    private int round;

    private Object content;

    private Object originalPeerId;

    private int action;

    private Object randNeighbor;

    /**
     * Creates a new Message object.
     */
    public Message()
    {
        this.id = Guid.generateGUID(this);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getAction()
    {
        return this.action;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Object getContent()
    {
        return this.content;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Object getId()
    {
        return this.id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Object getOriginalPeerId()
    {
        return this.originalPeerId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Object getRandNeighbor()
    {
        return this.randNeighbor;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public int getRound()
    {
        return this.round;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param action DOCUMENT ME!
     */
    public void setAction(int action)
    {
        this.action = action;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param content DOCUMENT ME!
     */
    public void setContent(Object content)
    {
        this.content = content;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param originalPeerId DOCUMENT ME!
     */
    public void setOriginalPeerId(Object originalPeerId)
    {
        this.originalPeerId = originalPeerId;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param randNeighbor DOCUMENT ME!
     */
    public void setRandNeighbor(Object randNeighbor)
    {
        this.randNeighbor = randNeighbor;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param round DOCUMENT ME!
     */
    public void setRound(int round)
    {
        this.round = round;
    }

    // // sender
    // private Object originalPeer;
    //
    // public Object getOriginalPeer() {
    // return this.originalPeer;
    // }
    //
    // public void setOriginalPeer(Object originalPeer) {
    // this.originalPeer = originalPeer;
    // }
}
