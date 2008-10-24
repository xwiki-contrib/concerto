package org.xwoot.mockiphone.iwootclient.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwoot.mockiphone.iwootclient.IWootClient;

public class IWootMockClient implements IWootClient
{
    private Map contents;

    private String uri;
    
    public IWootMockClient(String uri){
        this.uri=uri;
        this.contents=new HashMap<String, String>();   
        for(int i=0;i<10;i++){
           this.contents.put("Page"+i,"Page"+i+"Content");
        }
    }

    public List getPageList(){
        return new ArrayList<String>(this.contents.keySet());
    }
    
    public boolean putPage(String pageName,Map page){
        if (this.contents.containsKey(pageName)){
            this.contents.put(pageName, page.get("Content"));
        }
        else{
            this.createPage(pageName, (String)page.get("Content"));
        }
        return true;
    }
    
    public Map getPage(String pageName){
        Map result = new HashMap<String, String>();
        result.put("id", pageName);
        result.put("content",this.contents.get(pageName));
        result.put("pageId",pageName);
        return result;
    }
    
    public void createPage(String pageName,String content){
        this.contents.put(pageName,content);
    }


    public void removePage(String pageName)
    {
       this.contents.remove(pageName);
        
    }
    
    public String getUri()
    {
        return this.uri;
    }
}
