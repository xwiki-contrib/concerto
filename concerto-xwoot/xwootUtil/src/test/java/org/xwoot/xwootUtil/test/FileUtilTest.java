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

import org.junit.Test;
import org.xwoot.xwootUtil.FileUtil;

import java.util.Random;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FileUtilTest
{
    /**
     * Creates a new FileUtilTests object.
     */
    public FileUtilTest()
    {
        super();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
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

            for (int k = 0; k < filename.length(); k++) {
                Assert.assertEquals(filename.getBytes()[k], decodedFileName.getBytes()[k]);
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testFileName() throws Exception
    {
        String filename = "Essai";
        String encodedFilename = FileUtil.getEncodedFileName(filename);
        String decodedFileName = FileUtil.getDecodedFileName(encodedFilename);
        Assert.assertEquals(filename, decodedFileName);
    }
}
