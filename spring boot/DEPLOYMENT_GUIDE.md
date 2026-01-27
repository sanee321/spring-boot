# Deployment Guide - Docker & Kubernetes

## Table of Contents
1. [Local Development with Docker Compose](#docker-compose)
2. [Kubernetes Deployment](#kubernetes)
3. [Configuration Management](#configuration)
4. [Scaling & Performance](#scaling)
5. [Monitoring & Health Checks](#monitoring)
6. [Troubleshooting](#troubleshooting)

---

## Local Development with Docker Compose {#docker-compose}

### Prerequisites
- Docker Desktop installed (Windows/Mac) or Docker Engine (Linux)
- Docker Compose v2.0+
- At least 8GB RAM allocated to Docker
- 50GB disk space available

### Building Images

#### Build All Services
```bash
cd c:\GSAP\spring boot

# Build all images
docker-compose build

# Build with no cache
docker-compose build --no-cache

# Build specific service
docker-compose build payment-service
```

#### Build Individual Service
```bash
# Using Docker
cd payment-service
docker build -t payment-service:latest .

# With custom tag
docker build -t myregistry/payment-service:1.0.0 .
```

### Starting Services

#### Start All Services
```bash
docker-compose up -d
```

#### Start Specific Services
```bash
docker-compose up -d payment-service order-service
```

#### View Running Containers
```bash
docker-compose ps

# Expected output:
# NAME                 STATUS          PORTS
# payment-service      Up 2 minutes     8084:8084
# order-service        Up 2 minutes     8083:8083
# postgres             Up 5 minutes     5432:5432
```

#### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f payment-service

# Last 100 lines
docker-compose logs -f --tail=100
```

### Accessing Services

| Service | URL | Port |
|---------|-----|------|
| API Gateway | http://localhost:8080 | 8080 |
| Auth Service | http://localhost:9000 | 9000 |
| Eureka Dashboard | http://localhost:8761 | 8761 |
| User Service | http://localhost:8081 | 8081 |
| Product Service | http://localhost:8082 | 8082 |
| Order Service | http://localhost:8083 | 8083 |
| Payment Service | http://localhost:8084 | 8084 |
| Notification Service | http://localhost:8085 | 8085 |
| Inventory Service | http://localhost:8086 | 8086 |
| Review Service | http://localhost:8087 | 8087 |
| Analytics Service | http://localhost:8088 | 8088 |

### Stopping Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Stop specific service
docker-compose stop payment-service
```

### Docker Compose Health Check
```bash
# Check service health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP","components":{"discoveryClient":{"status":"UP"},...}}
```

---

## Kubernetes Deployment {#kubernetes}

### Prerequisites
- Kubernetes cluster (minikube, EKS, AKS, GKE, or local)
- kubectl CLI installed
- Helm (optional, for templating)
- Container registry (Docker Hub, ECR, ACR, GCR)

### Installation & Cluster Setup

#### For Minikube (Local Development)
```bash
# Install minikube
choco install minikube  # Windows
brew install minikube   # Mac
apt-get install minikube  # Linux

# Start minikube
minikube start --memory=8192 --cpus=4

# Enable ingress addon
minikube addons enable ingress
```

#### For EKS (AWS)
```bash
# Install eksctl
choco install eksctl  # Windows
brew install eksctl   # Mac

# Create cluster
eksctl create cluster --name ecommerce --region us-east-1 --nodes 3

# Get kubeconfig
aws eks update-kubeconfig --name ecommerce --region us-east-1
```

### Deploying to Kubernetes

#### Create Namespace
```bash
kubectl create namespace ecommerce

# Verify
kubectl get namespaces
```

#### Deploy All Services
```bash
cd k8s

# Apply all manifests
kubectl apply -f . -n ecommerce

# Verify deployments
kubectl get deployments -n ecommerce
kubectl get pods -n ecommerce
kubectl get services -n ecommerce
```

#### Deploy Specific Service
```bash
kubectl apply -f payment-service-deployment.yml -n ecommerce
kubectl apply -f payment-service-service.yml -n ecommerce
```

#### Monitor Deployment Progress
```bash
# Watch deployment
kubectl rollout status deployment/payment-service -n ecommerce

# View pod status
kubectl get pods -n ecommerce -w

# View detailed pod info
kubectl describe pod payment-service-xxx -n ecommerce
```

### Accessing Kubernetes Services

#### Port Forward (Local Testing)
```bash
# Forward API Gateway
kubectl port-forward svc/api-gateway 8080:8080 -n ecommerce

# Forward specific service
kubectl port-forward svc/payment-service 8084:8084 -n ecommerce

# Access via: http://localhost:8080
```

#### Ingress (Production)
```bash
# Get Ingress IP
kubectl get ingress -n ecommerce

# Expected URL:
# http://{INGRESS_IP}/api/payments
```

#### Service DNS (Pod-to-Pod Communication)
```bash
# From one pod to another
http://payment-service.ecommerce.svc.cluster.local:8084

# From same namespace
http://payment-service:8084
```

### Scaling Services
```bash
# Scale deployment
kubectl scale deployment payment-service --replicas=3 -n ecommerce

# Verify
kubectl get pods -n ecommerce | grep payment-service

# Horizontal Pod Autoscaling
kubectl autoscale deployment payment-service \
  --min=2 \
  --max=10 \
  --cpu-percent=80 \
  -n ecommerce
```

### Updating Deployments

#### Rolling Update
```bash
# Update image
kubectl set image deployment/payment-service \
  payment-service=payment-service:v2.0 \
  -n ecommerce

# Monitor rollout
kubectl rollout status deployment/payment-service -n ecommerce

# Rollback if needed
kubectl rollout undo deployment/payment-service -n ecommerce
```

### Deleting Resources

#### Delete Service
```bash
kubectl delete service payment-service -n ecommerce
```

#### Delete Deployment
```bash
kubectl delete deployment payment-service -n ecommerce
```

#### Delete Entire Namespace
```bash
kubectl delete namespace ecommerce
```

### Kubernetes Manifest Structure

#### Deployment Example
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  namespace: ecommerce
spec:
  replicas: 2
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
      - name: payment-service
        image: payment-service:latest
        ports:
        - containerPort: 8084
        env:
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: "http://eureka-server:8761/eureka"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8084
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8084
          initialDelaySeconds: 10
          periodSeconds: 5
```

#### Service Example
```yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: ecommerce
spec:
  type: ClusterIP
  selector:
    app: payment-service
  ports:
  - protocol: TCP
    port: 8084
    targetPort: 8084
```

#### ConfigMap Example
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-service-config
  namespace: ecommerce
data:
  application.yml: |
    spring:
      application:
        name: payment-service
      datasource:
        url: jdbc:postgresql://postgres:5432/ecommerce_db
    eureka:
      client:
        serviceUrl:
          defaultZone: http://eureka-server:8761/eureka
```

---

## Configuration Management {#configuration}

### Environment Variables

#### Docker Compose
```yaml
environment:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ecommerce_db
  - SPRING_DATASOURCE_USERNAME=ecommerce_user
  - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
```

#### Kubernetes ConfigMap
```bash
kubectl create configmap payment-service-config \
  --from-file=application.yml \
  -n ecommerce
```

#### Kubernetes Secrets
```bash
# Create database password secret
kubectl create secret generic db-credentials \
  --from-literal=username=ecommerce_user \
  --from-literal=password=secure_password \
  -n ecommerce

# Reference in deployment
env:
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: db-credentials
      key: username
```

### Configuration Profiles

#### Development
```properties
# application-dev.properties
spring.profiles.active=dev
logging.level.root=DEBUG
spring.jpa.show-sql=true
```

#### Production
```properties
# application-prod.properties
spring.profiles.active=prod
logging.level.root=WARN
spring.jpa.show-sql=false
management.endpoints.web.exposure.include=health
```

#### Testing
```properties
# application-test.properties
spring.profiles.active=test
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.root=INFO
```

---

## Scaling & Performance {#scaling}

### Horizontal Pod Autoscaling (HPA)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: payment-service-hpa
  namespace: ecommerce
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: payment-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Resource Requests & Limits
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

### Pod Disruption Budgets
```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: payment-service-pdb
  namespace: ecommerce
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: payment-service
```

---

## Monitoring & Health Checks {#monitoring}

### Liveness Probe
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8084
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8084
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 2
```

### Check Service Health
```bash
# Local
curl http://localhost:8084/actuator/health

# Kubernetes
kubectl exec -it payment-service-xxx -n ecommerce -- \
  curl localhost:8084/actuator/health
```

### View Metrics
```bash
# Prometheus metrics
curl http://localhost:8084/actuator/prometheus

# View application info
curl http://localhost:8084/actuator/info
```

---

## Troubleshooting {#troubleshooting}

### Service Not Starting
```bash
# Check logs
docker-compose logs payment-service

# Or Kubernetes
kubectl logs deployment/payment-service -n ecommerce --tail=100

# Check logs from crashed pod
kubectl logs payment-service-xxx -n ecommerce --previous
```

### Connection Issues Between Services
```bash
# Test DNS resolution
kubectl exec -it pod/payment-service-xxx -n ecommerce -- \
  nslookup order-service

# Test service connectivity
kubectl exec -it pod/payment-service-xxx -n ecommerce -- \
  curl http://order-service:8083/actuator/health
```

### Database Connection Problems
```bash
# Check database logs
docker-compose logs postgres

# Test database connection
kubectl exec -it postgres-0 -n ecommerce -- \
  psql -U ecommerce_user -d ecommerce_db -c "SELECT 1;"
```

### Pod Crash Loop
```bash
# View pod events
kubectl describe pod payment-service-xxx -n ecommerce

# Check resource limits
kubectl top pods -n ecommerce

# Increase memory if OOMKilled
kubectl set resources deployment payment-service \
  --limits=memory=1Gi \
  -n ecommerce
```

### Port Conflicts
```bash
# Find process using port
netstat -ano | findstr :8084  # Windows
lsof -i :8084  # Mac/Linux

# Kill process
taskkill /PID {PID} /F  # Windows
kill -9 {PID}  # Mac/Linux
```

### Eureka Registration Issues
```bash
# Check Eureka dashboard
curl http://localhost:8761/

# Verify service registration
curl http://localhost:8761/eureka/apps/payment-service

# Check service heartbeat
kubectl logs -f deployment/payment-service -n ecommerce | grep "Registering"
```

---

## Common Commands Reference

### Docker Compose
```bash
docker-compose up -d                  # Start all services
docker-compose down                   # Stop all services
docker-compose ps                     # View running services
docker-compose logs -f                # Follow logs
docker-compose build --no-cache       # Rebuild images
docker-compose pull                   # Pull latest images
```

### Kubernetes
```bash
kubectl apply -f k8s/                 # Deploy all manifests
kubectl get pods -n ecommerce         # List pods
kubectl describe pod {pod} -n ecommerce # Pod details
kubectl logs -f pod/{pod} -n ecommerce  # Stream logs
kubectl delete namespace ecommerce    # Delete namespace
kubectl scale deployment {name} --replicas=3  # Scale
kubectl port-forward svc/{service} 8080:8080  # Port forward
```

---

## Monitoring Setup (Optional)

### Prometheus Stack
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring
```

### ELK Stack (Elasticsearch, Logstash, Kibana)
```bash
docker run -d --name elasticsearch docker.elastic.co/elasticsearch/elasticsearch:8.0.0 \
  -e discovery.type=single-node
docker run -d --name kibana -p 5601:5601 \
  docker.elastic.co/kibana/kibana:8.0.0
```

---

**Last Updated**: January 27, 2026  
**Maintained By**: DevOps Team
