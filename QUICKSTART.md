# Quick Start Guide

## Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (for frontend development)

## Local Development

### Option 1: Docker Compose (Recommended)

Start all services with one command:

```bash
# Start infrastructure only (Kafka + PostgreSQL)
docker-compose up -d kafka zookeeper postgres

# Wait for services to be healthy, then start all services
docker-compose --profile services up --build
```

Access:
- Frontend: http://localhost:5173
- Order Service API: http://localhost:8081/swagger-ui.html
- Inventory Service API: http://localhost:8082/swagger-ui.html
- Payment Service API: http://localhost:8083/swagger-ui.html

### Option 2: Manual Start

1. **Start Infrastructure:**
   ```bash
   docker-compose up -d kafka zookeeper postgres
   ```

2. **Build Backend Services:**
   ```bash
   mvn clean install
   ```

3. **Start Services (in separate terminals):**
   ```bash
   # Terminal 1
   cd order-service && mvn spring-boot:run
   
   # Terminal 2
   cd inventory-service && mvn spring-boot:run
   
   # Terminal 3
   cd payment-service && mvn spring-boot:run
   ```

4. **Start Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Testing the Saga Pattern

1. **Browse Products:**
   - Go to http://localhost:5173
   - View available products

2. **Create an Order:**
   - Add products to cart
   - Proceed to checkout
   - Place order

3. **Monitor Order Status:**
   - Order status page auto-updates
   - Watch status transitions: PENDING → PROCESSING → COMPLETED (or CANCELLED)

4. **Test Failure Scenarios:**
   - Payment service has 20% failure rate (configurable in `payment-service/src/main/resources/application.yml`)
   - Try multiple orders to see payment failures and compensation

5. **View Kafka Events:**
   ```bash
   docker exec -it kafka kafka-console-consumer.sh \
     --bootstrap-server localhost:9092 \
     --topic order-events \
     --from-beginning
   ```

## API Testing with Swagger

1. Order Service: http://localhost:8081/swagger-ui.html
   - POST `/orders` - Create order
   - GET `/orders/{id}` - Get order status

2. Inventory Service: http://localhost:8082/swagger-ui.html
   - GET `/products` - List products
   - GET `/products/{id}` - Get product details

3. Payment Service: http://localhost:8083/swagger-ui.html
   - GET `/payments/order/{orderId}` - Get payment status

## Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
cd order-service && mvn test
```

## Troubleshooting

### Services can't connect to Kafka
- Ensure Kafka is healthy: `docker ps`
- Check Kafka logs: `docker logs kafka`
- Verify Kafka is accessible: `docker exec -it kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092`

### Database connection errors
- Ensure PostgreSQL is running: `docker ps`
- Check connection: `docker exec -it postgres psql -U postgres -d ecommerce`

### Port conflicts
- Check if ports are in use: `netstat -an | findstr "8081 8082 8083 5173 9092 5432"`
- Stop conflicting services or change ports in `application.yml`

### Frontend can't connect to backend
- Verify backend services are running
- Check CORS settings (if needed)
- Verify API URLs in `frontend/src/api/client.js`

## Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```
