# 📦 Clothing Store API — Tóm Tắt Dự Án

> **Cập nhật:** March 2026  
> **Trạng thái:** Đang phát triển (v0.0.1-SNAPSHOT)

---

## 📋 Tổng Quan

| Thuộc tính | Giá trị |
|---|---|
| **Tên project** | Clothing Store API (`ec`) |
| **Group ID** | `com.utc` |
| **Artifact ID** | `ec` |
| **Version** | `0.0.1-SNAPSHOT` |
| **Ngôn ngữ** | Java 21 |
| **Framework** | Spring Boot 3.5.11 |
| **Database** | MySQL 8.0 |
| **Port** | `8080` |
| **Base package** | `com.utc.ec` |

---

## 🏗️ Kiến Trúc

```
clothing-store/
├── .github/workflows/       # CI/CD GitHub Actions
├── ec/                      # Spring Boot Application
│   ├── src/main/java/com/utc/ec/
│   │   ├── config/          # Cấu hình (Security, Swagger, WebMVC, Exception Handler)
│   │   │   └── security/    # JWT Filter, Security Config, UserDetailsService
│   │   ├── controller/      # REST Controllers (8 controllers)
│   │   ├── dto/             # Data Transfer Objects
│   │   │   └── auth/        # DTOs cho Authentication
│   │   ├── entity/          # JPA Entities (23 entities)
│   │   ├── exception/       # Custom Exceptions
│   │   ├── mapper/          # MapStruct Mappers
│   │   ├── repository/      # Spring Data JPA Repositories
│   │   └── service/         # Business Logic
│   │       └── impl/        # Service Implementations
│   ├── src/main/resources/
│   │   ├── application.yml  # Cấu hình ứng dụng
│   │   ├── messages.properties  # I18n messages
│   │   └── init_sql/init.sql    # Script khởi tạo database
│   └── Dockerfile           # Multi-stage Docker build
├── docker-compose.yml       # Local development stack
├── .env.example             # Mẫu biến môi trường
└── deploy.sh                # Script deploy thủ công
```

---

## 🛠️ Công Nghệ Sử Dụng

### Backend
| Thư viện | Version | Mục đích |
|---|---|---|
| Spring Boot | 3.5.11 | Framework chính |
| Spring Security | 6.5.8 | Bảo mật & Authentication |
| Spring Data JPA | (Boot managed) | ORM / Database access |
| Spring Validation | (Boot managed) | Validate request body |
| Spring Actuator | (Boot managed) | Health check & Monitoring |
| MySQL Connector | (Boot managed) | Kết nối MySQL |
| JJWT | 0.12.6 | Tạo & xác thực JWT token |
| springdoc-openapi | 2.8.5 | Swagger UI / API docs |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Lombok | (Boot managed) | Giảm boilerplate code |

### Infrastructure
| Công nghệ | Mục đích |
|---|---|
| Docker | Container hóa ứng dụng |
| Docker Compose | Local development stack |
| MySQL 8.0 | Database chính |
| GitHub Actions | CI/CD Pipeline |
| GitHub Container Registry (ghcr.io) | Lưu trữ Docker image |
| SSH (appleboy/ssh-action) | Deploy lên server |

---

## 🔐 Bảo Mật (Spring Security + JWT)

### Cơ chế xác thực
- **Stateless** — Không dùng session, dùng JWT token
- **JWT** — Ký bằng HMAC-SHA, thời hạn mặc định **24 giờ** (86400000ms)
- **BCrypt** — Mã hóa password

### Phân quyền truy cập
| Loại endpoint | Quyền |
|---|---|
| `POST /api/auth/**` | Public (không cần token) |
| `GET /api/products/**` | Public |
| `GET /api/categories/**` | Public |
| `/v3/api-docs/**`, `/swagger-ui/**` | Public |
| `/uploads/images/**` | Public |
| `/actuator/**` | Public |
| Tất cả còn lại | Cần JWT token |

### Roles
| Role | Mô tả |
|---|---|
| `USER` | Người dùng thông thường (mặc định khi đăng ký) |
| `ADMIN` | Quản trị viên |

---

## 🗄️ Database Schema

Database: **`ecommerce`** (MySQL 8.0, port `3307`)

### Bảng chính

| Bảng | Mô tả |
|---|---|
| `site_user` | Tài khoản người dùng (username, email, password, role) |
| `product` | Sản phẩm (name, description, image, category_id) |
| `product_category` | Danh mục sản phẩm (hỗ trợ phân cấp cha-con) |
| `product_item` | Biến thể cụ thể của sản phẩm (SKU, số lượng, giá) |
| `variation` | Thuộc tính biến thể (Màu sắc, Size,...) |
| `variation_option` | Giá trị của variation (Đỏ, XL,...) |
| `product_configuration` | Ánh xạ product_item ↔ variation_option |
| `promotion` | Chương trình khuyến mãi |
| `promotion_category` | Ánh xạ promotion ↔ product_category |
| `shopping_cart` | Giỏ hàng của user |
| `shopping_cart_item` | Item trong giỏ hàng |
| `shop_order` | Đơn hàng |
| `order_line` | Chi tiết dòng trong đơn hàng |
| `order_status` | Trạng thái đơn hàng |
| `shipping_method` | Phương thức vận chuyển |
| `payment_type` | Loại thanh toán |
| `user_payment_method` | Phương thức thanh toán của user |
| `address` | Địa chỉ |
| `country` | Quốc gia |
| `user_address` | Ánh xạ user ↔ address |
| `user_review` | Đánh giá sản phẩm |

---

## 🌐 REST API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | Public | Đăng nhập, nhận JWT token |

### Products (`/api/products`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/products` | Public | Lấy tất cả sản phẩm |
| GET | `/api/products/paged` | Public | Lấy sản phẩm có phân trang |
| GET | `/api/products/search` | Public | Tìm kiếm sản phẩm theo keyword/category |
| GET | `/api/products/{id}` | Public | Lấy sản phẩm theo ID |
| POST | `/api/products` | 🔒 JWT | Tạo sản phẩm mới |
| PUT | `/api/products/{id}` | 🔒 JWT | Cập nhật sản phẩm |
| DELETE | `/api/products/{id}` | 🔒 JWT | Xóa sản phẩm |

### Product Categories (`/api/product-categories`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/product-categories` | Public | Lấy tất cả danh mục |
| GET | `/api/product-categories/{id}` | Public | Lấy danh mục theo ID |
| POST | `/api/product-categories` | 🔒 JWT | Tạo danh mục |
| PUT | `/api/product-categories/{id}` | 🔒 JWT | Cập nhật danh mục |
| DELETE | `/api/product-categories/{id}` | 🔒 JWT | Xóa danh mục |

### Product Items (`/api/product-items`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/product-items` | 🔒 JWT | Lấy tất cả product item |
| GET | `/api/product-items/{id}` | 🔒 JWT | Lấy product item theo ID |
| POST | `/api/product-items` | 🔒 JWT | Tạo product item |
| PUT | `/api/product-items/{id}` | 🔒 JWT | Cập nhật product item |
| DELETE | `/api/product-items/{id}` | 🔒 JWT | Xóa product item |

### Variations (`/api/variations`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/variations` | 🔒 JWT | Lấy tất cả variations |
| GET | `/api/variations/{id}` | 🔒 JWT | Lấy variation theo ID |
| POST | `/api/variations` | 🔒 JWT | Tạo variation |
| PUT | `/api/variations/{id}` | 🔒 JWT | Cập nhật variation |
| DELETE | `/api/variations/{id}` | 🔒 JWT | Xóa variation |

### Variation Options (`/api/variation-options`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| GET | `/api/variation-options` | 🔒 JWT | Lấy tất cả variation options |
| POST | `/api/variation-options` | 🔒 JWT | Tạo variation option |
| PUT | `/api/variation-options/{id}` | 🔒 JWT | Cập nhật variation option |
| DELETE | `/api/variation-options/{id}` | 🔒 JWT | Xóa variation option |

### Product Configurations (`/api/product-configurations`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/api/product-configurations` | 🔒 JWT | Tạo cấu hình sản phẩm |
| DELETE | `/api/product-configurations/{productItemId}/{variationOptionId}` | 🔒 JWT | Xóa cấu hình |

### File Upload (`/api/files`)
| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/api/files/image` | 🔒 JWT | Upload một ảnh |
| POST | `/api/files/images` | 🔒 JWT | Upload nhiều ảnh (tối đa 10) |
| DELETE | `/api/files/image/{fileName}` | 🔒 JWT | Xóa ảnh |

### Actuator (Monitoring)
| Endpoint | Mô tả |
|---|---|
| `/actuator/health` | Health check (liveness + readiness) |
| `/actuator/info` | Thông tin ứng dụng |
| `/actuator/metrics` | Metrics |

---

## 📄 API Documentation (Swagger)

| URL | Mô tả |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |

**Sử dụng Swagger với JWT:**
1. Gọi `POST /api/auth/login` để lấy `accessToken`
2. Click **Authorize** trên Swagger UI
3. Nhập `Bearer <accessToken>`
4. Thực hiện các API cần xác thực

---

## 📂 Cấu Trúc Package

```
com.utc.ec/
├── EcApplication.java              # Main class
├── config/
│   ├── GlobalExceptionHandler.java # Xử lý exception toàn cục (@RestControllerAdvice)
│   ├── MessageConfig.java          # Cấu hình i18n MessageSource
│   ├── SwaggerConfig.java          # Cấu hình OpenAPI / Swagger
│   ├── WebMvcConfig.java           # Cấu hình serve static file ảnh
│   └── security/
│       ├── SecurityConfig.java         # Spring Security filter chain
│       ├── JwtAuthenticationFilter.java # Filter xử lý JWT mỗi request
│       ├── JwtService.java             # Tạo / validate JWT token
│       └── CustomUserDetailsService.java # Load user từ DB
├── controller/
│   ├── AuthController.java             # /api/auth
│   ├── ProductController.java          # /api/products
│   ├── ProductCategoryController.java  # /api/product-categories
│   ├── ProductItemController.java      # /api/product-items
│   ├── VariationController.java        # /api/variations
│   ├── VariationOptionController.java  # /api/variation-options
│   ├── ProductConfigurationController.java # /api/product-configurations
│   └── FileUploadController.java       # /api/files
├── dto/
│   ├── ApiResponse.java            # Wrapper response chung
│   ├── PagedResponse.java          # Response có phân trang
│   ├── FileUploadResponse.java     # Response upload file
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── AuthResponse.java
│   └── [các DTO khác cho từng entity...]
├── entity/                        # 23 JPA Entity classes
├── exception/
│   ├── BusinessException.java      # Lỗi nghiệp vụ
│   └── ResourceNotFoundException.java # Không tìm thấy resource
├── mapper/                        # MapStruct mappers
├── repository/                    # Spring Data JPA repositories
└── service/
    ├── [các interface Service]
    └── impl/
        └── [các Service Implementation]
```

---

## ⚙️ Cấu Hình Biến Môi Trường

| Biến | Mặc định | Mô tả |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://160.30.113.40:3307/ecommerce?...` | URL kết nối database |
| `SPRING_DATASOURCE_USERNAME` | `root` | Username database |
| `SPRING_DATASOURCE_PASSWORD` | `123456` | Password database |
| `JWT_SECRET` | `3cfa76ef...` (64 ký tự) | Secret key ký JWT |
| `JWT_EXPIRATION` | `86400000` | Thời hạn JWT (ms) = 24 giờ |
| `FILE_UPLOAD_DIR` | `D:\CODE\uploads\images` | Thư mục lưu file upload |

> ⚠️ **Lưu ý bảo mật:** Luôn thay đổi `JWT_SECRET` và `DB_PASSWORD` trong môi trường production!

---

## 🚀 CI/CD Pipeline

**File:** `.github/workflows/ci-cd.yml`  
**Trigger:** Push/PR vào nhánh `main` (khi có thay đổi trong `ec/` hoặc `.github/workflows/`)

### Quy trình 3 bước:

```
[Job 1: Build & Test]
    ↓ (chỉ khi push vào main)
[Job 2: Build & Push Docker Image → ghcr.io]
    ↓
[Job 3: Deploy to Server via SSH]
```

### Chi tiết từng Job

**Job 1 — Build and Test:**
- Setup Java 21 (Temurin)
- Cache Maven packages
- Chạy unit tests (`mvn test`, `continue-on-error: true`)
- Build JAR (`mvn clean package -DskipTests`)
- Upload artifact JAR

**Job 2 — Docker Build & Push:**
- Login vào GitHub Container Registry
- Build multi-platform image (linux/amd64)
- Push với tags: `main`, `main-{sha}`, `latest`
- Sử dụng GitHub Actions cache để tăng tốc build

**Job 3 — Deploy:**
- SSH vào server bằng `appleboy/ssh-action@v1.2.0`
- Auth: `SERVER_HOST`, `SERVER_USER`, `SERVER_PASS` (từ GitHub Secrets)
- Pull image mới từ `ghcr.io`
- Stop/remove container cũ
- Start container mới với volume mount
- Verify health check (30 lần thử, 2s/lần)

### GitHub Secrets cần cấu hình

| Secret | Mô tả |
|---|---|
| `SERVER_HOST` | IP hoặc domain server |
| `SERVER_USER` | Username SSH |
| `SERVER_PASS` | Password SSH |
| `DB_URL` | JDBC URL kết nối database |
| `DB_USERNAME` | Username database |
| `DB_PASSWORD` | Password database |
| `JWT_SECRET` | JWT secret key |
| `JWT_EXPIRATION` | Thời hạn JWT (ms) |

---

## 🐳 Docker

### Dockerfile (Multi-stage Build)

**Stage 1 — Build:**
- Base: `maven:3.9.9-eclipse-temurin-21-alpine`
- Cache Maven dependencies
- Build JAR (`-DskipTests`)

**Stage 2 — Runtime:**
- Base: `eclipse-temurin:21-jre-alpine` (image nhẹ hơn)
- Tạo non-root user `spring` để bảo mật
- Thư mục: `/app/uploads/images`, `/app/logs`
- JVM options: `UseContainerSupport`, `MaxRAMPercentage=75.0`
- Health check: `/actuator/health`

### Docker Compose (Local)

```bash
docker-compose up -d
```

| Service | Container | Port |
|---|---|---|
| MySQL 8.0 | `clothing-store-mysql` | `3307:3306` |
| Spring Boot App | `clothing-store-api` | `8080:8080` |

**Volumes:**
- `mysql_data` — Dữ liệu MySQL
- `app_uploads` — File upload
- `app_logs` — Application logs
- `./ec/src/main/resources/init_sql/init.sql` — Auto init database

---

## 📤 Xử Lý File Upload

- **Upload dir:** Cấu hình qua `FILE_UPLOAD_DIR` (mặc định local: `D:\CODE\uploads\images`)
- **Max file size:** 10MB/file, 50MB/request
- **Định dạng hỗ trợ:** `jpeg`, `png`, `gif`, `webp`, `svg`
- **Max files/lần:** 10 file
- **URL truy cập ảnh:** `GET /uploads/images/{fileName}`
- **Serve static:** Cấu hình trong `WebMvcConfig` dùng `ResourceHandlerRegistry`

---

## 🌍 Internationalization (i18n)

- File: `src/main/resources/messages.properties`
- Cấu hình trong `MessageConfig.java`
- Sử dụng `MessageSource` để lấy message theo locale
- Hỗ trợ tiếng Anh và tiếng Việt (escape Unicode trong file properties)

---

## 🔄 Response Format

Tất cả API trả về dạng `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Thông báo",
  "errorCode": null,
  "data": { ... },
  "timestamp": "2026-03-15T10:00:00Z"
}
```

**Lỗi:**
```json
{
  "success": false,
  "errorCode": "NOT_FOUND",
  "message": "Resource not found.",
  "data": null,
  "timestamp": "2026-03-15T10:00:00Z"
}
```

**Phân trang (`PagedResponse<T>`):**
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

---

## 🚦 Health Check

```
GET /actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

---

## 🛠️ Chạy Project Locally

### Yêu cầu
- Java 21+
- Maven 3.9+
- Docker & Docker Compose (optional)

### Cách 1: Docker Compose (Recommended)
```bash
# Copy env file
cp .env.example .env

# Chỉnh sửa .env theo cấu hình của bạn

# Start tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f app
```

### Cách 2: Chạy thủ công
```bash
# Start MySQL (hoặc dùng instance sẵn có)
# Cập nhật application.yml với thông tin DB

cd ec
./mvnw spring-boot:run
```

### Cách 3: Build JAR
```bash
cd ec
./mvnw clean package -DskipTests
java -jar target/ec-0.0.1-SNAPSHOT.jar
```

---

## 📝 Ghi Chú Quan Trọng

1. **Database:** Schema được khởi tạo từ `init.sql` (DROP + CREATE), không dùng Hibernate DDL auto
2. **JPA:** `ddl-auto: none` — không tự động tạo/chỉnh schema
3. **Swagger version compatibility:** Sử dụng `springdoc-openapi 2.8.5` tương thích Spring Boot 3.5.x
4. **Docker image name:** Phải lowercase (`ghcr.io/utc-group-13/clothing-store-api`) — CI/CD đã xử lý bằng `tr '[:upper:]' '[:lower:]'`
5. **File upload path:** Khi deploy Docker, map volume `-v /host/uploads:/app/uploads` để persist data
6. **SSH Deploy:** Yêu cầu `SERVER_PASS` trong GitHub Secrets (hoặc SSH key)

