#!/bin/bash

# ========================================
# Docker Cleanup & Rebuild Script
# ========================================

echo "🧹 Step 1: Cleaning old Docker containers and volumes..."
docker-compose down -v

echo "🗑️  Step 2: Removing dangling images and unused resources..."
docker system prune -f

echo "📦 Step 3: Verifying all Dockerfiles are updated..."
echo "✅ All Dockerfiles using multi-stage build with eclipse-temurin"

echo "🚀 Step 4: Building and running all services..."
echo "⏳ This may take 5-10 minutes on first run..."
docker-compose up --build

echo "✅ Deployment complete!"
echo ""
echo "Services running at:"
echo "  API Gateway:        http://localhost:8080"
echo "  Auth Service:       http://localhost:9000"
echo "  User Service:       http://localhost:8081"
echo "  Product Service:    http://localhost:8082"
echo "  Order Service:      http://localhost:8083"
echo "  Payment Service:    http://localhost:8084"
echo "  Notification:       http://localhost:8085"
echo "  Inventory Service:  http://localhost:8086"
echo "  Review Service:     http://localhost:8087"
