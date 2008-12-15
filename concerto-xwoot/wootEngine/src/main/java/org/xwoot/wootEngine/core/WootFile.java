package org.xwoot.wootEngine.core;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;

import org.xwoot.wootEngine.WootEngineException;
import org.xwoot.xwootUtil.FileUtil;

/**
 * Handles a map of (id,{@link WootContent}) associated to a page name.
 * 
 * @version $Id$
 */
public class WootFile
{

    /** The name of the page associated to this WootFile. */
    private String pageName;

    /** Map of ({@link ContentId},{@link WootContent}). The associated page's contents. */
    private Map<ContentId, WootContent> contents;

    /**
     * Creates a new WootFile object associated to a given page name.
     * 
     * @param pageName the pageName of the new WootFile object.
     */
    public WootFile(String pageName)
    {
        this.setPageName(pageName);
        this.contents = new Hashtable<ContentId, WootContent>();
    }

    /**
     * @return the name of the file where those page's contents are being stored.
     * @throws WootEngineException if encoding problems are caused by the page's name.
     * @see FileUtil#getEncodedFileName(String)
     */
    public String getFileName() throws WootEngineException
    {
        String filename = "";

        try {
            filename = FileUtil.getEncodedFileName(this.getPageName());
        } catch (UnsupportedEncodingException e) {
            throw new WootEngineException("Problem with filename encoding", e);
        }

        return filename;
    }

    /**
     * @return the name of the page.
     */
    public String getPageName()
    {
        return this.pageName;
    }

    /**
     * @param pageName the pageName to set.
     * @throws IllegalArgumentException if pageName is a null or empty String.
     */
    public void setPageName(String pageName) throws IllegalArgumentException
    {
        if (pageName == null || pageName.length() == 0) {
            throw new IllegalArgumentException("Empty page names are not allowed.");
        }

        this.pageName = pageName;
    }

    /**
     * @return the contents map of this WootFile.
     */
    public Map<ContentId, WootContent> getContents()
    {
        return this.contents;
    }

    /**
     * @param contents the contents map to set.
     */
    public void setContents(Map<ContentId, WootContent> contents)
    {
        this.contents = contents;
    }

    /**
     * To add a content to the contents map of this WootFile.
     * 
     * @param wootContent the content to add.
     */
    public void addContent(WootContent wootContent)
    {
        this.contents.put(wootContent.getContentId(), wootContent);
    }
}
