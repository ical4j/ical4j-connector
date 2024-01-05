package org.ical4j.connector.dav.request;

import net.fortuna.ical4j.model.parameter.CuType;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.ical4j.connector.dav.property.BaseDavPropertyName;
import org.ical4j.connector.dav.property.CalDavPropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

public class PrincipalPropertySearch implements XmlSupport, XmlSerializable {

    private final CuType type;

    private final String nameToSearch;

    public PrincipalPropertySearch(CuType type) {
        this(type, null);
    }

    public PrincipalPropertySearch(CuType type, String nameToSearch) {
        this.type = type;
        this.nameToSearch = nameToSearch;
    }

    protected Element propertiesForPropSearch(Document document) {
        Element firstNameProperty = newCsElement(document, "first-name");
        Element recordTypeProperty = newCsElement(document, "record-type");
        Element calUserAddressSetProperty = newElement(document, CalDavPropertyName.USER_ADDRESS_SET);
        Element lastNameProperty = newCsElement(document, "last-name");
        Element principalUrlProperty = newElement(document, SecurityConstants.PRINCIPAL_URL);
        Element calUserTypeProperty = newElement(document, CalDavPropertyName.USER_TYPE);
        Element displayNameForProperty = newElement(document, DavPropertyName.DISPLAYNAME);
        Element emailAddressSetProperty = newCsElement(document, "email-address-set");

        return newElement(document, BaseDavPropertyName.PROP, firstNameProperty, recordTypeProperty,
                calUserAddressSetProperty, lastNameProperty, principalUrlProperty, calUserTypeProperty,
                displayNameForProperty, emailAddressSetProperty);
    }

    public Element build() throws ParserConfigurationException {
        Document document = newXmlDocument();
        return toXml(document);
    }

    @Override
    public Element toXml(Document document) {
        Element displayName;
        if (nameToSearch != null) {
            displayName = newElement(document, DavPropertyName.DISPLAYNAME);
        } else {
            displayName = newCalDavElement(document, "calendar-user-type");
        }
        Element displayNameProperty = newElement(document, BaseDavPropertyName.PROP, displayName);

        Element containsMatch = newDavElement(document, "match");
        if (nameToSearch != null) {
            containsMatch.setAttribute("match-type", "contains");
            containsMatch.setTextContent(nameToSearch);
        } else {
            containsMatch.setAttribute("match-type", "equals");
            containsMatch.setTextContent(type.getValue());
        }

        Element propertySearchDisplayName = newDavElement(document, "property-search",
                displayNameProperty, containsMatch);

        Element properties = propertiesForPropSearch(document);

        Element principalPropSearch = newDavElement(document, "principal-property-search");
        principalPropSearch.setAttribute("type", type.getValue());
        principalPropSearch.setAttribute("test", "anyof");
        principalPropSearch.appendChild(propertySearchDisplayName);
        if (nameToSearch != null) {

            Element emailAddressSet = newCsElement(document, "email-address-set");

            Element emailSetProperty = newElement(document, BaseDavPropertyName.PROP, emailAddressSet);

            Element startsWith = newDavElement(document, "match");
            startsWith.setAttribute("match-type", "starts-with");
            if (startsWith != null) {
                startsWith.setTextContent(nameToSearch);
            }

            Element propertySearchEmail = newDavElement(document, "property-search");
            propertySearchEmail.setTextContent(nameToSearch);
            propertySearchEmail.appendChild(emailSetProperty);
            propertySearchEmail.appendChild(startsWith);

            principalPropSearch.appendChild(propertySearchEmail);
        }
        principalPropSearch.appendChild(properties);
        return principalPropSearch;
    }
}
