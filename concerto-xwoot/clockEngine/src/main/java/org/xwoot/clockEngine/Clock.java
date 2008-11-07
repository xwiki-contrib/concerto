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

package org.xwoot.clockEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.xwoot.xwootUtil.FileUtil;

/**
 * Provides the clock service. It is used to associate a private clock to an object. The clock is stored in a file.
 * 
 * @version $Id$
 */
public class Clock implements Serializable
{
    /** The name of the file where to store the clock. */
    public static final String CLOCK_FILE_NAME = "clock";

    /** Unique ID for serialization. */
    private static final long serialVersionUID = -9038141018304727598L;

    /** The file path to store the clock. */
    private String clockFilePath;

    /** The value of the clock to increment. */
    private int clock;

    /**
     * Creates a new persistent clock stored in a file in the provided directory.
     * 
     * @param directoryPath where to store the clock.
     * @throws ClockException if path access problems occur.
     */
    public Clock(String directoryPath) throws ClockException
    {
        try {
            FileUtil.checkDirectoryPath(directoryPath);
        } catch (Exception e) {
            throw new ClockException("Problems while creating a Clock instance: ", e);
        }

        this.clockFilePath = directoryPath + File.separator + CLOCK_FILE_NAME;

        this.reset();
    }

    /**
     * Users <b>must<b> load clock before using this function.
     * 
     * @return the current clock value.
     * @throws PersistentClockException if problems occur.
     */
    public int getValue() throws ClockException
    {
        return this.clock;
    }

    /**
     * Removes the clock file from the system.
     */
    public void clearWorkingDir()
    {
        File file = new File(this.clockFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Deserializes the clock from the file it is stored in.
     * 
     * @return the current clock value
     * @throws ClockException if file access problems occur.
     */
    public synchronized Clock load() throws ClockException
    {
        File clockFile = new File(this.clockFilePath);

        if (!clockFile.exists()) {
            this.clock = 0;
            this.store();
        } else {

            FileInputStream fis = null;
            ObjectInputStream ois = null;

            try {
                fis = new FileInputStream(clockFile);
                ois = new ObjectInputStream(fis);

                this.clock = ((Integer) ois.readObject()).intValue();
            } catch (Exception e) {
                throw new ClockException("Problem when loading clock: ", e);
            } finally {
                try {
                    ois.close();
                    fis.close();
                } catch (Exception e) {
                    throw new ClockException("Problems while closing the file after loading the clock: ", e);
                }
            }
        }
        return this;
    }

    /**
     * @param value the clock's new value.
     * @throws ClockException if problems occur.
     */
    public void setValue(int value) throws ClockException
    {
        this.clock = value;
    }

    /**
     * Serializes the clock to file.
     * 
     * @throws ClockException if file access problems occur.
     */
    public synchronized void store() throws ClockException
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            fout = new FileOutputStream(this.clockFilePath);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(Integer.valueOf(this.clock));
            oos.flush();
        } catch (Exception e) {
            throw new ClockException("Problem when storing the clock: ", e);
        } finally {
            try {
                oos.close();
                fout.close();
            } catch (Exception e) {
                throw new ClockException("Problems closing the file after storing the clock: ", e);
            }
        }
    }

    /**
     * Resets the clock's value to 0.
     * 
     * @throws ClockException if problems occur.
     */
    public void reset() throws ClockException
    {
        this.clock = 0;
    }
}
