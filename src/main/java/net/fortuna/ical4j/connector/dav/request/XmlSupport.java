package net.fortuna.ical4j.connector.dav.request;

import net.fortuna.ical4j.connector.dav.CalDavConstants;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public interface XmlSupport {

    default Document newXmlDocument(Node...children) throws ParserConfigurationException {
        Document document = DomUtil.createDocument();
        Arrays.stream(children).forEach(document::appendChild);

        return document;
    }

    default Document parseXml(InputStream content) throws ParserConfigurationException, IOException, SAXException {
        return DomUtil.parseDocument(content);
    }

    default Element newDavElement(Document document, String elementName, Node...children) {
        Element element = DomUtil.createElement(document, elementName, DavConstants.NAMESPACE);
        Arrays.stream(children).forEach(element::appendChild);
        return element;
    }

    default Element newDavProperty(Document document, String propertyName) {
        Element property = newDavElement(document, "property");
        property.setAttribute("name", propertyName);
        return property;
    }

    default Element newDavProperty(Document document, String propertyName, Namespace namespace, Node...children) {
        Element property = newDavProperty(document, propertyName);
        property.setAttribute("namespace", namespace.getURI());
        Arrays.stream(children).forEach(property::appendChild);
        return property;
    }

    default Element newCalDavElement(Document document, String elementName, Node...children) {
        Element element = DomUtil.createElement(document, elementName, CalDavConstants.CALDAV_NAMESPACE);
        Arrays.stream(children).forEach(element::appendChild);
        return element;
    }

    default Element newComponentFilter(Document document, String componentName, Node...children) {
        Element calFilter = newCalDavElement(document, CalDavConstants.PROPERTY_COMP_FILTER);
        calFilter.setAttribute(CalDavConstants.ATTRIBUTE_NAME, componentName);
        Arrays.stream(children).forEach(calFilter::appendChild);
        return calFilter;
    }

    default Element newTimeRange(Document document, String startTime, String endTime) {
        Element timeRange = newCalDavElement(document, CalDavConstants.PROPERTY_TIME_RANGE);
        timeRange.setAttribute(CalDavConstants.ATTRIBUTE_START, startTime);
        timeRange.setAttribute(CalDavConstants.ATTRIBUTE_END, endTime);
        return timeRange;
    }

    default Element newCsElement(Document document, String elementName) {
        return DomUtil.createElement(document, elementName, CalDavConstants.CS_NAMESPACE);
    }
}
