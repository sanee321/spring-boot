# API Security Guide

## OAuth2 & JWT Token Configuration

### Overview
This microservices architecture uses OAuth2 authorization server with JWT (JSON Web Tokens) for secure API access. The Auth Service handles token generation and validation, while all other services act as resource servers.

---

## 1. OAuth2 Flow

### Authorization Code Flow (For Web Applications)

```
┌─────────┐                                      ┌──────────────┐
│  Client │                                      │ Auth Service │
└────┬────┘                                      └──────┬───────┘
     │                                                   │
     │  1. Redirect to login                           │
     ├──────────────────────────────────────────────────>
     │                                                   │
     │  2. User authenticates                           │
     │<──────────────────────────────────────────────────
     │                                                   │
     │  3. Authorization code issued                     │
     │<──────────────────────────────────────────────────
     │                                                   │
     │  4. Exchange code for token                       │
     ├──────────────────────────────────────────────────>
     │                                                   │
     │  5. Access token + Refresh token                 │
     │<──────────────────────────────────────────────────
```

### Implementation

#### Step 1: Initiate Login
```bash
GET /oauth2/authorization/{client-id}
  ?redirect_uri=http://localhost:3000/callback
  &response_type=code
  &scope=openid%20profile%20email
```

#### Step 2: Auth Service Redirects to Login
User enters credentials at:
```
http://localhost:9000/login
```

#### Step 3: Receive Authorization Code
After successful login, redirected to:
```
http://localhost:3000/callback?code=XXXXXX&state=YYYY
```

#### Step 4: Exchange Code for Token
```bash
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code={code_from_step_3}
&client_id=gateway-client
&client_secret=gateway-secret
&redirect_uri=http://localhost:3000/callback
```

#### Step 5: Receive Tokens
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJSUzI1NiIs...",
  "expires_in": 3600,
  "scope": "openid profile email",
  "token_type": "Bearer"
}
```

---

## 2. JWT Token Structure

### Access Token Payload (Sample)
```json
{
  "iss": "http://localhost:9000",
  "sub": "user-id-123",
  "aud": "payment-service",
  "exp": 1704067200,
  "iat": 1704063600,
  "scope": "openid profile email",
  "client_id": "gateway-client"
}
```

**Fields:**
- `iss` (issuer): Auth Service URL
- `sub` (subject): User ID
- `aud` (audience): Target service
- `exp` (expiration): Token expiry timestamp
- `iat` (issued at): Token creation timestamp
- `scope`: Permissions granted
- `client_id`: OAuth2 client ID

### Token Lifespan
- **Access Token**: 1 hour (3600 seconds)
- **Refresh Token**: 30 days
- **Sliding Window**: Tokens auto-refresh on API Gateway

---

## 3. Refresh Token Mechanism

### Why Refresh Tokens?
- Access tokens are short-lived for security
- Refresh tokens allow obtaining new access tokens without re-login
- Supports seamless user experience

### Refresh Token Flow

```bash
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token={refresh_token_value}
&client_id=gateway-client
&client_secret=gateway-secret
```

### Response
```json
{
  "access_token": "new-eyJhbGciOiJSUzI1NiIs...",
  "refresh_token": "new-eyJhbGciOiJSUzI1NiIs...",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

### Auto-Refresh on API Gateway
The API Gateway automatically refreshes tokens when:
1. Token expires in next 5 minutes
2. Request includes `RefreshTokenFilter`
3. Valid refresh token exists in session

---

## 4. Using Tokens to Call APIs

### With Access Token (Bearer Token)
```bash
GET /api/payments/123
Authorization: Bearer {access_token}
```

### Example cURL Request
```bash
curl -X GET http://localhost:8080/api/payments/123 \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIs..." \
  -H "Content-Type: application/json"
```

### Example JavaScript/Fetch
```javascript
const response = await fetch('http://localhost:8080/api/payments/123', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});
```

### Example Java/RestTemplate
```java
@Service
public class PaymentClient {
    @Autowired
    private RestTemplate restTemplate;

    public Payment getPayment(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            "http://localhost:8080/api/payments/" + id,
            HttpMethod.GET,
            entity,
            Payment.class
        ).getBody();
    }
}
```

---

## 5. API Gateway Token Relay

### Configuration
The API Gateway automatically relays access tokens to downstream services using `TokenRelay` filter:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: payment-service
          uri: http://payment-service:8084
          predicates:
            - Path=/api/payments/**
          filters:
            - TokenRelay
```

### How It Works
1. Client sends request: `GET /api/payments/123` with `Authorization: Bearer {token}`
2. API Gateway receives request
3. `TokenRelay` filter extracts token from `Authorization` header
4. Gateway forwards request to Payment Service with same token
5. Payment Service validates token and processes request

---

## 6. Resource Server Configuration

### Payment Service Example
```java
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract authorities from JWT claims
            return jwt.getClaimAsStringList("scope")
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("http://auth-service:9000");
    }
}
```

### Key Components
1. **JwtDecoder**: Validates JWT signature using public key from Auth Service
2. **JwtAuthenticationConverter**: Maps JWT claims to Spring Security authorities
3. **SecurityFilterChain**: Defines security rules for protected endpoints

---

## 7. Token Validation

### JWT Validation Steps

```
1. Check Token Format
   └─> Must be JWT (3 parts separated by dots)

2. Verify Signature
   └─> Use Auth Service's public RSA key
   └─> Ensures token wasn't tampered with

3. Check Issuer
   └─> Must match configured issuer URL

4. Check Audience
   └─> Must include current service

5. Check Expiration
   └─> Current time < exp claim

6. Check Required Claims
   └─> sub, iss, iat, exp must exist
```

### Validation Errors & Responses

#### Invalid Token Format
```
HTTP/1.1 401 Unauthorized
{
  "error": "invalid_token",
  "error_description": "Token is malformed"
}
```

#### Expired Token
```
HTTP/1.1 401 Unauthorized
{
  "error": "invalid_token",
  "error_description": "Token has expired"
}
```

#### Invalid Signature
```
HTTP/1.1  401 Unauthorized
{
  "error": "invalid_token",
  "error_description": "Token signature verification failed"
}
```

---

## 8. Scopes & Permissions

### Defined Scopes
- `openid`: Basic user identification
- `profile`: User profile information (name, email, etc.)
- `email`: User email address
- `payments:read`: Read payment information
- `payments:write`: Create/modify payments
- `orders:read`: Read orders
- `orders:write`: Create/modify orders
- `products:read`: View products
- `admin`: Administrative access

### Scope Usage in Controllers
```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_payments:read')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        // Only accessible with payments:read scope
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_payments:write')")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request) {
        // Only accessible with payments:write scope
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        // Only accessible with admin scope
    }
}
```

---

## 9. Security Best Practices

### ✅ Do's
1. **Always use HTTPS** in production (not HTTP)
2. **Store refresh tokens securely** (HttpOnly cookies, encrypted storage)
3. **Use short-lived access tokens** (1-2 hours)
4. **Rotate keys regularly** (monthly recommended)
5. **Validate token signature** (always, on every request)
6. **Log security events** (failed validations, token expiry)
7. **Use CORS properly** (restrict origins)
8. **Implement rate limiting** (prevent token brute-force)

### ❌ Don'ts
1. **Never expose refresh tokens in URLs** (logs, browser history)
2. **Never hardcode secrets** in source code
3. **Never disable signature verification**
4. **Never use weak keys** (RSA 2048-bit minimum)
5. **Never store tokens in localStorage** (XSS vulnerable)
6. **Never send tokens in GET query parameters**
7. **Never log complete tokens**

---

## 10. Troubleshooting

### Issue: "401 Unauthorized"
**Cause**: Missing or invalid token

**Solution**:
```bash
# Check token format
echo $TOKEN  # Should be JWT (3 parts with dots)

# Verify token not expired
# Decode at jwt.io

# Ensure proper Authorization header
Authorization: Bearer {token}  # Not "Token" or without "Bearer"
```

### Issue: "403 Forbidden"
**Cause**: Token valid but insufficient scopes

**Solution**:
1. Check required scopes in controller
2. Request new token with proper scopes
3. Verify client credentials for scope assignment

### Issue: "Invalid Signature"
**Cause**: Auth Service public key mismatch

**Solution**:
```bash
# Clear JWT decoder cache
# Restart resource server
# Verify Auth Service is running on correct URL
```

### Issue: Token Expires Too Quickly
**Cause**: Short TTL configured

**Solution**:
1. Use refresh tokens for automatic renewal
2. Configure longer TTL (max 24 hours recommended)
3. Implement token refresh before expiry

---

## 11. API Documentation with Swagger

### Access Swagger UI
Each service exposes API documentation at:
```
http://localhost:{port}/swagger-ui.html
```

### Service URLs
- **Auth Service**: http://localhost:9000/swagger-ui.html
- **API Gateway**: http://localhost:8080/swagger-ui.html
- **Payment Service**: http://localhost:8084/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html
- **Order Service**: http://localhost:8083/swagger-ui.html

### Try It Out
1. Open Swagger UI
2. Click "Authorize" button
3. Enter access token from step 5 above
4. Execute API requests directly

---

## 12. Sample End-to-End Workflow

### 1. Login & Get Tokens
```bash
# Step 1: Get authorization code
curl -X GET "http://localhost:9000/oauth2/authorize?client_id=gateway-client&response_type=code&scope=openid%20profile&redirect_uri=http://localhost:8080/callback"
# Returns: code in redirect URL

# Step 2: Exchange code for tokens
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code={code}&client_id=gateway-client&client_secret=gateway-secret&redirect_uri=http://localhost:8080/callback"

# Response includes: access_token, refresh_token
```

### 2. Use Access Token
```bash
curl -X GET http://localhost:8080/api/payments \
  -H "Authorization: Bearer {access_token}"
```

### 3. Refresh Token (When Expired)
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&refresh_token={refresh_token}&client_id=gateway-client&client_secret=gateway-secret"

# Use new access_token in subsequent requests
```

---

## 13. Additional Resources

- **Spring Security OAuth2**: https://spring.io/projects/spring-security-oauth
- **JWT.io**: https://jwt.io (decode/verify tokens)
- **OpenID Connect**: https://openid.net/connect
- **OAuth 2.0 RFC**: https://tools.ietf.org/html/rfc6749

---

## Support

For security issues, contact: security@example.com
