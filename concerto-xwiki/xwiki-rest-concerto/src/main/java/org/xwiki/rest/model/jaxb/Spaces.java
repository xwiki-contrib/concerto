//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.04.13 at 07:44:33 PM CEST 
//


package org.xwiki.rest.model.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.xwiki.org}LinkCollection">
 *       &lt;sequence>
 *         &lt;element name="space" type="{http://www.xwiki.org}Space" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "spaces"
})
@XmlRootElement(name = "spaces")
public class Spaces
    extends LinkCollection
{

    @XmlElement(name = "space")
    protected List<Space> spaces;

    /**
     * Gets the value of the spaces property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spaces property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpaces().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Space }
     * 
     * 
     */
    public List<Space> getSpaces() {
        if (spaces == null) {
            spaces = new ArrayList<Space>();
        }
        return this.spaces;
    }

    public Spaces withSpaces(Space... values) {
        if (values!= null) {
            for (Space value: values) {
                getSpaces().add(value);
            }
        }
        return this;
    }

    public Spaces withSpaces(Collection<Space> values) {
        if (values!= null) {
            getSpaces().addAll(values);
        }
        return this;
    }

    @Override
    public Spaces withLinks(Link... values) {
        if (values!= null) {
            for (Link value: values) {
                getLinks().add(value);
            }
        }
        return this;
    }

    @Override
    public Spaces withLinks(Collection<Link> values) {
        if (values!= null) {
            getLinks().addAll(values);
        }
        return this;
    }

}
