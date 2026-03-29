#!/bin/bash

# Script để deploy thủ công lên server (backup cho CI/CD)

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
SERVER_USER="${SERVER_USER:-root}"
SERVER_HOST="${SERVER_HOST:-160.30.113.40}"
SERVER_PORT="${SERVER_PORT:-22}"
CONTAINER_NAME="clothing-store-api"
IMAGE_NAME="clothing-store-api:latest"

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}  Manual Deployment Script${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# Step 1: Build JAR
echo -e "${YELLOW}[1/5] Building application...${NC}"
cd ec
mvn clean package -DskipTests -B
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi
cd ..
echo -e "${GREEN}✓ Build successful${NC}"
echo ""

# Step 2: Build Docker Image
echo -e "${YELLOW}[2/5] Building Docker image...${NC}"
docker build -t ${IMAGE_NAME} ./ec
if [ $? -ne 0 ]; then
    echo -e "${RED}Docker build failed!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker image built${NC}"
echo ""

# Step 3: Save and transfer image
echo -e "${YELLOW}[3/5] Saving Docker image...${NC}"
docker save ${IMAGE_NAME} | gzip > clothing-store-api.tar.gz
echo -e "${GREEN}✓ Image saved${NC}"
echo ""

echo -e "${YELLOW}[4/5] Transferring image to server...${NC}"
scp -P ${SERVER_PORT} clothing-store-api.tar.gz ${SERVER_USER}@${SERVER_HOST}:/tmp/
if [ $? -ne 0 ]; then
    echo -e "${RED}Transfer failed!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Image transferred${NC}"
echo ""

# Step 4: Deploy on server
echo -e "${YELLOW}[5/5] Deploying on server...${NC}"
ssh -p ${SERVER_PORT} ${SERVER_USER}@${SERVER_HOST} << 'ENDSSH'
    set -e

    # Load image
    echo "Loading Docker image..."
    docker load < /tmp/clothing-store-api.tar.gz

    # Stop old container
    echo "Stopping old container..."
    docker stop clothing-store-api || true
    docker rm clothing-store-api || true

    # Start new container
    echo "Starting new container..."
    docker run -d \
        --name clothing-store-api \
        --restart unless-stopped \
        -p 8080:8080 \
        -e SPRING_DATASOURCE_URL="jdbc:mysql://160.30.113.40:3307/clothing_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
        -e SPRING_DATASOURCE_USERNAME="root" \
        -e SPRING_DATASOURCE_PASSWORD="123456" \
        -e JWT_SECRET="3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b" \
        -e JWT_EXPIRATION="86400000" \
        -e FILE_UPLOAD_DIR="/app/uploads/images" \
        -v /opt/clothing-store/uploads:/app/uploads \
        -v /opt/clothing-store/logs:/app/logs \
        clothing-store-api:latest

    # Cleanup
    rm /tmp/clothing-store-api.tar.gz
    docker image prune -f

    echo "Deployment completed!"
    docker ps | grep clothing-store-api
ENDSSH

if [ $? -ne 0 ]; then
    echo -e "${RED}Deployment failed!${NC}"
    exit 1
fi

# Cleanup local
rm clothing-store-api.tar.gz

echo -e "${GREEN}✓ Deployment successful!${NC}"
echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}  Deployment completed!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Application URL: http://${SERVER_HOST}:8080"
echo "Swagger UI: http://${SERVER_HOST}:8080/swagger-ui.html"
echo "Health Check: http://${SERVER_HOST}:8080/actuator/health"
