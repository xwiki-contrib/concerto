package org.xwoot.wootEngine.core;

import java.io.Serializable;

/**
 * Provides an id for a woot content. A content is an object's field value ; the object is linked with a page id.
 * 
 * @version $Id$
 */
public class ContentId implements Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -4765972758004615324L;

    /** Id of the page which contains the object. */
    private String pageName;

    /** Id of the object which contains the field. */
    private String objectName;

    /** Id of the field containing the content. */
    private String fieldName;

    /** Is the content a copy of an another content. */
    private boolean isCopy;

    /**
     * Creates a new ContentId object.
     * 
     * @param pageName Id of the page which contains the object.
     * @param objectName Id of the object which contains the field.
     * @param fieldName Id of the field containing the content.
     * @param isCopy Is the content a copy of an another content.
     */
    public ContentId(String pageName, String objectName, String fieldName, boolean isCopy)
    {
        super();
        this.pageName = pageName;
        this.objectName = objectName;
        this.fieldName = fieldName;
        this.isCopy = isCopy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fieldName == null) ? 0 : this.fieldName.hashCode());
        result = prime * result + (this.isCopy ? 1231 : 1237);
        result = prime * result + ((this.objectName == null) ? 0 : this.objectName.hashCode());
        result = prime * result + ((this.pageName == null) ? 0 : this.pageName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ContentId)) {
            return false;
        }
        ContentId other = (ContentId) obj;

        if (this.isCopy != other.isCopy) {
            return false;
        }
        if (!this.equalsString(this.fieldName, other.fieldName)) {
            return false;
        }
        if (!this.equalsString(this.objectName, other.objectName)) {
            return false;
        }
        if (!this.equalsString(this.pageName, other.pageName)) {
            return false;
        }

        return true;
    }

    /**
     * Checks the equality between two strings.
     * 
     * @param s1 the first String to test.
     * @param s2 the second String to test.
     * @return the equality between two strings.
     */
    private boolean equalsString(String s1, String s2)
    {
        return s1 != null && s1.equals(s2);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String copy = "";
        if (this.isCopy) {
            copy = "copyOf.";
        }
        return copy + this.pageName + "." + this.objectName + "_" + this.fieldName;
    }

    /**
     * @return the page id of the ContentId.
     */
    public String getPageName()
    {
        return this.pageName;
    }

    /**
     * @return the object id of the ContentId.
     */
    public String getObjectName()
    {
        return this.objectName;
    }

    /**
     * @return the field id of the ContentId.
     */
    public String getFieldName()
    {
        return this.fieldName;
    }

    /**
     * @return : Is the content a copy of an another content.
     */
    public boolean isCopy()
    {
        return this.isCopy;
    }
}
