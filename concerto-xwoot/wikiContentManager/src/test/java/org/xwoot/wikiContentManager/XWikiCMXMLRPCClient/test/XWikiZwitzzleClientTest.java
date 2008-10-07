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

package org.xwoot.wikiContentManager.XWikiCMXMLRPCClient.test;

import org.junit.Test;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.XWikiSwizzleClient.XwikiSwizzleClient;

import java.util.List;
import java.io.File;
import java.security.MessageDigest;

import java.util.Map;

import junit.framework.Assert;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWikiZwitzzleClientTest
{
    private XwikiSwizzleClient client;

    private final static String PROPERTIESFILE =
        "./wikiContentManager/src/test/resources/xwiki.properties";

    /**
     * Creates a new XWikiZwitzzleClientTest object.
     * 
     * @param name DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public XWikiZwitzzleClientTest() throws Exception
    {
        System.out.println(new File(".").getAbsolutePath());
        this.client = new XwikiSwizzleClient(PROPERTIESFILE);      
        this.client.connect();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testComments() throws Exception
    {      
        this.client.createPage("test.testComments", "");
        Map comment =
            this.client.createComment("New comment for test", null, "", "test.newComment", "test.testComments", "", "");
        System.out.println(comment);
        comment = this.client.setComment("test.testComments", comment);
        System.out.println(comment);
        comment.put(WikiContentManager.CONTENT, "Yogourt");
        this.client.setComment("test.testComments", comment);
        System.out.println(this.client.getComments("test.testComments"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testCreatePage() throws Exception
    {
        this.client.removePage("test.1");
        this.client.createPage("test.1", "test");
        Assert.assertNotNull(this.client.getFields("test.1"));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testFields() throws Exception
    {
        this.client.removePage("test.2");
        this.client.createPage("test.2", "");

        Map fields = this.client.getFields("test.2");
        fields.put(WikiContentManager.PARENTID, "");
        fields.put(WikiContentManager.CONTENT, "");
        System.out.println("=+>" + fields);
        Assert.assertNotNull(fields);
        Assert.assertFalse("XWiki.terminator".equals(fields.get(WikiContentManager.CREATOR)));
        Assert.assertFalse("XWiki.terminator".equals(fields.get(WikiContentManager.MODIFIER)));
        Assert.assertFalse("Terminator's dad".equals(fields.get(WikiContentManager.PARENTID)));
        Assert.assertFalse("Hasta la vista, baby !".equals(fields.get(WikiContentManager.CONTENT)));

        fields.put(WikiContentManager.CREATOR, "XWiki.terminator");
        fields.put(WikiContentManager.MODIFIER, "XWiki.terminator");
        fields.put(WikiContentManager.PARENTID, "Terminator's dad");
        fields.put(WikiContentManager.CONTENT, "Hasta la vista, baby !");

        Assert.assertEquals("XWiki.terminator", fields.get(WikiContentManager.CREATOR));
        Assert.assertEquals("XWiki.terminator", fields.get(WikiContentManager.MODIFIER));
        Assert.assertEquals("Terminator's dad", fields.get(WikiContentManager.PARENTID));
        Assert.assertEquals("Hasta la vista, baby !", fields.get(WikiContentManager.CONTENT));

        this.client.setFields("test.2", fields);
        fields = null;
        fields = this.client.getFields("test.2");
        System.out.println("=++>" + fields);

        // assertEquals("XWiki.terminator",fields.get(WikiContentManager.CREATOR)
        // );
        // assertEquals("XWiki.terminator",
        // fields.get(WikiContentManager.MODIFIER));
        // assertEquals(fields.get(WikiContentManager.CREATED),
        // fields.get(WikiContentManager.CREATED));
        Assert.assertEquals("Terminator's dad", fields.get(WikiContentManager.PARENTID));
        Assert.assertEquals("Hasta la vista, baby !", fields.get(WikiContentManager.CONTENT));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testPageContent() throws Exception
    {

        this.client.removePage("test.5");
        this.client.createPage("test.5", "New test content.\n Alta la vista Baby!");

        Assert.assertEquals("New test content.\n Alta la vista Baby!", this.client.getPageContent("test.5"));

        MessageDigest md = MessageDigest.getInstance("md5");
        byte[] b = "New test content.\n Alta la vista Baby!".getBytes();
        md.update(b);
        this.client.overwritePageContent("test.5",
            "New test content.\n Alta la vista Baby! Add a line to modify diggest...\n");
        Assert.assertEquals("New test content.\n Alta la vista Baby! Add a line to modify diggest...\n", this.client
            .getPageContent("test.5"));

        String result =
            this.client.setPageContent("test.5", "New test content.\n Alta la vista Baby! Concurrent modif", "md5", md
                .digest());
        Assert.assertEquals("New test content.\n Alta la vista Baby! Add a line to modify diggest...\n", result);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testSpacesAndPages() throws Exception
    {
        List spaces = (List) this.client.getListSpaceId();
        System.out.println(spaces);
        Assert.assertTrue(spaces.contains("XWiki"));
        this.client.removePage("test.666");
        this.client.createPage("test.666", "");

        List<String> pages = (List<String>) this.client.getListPageId("test");
        System.out.println(pages);
        Assert.assertTrue(pages.contains("test.666"));
        this.client.removePage("test3.essai");
        this.client.createPage("test3.essai", "");
        System.out.println(this.client.getFields("test3.essai"));
        pages = (List<String>) this.client.getListPageId("test3");
        System.out.println(pages);
        Assert.assertTrue(pages.contains("test3.essai"));

        // remove page which not exist ; must not bug
        Assert.assertFalse(this.client.removePage("test3.dontexist"));

        // repeat creation of the same space ; must not bug
        this.client.removeSpace("test5");
        this.client.createSpace("test5");
        // this.client.createSpace("test5");
        pages = (List<String>) this.client.getListPageId("test5");
        Assert.assertTrue(pages.contains("test5.WebHome"));
        this.client.removePage("test5.page2");
        this.client.createPage("test5.page2", "Content which will be deleted");

        this.client.removeSpace("test5");

        spaces = (List<String>) this.client.getListSpaceId();

        Assert.assertFalse(spaces.contains("test5"));
    }

    @Test
    public void essai() throws Exception{
        this.client.essai();
    }
//    @Test
//    public void getAllPagesInTextFile() throws Exception
//    {
//        File temp = File.createTempFile("xwikipages", ".txt");
//        List spaces = (List) this.client.getListSpaceId();
//        BufferedWriter sortie = new BufferedWriter(new FileWriter(temp, true));
//        sortie.write("/****************/\n");
//        sortie.write("/* Space list : */\n");
//        sortie.write("/****************/\n");
//        Iterator i = spaces.iterator();
//        while (i.hasNext()) {
//            sortie.write((String) i.next() + "\n");
//        }
//        i = spaces.iterator();
//        while (i.hasNext()) {
//            String space = (String) i.next();
//            sortie.write("/****************/\n");
//            sortie.write("/* Space : " + space + " */\n");
//            sortie.write("/****************/\n");
//            Collection pages = this.client.getListPageId(space);
//            Iterator j = pages.iterator();
//            while (j.hasNext()) {
//                String page = (String) j.next();
//                sortie.write("/******************************** Page : " + page + " */\n");
//                sortie.write("\n");
//                sortie.write(this.client.getFields(page).toString() + "\n");
//                sortie.write("\n");
//                sortie.write("\n");
//            }
//        }
//
//        sortie.flush();
//        sortie.close();
//        System.out.println(temp);
//        File result = new File("/tmp/xwikipages.txt");
//        if (result.exists()) {
//            result.delete();
//        }
//        temp.renameTo(result);
//
//    }

    // @Test
    // public void essai() throws Exception {
    // this.client.essai();
    //        
    // }
}
