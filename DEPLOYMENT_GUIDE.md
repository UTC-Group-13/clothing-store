# CI/CD Deployment Guide for Clothing Store Project

## 📋 Tổng quan

CI/CD pipeline này tự động hóa quy trình build, test và deploy ứng dụng Spring Boot lên server khi có push lên nhánh `main`.

## 🏗️ Kiến trúc CI/CD

### Workflow gồm 3 jobs:

1. **Build & Test**: Compile code, chạy tests, tạo JAR file
2. **Docker Build**: Build Docker image và push lên GitHub Container Registry
3. **Deploy**: Deploy container lên server production

## 🚀 Cài đặt

### 1. Cấu hình GitHub Secrets

Truy cập `Settings` → `Secrets and variables` → `Actions` và thêm các secrets sau:

#### Server Configuration
- `SERVER_HOST`: Địa chỉ IP hoặc domain của server (VD: `160.30.113.40`)
- `SERVER_USER`: Username SSH để kết nối (VD: `root` hoặc `ubuntu`)
- `SERVER_PORT`: Port SSH (mặc định: `22`)
- `SSH_PRIVATE_KEY`: Private SSH key để kết nối server

#### Database Configuration
- `DB_URL`: JDBC URL kết nối database (VD: `jdbc:mysql://localhost:3306/ecommerce?useSSL=false`)
- `DB_USERNAME`: Username database
- `DB_PASSWORD`: Password database

#### JWT Configuration
- `JWT_SECRET`: Secret key cho JWT token (string 64 ký tự)
- `JWT_EXPIRATION`: Thời gian hết hạn token (ms, VD: `86400000` = 24h)

### 2. Tạo SSH Key

```bash
# Trên máy local, tạo SSH key pair
ssh-keygen -t rsa -b 4096 -C "github-actions" -f ~/.ssh/github_actions

# Copy public key lên server
ssh-copy-id -i ~/.ssh/github_actions.pub user@server_ip

# Copy private key và paste vào GitHub Secret SSH_PRIVATE_KEY
cat ~/.ssh/github_actions
```

### 3. Chuẩn bị Server

#### Cài đặt Docker trên server:

```bash
# SSH vào server
ssh user@server_ip

# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Thêm user vào docker group
sudo usermod -aG docker $USER

# Khởi động Docker
sudo systemctl enable docker
sudo systemctl start docker

# Kiểm tra
docker --version
```

#### Tạo thư mục lưu trữ:

```bash
# Tạo thư mục cho uploads và logs
sudo mkdir -p /opt/clothing-store/uploads
sudo mkdir -p /opt/clothing-store/logs

# Phân quyền
sudo chown -R $USER:$USER /opt/clothing-store
```

### 4. Cấu hình Database trên Server

```bash
# Chạy MySQL container
docker run -d \
  --name mysql-db \
  --restart unless-stopped \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=ecommerce \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0

# Import database schema nếu cần
# docker exec -i mysql-db mysql -uroot -p ecommerce < init.sql
```

## 📝 Cách sử dụng

### Trigger CI/CD Pipeline

Pipeline tự động chạy khi:

```bash
# Push code lên nhánh main
git add .
git commit -m "feat: new feature"
git push origin main
```

### Pull Request

Pipeline cũng chạy test khi tạo PR:

```bash
git checkout -b feature/new-feature
git add .
git commit -m "feat: implement new feature"
git push origin feature/new-feature
# Tạo PR trên GitHub
```

## 🔍 Theo dõi Deployment

### 1. Xem logs trên GitHub Actions

- Truy cập tab `Actions` trên GitHub repository
- Click vào workflow run mới nhất
- Xem từng job để debug nếu có lỗi

### 2. Kiểm tra trên Server

```bash
# SSH vào server
ssh user@server_ip

# Xem containers đang chạy
docker ps

# Xem logs của container
docker logs clothing-store-api

# Xem logs realtime
docker logs -f clothing-store-api

# Kiểm tra health của container
docker inspect clothing-store-api | grep -A 10 Health

# Kiểm tra resource usage
docker stats clothing-store-api
```

### 3. Test API

```bash
# Health check
curl http://your-server:8080/actuator/health

# API test
curl http://your-server:8080/api/products

# Swagger UI
# Mở browser: http://your-server:8080/swagger-ui.html
```

## 🛠️ Debug và Troubleshooting

### Container không khởi động

```bash
# Xem logs chi tiết
docker logs clothing-store-api --tail 100

# Kiểm tra container status
docker inspect clothing-store-api

# Vào trong container để debug
docker exec -it clothing-store-api sh
```

### Database connection issues

```bash
# Test kết nối MySQL từ app container
docker exec -it clothing-store-api sh
# Trong container:
wget --spider http://mysql-db:3306

# Kiểm tra MySQL từ server
docker exec -it mysql-db mysql -uroot -p -e "SHOW DATABASES;"
```

### Disk space issues

```bash
# Xóa unused images và containers
docker system prune -a

# Xóa volumes không dùng
docker volume prune

# Kiểm tra disk usage
df -h
docker system df
```

## 🔐 Security Best Practices

1. **Không commit secrets**: Luôn sử dụng GitHub Secrets
2. **SSH Key rotation**: Thay đổi SSH key định kỳ
3. **Minimal privileges**: Chỉ cấp quyền cần thiết cho user SSH
4. **Firewall**: Chỉ mở port cần thiết (8080, 3307, 22)
5. **HTTPS**: Sử dụng reverse proxy (Nginx) với SSL

## 📊 Monitoring (Optional)

### Thêm Health Check Endpoint

Application đã có Spring Boot Actuator, thêm vào `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Cấu hình trong `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## 🔄 Rollback

Nếu deployment có vấn đề, rollback về version trước:

```bash
# SSH vào server
ssh user@server_ip

# Xem list images
docker images | grep clothing-store-api

# Stop container hiện tại
docker stop clothing-store-api
docker rm clothing-store-api

# Chạy version cũ
docker run -d \
  --name clothing-store-api \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="..." \
  # ... other env vars
  ghcr.io/your-username/clothing-store-api:main-abc123
```

## 📚 Tài liệu tham khảo

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)

## 🆘 Support

Nếu gặp vấn đề:
1. Kiểm tra logs trên GitHub Actions
2. Kiểm tra logs container trên server
3. Review GitHub Secrets configuration
4. Kiểm tra network và firewall settings

