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

package org.xwoot.lpbcast.sender.httpservletlpbcast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.neighbors.NeighborsException;
import org.xwoot.lpbcast.neighbors.httpservletneighbors.HttpServletNeighbors;
import org.xwoot.lpbcast.sender.LpbCastAPI;
import org.xwoot.lpbcast.sender.SenderException;

import java.io.File;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class HttpServletLpbCast implements LpbCastAPI
{
    private int round;

    private Neighbors neighbors;

    private Integer id;

    public Integer getId()
    {
        return this.id;
    }

    protected final Log logger = LogFactory.getLog(this.getClass());

    public final static String SENDSTATECONTEXT = "/sendState.do";
    
    public final static String SENDAEDIFFCONTEXT = "/sendAEDiff.do";

    private HttpServletLpbCastState state;

    // list of all possible states
    public final HttpServletLpbCastStateConnected CONNECTED = new HttpServletLpbCastStateConnected(this);

    public final HttpServletLpbCastStateDisconnected DISCONNECTED = new HttpServletLpbCastStateDisconnected(this);

    /**
     * Creates a new LpbCast object.
     * 
     * @param workingDirPath DOCUMENT ME!
     * @param messagesRound DOCUMENT ME!
     * @param maxNeighbors DOCUMENT ME!
     * @param peerId DOCUMENT ME!
     * @throws HttpServletLpbCastException 
     */
    public HttpServletLpbCast(String workingDirPath, int messagesRound, int maxNeighbors, Integer id) throws HttpServletLpbCastException 
    {
        this.round = messagesRound;
        this.id = id;
        try {
            this.neighbors = new HttpServletNeighbors(workingDirPath, maxNeighbors, this.id);
        } catch (NeighborsException e) {
           throw new HttpServletLpbCastException(this.id+" : Problem when creating neighbors \n",e);
        }
        this.logger.info(this.id + " LPBCast created.");
        this.state = this.DISCONNECTED;
    }

    /*
     * (non-Javadoc)
     * @see org.xwoot.lpbcast.LpbCastAPI#addNeighbor(java.lang.Object)
     */
    public boolean addNeighbor(Object from, Object neighbor)
    {
        return this.state.addNeighbor(from, neighbor);
    }

    public void clearWorkingDir() 
    {
        this.neighbors.clearWorkingDir();
    }

    public void connectSender()
    {
        this.state.connectSender();
    }

    public void disconnectSender()
    {
        this.state.disconnectSender();
    }

    /*
     * (non-Javadoc)
     * @see org.xwoot.lpbcast.LpbCastAPI#getNeighbors()
     */
    public Neighbors getNeighbors()
    {
        return this.neighbors;
    }

    /*
     * (non-Javadoc)
     * @see org.xwoot.lpbcast.LpbCastAPI#getNewMessage(java.lang.Object, int, int)
     */
    public Message getNewMessage(Object creatorPeerId, Object content, int action, int r) 
    {
        this.logger.debug(this.getId() + " Creating new message to send.");

        Message result = new Message();
        result.setAction(action);
        result.setContent(content);
        result.setRound(r);
        result.setOriginalPeerId(creatorPeerId);
        try {
            result.setRandNeighbor(this.getNeighbors().getNeighborRandomly());
        } catch (NeighborsException e) {
            result.setRandNeighbor(null);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.xwoot.lpbcast.LpbCastAPI#getRound()
     */
    public int getRound()
    {
        return this.round;
    }

    /*
     * (non-Javadoc)
     * @see org.xwoot.lpbcast.LpbCastAPI#gossip(java.lang.Object)
     */
    public void gossip(Object from, Object message) throws SenderException
    {
        this.state.gossip(from, message);

    }

    public boolean isSenderConnected()
    {
        return this.state.isSenderConnected();
    }

    public void processSendState(HttpServletRequest request, HttpServletResponse response, File stateFile) throws SenderException
        
    {
        this.state.processSendState(request, response, stateFile);
    }
    
    public void processSendAE(HttpServletRequest request, HttpServletResponse response, Collection ae) throws SenderException
    
    {
        this.state.processSendAE(request, response, ae);
    }

    /*
     * (non-Javadoc)
     * @see org.xwoot.lpbcast.LpbCastAPI#sendTo(java.lang.Object, java.lang.Object)
     */
    public void sendTo(Object neighbor, Object toSend) throws SenderException
    {
        this.state.sendTo(neighbor, toSend);
    }

    // called by a state class to set new state to this connection
    protected void setState(HttpServletLpbCastState state)
    {
        this.state = state;
    }

    public Collection getNeighborsList() throws HttpServletLpbCastException
    {
        try {
            return this.neighbors.neighborsList();
        } catch (NeighborsException e) {
            throw new HttpServletLpbCastException(this.id+" : Problem to get neighbors list\n",e);
        }
    }

    public void removeNeighbor(Object neighbor) throws HttpServletLpbCastException
    {
        try {
            this.neighbors.removeNeighbor(neighbor);
        } catch (NeighborsException e) {
            throw new HttpServletLpbCastException(this.id+" : Problem to remove neighbor\n",e);
        }

    }
}
