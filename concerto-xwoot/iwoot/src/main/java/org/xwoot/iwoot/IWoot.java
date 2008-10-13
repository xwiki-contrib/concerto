package org.xwoot.iwoot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;

public class IWoot
{
    private XWootAPI xwoot;
    private WikiContentManager wcm;
    private Integer id;
    
    public IWoot(XWootAPI wootAPI, WikiContentManager wcm, Integer id)
    {
        super();
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
    
    private void disconnectXWoot() throws IWootException
    {
        try {
            this.xwoot.disconnectFromContentManager();
            this.xwoot.disconnectFromP2PNetwork();
        } catch (XWootException e) {
            throw new IWootException(this.id+" : Problem with XWoot",e);
        }  
    }

    private void reconnectXwoot() throws IWootException
    {
        try {
            this.xwoot.reconnectToP2PNetwork();
            this.xwoot.connectToContentManager(); 
        } catch (XWootException e) {
            throw new IWootException(this.id+" : Problem with XWoot",e);
        }  
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
                throw new IWootException(this.id+" : Problem with WikiContentManager (getListSpaceId)",e);
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
            this.wcm.setFields(pageId, page);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (setFields)",e);
        }
        return true;
    }
    
}
