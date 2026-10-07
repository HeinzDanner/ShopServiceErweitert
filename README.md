# ShopService (Erweitert)

Java/Maven-Projekt zur Verwaltung von Produkten und Bestellungen mit Status-Workflow, Exception-Handling und
Bonus-Erweiterungen (Transaction-File, Mengen/Lagerbestand).

## Features

- **Bestellstatus**: `PROCESSING`, `IN_DELIVERY`, `COMPLETED`
- **Filter nach Status**: `getOrdersByStatus(OrderStatus status)` (Streams)
- **Optional im ProductRepo**: `getProductById(...)` gibt `Optional<Product>` zurück
- **Fehlerbehandlung**: `addOrder(...)` wirft Exception bei unbekannter Produkt-ID
- **Order-Update**: Statusänderung über `updateOrder(orderId, newStatus)` (mit Lombok `@With`)
- **Zeitstempel**: Bestellzeitpunkt als `Instant` (zeitzonenrobust)
- **ID-Generierung**: `IdService` erzeugt UUIDs
- **Älteste Bestellung je Status**: `getOldestOrderPerStatus()`
- **Transaction-File Support** über `transactions.txt`:
    - `addOrder`
    - `setStatus`
    - `printOrders`
- **Menge & Lagerbestand**:
    - Produkte besitzen Bestand
    - Bestellungen reduzieren Bestand
    - Bei zu wenig Bestand wird Bestellung abgelehnt
    - Dezimalmengen werden unterstützt

## Tech Stack

- Java
- Maven
- JUnit 5
- Lombok

## Projektstruktur (vereinfacht)

- `Main` – Einstiegspunkt, liest `transactions.txt` und führt Befehle aus
- `ShopService` – Fachlogik
- `ProductRepo` – Produktdaten + Lagerbestand
- `OrderMapRepo` / `OrderListRepo` – Bestellspeicher
- `IdService` – UUID-Erzeugung

## Starten

### Tests ausführen

```bash
mvn test
```

### Main ausführen

```bash
mvn exec:java
```

> Hinweis: Das `exec-maven-plugin` ist konfiguriert (`mainClass=Main`), aber **nicht** an eine Maven-Phase gebunden.  
> Dadurch startet `Main` **nicht automatisch** bei `mvn test`, sondern nur bei `mvn exec:java`.

## Format von `transactions.txt`

Beispiel:

```text
addOrder A 1:2.5 2:1 3:0.5
addOrder B 1:1
setStatus A COMPLETED
printOrders
```

### Befehle

- `addOrder <ALIAS> <PRODUKTID:MENGE>...`
    - Legt eine Bestellung im Status `PROCESSING` an
    - Speichert die erzeugte Order-ID intern unter dem Alias

- `setStatus <ALIAS> <STATUS>`
    - Setzt den Status einer zuvor über Alias referenzierten Bestellung

- `printOrders`
    - Gibt alle Bestellungen aus

## Hinweise

- Für reproduzierbare Ausführung sollte `transactions.txt` im Projekt-Root liegen.
- Alle zentralen Schritte sind durch Tests abgedeckt.
