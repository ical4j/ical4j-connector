# iCal4j Connector API

The core module for iCal4j Connector.

## Overview

This library defines the base API for iCal4j Connector implementations, in addition to a REST API and
Command interface for manipulating iCalendar objects and collections.

## Connector API

The Connector API provides a common contract for connecting to iCalendar and vCard object stores. This API
is implemented by all the derivative modules providing connectivity to DAV servers, RDBMS, NoSQL, etc.

### Local Store Implementation

This library includes a Connector implementation supporting local file storage of iCalendar and vCard objects.
The local store may be used to manage a simple collection of vCard or iCalendar collections that are intended
to be accesses sequentially.


## Command Line Interface

This library also provides support for the Command pattern for manipulating iCalendar and vCard collections and
objects. These commands are used as the basis for a command line interface that allows you to access and manipulate
iCalendar and vCard data via interactive shell environments.

| Command           | Description                                              | Mandatory Arguments    |
|-------------------|----------------------------------------------------------|------------------------|
| create-collection | Create new object collection in the configured store     | Collection (JSON)      |
| create-calendar   | Create a new calendar object in the specified collection | Calendar (JSON)        |
| create-card       | Create a new card object in the specified collection     | Card (JSON)            |
| get-collection    |                                                          | Collection ID          |
| get-calendar      |                                                          | Calendar UID           |
| get-card          |                                                          | Card UID               |
| list-collections  |                                                          | -                      |
| list-calendars    |                                                          | -                      |
| list-cards        |                                                          | -                      |
| update-collection |                                                          | Collection (JSON), UID |
| update-calendar   |                                                          | Calendar (JSON), UID   |
| update-card       |                                                          | Card (JSON), UID       |
| replace-calendar  | Atomic equivalent to delete-calendar, create-calendar    | Calendar (JSON), UID   |
| replace-card      | Atomic equivalent to delete-card, create-card            | Card (JSON), UID       |
| delete-collection |                                                          | Collection UID         |
| delete-calendar   |                                                          | Calendar UID           |
| delete-card       |                                                          | Card UID               |


## REST API

The includes REST API is designed to provide fine-grained access to iCalendar and vCard resources, suitable for
use as a backend for calendar and card-based applications. Using the same Command pattern support as the CLI, the
REST API provides additional support for manipulation of nested components and properties directly within iCalendar
and vCard objects and collections.

   | Resource                                    | Description                                          | HTTP Verbs              |
|---------------------------------------------|------------------------------------------------------|-------------------------|
| /collections                                | List and create calendar and card collections        | GET, POST               |
| /collections/{uid}                          | Update and delete calendar and card collections      | GET, PATCH, DELETE      |
| /collections/{uid}/import                   | Import collection data from various formats          | POST                    |
| /collections/{uid}/query                    | Query collections using filter expressions           | GET, POST               |
| /collections/{uid}/calendars                | List and create calendars in a collection            | GET, POST               |
| /collections/{uid}/calendars/{uid}          | Update, replace and delete calendars in a collection | GET, PATCH, PUT, DELETE |
| /collections/{uid}/cards                    | List and create cards in a collection                | GET, POST               |
| /collections/{uid}/cards/{uid}              | Update, replace and delete cards in a collection     | GET, PATCH, PUT, DELETE |
 | /collections/{uid}/calendars/{uid}/freebusy | Query free or busy time for a calendar               | GET, POST               |
