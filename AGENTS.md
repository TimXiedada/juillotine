# Repository Guidelines

Juillotine is a lightweight URL-shortening service built with Java 21 and JAX-RS (Jersey), packaged as a Maven WAR. These guidelines describe how the repository is organized and how to contribute.

## Project Structure & Module Organization

- `src/main/java/net/xiedada/juillotine/` — application source. Storage backends live in the `adapters/` subpackage (`MemoryAdapter`, `BerkeleyDBAdapter`, `JDBCAdapter`).
- `src/test/java/` — unit tests, mirroring the main package structure.
- `src/main/resources/conf/juillotine.properties` — runtime configuration; test overrides live in `src/test/resources/conf/`.
- `web/WEB-INF/web.xml` and `src/main/webapp/` — servlet and web assets.
- `pom.xml` — Maven build definition. Generated output goes to `target/` (gitignored).

## Build, Test, and Development Commands

- `mvn clean package` — compiles, runs tests, and packages the shaded, executable WAR.
- `mvn test` — runs unit tests only.
- `java -jar target/juillotine-0.0.1-SNAPSHOT.war` — starts the embedded Jetty server on port 9090.
- The same WAR can be deployed to any servlet container (Tomcat, Jetty) with Jersey support.

## Coding Style & Naming Conventions

- Indent with 4 spaces; no tabs. Files are UTF-8.
- Root package is `net.xiedada.juillotine`; storage adapters go in `net.xiedada.juillotine.adapters`.
- Use PascalCase for classes, camelCase for methods and variables, and UPPER_SNAKE_CASE for constants.
- Add the Apache 2.0 license header (see existing files) to every new source file.
- No formatter or linter is configured; match the style of surrounding code.

## Testing Guidelines

- JUnit 4 tests live in `src/test/java`, mirroring the main package hierarchy.
- Name test classes `XxxTest` and methods descriptively, e.g. `testOptionsFromProperties`.
- Storage adapters must pass the shared contract in `StorageAdapterTest`.
- Run with `mvn test`; reports are written to `target/surefire-reports/`.

## Commit & Pull Request Guidelines

- Git history is informal; use concise imperative summaries such as "Add JDBC connection pooling", optionally prefixed with `feat:`, `fix:`, or `docs:`.
- Keep each commit focused on one logical change.
- Pull requests should explain what changed and why, link any related issue, confirm `mvn clean package` passes, and include screenshots when behavior or configuration is visible.

## Security & Configuration Tips

- All tunables live in `conf/juillotine.properties` (host validation, URL sanitization, storage adapter).
- Never commit credentials or environment-specific values; keep `defaultURL` and `requiredHost` consistent.
