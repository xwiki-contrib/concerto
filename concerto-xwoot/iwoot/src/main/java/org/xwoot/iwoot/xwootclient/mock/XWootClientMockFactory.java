package org.xwoot.iwoot.xwootclient.mock;

import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;

public class XWootClientMockFactory extends XWootClientFactory
{   
    @Override
    public XWootClientAPI createXWootClient() throws XWootClientMockException
    {
        return new XWootClientMock();
    }

    @Override
    public XWootClientAPI createXWootClient(String url) throws XWootClientException
    {
        return new XWootClientMock();
    }
}
