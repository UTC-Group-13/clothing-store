# 🚀 Hướng Dẫn CI/CD - Clothing Store Project

## 📋 Tổng quan

Tôi đã triển khai hoàn chỉnh CI/CD pipeline cho project của bạn với các tính năng:

✅ **Tự động build & test** khi push code  
✅ **Build Docker image** và lưu vào GitHub Container Registry  
✅ **Tự động deploy** lên server qua SSH  
✅ **Health check** sau khi deploy  
✅ **Rollback** dễ dàng nếu có lỗi  

## 📁 Các File Đã Tạo

```
clothing-store/
├── .github/
│   └── workflows/
│       └── ci-cd.yml              # GitHub Actions workflow chính
├── ec/
│   ├── Dockerfile                 # Cấu hình Docker image
│   └── .dockerignore              # Loại trừ file không cần thiết
├── docker-compose.yml             # Chạy local với Docker
├── .env.example                   # Template biến môi trường
├── deploy.sh                      # Script deploy thủ công (Linux/Mac)
├── run-local.ps1                  # Script chạy local (Windows)
├── CI_CD_README.md                # Hướng dẫn chi tiết bằng English
├── DEPLOYMENT_GUIDE.md            # Hướng dẫn deployment đầy đủ
└── HUONG_DAN_NHANH.md            # File này (Tiếng Việt)
```

## 🎯 Cách Sử Dụng

### Bước 1: Cấu hình GitHub Secrets

1. Vào repository trên GitHub
2. Vào **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** và thêm các secret sau:

#### Thông tin Server
```
SERVER_HOST        → Địa chỉ IP server (VD: 160.30.113.40)
SERVER_USER        → Username SSH (VD: root hoặc ubuntu)
SERVER_PORT        → Port SSH (mặc định: 22)
SSH_PRIVATE_KEY    → Private key SSH (xem bước 2)
```

#### Thông tin Database
```
DB_URL             → jdbc:mysql://host:port/database?params
DB_USERNAME        → Username database
DB_PASSWORD        → Password database
```

#### Thông tin JWT
```
JWT_SECRET         → Secret key cho JWT (string 64 ký tự)
JWT_EXPIRATION     → Thời gian hết hạn (86400000 = 24 giờ)
```

### Bước 2: Tạo SSH Key

#### Trên Windows (PowerShell):
```powershell
# Tạo SSH key mới
ssh-keygen -t rsa -b 4096 -f $env:USERPROFILE\.ssh\github_actions

# Copy public key lên server
type $env:USERPROFILE\.ssh\github_actions.pub | ssh user@server "cat >> ~/.ssh/authorized_keys"

# Xem private key để paste vào GitHub Secret
type $env:USERPROFILE\.ssh\github_actions
```

#### Trên Linux/Mac:
```bash
# Tạo SSH key mới
ssh-keygen -t rsa -b 4096 -f ~/.ssh/github_actions

# Copy public key lên server
ssh-copy-id -i ~/.ssh/github_actions.pub user@server

# Xem private key để paste vào GitHub Secret
cat ~/.ssh/github_actions
```

**Lưu ý:** Copy toàn bộ nội dung private key (từ `-----BEGIN` đến `-----END`) vào GitHub Secret `SSH_PRIVATE_KEY`

### Bước 3: Chuẩn bị Server

SSH vào server và chạy các lệnh sau:

```bash
# 1. Cài đặt Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 2. Tạo thư mục lưu trữ
sudo mkdir -p /opt/clothing-store/uploads
sudo mkdir -p /opt/clothing-store/logs
sudo chown -R $USER:$USER /opt/clothing-store

# 3. Cài đặt MySQL (nếu chưa có)
docker run -d \
  --name mysql-db \
  --restart unless-stopped \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=ecommerce \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0

# 4. Kiểm tra
docker --version
docker ps
```

### Bước 4: Deploy

Đơn giản chỉ cần push code lên nhánh `main`:

```bash
git add .
git commit -m "feat: implement CI/CD"
git push origin main
```

## 🔄 Quy Trình CI/CD

Khi bạn push code lên GitHub, workflow sẽ tự động:

1. **Build & Test** (3-5 phút)
   - Compile Java code
   - Run unit tests
   - Tạo file JAR

2. **Build Docker Image** (2-3 phút)
   - Build Docker image với multi-stage
   - Push lên GitHub Container Registry
   - Tag với commit SHA và `latest`

3. **Deploy to Server** (1-2 phút)
   - SSH vào server
   - Pull Docker image mới nhất
   - Stop container cũ
   - Start container mới
   - Health check

**Tổng thời gian:** ~6-10 phút

## 📊 Theo Dõi Deployment

### Trên GitHub
1. Vào tab **Actions** của repository
2. Click vào workflow run mới nhất
3. Xem logs của từng job

### Trên Server
```bash
# Xem container đang chạy
docker ps

# Xem logs realtime
docker logs -f clothing-store-api

# Xem 100 dòng log cuối
docker logs clothing-store-api --tail 100

# Kiểm tra health
curl http://localhost:8080/actuator/health
```

## 🧪 Test Local

### Sử dụng Docker Compose (Recommended)
```bash
# Khởi động tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f app

# Dừng services
docker-compose down
```

### Sử dụng Script PowerShell (Windows)
```powershell
# Build và chạy
.\run-local.ps1

# Xem logs
docker logs -f clothing-store-api
```

## 🔧 Xử Lý Sự Cố

### Lỗi khi Build
```bash
# Test build locally
cd ec
mvn clean package

# Check Java version
java -version  # Phải là Java 21

# Clean và build lại
mvn clean install -U
```

### Lỗi khi Deploy
```bash
# Kiểm tra SSH connection
ssh -i ~/.ssh/github_actions user@server

# Kiểm tra Docker trên server
docker ps
docker images

# Xem logs container
docker logs clothing-store-api --tail 100
```

### Lỗi Database Connection
```bash
# Kiểm tra MySQL
docker exec -it mysql-db mysql -uroot -p

# Test connection từ app container
docker exec -it clothing-store-api sh
# Trong container:
nc -zv db_host 3307
```

## 🔙 Rollback

Nếu version mới có vấn đề, rollback về version cũ:

```bash
# SSH vào server
ssh user@server

# Xem list images
docker images | grep clothing-store-api

# Stop container hiện tại
docker stop clothing-store-api
docker rm clothing-store-api

# Chạy lại version cũ (thay YOUR_OLD_TAG)
docker run -d \
  --name clothing-store-api \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="..." \
  -e SPRING_DATASOURCE_USERNAME="..." \
  -e SPRING_DATASOURCE_PASSWORD="..." \
  -e JWT_SECRET="..." \
  -v /opt/clothing-store/uploads:/app/uploads \
  ghcr.io/YOUR_USERNAME/clothing-store-api:YOUR_OLD_TAG
```

## 📱 URLs Quan Trọng

Sau khi deploy thành công:

```
Application:    http://YOUR_SERVER_IP:8080
Swagger UI:     http://YOUR_SERVER_IP:8080/swagger-ui.html
Health Check:   http://YOUR_SERVER_IP:8080/actuator/health
API Docs:       http://YOUR_SERVER_IP:8080/v3/api-docs
```

## ✅ Checklist Trước Khi Deploy

- [ ] ✅ GitHub Secrets đã cấu hình đầy đủ
- [ ] ✅ SSH key đã tạo và add vào server
- [ ] ✅ Docker đã cài đặt trên server
- [ ] ✅ MySQL đang chạy và accessible
- [ ] ✅ Firewall đã mở port 8080, 3307, 22
- [ ] ✅ Thư mục `/opt/clothing-store` đã tạo
- [ ] ✅ Database đã import schema

## 🎓 Các Lệnh Hữu Ích

### Quản lý Container
```bash
# Start
docker start clothing-store-api

# Stop
docker stop clothing-store-api

# Restart
docker restart clothing-store-api

# Remove
docker rm -f clothing-store-api

# Stats
docker stats clothing-store-api
```

### Quản lý Images
```bash
# List images
docker images

# Remove image
docker rmi image_name:tag

# Clean up
docker system prune -a
```

### Xem Logs
```bash
# All logs
docker logs clothing-store-api

# Last 50 lines
docker logs clothing-store-api --tail 50

# Follow logs
docker logs -f clothing-store-api

# Since 1 hour
docker logs clothing-store-api --since 1h
```

## 🔐 Bảo Mật

- ✅ **Secrets**: Tất cả thông tin nhạy cảm đều lưu trong GitHub Secrets
- ✅ **SSH**: Sử dụng SSH key thay vì password
- ✅ **Docker**: Container chạy với non-root user
- ✅ **Registry**: Private container registry
- ✅ **Env vars**: Không hardcode trong code

## 📞 Hỗ Trợ

Nếu gặp vấn đề:

1. ✅ Kiểm tra logs trên GitHub Actions
2. ✅ Kiểm tra logs container: `docker logs clothing-store-api`
3. ✅ Verify GitHub Secrets đã đúng
4. ✅ Test SSH connection thủ công
5. ✅ Kiểm tra resources server: `docker stats`

## 🎯 Kết Luận

Bạn đã có một CI/CD pipeline hoàn chỉnh! 🎉

- ✅ **Tự động hóa**: Không cần deploy thủ công
- ✅ **An toàn**: Có health check và rollback
- ✅ **Nhanh chóng**: Deploy trong 6-10 phút
- ✅ **Dễ quản lý**: Logs và monitoring đầy đủ

**Chúc bạn deploy thành công! 🚀**

---

📖 **Tài liệu chi tiết:**
- [CI_CD_README.md](CI_CD_README.md) - English version
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Detailed guide

