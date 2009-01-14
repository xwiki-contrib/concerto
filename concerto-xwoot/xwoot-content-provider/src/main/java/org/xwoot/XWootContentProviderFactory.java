package org.xwoot;

public abstract class XWootContentProviderFactory
{ 
    public static XWootContentProviderInterface getXWootContentProvider(String endpoint) throws XWootContentProviderException
    {
        try {
            if (endpoint==null || endpoint.equals("")){
                return new MockXWootContentProvider();
            }
            return new XWootContentProvider(endpoint, true);
        } catch (Exception e) {
            throw new XWootContentProviderException("Problem with XWoot content provider factory", e);
        }
    }
}
