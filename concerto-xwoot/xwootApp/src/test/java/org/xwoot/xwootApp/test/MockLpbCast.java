package org.xwoot.xwootApp.test;

//TODO : make a correct mock : it would be better to move all mocks in xwootApp test package
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

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.sender.LpbCastAPI;
import org.xwoot.xwootApp.XWootAPI;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockLpbCast implements LpbCastAPI, Serializable
{
    /**  */
    private static final long serialVersionUID = 5995726205784210068L;

    private int round;

    private transient List<XWootAPI> receivers;

    private boolean isConnected;

    /**
     * Creates a new MockLpbCast object.
     * 
     * @param workingDirPath DOCUMENT ME!
     * @param messagesRound DOCUMENT ME!
     * @param logDelay DOCUMENT ME!
     * @param maxNeighbors DOCUMENT ME!
     * @param peerId DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public MockLpbCast(String workingDirPath, int messagesRound, int logDelay, int maxNeighbors) throws Exception
    {
        File working = new File(workingDirPath);

        if (!working.exists() && !working.mkdir()) {
            throw new RuntimeException("Can't create pages directory: " + working);
        }

        this.round = messagesRound;
        this.receivers = new ArrayList<XWootAPI>();
        this.isConnected = false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception
     */
    public boolean addNeighbor(Object from, Object neighbor)
    {
        try {
            if (neighbor == null || from == null) {
                return false;
            }
            XWootAPI rafrom = null;
            XWootAPI ran = null;

            if (from instanceof String) {
                rafrom = this.getReceiver((String) from);
                if (rafrom == null) {
                    return false;
                }
            } else {
                rafrom = (XWootAPI) from;
            }

            if (neighbor instanceof String) {
                ran = this.getReceiver((String) neighbor);
                if (ran == null) {
                    throw new Exception("Given unknown neighbor");
                }
            } else {
                ran = (XWootAPI) neighbor;
            }

            if (rafrom.getXWootPeerId().equals(ran.getXWootPeerId())) {
                return false;
            }

            this.receivers.add(ran);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public File askState(String neighbor)
    {
        try {
            // ReceiverApi r= this.getReceiver(neighbor);
            return new File("MockFile");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void connectSender()
    {
        this.isConnected = true;

    }

    public void disconnectSender()
    {
        this.isConnected = false;

    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Collection getNeighborsList()
    {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < this.receivers.size(); i++) {
            try {
                result.add(this.receivers.get(i).getXWootPeerId());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param content DOCUMENT ME!
     * @param action DOCUMENT ME!
     * @param r DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public Message getNewMessage(Object creatorPeerId, Object content, int action, int r)
    {
        if (content == null) {
            return null;
        }

        Message message = new Message();
        message.setContent(content);
        message.setOriginalPeerId(creatorPeerId);
        message.setAction(action);
        message.setRound(r);
        XWootAPI rn = (XWootAPI) this.getNeighborRandomly();
        if (rn != null) {
            message.setRandNeighbor(rn.getXWootPeerId());
        }

        return message;
    }

    private Object getNeighborRandomly()
    {

        if (this.receivers.size() == 0) {
            return null;
        }

        Random generator = new Random();
        int randomIndex = generator.nextInt(this.receivers.size());
        Object result = this.receivers.toArray()[randomIndex];

        return result;
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
     * @param message DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void gossip(Message message)
    {
        this.notifyNeighbors(message);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isNeighborsListEmpty()
    {
        return this.receivers.isEmpty();
    }

    public boolean isSenderConnected()
    {
        return this.isConnected;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param receiver DOCUMENT ME!
     * @param toSend DOCUMENT ME!
     */
    public void notifyNeighbor(XWootAPI receiver, Object toSend)
    {
        if (receiver == null) {
            return;
        }
        try {
            receiver.receiveMessage((Message) toSend);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param message DOCUMENT ME!
     */
    public void notifyNeighbors(Object message)
    {
        for (int i = 0; i < this.receivers.size(); i++) {
            try {
                this.receivers.get(i).receiveMessage((Message) message);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void processSendState(HttpServletResponse response, File state)
    {
        // TODO Auto-generated method stub

    }

    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     */
    public void removeNeighbor(Object neighbor)
    {
        this.receivers.remove(neighbor);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param originalPeerId DOCUMENT ME!
     * @param toSend DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public void sendTo(Object to, Object toSend)
    {
        if (to instanceof String) {
            try {
                this.notifyNeighbor(this.getReceiver((String) to), toSend);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.notifyNeighbor((XWootAPI) to, toSend);
        }
    }

    private XWootAPI getReceiver(String originalPeerId) throws Exception
    {
        XWootAPI result = null;
        Iterator i = this.receivers.iterator();
        while (i.hasNext()) {
            XWootAPI r = (XWootAPI) i.next();
            if (r.getXWootPeerId().equals(originalPeerId)) {
                result = r;
            }
        }
        return result;
    }

    public Neighbors getNeighbors()
    {
        return null;
    }

    public void clearWorkingDir()
    {
        // TODO Auto-generated method stub

    }

    public void processSendAE(HttpServletResponse response, Collection ae)
    {
        // TODO Auto-generated method stub

    }
}
