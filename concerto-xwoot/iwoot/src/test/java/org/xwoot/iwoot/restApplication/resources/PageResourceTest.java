package org.xwoot.iwoot.restApplication.resources;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;
import org.xwoot.wikiContentManager.mockWikiContentManager.MockWikiContentManager;

public class PageResourceTest
{
    private RestApplication restapi;
    
    @Before
    public void setup() throws XWootClientException, WikiContentManagerException{
        IWoot iwoot=new IWoot(XWootClientFactory.getMockFactory().createXWootClient()
            ,WikiContentManagerFactory.getMockFactory().createWCM());
        this.restapi = new RestApplication(iwoot);
    }
    
    @Test
    public void testRemoveRepresentations()
    {
        Context context = new Context();
        Request request = new Request();
        Response response = new Response(request);

        PageResource page=new PageResource(context,request,response);
    }

    @Test
    public void testPageResource()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testRepresentVariant()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testStoreRepresentationRepresentation()
    {
        fail("Not yet implemented");
    }

}
