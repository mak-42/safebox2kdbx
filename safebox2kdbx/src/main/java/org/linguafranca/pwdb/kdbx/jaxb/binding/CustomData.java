package org.linguafranca.pwdb.kdbx.jaxb.binding;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * Third party programs and plugins can put custom data here. Unique element names should be used, e.g.
 * "PluginName_ItemName".
 * <p>
 * Java class for customData complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="customData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;item maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "customData", propOrder = { "item" })
public class CustomData {

    /**
     * List of custom items.
     */
    @XmlElement(name = "Item", required = true)
    protected List<CustomData.Item> item;

    /**
     * Gets the value of the item property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore item modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the item property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getAny().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link CustomData.Item }
     * 
     * @return list of custom items
     */
    public List<CustomData.Item> getItem() {
        if (item == null) {
            item = new ArrayList<CustomData.Item>();
        }
        return this.item;
    }

    /**
     * Custom data item (key/value pair) for plugins/ports. The key should be unique, e.g. "PluginName_ItemName".
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Item", propOrder = { "key", "value" }) // , "lastModificationTime" })
    @Data
    public static class Item {

        /**
         * Key.
         */
        @XmlElement(name = "Key", required = true)
        private String key;

        /**
         * Value.
         */
        @XmlElement(name = "Value", required = true)
        private String value;

        /**
         * The moment the record was last modified.
         */
        // @XmlElement(name = "LastModificationTime", required = false, type = String.class)
        // @XmlJavaTypeAdapter(Adapter1.class)
        // @XmlSchemaType(name = "dateTime")
        // @Nullable
        // private Date lastModificationTime;
    }
}
