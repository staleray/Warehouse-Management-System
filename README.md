# Warehouse Management System

A Java web application for managing warehouse inventory, orders and parcels, built with Spring Boot and PostgreSQL.

## Features

- Product management with SKU, category, warehouse location and stock quantities.
- Stock movement history for inbound, outbound and manual adjustments.
- Order creation with stock availability validation and order status tracking.
- Parcel management linked to orders.
- Server-rendered web interface with Thymeleaf and REST controllers.

## Technology

Java 17, Spring Boot 3.5.7, Spring Data JPA / Hibernate, PostgreSQL, Thymeleaf, Bean Validation, Lombok and Gradle Wrapper.

## Run locally

1. Install JDK 17 and PostgreSQL.
2. Create an empty PostgreSQL database named `small_warehouse`.
3. Set the database credentials as environment variables. In PowerShell:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/small_warehouse'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = '<your-local-database-password>'
.\gradlew.bat bootRun
```

On Linux or macOS, set the same environment variables and run `sh ./gradlew bootRun`.

4. Open http://localhost:8080. The home page redirects to the product list.

Hibernate updates the database schema on startup (`ddl-auto=update`). Use a local development database. Credentials are read from the environment; no real database password is included in this repository.

## Project structure

- `Controller/API` - REST endpoints and API exception handling.
- `Controller/View` - web page controllers.
- `Service` - inventory, order and parcel business logic.
- `Repository` - Spring Data repositories.
- `Entity` and `DTO` - persistence models and request objects.
- `src/main/resources/templates` - Thymeleaf pages.

Java sources are under `src/main/java/com/example/att1`.

## Build and tests

```powershell
.\gradlew.bat build
```

The application context test requires a reachable PostgreSQL database and the environment variables above. This repository is a portfolio project; no application authentication is configured.