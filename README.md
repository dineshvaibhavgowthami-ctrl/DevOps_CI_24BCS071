# Warehouse Inventory and Barcode Tracking System

A small Java 21, Spring Boot, Maven, H2 and vanilla HTML/CSS/JavaScript inventory application. The sample warehouse has two racks, two shelves per rack and three 100-unit cells per shelf.

## Run

Install Java 21 and Maven, then run from the project directory:

```text
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). H2 stores data in the local `data` directory; the H2 console is available at `/h2-console` (JDBC URL `jdbc:h2:file:./data/warehouse`, user `sa`, blank password).

## Tests and coverage

```text
mvn clean test
```

JaCoCo creates `target/site/jacoco/index.html` during the test phase. The report reflects the tests actually executed.

## Core service modules

- `LocationAllocationService`: validates quantities, selects the first suitable cell and allocates item capacity.
- `BarcodeService`: generates barcodes after a cell has been selected and searches existing inventory.
- `InventoryMovementService`: transfers capacity, records item movement history and verifies exits.

The three JUnit 5 / Mockito test classes cover allocation and capacity, barcode generation and lookup, item moves and history, and accepted/rejected exit verification. REST endpoints are under `/api`; the frontend calls them directly.
