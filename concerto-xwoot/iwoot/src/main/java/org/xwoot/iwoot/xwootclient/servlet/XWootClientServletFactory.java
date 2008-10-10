package org.xwoot.iwoot.xwootclient.servlet;

import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.xwootApp.XWootAPI;

public class XWootClientServletFactory extends XWootClientFactory
{   
    @Override
    public XWootAPI createXWootClient() throws XWootClientServletException
    {
        return new XWootClientServlet();
    }
}
