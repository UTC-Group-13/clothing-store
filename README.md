# 🛍️ Clothing Store E-commerce API

> **Backend API cho website bán quần áo trực tuyến**  
> Spring Boot 3.5.11 | Java 21 | MySQL 8.0 | JWT Authentication  
> **Trạng thái:** 96% hoàn thành — 88 API endpoints | 19 Controllers

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

---

## 📚 TÀI LIỆU DỰ ÁN

| Tài liệu | Mô tả | Link |
|----------|-------|------|
| 🤖 **Agents Guide** | Hướng dẫn cho AI coding agents: kiến trúc 3 tầng, code patterns, workflows | [AGENTS.md](AGENTS.md) |
| 📦 **Project Summary** | Tổng quan: tech stack, 81 endpoints, business logic, deployment | [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) |
| 🛍️ **Product Flow** | Luồng tạo sản phẩm 6 bước: Category → Color → Size → Product → Variant → Stock | [PRODUCT_FLOW.md](PRODUCT_FLOW.md) |
| 💻 **Frontend API Guide** | API reference cho frontend: 81 endpoints, request/response examples, code mẫu | [FRONTEND_API_GUIDE.md](FRONTEND_API_GUIDE.md) |
| 🗄️ **Database Analysis** | Phân tích schema 21 bảng, FK constraints, business logic, performance | [DATABASE_ANALYSIS.md](DATABASE_ANALYSIS.md) |
| 🗺️ **Use Case Diagram** | Sơ đồ use case tổng quan (Guest / User / Admin) | [USE_CASES.md](USE_CASES.md) |
| 🌟 **Reviews Design** | Thiết kế module đánh giá: 5 API endpoints, chống fake review, batch load | [REVIEWS_DESIGN.md](REVIEWS_DESIGN.md) |

### 🔗 Tài Liệu Tham Khảo Ngoài

| Loại | Link |
|------|------|
| 📐 **Database Schema** | [E-commerce Database Design (PDF)](https://dbshostedfiles.s3.us-west-2.amazonaws.com/dbs/dbdesign_ecommerce.pdf) |
| 🎨 **Figma Admin** | [E-commerce Admin Dashboard](https://www.figma.com/design/JkquAUJzeAbacXGYoD6XmL/E-commerce-Management--Community-?node-id=6-1370&p=f&t=0Sa5lQIvLqv9Pg0p-0) |
| 🎨 **Figma User** | [E-commerce User Interface](https://www.figma.com/design/HR3jsiclQTgSriEY9EBz1A/E-commerce-Website-Template--Freebie---Community-?node-id=39-1402&p=f&t=rUKKMA46X9Ix9dRX-0) |

---

## 🚀 QUICK START

### Yêu Cầu
- Java 21+ · Maven 3.8+ · MySQL 8.0 (port 3307) · Docker (tuỳ chọn)

### Option 1 — Docker Compose (Khuyến nghị ⭐)

```powershell
git clone <repository-url>
cd clothing-store
docker-compose up -d          # MySQL (3307) + App (8080)
docker-compose logs -f app    # Xem logs
```

### Option 2 — Maven

```powershell
cd clothing-store/ec
mysql -h localhost -P 3307 -u root -p < src/main/resources/init_sql/init.sql
mvn clean install
mvn spring-boot:run
```

### Sau khi chạy

| URL | Mô tả |
|-----|-------|
| http://localhost:8080/swagger-ui.html | Swagger UI (test 86 APIs) |
| http://localhost:8080/actuator/health | Health check |
| `POST /api/sample-data/generate` | Tạo 50 sản phẩm mẫu |

---

## 📊 TRẠNG THÁI DỰ ÁN

```
✅ Authentication (3 APIs)          [██████████] 100%
✅ Categories (8 APIs)              [██████████] 100%
✅ Colors (5 APIs)                  [██████████] 100%
✅ Sizes (6 APIs)                   [██████████] 100%
✅ Products (9 APIs)                [██████████] 100%
✅ Product Variants (7 APIs)        [██████████] 100%
✅ Variant Stocks (8 APIs)          [██████████] 100%
✅ Shopping Cart (5 APIs)           [██████████] 100%
✅ Orders (7 APIs)                  [██████████] 100%
✅ User Addresses (5 APIs)          [██████████] 100%
✅ Payment Methods (5 APIs)         [██████████] 100%
✅ Payment Types (6 APIs)           [██████████] 100%
✅ Shipping Methods (6 APIs)        [██████████] 100%
✅ Order Statuses (6 APIs)          [██████████] 100%
✅ Shop Bank Accounts (6 APIs)      [██████████] 100%
✅ File Upload (3 APIs)             [██████████] 100%
✅ Sample Data (1 API)              [██████████] 100%
✅ Reviews (5 APIs)                 [██████████] 100%
✅ AI Chat Bot (2 APIs)             [██████████] 100%  ✨ NEW
⚠️ Promotion (0 APIs)              [░░░░░░░░░░]   0%  (Entity có, chưa API)

Overall: 88 APIs · 19/20 modules   [█████████░]  96%
```

> **Chi tiết:** [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) · [DATABASE_ANALYSIS.md](DATABASE_ANALYSIS.md)

---

## 🎯 ROADMAP

| Phase | Features | Status |
|-------|----------|--------|
| **Phase 1** ✅ | Product Catalog, Auth, Categories, Colors, Sizes | Done |
| **Phase 2** ✅ | Cart, Orders, Addresses, Payments, Shipping, File Upload | Done |
| **Phase 3** ✅ | Reviews System, AI Chatbot (Claude API) | Done |
| **Phase 4** 🔄 | Promotion System | In Progress |
| **Phase 5** 📅 | Payment Gateway Webhooks, Email Notifications, Analytics | Todo |

---

## 🔑 FEATURES CHÍNH

### 🔐 Authentication & Security
- JWT Token (24h) · BCrypt Password · Role-based (USER / ADMIN)

### 🛍️ Product Catalog — Mô hình 3 tầng
```
Product (tên, giá gốc, brand, material)
  └─ ProductVariant (product × màu sắc, ảnh theo màu)
       └─ VariantStock (variant × size = SKU + tồn kho + giá override)
```
- Categories cây cha/con · Colors (hex + slug) · Sizes (3 loại)
- Tìm kiếm nâng cao: `name`, `categoryIds`, `colorIds`, `minPrice`, `maxPrice`
- Phân trang · Sắp xếp · Slug SEO-friendly

### 🛒 Shopping Cart
- Tự tạo cart · Auto-merge items · Validate tồn kho · Summary join 7 bảng

### 📦 Order Management
- Đặt hàng transaction 10 bước (trừ tồn kho → xóa giỏ)
- Hủy đơn PENDING → hoàn tồn kho · Mã đơn: `DH20260325001`
- VietQR Integration · Admin quản lý đơn + cập nhật trạng thái

### 👤 User Management
- Địa chỉ giao hàng (CRUD + default) · Phương thức thanh toán (CRUD + default)

### ⚙️ Configuration & Support
- Payment Types · Shipping Methods · Order Statuses · Shop Bank Accounts
- File Upload · Sample Data · Swagger UI · Exception Handler (i18n) · CORS

### 🤖 AI Shopping Chatbot ✨ NEW
- **Endpoint:** `POST /api/chat/message` — **Không cần đăng nhập**
- Tích hợp **Claude API** (Anthropic) — `claude-3-haiku-20240307`
- Tự động tìm sản phẩm liên quan từ DB làm context cho AI
- Trả về phản hồi tự nhiên + **top 5 sản phẩm gợi ý** kèm ảnh, giá
- Session management in-memory (tối đa 20 tin nhắn, TTL 2h)
- **Fallback thông minh** khi không có API key — vẫn trả sản phẩm từ DB
- Cấu hình: `CLAUDE_API_KEY=sk-ant-...` (biến môi trường)

### ⭐ Product Reviews
- Chống fake review: chỉ đánh giá được sau khi đơn hàng hoàn thành
- Rating 1-5 sao + comment · Thống kê đánh giá theo sản phẩm

---

## 🏗️ TECH STACK

| Layer | Công nghệ |
|-------|----------|
| **Framework** | Spring Boot 3.5.11 |
| **Language** | Java 21 |
| **Security** | Spring Security + JWT (io.jsonwebtoken 0.12.6) |
| **Database** | MySQL 8.0 + Spring Data JPA + Hibernate |
| **Validation** | Jakarta Bean Validation |
| **Mapping** | MapStruct 1.6.3 · Lombok |
| **API Docs** | SpringDoc OpenAPI 2.8.5 |
| **AI Integration** | Claude API (Anthropic) — `claude-3-haiku-20240307` |
| **Build** | Maven |
| **DevOps** | Docker + Docker Compose · Spring Boot Actuator |

---

## 📡 API ENDPOINTS (86 APIs)

### Public (không cần token)
```
POST   /api/auth/register · /login             Auth
POST   /api/chat/message                       AI Chatbot (gợi ý sản phẩm) ✨
GET    /api/products/search?name&categoryIds&…  Tìm kiếm nâng cao
GET    /api/products/slug/{slug}               Chi tiết (SEO)
GET    /api/categories/roots                   Danh mục gốc
GET    /api/product-variants/product/{id}      Tất cả màu
GET    /api/variant-stocks/variant/{id}        Tất cả size
GET    /api/colors · /sizes · /payment-types   Danh sách config
GET    /api/shipping-methods · /order-statuses Config
GET    /api/shop-bank-accounts/active          TK NH shop
GET    /api/reviews/product/{id}               Reviews sản phẩm
GET    /api/reviews/product/{id}/summary       Thống kê đánh giá
```

### User (cần JWT)
```
POST   /api/auth/change-password               Đổi mật khẩu
CRUD   /api/cart + /cart/items/{id}             Giỏ hàng (5 APIs)
POST   /api/orders · GET · PATCH /cancel       Đơn hàng (4 APIs)
CRUD   /api/addresses + PATCH /default          Địa chỉ (5 APIs)
CRUD   /api/payment-methods + PATCH /default    Thanh toán (5 APIs)
POST   /api/reviews · GET /my · DELETE /{id}   Đánh giá (3 APIs)
POST   /api/files/image · /images               Upload ảnh
```

### Admin (cần JWT + ADMIN)
```
CRUD   /api/products · categories · colors · sizes · variants · stocks
GET    /api/orders/admin/all · /{id}           Quản lý đơn
PATCH  /api/orders/{id}/status                 Cập nhật trạng thái
CRUD   /api/payment-types · shipping-methods · order-statuses · shop-bank-accounts
```

> **API Reference đầy đủ:** [FRONTEND_API_GUIDE.md](FRONTEND_API_GUIDE.md)

---

## 🗂️ DATABASE SCHEMA

### 21 bảng — `clothing_db` (MySQL 8.0, port 3307)

| Nhóm | Bảng |
|------|------|
| **Auth** | site_user · address · user_address · country |
| **Catalog** | categories · colors · sizes · products · product_variants · variant_stocks |
| **Shopping** | shopping_cart · shopping_cart_item |
| **Orders** | shop_order · order_line · order_status · shipping_method |
| **Payment** | payment_type · user_payment_method · shop_bank_account |
| **Marketing** | promotion · promotion_category |
| **Reviews** | user_review |

```
Áo Nike (Product: basePrice 250k)
  ├─ Màu Đỏ (Variant) → [img1, img2]
  │   ├─ Size S → SKU: NIKE-RED-S, qty: 10, giá: 250k
  │   └─ Size M → SKU: NIKE-RED-M, qty: 5,  giá: 250k
  └─ Màu Xanh (Variant)
      └─ Size M → SKU: NIKE-BLU-M, qty: 8,  giá: 280k (override ⭐)
```

> **Chi tiết:** [DATABASE_ANALYSIS.md](DATABASE_ANALYSIS.md)

---

## 🔧 CONFIGURATION

```powershell
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/clothing_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456
JWT_SECRET=3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b
JWT_EXPIRATION=86400000                # 24 giờ
FILE_UPLOAD_DIR=C:\CODE\uploads\images
CLAUDE_API_KEY=sk-ant-api03-...        # Lấy tại https://console.anthropic.com (tuỳ chọn)
```

> **Chi tiết:** `ec/src/main/resources/application.yml`

---

## 📖 HƯỚNG DẪN SỬ DỤNG

| Mục đích | Tài liệu |
|----------|----------|
| 🛍️ Tạo sản phẩm (6 bước) | [PRODUCT_FLOW.md](PRODUCT_FLOW.md) |
| 💻 Tích hợp frontend | [FRONTEND_API_GUIDE.md](FRONTEND_API_GUIDE.md) |
| 🤖 AI coding agent | [AGENTS.md](AGENTS.md) |
| 🗺️ Use case diagram | [USE_CASES.md](USE_CASES.md) |
| 🌟 Đánh giá sản phẩm (Reviews) | [REVIEWS_DESIGN.md](REVIEWS_DESIGN.md) |
| 🗄️ Database design | [DATABASE_ANALYSIS.md](DATABASE_ANALYSIS.md) |

---

## 🧪 TESTING APIs

### Swagger UI (Khuyến nghị ⭐)
```
http://localhost:8080/swagger-ui.html
```

### cURL
```bash
# Đăng nhập
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# Tìm sản phẩm (Public)
curl "http://localhost:8080/api/products/search?name=áo&minPrice=100000"

# Đặt hàng (cần token)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddressId":1,"paymentTypeId":1,"shippingMethodId":1}'
```

### Postman
Import: `http://localhost:8080/v3/api-docs`

---

## 🎓 KIẾN TRÚC

```
Controller (REST)  →  Service (Logic)  →  Repository (JPA)  →  MySQL
     ↕                     ↕
   DTO  ←── MapStruct ──→ Entity
     ↕
GlobalExceptionHandler (i18n)
```

### Order Transaction (10 bước)
```
POST /api/orders
 1. Validate address · payment · shipping
 2. Get cart items
 3. Check stock TẤT CẢ items
 4. Calculate total
 5. Create ShopOrder (PENDING)
 6. Create OrderLines
 7. DEDUCT STOCK  ← @Transactional
 8. CLEAR CART    ← rollback nếu fail
 9. Generate order code (DH20260325001)
10. Generate VietQR URL
```

> **Chi tiết:** [AGENTS.md](AGENTS.md) · [DATABASE_ANALYSIS.md](DATABASE_ANALYSIS.md)

---

## 🐛 TROUBLESHOOTING

| Vấn đề | Giải pháp |
|--------|----------|
| Database Connection Failed | Kiểm tra MySQL: `mysql -h localhost -P 3307 -u root -p` |
| Port 8080 Already in Use | `netstat -ano \| findstr :8080` rồi `taskkill /PID <PID> /F` |
| JWT Token Invalid | Token hết hạn 24h → đăng nhập lại · Header: `Authorization: Bearer <token>` |
| CORS Error | Backend cho phép `*` · Kiểm tra `Content-Type: application/json` |

---

## 🤝 CONTRIBUTING

### Còn thiếu 1 module:

1. **Promotion System** — Entity + Repo có rồi, cần ServiceImpl + Controller

### Quy trình:
1. Đọc [AGENTS.md](AGENTS.md) — code patterns bắt buộc
2. Follow existing patterns (Service → Controller → Mapper → DTO)
3. Thêm message keys vào `messages.properties`
4. Test qua Swagger UI

---

## 📝 CHANGELOG

### v0.0.1-SNAPSHOT (March 25, 2026)
- ✅ 19 Controllers · 88 APIs · 21 Database Tables
- ✅ Auth (JWT + BCrypt) · Product Catalog 3 tầng (45 APIs)
- ✅ Cart (5) · Orders (7) · Addresses (5) · Payments (10) · Config (24 APIs)
- ✅ Reviews (5 APIs) — chống fake review qua FK tới order_line
- ✅ **AI Chatbot (2 APIs)** — Claude API + in-memory session + product context từ DB ✨
- ✅ File Upload · Sample Data · VietQR · Swagger · i18n · CORS · Docker
- ✅ 7 documentation files
- ⚠️ Todo: Promotion · Payment Webhooks · Email

---

**Made with ❤️ using Spring Boot & GitHub Copilot**
