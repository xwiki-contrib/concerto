package org.xwoot.iwoot.restApplication;
import java.util.List;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.resources.PagesResource;
import org.xwoot.iwoot.restApplication.resources.PageResource;
import org.xwoot.wikiContentManager.WikiContentManagerException;

public class RestApplication extends Application
{
   private IWoot iwoot;
    
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
        router.attach("/pages", PagesResource.class);
        // Defines a route for the resource "page"
        router.attach("/items/{itemName}", PageResource.class);

        return router;
    }

    /**
     * Returns the list of pages.
     *
     * @return the list of pages.
     * @throws IWootException 
     * 
     */
    public Map<String,Map> getPages() throws IWootException {
        return this.iwoot.getPages();
    }

    /**
     * Returns the list of pages ids.
     *
     * @return the list of pages ids.
     * @throws IWootException 
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
     * @throws WikiContentManagerException 
     * @throws WikiContentManagerException 
     */
    public Map getPage(String id) throws IWootException{
        return this.iwoot.getPage(id);
    }
    
    /**
     * Remove the page id.
     *
     * @param id : the id of the page to remove
     * @return boolean
     * @throws WikiContentManagerException 
     */
    public boolean removePage(String id) throws IWootException {
        return this.iwoot.removepage(id);
    }
    
    /**
     * Store the page .
     *
     * @param page : the page to store
     * @return boolean
     * @throws WikiContentManagerException 
     */
    public boolean storePage(String id,Map page) throws IWootException{
        return this.iwoot.storePage(id,page);
    }

    
}
