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
        var firstNameProperty = newCsElement(document, "first-name");
        var recordTypeProperty = newCsElement(document, "record-type");
        var calUserAddressSetProperty = newElement(document, CalDavPropertyName.USER_ADDRESS_SET);
        var lastNameProperty = newCsElement(document, "last-name");
        var principalUrlProperty = newElement(document, SecurityConstants.PRINCIPAL_URL);
        var calUserTypeProperty = newElement(document, CalDavPropertyName.USER_TYPE);
        var displayNameForProperty = newElement(document, DavPropertyName.DISPLAYNAME);
        var emailAddressSetProperty = newCsElement(document, "email-address-set");

        return newElement(document, BaseDavPropertyName.PROP, firstNameProperty, recordTypeProperty,
                calUserAddressSetProperty, lastNameProperty, principalUrlProperty, calUserTypeProperty,
                displayNameForProperty, emailAddressSetProperty);
    }

    public Element build() throws ParserConfigurationException {
        var document = newXmlDocument();
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
        var displayNameProperty = newElement(document, BaseDavPropertyName.PROP, displayName);

        var containsMatch = newDavElement(document, "match");
        if (nameToSearch != null) {
            containsMatch.setAttribute("match-type", "contains");
            containsMatch.setTextContent(nameToSearch);
        } else {
            containsMatch.setAttribute("match-type", "equals");
            containsMatch.setTextContent(type.getValue());
        }

        var propertySearchDisplayName = newDavElement(document, "property-search",
                displayNameProperty, containsMatch);

        var properties = propertiesForPropSearch(document);

        var principalPropSearch = newDavElement(document, "principal-property-search");
        principalPropSearch.setAttribute("type", type.getValue());
        principalPropSearch.setAttribute("test", "anyof");
        principalPropSearch.appendChild(propertySearchDisplayName);
        if (nameToSearch != null) {

            var emailAddressSet = newCsElement(document, "email-address-set");

            var emailSetProperty = newElement(document, BaseDavPropertyName.PROP, emailAddressSet);

            var startsWith = newDavElement(document, "match");
            startsWith.setAttribute("match-type", "starts-with");
            if (startsWith != null) {
                startsWith.setTextContent(nameToSearch);
            }

            var propertySearchEmail = newDavElement(document, "property-search");
            propertySearchEmail.setTextContent(nameToSearch);
            propertySearchEmail.appendChild(emailSetProperty);
            propertySearchEmail.appendChild(startsWith);

            principalPropSearch.appendChild(propertySearchEmail);
        }
        principalPropSearch.appendChild(properties);
        return principalPropSearch;
    }
}
