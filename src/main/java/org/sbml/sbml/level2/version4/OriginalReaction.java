//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package org.sbml.sbml.level2.version4;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Reaction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Reaction">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.sbml.org/sbml/level2/version4}SBase">
 *       &lt;sequence>
 *         &lt;element name="listOfReactants" type="{http://www.sbml.org/sbml/level2/version4}ListOfSpeciesReferences" minOccurs="0"/>
 *         &lt;element name="listOfProducts" type="{http://www.sbml.org/sbml/level2/version4}ListOfSpeciesReferences" minOccurs="0"/>
 *         &lt;element name="listOfModifiers" type="{http://www.sbml.org/sbml/level2/version4}ListOfModifierSpeciesReferences" minOccurs="0"/>
 *         &lt;element name="kineticLaw" type="{http://www.sbml.org/sbml/level2/version4}KineticLaw" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.sbml.org/sbml/level2/version4}SId" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="reversible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="fast" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "listOfReactants",
    "listOfProducts",
    "listOfModifiers",
    "kineticLaw"
})
@XmlSeeAlso({
    Reaction.class
})
public class OriginalReaction
    extends SBase
{

    protected ListOfSpeciesReferences listOfReactants;
    protected ListOfSpeciesReferences listOfProducts;
    protected ListOfModifierSpeciesReferences listOfModifiers;
    protected KineticLaw kineticLaw;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "reversible")
    protected Boolean reversible;
    @XmlAttribute(name = "fast")
    protected Boolean fast;

    /**
     * Gets the value of the listOfReactants property.
     * 
     * @return
     *     possible object is
     *     {@link ListOfSpeciesReferences }
     *     
     */
    public ListOfSpeciesReferences getListOfReactants() {
        return listOfReactants;
    }

    /**
     * Sets the value of the listOfReactants property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOfSpeciesReferences }
     *     
     */
    public void setListOfReactants(ListOfSpeciesReferences value) {
        this.listOfReactants = value;
    }

    /**
     * Gets the value of the listOfProducts property.
     * 
     * @return
     *     possible object is
     *     {@link ListOfSpeciesReferences }
     *     
     */
    public ListOfSpeciesReferences getListOfProducts() {
        return listOfProducts;
    }

    /**
     * Sets the value of the listOfProducts property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOfSpeciesReferences }
     *     
     */
    public void setListOfProducts(ListOfSpeciesReferences value) {
        this.listOfProducts = value;
    }

    /**
     * Gets the value of the listOfModifiers property.
     * 
     * @return
     *     possible object is
     *     {@link ListOfModifierSpeciesReferences }
     *     
     */
    public ListOfModifierSpeciesReferences getListOfModifiers() {
        return listOfModifiers;
    }

    /**
     * Sets the value of the listOfModifiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListOfModifierSpeciesReferences }
     *     
     */
    public void setListOfModifiers(ListOfModifierSpeciesReferences value) {
        this.listOfModifiers = value;
    }

    /**
     * Gets the value of the kineticLaw property.
     * 
     * @return
     *     possible object is
     *     {@link KineticLaw }
     *     
     */
    public KineticLaw getKineticLaw() {
        return kineticLaw;
    }

    /**
     * Sets the value of the kineticLaw property.
     * 
     * @param value
     *     allowed object is
     *     {@link KineticLaw }
     *     
     */
    public void setKineticLaw(KineticLaw value) {
        this.kineticLaw = value;
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
     * Gets the value of the reversible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isReversible() {
        if (reversible == null) {
            return true;
        } else {
            return reversible;
        }
    }

    /**
     * Sets the value of the reversible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReversible(Boolean value) {
        this.reversible = value;
    }

    /**
     * Gets the value of the fast property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFast() {
        return fast;
    }

    /**
     * Sets the value of the fast property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFast(Boolean value) {
        this.fast = value;
    }

}
