package org.xwoot;

public abstract class XWootContentProviderFactory
{ 
    public static XWootContentProviderInterface getXWootContentProvider(String endpoint, boolean reInitialize) throws XWootContentProviderException
    {
        try {
            if (endpoint==null || endpoint.equals("")){
                return new MockXWootContentProvider();
            }
            return new XWootContentProvider(endpoint, reInitialize);
        } catch (Exception e) {
            throw new XWootContentProviderException("Problem with XWoot content provider factory", e);
        }
    }
}
