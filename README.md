# 🛍️ Clothing Store E-commerce API

> **Backend API cho website bán quần áo trực tuyến**  
> Spring Boot 3.5.11 | Java 21 | MySQL 8.0 | JWT Authentication

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📚 TÀI LIỆU DỰ ÁN

### 🎯 Tài Liệu Chính

| Tài liệu | Mô tả | Link |
|----------|-------|------|
| 📊 **Project Analysis** | Phân tích tổng quan dự án: đánh giá code, chức năng đã có/thiếu, roadmap phát triển, vấn đề tiềm ẩn (40+ trang) | [PROJECT_ANALYSIS.md](PROJECT_ANALYSIS.md) |
| 🎨 **System Flow Diagram** | Sơ đồ trực quan (Mermaid): kiến trúc, luồng authentication, product flow, database ER diagram, use cases (10+ diagrams) | [SYSTEM_FLOW_DIAGRAM.md](SYSTEM_FLOW_DIAGRAM.md) |
| 📋 **API Endpoints List** | Danh sách đầy đủ 105 APIs: 42 đã triển khai + 63 thiếu, request/response examples, business logic chi tiết | [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md) |
| 📦 **Project Summary** | Tổng quan dự án: tech stack, cấu trúc thư mục, dependencies, deployment guide | [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) |

### 🛠️ Hướng Dẫn Kỹ Thuật

| Tài liệu | Mô tả | Link |
|----------|-------|------|
| 🛍️ **Product Creation Flow** | Hướng dẫn từng bước tạo sản phẩm hoàn chỉnh: từ category → variation → product → product item (với API examples) | [PRODUCT_CREATION_FLOW.md](PRODUCT_CREATION_FLOW.md) |
| 🔧 **CORS Fix Guide** | Hướng dẫn sửa lỗi CORS khi ghép frontend: cấu hình chi tiết, testing, troubleshooting | [CORS_FIX.md](CORS_FIX.md) |

### 🔗 Tài Liệu Tham Khảo Ngoài

| Loại | Link |
|------|------|
| 📐 **Database Schema** | [E-commerce Database Design (PDF)](https://dbshostedfiles.s3.us-west-2.amazonaws.com/dbs/dbdesign_ecommerce.pdf) |
| 🎨 **Figma Admin** | [E-commerce Admin Dashboard](https://www.figma.com/design/JkquAUJzeAbacXGYoD6XmL/E-commerce-Management--Community-?node-id=6-1370&p=f&t=0Sa5lQIvLqv9Pg0p-0) |
| 🎨 **Figma User** | [E-commerce User Interface](https://www.figma.com/design/HR3jsiclQTgSriEY9EBz1A/E-commerce-Website-Template--Freebie---Community-?node-id=39-1402&p=f&t=rUKKMA46X9Ix9dRX-0) |

---

## 🚀 QUICK START

### Yêu Cầu Hệ Thống
- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Git

### Cài Đặt & Chạy

```powershell
# 1. Clone repository
git clone <repository-url>
cd clothing-store/ec

# 2. Cấu hình database (application.yml hoặc biến môi trường)
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/clothing_db"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="your_password"

# 3. Tạo database và import schema
mysql -u root -p < src/main/resources/init_sql/init.sql

# 4. Build & Run
mvn clean install
mvn spring-boot:run

# 5. Truy cập
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

---

## 📊 TRẠNG THÁI DỰ ÁN

### ✅ Đã Hoàn Thành (40%)
- ✅ Authentication & JWT
- ✅ Product Management (CRUD đầy đủ)
- ✅ Category Management (hỗ trợ phân cấp)
- ✅ Variation & Options Management
- ✅ Product Configuration
- ✅ File Upload (images)
- ✅ API Documentation (Swagger)
- ✅ Error Handling
- ✅ CORS Configuration

### ⚠️ Đang Phát Triển (60%)
- ❌ Shopping Cart APIs
- ❌ Order Processing & Checkout
- ❌ User Profile & Address Management
- ❌ Payment Integration
- ❌ Promotion System
- ❌ User Reviews & Ratings
- ❌ Inventory Management
- ❌ Analytics Dashboard

> **Chi tiết đầy đủ:** Xem [PROJECT_ANALYSIS.md](PROJECT_ANALYSIS.md) → Phần "Bảng Đánh Giá Tổng Thể"

---

## 🎯 ROADMAP

| Phase | Timeline | Features | Status |
|-------|----------|----------|--------|
| **Phase 1** | ✅ Done | Product Catalog, Authentication | 100% |
| **Phase 2** | 🚧 2-3 weeks | Shopping Cart, Order Processing, User Management | 0% |
| **Phase 3** | 📅 1-2 weeks | Payment Gateway, Promotions | 0% |
| **Phase 4** | 📅 1-2 weeks | Reviews, Inventory, Analytics | 0% |

> **Roadmap chi tiết:** Xem [PROJECT_ANALYSIS.md](PROJECT_ANALYSIS.md) → Phần "Roadmap Khuyến Nghị"

---

## 🔑 FEATURES CHÍNH

### Authentication & Security
- 🔐 JWT Token Authentication
- 🛡️ Spring Security with Role-based Authorization
- 👤 User Registration & Login
- 🔑 BCrypt Password Encoding

### Product Catalog
- 📦 Product Management (CRUD)
- 🏷️ Multi-level Category System
- 🎨 Product Variants with SKU
- 🔧 Flexible Variation System (Color, Size, etc.)
- 🖼️ Image Upload & Management
- 🔍 Product Search & Filtering
- 📄 Pagination Support

### API Features
- 📖 Swagger/OpenAPI Documentation
- 🌐 CORS Support for Frontend Integration
- ✅ Input Validation
- 🚨 Global Exception Handling
- 🌍 i18n Messages (messages.properties)
- 📊 Health Check & Metrics (Actuator)

---

## 🏗️ TECH STACK

### Backend
- **Framework:** Spring Boot 3.5.11
- **Language:** Java 21
- **Security:** Spring Security + JWT (io.jsonwebtoken 0.12.6)
- **Database:** MySQL 8.0 + Spring Data JPA + Hibernate
- **Validation:** Jakarta Bean Validation
- **Mapping:** MapStruct
- **API Docs:** SpringDoc OpenAPI 2.8.5
- **Build Tool:** Maven

### DevOps
- **Containerization:** Docker + Docker Compose
- **Database:** MySQL 8.0 Container
- **Monitoring:** Spring Boot Actuator

---

## 📡 API ENDPOINTS

### Tổng Quan
- **Tổng cộng:** 105 APIs (planned)
- **Đã triển khai:** 42 APIs ✅
- **Chưa triển khai:** 63 APIs ⚠️

### Public Endpoints (không cần authentication)
```
POST   /api/auth/register          - Đăng ký tài khoản
POST   /api/auth/login             - Đăng nhập
GET    /api/products               - Danh sách sản phẩm
GET    /api/products/{id}          - Chi tiết sản phẩm
GET    /api/product-categories     - Danh sách danh mục
GET    /swagger-ui.html            - API Documentation
```

### Protected Endpoints (cần JWT token)
```
POST   /api/products               - Tạo sản phẩm (Admin)
PUT    /api/products/{id}          - Cập nhật sản phẩm (Admin)
DELETE /api/products/{id}          - Xóa sản phẩm (Admin)
POST   /api/files/upload           - Upload ảnh (Admin)
```

> **Danh sách đầy đủ:** Xem [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md)

---

## 🗂️ DATABASE SCHEMA

### 21 Tables (clothing_db)
**Authentication:** site_user, user_address, address, country, user_payment_method, payment_type  
**Product Catalog:** categories, colors, sizes, products, product_variants, variant_stocks  
**Shopping:** shopping_cart, shopping_cart_item  
**Orders:** shop_order, order_line, order_status, shipping_method  
**Marketing:** promotion, promotion_category  
**Reviews:** user_review

### Luồng sản phẩm quần áo
```
categories (danh mục cha/con)
    └── products (sản phẩm + base_price, brand, material)
            └── product_variants (sản phẩm × màu sắc)
                    └── variant_stocks (biến thể × size = SKU + tồn kho + giá riêng)

colors (bảng màu sắc riêng)
sizes  (bảng size riêng: clothing/numeric/shoes)
```

> **ER Diagram:** Xem [SYSTEM_FLOW_DIAGRAM.md](SYSTEM_FLOW_DIAGRAM.md) → Phần "Database Relationships"

---

## 🛠️ DEVELOPMENT

### Build Project
```powershell
mvn clean install
```

### Run Tests
```powershell
mvn test
```

### Run Application
```powershell
mvn spring-boot:run
```

### Build Docker Image
```powershell
docker build -t clothing-store-api ./ec
```

### Run with Docker Compose
```powershell
docker-compose up -d
```

---

## 🔧 CONFIGURATION

### Environment Variables
```powershell
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/clothing_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=86400000

# CORS (Frontend URLs)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# File Upload
FILE_UPLOAD_DIR=C:\CODE\uploads\images
```

> **Chi tiết:** Xem `ec/src/main/resources/application.yml`

---

## 📖 HƯỚNG DẪN SỬ DỤNG

### 1. Tạo Sản Phẩm Hoàn Chỉnh
Xem hướng dẫn chi tiết 6 bước trong: [PRODUCT_CREATION_FLOW.md](PRODUCT_CREATION_FLOW.md)

### 2. Sửa Lỗi CORS Khi Ghép Frontend
Xem hướng dẫn: [CORS_FIX.md](CORS_FIX.md)

### 3. Hiểu Luồng Hoạt Động Hệ Thống
Xem các sơ đồ trực quan: [SYSTEM_FLOW_DIAGRAM.md](SYSTEM_FLOW_DIAGRAM.md)

### 4. Implement Chức Năng Mới
Tham khảo danh sách APIs và business logic: [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md)

---

## 🧪 TESTING APIs

### Option 1: Swagger UI (Recommended)
```
http://localhost:8080/swagger-ui.html
```
- ✅ Interactive testing
- ✅ Tự động generate request/response
- ✅ Support JWT authentication

### Option 2: Postman
Import OpenAPI spec:
```
http://localhost:8080/v3/api-docs
```

### Option 3: cURL
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your_password"}'

# Get Products
curl http://localhost:8080/api/products

# Create Product (with JWT)
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"categoryId":1,"name":"Test Product","description":"Test"}'
```

---

## 🎓 TECH INSIGHTS

### Kiến Trúc
```
Controller → Service → Repository → Entity → Database
     ↓          ↓
   DTO    Exception Handler
```

### Security Flow
```
Request → JwtAuthenticationFilter → SecurityContext → Controller
```

### File Upload Flow
```
MultipartFile → FileStorageService → /uploads/images/{filename}
```

> **Chi tiết:** Xem [SYSTEM_FLOW_DIAGRAM.md](SYSTEM_FLOW_DIAGRAM.md)

---

## 🐛 TROUBLESHOOTING

### Lỗi CORS khi gọi từ Frontend
**Giải pháp:** [CORS_FIX.md](CORS_FIX.md)

### Database Connection Failed
```powershell
# Kiểm tra MySQL đang chạy
Get-Service MySQL80

# Test connection
mysql -h localhost -P 3306 -u root -p
```

### Port 8080 Already in Use
```powershell
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

### JWT Token Invalid
- Kiểm tra `JWT_SECRET` phải giống nhau giữa các lần chạy
- Token hết hạn sau 24h (default)
- Format header: `Authorization: Bearer <token>`

---

## 📞 SUPPORT & CONTACT

### Documentation
- 📊 Full Analysis: [PROJECT_ANALYSIS.md](PROJECT_ANALYSIS.md)
- 🎨 System Diagrams: [SYSTEM_FLOW_DIAGRAM.md](SYSTEM_FLOW_DIAGRAM.md)
- 📋 API List: [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md)
- 🛍️ Product Guide: [PRODUCT_CREATION_FLOW.md](PRODUCT_CREATION_FLOW.md)
- 🔧 CORS Fix: [CORS_FIX.md](CORS_FIX.md)

### External Resources
- 📐 [Database Design Reference](https://dbshostedfiles.s3.us-west-2.amazonaws.com/dbs/dbdesign_ecommerce.pdf)
- 🎨 [Admin UI Design (Figma)](https://www.figma.com/design/JkquAUJzeAbacXGYoD6XmL/E-commerce-Management--Community-?node-id=6-1370&p=f&t=0Sa5lQIvLqv9Pg0p-0)
- 🎨 [User UI Design (Figma)](https://www.figma.com/design/HR3jsiclQTgSriEY9EBz1A/E-commerce-Website-Template--Freebie---Community-?node-id=39-1402&p=f&t=rUKKMA46X9Ix9dRX-0)

---

## 📈 PROJECT STATUS

```
✅ Product Catalog Module:     [██████████] 100%
✅ Authentication Module:       [████████░░] 80%
⚠️ Shopping Cart Module:       [░░░░░░░░░░] 0%
⚠️ Order Management Module:    [░░░░░░░░░░] 0%
⚠️ User Management Module:     [░░░░░░░░░░] 0%
⚠️ Payment Module:              [░░░░░░░░░░] 0%
⚠️ Promotion Module:            [░░░░░░░░░░] 0%
⚠️ Review Module:               [░░░░░░░░░░] 0%

Overall Progress:               [████░░░░░░] 40%
```

**Next Sprint:** Shopping Cart + Order Processing (Sprint 1 - 2 weeks)

---

## 🤝 CONTRIBUTING

Xem roadmap và APIs cần implement trong: [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md)

**Priority:** 
1. 🔴 Shopping Cart APIs (6 endpoints)
2. 🔴 Order Processing (8 endpoints)
3. 🔴 User Address Management (6 endpoints)

---

## 📄 LICENSE

MIT License - Xem file [LICENSE](LICENSE)

---

## 📝 CHANGELOG

### v0.0.1-SNAPSHOT (Current)
- ✅ Tạo project structure
- ✅ Setup Spring Security + JWT
- ✅ Product Catalog APIs (29 endpoints)
- ✅ File Upload
- ✅ CORS Configuration
- ✅ Swagger Documentation
- 📝 Complete project documentation (5 MD files)

---

**Made with ❤️ using Spring Boot & GitHub Copilot**

