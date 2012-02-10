//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.10 at 02:45:28 PM PST 
//


package com.relteq.sirius.jaxb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}VehicleTypeOrder" minOccurs="0"/>
 *         &lt;element ref="{}decision_point_split" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="network_id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="node_id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="start_time" type="{http://www.w3.org/2001/XMLSchema}decimal" default="0" />
 *       &lt;attribute name="dt" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vehicleTypeOrder",
    "decisionPointSplit"
})
@XmlRootElement(name = "decision_point")
public class DecisionPoint {

    @XmlElement(name = "VehicleTypeOrder")
    protected VehicleTypeOrder vehicleTypeOrder;
    @XmlElement(name = "decision_point_split")
    protected List<DecisionPointSplit> decisionPointSplit;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "network_id")
    protected String networkId;
    @XmlAttribute(name = "node_id", required = true)
    protected String nodeId;
    @XmlAttribute(name = "start_time")
    protected BigDecimal startTime;
    @XmlAttribute(name = "dt")
    protected BigDecimal dt;

    /**
     * Gets the value of the vehicleTypeOrder property.
     * 
     * @return
     *     possible object is
     *     {@link VehicleTypeOrder }
     *     
     */
    public VehicleTypeOrder getVehicleTypeOrder() {
        return vehicleTypeOrder;
    }

    /**
     * Sets the value of the vehicleTypeOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link VehicleTypeOrder }
     *     
     */
    public void setVehicleTypeOrder(VehicleTypeOrder value) {
        this.vehicleTypeOrder = value;
    }

    /**
     * Gets the value of the decisionPointSplit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the decisionPointSplit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDecisionPointSplit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DecisionPointSplit }
     * 
     * 
     */
    public List<DecisionPointSplit> getDecisionPointSplit() {
        if (decisionPointSplit == null) {
            decisionPointSplit = new ArrayList<DecisionPointSplit>();
        }
        return this.decisionPointSplit;
    }

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
     * Gets the value of the networkId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * Sets the value of the networkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetworkId(String value) {
        this.networkId = value;
    }

    /**
     * Gets the value of the nodeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Sets the value of the nodeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodeId(String value) {
        this.nodeId = value;
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

}
