package org.xwoot.mockiphone.iwootclient;

import java.util.List;
import java.util.Map;

public interface IWootClient
{
    public boolean putPage(String pageName,Map page) throws IWootClientException;
    
    public Map getPage(String pageName) throws IWootClientException;
    
    public List getPageList() throws IWootClientException;
    
    public String getUri() throws IWootClientException;
}
