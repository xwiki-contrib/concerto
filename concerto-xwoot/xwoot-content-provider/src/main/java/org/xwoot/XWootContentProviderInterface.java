package org.xwoot;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

public interface XWootContentProviderInterface
{

    /**
     * Login to the remote XWiki.
     * 
     * @param username
     * @param password
     * @throws XWootContentProviderException
     * @throws XmlRpcException
     * @throws MalformedURLException
     */
    void login(String username, String password) throws XWootContentProviderException;

    /**
     * Logout from the remote XWiki.
     * 
     * @throws XmlRpcException
     */
    void logout();

    /**
     * Returns a list of references where each reference points to a different page at its oldest modification available
     * in the modification list that has not been cleared.
     * 
     * @return A set of XWootIds.
     * @throws XWootContentProviderException
     */
    Set<XWootId> getModifiedPagesIds() throws XWootContentProviderException;

    /**
     * Set the "cleared" flag of the modification related to the id passed as parameter. This means that the
     * modification has been processed and should not be returned in subsequent calls.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    void clearModification(XWootId xwootId) throws XWootContentProviderException;

    /**
     * Set the "cleared" flag of all the modifications up to the one (included) related to the id passed as parameter.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    void clearAllModifications(XWootId xwootId) throws XWootContentProviderException;

    /**
     * Clear all the modifications. Useful for testing purpose.
     * 
     * @throws XWootContentProviderException
     */
    void clearAllModifications() throws XWootContentProviderException;

    /**
     * Returns a list of XWootObjects that contains all the entities that have been modified in the page identified by
     * the the XWootId (i.e., at a given timestamp). The contract here is that each XWootObject in the list will contain
     * only the fields that have been modified (i.e., a subset of the fields that actually make up the underlying
     * object). If the entity didn't exist in the previous version, then all the fields are present in the corresponding
     * XWootObject. Here we return only XWootObjects since we decided to process in a uniform way XWikiPages and
     * XWikiObjects, since they both can be seen as a collection of pairs name=value.
     * 
     * @param xwootId
     * @return
     * @throws XWootContentProviderException
     */
    List<XWootObject> getModifiedEntities(XWootId xwootId) throws XWootContentProviderException;

    /**
     * Updates xwiki's data.
     * 
     * @param object : the object to update
     * @return true if no concurrent modification detected.
     * @throws XWootContentProviderException
     */
    boolean store(XWootObject object) throws XWootContentProviderException;

}
