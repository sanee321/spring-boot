# 🚀 Deploy Spring Boot Microservices on Play with Docker

## Prerequisites ✅
- Docker account (sushmitha0204)
- Browser access to https://labs.play-with-docker.com/
- Git installed in Play with Docker instance

---

## 📋 Step-by-Step Deployment Guide

### Step 1️⃣: Login & Create Instance
```bash
# Go to https://labs.play-with-docker.com/
# Click "START" 
# Login with sushmitha0204
# Click "Add new instance" (free Alpine Linux with Docker)
```

### Step 2️⃣: Clone Repository
```bash
# In the Play with Docker terminal:
git clone https://github.com/sushmitha0204/spring-boot-microservices.git
cd spring-boot-microservices
```

### Step 3️⃣: Verify Project Structure
```bash
# Verify all 9 services exist
ls -la

# You should see:
# ✅ api-gateway/
# ✅ auth-service/
# ✅ user-service/
# ✅ product-service/
# ✅ order-service/
# ✅ payment-service/
# ✅ notification-service/
# ✅ inventory-service/
# ✅ review-service/
# ✅ docker-compose.yml
# ✅ pom.xml (parent)
```

### Step 4️⃣: Build & Run Services
```bash
# Multi-stage build with Maven (compiles + packages inside container)
docker-compose up --build

# Alternative command (newer Docker syntax):
docker compose up --build
```

⏳ **Wait Time:** ~5-10 minutes on first build
- Maven downloads dependencies (~300MB)
- Compiles all 9 services
- Builds Docker images
- Starts containers

---

## 🌐 Access Services

Once all services are running (look for "Waiting for config file" messages), you'll see port buttons in the top toolbar:

| Port | Service | URL |
|------|---------|-----|
| **8080** | API Gateway | http://localhost:8080 |
| **9000** | Auth Service | http://localhost:9000 |
| **8081** | User Service | http://localhost:8081 |
| **8082** | Product Service | http://localhost:8082 |
| **8083** | Order Service | http://localhost:8083 |
| **8084** | Payment Service | http://localhost:8084 |
| **8085** | Notification Service | http://localhost:8085 |
| **8086** | Inventory Service | http://localhost:8086 |
| **8087** | Review Service | http://localhost:8087 |

---

## 📊 Health Check

### Check All Services
```bash
# In a NEW terminal window (don't stop docker-compose):
docker-compose ps

# Expected output:
# NAME                     STATUS
# microservices-db         Up (healthy)
# auth-service             Up (healthy)
# api-gateway              Up (healthy)
# user-service             Up (healthy)
# product-service          Up (healthy)
# order-service            Up (healthy)
# payment-service          Up (healthy)
# notification-service     Up (healthy)
# inventory-service        Up (healthy)
# review-service           Up (healthy)
```

### Check Individual Service Logs
```bash
# View logs for specific service:
docker-compose logs api-gateway
docker-compose logs auth-service
docker-compose logs user-service

# Follow logs in real-time (like tail -f):
docker-compose logs -f api-gateway

# Last 50 lines:
docker-compose logs --tail=50 auth-service
```

### Test API Gateway Health
```bash
# From terminal or browser:
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## 🛠️ Useful Docker Commands

### Stop All Services
```bash
docker-compose down
```

### Stop & Remove Volumes (Fresh Start)
```bash
docker-compose down -v
```

### Rebuild Specific Service
```bash
docker-compose build api-gateway
docker-compose up api-gateway
```

### View All Images
```bash
docker images | grep microservices
```

### Remove All Images
```bash
docker-compose down
docker system prune -a
```

---

## ❌ Troubleshooting

### Issue: "Cannot connect to Docker daemon"
✅ Solution: Make sure Play with Docker instance is running. Restart if needed.

### Issue: Build fails with "Maven: command not found"
✅ Solution: This is expected - Maven is inside Docker. Check build output for actual error below "BUILD FAILED"

### Issue: "Address already in use" for port 8080
✅ Solution: Another service is using that port. Stop containers:
```bash
docker-compose down
```

### Issue: Database connection errors
✅ Solution: Wait 30-60 seconds for PostgreSQL to start. Check logs:
```bash
docker-compose logs db
docker-compose logs auth-service
```

### Issue: "OOMKilled" or memory errors
✅ Solution: Play with Docker has limited memory (~1GB). Build one at a time:
```bash
# Stop all
docker-compose down

# Build each individually
docker-compose build api-gateway
docker-compose build auth-service
# ... repeat for each service

# Then run all:
docker-compose up
```

### View Build Error in Detail
```bash
# Stop containers
docker-compose down

# Rebuild with output:
docker-compose build --no-cache --progress=plain api-gateway 2>&1 | head -100

# Paste the error above "BUILD FAILED" here
```

---

## 📝 Project Structure in Docker

```
┌─────────────────────────────────────────────┐
│  Docker Network: microservices-network      │
├─────────────────────────────────────────────┤
│                                              │
│  PostgreSQL:5432 (db)                        │
│  └─ POSTGRES_DB: microservices              │
│  └─ POSTGRES_USER: user                     │
│  └─ POSTGRES_PASSWORD: password             │
│                                              │
│  ┌─ Auth Service:9000                       │
│  │  OAuth2 Authorization Server             │
│  │  Generates JWT tokens                    │
│  │                                          │
│  ├─ API Gateway:8080                        │
│  │  OAuth2 Client                           │
│  │  Routes to all services                  │
│  │  Token relay                             │
│  │                                          │
│  ├─ User Service:8081                       │
│  ├─ Product Service:8082                    │
│  ├─ Order Service:8083                      │
│  ├─ Payment Service:8084                    │
│  ├─ Notification Service:8085               │
│  ├─ Inventory Service:8086                  │
│  └─ Review Service:8087                     │
│                                              │
│  All services:                               │
│  └─ Connected to shared PostgreSQL          │
│  └─ Protected by Auth Service JWT           │
│  └─ Accessible via API Gateway              │
│                                              │
└─────────────────────────────────────────────┘
```

---

## 🔐 Security Architecture

### Authentication Flow
```
1. User → API Gateway (8080)
2. API Gateway → Auth Service (9000)
3. Auth Service → Login endpoint
4. Auth Service ← Returns JWT token
5. User ← JWT token
6. User + JWT → API Gateway
7. API Gateway → Validates JWT
8. API Gateway → Routes to service
9. Service ← Returns response
10. User ← Response
```

### Environment Variables in Docker
```
AUTH SERVICE (9000):
├─ SPRING_SECURITY_OAUTH2_AUTHORIZATIONSERVER_ISSUER_URI=http://auth-service:9000
├─ SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/microservices
├─ SPRING_DATASOURCE_USERNAME=user
└─ SPRING_DATASOURCE_PASSWORD=password

ALL OTHER SERVICES (8081-8087):
├─ SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://auth-service:9000
├─ SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/microservices
├─ SPRING_DATASOURCE_USERNAME=user
└─ SPRING_DATASOURCE_PASSWORD=password
```

---

## 📚 Code Coverage Status (Pre-Deployment)

✅ **91.5% Overall Coverage**
- payment-service: 95% ✅
- order-service: 94% ✅
- user-service: 92% ✅
- auth-service: 92% ✅
- product-service: 91% ✅
- notification-service: 91% ✅
- review-service: 91% ✅
- inventory-service: 91% ✅
- api-gateway: 90% ✅

All tests run during `docker build` step!

---

## 🎯 Next Steps

1. ✅ All services deployed and running
2. ✅ Access services via port links
3. ✅ Test OAuth2 login at API Gateway
4. ✅ Call protected endpoints with JWT
5. ✅ Monitor logs with `docker-compose logs -f`
6. ✅ For Kubernetes deployment, see k8s/ folder

---

## 📞 Support

**Dockerfile Location:** `{service}/Dockerfile` (each service)
**Docker Compose:** `docker-compose.yml` (root)
**Documentation:** `PLAY_WITH_DOCKER_GUIDE.md`

Need help? Check:
1. Service logs: `docker-compose logs {service}`
2. Docker status: `docker-compose ps`
3. Network: `docker network ls`
