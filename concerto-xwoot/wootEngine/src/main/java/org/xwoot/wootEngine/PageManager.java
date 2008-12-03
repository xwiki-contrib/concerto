/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwoot.wootEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;
import org.xwoot.wootEngine.core.WootPage;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.xwootUtil.FileUtil;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Handles WootPages for the internal WootEngine model.
 * 
 * @version $Id$
 */
public class PageManager extends LoggedWootExceptionThrower
{
    /** The name of the directory inside the workingDir where to serialize {@link WootPage} objects. */
    public static final String PAGES_DIRECTORY_NAME = "pages";
    
    /** The name of the file where to serialize {@link WootPage} the last page name list. */
    public static final String PAGENAME_LIST_FILE_NAME = "pageList";
    
    /** The directory path where to store pages. */
    private String pagesDirPath;

    /** The list of the names of the modified pages */
    private List pageNameList;
    
    /** The file path where to serialize {@link WootPage} the last page name list. */
    private String PagenameListPath;
    /**
     * Creates a new PageManager instance to be used by the WootEngine. The PageManager will store it's pages in a
     * sub-directory of the owning WootEngine's working directory.
     * 
     * @param wootEngineId the Id of the WootEngine instance this page manager belongs to.
     * @param wootEngineWorkingDirPath the workingDir for the WootEngine this page manager belongs to.
     * @throws WootEngineException if the WootEngine's working directory is not accessible.
     * @see WootEngine#getWootEngineId()
     * @see WootEngine#getWorkingDir()
     * @see FileUtil#checkDirectoryPath(String)
     */
    public PageManager(int wootEngineId, String wootEngineWorkingDirPath) throws WootEngineException
    {
        String newPagesDirPath = wootEngineWorkingDirPath + File.separator + PAGES_DIRECTORY_NAME;
        
        this.pagesDirPath = newPagesDirPath;
        
        this.PagenameListPath=wootEngineWorkingDirPath + File.separator + PAGENAME_LIST_FILE_NAME;

        this.createWorkingDir();

        this.wootEngineId = wootEngineId;
        this.logger = LogFactory.getLog(this.getClass());
    }

    /**
     * Creates the directory structure for the PageManager.
     * 
     * @throws WootEngineException if file access problems occur.
     * @see FileUtil#checkDirectoryPath(String)
     */
    public void createWorkingDir() throws WootEngineException
    {
        try {
            FileUtil.checkDirectoryPath(this.getPagesDirPath());
        } catch (Exception e) {
            this.throwLoggedException("Problems creating the PageManager's working directory.", e);
        }
    }

    /**
     * Deletes and reinitializes the contents of the working dir.
     * 
     * @throws WootEngineException if problems occur while recreating the working directory's structure.
     * @see #createWorkingDir()
     */
    public void clearWorkingDir() throws WootEngineException
    {
        File workingDir = new File(this.getPagesDirPath());

        if (workingDir.exists()) {
            FileUtil.deleteDirectory(workingDir);
        }
        
        File pageNameVectorFile=new File(this.getPagenameListPath());
        pageNameVectorFile.delete();
        createWorkingDir();
    }

    /**
     * Creates a new empty page and stores it in the model.
     * 
     * @param pageName the name of the new page to create. If this is empty or null, an InvalidParameterException is
     *            thrown.
     * @return the newly created {@link WootPage} object.
     * @throws WootEngineException if the page already exists or if problems occurred while serializing.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     * @see WootPage#WootPage(String)
     * @see #storePage(WootPage)
     */
    public WootPage createPage(String pageName) throws WootEngineException, IllegalArgumentException
    {
        if (this.pageExists(pageName)) {
            this.throwLoggedException("The page named " + pageName + " already exits.");
        }

        WootPage wootPage = new WootPage(pageName);

        this.logger.debug(this.getWootEngineId() + " - Create woot page : " + wootPage.getFileName());

        this.storePage(wootPage);

        return wootPage;
    }

    /**
     * Serializes the {@link WootPage}.
     * 
     * @param wootPage the page to serialize.
     * @throws WootEngineException if file access problems occur.
     */
    private void storePage(WootPage wootPage) throws WootEngineException
    {
        XStream xstream = new XStream(new DomDriver());

        String pageFilePath = this.getPagesDirPath() + File.separator + wootPage.getFileName();
        Charset fileEncodingCharset = Charset.forName(System.getProperty("file.encoding"));

        OutputStreamWriter osw = null;
        PrintWriter output = null;

        try {
            osw = new OutputStreamWriter(new FileOutputStream(pageFilePath), fileEncodingCharset);
            output = new PrintWriter(osw);
            output.print(xstream.toXML(wootPage));
            output.flush();
        } catch (Exception e) {
            this.throwLoggedException("Problem storing page " + wootPage.getPageName(), e);
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                this.throwLoggedException("Problem closing the file " + pageFilePath + " after storing page "
                    + wootPage.getPageName(), e);
            }
        }
        this.addPageNameInList(wootPage.getPageName());
    }

    /**
     * Check if a page name already exists in the model.
     * 
     * @param pageName the name of the page to check.
     * @return true if the page exists, false if it does not or if the page name is empty or null.
     * @throws WootEngineException if the name of the page caused encoding problems.
     * @see FileUtil#getEncodedFileName(String)
     */
    public boolean pageExists(String pageName) throws WootEngineException
    {
        if (pageName == null || pageName.length() == 0) {
            return false;
        }

        String pageFileName = null;
        try {
            pageFileName = FileUtil.getEncodedFileName(pageName);
        } catch (UnsupportedEncodingException e) {
            this.throwLoggedException("Problem with filename encoding for page " + pageName, e);
        }

        File pageFile = new File(this.getPagesDirPath(), pageFileName);

        return pageFile.exists();
    }

    /**
     * Loads a WootPage previously stored.
     * 
     * @param pageName the name of the wanted page.
     * @return the requested WootPage or if it does not exist, a new WootPage with the same name that is automatically
     *         added to the model.
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    public synchronized WootPage loadPage(String pageName) throws WootEngineException
    {
        if (!this.pageExists(pageName)) {
            return this.createPage(pageName);
        }

        String filename = null;
        try {
            filename = FileUtil.getEncodedFileName(pageName);
        } catch (UnsupportedEncodingException e) {
            this.throwLoggedException("Problem with filename encoding of page : " + pageName, e);
        }

        XStream xstream = new XStream(new DomDriver());
        String filePath = this.getPagesDirPath() + File.separator + filename;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(filePath);

            return (WootPage) xstream.fromXML(fis);
        } catch (Exception e) {
            this.throwLoggedException("Problems loading page " + pageName + " from file " + filePath, e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                this.throwLoggedException("Problems closing file " + filePath + " after loading page " + pageName);
            }
        }

        // never reachable.
        return null;
    }

    /**
     * Loads the copy of a page previously stored. The copy is saved on drive with the file extension
     * {@link WootPage#SAVED_FILE_EXTENSION}.
     * 
     * @param pageName the name of the wanted page.
     * @return the requested WootPage or if it does not exist, a new WootPage with the same name that is automatically
     *         added to the model.
     * @throws WootEngineException if the pageName causes encoding problems or if serializing/deserializing problems
     *             occur.
     * @throws IllegalArgumentException if the pageName is a null or empty String.
     */
    public synchronized WootPage loadCopy(String pageName) throws WootEngineException
    {
        WootPage result = null;
        String savePageName = pageName + WootPage.SAVED_FILE_EXTENSION;

        result = loadPage(savePageName);

        result.setPageName(pageName);
        result.setSavedPage(true);

        return result;
    }

    /**
     * @param pageName the name of the page.
     * @return the visible content of a page or null if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see WootPage#toHumanString()
     * @see #loadPage(String)
     * @see WootRow#isVisible()
     */
    public String getPage(String pageName) throws WootEngineException
    {
        if (!this.pageExists(pageName)) {
            return "";
        }

        return this.loadPage(pageName).toHumanString();
    }

    /**
     * @param pageName the name of the page.
     * @return the full content of the page, as stored internally, or null if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see WootPage#toHumanString()
     * @see #loadPage(String)
     */
    public String getPageInternal(String pageName) throws WootEngineException
    {
        if (!this.pageExists(pageName)) {
            return null;
        }

        return this.loadPage(pageName).toString();
    }
    
    /**
     * @param pageName the name of the page.
     * @return only the visible content of the page, as stored internally or null if the page does not exist.
     * @throws WootEngineException if problems occur while accessing the page.
     * @see #loadPage(String)
     * @see WootPage#toVisibleString()
     */
    public String getPageInternalVisible(String pageName) throws WootEngineException
    {
        if (!this.pageExists(pageName)) {
            return null;
        }

        return this.loadPage(pageName).toVisibleString();
    }

    /**
     * Serializes a WootPage object and requests finalization and garbage collection to free the used resources.
     * 
     * @param wootPage the object to serialize and unload.
     * @throws WootEngineException if problems occur while serializing the object.
     * @see #storePage(WootPage)
     * @see System#runFinalization()
     * @see System#gc()
     */
    public synchronized void unloadPage(WootPage wootPage) throws WootEngineException
    {
        this.storePage(wootPage);
        System.runFinalization();
        System.gc();
    }

    /**
     * If a page exists, it is loaded and its savedPage field is set to true. After that, the page is unloaded.
     * <p>
     * If the page is not found, nothing happens. FIXME: Find with a better explication.
     * 
     * @param pageName the name of the page.
     * @throws WootEngineException if problems occur while loading/storing the page.
     */
    public void copyPage(String pageName) throws WootEngineException
    {
        if (this.pageExists(pageName)) {
            WootPage currentPage = this.loadPage(pageName);
            currentPage.setSavedPage(true);
            this.unloadPage(currentPage);
        }
    }

    /**
     * @return An array of all WootPages names in the model in decoded format or null if there are no pages in the
     *         model.
     * @throws WootEngineException if filename decoding problems occur.
     * @see FileUtil#getDecodedFileName(String)
     */
    public String[] listPages() throws WootEngineException
    {
        File dir = new File(this.getPagesDirPath());
        String[] pageNames = dir.list();

        // FIXME: watch out for directories not supposed to be in the pagesDir. They will show up as page names.

        if (pageNames != null) {

            for (int i = 0; i < pageNames.length; i++) {
                try {
                    pageNames[i] = FileUtil.getDecodedFileName(pageNames[i]);
                } catch (UnsupportedEncodingException e) {
                    this.throwLoggedException("Problems decoding file name " + pageNames[i], e);
                }
            }

        }

        return pageNames;
    }

    /**
     * @param pageName the name of the page.
     * @return the content of a WootPage, with each line wrapped as a paragraph with the class "visible" or "invisible",
     *         depending on the status of a {@link WootRow} (page line). If the page has no content, then an empty
     *         string is returned.
     * @throws WootEngineException if problems loading the page occur.
     * @see #loadPage(String)
     * @see WootRow#isVisible()
     */
    public String getPageContentModifications(String pageName) throws WootEngineException
    {
        WootPage page = this.loadPage(pageName);

        if (page == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer("");
        String paragraphEnd = "</p>\n";
        for (WootRow row : page.getRows()) {
            if (row.isVisible()) {
                sb.append("<p class=\"visibleLine\">" + row.getContent() + paragraphEnd);
            } else {
                sb.append("<p class=\"invisibleLine\">" + row.getContent() + paragraphEnd);
            }
        }

        return sb.toString();
    }

    /**
     * @return the directory path where the WootPages of the internal model are stored.
     */
    public String getPagesDirPath()
    {
        return this.pagesDirPath;
    }
    
    
    private void loadPageNameList() throws WootEngineException{
        XStream xstream = new XStream(new DomDriver());
        String filePath = this.getPagenameListPath();
        
        if (!new File(filePath).exists())
        {
            this.setPageNameList(new Vector<String>());
        }
        else {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filePath);
                this.setPageNameList((Vector<String>) xstream.fromXML(fis));
            } catch (Exception e) {
                this.throwLoggedException("Problems loading pagename vector file " + this.getPagenameListPath(), e);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Exception e) {
                    this.throwLoggedException("Problems closing file " + filePath,e);
                }
            }  
        }
    }
    
    private void storePageNameList() throws WootEngineException{
        
        XStream xstream = new XStream(new DomDriver());
        String filePath = this.getPagenameListPath();
        Charset fileEncodingCharset = Charset.forName(System.getProperty("file.encoding"));
        OutputStreamWriter osw = null;
        PrintWriter output = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(filePath), fileEncodingCharset);
            output = new PrintWriter(osw);
            output.print(xstream.toXML(this.getPageNameList()));
            output.flush();
            
        } catch (Exception e) {
            this.throwLoggedException("Problems loading file " + this.getPagenameListPath(), e);
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                this.throwLoggedException("Problems closing file " + filePath,e);
            }
        }
    }
    
    public void addPageNameInList(String pageName) throws WootEngineException{
        this.loadPageNameList();
        this.getPageNameList().add(pageName);
        this.storePageNameList();
    }
    
    public void removeNOccurencesInList(String pageName,int number) throws WootEngineException{
       this.loadPageNameList();
       int index=0;
       for(int i=0;i<number && index!=-1;i++){
           index=this.getPageNameList().indexOf(pageName);
           if (index!=-1){
               this.getPageNameList().remove(index);
           }  
       }
       this.storePageNameList();
    }
    
    public List getCurrentPageNameList() throws WootEngineException{
        this.loadPageNameList();
        return this.getPageNameList();
    }

    private void setPageNameList(List list)
    {
       this.pageNameList=list; 
    }
    
    public List getPageNameList()
    {
        return this.pageNameList;
    }

    public String getPagenameListPath()
    {
        return this.PagenameListPath;
    }

    public void setPagenameVectorPath(String pagenameVectorPath)
    {
        this.PagenameListPath = pagenameVectorPath;
    }
}
