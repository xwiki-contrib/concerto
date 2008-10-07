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

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$ this class embed an hashtable used to store objects. Like with a hashtable each object can be
 *          load with his key. The embedded hashtable is serialized in a given file. Log is used by antiEntropy to store
 *          generated and received messages.
 */
public class Log implements Serializable
{
    /**
     * serialVersionUID : (for serialize) log : the embedded hashtable store the objects logFilePath : the file path to
     * serialize the log
     */
    private static final long serialVersionUID = -3836149914436685731L;

    private Map<Object, Object> log;

    private String logFilePath;

    public static final String LOGFILENAME = "log";

    // constructor
    /**
     * Creates a new Log object.
     * 
     * @param logFilePath : the file path used to serialize the log
     * @throws LogException
     * @throws IOException
     */
    public Log(String directoryPath) throws IOException
    {
        File f = new File(directoryPath);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new IOException("Can't create directory: " + directoryPath);
            }
        } else if (!f.isDirectory()) {
            throw new IOException("given path : " + directoryPath + " -- is not a directory");
        } else if (!f.canWrite()) {
            throw new IOException("given path : " + directoryPath + " -- isn't writable");
        }

        this.logFilePath = directoryPath + File.separator + "clock";
    }

    public void clearWorkingDir()
    {
        File f = new File(this.logFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * To add an entry in log.
     * 
     * @param id : Key for new log entry
     * @param message : the new log entry
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning object serializing
     */
    public synchronized void addMessage(Object id, Object message) throws IOException, ClassNotFoundException
    {
        this.loadLog();
        this.log.put(id, message);
        this.storeLog();
    }

    /**
     * To remove all entries in log
     * 
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning object serializing
     */
    public void clearLog() throws IOException
    {
        this.log = new Hashtable<Object, Object>();
        this.storeLog();
    }

    /**
     * To test if a key exist in log
     * 
     * @param messageId The searched key
     * @return a boolean value
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception TODO better exception gestion ... exceptions concerning object serializing
     */
    public boolean existInLog(Object messageId) throws IOException, ClassNotFoundException
    {
        this.loadLog();

        return this.log.containsKey(messageId);
    }

    /**
     * to get an hashtable with all log entries
     * 
     * @return all entries in a hashtable
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception TODO better exception gestion ... exceptions concerning object serializing
     */
    public Map<Object, Object> getAllEntries() throws IOException, ClassNotFoundException
    {
        this.loadLog();

        return this.log;
    }

    /**
     * this function compute the diff beetween a given table ok keys and the log's keys.
     * 
     * @param site2ids a table of keys
     * @return all of log entries that associate key isn't in the given table
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception TODO better exception gestion ... exceptions concerning object serializing
     */
    public Object[] getDiffKey(Object[] site2ids) throws IOException, ClassNotFoundException
    {
        this.loadLog();

        Set diff = this.log.keySet();

        for (Object id : site2ids) {
            if (this.log.keySet().contains(id)) {
                diff.remove(id);
            }
        }

        return diff.toArray();
    }

    /**
     * To get an entry in log with his key
     * 
     * @param id : the key associate with the wanted log entry
     * @return the wanted log entry or null if key's not present in log
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception TODO better exception gestion ... exceptions concerning object serializing
     */
    public Object getMessage(Object id) throws IOException, ClassNotFoundException
    {
        this.loadLog();

        return this.log.get(id);
    }

    /**
     * To get all keys in log.
     * 
     * @return a table with all keys
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception TODO better exception gestion ... exceptions concerning object serializing
     */
    public Object[] getMessagesId() throws IOException, ClassNotFoundException
    {
        this.loadLog();

        return this.log.keySet().toArray();
    }

    // private
    // methods for persistent storage. Load the log from file.
    @SuppressWarnings("unchecked")
    private void loadLog() throws IOException, ClassNotFoundException
    {
        File logFile = new File(this.logFilePath);

        if (!logFile.exists()) {
            this.log = new Hashtable<Object, Object>();
            this.storeLog();
            return;
        }
        FileInputStream fis = new FileInputStream(logFile);
        ObjectInputStream ois = null;
        ois = new ObjectInputStream(fis);

        Map<Object, Object> readObject = (Map<Object, Object>) ois.readObject();
        this.log = readObject;
        ois.close();
        fis.close();
    }

    /**
     * to get the number of entries in log
     * 
     * @return : the number of entries in log
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws Exception : TODO better exception gestion ... exceptions concerning object serializing
     */
    public int logSize() throws IOException, ClassNotFoundException
    {
        this.loadLog();

        return this.log.size();
    }

    // private
    // methods for persistent storage. Store the log in file.
    private void storeLog() throws IOException
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
        fout = new FileOutputStream(this.logFilePath);
        oos = new ObjectOutputStream(fout);
        oos.writeObject(this.log);
        oos.flush();
        oos.close();
        fout.close();
    }
}
