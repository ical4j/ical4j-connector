[Jackrabbit WebDAV]: https://jackrabbit.apache.org/jcr/components/jackrabbit-webdav-library.html
[DAVResource]: https://jackrabbit.apache.org/api/trunk/org/apache/jackrabbit/webdav/DavResource.html

# iCal4j Connector for CalDAV and CardDAV

iCalendar and vCard data management with CalDAV and CardDAV.

## Overview

This library provides client support for backend calendar services using the iCal4j object model. The
most popular services implement DAV extensions for Calendaring (CalDAV) and VCard (CardDAV).

## CalDAV and CardDAV

The iCal4j Connector now supports three approaches for integrating with CalDAV and CardDAV servers. First
there is a low-level DAV client that supports HTTP methods for communicating with WebDAV servers. This is
defined by the CalDAVSupport and CardDAVSupport interfaces. The 
[DAVResource] implementation that builds on the [Jackrabbit WebDAV] library supports response caching and path 
abstraction for different
server implementations. Finally the CalendarStore, CardStore, CalendarCollection and CardCollection interfaces provide 
a higher-level abstraction for
Calendar and VCard resource management including collection discovery.

### DAV Client

The DAV client may be used to perform explicit CalDAV and CardDAV operations against a specified URL. A
DAV client instance is initialised with a server URL and configuration options.

```java
DavClientConfiguration configuration = new DavClientConfiguration().withPreemptiveAuth(true)
        .withFollowRedirects(true);
DavClientFactory clientFactory = new DavClientFactory(configuration);

DavClient client = clientFactory.newInstance("https://dav.example.com");
```    

Note that whilst the `DavClientFactory` currently returns a concrete type, it is recommended to use
interfaces for local references.

```java
CalDavSupport caldav = clientFactory.newInstance("https://dav.example.com/.well-known/caldav");

CardDavSupport carddav = clientFactory.newInstance("https://dav.example.com/.well-known/carddav");
```

#### Authentication

TBD.

#### Methods

New calendar collections are created via the `MKCALENDAR` method.

```java
DavPropertySet props = new DavPropertySet();
props.add(new DavPropertyBuilder<>().name(DavPropertyName.DISPLAYNAME).value('Test Collection').build());
props.add(new DavPropertyBuilder<>().name(CalDavPropertyName.CALENDAR_DESCRIPTION)
        .value('A simple mkcalendar test').build());
caldav.mkCalendar('admin/test', props);
```

Existing calendar resources are retrieved via the `GET` method.

```java
Calendar calendar = caldav.getCalendar('admin/test');
```

Resource and collection metadata is accessed via the `PROPFIND` method.

```java
DavPropertySet props = caldav.propFind('admin', SecurityConstants.PRINCIPAL_COLLECTION_SET);
```

Note that to support multiple CalDAV implementations you may need to use a PathResolver.

```java
String path = PathResolver.Defaults.RADICALE.getCalendarPath("test", "admin");
Calendar calendar = caldav.getCalendar(path);
```

### DavResource

A `DavResource` is a higher-level abstraction of a resource path that supports hierarchical discovery of
resources and caching of properties.

```java
DavClientConfiguration configuration = new DavClientConfiguration().withPreemptiveAuth(true)
        .withFollowRedirects(true);
DavClientFactory clientFactory = new DavClientFactory(configuration);

DavLocatorFactory locatorFactory = new CalDavLocatorFactory(PathResolver.Defaults.RADICALE);
DavResourceLocator locator = locatorFactory.createResourceLocator("https://dav.example.com",
        "user", "testcal");

DavResourceFactory resourceFactory = new CalDavResourceFactory(clientFactory);
DavResource resource = resourceFactory.createResource(locator, null);
```

A `DavResource` may be queried for common metadata properties.

```java
if (resource.isCollection()) {
        System.out.println("Collection name:" + resource.getDisplayName());
        System.out.println("Collection exists:" + resource.exists());
        System.out.println("Collection description:"
            + resource.getProperty(CalDavPropertyName.CALENDAR_DESCRIPTION).getValue());
}
```

A `DavResource` may be altered by adding and removing properties.

```java
// overwrite resource description property
resource.setProperty(new DavPropertyBuilder<>().name(CalDavPropertyName.CALENDAR_DESCRIPTION)
        .value('A simple mkcalendar test').build());

// unset calendar timezone
resource.removeProperty(CalDavPropertyName.CALENDAR_TIMEZONE);

// add and remove properties with a single request
resource.alterProperties(Arrays.asList(
        new DavPropertyBuilder<>().name(CalDavPropertyName.CALENDAR_DESCRIPTION)
            .value('A simple mkcalendar test').build(),
        CalDavPropertyName.CALENDAR_TIMEZONE
));
```

Child and sibling resources are also easily accessed.

```java
// add a child resource
resource.addMember(...);

// add a sibling resource
resource.getCollection().addMember(...);

// remove all children
for (DavResource child : resouce.getMembers()) {
        resource.removeMember(child);
}
```

### CalDavCalendarStore and CardDavStore

The third method, with the highest level of abstraction are the CalDAV and CardDAV implementations of
`CalendarStore` and `CardStore`. This approach provides a logical separation of collections and other
resources, as well as supporting more complex querying.

```java
CalendarStore store = new CalDavCalendarStore(...);
store.connect('admin', 'admin'.toCharArray());

for (CalendarCollection collection : store.getCollections()) {
        System.out.println("Collection name: " + collection.getDisplayName());
}
```

Collections are accessed via a store instance.

```java
CalendarCollection collection = store.getCollection("testcol");
System.out.println("Collection description: " + collection.getDescription());
System.out.println("Collection timezone: " + collection.getTimeZone());

// retrieve a calendar resource filtered on UID
Calendar cal = collection.getCalendar(...);
```
