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

package org.xwoot.lpbcast.receiver.httpservletreceiver;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.ReceiverApi;
import org.xwoot.lpbcast.receiver.ReceiverException;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class HttpServletReceiverAPI extends HttpServlet implements ReceiverApi
{

    private static final long serialVersionUID = -3497389707414403588L;

    public static final String RECEIVERSERVLETCONTEXT = "/receiveMessage";

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public abstract Object getPeerId();

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     * @throws HttpServletReceiverException 
     */
    public void processReceiveMessage(HttpServletRequest request, HttpServletResponse response) throws
       ReceiverException
    {
        if (this.isReceiverConnected()) {
            System.out.println("Site " + this.getPeerId() + " : Receive message -");
            if (request.getParameter("test") != null) {
                System.out.println("It's a neighbor test... ");
            } else {
                ObjectInputStream ois;
                try {
                    ois = new ObjectInputStream(request.getInputStream());
                    Message message = null;

                    
                    message = (Message) ois.readObject();
                    ois.close();
                    this.receive(message);
                } catch (IOException e1) {
                   throw new HttpServletReceiverException(this.getPeerId()+" : Problem to read message from http connexion"+e1);         
                } catch (ClassNotFoundException e) { 
                    throw new HttpServletReceiverException(this.getPeerId()+" : Problem when reading message from http connexion with class cast "+e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param content DOCUMENT ME!
     * @throws HttpServletReceiverException 
     * @throws ReceiverException 
     * 
     */
    public abstract void receive(Message message) throws ReceiverException;
}
