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
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.xwootUtil.FileUtil;

import java.io.File;

/**
 * Abstract Test body for wootEngine tests.
 * <p>
 * Just add tests.
 * 
 * @version $Id$
 */
public abstract class AbstractWootEngineTest
{
    /** Working directory for testing this module. */
    protected String workingDir = FileUtil.getTestsWorkingDirectoryPathForModule("wootEngine");

    /** The internal content of an empty page. */
    protected String emptyPageContent = this.wrapStartEndMarkers("");

    /** Test line of text. */
    protected String line1 = "line1";

    /** Test line of text. */
    protected String line2 = "line2";

    /** Test line of text. */
    protected String line3 = "line3";

    /** Test line of text. */
    protected String line4 = "line4";

    /** Test line of text. */
    protected String line5 = "line5";

    /** Name of page used for testing. */
    protected String pageName = "testPage";
    
    /** Test WootEngine. */
    protected WootEngine site0;
    
    /** Test WootEngine. */
    protected WootEngine site1;
    
    /** Test WootEngine. */
    protected WootEngine site2;

    /**
     * Creates a new AbstractWootEngineTest object.
     */
    public AbstractWootEngineTest()
    {
        try {
            FileUtil.checkDirectoryPath(this.workingDir);
        } catch (Exception e) {
            throw new RuntimeException("The working directory " + this.workingDir + " for this test is not usable.", e);
        }
    }

    /**
     * Creates a wootEngine and stores it in a sub-directory of the test's working directory having the same name as the
     * ID of the newly created WootEngine instance.
     * 
     * @param id the id of the new WootEngine instance.
     * @return the newly created WootEngine instance.
     * @throws Exception if problems occur initializing the wootEngine or it's components.
     */
    protected WootEngine createEngine(int id) throws Exception
    {
        String engineWorkingDirectoryPath = this.workingDir + File.separator + id;
        FileUtil.checkDirectoryPath(engineWorkingDirectoryPath);

        Clock clock = new Clock(engineWorkingDirectoryPath);
        WootEngine wootEngine = new WootEngine(id, engineWorkingDirectoryPath, clock);

        return wootEngine;
    }

    /**
     * Clears the test's working directory if it exists. This is executed before each test.
     */
    @Before
    public void setUp() throws Exception
    {
        FileUtil.deleteDirectory(this.workingDir);
        
        this.site0 = this.createEngine(0);
        this.site1 = this.createEngine(1);
        this.site2 = this.createEngine(2);
    }

    /**
     * @param content the content to be wrapped.
     * @return the content string wrapped by the start and end rows. Example: "[content]".
     */
    public String wrapStartEndMarkers(String content)
    {
        return WootRow.FIRST_WOOT_ROW.getContent() + content + WootRow.LAST_WOOT_ROW.getContent();
    }

    /**
     * @param line the content of a line without a terminating "\n" character.
     * @return the line appended with a "\n" character.
     */
    public String addEndLine(String line)
    {
        return line + "\n";
    }
}
