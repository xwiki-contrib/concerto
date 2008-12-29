package org.xwoot;

import java.io.Serializable;

/**
 * This class represents an field of an XWoot object.
 * 
 * @version $Id$
 */
public class XWootObjectField implements Serializable
{
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 3230723710897699107L;

    /**
     * True is the field should be handled by XWoot.
     */
    private boolean wootable;

    /**
     * Field name.
     */
    private String name;

    /**
     * Field value. If wootable is true then this field has to be a String.
     */
    private Serializable value;

    private Class originalType;

    public XWootObjectField(String name, Serializable value, boolean wootable)
    {
        this(name, value, value.getClass(), wootable);
    }

    public XWootObjectField(String name, Serializable value, Class originalType, boolean wootable)
    {
        if (wootable) {
            if (!value.getClass().equals(String.class)) {
                throw new IllegalArgumentException("Wootable fields must have String values");
            }
        }

        this.name = name;
        this.value = value;
        this.originalType = originalType;
        this.wootable = wootable;
    }

    public Serializable getValue()
    {
        return value;
    }

    public void setValue(Serializable value)
    {
        if (!value.getClass().equals(this.value.getClass())) {
            throw new IllegalArgumentException(String.format("Invalid type for %s. Expected %s, got %s", name,
                this.value.getClass(), value.getClass()));
        }
        this.value = value;
    }

    public boolean isWootable()
    {
        return wootable;
    }

    public String getName()
    {
        return name;
    }

    public Class getOriginalType()
    {
        return originalType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XWootObjectField))
            return false;
        XWootObjectField other = (XWootObjectField) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
