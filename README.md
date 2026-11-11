# CQRS bank account

A sample Kotlin/Spring Boot backend demonstrating CQRS, event sourcing, and Spring Integration. MongoDB stores account events separately from the account read model. Spring Integration routes commands and polled events; commands return after their events and idempotency receipts commit, while the read model is updated asynchronously and may briefly lag.

## Requirements and local setup

- Java 25
- Docker, for the MongoDB replica set used by this sample

Start MongoDB in one terminal:

```shell
docker run --name cqrs-mongo -p 27017:27017 mongo:8 --replSet rs0 --bind_ip_all
```

In another terminal, initialize the replica set:

```shell
docker exec cqrs-mongo mongosh --eval "rs.initiate()"
```

MongoDB transactions require a replica set; a standalone MongoDB server is not sufficient. The application uses the `cqrs_bank_account` database on `localhost:27017`. Set `spring.mongodb.uri` to use a different instance, including its replica-set name.

Run the app:

```shell
# Windows
.\gradlew.bat bootRun

# Linux/macOS
bash ./gradlew bootRun
```

OpenAPI UI: <http://localhost:8080/swagger-ui/index.html>

## API

Open an account:

```http
POST /api/v1/open-bank-account
Content-Type: application/json
Idempotency-Key: open-ada-001

{"accountHolder":"Ada Lovelace","accountType":"SAVINGS","openingBalance":100.00}
```

Deposit or withdraw:

```http
POST /api/v1/accounts/{id}/deposits
Content-Type: application/json
Idempotency-Key: deposit-ada-001

{"amount":25.00}
```

Use `/api/v1/accounts/{id}/withdrawals` for withdrawals. Read an account with `GET /api/v1/accounts/{id}`, list accounts with `GET /api/v1/accounts`, and close an account with `DELETE /api/v1/close-bank-account/{id}`. All three write operations accept an optional `Idempotency-Key` header.

### Idempotency keys

The client creates a unique opaque key (for example, a UUID) for each logical write and reuses that key with the exact same request if retrying after a timeout or uncertain response. A repeated key and payload returns the original result without appending another event. Reusing the key with a different operation or payload is rejected. Keys are optional; requests without one are treated as new commands on every attempt. Receipts are stored in MongoDB and currently have no expiry.

### Account rules and consistency

Amounts use decimal values, not floating-point numbers. Opening balances must be non-negative; deposits and withdrawals must be positive. Accounts can only be closed at a zero balance. Invalid requests return `400`, missing accounts return `404`, and business-rule conflicts (including a key reused with a different payload) return `409`.

Each command's versioned events and idempotency receipt are committed in one MongoDB transaction. A unique `(aggregateIdentifier, version)` index and expected-version check protect the event stream from conflicting writes. MongoDB transactions do not include the read model: a scheduled projection worker reads the event log, and retries failed accounts on later polls. The read model stores the last applied event version with the account data, so duplicate deliveries are ignored and version gaps are retried instead of corrupting the balance. A read immediately after a successful command may therefore briefly return the previous projection.

## Tests and CI

Run tests locally:

```shell
# Windows
.\gradlew.bat test

# Linux/macOS
bash ./gradlew test --no-daemon
```

The Mongo persistence integration tests start a MongoDB replica set with Testcontainers. They verify event and receipt persistence, idempotency, expected-version checks, and idempotent projection updates. They run when Docker is available and are skipped otherwise. GitHub Actions runs the complete test suite for pull requests targeting `main` and for pushes to `main`, using Java 25 and Docker.
