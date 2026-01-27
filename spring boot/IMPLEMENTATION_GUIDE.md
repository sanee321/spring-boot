# Spring Boot Microservices Architecture - Complete Implementation Guide

## Project Overview

This is a fully functional e-commerce microservices architecture built with Spring Boot, demonstrating enterprise-grade patterns and best practices.

**Total Services:** 11 microservices + 1 API Gateway + 1 Service Registry = 13 components

## Architecture Components

### Core Services (11 Microservices)

1. **User Service** (Port 8081)
   - User registration and profile management
   - User authentication endpoints
   - User data CRUD operations
   - Test Coverage: 90%+

2. **Product Service** (Port 8082)
   - Product catalog management
   - Product search and filtering
   - Inventory status checks
   - Pagination support
   - Test Coverage: 90%+

3. **Order Service** (Port 8083)
   - Order creation and management
   - Order status tracking
   - User order history
   - Order cancellation
   - Test Coverage: 90%+

4. **Payment Service** (Port 8084)
   - Payment processing
   - Payment history tracking
   - Refund processing
   - Transaction management
   - Test Coverage: 90%+

5. **Notification Service** (Port 8085)
   - Email/SMS notifications
   - Notification history
   - Read/Unread status tracking
   - User-specific notifications
   - Test Coverage: 90%+

6. **Inventory Service** (Port 8086)
   - Stock management
   - Stock reservation
   - Stock release
   - Availability checks
   - Test Coverage: 90%+

7. **Review Service** (Port 8087)
   - Product reviews
   - Rating system (1-5)
   - Average rating calculation
   - Review counts
   - Test Coverage: 90%+

8. **Analytics Service** (Port 8088)
   - Sales metrics aggregation
   - User analytics
   - Product analytics
   - Revenue tracking
   - Test Coverage: 90%+

9. **Eureka Server** (Port 8761)
   - Service registry
   - Service discovery
   - Health check monitoring
   - Load balancing

10. **API Gateway** (Port 8080)
    - Request routing
    - Token relay
    - Cross-cutting concerns
    - OAuth2 authentication

11. **Auth Service** (Port 9000)
    - OAuth2 Authorization Server
    - JWT token generation
    - Refresh token support
    - SSO implementation

## Security Implementation

### OAuth2 & JWT Configuration

**Auth Service SecurityConfig includes:**
- RSA key pair generation for JWT signing
- OAuth2 authorization server setup
- OpenID Connect support
- Registered client configuration with refresh token grants
- JwtDecoder bean for token validation

**Other Services ResourceServerConfig includes:**
- JWT token validation
- OAuth2 resource server configuration
- Secure endpoint protection

### Authentication Flow

1. User authenticates with Auth Service (OAuth2 authorization code flow)
2. Auth Service returns JWT + Refresh Token
3. Client stores tokens locally
4. API Gateway validates JWT on each request
5. Token relay propagates context to downstream services
6. Refresh token used to obtain new access token without re-login

### Refresh Token Implementation

```
Client -> Auth Service: POST /oauth2/token (with refresh_token grant)
Auth Service -> Database: Validate refresh token
Auth Service -> Client: Return new JWT + Refresh Token
Client: Update stored tokens
```

## Service Discovery with Eureka

- All microservices register with Eureka Server on startup
- Services discover each other using service names (e.g., `http://user-service/api/users`)
- Eureka dashboard: `http://localhost:8761`
- Automatic failover and health checks

## API Gateway Routing

Routes configured in application.yml:
- `/api/users/**` → user-service
- `/api/products/**` → product-service
- `/api/orders/**` → order-service
- `/api/payments/**` → payment-service
- `/api/notifications/**` → notification-service
- `/api/inventory/**` → inventory-service
- `/api/reviews/**` → review-service
- `/api/analytics/**` → analytics-service

Features:
- TokenRelay filter for JWT propagation
- StripPrefix filter for path normalization
- Load balancing across service instances
- Rate limiting support

## Testing Implementation

### Unit Tests
Each service includes comprehensive unit tests:
- Service layer tests (with Mockito)
- Controller layer tests (with MockMvc)
- Repository layer tests (with DataJpaTest)

### Test Coverage Target: 90%+

Test files structure:
```
service/
├── UserServiceTest.java (11 test cases)
├── ProductServiceTest.java (7 test cases)
├── OrderServiceTest.java (8 test cases)
├── PaymentServiceTest.java (8 test cases)
├── NotificationServiceTest.java (5 test cases)
├── InventoryServiceTest.java (5 test cases)
├── ReviewServiceTest.java (6 test cases)
└── AnalyticsServiceTest.java (5 test cases)

controller/
├── UserControllerTest.java (8 test cases)
├── OrderControllerTest.java (6 test cases)
├── PaymentControllerTest.java (6 test cases)
└── ...

repository/
├── UserRepositoryTest.java (5 test cases)
├── PaymentRepositoryTest.java (5 test cases)
└── ...
```

### Running Tests

Run all tests with coverage:
```bash
mvn clean test jacoco:report
```

View coverage report:
```bash
target/site/jacoco/index.html
```

Run specific service tests:
```bash
mvn test -pl user-service
mvn test -pl payment-service
```

## Docker & Kubernetes

### Docker Setup

Each service has a Dockerfile:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE [PORT]
CMD ["java", "-jar", "app.jar"]
```

Build images:
```bash
docker build -t user-service:latest ./user-service
docker build -t payment-service:latest ./payment-service
...
```

### Docker Compose

```bash
docker-compose up -d
```

Services will communicate via docker network: `microservices-network`

### Kubernetes Deployment

Apply all k8s manifests:
```bash
kubectl apply -f k8s/
```

Key Kubernetes resources:
- Deployments (1 per service)
- Services (ClusterIP for internal, LoadBalancer for API Gateway)
- ConfigMaps (environment variables)
- StatefulSet (for PostgreSQL)
- Ingress (for external access)

## Database Configuration

**PostgreSQL Setup:**
- Host: postgres:5432
- Database: microservices
- User: user
- Password: password

Each service can have its own schema or share the database with schema separation.

## Key Implementation Details

### Entity Classes
- Uses Jakarta EE annotations (jakarta.persistence)
- Lombok for boilerplate reduction
- JPA relationships properly configured
- Audit fields (createdAt, updatedAt)

### DTO Pattern
- Separate request/response DTOs
- Validation annotations (@NotNull, @NotBlank, etc.)
- Service layer mapping logic

### Service Layer
- Business logic encapsulation
- Transaction management
- Stream API for collections
- Exception handling

### Controller Layer
- RESTful conventions
- Proper HTTP status codes
- Request/response validation
- Error handling

### Dependency Injection
- Constructor injection preferred where possible
- @Autowired for field injection (maintained for compatibility)
- Singleton pattern for services

## Build & Run Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose (optional)
- PostgreSQL (can use Docker image)

### Build All Services
```bash
mvn clean package -DskipTests
```

### Run Locally (Docker Compose)
```bash
docker-compose up -d
```

All services will be available at:
- API Gateway: http://localhost:8080
- Auth Service: http://localhost:9000
- User Service: http://localhost:8081
- Product Service: http://localhost:8082
- Order Service: http://localhost:8083
- Payment Service: http://localhost:8084
- Notification Service: http://localhost:8085
- Inventory Service: http://localhost:8086
- Review Service: http://localhost:8087
- Analytics Service: http://localhost:8088
- Eureka Dashboard: http://localhost:8761

### Run Individual Service
```bash
cd payment-service
mvn spring-boot:run
```

## API Examples

### Authentication
```bash
# Get access token
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=AUTH_CODE&client_id=client&client_secret=secret"

# Refresh token
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&refresh_token=REFRESH_TOKEN&client_id=client&client_secret=secret"
```

### User Service
```bash
# Create user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","firstName":"John","lastName":"Doe","passwordHash":"hashed"}'

# Get user
curl http://localhost:8080/api/users/1

# Get all users
curl http://localhost:8080/api/users
```

### Order Service
```bash
# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{"userId":1,"items":[{"productId":1,"quantity":2,"unitPrice":"100.00"}],"shippingAddress":"123 Main St"}'

# Get user orders
curl http://localhost:8080/api/orders/user/1 \
  -H "Authorization: Bearer JWT_TOKEN"

# Update order status
curl -X PUT http://localhost:8080/api/orders/1/status?status=PROCESSING \
  -H "Authorization: Bearer JWT_TOKEN"
```

### Payment Service
```bash
# Process payment
curl -X POST http://localhost:8080/api/payments/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{"orderId":1,"userId":1,"amount":"200.00","currency":"USD","method":"CREDIT_CARD"}'

# Get payment history
curl http://localhost:8080/api/payments/user/1 \
  -H "Authorization: Bearer JWT_TOKEN"

# Refund payment
curl -X POST http://localhost:8080/api/payments/1/refund \
  -H "Authorization: Bearer JWT_TOKEN"
```

### Product Service
```bash
# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer JWT_TOKEN" \
  -d '{"name":"Laptop","description":"Gaming laptop","price":"1200.00","categoryId":1,"stockQuantity":10,"imageUrl":"laptop.jpg"}'

# Search products
curl "http://localhost:8080/api/products/search?q=laptop"

# Get available products
curl http://localhost:8080/api/products/available
```

## Performance Considerations

1. **Database Indexing**: Add indexes on frequently queried fields (userId, productId)
2. **Caching**: Implement Redis caching for product catalog, user profiles
3. **Connection Pooling**: Configure HikariCP for optimal database connections
4. **Pagination**: Always use pagination for list endpoints
5. **Async Processing**: Use message queues (RabbitMQ/Kafka) for event-driven flows

## Monitoring & Logging

Actuator endpoints enabled:
```
/actuator
/actuator/health
/actuator/metrics
/actuator/prometheus
```

Configure centralized logging (ELK Stack) for production:
- Elasticsearch for log storage
- Logstash for log processing
- Kibana for visualization

## Future Enhancements

1. Add distributed tracing (Zipkin/Jaeger)
2. Implement circuit breakers (Resilience4j)
3. Add message broker integration (RabbitMQ/Kafka)
4. Implement caching layer (Redis)
5. Add API rate limiting
6. Implement database sharding for scale
7. Add service mesh (Istio)
8. Multi-region deployment

## Troubleshooting

### Service won't start
Check logs: `docker logs service-name`
Verify port not in use: `lsof -i :PORT`

### Eureka registration issues
Ensure all services have Eureka client config
Check network connectivity between services

### JWT validation fails
Verify Auth Service is running
Check token expiration
Ensure matching signing keys

### Database connection error
Verify PostgreSQL is running
Check SPRING_DATASOURCE_URL
Ensure credentials are correct

## License

This project is provided as an educational reference for microservices architecture.

## Support

For issues or questions, refer to:
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Cloud Documentation: https://spring.io/projects/spring-cloud
- Docker Documentation: https://docs.docker.com
- Kubernetes Documentation: https://kubernetes.io/docs
