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

package org.xwoot.thomasRuleEngine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class EntriesList
{
    private Map<Identifier, Entry> entriesList;

    private String filePath;

    private String filename;

    private String workingDir;

    /**
     * Creates a new EntriesList object.
     * 
     * @param WORKINGDIR DOCUMENT ME!
     * @param filename DOCUMENT ME!
     */
    public EntriesList(String workingDir, String filename)
    {
        if ((workingDir == null) || (filename == null)) {
            throw new NullPointerException("Parameters must not be null.");
        }

        this.workingDir = workingDir;
        this.filename = filename;
        this.filePath = this.workingDir + File.separator + this.filename;

        File working = new File(workingDir);

        if (!working.exists()) {
            throw new RuntimeException("Directory " + workingDir + " doesn't exist");
        }

        File file = new File(this.filePath);

        if (file.exists()) {
            file.delete();
        }
    }

    public void clearWorkingDir()
    {
        File f = new File(this.filePath);
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param entry DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    public void addEntry(Entry entry) throws IOException, ClassNotFoundException
    {
        if ((entry == null) || (entry.getId() == null)) {
            throw new NullPointerException("Parameters must not be null");
        }

        this.load();
        this.entriesList.put(entry.getId(), entry);
        this.store();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    public Entry getEntry(Identifier id) throws IOException, ClassNotFoundException
    {
        if (id == null) {
            throw new NullPointerException("Parameters must not be null");
        }

        this.load();

        return this.entriesList.get(id);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getWorkingDir()
    {
        return this.workingDir;
    }

    private void load() throws IOException, ClassNotFoundException
    {
        File file = new File(this.filePath);
        ObjectInputStream ois = null;

        if (!file.exists()) {
            this.entriesList = new Hashtable<Identifier, Entry>();

            return;
        }

        ois = new ObjectInputStream(new FileInputStream(file));

        this.entriesList = (Hashtable<Identifier, Entry>) ois.readObject();
        ois.close();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param id DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    public void removeEntry(Identifier id) throws IOException, ClassNotFoundException
    {
        if (id == null) {
            throw new NullPointerException("Parameters must not be null");
        }

        this.load();
        this.entriesList.remove(id);
        this.store();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     */
    public int size() throws IOException, ClassNotFoundException
    {
        this.load();

        return this.entriesList.size();
    }

    private void store() throws IOException
    {
        if (this.entriesList.isEmpty()) {
            File file = new File(this.filePath);

            if (file.exists()) {
                file.delete();
            }

            return;
        }

        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        fout = new FileOutputStream(this.filePath);
        oos = new ObjectOutputStream(fout);
        oos.writeObject(this.entriesList);
        oos.flush();
        oos.close();
        fout.close();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        try {
            this.load();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String result = "List : ";
        Set e = this.entriesList.entrySet();
        Iterator i = e.iterator();

        while (i.hasNext()) {
            Entry temp = (Entry) i.next();
            result = result + "<" + temp.toString() + ">";
        }

        return result;
    }
}
