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

package org.xwoot.thomasRuleEngine.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.thomasRuleEngine.ThomasRuleEngineException;
import org.xwoot.thomasRuleEngine.core.EntriesList;
import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.mock.MockIdentifier;
import org.xwoot.thomasRuleEngine.mock.MockValue;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class EntriesListTest
{
    private final static String FILENAME = "TREFile";

    private final static String WORKINGDIR = "/tmp/testsThomasRuleEngine";

    @BeforeClass
    public static void initFile()
    {
        if (!new File(WORKINGDIR).exists()) {
            new File(WORKINGDIR).mkdirs();
        }
    }

    @Before
    public void removeFile() throws Exception
    {
        File f1 = new File(WORKINGDIR + File.separatorChar + FILENAME);
        if (f1.exists())
            f1.delete();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertTrue(new File(WORKINGDIR).exists());
    }

    /**
     * DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     * 
     */
    @Test
    public void testAddEntry() throws ThomasRuleEngineException
    {
        // create new List
        EntriesList el = new EntriesList(WORKINGDIR, FILENAME);
        MockIdentifier id1 = new MockIdentifier("page1","Id1");
        MockValue val1 = new MockValue("value1");
        Entry entry1 = new Entry(id1, val1, false, null, null);
        // add one entry
        el.addEntry(entry1);

        File f = new File(WORKINGDIR + File.separator + FILENAME);
        // verify the creation of the file resulting of the list serialisation
        Assert.assertEquals(true, f.exists());
        // test the getEntry function
        Assert.assertEquals(entry1, el.getEntry(id1));

        // test unicity of the add
        el.addEntry(entry1);
        Assert.assertEquals(entry1, el.getEntry(id1));
        Assert.assertEquals(el.size(), 1);

        // test a second entry
        MockIdentifier id2 = new MockIdentifier("page1","Id2");
        val1.set("value2");

        Entry entry2 = new Entry(id2, val1, false, null, null);
        el.addEntry(entry2);
        Assert.assertEquals(2, el.size());
        Assert.assertEquals(2, el.getEntries("page1").size());
        Assert.assertEquals(entry2, el.getEntry(id2));
        
        // test a third in a different page entry
        MockIdentifier id3 = new MockIdentifier("page2","Id3");
        val1.set("value3");

        Entry entry3 = new Entry(id3, val1, false, null, null);
        el.addEntry(entry3);
        Assert.assertEquals(3, el.size());
        Assert.assertEquals(2, el.getEntries("page1").size());
        Assert.assertEquals(1, el.getEntries("page2").size());
        Assert.assertEquals(entry3, el.getEntry(id3));
        
        
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws IOException DOCUMENT ME!
     */
    @Test
    public void testConstructor() throws IOException
    {
        String fakeDirectory = "/this/directory/does/not/exist";
        EntriesList el = null;

        // Constructor must send an exception if the given working dir don't
        // exist
        try {
            el = new EntriesList(fakeDirectory, FILENAME);
        } catch (Exception e) {
            Assert.assertEquals(true, (e instanceof RuntimeException));
            Assert.assertEquals("Directory " + fakeDirectory + " doesn't exist", e.getMessage());
            Assert.assertNull(el);
        }

        // Constructor must make the wanted object
        el = new EntriesList(WORKINGDIR, FILENAME);
        Assert.assertNotNull(el);

        // Constructor must delete the file corresponding to the given if it
        // exist in file system
        File f = new File(WORKINGDIR + File.separator + FILENAME);
        Assert.assertEquals(true, f.createNewFile());
        Assert.assertEquals(true, f.exists());
        el = new EntriesList(WORKINGDIR, FILENAME);
        Assert.assertEquals(false, f.exists());
    }

    /**
     * DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     * 
     */
    @Test
    public void testRemoveEntry() throws ThomasRuleEngineException 
    {
        // create new List
        EntriesList el = new EntriesList(WORKINGDIR, FILENAME);
        MockIdentifier id1 = new MockIdentifier("page1","Id1");
        MockValue val1 = new MockValue("value1");
        // add one entry
        el.addEntry(new Entry(id1, val1, false, null, null));
        // test the removeEntry function
        el.removeEntry(id1);
        Assert.assertEquals(el.size(), 0);
        // test unicity of the remove
        el.removeEntry(id1);
        Assert.assertEquals(el.size(), 0);

        // no file when list size is 0
        File f = new File(WORKINGDIR + File.separator + FILENAME);
        Assert.assertEquals(false, f.exists());
    }

    /**
     * DOCUMENT ME!
     * @throws ThomasRuleEngineException 
     * 
     */
    @Test
    public void testSetEntry() throws ThomasRuleEngineException 
    {
        // create new List
        EntriesList el = new EntriesList(WORKINGDIR, FILENAME);
        MockIdentifier id1 = new MockIdentifier("page1","Id1");
        MockValue val1 = new MockValue("value1");
        // add one entry
        el.addEntry(new Entry(id1, val1, false, null, null));
        Assert.assertEquals(el.size(), 1);

        MockValue val2 = new MockValue("value2");
        Entry e2 = new Entry(id1, val2, false, null, null);
        el.addEntry(e2);
        Assert.assertEquals(el.size(), 1);
        Assert.assertEquals(e2, el.getEntry(id1));
    }
}
