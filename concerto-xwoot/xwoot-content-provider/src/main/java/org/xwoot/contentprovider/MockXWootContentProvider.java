package org.xwoot.contentprovider;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

/**
 * XWootContentProvider. This class is the implementation of the XWiki interface for handling modifications and
 * performing changes. For more details see {@link http://concerto.xwiki.com/xwiki/bin/view/Main/APIChat281108}
 * 
 * @version $Id$
 */
public class MockXWootContentProvider implements XWootContentProviderInterface
{
    private Map<XWootId, List<XWootObject>> list;

    /**
     * Constructor.
     * 
     * @throws XWootContentProviderException
     */
    public MockXWootContentProvider() throws XWootContentProviderException
    {
        this.list = new Hashtable<XWootId, List<XWootObject>>();
    }

    /**
     * Login to the remote XWiki.
     * 
     * @param username
     * @param password
     * @throws XWootContentProviderException
     */
    public void login(String username, String password) throws XWootContentProviderException
    {
        return;
    }

    /**
     * Logout from the remote XWiki.
     * 
     * @throws XmlRpcException
     */
    public void logout()
    {
        return;
    }

    /**
     * Returns a list of references where each reference points to a different page at its oldest modification available
     * in the modification list that has not been cleared.
     * 
     * @return A list of XWootIds.
     * @throws XWootContentProviderException
     */
    public Set<XWootId> getModifiedPagesIds() throws XWootContentProviderException
    {
        return this.list.keySet();
    }

    /**
     * Set the "cleared" flag of the modification related to the id passed as parameter. This means that the
     * modification has been processed and should not be returned in subsequent calls.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    public void clearModification(XWootId xwootId) throws XWootContentProviderException
    {
        this.list.remove(xwootId);
        return;
    }

    /**
     * Set the "cleared" flag of all the modifications up to the one (included) related to the id passed as parameter.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    public void clearAllModifications(XWootId xwootId) throws XWootContentProviderException
    {
        for (XWootId id : this.list.keySet()) {
            if (id.getTimestamp() >= xwootId.getTimestamp()) {
                this.list.remove(id);
            }
        }
        return;
    }

    @SuppressWarnings("unused")
    public List<XWootObject> getModifiedEntities(XWootId xwootId) throws XWootContentProviderException
    {
        return this.list.get(xwootId);
    }

    public void addEntryInList(XWootId id, XWootObject object)
    {
        List current = this.list.get(id);
        if (current == null) {
            current = new ArrayList<XWootObject>();
        }
        current.add(object);
        this.list.put(id, current);
    }

    public XWootId store(XWootObject o, XWootId versionAdjustement)
    {
        for (Iterator i = this.list.keySet().iterator(); i.hasNext();) {
            XWootId id = (XWootId) i.next();
            if (id.getPageId().equals(o.getPageId())) {
                return null;
            }
        }
        return new XWootId(o.getPageId(), 0, o.getPageVersion() + 1, o.getPageMinorVersion());
    }

    public void clearAllModifications() throws XWootContentProviderException
    {
        return;
        // TODO

    }

    public XWootContentProviderConfiguration getConfiguration()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Entry> getEntries(String pageId, int start, int number)
    {
        return new ArrayList<Entry>();
    }

    public boolean isConnected()
    {        
        return true;
    }
}
