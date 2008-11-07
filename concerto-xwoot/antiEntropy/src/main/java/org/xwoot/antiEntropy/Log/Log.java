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

package org.xwoot.antiEntropy.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.xwoot.xwootUtil.FileUtil;

/**
 * This class embeds a {@link Hashtable} used to store messages having ids as keys. The embedded hashtable is serialized
 * in a given file.
 * <p>
 * Log is used by {@link org.xwoot.antiEntropy.AntiEntropy AntiEntropy} to store generated and received messages.
 * 
 * @version $Id:$
 * @see org.xwoot.antiEntropy.AntiEntropy
 */
public class Log implements Serializable
{
    /**
     * The name of the file where the hashtable will be serialized.
     */
    public static final String LOG_FILE_NAME = "log";

    /**
     * Unique ID used in the serialization process.
     */
    private static final long serialVersionUID = -3836149914436685731L;

    /**
     * The serialized hashtable storing key-value having the id as key and the message as value.
     */
    private Map<Object, Object> log;

    /**
     * The path on drive where to serialize the log.
     */
    private String logFilePath;

    /**
     * Creates a new Log object.
     * 
     * @param logFilePath the file path used to serialize the log. If it does not exist, it will be created.
     * @throws LogException if the specified path is not a writable directory.
     */
    public Log(String logFilePath) throws LogException
    {
        try {
            FileUtil.checkDirectoryPath(logFilePath);
        } catch (Exception e) {
            throw new LogException("Problems with the specified log file path: ", e);
        }

        this.logFilePath = logFilePath + File.separator + LOG_FILE_NAME;
    }

    /**
     * Deletes the working directory containing the log file.
     */
    public void clearWorkingDir()
    {
        File file = new File(this.logFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * @param id key for new log entry.
     * @param message the value of the new log entry.
     * @throws LogException if log serialization/deserialization problems occur.
     */
    public synchronized void addMessage(Object id, Object message) throws LogException
    {
        this.loadLog();
        this.log.put(id, message);
        this.storeLog();
    }

    /**
     * Removes all entries in the log.
     * 
     * @throws LogException if serialization problems occur.
     */
    public void clearLog() throws LogException
    {
        this.log = new Hashtable<Object, Object>();
        this.storeLog();
    }

    /**
     * @param messageId the searched message's id
     * @return true if the message exists in the log, false otherwise.
     * @throws LogException if deserialization problems occur.
     */
    public boolean existInLog(Object messageId) throws LogException
    {
        this.loadLog();

        return this.log.containsKey(messageId);
    }

    /**
     * @return a Map of all the entries in the log.
     * @throws LogException if deserialization problems occur.
     */
    public Map<Object, Object> getAllEntries() throws LogException
    {
        this.loadLog();

        return this.log;
    }

    /**
     * Computes the diff beetween a given table of keys and the log's keys.
     * 
     * @param site2ids an array of message ids.
     * @return an array of all the message ids, excluding the ones in the given array.
     * @throws LogException if deserialization problems occur.
     */
    public Object[] getDiffKey(Object[] site2ids) throws LogException
    {
        this.loadLog();

        Set<Object> diff = this.log.keySet();

        for (Object id : site2ids) {
            diff.remove(id);
        }

        return diff.toArray();
    }

    /**
     * @param id the key associate with the wanted log entry.
     * @return the wanted log entry or null if key's not present in log.
     * @throws LogException if deserialization problems occur.
     */
    public Object getMessage(Object id) throws LogException
    {
        this.loadLog();

        return this.log.get(id);
    }

    /**
     * @return a table with all message ids.
     * @throws LogException if deserialization problems occur.
     */
    public Object[] getMessageIds() throws LogException
    {
        this.loadLog();

        return this.log.keySet().toArray();
    }

    /**
     * Method for persistent storage. Loads the log from file.
     * 
     * @throws LogException if problems occur while accessing or reading the log from file.
     */
    @SuppressWarnings("unchecked")
    private void loadLog() throws LogException
    {
        File logFile = new File(this.logFilePath);

        if (!logFile.exists()) {
            this.log = new Hashtable<Object, Object>();
            this.storeLog();
            return;
        }

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(logFile);
            ois = new ObjectInputStream(fis);

            this.log = (Map<Object, Object>) ois.readObject();
        } catch (Exception e) {
            throw new LogException("Problem while loading log file " + this.logFilePath, e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                throw new LogException("Problem closing log file after loading " + this.logFilePath, e);
            }
        }
    }

    /**
     * @return the number of entries in log
     * @throws LogException if deserialization problems occur.
     */
    public int logSize() throws LogException
    {
        this.loadLog();

        return this.log.size();
    }

    /**
     * Method for persistent storage. Store the log on file.
     * 
     * @throws LogException if problems occur while accessing or writing the log to file.
     */
    private void storeLog() throws LogException
    {
        if (this.log.isEmpty()) {
            File logFile = new File(this.logFilePath);

            if (logFile.exists()) {
                logFile.delete();
            }

            return;
        }

        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream(this.logFilePath);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(this.log);
            oos.flush();
        } catch (Exception e) {
            throw new LogException("Problem when storing log in file " + this.logFilePath, e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception e) {
                throw new LogException("Problem closing log file after saving " + this.logFilePath, e);
            }
        }
    }
}
