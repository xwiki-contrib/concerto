package org.xwoot.mockiphone.iwootclient.rest;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StreamRepresentation;
import org.xwoot.mockiphone.iwootclient.IWootClient;
import org.xwoot.mockiphone.iwootclient.IWootClientException;

public class IWootRestClient implements IWootClient
{
    private Client client ; 
    
    static public String PAGESKEY="pages";

    private String uri;
    
    public IWootRestClient(String uri){
        this.uri=uri;
        this.client= new Client(Protocol.HTTP);   
    }
    
    //TODO voir pour mettre les uri en id de page 
    private Reference getResourceReference(String pageName) {
        Reference reference =null;
        // Create the resource reference
        if (pageName!=null&& !pageName.equals("")){
            reference = new Reference(this.uri + "/" + PAGESKEY + "/" + pageName);
        }
        else{
            reference = new Reference(this.uri + "/" + PAGESKEY);
        }
        return reference;
    }
    
    private Serializable getResource(Response response) throws IWootRestClientException{
        
        //SUCCESS_OK => 200 resource found and return it in a entity-body
        if (response.getStatus().equals(Status.SUCCESS_OK)){
            if (!response.isEntityAvailable()){
                throw new IWootRestClientException("Response status 200 (SUCCESS_OK) but no entity available");
            }
            try {
            // get The resource object in the response entity
                StreamRepresentation representation = new ObjectRepresentation<Serializable>(response.getEntity());
                Serializable entity = ((ObjectRepresentation<Serializable>) representation).getObject();
                return entity;
            } catch (IOException e) {
                throw new IWootRestClientException("Problem to get Object in flux",e);
            } catch (IllegalArgumentException e) {
                throw new IWootRestClientException("Problem to get Object in flux",e);
            } catch (ClassNotFoundException e) {
                throw new IWootRestClientException("Problem to get Object in flux",e);
            }
        }
        //CLIENT_ERROR_NOT_FOUND => 404 page not found unknown uri
        else if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)){
            return null;
        }
        else {
            throw new IWootRestClientException("Unexpected response status : "+response.getStatus());
        }
    }
  
    public boolean putPage(String pageName,Map page) throws IWootRestClientException{
        // Gathering new informations into a Web form.
        Form form = new Form();
        Set set=page.entrySet();
        Iterator i=set.iterator();
        while(i.hasNext()){
            Entry e=(Entry)i.next();
            form.add((String)e.getKey(),(String)e.getValue());
        }
        Representation rep = form.getWebRepresentation();
        Reference reference=this.getResourceReference(pageName);
        // Launch the request to create the resource
        Response response = this.client.put(reference, rep);
        if (response.getStatus().equals(Status.SUCCESS_CREATED) || response.getStatus().equals(Status.SUCCESS_OK)){
            return true;
        }
        else if (response.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY) || response.getStatus().equals(Status.SERVER_ERROR_INTERNAL)){
            return false;
        }
        else {
            throw new IWootRestClientException("Unexpected response status : "+response.getStatus());
        }
    }
    
    public Map getPage(String pageName) throws IWootRestClientException{
        Reference r=this.getResourceReference(pageName);
        Response response=this.client.get(r);
        
        return (Map) this.getResource(response);
    }
    
    public List getPageList() throws IWootRestClientException{
        Reference r=this.getResourceReference(null);
        Response response=this.client.get(r);
        
        return (List) this.getResource(response);
    }

    public String getUri() throws IWootClientException
    {
        return this.uri;
    }
}
