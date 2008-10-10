package org.xwoot.iwoot.restApplication.resources;

import java.util.Map;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.iwoot.restApplication.RestApplication;


public abstract class BaseResource extends Resource
{
    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }
    
}
