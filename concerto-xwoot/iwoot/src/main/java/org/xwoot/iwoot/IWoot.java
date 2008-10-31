package org.xwoot.iwoot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
    public static final String XML_NODE_NAME_XWIKIPAGELIST="XWikiPageList";
    public static final String XML_NODE_NAME_ENTRY_VALUE="Value";
    public static final String XML_NODE_NAME_ENTRY_KEY="Key";
    public static final String XML_NODE_NAME_ENTRY="Entry";
    public static final String XML_NODE_NAME_ENTRIES="Entries";
    public static final String XML_NODE_NAME_XWIKIPAGE="XWikiPage";
    public static final String XML_ATTRIBUTE_NAME_HREF="href";
    public static final String XML_ATTRIBUTE_NAME_LISTSIZE="size";
    public static final String XML_ATTRIBUTE_NAME_XWIKIPAGEID="id";

    public IWoot(XWootClientAPI wootAPI, WikiContentManager wcm, Integer id)
    { 
        this.xwoot = wootAPI;
        this.wcm = wcm;
        this.id=id;
        this.logger.info("Iwoot engine created. Id : "+id);
    }

//    public synchronized Map<String, Map> getPages() throws IWootException 
//    {
//        this.reconnectXwoot();
//        HashMap< String, Map> result=new HashMap<String, Map>();
//        Collection spaces;
//        try {
//            spaces = this.wcm.getListSpaceId(); 
//            Iterator i=spaces.iterator();
//            while(i.hasNext()){
//                String space=(String)i.next();
//                Collection pages=this.wcm.getListPageId(space);
//                Iterator j = pages.iterator();
//                while(j.hasNext()){
//                    String page = (String)j.next();
//                    Map pageMap=this.wcm.getFields(page);
//                    result.put(page, pageMap);
//                }
//            }
//        } catch (WikiContentManagerException e) {
//            throw new IWootException(this.id+" : Problem with WikiContentManager when getting pages",e);
//        }
//        this.disconnectXWoot();
//        return result;
//    }

    private void disconnectXWoot()
    {
        this.xwoot.disconnectFromContentManager();  
    }

    private void reconnectXwoot() 
    {
        this.xwoot.connectToContentManager(); 
    }

    private synchronized List<String> getPagesNames() throws IWootException
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
    
    public synchronized Document getPageList(String pagesHRef) throws IWootException{
        List<String> list=this.getPagesNames();
        if (list!=null){

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance (); 
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder ();
            } catch (ParserConfigurationException e) {
                throw new IWootException(e);
            } 
            
            Document document=builder.newDocument();

            // Propriétés du DOM
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            // Création de l'arborescence du DOM
            Element racine = document.createElement(IWoot.XML_NODE_NAME_XWIKIPAGELIST);
            racine.setAttribute(IWoot.XML_ATTRIBUTE_NAME_LISTSIZE, String.valueOf(list.size()));

            Iterator i=list.iterator();
            Element page=null;

            while(i.hasNext()){                    
                String k=(String) i.next();

                page=document.createElement(IWoot.XML_NODE_NAME_XWIKIPAGE);
                page.setAttribute(IWoot.XML_ATTRIBUTE_NAME_XWIKIPAGEID, k);
                page.setAttribute(IWoot.XML_ATTRIBUTE_NAME_HREF, pagesHRef+"/"+k);
                racine.appendChild(page);
            }
            document.appendChild(racine);
            document.normalizeDocument(); 
            return document;
        }
        return null;
    }

    public synchronized Document getPage(String pageId,String href) throws IWootException
    {
        Map pageMap=null;
        try {
            pageMap=this.wcm.getFields(pageId);
            return getPage(pageId,href,pageMap);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (getFields)",e);
        }
       
    }
    
    static public synchronized Document getPage(String pageId,String href,Map pageMap) throws IWootException
    {
        if (pageMap!=null){
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance (); 
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder ();
            } catch (ParserConfigurationException e) {
                throw new IWootException(e);
            } 
            
            Document document=builder.newDocument();
            
            // Propriétés du DOM
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            // Création de l'arborescence du DOM
            Element racine = document.createElement(IWoot.XML_NODE_NAME_XWIKIPAGE);
            racine.setAttribute(IWoot.XML_ATTRIBUTE_NAME_XWIKIPAGEID, pageId);
            racine.setAttribute(IWoot.XML_ATTRIBUTE_NAME_HREF, href);
            Element entries = document.createElement(IWoot.XML_NODE_NAME_ENTRIES);  
           
            Iterator i=pageMap.entrySet().iterator(); 
            Element entry=null;
            Element key=null;
            Element value=null;
           
            while(i.hasNext()){
                Entry k=(Entry) i.next();

                entry=document.createElement(IWoot.XML_NODE_NAME_ENTRY);

                key = document.createElement(IWoot.XML_NODE_NAME_ENTRY_KEY);
                key.appendChild(document.createTextNode((String)k.getKey()));
                entry.appendChild(key);

                value = document.createElement(IWoot.XML_NODE_NAME_ENTRY_VALUE);
                value.appendChild(document.createTextNode((String)k.getValue()));
                entry.appendChild(value);

                entries.appendChild(entry);
            }
            racine.appendChild(entries);
            document.appendChild(racine);
            document.normalizeDocument();
            return document;
        } 
        return null;
    }



    public synchronized boolean removepage(String pageId) throws IWootException{
        try {
            return this.wcm.removePage(pageId);
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (removePage)",e);
        }
    }

    public synchronized boolean storePage(String pageId,Document document) throws IWootException{
        Map resultMap=new Hashtable<String, String>();
        NodeList entries=document.getFirstChild().getFirstChild().getChildNodes();
        for(int i=0;i<entries.getLength();i++){
            if (entries.item(i).getChildNodes().item(0).getNodeName().equals(XML_NODE_NAME_ENTRY_KEY)){
                resultMap.put(entries.item(i).getChildNodes().item(0).getTextContent(),entries.item(i).getChildNodes().item(1).getTextContent());
            }else {
                resultMap.put(entries.item(i).getChildNodes().item(1).getTextContent(),entries.item(i).getChildNodes().item(0).getTextContent());
            }
        }
        try {    
            return (!(this.wcm.setFields(pageId, resultMap)==null));          
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

    public boolean createPage(Document newPage) throws IWootException
    {
        Map resultMap=new Hashtable<String, String>();
        NodeList entries=newPage.getFirstChild().getFirstChild().getChildNodes();
        for(int i=0;i<entries.getLength();i++){
            if (entries.item(i).getChildNodes().item(0).getNodeName().equals(XML_NODE_NAME_ENTRY_KEY)){
                resultMap.put(entries.item(i).getChildNodes().item(0).getTextContent(),entries.item(i).getChildNodes().item(1).getTextContent());
            }else {
                resultMap.put(entries.item(i).getChildNodes().item(1).getTextContent(),entries.item(i).getChildNodes().item(0).getTextContent());
            }
        }
        if (!resultMap.containsKey(WikiContentManager.ID) || !resultMap.containsKey(WikiContentManager.CONTENT)){
            return false;
        }
        try {
            this.wcm.createPage((String)resultMap.get(WikiContentManager.ID),(String) resultMap.get(WikiContentManager.CONTENT));
        } catch (WikiContentManagerException e) {
            throw new IWootException(this.id+" : Problem with WikiContentManager (createPage)",e);
        }
        return true;       
    }

    public XWootClientAPI getXwoot()
    {
        return this.xwoot;
    }

}
