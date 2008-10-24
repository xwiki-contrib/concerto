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

package org.xwoot.mockiphone;

//Harg ! Coupling between patch and XWoot ...
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.mockiphone.core.MockIphonePage;
import org.xwoot.mockiphone.iwootclient.IWootClient;
import org.xwoot.mockiphone.iwootclient.IWootClientException;
import org.xwoot.mockiphone.iwootclient.rest.IWootRestClient;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class MockIphone
{
    
    private Integer id;

    private final Log logger = LogFactory.getLog(this.getClass());

    private String workingDir;
    
    private IWootClient iwootRestClient;

    public IWootClient getIwootRestClient()
    {
        return this.iwootRestClient;
    }

    /**
     * Creates a new IWoot object.
     * 
     * @param iwootUrl DOCUMENT ME!
     * @param id DOCUMENT ME!
     * @param WORKINGDIR DOCUMENT ME!
     * @throws MockIphoneException 
     * 
     */
    public MockIphone(String workingDir, Integer id, String iwootUrl) throws MockIphoneException
        {
        this.id=id;
        this.workingDir = workingDir;
        this.createWorkingDir();
        this.iwootRestClient=new IWootRestClient(iwootUrl);
        this.logger.info(this.id + " : MockIphone engine created. working directory : " + workingDir + "\n\n");
        }

    /**
     * DOCUMENT ME!
     * 
     * @param dir DOCUMENT ME!
     */
    public static void deleteDirectory(File dir)
    {
        if (dir.exists()) {
            String[] children = dir.list();

            for (String element : children) {
                File f = new File(dir, element);
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }

            dir.delete();
        }
    }
    
    public void clearWorkingDir() throws MockIphoneException 
    {
        File f = new File(this.workingDir);
        System.out.println("=>" + this.workingDir);
        if (f.exists()) {
            this.logger.info(this.id + " Delete working dir mockIphone : " + f.toString());
            deleteDirectory(f);
        }
        this.createWorkingDir();
    }

    private void createWorkingDir() throws MockIphoneException 
    {
        File working = new File(this.workingDir);

        if (!working.exists() && !working.mkdir()) {
            throw new MockIphoneException("Can't create xwoot directory: " + working);
        }

        if (!working.isDirectory()) {
            throw new RuntimeException(working + " is not a directory");
        } else if (!working.canWrite()) {
            throw new MockIphoneException("Can't write in directory: " + working);
        }
    }
    
    public void setPageContent(String pageName,String xmlContent) throws MockIphoneException{
        this.logger.info("Add page : "+pageName+" to management.");
        MockIphonePage page = new MockIphonePage(pageName);
        page.savePage(this.workingDir, xmlContent);
    } 
    
    public Map getManagedPages() throws MockIphoneException{
        Map map=new HashMap<String, Boolean>();
        Collection list=MockIphonePage.getManagedPageNames(this.workingDir);
        Iterator i=list.iterator();
        while(i.hasNext()){
            String pageName=(String)i.next();
            MockIphonePage page=new MockIphonePage(pageName);
            map.put(pageName, Boolean.valueOf(page.isModified(this.workingDir)));
        }
        return map;
    }
    
    public Integer getId()
    {
        return this.id;
    }

    public String getPage(String pageName) throws MockIphoneException{
        if (pageName==null || pageName.equals("")){
            return "";
        }
        MockIphonePage page = new MockIphonePage(pageName);
        return page.toXML(this.workingDir);
    }
    
    public void createPage(String pageName,String content) throws MockIphoneException{
        MockIphonePage page=new MockIphonePage(pageName);
        page.createPage(this.workingDir);
        page.savePage(this.workingDir, content);
    }
    
    public String getPageForEdition(String pageName) throws MockIphoneException{
        if (pageName==null || pageName.equals("")){
            return "";
        }
        MockIphonePage page = new MockIphonePage(pageName);
        return page.toXmlForEdition(this.workingDir);
    }
    
    public void refreshPageList() throws MockIphoneException, IWootClientException
    {
        List l=this.iwootRestClient.getPageList();
        MockIphonePage.savePageList(this.workingDir, l);
    }

    public void removePage(String pageName) throws MockIphoneException
    {
        if (pageName==null || pageName.equals("")){
            return;
        }
        MockIphonePage page=new MockIphonePage(pageName);
        page.deletePage(this.workingDir);
    }  
    
    public String getTemplate(){
       return MockIphonePage.getTemplate();
        
    }
    
    public void sendPage(String pageName) throws MockIphoneException, IWootClientException{
        MockIphonePage page=new MockIphonePage(pageName);
        /*if (page.isRemoved(this.workingDir)){
            page.removePageFile(this.workingDir);
            this.iwootRestClient.removePage(pageName);
            return ;
        }*/
        this.iwootRestClient.putPage(pageName, page.getMap(this.workingDir));
        page.setModified(this.workingDir,false); 
    }
    
    public List askPageList() throws MockIphoneException, IWootClientException{
        List l=this.iwootRestClient.getPageList();
        MockIphonePage.savePageList(this.workingDir,l);
        Collection list=MockIphonePage.getManagedPageNames(this.workingDir);
        l.removeAll(list);
        return l;
    }
    
    public void askPage(String pageName) throws MockIphoneException, IWootClientException
    {
        if (pageName==null || pageName.equals("")){
            return;
        }
       Map page =this.iwootRestClient.getPage(pageName);
       this.logger.info("Ask page : "+pageName+" to rest server.");
       this.logger.info("Page  : "+page);
       MockIphonePage p=new MockIphonePage(pageName,page);
       if (p.existPage(this.workingDir)){
          p.removePageFile(this.workingDir);
       }
       p.createPage(this.workingDir);
    }
    
}
