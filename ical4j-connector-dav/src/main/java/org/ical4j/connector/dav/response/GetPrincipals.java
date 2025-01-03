package org.ical4j.connector.dav.response;

import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.property.Attendee;
import org.apache.http.HttpResponse;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.ical4j.connector.dav.property.CSDavPropertyName;
import org.ical4j.connector.dav.property.CalDavPropertyName;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class GetPrincipals extends AbstractResponseHandler<List<Attendee>> {

    @Override
    public List<Attendee> handleResponse(HttpResponse response) throws IOException {
        List<Attendee> resources = new ArrayList<>();

        try {
            var multiStatus = getMultiStatus(response);
            var responses = multiStatus.getResponses();
            for (var msr : responses) {

                var resource = new Attendee();
                var propertiesInResponse = msr.getProperties(DavServletResponse.SC_OK);

                DavProperty<?> displayNameFromResponse = propertiesInResponse.get("displayname",
                        DavConstants.NAMESPACE);
                if ((displayNameFromResponse != null) && (displayNameFromResponse.getValue() != null)) {
                    resource.add(new Cn((String) displayNameFromResponse.getValue()));
                }

                var calAddressUri = getCalAddress(propertiesInResponse);
                if (calAddressUri != null) {
                    resource.setCalAddress(calAddressUri);
                }

                DavProperty<?> calendarUserType = propertiesInResponse.get(CalDavPropertyName.USER_TYPE);
                if ((calendarUserType != null) && (calendarUserType.getValue() != null)) {
                    resource.add(new CuType((String) calendarUserType.getValue()));
                }

                resources.add(resource);
            }
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
        return resources;
    }

    private URI getCalAddress(DavPropertySet propertiesInResponse) {
        DavProperty<?> emailSet = propertiesInResponse.get("email-address-set",
                CSDavPropertyName.NAMESPACE);

        if (emailSet != null && emailSet.getValue() != null) {
            var emailSetValue = emailSet.getValue();
            if (emailSetValue instanceof List) {
                for (var email: (List<?>)emailSetValue) {
                    if (email instanceof org.w3c.dom.Node) {
                        var emailAddress = ((org.w3c.dom.Node)email).getTextContent();
                        if (emailAddress != null && emailAddress.trim().length() > 0) {
                            if (!emailAddress.startsWith("mailto:")) {
                                emailAddress = "mailto:".concat(emailAddress);
                            }
                            return URI.create(emailAddress);
                        }
                    }
                }
            }
        } else {
            DavProperty<?> calendarUserAddressSet = propertiesInResponse.get(CalDavPropertyName.PROPERTY_USER_ADDRESS_SET,
                    CalDavPropertyName.NAMESPACE);
            if (calendarUserAddressSet != null && calendarUserAddressSet.getValue() != null) {
                var value = calendarUserAddressSet.getValue();
                if (value instanceof List) {
                    for (var addressSet: (List<?>)value) {
                        if (addressSet instanceof org.w3c.dom.Node) {
                            var url = ((org.w3c.dom.Node)addressSet).getTextContent();
                            if (url.startsWith("urn:uuid")) {
                                return URI.create(url);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
