# Agent Guidelines for iCal4j Connector

This document provides essential information for AI agents working on the iCal4j Connector codebase.

## Project Overview

iCal4j Connector is a Java client library that provides support for various iCalendar and vCard servers, with particular focus on CalDAV and CardDAV implementations. It's built on top of the iCal4j object model and provides a unified API for accessing different backend calendar services.

### Technology Stack
- **Language**: Java 17+ (source/target compatibility: Java 11)
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: Spock (Groovy), JUnit Platform, Testcontainers
- **Logging**: SLF4J with Log4j2
- **Modularity**: Java 9 modules (`module-info.java` files)
- **Dependencies**: iCal4j core, Apache HttpClient, Jackrabbit WebDAV

## Project Structure

### Multi-Module Architecture
- **`ical4j-connector-api`**: Core API and local implementations
- **`ical4j-connector-dav`**: CalDAV/CardDAV implementation using Jackrabbit WebDAV
- **`ical4j-connector-google`**: Google Calendar/Tasks/Keep integration
- **`ical4j-connector-jpa`**: JPA-based persistence implementation
- **`ical4j-connector-msgraph`**: Microsoft Graph API integration

### Key Directories
- `src/main/java/`: Production Java code
- `src/test/groovy/`: Spock test specifications
- `src/test/java/`: JUnit tests
- `src/main/resources/`: Resources including `module-info.java`
- `etc/`: Configuration files (Checkstyle, samples)
- `.github/workflows/`: CI/CD workflows

## Essential Commands

### Build and Test
```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run checks (including tests, static analysis)
./gradlew check

# Verify (more comprehensive than check)
./gradlew verify

# Install to local Maven repository
./gradlew publishToMavenLocal
```

### Using Makefile (Recommended)
```bash
# Run all checks
make check

# Run tests only
make test

# Clean and build
make build

# Install locally
make install

# Check current version
make currentVersion

# List API changes
make listApiChanges

# Approve API changes (requires justification)
make approveApiChanges "reason for change"
```

### Release Commands
```bash
# Mark next version
make markNextVersion 1.2.3

# Release (builds, verifies, tags)
make release

# Publish to Maven Central
make publish
```

## Code Conventions

### Package Structure
- Base package: `org.ical4j.connector`
- Module-specific packages: `org.ical4j.connector.{module}` (e.g., `dav`, `google`, `local`)
- Service interfaces in: `org.ical4j.connector.{module}.service`

### Naming Conventions
- **Classes**: PascalCase (e.g., `CalDavCalendarStore`, `LocalCalendarCollection`)
- **Interfaces**: Usually end with descriptive suffix (e.g., `ObjectStore`, `CalendarCollection`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_WORKSPACE`)
- **Methods**: camelCase following JavaBean conventions

### Design Patterns
- **Store Pattern**: `ObjectStore<T>` implementations for different backends
- **Collection Pattern**: `ObjectCollection<T>` for managing calendar/card collections
- **Builder Pattern**: Used for complex object construction (e.g., response builders)
- **Factory Pattern**: Used for creating DAV clients and resources
- **Event/Listener Pattern**: Support for store and collection events

### Key Abstractions
- `ObjectStore<C>`: Top-level interface for calendar/card stores
- `ObjectCollection<T>`: Interface for collections of calendar objects
- `CalendarCollection`: Specialized for calendar objects
- `CardCollection`: Specialized for vCard objects

### Module Dependencies
- All modules depend on `ical4j-connector-api`
- DAV module uses Apache Jackrabbit WebDAV library
- Google module uses Google API client libraries
- JPA module uses Jakarta Persistence API

## Testing Patterns

### Test Structure
- **Unit Tests**: Use Spock (Groovy) in `src/test/groovy/`
- **Integration Tests**: End with `IntegrationTest` suffix
- **Test Support**: Abstract base classes for common test scenarios

### Common Test Classes
- `AbstractLocalTest.groovy`: Base for local implementation tests
- `AbstractIntegrationTest.groovy`: Base for DAV integration tests
- `*TestSupport.groovy`: Utility classes for specific server testing

### Test Lifecycle Classes
- `*Lifecycle.java`: Manage test environment setup/teardown
- Use Testcontainers for integration testing with real servers

### Test Configuration
- Test properties in `src/test/resources/`
- Server configurations for integration tests (Radicale, Baikal)
- Logging configured via `logback.groovy` or `log4j2.properties`

## Configuration Management

### Build Configuration
- **Version Catalog**: `gradle/libs.versions.toml` - centralized dependency management
- **Root Build**: `build.gradle` - shared configuration for all submodules
- **Module Builds**: Individual `build.gradle` files for module-specific dependencies

### Version Management
- Uses Axion Release Plugin for semantic versioning
- Version determined from Git tags with `ical4j-connector-` prefix
- Branch-specific versioning strategy (master/develop)

### Dependencies
- Core dependencies defined in version catalog
- OSGi annotations for modular builds
- Spock BOM for consistent test dependencies

## Module-Specific Notes

### API Module (`ical4j-connector-api`)
- Core interfaces and abstract implementations
- Local file-based implementations
- Event system for store/collection notifications
- No external service dependencies

### DAV Module (`ical4j-connector-dav`)
- CalDAV/CardDAV protocol implementations
- Uses Apache Jackrabbit WebDAV library
- Comprehensive integration test suite
- Support for multiple DAV servers (Radicale, Baikal, etc.)

### Google Module (`ical4j-connector-google`)
- Google Calendar, Tasks, and Keep service integration
- Uses Google API client libraries
- OAuth2 authentication flow support

### JPA Module (`ical4j-connector-jpa`)
- Database persistence using Jakarta Persistence API
- Entity mappings for collections and objects
- Suitable for application-embedded use cases

### MSGraph Module (`ical4j-connector-msgraph`)
- Microsoft Graph API integration
- Support for Outlook Calendar, OneNote, Planner, To-Do
- Event building and calendar conversion utilities

## Common Gotchas

### Module System
- Each module has `module-info.java` - update when adding dependencies
- Transitive dependencies must be properly declared
- Use `requires transitive` for API dependencies

### DAV Implementation
- WebDAV operations are stateful - manage connections properly
- Different servers have quirks - check integration tests for patterns
- Path resolution varies between servers

### Testing
- Integration tests require proper server setup
- Use lifecycle classes for complex test environments
- Testcontainers tests may require Docker

### Version Compatibility
- Java 11 source compatibility but targeting Java 17+ runtime
- iCal4j version compatibility is critical
- Check API changes with RevAPI plugin before releases

## Error Handling

### Exception Hierarchy
- `ObjectStoreException`: Base exception for store operations
- `ObjectNotFoundException`: When collections/objects don't exist
- `FailedOperationException`: When operations fail

### Connection Management
- Always check `isConnected()` before operations
- Properly handle connection failures
- Implement retry logic for transient failures

## Development Workflow

### Before Making Changes
1. Run `make check` to ensure clean starting point
2. Check for API compatibility with `make listApiChanges`
3. Review existing patterns in similar modules

### After Making Changes
1. Run relevant tests: `./gradlew :module:test`
2. Run full check: `make check`
3. If API changes, document and approve: `make approveApiChanges "description"`
4. Update module-info.java if adding dependencies

### Common Operations
- Adding a new module: Update `settings.gradle` and follow existing patterns
- Adding dependencies: Update `gradle/libs.versions.toml` first
- Adding tests: Follow Spock patterns for new tests, JUnit for simple cases

## Debugging Tips

### Logging
- Use SLF4J for logging in production code
- Configure logging in test resources
- Enable debug logging for DAV operations when troubleshooting

### Integration Tests
- Check test server configurations in `src/test/resources/`
- Use breakpoints in lifecycle classes for setup debugging
- Container logs available when using Testcontainers

### Build Issues
- Clean build: `./gradlew clean`
- Check dependency conflicts: `./gradlew dependencies`
- Verify Java version: ensure Java 17+ for building

## Performance Considerations

### DAV Operations
- Batch operations when possible
- Use appropriate query depth for collections
- Implement connection pooling for high-volume scenarios

### Local Storage
- Consider file locking for concurrent access
- Monitor disk space for large collections
- Implement cleanup strategies for temporary files

This guide should help agents understand the codebase structure and conventions. When in doubt, examine existing implementations and follow established patterns.