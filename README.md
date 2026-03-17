# Juillotine

>This application is a work in progress. It still cannot run properly in any forms.

A lightweight URL shortening service built with Java and JAX-RS.

## Features

- Create short URLs with custom shortcodes
- Redirect shortcodes to original URLs
- Host validation and URL sanitization
- Pluggable storage adapters (memory, Berkeley DB, JDBC)
- Configurable shortcode generation (fixed charset or MD5-based)
- RESTful API

## Quick Start

### Prerequisites

- Java 21 or later
- Maven 3.6+

### Build

```bash
mvn clean package
```

### Configuration

Copy `src/main/resources/conf/juillotine.properties` to your classpath and modify:

```properties
juillotine.defaultURL=https://example.com/
juillotine.requiredHost=example.com
juillotine.dbAdapter=MemoryAdapter
```

### Run

Deploy the WAR file to a servlet container (Tomcat, Jetty, etc.) with Jersey support.

## API Endpoints

- `GET /` - Redirects to default URL
- `GET /{code}` - Redirects to original URL for given shortcode
- `POST /` - Create a new short URL
  - Form parameters: `url` (required), `code` (optional custom shortcode)

## Storage Adapters

- **MemoryAdapter** (default): In-memory storage using Google Guava
- **BerkeleyDBAdapter**: Persistent storage with Berkeley DB
- **JDBCAdapter**: Database storage via JDBC

## License

See [LICENSE.txt](LICENSE.txt)