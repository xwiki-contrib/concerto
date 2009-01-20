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

import java.io.ObjectInputStream;
import java.util.Collection;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.sender.SenderException;
import org.xwoot.xwootApp.XWoot2;
import org.xwoot.xwootApp.web.XWootSite;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SendAEDiff extends HttpServlet
{
    private static final long serialVersionUID = 5009984916472092893L;

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Message[] log=null;
        Collection diff=null;
        try {
            ObjectInputStream ois=new ObjectInputStream(request.getInputStream());
            log=(Message[]) ois.readObject();
            if (log!=null){
                diff=((XWoot2)XWootSite.getInstance().getXWootEngine()).getAntiEntropy().answerAntiEntropy(log);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        if (diff!=null) {
            try {
                ((XWoot2) XWootSite.getInstance().getXWootEngine()).getSender().processSendAE(response, diff);
            } catch (SenderException e) {
                throw new ServletException(e);
            }
        }
    }
}
