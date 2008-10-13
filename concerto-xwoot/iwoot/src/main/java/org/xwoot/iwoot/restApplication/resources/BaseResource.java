package org.xwoot.iwoot.restApplication.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;


public abstract class BaseResource extends Resource
{
    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }
    
}
