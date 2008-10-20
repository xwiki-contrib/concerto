package org.xwoot.iwoot.restApplication.resources;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StreamRepresentation;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class PageResourceTest
{
    static private RestApplication RESTAPI;

    static private WikiContentManager WCM;

    static private Component COMPONENT;

    static private Client CLIENT;

    /** Base application URI. */
    private static final String APPLICATION_URI = "http://localhost:8182/iwoot";
    
    private static final String TESTSPACENAME = "SpaceTest";

    @BeforeClass
    static public void setup() throws WikiContentManagerException, XWootClientException
    {
        // Create a new WikiContentManager (resources container)
        WCM = WikiContentManagerFactory.getMockFactory().createWCM();

        // Create a new IWoot module (interface between IPhone and XWoot)
        IWoot iwoot = new IWoot(XWootClientFactory.getMockFactory().createXWootClient(), WCM, Integer.valueOf(1));

        // Create a new REST api which use IWoot to access to resources
        RESTAPI = new RestApplication(iwoot);

        // Create new REstlet Component (Servers managements)
        COMPONENT = new Component();

        // Add a new HTTP server listening on port 8182.
        COMPONENT.getServers().add(Protocol.HTTP, 8182);

        // Attach the sample application.
        COMPONENT.getDefaultHost().attach("/iwoot", RESTAPI);

        // Start the component.
        try {
            COMPONENT.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Define an HTTP client.
        CLIENT = new Client(Protocol.HTTP);

    }

    @AfterClass
    static public void finalTearDown() throws Exception
    {
        // Stop the server
        COMPONENT.stop();
    }

    /**
     * 
     *  Test to get an existing page.
     * 
     * @throws WikiContentManagerException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     */
    @Test
    public void testGetPage() throws WikiContentManagerException, IOException, IllegalArgumentException,
        ClassNotFoundException
    {
        String pageId=TESTSPACENAME+".page0";
        String pageContent="Content of existing page";
       
        // Create the resource reference
        Reference reference = new Reference(APPLICATION_URI + "/" + PagesResource.KEY + "/" + pageId);
        
        // Use client to get the wanted resource 
        Response response = CLIENT.get(reference);
        
        // Verify not found
        Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        
        // Create a new Page in resources container
        Map page = WCM.createPage(pageId, pageContent);
       
        // Use client to get the wanted resource
        response = CLIENT.get(reference);

        // Verify success
        Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
        // verify entity
        Assert.assertTrue(response.isEntityAvailable());

        // get The resource object in the response entity
        StreamRepresentation representation = new ObjectRepresentation<Serializable>(response.getEntity());
        Map pageTemp = (Map) ((ObjectRepresentation<Serializable>) representation).getObject();

        // verify resource equality
        Assert.assertEquals(page.get(WikiContentManager.ID), pageTemp.get(WikiContentManager.ID));
        Assert.assertEquals(page.get(WikiContentManager.CONTENT), pageTemp.get(WikiContentManager.CONTENT));
    }

    /**
     * Test to remove an existing page.
     * 
     * @throws WikiContentManagerException
     */
    @Test
    public void testRemoveRepresentations() throws WikiContentManagerException
    {
        String pageId=TESTSPACENAME+".test";
        String pageContent="Page to be deleted ! ";
        // Create the resource to remove
        if (!WCM.existPage(pageId)) {
            WCM.createPage(pageId, pageContent);
        }

        // create the resource reference
        Reference pageUri = new Reference(APPLICATION_URI + "/" + PagesResource.KEY + "/" + pageId);
        Request request = new Request(Method.DELETE, pageUri);

        // verify the resource exist
        Assert.assertEquals(pageContent, WCM.getPageContent(pageId));

        // Use client to delete resource
        Response resp = CLIENT.handle(request);

        // verify success
        Assert.assertEquals(Status.SUCCESS_NO_CONTENT, resp.getStatus());

        // verify resource is deleted
        Assert.assertFalse(WCM.existPage(pageId));

    }

    /**
     * Test to create a new page, and update it.
     * 
     * @throws WikiContentManagerException
     * 
     */
    @Test
    public void testCreateAndSetPage() throws WikiContentManagerException
    {
        String pageId=TESTSPACENAME+".test";
        String pageContent="Content of new page";
        String pageContent2="Modified Content of existing page";
        
        // The resource to create musn't exist
        if (WCM.existPage(pageId)) {
            WCM.removePage(pageId);
        }

        // verify the resource do not exist
        assertFalse(WCM.existPage(pageId));

        // Gathering informations into a Web form.
        Form form = new Form();
        form.add(WikiContentManager.ID, pageId);
        form.add(WikiContentManager.CONTENT, pageContent);
        Representation rep = form.getWebRepresentation();

        // Launch the request to create the resource
        Response response = CLIENT.post(APPLICATION_URI + "/" + PagesResource.KEY, rep);

        // verify success
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent, WCM.getPageContent(pageId));
        
        // get the URI of the created page
        Reference r=response.getEntity().getIdentifier();
        
        // Gathering new informations into a Web form.
        Form form2 = new Form();
        form2.add(WikiContentManager.ID, pageId);
        form2.add(WikiContentManager.CONTENT, pageContent2);
        Representation rep2 = form2.getWebRepresentation();
        
        // Launch the request to create the resource
        response = CLIENT.put(r, rep2);
        
        // verify success
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent2, WCM.getPageContent(pageId));
    }
    
    /**
     * Test to create a new page, and update it.
     * 
     * @throws WikiContentManagerException
     * @throws ClassNotFoundException 
     * @throws IOException 
     * @throws IllegalArgumentException 
     * 
     */
    @Test
    public void testGetPages() throws WikiContentManagerException, IllegalArgumentException, IOException, ClassNotFoundException
    {
        String pageId1="SpaceTest.page1";
        String pageId2="SpaceTest.page2";
        String pageContent1="Content of new page";
        String pageContent2="Content of newx page Blabla";
        
        WCM.removeSpace(TESTSPACENAME);
        
        /*****************/
        // Get empty list /
        /*****************/
        // Launch the request to get the resource list
        Response response = CLIENT.get(APPLICATION_URI + "/" + PagesResource.KEY);
        
        // get The list in the response entity
        StreamRepresentation representation = new ObjectRepresentation<Serializable>(response.getEntity());
        List pageTemp = (List) ((ObjectRepresentation<Serializable>) representation).getObject();
        assertEquals(0,pageTemp.size());
        
        /********************/
        // Create first page /
        /********************/
        
        // The resource to create musn't exist
        if (WCM.existPage(pageId1)) {
            WCM.removePage(pageId1);
        }

        // verify the resource do not exist
        assertFalse(WCM.existPage(pageId1));

        // Gathering informations into a Web form.
        Form form = new Form();
        form.add(WikiContentManager.ID,pageId1);
        form.add(WikiContentManager.CONTENT,pageContent1);
        Representation rep = form.getWebRepresentation();

        // Launch the request to create the resource
        response = CLIENT.post(APPLICATION_URI + "/" + PagesResource.KEY, rep);

        // verify success
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent1, WCM.getPageContent(pageId1));
        
        /***********************/
        // Get list with 1 page /
        /***********************/
        // Launch the request to get the resource list
        response = CLIENT.get(APPLICATION_URI + "/" + PagesResource.KEY);
        
        // get The list in the response entity
        representation = new ObjectRepresentation<Serializable>(response.getEntity());
        pageTemp = (List) ((ObjectRepresentation<Serializable>) representation).getObject();
        assertEquals(1,pageTemp.size());
        assertEquals(pageId1,pageTemp.get(0));
        
        /********************/
        // Create a second page /
        /********************/
        
        // The resource to create musn't exist
        if (WCM.existPage(pageId2)) {
            WCM.removePage(pageId2);
        }

        // verify the resource do not exist
        assertFalse(WCM.existPage(pageId2));

        // Gathering informations into a Web form.
        Form form2 = new Form();
        form2.add(WikiContentManager.ID, pageId2);
        form2.add(WikiContentManager.CONTENT, pageContent2);
        Representation rep2 = form2.getWebRepresentation();

        // Launch the request to create the resource
        response = CLIENT.post(APPLICATION_URI + "/" + PagesResource.KEY, rep2);

        // verify success
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent2, WCM.getPageContent(pageId2));
        
        /***********************/
        // Get list with 2 page /
        /***********************/
        // Launch the request to get the resource list
        response = CLIENT.get(APPLICATION_URI + "/" + PagesResource.KEY);
        
        // get The list in the response entity
        representation = new ObjectRepresentation<Serializable>(response.getEntity());
        pageTemp = (List) ((ObjectRepresentation<Serializable>) representation).getObject();
        assertEquals(2,pageTemp.size());
        assertEquals(pageId1,pageTemp.get(0));
        assertEquals(pageId2,pageTemp.get(1));
        
    }
    
    
    @Test
    public void testXTream() throws WikiContentManagerException {
        Map page=WCM.createPage("test.essai", "Dans ton cul lulu");
        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        xstream.alias("page", Map.class);
        
        System.out.println(xstream.toXML(page));     
    }

}
