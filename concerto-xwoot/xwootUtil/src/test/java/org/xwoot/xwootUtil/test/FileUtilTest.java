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

package org.xwoot.xwootUtil.test;

import org.junit.Before;
import org.junit.Test;
import org.xwoot.xwootUtil.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Random;

import junit.framework.Assert;

/**
 * Tests for the utility class FileUtil.
 * 
 * @version $Id:$
 */
public class FileUtilTest
{
    /** Working directory for tests. */
    private static final String WORKING_DIR = FileUtil.getTestsWorkingDirectoryPathForModule("xwootUtil");

    /**
     * Creates a new FileUtilTests object.
     */
    public FileUtilTest()
    {
        super();
    }

    /**
     * Initializes the working directory.
     * 
     * @throws Exception if problems occur.
     */
    @Before
    public void initWorkingDir() throws Exception
    {
        FileUtil.checkDirectoryPath(WORKING_DIR);
    }

    /**
     * Exhaustively test the getEncodedFileName and getDecodedFileName methods by generating random strings.
     * <p>
     * Result: The result of decoding a previously encoded string must be equal with the original string.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testBrutForceFileName() throws Exception
    {
        for (int i = 0; i < 50000; i++) {
            Random r = new Random();
            Random r2 = new Random();
            byte[] tab = new byte[r2.nextInt(100)];
            r.nextBytes(tab);

            String filename = new String(tab);
            String encodedFilename = FileUtil.getEncodedFileName(filename);
            String decodedFileName = FileUtil.getDecodedFileName(encodedFilename);

            Assert.assertTrue(Arrays.equals(filename.getBytes(), decodedFileName.getBytes()));
        }
    }

    /**
     * Test decoding an encoded string and checking it with the original.
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testFileName() throws Exception
    {
        String filename = "Test file name";
        String encodedFilename = FileUtil.getEncodedFileName(filename);
        String decodedFileName = FileUtil.getDecodedFileName(encodedFilename);
        Assert.assertEquals(filename, decodedFileName);
    }

    /**
     * Test copying a file from a location to another.
     * <p>
     * Result: The two files will be identical.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCopyFile() throws Exception
    {
        String sourceFileName = "source";
        String destinationFileName = "destination";

        String sourceFileContent = "first line\nsecond line\nthird line\n";

        File sourceFile = new File(WORKING_DIR, sourceFileName);
        // sourceFile.createNewFile();
        File destinationFile = new File(WORKING_DIR, destinationFileName);
        // destinationFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(sourceFile);

        fos.write(sourceFileContent.getBytes());
        fos.close();

        FileUtil.copyFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());

        BufferedReader br = new BufferedReader(new FileReader(destinationFile));

        String destinationFileContent = "";
        String line = null;
        while ((line = br.readLine()) != null) {
            destinationFileContent = destinationFileContent + line + "\n";
        }

        Assert.assertEquals(sourceFileContent, destinationFileContent);
    }
}
