# Script để build và chạy local với Docker

Write-Host "================================" -ForegroundColor Green
Write-Host "  Local Docker Build & Run" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""

# Step 1: Build JAR
Write-Host "[1/3] Building application..." -ForegroundColor Yellow
Set-Location ec
mvn clean package -DskipTests -B
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}
Set-Location ..
Write-Host "✓ Build successful" -ForegroundColor Green
Write-Host ""

# Step 2: Build Docker Image
Write-Host "[2/3] Building Docker image..." -ForegroundColor Yellow
docker build -t clothing-store-api:latest ./ec
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Docker image built" -ForegroundColor Green
Write-Host ""

# Step 3: Run Container
Write-Host "[3/3] Starting container..." -ForegroundColor Yellow

# Stop old container if exists
docker stop clothing-store-api 2>$null
docker rm clothing-store-api 2>$null

# Run new container
docker run -d `
    --name clothing-store-api `
    --restart unless-stopped `
    -p 8080:8080 `
    -e SPRING_DATASOURCE_URL="jdbc:mysql://160.30.113.40:3307/ecommerce?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" `
    -e SPRING_DATASOURCE_USERNAME="root" `
    -e SPRING_DATASOURCE_PASSWORD="123456" `
    -e JWT_SECRET="3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b" `
    -e JWT_EXPIRATION="86400000" `
    -e FILE_UPLOAD_DIR="/app/uploads/images" `
    -v ${PWD}/uploads:/app/uploads `
    clothing-store-api:latest

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start container!" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Container started" -ForegroundColor Green
Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "  Application is running!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Application URL: http://localhost:8080"
Write-Host "Swagger UI: http://localhost:8080/swagger-ui.html"
Write-Host "Health Check: http://localhost:8080/actuator/health"
Write-Host ""
Write-Host "View logs: docker logs -f clothing-store-api"
Write-Host "Stop container: docker stop clothing-store-api"

