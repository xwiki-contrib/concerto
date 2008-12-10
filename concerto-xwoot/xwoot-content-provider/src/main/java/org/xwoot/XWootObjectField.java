package org.xwoot;

import java.io.Serializable;

/**
 * This class represents an field of an XWoot object.
 * 
 * @version $Id$
 */
public class XWootObjectField
{
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

    public XWootObjectField(String name, Serializable value, boolean wootable)
    {
        if (wootable) {
            if (!value.getClass().equals(String.class)) {
                throw new IllegalArgumentException("Wootable fields must have String values");
            }
        }

        this.name = name;
        this.value = value;
        this.wootable = wootable;
    }

    public Serializable getValue()
    {
        return value;
    }

    public void setValue(Serializable value)
    {
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

}
