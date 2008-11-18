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

package org.xwoot.wootEngine.test;

import org.junit.Before;
import org.xwoot.clockEngine.Clock;

import org.xwoot.wootEngine.WootEngine;

import java.io.File;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class AbstractWootEngineTest
{
    protected String workingDir = "/tmp/xwootTests/wootEngine/";

    /**
     * Creates a new AbstractWootEngineTest object. DOCUMENT ME!
     */
    public AbstractWootEngineTest()
    {
        if (!new File(this.workingDir).exists()) {
            new File(this.workingDir).mkdirs();
        }
    }

    protected void cleanTests(String directory) throws Exception
    {
        File rootDir = new File(directory);

        if (rootDir.exists()) {
            String[] children = rootDir.list();

            for (String s : children) {
                File toErase = new File(directory, s);

                if (toErase.isDirectory()) {
                    this.cleanTests(toErase.toString());
                } else {
                    toErase.delete();
                }
            }

            rootDir.delete();
        }
    }

    protected WootEngine createEngine(int id) throws Exception
    {
        File working = new File(this.workingDir);

        if (!working.exists()) {
            if (!working.mkdir()) {
                throw new RuntimeException("Can't create clocks directory: " + this.workingDir);
            }
        }

        File testsDir = new File(this.workingDir + File.separator + id);

        if (!testsDir.exists()) {
            if (!testsDir.mkdir()) {
                throw new RuntimeException("Can't create clocks directory: " + this.workingDir);
            }
        }

        Clock clock = new Clock(testsDir.toString());

        WootEngine wootEngine = new WootEngine(id, testsDir.toString(), clock);

        return wootEngine;
    }

    @Before
    public void setUp() throws Exception
    {
        this.cleanTests(this.workingDir + File.separator);
    }
}
