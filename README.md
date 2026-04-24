# OrderFlow Engine

An event-driven order processing backend built with Spring Boot and Apache Kafka. Orders flow through processing stages via Kafka topics, with retry logic and a dead-letter queue for failed messages.

## What it does

- **Order lifecycle management** — CREATED → PAYMENT_VERIFIED → FULFILLMENT → COMPLETED
- **Event-driven flow** — each stage transition publishes a Kafka event; the next stage listens and reacts
- **Optimistic locking** — prevents race conditions when multiple threads process the same order
- **Retry + Dead-letter queue** — failed messages retry 3 times with backoff, then go to a DLT for investigation
- **OpenAPI docs** — interactive Swagger UI at `/swagger-ui.html`

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| Messaging | Apache Kafka |
| Database | PostgreSQL 16 |
| Testing | JUnit 5, Mockito |
| API Docs | SpringDoc OpenAPI |
| Runtime | Java 21 |
| Containers | Docker, Docker Compose |

## Run locally (one command)

```bash
docker-compose up --build
```

App runs on `http://localhost:8081`
Swagger UI: `http://localhost:8081/swagger-ui.html`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create a new order |
| GET | `/api/orders` | List all orders |
| GET | `/api/orders/{id}` | Get order by ID |

## Example

```bash
# Create an order
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "productName": "Product A",
    "amount": 149.99
  }'
```

The order is saved, a Kafka event is published, and you can watch it move through the stages in the logs.

## Kafka Topics

| Topic | Purpose |
|-------|---------|
| `order.created` | Fired when a new order is placed |
| `order.payment.verified` | Fired after payment is confirmed |
| `order.fulfillment` | Fired when order enters fulfillment |
| `order.dead-letter` | Failed messages after 3 retries |

## Project Structure

```
src/main/java/com/nivedha/orderflow/
├── controller/     # REST endpoints
├── kafka/          # Producer and consumer
├── model/          # Order entity with optimistic locking
├── repository/     # Database access
└── service/        # Business logic and state transitions
```
