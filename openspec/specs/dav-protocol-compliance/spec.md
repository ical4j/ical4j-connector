# dav-protocol-compliance Specification

## Purpose

TBD - created by archiving change fix-dav-client-protocol-compliance. Update Purpose after archive.
## Requirements

### Requirement: MKCOL request body conforms to RFC 5689 Extended MKCOL

DAV client MKCOL requests that include properties SHALL use the request body format defined in RFC 5689 §3, with `<D:mkcol>` as the root element in the `DAV:` namespace, containing one or more `<D:set><D:prop>` blocks. The DAV client SHALL NOT use the non-standard `<D:create>` root element.

#### Scenario: Creating an addressbook against a strict CardDAV server

- **WHEN** the caller issues `addCollection(...)` against a server that strictly validates Extended MKCOL request bodies (Baikal)
- **THEN** the server accepts the request and the collection is created (no `400 Bad Request`)

#### Scenario: Creating a calendar against a lenient CalDAV server

- **WHEN** the caller issues `addCollection(...)` against a server that accepts either body format (Radicale)
- **THEN** the server accepts the request and the collection is created

### Requirement: Path resolution is idempotent for server-absolute responses

The DAV client SHALL accept paths in two forms when issuing requests: (a) repository-relative paths (without the configured repository prefix), and (b) server-absolute paths (already including the repository prefix). When given a path of form (b), the client SHALL NOT re-prepend the repository prefix.

The detection rule: a path is treated as server-absolute when it either equals the configured `repositoryPath` exactly, or starts with `repositoryPath` followed by `/`. All other non-null paths are treated as repository-relative.

#### Scenario: Server returns server-absolute calendar-home-set

- **WHEN** the server's `calendar-home-set` PROPFIND response returns a path like `/dav.php/calendars/test/` and the client's `repositoryPath` is `/dav.php`
- **THEN** subsequent requests against the returned path resolve to `/dav.php/calendars/test/` (not `/dav.php/dav.php/calendars/test/`)

#### Scenario: Server returns repository-relative path

- **WHEN** the server's response returns a path like `/calendars/test/` (without the repository prefix)
- **THEN** subsequent requests against the returned path resolve to `/dav.php/calendars/test/` as before

#### Scenario: Caller passes a relative path

- **WHEN** the caller passes a path that does not begin with `/`
- **THEN** the client prepends `<repositoryPath>/` exactly once

### Requirement: PROPFIND property-value parsing tolerates Element and String shapes

The DAV client's property-value response handler SHALL extract the underlying text value from a property regardless of whether the parsed value is an `org.w3c.dom.Element` (with text content as a child node) or a plain `java.lang.String` (already-extracted text). Unknown value shapes SHALL be returned as `null` rather than propagating a `ClassCastException`.

#### Scenario: Property value is an Element

- **WHEN** the parsed property value for `calendar-home-set` is an `Element` whose first child text node contains `/dav.php/calendars/test/` (Radicale)
- **THEN** the handler returns `/dav.php/calendars/test/`

#### Scenario: Property value is a String

- **WHEN** the parsed property value for `addressbook-home-set` is the string `/dav.php/addressbooks/test/` (Baikal)
- **THEN** the handler returns `/dav.php/addressbooks/test/`

#### Scenario: Property value is an unrecognised type

- **WHEN** the parsed property value is neither an `Element` nor a `String`
- **THEN** the handler returns `null` (no exception thrown)
