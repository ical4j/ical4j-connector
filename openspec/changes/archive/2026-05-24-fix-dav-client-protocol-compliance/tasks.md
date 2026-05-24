## 1. MkColEntity RFC 5689 compliance

- [x] 1.1 In `ical4j-connector-dav/src/main/java/org/ical4j/connector/dav/request/MkColEntity.java`, change the root element from `"create"` to `"mkcol"` in `toXml(Document document)`. The inner `<D:set><D:prop>` structure stays unchanged.

## 2. DefaultDavClient.resolvePath idempotency

- [x] 2.1 In `ical4j-connector-dav/src/main/java/org/ical4j/connector/dav/DefaultDavClient.java`, modify `resolvePath(String path)` to detect server-absolute paths and return them unchanged after the `/+` → `/` collapse. The new branch comes before the existing `path.startsWith("/")` branch: when `path` either equals `repositoryPath` or starts with `repositoryPath + "/"`, return `path.replaceAll("/+", "/")` directly.

## 3. GetPropertyValue type tolerance + named-property lookup

- [x] 3.1 In `ical4j-connector-dav/src/main/java/org/ical4j/connector/dav/response/GetPropertyValue.java`, replace the unconditional `(Element) ... getValue()` cast with a `instanceof` branch: if the value is an `Element`, retain the current `propElement.getFirstChild().getNodeValue()` extraction; if the value is a `String`, return it as-is; otherwise return `null`. Cast results to `T` as needed.
- [x] 3.2 (Added during apply) Refactor `GetPropertyValue` to require a `DavPropertyName` constructor argument and look up the specific property via `getProperties(SC_OK).get(propertyName)` instead of `iterator().nextProperty()`. Switch the Element-shape extraction to `getTextContent()` so both bare-text and href-wrapped property shapes work. Update the 6 call sites in `AbstractDavObjectCollection.java`, `AbstractDavResource.java`, `CalDavCalendarStore.java` (3 sites), `CardDavStore.java` (1 site) to pass the relevant `DavPropertyName`.

## 4. GetCollections type tolerance

- [x] 4.1 (Added during apply) In `ical4j-connector-dav/src/main/java/org/ical4j/connector/dav/response/GetCollections.java`, replace the unconditional `(List<Element>) resourcetype.getValue()` cast with a `resourceTypeNames(Object)` helper that branches on the value's runtime type: a `List` is streamed, a single `Element` is wrapped in a one-element stream, any other shape returns an empty stream. Use the resulting `Stream<String>` of local element names for the resource-type match.

## 5. Unblock Baikal integration tests

- [x] 5.1 In `ical4j-connector-dav/src/test/groovy/org/ical4j/connector/dav/AbstractCalendarStoreIntegrationTest.groovy`, remove the `@IgnoreIf({ instance.getClass().simpleName.contains('Baikal') })` annotation and adjacent explanatory comment from the `'test getCollections accepts DEFAULT_WORKSPACE'` feature. Remove the now-unused `import spock.lang.IgnoreIf` if no other annotations remain.
- [x] 5.2 In `ical4j-connector-dav/src/test/groovy/org/ical4j/connector/dav/AbstractCardStoreIntegrationTest.groovy`, remove the `@IgnoreIf(...)` annotations (and the `BAIKAL_MKCOL_BUG` private constant + adjacent comments) from all six gated features. Remove the unused `import spock.lang.IgnoreIf` if no other annotations remain.

## 6. Verification

- [x] 6.1 Run integration tests across `RadicaleCalendar/Card` + `BaikalCalendar/Card` store classes. Result: 30 tests, 2 skipped (pre-existing `test collection creation` on both Cal containers), 0 failures, 0 errors. Skip count dropped from 9 to 2 — all 7 Cut B `@IgnoreIf` guards now run green.
- [x] 6.2 Run the full DAV module test suite. Result: 71 tests, 18 skipped (down from 27 pre-change), 0 failures, 0 errors. No regression on `RadicaleDavClientIntegrationTest`, `BaikalDavClientIntegrationTest`, `PathResolverTest`, `DavClientFactoryTest`, or any other previously-passing test class.
- [x] 6.3 Run `./gradlew :ical4j-connector-api:check :ical4j-connector-dav:check` — both clean. (Full `make check` still blocked by unrelated in-flight `:ical4j-connector-google:compileJava` failure — out of scope.)
