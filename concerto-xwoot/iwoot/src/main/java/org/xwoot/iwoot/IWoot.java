package org.xwoot.iwoot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;

public class IWoot
{
    private XWootClientAPI xwoot;
    private WikiContentManager wcm;
    private Integer id;
    
    //logger
    private final Log logger = LogFactory.getLog(this.getClass());
    
    public IWoot(XWootClientAPI wootAPI, WikiContentManager wcm, Integer id)
    { 
        this.xwoot = wootAPI;
        this.wcm = wcm;
        this.id=id;
    }

    public synchronized Map<String, Map> getPages() throws IWootException 
    {
        this.reconnectXwoot();
        HashMap< String, Map> result=new HashMap<String, Map>();
        Collection spaces;
        try {
            spaces = this.wcm.getListSpaceId(); 
            Iterator i=spaces.iterator();
            while(i.hasNext()){
                String space=(String)i.next();
                Collection pages=this.wcm.getListPageId(space);
                Iterator j = pages.iterator();
                while(j.hasNext()){
                    String page = (String)j.next();
                    Map pageMap=this.wcm.getFields(page);
                    result.put(page, pageMap);
                }
            }
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager when getting pages",e);
        }
        this.disconnectXWoot();
        return result;
    }
    
    private void disconnectXWoot()
    {
        this.xwoot.disconnectFromContentManager();  
    }

    private void reconnectXwoot() 
    {
        this.xwoot.connectToContentManager(); 
    }

    public synchronized List<String> getPagesNames() throws IWootException
    { 
        this.reconnectXwoot();
        ArrayList< String> result=new ArrayList<String>();
        Collection spaces;
        try {
            spaces = this.wcm.getListSpaceId();
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (getListSpaceId)",e);
        }
        Iterator i=spaces.iterator();
        while(i.hasNext()){
            String space=(String)i.next();
            try {
                Collection pages=this.wcm.getListPageId(space);
                result.addAll(pages);
            } catch(WikiContentManagerException e ){
                throw new IWootException(this.id+" : Problem with WikiContentManager (getListPageId)",e);
            }  
        }
        this.disconnectXWoot();
        return result;
    }

    public synchronized Map getPage(String pageId) throws IWootException
    {
        try {
            return this.wcm.getFields(pageId);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (getFields)",e);
        }
    }
    
    public synchronized boolean removepage(String pageId) throws IWootException{
        try {
            return this.wcm.removePage(pageId);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (removePage)",e);
        }
    }
    
    public synchronized boolean storePage(String pageId,Map page) throws IWootException{
        try {
            return (!(this.wcm.setFields(pageId, page)==null));          
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (setFields)",e);
        }
    }

    public boolean existPage(String pageId) throws IWootException
    {
        try {
            return this.wcm.existPage(pageId);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (existPage)",e);  
        }
    }

    public boolean createPage(Map<String, String> valuesMap) throws IWootException
    {
        if (!valuesMap.containsKey(WikiContentManager.ID) || !valuesMap.containsKey(WikiContentManager.CONTENT)){
            return false;
        }
        try {
            this.wcm.createPage(valuesMap.get(WikiContentManager.ID), valuesMap.get(WikiContentManager.CONTENT));
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (createPage)",e);
        }
        return true;       
    }
    
}
