package org.xwoot.iwoot.restApplication.resources;


import java.io.Serializable;

import java.util.List;


import org.restlet.Application;
import org.restlet.Context;

import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;

import org.restlet.resource.ResourceException;
import org.restlet.resource.StreamRepresentation;

import org.restlet.resource.Variant;

import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.wikiContentManager.WikiContentManagerException;


public class PagesResource extends BaseResource
{
    /** List of items. */
    List pagesNames;

    public PagesResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get the items directly from the "persistence layer".
        try {
            this.pagesNames = ((RestApplication)Application.getCurrent()).getPagesNames();
        } catch (WikiContentManagerException e) {    
            e.printStackTrace();
            this.pagesNames =null;
        }

        // modifications of this resource via POST requests are not allow 
        setModifiable(false);

        // Declare the kind of representations supported by this resource.
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    /**
     * Returns a listing of all registered items.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType()) && this.pagesNames!=null) {    
            StreamRepresentation representation = new ObjectRepresentation<Serializable>((Serializable)this.pagesNames);
            // Returns the XML representation of this document.
            return representation;
        }
        return null;
    }
}
