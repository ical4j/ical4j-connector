package net.fortuna.ical4j.connector.dav.request;

import net.fortuna.ical4j.connector.dav.CalDavConstants;
import net.fortuna.ical4j.model.parameter.CuType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

public class PrincipalPropertySearch implements XmlSupport {

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
        Element calUserAddressSetProperty = newCalDavElement(document, CalDavConstants.PROPERTY_USER_ADDRESS_SET);
        Element lastNameProperty = newCsElement(document, "last-name");
        Element principalUrlProperty = newDavElement(document, "principal-URL");
        Element calUserTypeProperty = newCalDavElement(document, CalDavConstants.PROPERTY_USER_TYPE);
        Element displayNameForProperty = newDavElement(document, "displayname");
        Element emailAddressSetProperty = newCsElement(document, "email-address-set");

        return newDavElement(document, "prop", firstNameProperty, recordTypeProperty,
                calUserAddressSetProperty, lastNameProperty, principalUrlProperty, calUserTypeProperty,
                displayNameForProperty, emailAddressSetProperty);
    }

    public Element build() throws ParserConfigurationException {
        Document document = newXmlDocument();

        Element displayName;
        if (nameToSearch != null) {
            displayName = newDavElement(document, "displayname");
        } else {
            displayName = newCalDavElement(document, "calendar-user-type");
        }
        Element displayNameProperty = newDavElement(document, "prop", displayName);

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

            Element emailSetProperty = newDavElement(document, "prop", emailAddressSet);

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
