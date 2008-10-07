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

package org.xwoot.xwootApp.web.servlets;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.receiver.httpservletreceiver.HttpServletReceiverAPI;
import org.xwoot.xwootApp.XWoot;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class ReceiveMessage extends HttpServletReceiverAPI
{
    private static final long serialVersionUID = -5054911966556831132L;

    public void connectReceiver() throws Exception
    {
        XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
    }

    public void disconnectReceiver() throws Exception
    {
        XWootSite.getInstance().getXWootEngine().disconnectFromP2PNetwork();

    }

    @Override
    public String getPeerId()
    {
        return XWootSite.getInstance().getXWootEngine().getPeerId();
    }

    public boolean isReceiverConnected()
    {
        return XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork();
    }

    @Override
    public void receive(Message message) throws IOException, ClassNotFoundException, URISyntaxException
    {
        ((XWoot) XWootSite.getInstance().getXWootEngine()).receivePatch(message);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        try {
            this.processReceiveMessage(request, response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
