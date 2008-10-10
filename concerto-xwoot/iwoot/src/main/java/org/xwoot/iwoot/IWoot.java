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

public class IWoot
{
    private XWootAPI xwoot;
    private WikiContentManager wcm;
    
    public IWoot(XWootAPI wootAPI, WikiContentManager wcm)
    {
        super();
        this.xwoot = wootAPI;
        this.wcm = wcm;
    }

    public synchronized Map<String, Map> getPages() throws Exception
    {
        this.reconnectXwoot();
        HashMap< String, Map> result=new HashMap<String, Map>();
        Collection spaces=this.wcm.getListSpaceId();
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
        this.disconnectXWoot();
        return result;
    }
    
    private void disconnectXWoot() throws Exception
    {
        this.xwoot.disconnectFromContentManager();
        this.xwoot.disconnectFromP2PNetwork();     
    }

    private void reconnectXwoot() throws Exception
    {
        this.xwoot.reconnectToP2PNetwork();
        this.xwoot.connectToContentManager(); 
    }

    public synchronized List<String> getPagesNames() throws Exception
    { 
        this.reconnectXwoot();
        ArrayList< String> result=new ArrayList<String>();
        Collection spaces=this.wcm.getListSpaceId();
        Iterator i=spaces.iterator();
        while(i.hasNext()){
            String space=(String)i.next();
            Collection pages=this.wcm.getListPageId(space);
            result.addAll(pages);
        }
        this.disconnectXWoot();
        return result;
    }

    public synchronized Map getPage(String id) throws WikiContentManagerException
    {
        return this.wcm.getFields(id);
    }
    
    public synchronized boolean removepage(String id) throws WikiContentManagerException{
        return this.wcm.removePage(id);
    }
    
    public synchronized boolean storePage(String id,Map page) throws WikiContentManagerException{
        this.wcm.setFields(id, page);
        return true;
    }
    
}
