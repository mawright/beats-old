//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.10 at 02:45:28 PM PST 
//


package com.relteq.sirius.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="knob" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="start_time" type="{http://www.w3.org/2001/XMLSchema}decimal" default="0" />
 *       &lt;attribute name="dt" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="network_id_origin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="link_id_origin" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "demandProfile")
public class DemandProfile {

    @XmlValue
    protected String content;
    @XmlAttribute(name = "knob", required = true)
    protected BigDecimal knob;
    @XmlAttribute(name = "start_time")
    protected BigDecimal startTime;
    @XmlAttribute(name = "dt")
    protected BigDecimal dt;
    @XmlAttribute(name = "network_id_origin")
    protected String networkIdOrigin;
    @XmlAttribute(name = "link_id_origin", required = true)
    protected String linkIdOrigin;

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * Gets the value of the knob property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getKnob() {
        return knob;
    }

    /**
     * Sets the value of the knob property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setKnob(BigDecimal value) {
        this.knob = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getStartTime() {
        if (startTime == null) {
            return new BigDecimal("0");
        } else {
            return startTime;
        }
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setStartTime(BigDecimal value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the dt property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getDt() {
        return dt;
    }

    /**
     * Sets the value of the dt property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDt(BigDecimal value) {
        this.dt = value;
    }

    /**
     * Gets the value of the networkIdOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetworkIdOrigin() {
        return networkIdOrigin;
    }

    /**
     * Sets the value of the networkIdOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetworkIdOrigin(String value) {
        this.networkIdOrigin = value;
    }

    /**
     * Gets the value of the linkIdOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkIdOrigin() {
        return linkIdOrigin;
    }

    /**
     * Sets the value of the linkIdOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkIdOrigin(String value) {
        this.linkIdOrigin = value;
    }

}
