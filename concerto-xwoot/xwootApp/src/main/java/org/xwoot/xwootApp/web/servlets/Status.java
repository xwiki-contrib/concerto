package org.xwoot.xwootApp.web.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.protocol.PipeAdvertisement;

import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.web.XWootSite;

public class Status extends HttpServlet
{
    private static final long serialVersionUID = 209685845279022541L;
    
    private static final String TYPE_QUERY_PARAMETER = "type";
    
    private static final String CONNECTIONS = "connections";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
       Properties properties = new Properties();
       
       if(CONNECTIONS.equalsIgnoreCase(req.getParameter(TYPE_QUERY_PARAMETER))) {
           properties = getStatusPropertiesForConnections();
       }
              
       resp.setContentType("application/xml");
       
       properties.storeToXML(resp.getOutputStream(), "Status");       
    }

    private Properties getStatusPropertiesForConnections()
    {
        XWootSite xwootSite = XWootSite.getInstance();
        XWootAPI xwootAPI = xwootSite.getXWootEngine();
                
        Properties properties = new Properties();        
        
        properties.setProperty("xwootStarted", Boolean.toString(xwootSite.isStarted()));
        properties.setProperty("xwootConnectedToP2PNetwork", xwootAPI != null ? Boolean.toString(xwootAPI.isConnectedToP2PNetwork()) : Boolean.toString(false));
        properties.setProperty("xwootConnectedToP2PGroup", xwootAPI != null ? Boolean.toString(xwootAPI.isConnectedToP2PGroup()) : Boolean.toString(false));
        properties.setProperty("xwootConnectedToXWiki", xwootAPI != null ? Boolean.toString(xwootAPI.isContentManagerConnected()) : Boolean.toString(false));
        properties.setProperty("xwootIgnorePatterns", xwootAPI != null ? xwootAPI.getContentProvider().getConfiguration().getIgnorePatterns().toString() : "No content provider available.");
        properties.setProperty("xwootAcceptPatterns", xwootAPI != null ? xwootAPI.getContentProvider().getConfiguration().getAcceptPatterns().toString() : "No content provider available.");
        
        List<String> neighborNames = new ArrayList<String>();
        if(xwootAPI != null) {
            Collection<PipeAdvertisement> neighbors;
            try {
                neighbors = xwootAPI.getNeighborsList();
                for(PipeAdvertisement advertisement : neighbors) {
                    neighborNames.add(advertisement.getName());
                }
            } catch (XWootException e) {                
                e.printStackTrace();
            }            
        }    
        
        properties.setProperty("xwootNeighbours", neighborNames.toString());
        
        return properties;
    }

    
    
}
