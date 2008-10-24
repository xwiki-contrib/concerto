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

package org.xwoot.mockiphone.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwoot.mockiphone.MockIphoneException;

import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
//import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockIphonePage implements Serializable
{
    private static final long serialVersionUID = 6228704590406225117L;
    private String pageName;
    private Map content;
    private boolean isModified;
    private boolean isRemoved;

    public final static String PAGELISTFILENAME=".pageList";

    public MockIphonePage(String pageName)
    {
        this.pageName = pageName;
        this.content = new HashMap<String,String>();
        this.isModified=false;
        this.isRemoved=false;
    }

    public MockIphonePage(String pageName, Map page)
    { 
        this.pageName = pageName;
        this.content = page;
        this.isModified=false;
        this.isRemoved=false;
       
    }

    private synchronized void loadPage(String pagesDir) throws MockIphoneException 
    {
        if (!this.existPage(pagesDir)) {
            this.createPage(pagesDir);
        }
    
        XStream xstream = new XStream(new DomDriver());
        MockIphonePage page;
        try {
            page = (MockIphonePage) xstream.fromXML(new FileInputStream(pagesDir + File.separator + this.pageName));
            this.setValues(page);
        } catch (FileNotFoundException e) { 
            throw new MockIphoneException("File not found : "+pagesDir + File.separator + this.pageName+"\n",e);
        }
    }

    private synchronized void storePage(String pagesDir) throws MockIphoneException
    {
        XStream xstream = new XStream();
    
        OutputStreamWriter osw;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(pagesDir + File.separator + this.pageName), Charset
                .forName(System.getProperty("file.encoding")));
            PrintWriter output = new PrintWriter(osw);
    
            output.print(xstream.toXML(this));
            output.flush();
            output.close();
        } catch (FileNotFoundException e) { 
            throw new MockIphoneException("File not found : "+pagesDir + File.separator + this.pageName+"\n",e);
        }
    
    }

    private synchronized void unloadPage(String pagesDir) throws MockIphoneException
    {
        this.storePage(pagesDir);
        System.runFinalization();
        System.gc();
    }
    
    private void setValues(MockIphonePage page){
        this.pageName = page.getPageName();
        this.content = page.getContent();
        this.isModified=page.isModified();
        this.isRemoved=page.isRemoved();
    }
    
    private boolean isRemoved()
    {
       return this.isRemoved;
    }

    private boolean isModified()
    {
       return this.isModified;
    }

    private Map getContent()
    {
        return this.content;
    }

    private String getPageName()
    {
        return this.pageName;
    }

    public static String getTemplate(){
        XStream xstream = new XStream(new DomDriver());
        xstream.aliasAttribute(MockIphonePage.class, "content", "content");
        xstream.useAttributeFor(MockIphonePage.class, "pageName");
        xstream.omitField(MockIphonePage.class, "isModified");
        xstream.omitField(MockIphonePage.class, "isRemoved");
        xstream.alias("page", MockIphonePage.class);
        MockIphonePage page=new MockIphonePage("");
        page.getContent().put("content", "");
        return xstream.toXML(page);
    }

    public void createPage(String pagesDir) throws MockIphoneException
    {
        if (!this.existPage(pagesDir)) {
            this.isModified=false;
            this.isRemoved=false;
            XStream xstream = new XStream();
            try {
                PrintWriter pw =
                    new PrintWriter(new FileOutputStream(pagesDir + File.separator + this.pageName));
                pw.print(xstream.toXML(this));
                pw.flush();
                pw.close();
            }catch (FileNotFoundException e) {
                throw new MockIphoneException("File not found : "+pagesDir + File.separator + this.pageName+"\n",e);
            }
    
        }
    }
    
    public void removePageFile(String pagesDir) throws MockIphoneException
    {
        File f=new File(pagesDir+File.separatorChar+this.pageName);
        this.unloadPage(pagesDir);
        System.out.println("delete..."+f+f.canRead()+f.canWrite());
        
        if (f.exists()){
            System.out.println(f.delete());
            System.out.println(f.delete());
        }
    }

    public void savePage(String pagesDir,String xml) throws MockIphoneException {
        XStream xstream = new XStream(new DomDriver());
        xstream.aliasAttribute(MockIphonePage.class, "isModified", "isModified");
        xstream.aliasAttribute(MockIphonePage.class, "isRemoved", "isRemoved");
        xstream.aliasAttribute(MockIphonePage.class, "content", "content");
        xstream.useAttributeFor(MockIphonePage.class, "pageName");
        xstream.alias("page", MockIphonePage.class);
        MockIphonePage page=(MockIphonePage) xstream.fromXML(xml);
        this.setValues(page);
        this.isModified=true;
        this.unloadPage(pagesDir);   
    }
    
    public void deletePage(String pagesDir) throws MockIphoneException{
        this.loadPage(pagesDir);
        this.isModified=true;
        this.isRemoved=true;
        this.unloadPage(pagesDir);   
    }

    public String toXML(String pagesDir) throws MockIphoneException
    {
       this.loadPage(pagesDir);
       //XStream xstream = new XStream(new JettisonMappedXmlDriver());
       // XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
       XStream xstream = new XStream();
       xstream.aliasAttribute(MockIphonePage.class, "isModified", "isModified");
       xstream.aliasAttribute(MockIphonePage.class, "isRemoved", "isRemoved");
       xstream.aliasAttribute(MockIphonePage.class, "content", "content");
       xstream.useAttributeFor(MockIphonePage.class, "pageName");
       xstream.alias("page", MockIphonePage.class);
       
       return xstream.toXML(this);     
    }

    public String toXmlForEdition(String pagesDir) throws MockIphoneException
    {
       this.loadPage(pagesDir);
       //XStream xstream = new XStream(new JettisonMappedXmlDriver());
       // XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
       XStream xstream = new XStream();
       xstream.alias("page", MockIphonePage.class);
       xstream.aliasAttribute(MockIphonePage.class, "content", "content");
       xstream.omitField(MockIphonePage.class, "isModified");
       xstream.omitField(MockIphonePage.class, "isRemoved");
       System.out.println(xstream.toXML(this));
       return xstream.toXML(this);     
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pagesDir DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean existPage(String pagesDir)
    {
        File dir = new File(pagesDir);
        String[] listPages = dir.list();
    
        // for each page on the site
        for (String listPage : listPages) {
            if (listPage.equals(this.pageName)) {
                return true;
            }
        }
    
        return false;
    }

    static public void savePageList(String workingDir,List list) throws MockIphoneException
    {  
        File listFile=new File(workingDir+File.separatorChar+PAGELISTFILENAME);
        if (listFile.exists()){
            listFile.delete();
        }
        XStream xstream = new XStream();
        try {
            PrintWriter pw =
                new PrintWriter(new FileOutputStream(listFile));
            pw.print(xstream.toXML(list));
            pw.flush();
            pw.close();
        }catch (FileNotFoundException e) {
            throw new MockIphoneException("File not found : "+ listFile + "\n",e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param pagesDir DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws MockIphoneException 
     * @throws FileNotFoundException DOCUMENT ME!
     */
    static public Collection getManagedPageNames(String pagesDir) throws MockIphoneException 
    {
        File dir = new File(pagesDir);
        String[] listPages = dir.list();
        ArrayList result=new ArrayList<String>();
        for (int i=0;i<listPages.length;i++){
            result.add(listPages[i]);
        }
        result.remove(PAGELISTFILENAME);
        return result;
    }

    public boolean isModified(String workingDir) throws MockIphoneException
    {
        this.loadPage(workingDir);
        return this.isModified;     
    }   
    
    public boolean isRemoved(String workingDir) throws MockIphoneException
    {
        this.loadPage(workingDir);
        return this.isRemoved;     
    }

    public Map getMap(String workingDir) throws MockIphoneException
    {
        this.loadPage(workingDir);
        return this.content;
    }

    public void setModified(String workingDir, boolean b) throws MockIphoneException
    {
        this.loadPage(workingDir);
        this.isModified=b;
        this.unloadPage(workingDir);
        
    }   
}
