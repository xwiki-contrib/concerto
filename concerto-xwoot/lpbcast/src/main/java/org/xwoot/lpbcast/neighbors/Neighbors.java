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

package org.xwoot.lpbcast.neighbors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class Neighbors
{
    private HashSet<Object> neighbors;

    private String neighborsFilePath;

    private int maxNumber;

    private Integer id;

    // constructor
    /**
     * Creates a new Neighbors object.
     * 
     * @param neighborsFilePath DOCUMENT ME!
     * @param maxNumber DOCUMENT ME!
     * 
     */
    public Neighbors(String directoryPath, int maxNumber, Integer id) throws NeighborsException
    {
        File f = new File(directoryPath);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new NeighborsException(this.id+" : Can't create directory: " + directoryPath);
            }
        } else if (!f.isDirectory()) {
            throw new NeighborsException(this.id+" : given path : " + directoryPath + " -- is not a directory");
        } else if (!f.canWrite()) {
            throw new NeighborsException(this.id+" : given path : " + directoryPath + " -- isn't writable");
        }

        this.neighborsFilePath = directoryPath + File.separator + "neighbors";
        this.maxNumber = maxNumber;
        this.id = id;
        this.neighbors = new HashSet<Object>();
        // this.loadNeighbors();
    }

    public void clearWorkingDir()
    {
        File f = new File(this.neighborsFilePath);
        if (f.exists()) {
            f.delete();
        }
    }

    // add neighbor
    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws NeighborsException 
     * 
     */
    public int addNeighbor(Object neighbor) throws NeighborsException
    {
        int result = 0;

        if (neighbor == null) {
            return -1;
        }

        this.loadNeighbors();

        if (this.neighbors.size() == this.maxNumber) {
            this.removeNeighborRandomly();
        }

        if (!this.neighbors.contains(neighbor)) {
            if (this.neighbors.size() == this.maxNumber) {
                this.removeNeighborRandomly();
            } else {
                result = 1;
            }

            this.neighbors.add(neighbor);
        } else {
            result = -1;
        }

        this.storeNeighbors();

        return result;
    }

    // remove all neighbors
    /**
     * DOCUMENT ME!
     * @throws NeighborsException 
     * 
     */
    public void clearNeighbors() throws NeighborsException
    {
        this.neighbors = new HashSet<Object>();
        this.storeNeighbors();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Integer getId()
    {
        return this.id;
    }

    // return one neighbor randomly in the neighbors table
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws NeighborsException 
     */
    public Object getNeighborRandomly() throws NeighborsException
    {
        this.loadNeighbors();

        if (this.neighbors.size() == 0) {
            return null;
        }

        Random generator = new Random();
        int randomIndex = generator.nextInt(this.neighbors.size());
        Object result = this.neighbors.toArray()[randomIndex];
        this.storeNeighbors();

        return result;
    }

    // is connected to one neighbor ?
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws NeighborsException 
     * 
     */
    public boolean isConnected() throws NeighborsException
    {
        this.loadNeighbors();

        return this.neighbors.size() > 0;
    }

    // methods for persistent storage
    @SuppressWarnings("unchecked")
    private void loadNeighbors() throws NeighborsException 
    {
        File neighborsFile = new File(this.neighborsFilePath);

        if (!neighborsFile.exists()) {
            this.neighbors = new HashSet<Object>();

            return;
        }

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(neighborsFile));
            HashSet<Object> readObject = (HashSet<Object>) ois.readObject();
            this.neighbors = readObject;
            ois.close();
        } catch (IOException e) {
            throw new NeighborsException("Problem to load neighbors file "+this.neighborsFilePath+"\n",e);
        } catch (ClassNotFoundException e) {
            throw new NeighborsException("Problem when loading neighbors file with class cast "+this.neighborsFilePath+"\n",e);
        }

      
    }

    // get neighbors list
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws NeighborsException 
     * 
     */
    public Collection neighborsList() throws NeighborsException
    {
        this.loadNeighbors();

        Collection result = (Collection) this.neighbors.clone();
        this.storeNeighbors();

        return result;
    }

    // neighbors list size
    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws NeighborsException 
     * 
     */
    public int neighborsListSize() throws NeighborsException 
    {
        this.loadNeighbors();

        int result = this.neighbors.size();
        this.storeNeighbors();

        return result;
    }

    public abstract void notifyNeighbor(Object neighbor, Object message);

    public abstract void notifyNeighbors(Object message);

    // remove neighbor
    /**
     * DOCUMENT ME!
     * 
     * @param neighbor DOCUMENT ME!
     * @throws NeighborsException 
     * 
     */
    public void removeNeighbor(Object neighbor) throws NeighborsException
    {
        this.loadNeighbors();
        this.neighbors.remove(neighbor);
        this.storeNeighbors();
    }

    // private
    // remove one neighbor ramdomly
    private void removeNeighborRandomly() throws NeighborsException
    {
        this.loadNeighbors();

        Random generator = new Random();
        int randomIndex = generator.nextInt(this.neighbors.size());
        this.neighbors.remove(this.neighbors.toArray()[randomIndex]);
        this.storeNeighbors();
    }

    private void storeNeighbors() throws NeighborsException
    {
        if (this.neighbors.isEmpty()) {
            File neighborsFile = new File(this.neighborsFilePath);

            if (neighborsFile.exists()) {
                neighborsFile.delete();
            }

            return;
        }

        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream(this.neighborsFilePath);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(this.neighbors);
            oos.flush();
            oos.close();
            fout.close();
        } catch (IOException e) {
            throw new NeighborsException(this.id+" : Problem to store neighbors file "+this.neighborsFilePath+"\n",e);
        }
       
    }
}
