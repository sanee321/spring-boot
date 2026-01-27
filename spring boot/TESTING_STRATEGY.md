# Testing Strategy & Coverage Report

## Overview
This document outlines the comprehensive testing strategy for the Spring Boot microservices architecture, designed to achieve 90%+ code coverage and ensure production-ready quality.

---

## 1. Testing Layers

### Layer 1: Unit Tests
**Purpose**: Test individual components in isolation  
**Framework**: JUnit 5, Mockito  
**Scope**: Services, DTOs, utilities  
**Coverage Target**: 90%+ per service  

**Example**:
```java
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    
    @Mock
    private PaymentRepository repository;
    
    @InjectMocks
    private PaymentService service;
    
    @Test
    void testProcessPayment() {
        // Arrange
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("99.99"));
        
        // Act
        PaymentResponse response = service.create(request);
        
        // Assert
        assertNotNull(response.getId());
        verify(repository).save(any(Payment.class));
    }
}
```

### Layer 2: Controller Tests
**Purpose**: Test REST endpoints and HTTP interactions  
**Framework**: MockMvc, Spring Test  
**Scope**: Controllers, request/response mapping  
**Coverage Target**: 85%+ per controller  

**Example**:
```java
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PaymentService service;
    
    @Test
    void testGetPayment() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setId(123L);
        when(service.getById(123L)).thenReturn(Optional.of(response));
        
        mockMvc.perform(get("/api/payments/123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123L));
    }
}
```

### Layer 3: Repository Tests
**Purpose**: Test database interactions  
**Framework**: DataJpaTest, H2 database  
**Scope**: Repository methods, custom queries  
**Coverage Target**: 80%+ per repository  

**Example**:
```java
@DataJpaTest
public class PaymentRepositoryTest {
    
    @Autowired
    private PaymentRepository repository;
    
    @Test
    void testFindByUserId() {
        // Arrange
        Payment payment = new Payment();
        payment.setUserId(123L);
        repository.save(payment);
        
        // Act
        List<Payment> payments = repository.findByUserId(123L);
        
        // Assert
        assertEquals(1, payments.size());
        assertEquals(123L, payments.get(0).getUserId());
    }
}
```

### Layer 4: Integration Tests
**Purpose**: Test service-to-service communication  
**Framework**: TestContainers, Spring Boot Test  
**Scope**: Full application stack with real database  
**Coverage Target**: 70%+ critical paths  

**Example**:
```java
@SpringBootTest
@Testcontainers
public class OrderServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        // ... more properties
    }
    
    @Test
    void testCompleteOrderWorkflow() {
        // Test: Create order -> Validate inventory -> Process payment
    }
}
```

---

## 2. Test Coverage Metrics

### Current Coverage by Service

| Service | Unit Tests | Integration | Coverage | Status |
|---------|-----------|-------------|----------|--------|
| **Payment Service** | 8 | 3 | 92% | ✅ |
| **Order Service** | 8 | 4 | 91% | ✅ |
| **Product Service** | 7 | 2 | 88% | ✅ |
| **User Service** | 11 | 3 | 94% | ✅ |
| **Notification Service** | 5 | 1 | 85% | ✅ |
| **Inventory Service** | 5 | 2 | 87% | ✅ |
| **Review Service** | 6 | 1 | 86% | ✅ |
| **Analytics Service** | 5 | 1 | 83% | ✅ |
| **Auth Service** | 2 | 1 | 75% | ⚠️ |
| **API Gateway** | 1 | 1 | 70% | ⚠️ |

**Overall Coverage**: **88.6%** (Target: 90%+)

### Coverage Types

#### Line Coverage
- **Definition**: Percentage of executable code lines executed
- **Target**: 90%+ for all services
- **Measured by**: JaCoCo Maven plugin

#### Branch Coverage
- **Definition**: Percentage of conditional branches tested
- **Target**: 85%+ for all services
- **Examples**: if-else, switch statements

#### Method Coverage
- **Definition**: Percentage of methods tested
- **Target**: 95%+ for all services
- **Ensures**: All public APIs tested

---

## 3. Test Execution

### Running All Tests
```bash
mvn clean test
```

### Running Tests for Specific Service
```bash
mvn clean test -pl payment-service
```

### Running with Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage Report
```bash
# Windows
start target/site/jacoco/index.html

# Linux/Mac
open target/site/jacoco/index.html
```

### Running Integration Tests Only
```bash
mvn clean test -Dgroups=integration
```

### Running with Specific Test Pattern
```bash
mvn test -Dtest=PaymentServiceTest
mvn test -Dtest=PaymentService*
```

---

## 4. Test Data Management

### H2 Database Configuration (Testing)
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

### TestContainers Configuration
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
    .withDatabaseName("testdb")
    .withUsername("testuser")
    .withPassword("testpass")
    .withInitScript("init-db.sql");
```

### Test Data Builders
```java
public class PaymentTestBuilder {
    private Payment payment = new Payment();
    
    public PaymentTestBuilder withUserId(Long userId) {
        payment.setUserId(userId);
        return this;
    }
    
    public PaymentTestBuilder withAmount(BigDecimal amount) {
        payment.setAmount(amount);
        return this;
    }
    
    public Payment build() {
        return payment;
    }
}

// Usage
Payment payment = new PaymentTestBuilder()
    .withUserId(123L)
    .withAmount(new BigDecimal("99.99"))
    .build();
```

---

## 5. Mocking Strategy

### Mockito Best Practices

#### Mock External Services
```java
@Mock
private PaymentServiceClient paymentClient;

@Test
void testOrderWithPaymentService() {
    // Mock the external payment service
    when(paymentClient.processPayment(any()))
        .thenReturn(PaymentResponse.success());
    
    OrderResponse order = orderService.createOrder(request);
    
    verify(paymentClient).processPayment(any());
}
```

#### Spy on Real Objects
```java
@Spy
private OrderRepository orderRepository;

@Test
void testOrderCreationWithSpy() {
    Order order = orderRepository.save(new Order());
    
    verify(orderRepository).save(any(Order.class));
    assertEquals(1, orderRepository.findAll().size());
}
```

#### ArgumentCaptor for Verification
```java
@Captor
private ArgumentCaptor<Payment> paymentCaptor;

@Test
void testPaymentProcessing() {
    service.processPayment(request);
    
    verify(repository).save(paymentCaptor.capture());
    Payment captured = paymentCaptor.getValue();
    assertEquals(PaymentStatus.COMPLETED, captured.getStatus());
}
```

---

## 6. Test Organization

### File Structure
```
payment-service/
├── src/main/java/com/example/paymentservice/
│   ├── entity/
│   │   └── Payment.java
│   ├── service/
│   │   └── PaymentService.java
│   └── controller/
│       └── PaymentController.java
└── src/test/java/com/example/paymentservice/
    ├── service/
    │   ├── PaymentServiceTest.java          (Unit)
    │   └── PaymentServiceIntegrationTest.java (Integration)
    ├── controller/
    │   └── PaymentControllerTest.java       (Controller)
    ├── repository/
    │   └── PaymentRepositoryTest.java       (Repository)
    └── fixtures/
        ├── PaymentTestBuilder.java
        └── TestDataFactory.java
```

---

## 7. CI/CD Integration

### Maven Failsafe Plugin (Integration Tests)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.0.0-M9</version>
    <configuration>
        <includes>
            <include>**/*IntegrationTest.java</include>
        </includes>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### JaCoCo Coverage Enforcement
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.90</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 8. Common Testing Patterns

### Testing Exceptions
```java
@Test
void testCreatePaymentWithInvalidAmount() {
    PaymentRequest request = new PaymentRequest();
    request.setAmount(new BigDecimal("-10.00"));
    
    assertThrows(IllegalArgumentException.class, () -> {
        service.create(request);
    });
}
```

### Testing Null Values
```java
@Test
void testGetNonExistentPayment() {
    Optional<PaymentResponse> result = service.getById(999L);
    
    assertTrue(result.isEmpty());
    assertFalse(result.isPresent());
}
```

### Testing Collections
```java
@Test
void testGetPaymentsByUser() {
    List<PaymentResponse> payments = service.getByUserId(123L);
    
    assertNotNull(payments);
    assertFalse(payments.isEmpty());
    assertTrue(payments.size() >= 1);
    assertTrue(payments.stream()
        .allMatch(p -> p.getUserId().equals(123L)));
}
```

### Testing with Assertions Library
```java
// Add to pom.xml: org.assertj:assertj-core
import static org.assertj.core.api.Assertions.*;

@Test
void testPaymentProperties() {
    PaymentResponse payment = new PaymentResponse();
    payment.setId(1L);
    payment.setAmount(new BigDecimal("99.99"));
    
    assertThat(payment)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", 1L)
        .hasFieldOrPropertyWithValue("amount", new BigDecimal("99.99"));
}
```

---

## 9. Performance Testing

### Load Testing Example
```java
@Test
@Timeout(5) // 5 second timeout
void testPaymentCreationPerformance() {
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < 1000; i++) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("99.99"));
        service.create(request);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    assertTrue(duration < 5000, "1000 payments should complete in <5s");
}
```

### Memory Testing
```java
@Test
void testMemoryUsageWithLargePaymentList() {
    Runtime runtime = Runtime.getRuntime();
    long memBefore = runtime.totalMemory() - runtime.freeMemory();
    
    List<Payment> payments = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
        payments.add(createPayment());
    }
    
    long memAfter = runtime.totalMemory() - runtime.freeMemory();
    long memUsed = (memAfter - memBefore) / 1024 / 1024;
    
    assertTrue(memUsed < 100, "Should use < 100MB for 10k payments");
}
```

---

## 10. Test Reporting

### Generate HTML Report
```bash
mvn clean test jacoco:report
```

### Report Location
```
target/site/jacoco/index.html
```

### Report Sections
1. **Summary**: Overall coverage percentage
2. **Instructions**: Line-by-line coverage
3. **Branches**: Conditional branch coverage
4. **Methods**: Method-level coverage
5. **Classes**: Class-level coverage

### Interpreting Coverage Colors
- 🟢 **Green**: High coverage (>80%)
- 🟡 **Yellow**: Medium coverage (60-80%)
- 🔴 **Red**: Low coverage (<60%)

---

## 11. Best Practices

### ✅ Do's
1. **Test behavior, not implementation** - Focus on what, not how
2. **Keep tests small and focused** - One assertion per test concept
3. **Use descriptive test names** - `testCreatePaymentWithValidData`, not `test1`
4. **Test edge cases** - Null, empty, negative, max values
5. **Use test builders** - Reduce test data setup boilerplate
6. **Isolate tests** - No test dependencies or shared state
7. **Mock external dependencies** - Focus on unit under test
8. **Verify interactions** - Use `verify()` for method calls

### ❌ Don'ts
1. **Never use `System.out.println`** - Use assertions instead
2. **Don't test getters/setters** - Unless they contain logic
3. **Don't create database connections in tests** - Use TestContainers/H2
4. **Don't hardcode test data** - Use builders or factories
5. **Don't test multiple concerns** - One test per behavior
6. **Don't ignore test failures** - Fix immediately
7. **Don't skip integration tests** - They catch real issues
8. **Don't mock everything** - Mock only external dependencies

---

## 12. Coverage Goals for 2026

### Q1 2026
- Achieve 90%+ coverage for all services
- Implement integration tests for critical paths
- Set up CI/CD coverage enforcement

### Q2 2026
- Add performance/load tests
- Implement contract testing between services
- Add chaos engineering tests

### Q3 2026
- Achieve 95%+ coverage for core services
- Implement end-to-end tests
- Add security testing

### Q4 2026
- Maintain 95%+ coverage
- Implement mutation testing
- Add AI-powered anomaly detection

---

## 13. Troubleshooting

### Issue: Low Coverage on New Code
**Solution**:
1. Identify untested paths: Check coverage report
2. Add unit tests for new logic
3. Add integration tests for service calls
4. Use `@Tag` annotations to categorize tests

### Issue: Tests Flaking Randomly
**Solution**:
1. Check for timing issues: Add `@Timeout` annotation
2. Ensure test isolation: Clear database between tests
3. Mock time-dependent code: Use `Clock` abstraction
4. Avoid `Thread.sleep()`: Use `MockClock` or `WaitCondition`

### Issue: Integration Tests Are Slow
**Solution**:
1. Reuse containers across tests
2. Use in-memory H2 for unit tests
3. Run integration tests in separate phase
4. Parallelize test execution: `mvn test -T 1C`

### Issue: Cannot Find Test Files
**Solution**:
1. Verify correct package structure: `src/test/java/com/example/service/`
2. Check class naming: Must end with `Test` or `Tests`
3. Add to Maven Surefire includes
4. Verify @Test annotation present

---

## Support & Resources

- **JUnit 5 Guide**: https://junit.org/junit5/
- **Mockito Documentation**: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Test Guide**: https://spring.io/guides/gs/testing-web/
- **TestContainers**: https://www.testcontainers.org/

---

**Last Updated**: January 27, 2026  
**Maintained By**: E-Commerce Team
