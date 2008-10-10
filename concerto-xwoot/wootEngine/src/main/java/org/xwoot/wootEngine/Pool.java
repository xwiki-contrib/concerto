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

package org.xwoot.wootEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import org.xwoot.wootEngine.op.WootOp;

/**
 * DOCUMENT ME!
 * 
 * @author nabil
 */
public class Pool implements Serializable
{
    private static final long serialVersionUID = -7473219928332142278L;

    private File poolFile;

    private List<WootOp> content;

    /**
     * Creates a new instance of Pool
     * @throws WootEngineException 
     */
    public Pool(String location) throws WootEngineException
    {
        this.poolFile = new File(location);
        this.initializePool(false);
    }

    /**
     * Call it frequently after doing interaction with log file (loading the content, for example) this method let free
     * some resource by calling the garbage collecotr.
     */
    public void free()
    {
        this.getContent().clear();
        this.setContent(new ArrayList<WootOp>());
        System.runFinalization();
        System.gc();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param i DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public WootOp get(int i)
    {
        if (this.content != null) {
            return this.content.get(i);
        }

        return null;
    }

    /**
     * Return the whole content of the log
     * 
     * @return A Vector, each entry in this Vector is a {@link WootOp} instance
     */
    public List<WootOp> getContent()
    {
        return this.content;
    }

    /**
     * Give back the location of log
     * 
     * @return The abstract representation of the location for log
     */
    public File getPoolFile()
    {
        return this.poolFile;
    }

    /**
     * Initialize the log, by creating the file that will hold the serialization of the content (see also:
     * {@link #getContent()})
     * 
     * @param override Indicate if the method must override any existing log
     * @throws WootEngineException 
     */
    public void initializeLog(boolean override) throws WootEngineException
    {
        if (this.getPoolFile().exists() && override) {
            this.getPoolFile().delete();
        }

        if (!this.getPoolFile().exists()) {
            try {
                FileOutputStream fout = new FileOutputStream(this.getPoolFile());
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                List<WootOp> elem = new ArrayList<WootOp>();
                oos.writeObject(elem);
                oos.flush();
                oos.close();
            } catch (Exception e) {
               throw new WootEngineException("problem when initializing pool\n"+e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param overwrite DOCUMENT ME!
     * @throws WootEngineException 
     */
    private void initializePool(boolean overwrite) throws WootEngineException
    {
        if ((this.poolFile.exists() && overwrite) || !(this.poolFile.exists())) {
            try {
                FileOutputStream fout = new FileOutputStream(this.getPoolFile());
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                List<WootOp> elem = new ArrayList<WootOp>();
                oos.writeObject(elem);
                oos.flush();
                oos.close();
            } catch (Exception e) {
                throw new WootEngineException("problem when initializing pool\n"+e);  
            }
        }
    }

    /**
     * DOCUMENT ME!
     * @throws WootEngineException 
     */
    public synchronized void loadPool() throws WootEngineException
    {
        FileInputStream fin = null;
        ObjectInputStream ois = null;

        try {
            fin = new FileInputStream(this.getPoolFile());
            ois = new ObjectInputStream(fin);

            List<WootOp> readObject = (ArrayList<WootOp>) ois.readObject();
            this.setContent(readObject);
        } catch (Exception e) {
            throw new WootEngineException("problem when loading pool\n"+e);  
          
            // make sure the file is properly close
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    throw new WootEngineException("problem when closing pool\n"+e);  
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param i DOCUMENT ME!
     */
    public void remove(int i)
    {
        if (this.content != null) {
            this.content.remove(i);
        }
    }

    /**
     * Provide the log content
     * 
     * @param content A Vector, each entry in this Vector represent a {@link WootOp} instance
     */
    public void setContent(List<WootOp> content)
    {
        this.content = content;
    }

    /**
     * Provide the location for log
     * 
     * @param poolFile The abstract representation of the location for log
     */
    public void setPoolFile(File poolFile)
    {
        this.poolFile = poolFile;
    }

    /**
     * DOCUMENT ME!
     * @throws WootEngineException 
     */
    public synchronized final void storePool() throws WootEngineException
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            fout = new FileOutputStream(this.getPoolFile());
            oos = new ObjectOutputStream(fout);
            oos.writeObject(this.getContent());
            oos.flush();
        } catch (Exception e) {
            throw new WootEngineException("problem when storing pool\n"+e);  
           

            // make sure the file is properly close
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    throw new WootEngineException("problem when closing pool\n"+e);  
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     * @throws WootEngineException 
     */
    public synchronized final void unLoadPool() throws WootEngineException
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            fout = new FileOutputStream(this.getPoolFile());
            oos = new ObjectOutputStream(fout);
            oos.writeObject(this.getContent());
            oos.flush();
            // free the resource by calling setting the content to empty
            // and calling garbage collector
            this.free();
        } catch (Exception e) {
            throw new WootEngineException("problem when closing pool\n"+e);  
            // make sure the file is properly close
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    throw new WootEngineException("problem when closing pool\n"+e);  
                }
            }
        }
    }
}
