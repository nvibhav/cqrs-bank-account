# CQRS bank account

A sample Kotlin/Spring Boot backend demonstrating Command Query Responsibility Segregation (CQRS), event sourcing, and Spring Integration. MongoDB stores the append-only account event stream and a separately updated account read model. Spring Integration `DirectChannel`s route commands to the command handler and polled events to the projection handler. Event appends are transactional; projections update asynchronously and can briefly lag behind accepted commands.

## Run locally

Use Java 25 and a MongoDB replica set on `localhost:27017` (MongoDB transactions are not supported on a standalone server). For a local sample instance:

```shell
docker run --name cqrs-mongo -p 27017:27017 mongo:8 --replSet rs0 --bind_ip_all
docker exec cqrs-mongo mongosh --eval "rs.initiate()"
```

Or configure `spring.data.mongodb.uri`, including the replica-set name.

```shell
./gradlew bootRun
```

The API documentation is available at `http://localhost:8080/swagger-ui.html`.

## API

Open an account:

```http
POST /api/v1/open-bank-account
Content-Type: application/json

{"accountHolder":"Ada Lovelace","accountType":"SAVINGS","openingBalance":100.00}
```

For safe client retries, send an `Idempotency-Key` header with every write request. Repeating the same key and payload returns the original result without appending another event; reusing a key with a different payload is rejected. Keys are optional, but retries without one are treated as new commands.

Deposit or withdraw:

```http
POST /api/v1/accounts/{id}/deposits
Content-Type: application/json

{"amount":25.00}
```

Use `/api/v1/accounts/{id}/withdrawals` for withdrawals. Read one account with `GET /api/v1/accounts/{id}`, list accounts with `GET /api/v1/accounts`, and close an account with `DELETE /api/v1/close-bank-account/{id}`. Accounts must have a zero balance before closure; amounts must be positive except for the non-negative opening balance.

Commands append versioned events and an idempotency receipt to MongoDB in one transaction. The unique `(aggregateIdentifier, version)` index and expected-version check prevent concurrent writes from silently overwriting one another; each event append and receipt either commit together or roll back. The event log is the durable source for a scheduled projection worker, which retries failed accounts on later polls. The read model records the last applied event version in the same document as the balance: duplicate deliveries are ignored, and out-of-order events are rejected for retry instead of corrupting the balance. A read immediately after a successful command may therefore briefly return the previous projection. Mongo transactions require a replica set and do not include the asynchronously updated read model.
