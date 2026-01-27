# Play with Docker - Spring Boot Microservices Setup

## Quick Start for Play with Docker

### Step 1: Login to Play with Docker
1. Go to: https://labs.play-with-docker.com/
2. Login with your Docker account (sushmitha0204)
3. Click **"Start"**

### Step 2: Create a New Instance
- Click **"Add new instance"** (creates free Alpine Linux container with Docker pre-installed)
- Wait for instance to start

### Step 3: Clone and Deploy
Copy and paste this in the browser terminal:

```bash
# Clone the repository
git clone https://github.com/your-username/spring-boot-microservices.git
cd spring-boot-microservices

# Build and run all services
docker-compose up --build
```

**Note:** Replace `https://github.com/your-username/spring-boot-microservices.git` with your actual repository URL

### Step 4: Access Services
Play with Docker will show clickable port links in the top toolbar:
- **8080** - API Gateway
- **9000** - Auth Service
- **8081** - User Service
- **8082** - Product Service
- **8083** - Order Service
- **8084** - Payment Service
- **8085** - Notification Service
- **8086** - Inventory Service
- **8087** - Review Service
- **5432** - PostgreSQL (internal only)

---

## Alternative: Using Production Compose File

For optimized configuration with health checks:

```bash
docker-compose -f docker-compose.prod.yml up --build
```

---

## Docker Commands in Play with Docker

### View running containers
```bash
docker ps
```

### View service logs
```bash
docker logs <service-name>
# Example:
docker logs api-gateway
docker logs auth-service
```

### Stop all services
```bash
docker-compose down
```

### Remove all data (fresh start)
```bash
docker-compose down -v
```

### Check service health
```bash
docker-compose ps
```

---

## Troubleshooting

### Services failing to start
- Check logs: `docker-compose logs <service-name>`
- Ensure database is healthy first
- Wait for dependencies (auth-service, db) to be ready

### Port conflicts
- If port already in use, modify docker-compose.yml
- Change `ports: ["8080:8080"]` to `ports: ["8080:8080"]`

### Database connection issues
- Wait for PostgreSQL to start (health check takes ~30s)
- Check database credentials match services

### Memory/Resource limits
- Play with Docker has limited resources
- May need to build one service at a time:
```bash
docker-compose build auth-service
docker-compose up auth-service
```

---

## Service Architecture

```
┌─────────────────────────────────────────┐
│        API Gateway (8080)               │
│     OAuth2 Client, Route Config          │
└────────────────┬────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼────────┐      ┌────────▼───┐
│Auth Service│      │All Business │
│  (9000)    │      │  Services   │
│ OAuth2 Srv │      │   (8081+)   │
└────────────┘      └─────┬──────┘
                          │
                    ┌─────▼──────┐
                    │ PostgreSQL  │
                    │  (5432)     │
                    └─────────────┘
```

---

## Environment Variables

All services configured with:
- **Database:** PostgreSQL on port 5432
- **Auth Server:** http://auth-service:9000
- **Network:** microservices-network (internal Docker network)

Custom environment variables can be added in docker-compose file for each service.

---

## Next Steps

1. Services will take ~1-2 minutes to start
2. Check logs: `docker-compose logs -f`
3. Test API Gateway health: Click 8080 port link
4. Login via OAuth2 at http://api-gateway:8080/login
5. Call protected endpoints with JWT token

---

## Build Locally Without Play with Docker

If you have Docker installed locally:

```powershell
# Windows PowerShell
cd C:\Users\g.sai.sushmitha\spring boot\spring boot
docker-compose up --build

# Linux/Mac
cd ~/spring boot
docker-compose up --build
```

---

## Production Deployment

For production on cloud (AWS, Azure, GCP):

1. Push images to Docker Hub:
```bash
docker login -u sushmitha0204
docker build -t sushmitha0204/api-gateway ./api-gateway
docker push sushmitha0204/api-gateway
# Repeat for all services
```

2. Use Kubernetes manifests:
```bash
kubectl apply -f k8s/
```

3. Configure cloud ingress and load balancing

---

**For support:** Check README.md or deployment guides in project root
