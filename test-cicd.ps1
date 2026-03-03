# Test CI/CD Locally Script

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Testing CI/CD Workflow" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"

# Test 1: Check Java
Write-Host "Test 1: Checking Java version..." -ForegroundColor Yellow
java -version 2>&1 | Select-String "version"
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Java is installed" -ForegroundColor Green
} else {
    Write-Host "✗ Java not found! Install Java 21" -ForegroundColor Red
}
Write-Host ""

# Test 2: Check Maven
Write-Host "Test 2: Checking Maven..." -ForegroundColor Yellow
Set-Location ec
mvn -version | Select-String "Apache Maven"
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Maven is installed" -ForegroundColor Green
} else {
    Write-Host "✗ Maven not found! Install Maven" -ForegroundColor Red
}
Set-Location ..
Write-Host ""

# Test 3: Check Docker
Write-Host "Test 3: Checking Docker..." -ForegroundColor Yellow
docker --version
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker is installed" -ForegroundColor Green
} else {
    Write-Host "✗ Docker not found! Install Docker" -ForegroundColor Red
}
Write-Host ""

# Test 4: Maven Build
Write-Host "Test 4: Testing Maven build..." -ForegroundColor Yellow
Set-Location ec
mvn clean package -DskipTests -B -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Maven build successful" -ForegroundColor Green
    Write-Host "  JAR file created at: ec/target/*.jar" -ForegroundColor Gray
} else {
    Write-Host "✗ Maven build failed!" -ForegroundColor Red
}
Set-Location ..
Write-Host ""

# Test 5: Docker Build
Write-Host "Test 5: Testing Docker build..." -ForegroundColor Yellow
docker build -t clothing-store-api:test ./ec -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker build successful" -ForegroundColor Green
} else {
    Write-Host "✗ Docker build failed!" -ForegroundColor Red
}
Write-Host ""

# Test 6: Check GitHub Actions Workflow
Write-Host "Test 6: Validating GitHub Actions workflow..." -ForegroundColor Yellow
if (Test-Path ".github/workflows/ci-cd.yml") {
    Write-Host "✓ Workflow file exists" -ForegroundColor Green

    # Validate YAML syntax
    try {
        $content = Get-Content ".github/workflows/ci-cd.yml" -Raw
        if ($content -match "name:" -and $content -match "jobs:") {
            Write-Host "✓ Workflow syntax looks valid" -ForegroundColor Green
        } else {
            Write-Host "⚠ Workflow syntax might have issues" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "✗ Cannot read workflow file" -ForegroundColor Red
    }
} else {
    Write-Host "✗ Workflow file not found!" -ForegroundColor Red
}
Write-Host ""

# Test 7: Check Required Files
Write-Host "Test 7: Checking required files..." -ForegroundColor Yellow
$requiredFiles = @(
    "ec/Dockerfile",
    "ec/pom.xml",
    "docker-compose.yml",
    ".env.example",
    ".github/workflows/ci-cd.yml"
)

$allFilesExist = $true
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file not found!" -ForegroundColor Red
        $allFilesExist = $false
    }
}
Write-Host ""

# Test 8: Check Secrets Configuration (Manual)
Write-Host "Test 8: GitHub Secrets Checklist..." -ForegroundColor Yellow
Write-Host "  Please verify these secrets are set in GitHub:" -ForegroundColor Gray
Write-Host "  [ ] SERVER_HOST" -ForegroundColor Gray
Write-Host "  [ ] SERVER_USER" -ForegroundColor Gray
Write-Host "  [ ] SSH_PRIVATE_KEY" -ForegroundColor Gray
Write-Host "  [ ] DB_URL" -ForegroundColor Gray
Write-Host "  [ ] DB_USERNAME" -ForegroundColor Gray
Write-Host "  [ ] DB_PASSWORD" -ForegroundColor Gray
Write-Host "  [ ] JWT_SECRET" -ForegroundColor Gray
Write-Host ""

# Summary
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "All tests completed!" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Configure GitHub Secrets (see HUONG_DAN_NHANH.md)" -ForegroundColor White
Write-Host "2. Generate and add SSH key to server" -ForegroundColor White
Write-Host "3. Push code to GitHub main branch" -ForegroundColor White
Write-Host "4. Monitor deployment in GitHub Actions tab" -ForegroundColor White
Write-Host ""
Write-Host "Documentation:" -ForegroundColor Yellow
Write-Host "- HUONG_DAN_NHANH.md (Tiếng Việt)" -ForegroundColor White
Write-Host "- CI_CD_README.md (English)" -ForegroundColor White
Write-Host "- DEPLOYMENT_GUIDE.md (Detailed)" -ForegroundColor White
Write-Host ""

# Cleanup test image
Write-Host "Cleaning up test image..." -ForegroundColor Gray
docker rmi clothing-store-api:test -f 2>$null
Write-Host "Done!" -ForegroundColor Green

