## Why

The `complete-dav-module-parity` change established the `DEFAULT_WORKSPACE` contract on the DAV stores: workspace methods accept null/DEFAULT_WORKSPACE and reject anything else with `ObjectStoreException`. That was the right scope for parity with MSGraph/Google, but it leaves real DAV semantics on the floor. In DAV's data model, every collection lives under a principal (user) — the `workspace` argument of the store methods is the natural place to express "operate on a different principal's collections." Servers grant cross-principal access via standard DAV delegation (RFC 3744 ACLs, RFC 6638 calendar-proxy, etc.); the client just needs to be willing to address those principals. This change drops the rejection gate and uses the workspace value as the principal name in path resolution.

We also bundle a small same-family fix flagged as latent in the previous change's design notes: `EventQuery` emits `<C:getetag/>` (CalDAV namespace) where RFC 4791 §9.5 specifies `<D:getetag/>` (DAV namespace).

## What Changes

- **Drop `assertDefaultWorkspace(...)` rejection gate** from all workspace-parameterised methods on `CalDavCalendarStore` and `CardDavStore`. Replace with: if workspace is `null` or `DEFAULT_WORKSPACE`, delegate to the existing session-user code path; otherwise treat the workspace value as a principal name and route to a new principal-targeted code path.
- **Add principal-aware home-set helpers**: `CalDavCalendarStore.findCalendarHomeSet(String principal)` and `CardDavStore.findAddressBookHomeSet(String principal)` private overloads that propfind that principal's path for the home-set property. The current no-arg versions (which read from the session user) become thin wrappers around the new overloads.
- **Route workspace-parameterised methods through the new helpers**: `getCollections(workspace)`, `getCollection(id, workspace)`, `addCollection(name, workspace)`, `addCollection(id, name, description, supportedComponents, timezone, workspace)` resolve paths against the given principal.
- **`listWorkspaceIds()` unchanged**: continues to return `[DEFAULT_WORKSPACE]`. The sentinel meaning "the session user's workspace" remains. Discovering all principals visible to the session user is a separate, future change (heavyweight DAV operation against the principal collection set).
- **Fix `EventQuery.toXml`**: change `newCalDavElement(document, DavConstants.PROPERTY_GETETAG)` to `newElement(document, DavConstants.PROPERTY_GETETAG, DavConstants.NAMESPACE)`. RFC compliance, same shape as the fix already applied to `CalendarMultiget`.
- **Out of scope** (future changes):
  - Provisioning/discovering delegated collections in Baikal/Radicale test fixtures.
  - Server-side `listWorkspaceIds()` discovery (PROPFIND on principal collection set).
  - Higher-level "share/unshare" operations.

## Capabilities

### New Capabilities
<!-- None — this change extends existing capabilities. -->

### Modified Capabilities
- `dav-calendar-connector`: The DEFAULT_WORKSPACE-rejection requirement on workspace-parameterised methods is replaced with one that accepts arbitrary principal names. The behaviour for `null`/`DEFAULT_WORKSPACE` is unchanged; the new behaviour is that any other string is interpreted as a principal name and used in path resolution, with server-side delegation errors (typically 403) propagating as `ObjectStoreException`.
- `dav-card-connector`: Same change shape — DEFAULT_WORKSPACE-rejection requirement replaced with the principal-name interpretation.

## Impact

- **Module**: `ical4j-connector-dav`
  - Modified: `CalDavCalendarStore.java`, `CardDavStore.java` (workspace methods + new principal-targeted helpers + delete `assertDefaultWorkspace`), `request/EventQuery.java` (getetag namespace fix).
  - Test changes: extend `AbstractCalendarStoreIntegrationTest` + `AbstractCardStoreIntegrationTest` with positive (session-user equivalence) and negative (foreign-principal 403) workspace scenarios. Add an `EventQuery` round-trip test via `getEventsForTimePeriod` to confirm the namespace fix is exercised on both servers.
- **API surface**: No changes to method signatures on `ObjectStore` or the DAV store classes. **BREAKING** in semantics for any caller that today passes a non-default workspace value expecting it to throw — but no internal caller does this, and the prior behaviour (reject) was itself a placeholder pending real implementation. The proposal supersedes the previous rejection contract on the modified capabilities.
- **Dependencies**: No new dependencies.
- **Risk**: Principal name to path-prefix mapping is server-specific. For Baikal and Radicale (both `pathResolver.RADICALE` / `pathResolver.BAIKAL`), the workspace value already participates in path-format strings (`/%2$s/%1$s` and `/calendars/%2$s/%1$s` respectively), so route-and-resolve already works at the path layer. The home-set propfind step is where the principal-targeted lookup actually exercises new code.
