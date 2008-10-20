package org.xwoot.iwoot.restApplication.resources;

import java.io.Serializable;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.RestApplication;

public class PageResource extends BaseResource
{
    
    /** The sequence of characters that identifies the resource. */
    String id;

    /** The underlying resource object. */
    Map page;
    
    public final static String KEY="id";

    public PageResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get the "id" attribute value taken from the URI template
        // /pages/{id}.
        this.id = (String) getRequest().getAttributes().get(PageResource.KEY);

        // Get the page directly from the "persistence layer".
        try {
            this.page = ((RestApplication)getApplication()).getPage(this.id);
        } catch (IWootException e) {
            e.printStackTrace();
            this.page=null;
        }
  
        if (this.page != null) {
            // Define the supported variant.
            getVariants().add(new Variant(RestApplication.USINGMEDIATYPE));
            // By default a resource cannot be updated.
            setModifiable(true);
        } else {
            // This resource is not available.
            setAvailable(false);
        }
    }

    /**
     * 
     * Handle DELETE requests.
     * 
     * OK => SUCCESS_NO_CONTENT (HTTP RFC - 10.2.5 204 No Content)
     * 
     * PROBLEM DURING REMOVED (catch exception) => SERVER_ERROR_INTERNAL (HTTP RFC - 10.5.1 500 Internal Server Error)
     * NO DELETION => CLIENT_ERROR_EXPECTATION_FAILED (HTTP RFC - 10.4.18 417 Expectation Failed)
     * 
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        boolean isRemoved=true;
        
        if (this.page != null) {
            // Remove the item from the list.
            try {
                isRemoved=((RestApplication)getApplication()).removePage(this.id);
            } catch (IWootException e) {
                e.printStackTrace();
                this.page=null;
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return ;
            }
        }
        if (isRemoved){
            // Tells the client that the request has been successfully fulfilled.
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);     
        }
        else{
            getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED);
        }        
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
           return this.getRepresentation(variant, (Serializable) this.page);
    }

    /**
     * Handle PUT requests.
     * 
     * OK CREATED => SUCCESS_CREATED (HTTP RFC - 10.2.2 201 Created)
     * OK UPDATED => SUCCESS_OK (HTTP RFC - 10.2.1 200 OK)
     * 
     * ERROR DURING CREATION/UPDATE (missing parameters) => CLIENT_ERROR_UNPROCESSABLE_ENTITY (WEBDAV RFC - 10.3 422 Unprocessable Entity)
     * PROBLEM DURING CREATION/UPDATE (catch exception) => SERVER_ERROR_INTERNAL (HTTP RFC - 10.5.1 500 Internal Server Error)
     * 
     */
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        // Tells if the item is to be created of not.
        boolean creation = (this.page == null);

        // The PUT request updates or creates the resource.
       /* if (this.page == null) {
            this.page = new Map...;
        }*/
        Map pageTemp=this.getPage(entity);
           
        if (pageTemp!=null) {
            try {
                if (!((RestApplication)Application.getCurrent()).storePage(this.id,pageTemp)){
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                    return ;
                }
            } catch (IWootException e) {
                e.printStackTrace();
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
        else {
            try {
                Form form = new Form(entity);
                if (!((RestApplication)Application.getCurrent()).createPage(form)){
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                }
            } catch (IWootException e) {
                e.printStackTrace();
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
        
        if (creation) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }
    }
}
