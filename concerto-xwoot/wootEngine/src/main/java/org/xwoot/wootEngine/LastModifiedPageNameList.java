package org.xwoot.wootEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Manages a page name list, gets pagename list, adds pagename or removes a given occurrences number of a pagename.
 * 
 * It is used by {@link PageManager} to store last modified page names. The {@link WootEngine} user can consume pagenames
 * of the list. 
 * 
 * @version 
 */
public class LastModifiedPageNameList extends LoggedWootExceptionThrower
{
    
    /** The list of the page names */
    private List pageNameList;
    
    /** The file path where to serialize */
    private String PageNameListPath;
    
    /** The name of the file where to serialize */
    public static final String PAGENAME_LIST_FILE_NAME = "pageList";
    
    /**
     * Creates a new LastModifiedPageNameList instance to be used by the {@link PageManager}. The PageManager will add the name 
     * of each modified page. A consumer can get the list and remove some occurrences of a given pageName.
     * 
     * @param wootEngineWorkingDirPath the workingDir for the WootEngine this page manager belongs to.
     * 
     */
    public LastModifiedPageNameList(String wootEngineWorkingDirPath){
        this.logger = LogFactory.getLog(this.getClass());
        this.PageNameListPath=wootEngineWorkingDirPath + File.separator + PAGENAME_LIST_FILE_NAME;
        File pageNameListFile=new File(this.getPagenameListPath());
        pageNameListFile.delete();
    }
    /**
     * Loads the list of the page names previously stored. Creates a new list if it has never been stored. 
     * 
     * @throws WootEngineException if the loading causes serializing/deserializing problems.
     *             
     */
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
                this.throwLoggedException("Problems loading pagename list file " + this.getPagenameListPath(), e);
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
    
    /**
     * Serializes the list of page names
     * 
     * @throws WootEngineException if file access problems occur.
     */
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
    
    /**
     * 
     * @param list the list of the page names to set
     * 
     */
    private void setPageNameList(List list)
    {
       this.pageNameList=list; 
    }
    
    /**
     * @return the page names list
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    private List getPageNameList()
    {
        return this.pageNameList;
    }

    /**
     * @return the path of page names list
     */
    private String getPagenameListPath()
    {
        return this.PageNameListPath;
    }
    
    /**
     * 
     * @param pageName the name of a wootPage to add in the list  
     * @throws WootEngineException if problems occur while loading/storing the page list.
     * 
     */
    public void addPageNameInList(String pageName) throws WootEngineException{
        this.loadPageNameList();
        this.getPageNameList().add(pageName);
        this.storePageNameList();
    }
    
    /**
     * 
     * @param pageName the name of a wootPage to remove in the list 
     * @param number the number of first occurrences to remove 
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
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
    
    /**
     * @return the page names list
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    public List getCurrentPageNameList() throws WootEngineException{
        this.loadPageNameList();
        return this.getPageNameList();
    }
}
