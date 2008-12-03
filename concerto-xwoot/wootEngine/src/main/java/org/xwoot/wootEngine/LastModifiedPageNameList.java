package org.xwoot.wootEngine;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Manages a page name list, gets pagename list, adds pagename or removes a given number of occurrences of a pagename.
 * <p>
 * It is used by {@link PageManager} to store last modified page names. The {@link WootEngine} user can consume
 * pagenames of the list.
 * 
 * @version $Id:$
 */
public class LastModifiedPageNameList extends LoggedWootExceptionThrower
{
    /** The name of the file where to serialize. */
    public static final String PAGENAME_LIST_FILE_NAME = "pageList";

    /** The list of the page names. */
    private List<String> pageNameList;

    /** The file path where to serialize. */
    private String pageNameListFilePath;

    /**
     * Creates a new LastModifiedPageNameList instance to be used by the {@link PageManager}. The PageManager will add
     * the name of each modified page. A consumer can get the list and remove some occurrences of a given pageName.
     * 
     * @param wootEngineWorkingDirPath the workingDir for the WootEngine the page manager belongs to.
     */
    public LastModifiedPageNameList(String wootEngineWorkingDirPath)
    {
        this.logger = LogFactory.getLog(this.getClass());
        this.pageNameListFilePath = wootEngineWorkingDirPath + File.separator + PAGENAME_LIST_FILE_NAME;
    }

    /**
     * Serializes the list of page names.
     * 
     * @throws WootEngineException if serialization or file access problems occur.
     */
    private void storePageNameList() throws WootEngineException
    {
        try {
            FileUtil.saveCollectionToXml(this.pageNameList, this.getPagenameListFilePath());
        } catch (Exception e) {
            this.throwLoggedException("Problems storing the modified pages name list.", e);
        }
    }

    /**
     * Loads the list of the page names previously stored. Creates a new list if it has never been stored.
     * 
     * @throws WootEngineException if the loading causes deserializing problems.
     */
    @SuppressWarnings("unchecked")
    private void loadPageNameList() throws WootEngineException
    {
        String filePath = this.getPagenameListFilePath();

        if (!new File(filePath).exists()) {
            this.setPageNameList(new Vector<String>());
        } else {
            try {
                this.setPageNameList((Vector<String>) FileUtil.loadObjectFromXml(filePath));
            } catch (Exception e) {
                this.throwLoggedException("Problems loading the modified pages name list.", e);
            }
        }
    }

    /**
     * @return the list of page names that were modified.
     */
    private List<String> getPageNameList()
    {
        return this.pageNameList;
    }

    /**
     * @param list the list of the page names to set
     */
    private void setPageNameList(List<String> list)
    {
        this.pageNameList = list;
    }

    /**
     * @return the path of the file where the page names list is saved.
     */
    private String getPagenameListFilePath()
    {
        return this.pageNameListFilePath;
    }

    /**
     * @param pageName the name of a wootPage to add in the list
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    public void addPageNameInList(String pageName) throws WootEngineException
    {
        this.loadPageNameList();
        this.getPageNameList().add(pageName);
        this.storePageNameList();
    }

    /**
     * @param pageName the name of a wootPage to remove in the list
     * @param number the number of first occurrences to remove
     * @throws WootEngineException if problems occur while loading/storing the page list.
     */
    public void removeNOccurencesInList(String pageName, int number) throws WootEngineException
    {
        this.loadPageNameList();
        int index = this.getPageNameList().indexOf(pageName);

        for (int i = 0; i < number && index != -1; i++) {
            this.getPageNameList().remove(index);
            index = this.getPageNameList().indexOf(pageName);
        }
        this.storePageNameList();
    }

    /**
     * @return the page names list, not as it is in memory, but as it is stored on drive.
     * @throws WootEngineException if problems occur while loading the page list.
     */
    public List<String> getCurrentPageNameList() throws WootEngineException
    {
        this.loadPageNameList();
        return this.getPageNameList();
    }
}
