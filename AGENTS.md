# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

**智错本学习系统** — a Java 11 microservices-based system for student wrong-question capture, correction, spaced review, practice, and learning reports, built with Spring Boot 2.3 and Spring Cloud.

## Tech Stack

- **Java 11**, **Maven** multi-module project
- **Spring Boot 2.3.12**, **Spring Cloud Hoxton.SR12**, **Spring Cloud Alibaba 2.2.9**
- **Nacos** — service discovery and config center (port 8848)
- **Dubbo 2.7.23** — RPC between services
- **MyBatis-Plus 3.5.8** — ORM
- **MySQL 8.0**, **Redis**, **Druid** connection pool
- **Swagger** — API documentation
- **Lombok**, **MapStruct**, **EasyExcel**, **Hutool**, **Fastjson**

## Module Structure

| Module | Port | Description |
|---|---|---|
| `study-gateway` | 8080 | API gateway |
| `study-module-system` | 8081 | Student learning, wrong-question, file, review, report, and system foundation service |
| `study-module-schedule` | 8083 | Review reminders and scheduled jobs |
| `study-api` | — | Shared API DTOs, constants, and Dubbo contracts |
| `study-common` | — | Shared libraries (core, mybatis, redis, security, dubbo, excel) |

### study-common submodules
- `study-common-core` — domain classes (`Result`, `PageResult`), exceptions, utils, validation
- `study-common-mybatis` — MyBatis-Plus configuration
- `study-common-redis-starter` — Redis utilities
- `study-common-security` — Spring Security / JWT auth
- `study-common-dubbo` — Dubbo RPC configuration
- `study-common-excel` — EasyExcel utilities

### study-module-system business domains
The system module contains learning domains including `wrongquestion`, `review`, `questionbank`, `report`, `textbook`, `file`, `user`, `role`, `menu`, `resource`, and `dict`.

## Build & Run Commands

```bash
# Build all modules
mvn clean install

# Build and test the main business module
mvn -pl study-module-system -am test

# Run the system service
mvn spring-boot:run -pl study-module-system

# Package as JAR
mvn clean package -DskipTests
```

Services bootstrap from Nacos config center. Configuration is loaded from `bootstrap.yml` which points to Nacos at `127.0.0.1:8848`. Shared configs (`database-config.yaml`, `redis-config.yaml`, `security-config.yaml`) are pulled from Nacos.

## Code Architecture Patterns

### Layered structure (per domain package)

```
com.study.module.system.{domain}/
├── controller/   — REST endpoints (@RestController, @Api, @PreAuthorize)
├── service/      — Service interfaces (often split by operation: *CreateService, *UpdateService, *ListService, etc.)
│   └── impl/     — Service implementations
├── entity/       — MyBatis-Plus entity classes
├── mapper/       — MyBatis mapper interfaces
└── dto/          — Request/Response DTOs
    ├── request/
    └── response/
```

### Controller conventions
- `@RestController` with `@RequestMapping("/api/{resource}")`
- Swagger: `@Api(tags = "...")`, `@ApiOperation("...")`
- Auth: `@PreAuthorize("hasAuthority('system:{resource}:{action}')")`
- Params: `@Validated` on request DTOs; `@RequestBody` for POST
- Return: `Result<T>` or `Result<PageResult<T>>` wrapped with `ResultUtils.success()`

### Service conventions
- Services are split by operation type (CQRS-lite): `*CreateService`, `*UpdateService`, `*ListService`, `*DetailService`, `*DeleteService`, `*FileService`, etc.
- Use `@Autowired` field injection
- Entities extend MyBatis-Plus base classes

### Annotation processors
- **Lombok** and **MapStruct** — both must be on the annotation processor path. Maven config is in each module's `pom.xml`.

## Port Assignments

| Service | Port |
|---|---|
| Gateway | 8080 |
| System | 8081 |
| Schedule | 8083 |
| Redis | 6379 |
| MySQL | 3306 |
| Nacos | 8848 |

## Important Notes

- Unit tests live under `study-module-system/src/test`; run the module test command before handoff.
- Configuration is managed via Nacos config center, not local `application.yml` files.
- Git ignores local config files (`application-loc.yml`, `application-test.yml`).
