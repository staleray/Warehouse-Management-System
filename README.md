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

## Project structure

- `Controller/API` - REST endpoints and API exception handling.
- `Controller/View` - web page controllers.
- `Service` - inventory, order and parcel business logic.
- `Repository` - Spring Data repositories.
- `Entity` and `DTO` - persistence models and request objects.
- `src/main/resources/templates` - Thymeleaf pages.

Java sources are under `src/main/java/com/example/att1`.
