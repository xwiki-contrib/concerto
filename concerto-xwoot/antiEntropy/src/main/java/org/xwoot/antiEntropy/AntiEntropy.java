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

package org.xwoot.antiEntropy;

import org.xwoot.antiEntropy.Log.Log;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$ This object give the antientropy service. It's used by receiver engine to response at an asked
 *          antientropy message.
 */
public class AntiEntropy
{
    /** log : the associate log */
    private Log log;

    /**
     * Creates a new AntiEntropy object.
     * 
     * @param logPath : the filepath for log persistence storage with serialization.
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning log serializing
     */
    public AntiEntropy(String logPath) throws IOException
    {
        File f = new File(logPath);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new IOException("Can't create directory: " + logPath);
            }
        } else if (!f.isDirectory()) {
            throw new IOException("given path : " + logPath + " -- is not a directory");
        } else if (!f.canWrite()) {
            throw new IOException("given path : " + logPath + " -- isn't writable");
        }
        this.log = new Log(logPath + File.separator + "log");
    }

    public void clearWorkingDir()
    {
        this.log.clearWorkingDir();
    }

    /**
     * To get diff between content of a remote log and content of the local log.Use it to get message <b>keys</b> in the
     * local log which are not in a given remote log.
     * 
     * @param site2ids : table of all remote log keys
     * @return : collection of all local keys which are not in the given table
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning log serializing
     */
    public Collection answerAntiEntropy(Object site2ids) throws IOException, ClassNotFoundException
    {
        // diff with local log
        Object[] diff = this.log.getDiffKey((Object[])site2ids);
        List<Object> toSend = new ArrayList<Object>();

        for (Object id : diff) {
            Object missingMessage = this.log.getMessage(id);
            toSend.add(missingMessage);
        }

        return toSend;
    }

    /**
     * To get the table of all message keys in log
     * 
     * @return the table of all message keys in log
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning log serializing
     */
    public Object[] getContentForAskAntiEntropy() throws IOException, ClassNotFoundException
    {
        return this.log.getMessagesId();
    }

    /**
     * To get the log
     * 
     * @return the log
     */
    public Log getLog()
    {
        return this.log;
    }

    /**
     * To add a message in log.
     * 
     * @param id : the key of the message to add
     * @param message : the message to add
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning log serializing
     */
    public void logMessage(Object id, Object message) throws IOException, ClassNotFoundException
    {
        this.log.addMessage(id, message);
    }
}
