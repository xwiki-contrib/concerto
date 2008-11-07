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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides the antiEntropy service. It's used by the receiver engine to process antientropy messages.
 * 
 * @version $Id:$
 */
public class AntiEntropy
{
    /** The name of the working directory for the Log. */
    public static final String LOG_DIRECTORY_NAME = "log";
    
    /**
     * The associated log.
     * 
     * @see org.xwoot.antiEntropy.Log.Log
     **/
    private Log log;

    /**
     * Creates a new AntiEntropy object.
     * 
     * @param logPath the directory on drive for log persistence storage.
     * @throws AntiEntropyException if the log does not initialize correctly.
     */
    public AntiEntropy(String logPath) throws AntiEntropyException
    {
        this.log = new Log(logPath + File.separator + LOG_DIRECTORY_NAME);
    }

    /**
     * @see Log#clearWorkingDir()
     */
    public void clearWorkingDir()
    {
        this.log.clearWorkingDir();
    }

    /**
     * Computes the diff between content of a remote log and content of the local log by comparing keys. 
     * 
     * @param site2ids object representing an array of remote log keys that need to be checked.
     * @return collection of all local messages corresponding to the keys that are not in the given array.
     * @throws AntiEntropyException if problems while reading the log occur.
     */
    @SuppressWarnings("unchecked")
    public Collection answerAntiEntropy(Object site2ids) throws AntiEntropyException
    {
        // diff with local log
        Object[] diff = this.log.getDiffKey((Object[]) site2ids);
        List<Object> missingMessages = new ArrayList<Object>();

        for (Object id : diff) {
            Object missingMessage = this.log.getMessage(id);
            missingMessages.add(missingMessage);
        }

        return missingMessages;
    }

    /**
     * @return the table of all message keys in log
     * @throws AntiEntropyException if problems while reading the log occur.
     */
    public Object[] getMessageIdsForAskAntiEntropy() throws AntiEntropyException
    {
        return this.log.getMessageIds();
    }

    /**
     * @return the {@link Log} object
     */
    public Log getLog()
    {
        return this.log;
    }

    /**
     * Adds a message in the log.
     * 
     * @param id the id of the message
     * @param message the message
     * @throws AntiEntropyException if problems occur while handling the log.
     */
    public void logMessage(Object id, Object message) throws AntiEntropyException
    {
        this.log.addMessage(id, message);
    }
}
