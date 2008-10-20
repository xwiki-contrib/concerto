package org.xwoot.iwoot.restApplication.resources;

import java.io.Serializable;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StreamRepresentation;
import org.restlet.resource.Variant;
import org.xwoot.iwoot.restApplication.RestApplication;


public abstract class BaseResource extends Resource
{
    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public Representation getRepresentation(Variant variant,Serializable object) {
        if (RestApplication.USINGMEDIATYPE.equals(variant.getMediaType())){
            StreamRepresentation representation = new ObjectRepresentation<Serializable>(object);
            return representation;
        }
        return null;
    }
    
    public Map getPage(Representation entity){
        Map result=null;
        if(entity!=null){ 
            // get the page 
            Form form = new Form(entity);
            result=form.getValuesMap();
        }
        return result;
    
    }
}
