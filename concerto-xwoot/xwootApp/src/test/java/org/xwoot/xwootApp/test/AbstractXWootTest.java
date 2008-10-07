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

package org.xwoot.xwootApp.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.xwoot.antiEntropy.AntiEntropy;
import org.xwoot.clockEngine.Clock;
import org.xwoot.clockEngine.ClockFactory;
import org.xwoot.lpbcast.sender.LpbCastAPI;
import org.xwoot.thomasRuleEngine.ThomasRuleEngine;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;
import org.xwoot.wootEngine.WootEngine;
import org.xwoot.xwootApp.XWoot;

import java.io.File;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public abstract class AbstractXWootTest
{
    protected final static String WORKINGDIR = "/tmp/xwootTests";

    protected int round = 6;

    protected int logDelay = 60;

    protected int maxNeighbors = 5;
    
    WikiContentManager xwiki1;

    WikiContentManager xwiki2;

    WikiContentManager xwiki3;

    Clock opClock1;

    Clock opClock2;

    Clock opClock3;

    WootEngine wootEngine1;

    WootEngine wootEngine2;

    WootEngine wootEngine3;

    LpbCastAPI lpbCast1;

    LpbCastAPI lpbCast2;

    LpbCastAPI lpbCast3;

    XWoot xwoot1;

    XWoot xwoot2;

    XWoot xwoot3;

    AntiEntropy ae1;

    AntiEntropy ae2;

    AntiEntropy ae3;

    ThomasRuleEngine tre1;

    ThomasRuleEngine tre2;

    ThomasRuleEngine tre3;

    @Before
    public void setup() throws Exception
    {

        // Configure working dir for temporary files
        this.cleanTests(WORKINGDIR);
        this.cleanTests(WORKINGDIR + File.separatorChar + "Site1");
        this.cleanTests(WORKINGDIR + File.separatorChar + "Site2");
        this.cleanTests(WORKINGDIR + File.separatorChar + "Site3");

        File working = new File(WORKINGDIR);

        if (!working.exists()) {
            if (!working.mkdir()) {
                throw new RuntimeException("Can't create working directory: " + WORKINGDIR);
            }
        }

        File site1 = new File(WORKINGDIR + File.separator + "Site1");

        if (!site1.exists()) {
            if (!site1.mkdir()) {
                throw new RuntimeException("Can't create working directory: " + site1);
            }
        }

        File site2 = new File(WORKINGDIR + File.separator + "Site2");

        if (!site2.exists()) {
            if (!site2.mkdir()) {
                throw new RuntimeException("Can't create working directory: " + site2);
            }
        }

        File site3 = new File(WORKINGDIR + File.separator + "Site3");

        if (!site3.exists()) {
            if (!site3.mkdir()) {
                throw new RuntimeException("Can't create working directory: " + site3);
            }
        }
        //        
        // this.xwiki1 =
        // WikiContentManagerFactory.getSwizzleFactory().createWCM(
        // this.propertiesFile1);
        // this.xwiki2 =
        // WikiContentManagerFactory.getSwizzleFactory().createWCM(
        // this.propertiesFile2);
        // this.xwiki3 =
        // WikiContentManagerFactory.getSwizzleFactory().createWCM(
        // this.propertiesFile3);
        //        
        this.xwiki1 = WikiContentManagerFactory.getMockFactory().createWCM();
        this.xwiki2 = WikiContentManagerFactory.getMockFactory().createWCM();
        this.xwiki3 = WikiContentManagerFactory.getMockFactory().createWCM();

        this.opClock1 =
            ClockFactory.getFactory().createClock(WORKINGDIR + File.separator + "Site1" + File.separator + "WootClock");
        this.opClock2 =
            ClockFactory.getFactory().createClock(WORKINGDIR + File.separator + "Site2" + File.separator + "WootClock");
        this.opClock3 =
            ClockFactory.getFactory().createClock(WORKINGDIR + File.separator + "Site3" + File.separator + "WootClock");

        // 3 wootEngines
        this.wootEngine1 =
            new WootEngine(1, WORKINGDIR + File.separator + "Site1" + File.separator + "wootEngine", this.opClock1);
        this.wootEngine2 =
            new WootEngine(2, WORKINGDIR + File.separator + "Site2" + File.separator + "wootEngine", this.opClock2);
        this.wootEngine3 =
            new WootEngine(3, WORKINGDIR + File.separator + "Site3" + File.separator + "wootEngine", this.opClock3);

         // 3 sender for 3 xwoot
         this.lpbCast1 = new MockLpbCast(WORKINGDIR + File.separator
         + "Site1" + File.separator + "receiver", this.round,
         this.logDelay, this.maxNeighbors);
         this.lpbCast2 = new MockLpbCast(WORKINGDIR + File.separator
         + "Site2" + File.separator + "receiver", this.round,
         this.logDelay, this.maxNeighbors);
         this.lpbCast3 = new MockLpbCast(WORKINGDIR + File.separator
         + "Site3" + File.separator + "receiver", this.round,
         this.logDelay, this.maxNeighbors);
//        this.lpbCast1 = null;
//        this.lpbCast2 = null;
//        this.lpbCast3 = null;

        // 3 antiEntropy
        this.ae1 = new AntiEntropy(WORKINGDIR + File.separator + "Site1" + File.separator + "ae");
        this.ae2 = new AntiEntropy(WORKINGDIR + File.separator + "Site2" + File.separator + "ae");
        this.ae3 = new AntiEntropy(WORKINGDIR + File.separator + "Site3" + File.separator + "ae");

        // 3 tre
        this.tre1 = new ThomasRuleEngine(1, WORKINGDIR + File.separator + "Site1" + File.separator + "tre");
        this.tre2 = new ThomasRuleEngine(2, WORKINGDIR + File.separator + "Site2" + File.separator + "tre");
        this.tre3 = new ThomasRuleEngine(2, WORKINGDIR + File.separator + "Site3" + File.separator + "tre");

        // 3 xwoot

        this.xwoot1 =
            new XWoot(this.xwiki1, this.wootEngine1, this.lpbCast1, WORKINGDIR + File.separator + "Site1", "Site1",
                new Integer(1), this.tre1, this.ae1);

        this.xwoot2 =
            new XWoot(this.xwiki2, this.wootEngine2, this.lpbCast2, WORKINGDIR + File.separator + "Site2", "Site2",
                new Integer(2), this.tre2, this.ae2);
        this.xwoot3 =
            new XWoot(this.xwiki3, this.wootEngine3, this.lpbCast3, WORKINGDIR + File.separator + "Site3", "Site3",
                new Integer(3), this.tre3, this.ae3);
        this.cleanXWikis();
    }

    /**
     * Creates a new AbstractXWootTest object.
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

    protected void cleanXWikis() throws Exception
    {

        this.xwiki1.removePage("test.1");
        this.xwiki1.removePage("test.2");
        this.xwiki2.removePage("test.1");
        this.xwiki2.removePage("test.2");
        this.xwiki3.removePage("test.1");
        this.xwiki3.removePage("test.2");
    }
}
