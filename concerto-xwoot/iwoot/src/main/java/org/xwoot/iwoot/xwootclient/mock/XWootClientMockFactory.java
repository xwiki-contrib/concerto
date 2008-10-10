package org.xwoot.iwoot.xwootclient.mock;

import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.xwootApp.XWootAPI;

public class XWootClientMockFactory extends XWootClientFactory
{   
    @Override
    public XWootAPI createXWootClient() throws XWootClientMockException
    {
        return new XWootClientMock();
    }
}
