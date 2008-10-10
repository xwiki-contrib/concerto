package org.xwoot.iwoot.restApplication.resources;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StreamRepresentation;
import org.restlet.resource.Variant;
import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.wikiContentManager.WikiContentManagerException;

public class PageResource extends BaseResource
{
    
    /** The sequence of characters that identifies the resource. */
    String id;

    /** The underlying Item object. */
    Map page;

    public PageResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get the "itemName" attribute value taken from the URI template
        // /items/{itemName}.
        this.id = (String) getRequest().getAttributes().get("id");

        // Get the item directly from the "persistence layer".
        try {
            this.page = ((RestApplication)Application.getCurrent()).getPage(this.id);
        } catch (WikiContentManagerException e) {
            e.printStackTrace();
            this.page=null;
        }

        if (this.page != null) {
            // Define the supported variant.
            getVariants().add(new Variant(MediaType.TEXT_XML));
            // By default a resource cannot be updated.
            setModifiable(true);
        } else {
            // This resource is not available.
            setAvailable(false);
        }
    }

    /**
     * Handle DELETE requests.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        boolean isRemoved=true;
        if (this.page != null) {
            // Remove the item from the list.
            try {
                isRemoved=((RestApplication)Application.getCurrent()).removePage(this.id);
            } catch (WikiContentManagerException e) {
                e.printStackTrace();
                this.page=null;
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
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {    
                StreamRepresentation representation = new ObjectRepresentation<Serializable>((Serializable)this.page);
                // Returns the XML representation of this document.
                return representation;
        }
        return null;
    }

    /**
     * Handle PUT requests.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        // Tells if the item is to be created of not.
        boolean creation = (this.page == null);

        // The PUT request updates or creates the resource.
        /*if (this.page == null) {
            this.page = new Item(itemName);
        }*/
        Map pageTemp=null;
        if(entity!=null){
            ObjectRepresentation representation;
            try {
                representation = new ObjectRepresentation<Serializable>(entity);
                pageTemp=(Map)representation.getObject();
            } catch (IllegalArgumentException e) { 
                e.printStackTrace();
            } catch (IOException e) {    
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }         
        }
        
        if (pageTemp!=null) {
            try {
                ((RestApplication)Application.getCurrent()).storePage(this.id,pageTemp);
            } catch (WikiContentManagerException e) {
                e.printStackTrace();
            }
        }
        
        if (creation) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }
    }

}
