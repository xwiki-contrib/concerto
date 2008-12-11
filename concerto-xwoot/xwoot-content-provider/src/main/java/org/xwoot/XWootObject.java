package org.xwoot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an XWootObject. XWootObjects are the abstraction used for representing XWiki data structures
 * (i.e. XWikiPages and XWikiObejcts) in a uniform way and for communicating them to the XWoot engine. XWootObjects can
 * be cumulative or non-cumulative (i.e., at most one instance of the underlying entity can exist per page). Pages, and
 * several types of XWikiObjects are an example what is considered a non-cumulative XWootObjects. Comments, Calendar
 * Events, etc. are examples of what is considerer a cumulative XWootObject. For more details see: {@link http
 * ://concerto.xwiki.com/xwiki/bin/view/Main/APIChat281108}
 * 
 * @version $Id$
 */
public class XWootObject
{
    /**
     * The pageId of the page this XWootObject is built from.
     */
    private String pageId;

    /**
     * The GUID for uniquely identifying this object. See: {@link http
     * ://concerto.xwiki.com/xwiki/bin/view/Main/APIChat101208}
     */
    private String guid;

    /**
     * True if the object is cumulative.
     */
    private boolean cumulative;

    /**
     * True if this object didn't exist in the previous version of the page from where it comes from.
     */
    private boolean newlyCreated;

    /**
     * The list of the fields that make up the object.
     */
    private List<XWootObjectField> fields;

    public XWootObject(String pageId, String guid, boolean cumulative, List<XWootObjectField> fields,
        boolean newlyCreated)
    {
        this.pageId = pageId;
        this.guid = guid;
        this.cumulative = cumulative;
        this.fields = fields;
        this.newlyCreated = newlyCreated;
    }

    public String getPageId()
    {
        return pageId;
    }

    public List<String> getFieldNames()
    {
        List<String> result = new ArrayList<String>();
        for (XWootObjectField field : fields) {
            result.add(field.getName());
        }

        return result;
    }

    public List<XWootObjectField> getFields()
    {
        return fields;
    }

    public Serializable getFieldValue(String name)
    {
        XWootObjectField field = lookupField(name);
        return field != null ? field.getValue() : null;
    }

    public void setFieldValue(String name, Serializable value)
    {
        XWootObjectField field = lookupField(name);
        if (field != null) {
            field.setValue(value);
        }
    }

    public boolean isFieldWootable(String name)
    {
        XWootObjectField field = lookupField(name);
        return field != null ? field.isWootable() : false;
    }

    public String getGuid()
    {
        return guid;
    }

    public boolean isCumulative()
    {
        return cumulative;
    }

    public boolean isNewlyCreated()
    {
        return newlyCreated;
    }

    public boolean hasWootableFields()
    {
        for (XWootObjectField field : fields) {
            if (field.isWootable()) {
                return true;
            }
        }

        return false;
    }

    private XWootObjectField lookupField(String name)
    {
        for (XWootObjectField field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

}
