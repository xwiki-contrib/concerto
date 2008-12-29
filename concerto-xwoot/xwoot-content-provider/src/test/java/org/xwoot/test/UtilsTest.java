package org.xwoot.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.xmlrpc.XmlRpcException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.Utils;
import org.xwoot.XWootObject;

import junit.framework.TestCase;

public class UtilsTest extends TestCase
{
    protected XWikiXmlRpcClient rpc;

    protected Random random;

    public void setUp() throws Exception
    {
        rpc = new XWikiXmlRpcClient(TestConstants.ENDPOINT);
        rpc.login(TestConstants.USERNAME, TestConstants.PASSWORD);
        random = new Random();
    }

    public void tearDown() throws Exception
    {
        rpc.logout();
        rpc = null;
    }

    public void testXWikiObjectToXWootObject() throws XmlRpcException
    {
        List<XWikiObjectSummary> objectSummaries = rpc.getObjects("Main.WebHome");
        
        for(XWikiObjectSummary objectSummary : objectSummaries) {
            XWikiObject object = rpc.getObject("Main.WebHome", objectSummary.getGuid());
            
            XWootObject xwo = Utils.xwikiObjectToXWootObject(object, false);
            
            System.out.format("%s\n", xwo);
        }
        
    }

    public void testXWootObjectToXWikiPageAndViceversa() throws XmlRpcException {
        String content = String.format("This is a modified content %d\n", random.nextInt()); 
        
        XWikiPage page = rpc.getPage("Main.WebHome");
        
        XWootObject xwootObject = Utils.xwikiPageToXWootObject(page, false);
        System.out.format("%s\n", xwootObject);
        xwootObject.setFieldValue("content", content);        
        
        page = Utils.xwootObjectToXWikiPage(xwootObject);
        rpc.storePage(page);
        
        page = rpc.getPage("Main.WebHome");
        
        assertTrue(content.equals(page.getContent()));
    }
    
    public void testXWootObjectToXWikiObjectAndViceversa() throws XmlRpcException {
        String value = "a\nb\nc";
        
        XWikiObject object = rpc.getObject("Main.WebHome", "XWiki.TagClass", 0);
        XWootObject xwootObject = Utils.xwikiObjectToXWootObject(object, false);
        
        xwootObject.setFieldValue("tags", value);
        
        object = Utils.xwootObjectToXWikiObject(xwootObject);
        
        rpc.storeObject(object);
        
        object = rpc.getObject("Main.WebHome", "XWiki.TagClass", 0);
        
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        
        assertTrue(list.equals(object.getProperty("tags")));
    }
    
    public void testWootableListConversion() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        
        XWikiObject object = new XWikiObject();
        object.setClassName("XWiki.TagClass");        
        object.setProperty("tags", list);
        
        XWootObject xwootObject = Utils.xwikiObjectToXWootObject(object, false);
        object = Utils.xwootObjectToXWikiObject(xwootObject);
        
        System.out.format("%s\n", object.getProperty("tags").getClass());
        
        assertTrue(List.class.isAssignableFrom(object.getProperty("tags").getClass()));
    }
}
