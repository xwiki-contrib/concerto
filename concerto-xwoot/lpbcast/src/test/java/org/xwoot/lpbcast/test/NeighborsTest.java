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

package org.xwoot.lpbcast.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.neighbors.httpservletneighbors.HttpServletNeighbors;

import java.io.File;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class NeighborsTest
{
    private String neighborsFilePath;

    private Neighbors neighbors;

    protected final static String WORKINGDIR = "/tmp/testsPbcast";

    /**
     * Creates a new LpbCastCase object.
     * 
     * @param name DOCUMENT ME!
     */
    @BeforeClass
    public static void initFile()
    {
        if (!new File(WORKINGDIR).exists()) {
            new File(WORKINGDIR).mkdirs();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        this.neighborsFilePath = WORKINGDIR + File.separator + "neighbors";
        this.neighbors = new HttpServletNeighbors(this.neighborsFilePath, 10, new Integer(0));
    }

    @After
    public void tearDown() throws Exception
    {
        this.neighbors.clearNeighbors();
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 0);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testInitFile() throws Exception
    {
        Assert.assertTrue(new File(WORKINGDIR).exists());
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testAdd() throws Exception
    {
        this.neighbors.addNeighbor("neighbor 0");
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 1);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testInit() throws Exception
    {
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 0);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testIsConnected() throws Exception
    {
        Assert.assertFalse(this.neighbors.isConnected());

        this.neighbors.addNeighbor("neighbor 0");
        Assert.assertTrue(this.neighbors.isConnected());
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testList() throws Exception
    {
        for (int i = 0; i < 10; i++) {
            this.neighbors.addNeighbor("neighbor " + i);
        }

        Assert.assertEquals(this.neighbors.getNeighborsList().size(), 10);

        this.neighbors.clearNeighbors();

        for (int i = 0; i < 5; i++) {
            this.neighbors.addNeighbor("neighbor " + i);
        }

        Assert.assertEquals(this.neighbors.getNeighborsList().size(), 5);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testMax() throws Exception
    {
        for (int i = 0; i < 100; i++) {
            this.neighbors.addNeighbor("neighbor " + i);
        }

        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 10);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testRemove() throws Exception
    {
        this.neighbors.addNeighbor("neighbor 0");
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 1);

        this.neighbors.removeNeighbor("neighbor 0");
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 0);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testUnicity() throws Exception
    {
        this.neighbors.addNeighbor("neighbor 0");
        this.neighbors.addNeighbor("neighbor 0");
        Assert.assertEquals(this.neighbors.getNeighborsListSize(), 1);
    }
}
