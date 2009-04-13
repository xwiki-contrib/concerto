//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.04.13 at 07:44:33 PM CEST 
//


package org.xwiki.rest.model.jaxb;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Space complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Space">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.xwiki.org}LinkCollection">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wiki" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="home" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="xwikiRelativeUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="xwikiAbsoluteUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Space", propOrder = {
    "id",
    "wiki",
    "name",
    "home",
    "xwikiRelativeUrl",
    "xwikiAbsoluteUrl"
})
@XmlRootElement(name = "space")
public class Space
    extends LinkCollection
{

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String wiki;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String home;
    @XmlElement(required = true)
    protected String xwikiRelativeUrl;
    @XmlElement(required = true)
    protected String xwikiAbsoluteUrl;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the wiki property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWiki() {
        return wiki;
    }

    /**
     * Sets the value of the wiki property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWiki(String value) {
        this.wiki = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the home property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHome() {
        return home;
    }

    /**
     * Sets the value of the home property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHome(String value) {
        this.home = value;
    }

    /**
     * Gets the value of the xwikiRelativeUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXwikiRelativeUrl() {
        return xwikiRelativeUrl;
    }

    /**
     * Sets the value of the xwikiRelativeUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXwikiRelativeUrl(String value) {
        this.xwikiRelativeUrl = value;
    }

    /**
     * Gets the value of the xwikiAbsoluteUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXwikiAbsoluteUrl() {
        return xwikiAbsoluteUrl;
    }

    /**
     * Sets the value of the xwikiAbsoluteUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXwikiAbsoluteUrl(String value) {
        this.xwikiAbsoluteUrl = value;
    }

    public Space withId(String value) {
        setId(value);
        return this;
    }

    public Space withWiki(String value) {
        setWiki(value);
        return this;
    }

    public Space withName(String value) {
        setName(value);
        return this;
    }

    public Space withHome(String value) {
        setHome(value);
        return this;
    }

    public Space withXwikiRelativeUrl(String value) {
        setXwikiRelativeUrl(value);
        return this;
    }

    public Space withXwikiAbsoluteUrl(String value) {
        setXwikiAbsoluteUrl(value);
        return this;
    }

    @Override
    public Space withLinks(Link... values) {
        if (values!= null) {
            for (Link value: values) {
                getLinks().add(value);
            }
        }
        return this;
    }

    @Override
    public Space withLinks(Collection<Link> values) {
        if (values!= null) {
            getLinks().addAll(values);
        }
        return this;
    }

}
