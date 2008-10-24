package org.xwoot.iwoot.restApplication;
import java.util.List;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.resources.PagesResource;
import org.xwoot.iwoot.restApplication.resources.PageResource;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

public class RestApplication extends Application
{
   private IWoot iwoot;
   public final static MediaType USINGMEDIATYPE=MediaType.APPLICATION_JAVA_OBJECT;
    
    public RestApplication() {
        super();
        System.out.println("Bootstrap IWoot !");
        // Create a new WikiContentManager (resources container)
        WikiContentManager WCM;
     
        // Create a new IWoot module (interface between IPhone and XWoot)
        try {
            
            WCM = WikiContentManagerFactory.getMockFactory().createWCM();
           
            
            this.iwoot = new IWoot(XWootClientFactory.getMockFactory().createXWootClient(), WCM, Integer.valueOf(1));

            String pageId="test.page0";
            String pageContent="Content of existing page";
            WCM.createPage(pageId, pageContent);

      
        } catch (XWootClientException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RestApplication(IWoot iwoot) {
        super();
        this.iwoot=iwoot;
        
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot() {
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of pages"
        router.attach("/"+PagesResource.KEY, PagesResource.class);
        // Defines a route for the page resource : /iwoot/pages/{id} 
        // {id} is a generic value for the pagename like : /iwoot/pages/space1.page1
        router.attach("/"+PagesResource.KEY+"/{"+PageResource.KEY+"}", PageResource.class);

        return router;
    }

    /**
     * Returns the list of pages.
     *
     * @return the list of pages.
     * 
     */
    public Map<String,Map> getPages() throws IWootException {
        return this.iwoot.getPages();
    }

    /**
     * Returns the list of pages ids.
     *
     * @return the list of pages ids.
     *  
     */
    public List getPagesNames() throws IWootException  {
        return this.iwoot.getPagesNames();
    }
    
    /**
     * Return the page id.
     *
     * @param id : the id of the wanted page
     * @return the page id.
     */
    public Map getPage(String id) throws IWootException{
        return this.iwoot.getPage(id);
    }
    
    /**
     * Remove the page id.
     *
     * @param id : the id of the page to remove
     * @return boolean
     */
    public boolean removePage(String id) throws IWootException {
        return this.iwoot.removepage(id);
    }
    
    /**
     * Store the page .
     *
     * @param page : the page to store
     * @return boolean
     */
    public boolean storePage(String id,Map page) throws IWootException{
        return this.iwoot.storePage(id,page);
    }

    public boolean exist(String pageId) throws IWootException
    {
        return this.iwoot.existPage(pageId);
    }

    public boolean createPage(Form form) throws IWootException
    {
       return this.iwoot.createPage(form.getValuesMap());
    }
    
}
