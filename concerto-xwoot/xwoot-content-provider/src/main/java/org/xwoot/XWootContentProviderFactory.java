package org.xwoot;

import java.util.Properties;

public abstract class XWootContentProviderFactory
{
    public static XWootContentProviderInterface getXWootContentProvider(String endpoint, String dbName,
        boolean reInitialize, Properties configurationProperties) throws XWootContentProviderException
    {
        try {
            if (endpoint == null || endpoint.equals("")) {
                return new MockXWootContentProvider();
            }
            return new NewXWootContentProvider(endpoint, dbName, reInitialize, configurationProperties);
        } catch (Exception e) {
            throw new XWootContentProviderException("Problem with XWoot content provider factory", e);
        }
    }
}
