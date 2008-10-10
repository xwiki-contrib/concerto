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

package org.xwoot.clockEngine.persistent;

import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$ This class give the Persistence clock service. This is used to associate a private clock to an
 *          object. The clock is stored in a file.
 */
public class PersistentClock implements Serializable, Clock
{
    /**
     * serialVersionUID : for serialization clockFile : the filepath to store the clock clock : the value to increment
     */
    private static final long serialVersionUID = -9038141018304727598L;

    private String clockFilePath;

    private int clock;

    public static final String CLOCKFILENAME = "clock";

    /**
     * Creates a new instance of PersistentClock
     * 
     * @param clockFile : the filepath used for persistence storage
     * @throws PersistentClockException
     */
    public PersistentClock(String directoryPath) throws PersistentClockException
    {
        File f = new File(directoryPath);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new PersistentClockException("Can't create directory: " + directoryPath);
            }
        } else if (!f.isDirectory()) {
            throw new PersistentClockException("given path : " + directoryPath + " -- is not a directory");
        } else if (!f.canWrite()) {
            throw new PersistentClockException("given path : " + directoryPath + " -- isn't writable");
        }

        this.clockFilePath = directoryPath + File.separator + "clock";
    }

    /**
     * to get current clock value. User have to load clock before using this function.
     * 
     * @return current clock value
     * @throws PersistentClockException
     */
    public int getValue() throws PersistentClockException
    {
        return this.clock;
    }

    public void clearWorkingDir()
    {
        File f = new File(this.clockFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * To load clock file in memory
     * 
     * @return
     * @return the current clock value
     * @throws PersistentClockException
     */
    public synchronized PersistentClock load() throws PersistentClockException
    {
        File clockFile = new File(this.clockFilePath);

        if (!clockFile.exists()) {
            this.clock = 0;
            this.store();
        } else {
            FileInputStream fis;
            try {
                fis = new FileInputStream(clockFile);
                ObjectInputStream ois;
                ois = new ObjectInputStream(fis);
                this.clock = ((Integer) ois.readObject()).intValue();
                ois.close();
                fis.close();
            } catch (IOException e) {
                throw new PersistentClockException("Problem when loading clock", e);
            } catch (ClassNotFoundException e) {
                throw new PersistentClockException("Problem when loading clock", e);
            }
        }
        return this;
    }

    /**
     * to set current clock value.
     * 
     * @param i the new clock value
     * @throws PersistentClockException
     */
    public void setValue(int i) throws PersistentClockException
    {
        this.clock = i;
    }

    /**
     * To store current clock value
     * 
     * @throws PersistentClockException
     * 
     */
    public synchronized void store() throws PersistentClockException
    {
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(this.clockFilePath);
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(fout);
            oos.writeObject(Integer.valueOf(this.clock));
            oos.flush();
            oos.close();
            fout.close();
        } catch (IOException e) {
            throw new PersistentClockException("Problem when storing clock", e);
        }
    }

    public void reset() throws ClockException
    {
        this.clock = 0;
    }
}
