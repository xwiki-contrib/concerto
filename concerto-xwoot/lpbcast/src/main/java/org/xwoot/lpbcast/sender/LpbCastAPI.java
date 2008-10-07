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

package org.xwoot.lpbcast.sender;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public interface LpbCastAPI
{
    /** DOCUMENT ME! */
    int LOG_OBJECT = 0;

    /** DOCUMENT ME! */
    int LOG_AND_GOSSIP_OBJECT = 1;

    /** DOCUMENT ME! */
    int ANTI_ENTROPY = 2;

    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception DOCUMENT ME!
     */
    boolean addNeighbor(Object from, Object neighbor);

    void connectSender();

    void disconnectSender();

    void clearWorkingDir() throws Exception;

    /**
     * DOCUMENT ME!
     * 
     * @param content DOCUMENT ME!
     * @param action DOCUMENT ME!
     * @param round DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    Message getNewMessage(Object creatorPeerId, Object content, int action, int round) throws IOException,
        ClassNotFoundException;

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    int getRound();

    /**
     * DOCUMENT ME!
     * 
     * @param message DOCUMENT ME!
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception DOCUMENT ME!
     */
    void gossip(Object from, Object message) throws IOException, ClassNotFoundException, URISyntaxException;

    boolean isSenderConnected();

    void processSendState(HttpServletRequest request, HttpServletResponse response, File state) throws IOException;
    void processSendAE(HttpServletRequest request, HttpServletResponse response, Collection ae) throws IOException;

    /**
     * DOCUMENT ME!
     * 
     * @param originalPeerId DOCUMENT ME!
     * @param toSend DOCUMENT ME!
     * @throws IOException
     * @throws Exception DOCUMENT ME!
     */
    void sendTo(Object to, Object toSend) throws IOException;

    Collection getNeighborsList() throws IOException, ClassNotFoundException;

    void removeNeighbor(Object neighbor) throws IOException, ClassNotFoundException;

    Neighbors getNeighbors();

}
