package org.xwoot.xwootApp.core;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.xwoot.XWootId;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Manages a content id map which associates a {@link XWootId} to a set of content id, gets the map, adds content id or
 * removes a given number of occurrences of a content id in an entry.
 * <p>
 * It is used by {@link XWoot} to store last modified contents id. The {@link XWoot} user can consume contents id of the
 * Map.
 * 
 * @version $Id$
 */
public class LastModifiedContentIdMap
{
    /** The name of the file where to serialize. */
    public static final String CONTENTID_MAP_FILE_NAME = "contentIdMap";

    /** map : ({@link XWootId}pageName,contentId set). */
    private Map<XWootId, Set<String>> contentIdMap;

    /** The file path where to serialize. */
    private String contentIdMapFilePath;

    /**
     * Creates a new LastModifiedContentIdMap instance to be used by the {@link ContentManager}. The PageManager will
     * add the id of each modified content in a page. A consumer can gets the Map and removes some occurrences of a
     * given ContentId in an entry.
     * 
     * @param xwootWorkingDirPath the workingDir for the XWoot the page manager belongs to.
     */
    public LastModifiedContentIdMap(String xwootWorkingDirPath)
    {
        this.contentIdMapFilePath = xwootWorkingDirPath + File.separator + CONTENTID_MAP_FILE_NAME;
    }

    /**
     * Serializes the map.
     * 
     * @throws WootEngineException if serialization or file access problems occur.
     */
    private void storeContentIdMap() throws XWootException
    {
        if (!this.contentIdMap.isEmpty()) {
            try {
                PersistencyUtil.saveObjectToXml(this.contentIdMap, this.getContentIdMapFilePath());
            } catch (Exception e) {
                throw new XWootException("Problems storing the content id Map.", e);
            }
        } else {
            new File(this.getContentIdMapFilePath()).delete();
        }
    }

    /**
     * Loads the map previously stored. Creates a new map if it has never been stored.
     * 
     * @throws WootEngineException if the loading causes deserializing problems.
     */
    private void loadContentIdMap() throws XWootException
    {
        String filePath = this.getContentIdMapFilePath();

        if (!new File(filePath).exists()) {
            this.setContentIdMap(new Hashtable<XWootId, Set<String>>());
        } else {
            try {
                Hashtable<XWootId, Set<String>> map =
                    (Hashtable<XWootId, Set<String>>) PersistencyUtil.loadObjectFromXml(filePath);
                if (map == null) {
                    this.setContentIdMap(new Hashtable<XWootId, Set<String>>());
                } else {
                    this.setContentIdMap(map);
                }
            } catch (Exception e) {
                throw new XWootException("Problems loading the content id Map.", e);
            }
        }
    }

    /**
     * @return the map.
     */
    private Map<XWootId, Set<String>> getContentIdMap()
    {
        return this.contentIdMap;
    }

    /**
     * @param map the map to set.
     */
    private void setContentIdMap(Map<XWootId, Set<String>> map)
    {
        this.contentIdMap = map;
    }

    /**
     * @return the path of the file where the map is serialized.
     */
    private String getContentIdMapFilePath()
    {
        return this.contentIdMapFilePath;
    }

    /**
     * @param pageName the {@link XWootId} corresponding to the name of container page of the content id to add.
     * @param contentId the content id to add.
     * @throws WootEngineException if problems occur while loading/storing the Map.
     */
    public void add(XWootId pageName, String contentId) throws XWootException
    {
        this.loadContentIdMap();
        Set contents = this.getContentIdMap().get(pageName);
        if (contents == null) {
            // use a Vector to allow duplicate contentId
            contents = new TreeSet<String>();
            this.getContentIdMap().put(pageName, contents);
        }
        contents.add(contentId);

        this.storeContentIdMap();
    }

    // /**
    // * @param pageName the name of container page of the content id to add.
    // * @param contentId the content id to remove.
    // * @param number the number of first occurrences to remove
    // * @throws WootEngineException if problems occur while loading/storing the Map.
    // */
    // public void removeNOccurrencyOfContentId(String pageName, String contentId, int number) throws XWootException
    // {
    // this.loadContentIdMap();
    // Set contents = this.getContentIdMap().get(pageName);
    // if (contents == null) {
    // return;
    // }
    //
    // int index = contents.indexOf(contentId);
    //
    // for (int i = 0; i < number && index != -1; i++) {
    // contents.remove(index);
    // index = contents.indexOf(contentId);
    // }
    // if (index == -1 || contents.size() == 0) {
    // this.getContentIdMap().remove(pageName);
    // }
    // this.storeContentIdMap();
    // }

    /**
     * @return the map, not as it is in memory, but as it is stored on drive.
     * @throws WootEngineException if problems occur while loading the Map.
     */
    public Map<XWootId, Set<String>> getCurrentMap() throws XWootException
    {
        this.loadContentIdMap();
        return this.getContentIdMap();
    }

    public void remove(XWootId id) throws XWootException
    {
        this.loadContentIdMap();
        this.contentIdMap.remove(id);
        this.storeContentIdMap();
    }
}
