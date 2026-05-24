## ADDED Requirements

### Requirement: Export all vCards in a collection as a single aggregate vCard

`CardDavCollection.export()` SHALL return a single `net.fortuna.ical4j.vcard.VCard` that aggregates every vCard in the collection via `VCard.merge`. The method SHALL return an empty `VCard` if the collection contains no cards.

#### Scenario: Export aggregates multiple vCards

- **WHEN** the collection contains three vCards with distinct UIDs and the caller invokes `export()`
- **THEN** the returned `VCard` contains content merged from all three (no exception)

#### Scenario: Export empty collection

- **WHEN** the collection contains no vCards
- **THEN** `export()` returns an empty `VCard` (not `null`, no exception)
