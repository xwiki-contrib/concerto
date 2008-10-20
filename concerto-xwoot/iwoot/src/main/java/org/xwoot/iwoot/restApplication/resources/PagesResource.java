package org.xwoot.iwoot.restApplication.resources;

import java.io.Serializable;

import java.util.List;

import org.restlet.Context;

import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import org.restlet.resource.ResourceException;

import org.restlet.resource.Variant;

import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.RestApplication;


public class PagesResource extends BaseResource
{
    /** List of items. */
    List pagesNames;
    public final static String KEY="pages";

    public PagesResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get the items directly from the "persistence layer".
        try {
            this.pagesNames = ((RestApplication)getApplication()).getPagesNames();
        } catch (IWootException e) {    
            e.printStackTrace();
            this.pagesNames =null;
        }

        // modifications of this resource via POST requests are not allow 
        setModifiable(true);

        // Declare the kind of representations supported by this resource.
        getVariants().add(new Variant(RestApplication.USINGMEDIATYPE));
    }

    /**
     * Returns a listing of all registered items.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (this.pagesNames!=null){
            return this.getRepresentation(variant, (Serializable)this.pagesNames);
        }   
        return null;
    }

    /**
     * Handle POST requests: create a new page.
     * 
     *  OK => SUCCESS_CREATED (HTTP RFC - 10.2.2 201 Created)
     *  
     *  ALREADY EXIST => CLIENT_ERROR_CONFLICT (HTTP RFC - 10.4.10 409 Conflict)
     *  ERROR DURING CREATION (missing parameters) => CLIENT_ERROR_UNPROCESSABLE_ENTITY (WEBDAV RFC - 10.3 422 Unprocessable Entity)
     *  PROBLEM DURING CREATION (catch exception) => SERVER_ERROR_INTERNAL (HTTP RFC - 10.5.1 500 Internal Server Error)
     *  
     */
    @Override
    public void acceptRepresentation(Representation entity)
    throws ResourceException {
        // Parse the given representation and retrieve pair of
        // "name=value" tokens.
        Form form = new Form(entity);
        String pageId = form.getFirstValue(PageResource.KEY);
        try{
            // Check that the item is not already registered.
            if (((RestApplication)getApplication()).exist(pageId)) {
                getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
            }else {
                if (!((RestApplication)getApplication()).createPage(form)){
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                }
            }
        }catch(IWootException e){
            e.printStackTrace();
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        // Set the response's status and entity
        getResponse().setStatus(Status.SUCCESS_CREATED);
        Representation rep = new StringRepresentation("Item created",RestApplication.USINGMEDIATYPE);
        // Indicates where is located the new resource.
        rep.setIdentifier(getRequest().getResourceRef().getIdentifier()
            + "/" + pageId);
        getResponse().setEntity(rep);
    }
}
