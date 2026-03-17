# 📊 PHÂN TÍCH DỰ ÁN CLOTHING STORE API

> **Ngày phân tích:** March 17, 2026  
> **Phiên bản:** 0.0.1-SNAPSHOT  
> **Trạng thái:** ✅ Code cơ bản đúng, thiếu một số chức năng quan trọng

---

## 🎯 TÓM TẮT ĐÁNH GIÁ

### ✅ Đã Làm Tốt
1. **Kiến trúc phân tầng rõ ràng**: Controller → Service → Repository
2. **Bảo mật JWT đầy đủ**: Authentication, Authorization với Spring Security
3. **Ánh xạ database chính xác**: 23 entities tương ứng với schema SQL
4. **API Documentation**: Tích hợp Swagger/OpenAPI
5. **Error Handling**: GlobalExceptionHandler và custom exceptions
6. **Validation**: Sử dụng Bean Validation
7. **Dockerization**: Dockerfile và docker-compose sẵn sàng

### ⚠️ Chức Năng Còn Thiếu
1. **Shopping Cart Management** - Không có API quản lý giỏ hàng
2. **Order Processing** - Chưa có API đặt hàng và quản lý đơn hàng
3. **User Management** - Thiếu API quản lý thông tin user, địa chỉ
4. **Payment Integration** - Chưa xử lý thanh toán
5. **Promotion/Discount** - Chưa có API áp dụng khuyến mãi
6. **User Reviews** - Chưa có API đánh giá sản phẩm
7. **Address Management** - Thiếu API quản lý địa chỉ giao hàng
8. **Inventory Management** - Chưa có logic kiểm tra tồn kho khi đặt hàng

---

## 📐 SƠ ĐỒ LUỒNG TỔNG THỂ DỰ ÁN

### 1️⃣ LUỒNG AUTHENTICATION & AUTHORIZATION

```
┌─────────────────────────────────────────────────────────────────┐
│                    AUTHENTICATION FLOW                           │
└─────────────────────────────────────────────────────────────────┘

[User] 
   │
   ├─► POST /api/auth/register
   │      ├─ RegisterRequest (username, email, password)
   │      ├─ BCrypt password encoding
   │      ├─ Role: USER (default)
   │      └─► Return JWT Token
   │
   └─► POST /api/auth/login
          ├─ LoginRequest (username, password)
          ├─ Spring Security Authentication
          ├─ UserDetailsService validates credentials
          └─► Return JWT Token (expiration: 24h)

[Protected APIs]
   ├─ JwtAuthenticationFilter intercepts
   ├─ Validates JWT token
   ├─ Sets SecurityContext
   └─ Proceeds to Controller

PUBLIC Endpoints:
 • /api/auth/** (login, register)
 • GET /api/products/** (xem sản phẩm)
 • GET /api/categories/** (xem danh mục)
 • /swagger-ui/** (API docs)
 • /uploads/images/** (static files)

PROTECTED Endpoints:
 • POST/PUT/DELETE /api/products/** (CRUD sản phẩm)
 • POST/PUT/DELETE /api/product-categories/** (CRUD danh mục)
 • Tất cả API khác (yêu cầu JWT)
```

---

### 2️⃣ LUỒNG QUẢN LÝ SẢN PHẨM (PRODUCT MANAGEMENT)

```
┌─────────────────────────────────────────────────────────────────┐
│              PRODUCT CATALOG STRUCTURE                           │
└─────────────────────────────────────────────────────────────────┘

product_category (Danh mục)
    ↓ (1:N)
    ├─► product (Sản phẩm gốc)
    │      ↓ (1:N)
    │      └─► product_item (Biến thể cụ thể - SKU, giá, tồn kho)
    │             ↓ (N:M)
    │             └─► product_configuration
    │                    ↓
    └─► variation (Loại thuộc tính: Màu sắc, Size)
           ↓ (1:N)
           └─► variation_option (Giá trị: Đỏ, Xanh, M, L, XL)


═══════════════════════════════════════════════════════════════════
  LUỒNG TẠO SẢN PHẨM HOÀN CHỈNH (6 BƯỚC)
═══════════════════════════════════════════════════════════════════

BƯỚC 1: Tạo Danh Mục
────────────────────────────────────────────────────────────────
POST /api/product-categories
{
  "categoryName": "Áo thun",
  "parentCategoryId": null    // Danh mục gốc
}
→ Nhận: { id: 1 }

POST /api/product-categories
{
  "categoryName": "Áo thun nam",
  "parentCategoryId": 1       // Danh mục con
}
→ Nhận: { id: 2 }


BƯỚC 2: Tạo Loại Thuộc Tính (Variation)
────────────────────────────────────────────────────────────────
POST /api/variations
{
  "name": "Màu sắc",
  "categoryId": 2             // Gắn với category
}
→ Nhận: { id: 1 }

POST /api/variations
{
  "name": "Kích thước",
  "categoryId": 2
}
→ Nhận: { id: 2 }


BƯỚC 3: Tạo Giá Trị Thuộc Tính (Variation Option)
────────────────────────────────────────────────────────────────
POST /api/variation-options
{
  "variationId": 1,
  "value": "Đỏ"
}
→ Nhận: { id: 1 }

POST /api/variation-options
{
  "variationId": 1,
  "value": "Xanh"
}
→ Nhận: { id: 2 }

POST /api/variation-options
{
  "variationId": 2,
  "value": "M"
}
→ Nhận: { id: 3 }

POST /api/variation-options
{
  "variationId": 2,
  "value": "L"
}
→ Nhận: { id: 4 }


BƯỚC 4: Tạo Sản Phẩm Gốc (Product)
────────────────────────────────────────────────────────────────
POST /api/products
{
  "categoryId": 2,
  "name": "Áo thun cổ tròn basic",
  "description": "Chất liệu cotton 100%",
  "productImage": "/uploads/images/ao-thun-basic.jpg"
}
→ Nhận: { id: 100 }

⚠️ LƯU Ý: Product KHÔNG có giá!


BƯỚC 5: Tạo Biến Thể Sản Phẩm (Product Item)
────────────────────────────────────────────────────────────────
POST /api/product-items
{
  "productId": 100,
  "sku": "ATCT-RED-M",
  "qtyInStock": 50,
  "price": 150000,
  "productImage": "/uploads/images/ao-do-m.jpg"
}
→ Nhận: { id: 1001 }

POST /api/product-items
{
  "productId": 100,
  "sku": "ATCT-RED-L",
  "qtyInStock": 30,
  "price": 160000,
  "productImage": "/uploads/images/ao-do-l.jpg"
}
→ Nhận: { id: 1002 }

POST /api/product-items
{
  "productId": 100,
  "sku": "ATCT-BLUE-M",
  "qtyInStock": 40,
  "price": 150000,
  "productImage": "/uploads/images/ao-xanh-m.jpg"
}
→ Nhận: { id: 1003 }


BƯỚC 6: Gắn Thuộc Tính Cho Biến Thể (Product Configuration)
────────────────────────────────────────────────────────────────
POST /api/product-configurations
{
  "productItemId": 1001,
  "variationOptionId": 1      // Đỏ
}

POST /api/product-configurations
{
  "productItemId": 1001,
  "variationOptionId": 3      // M
}

POST /api/product-configurations
{
  "productItemId": 1002,
  "variationOptionId": 1      // Đỏ
}

POST /api/product-configurations
{
  "productItemId": 1002,
  "variationOptionId": 4      // L
}

POST /api/product-configurations
{
  "productItemId": 1003,
  "variationOptionId": 2      // Xanh
}

POST /api/product-configurations
{
  "productItemId": 1003,
  "variationOptionId": 3      // M
}

✅ KẾT QUẢ:
   Sản phẩm "Áo thun cổ tròn basic" có 3 biến thể:
   • ATCT-RED-M: Đỏ, Size M, 150k, còn 50 cái
   • ATCT-RED-L: Đỏ, Size L, 160k, còn 30 cái
   • ATCT-BLUE-M: Xanh, Size M, 150k, còn 40 cái
```

---

### 3️⃣ LUỒNG MUA HÀNG (E-COMMERCE FLOW) - ⚠️ CHƯA TRIỂN KHAI

```
┌─────────────────────────────────────────────────────────────────┐
│            SHOPPING & CHECKOUT FLOW (MISSING)                    │
└─────────────────────────────────────────────────────────────────┘

[Customer] 
   │
   ├─► BROWSE Products
   │      GET /api/products?categoryId=2&page=0&size=20
   │      → Danh sách sản phẩm với phân trang
   │
   ├─► VIEW Product Detail
   │      GET /api/products/100
   │      GET /api/product-items?productId=100
   │      → Chi tiết sản phẩm + các biến thể
   │
   ├─► ADD TO CART ⚠️ CHƯA CÓ API
   │      POST /api/shopping-cart/items
   │      {
   │        "productItemId": 1001,
   │        "qty": 2
   │      }
   │      ├─ Kiểm tra tồn kho (product_item.qty_in_stock)
   │      ├─ Tạo/Update shopping_cart cho user
   │      └─ Insert shopping_cart_item
   │
   ├─► VIEW CART ⚠️ CHƯA CÓ API
   │      GET /api/shopping-cart
   │      → Danh sách sản phẩm trong giỏ
   │      → Tính tổng tiền tạm tính
   │
   ├─► UPDATE CART ⚠️ CHƯA CÓ API
   │      PUT /api/shopping-cart/items/{id}
   │      DELETE /api/shopping-cart/items/{id}
   │
   ├─► SELECT ADDRESS ⚠️ CHƯA CÓ API
   │      GET /api/addresses
   │      POST /api/addresses (nếu thêm mới)
   │
   ├─► SELECT SHIPPING ⚠️ CHƯA CÓ API
   │      GET /api/shipping-methods
   │      → Chọn phương thức giao hàng
   │
   ├─► CHECKOUT ⚠️ CHƯA CÓ API
   │      POST /api/orders/checkout
   │      {
   │        "shippingAddressId": 5,
   │        "shippingMethodId": 1,
   │        "paymentTypeId": 2,
   │        "items": [
   │          { "productItemId": 1001, "qty": 2 }
   │        ]
   │      }
   │      ├─ Validate tồn kho
   │      ├─ Tính tổng tiền (items + shipping)
   │      ├─ Áp dụng promotion (nếu có) ⚠️ CHƯA CÓ
   │      ├─ Tạo shop_order
   │      ├─ Tạo order_line cho từng item
   │      ├─ Trừ tồn kho (product_item.qty_in_stock -= qty)
   │      ├─ Xóa shopping_cart_item
   │      └─ Return order_id
   │
   ├─► PAYMENT ⚠️ CHƯA CÓ API
   │      POST /api/payments/process
   │      → Tích hợp payment gateway (VNPay, MoMo, etc.)
   │      → Update shop_order.order_status
   │
   ├─► VIEW ORDERS ⚠️ CHƯA CÓ API
   │      GET /api/orders
   │      GET /api/orders/{id}
   │      → Lịch sử đơn hàng
   │
   └─► WRITE REVIEW ⚠️ CHƯA CÓ API
          POST /api/reviews
          {
            "orderedProductId": 500,  // order_line.id
            "ratingValue": 5,
            "comment": "Sản phẩm rất tốt!"
          }


═══════════════════════════════════════════════════════════════════
  DATABASE RELATIONSHIPS - ORDER FLOW
═══════════════════════════════════════════════════════════════════

site_user (id=1)
   ↓
shopping_cart (id=10, user_id=1)
   ↓
shopping_cart_item (cart_id=10, product_item_id=1001, qty=2)
   ↓
   │ [CHECKOUT BUTTON]
   ↓
shop_order (id=500, user_id=1, order_date, order_total=320000)
   ├─ shipping_address → address(id)
   ├─ shipping_method → shipping_method(id)
   ├─ payment_method_id → user_payment_method(id)
   └─ order_status → order_status(id)
   ↓
order_line (order_id=500)
   ├─ product_item_id=1001, qty=2, price=150000
   └─ product_item_id=1002, qty=1, price=160000
   ↓
user_review (ordered_product_id=order_line.id, rating_value=5)
```

---

### 4️⃣ LUỒNG QUẢN TRỊ (ADMIN MANAGEMENT)

```
┌─────────────────────────────────────────────────────────────────┐
│                 ADMIN MANAGEMENT FLOW                            │
└─────────────────────────────────────────────────────────────────┘

[Admin User] (role=ADMIN)
   │
   ├─► PRODUCT MANAGEMENT (✅ Đã có)
   │      ├─ POST /api/products
   │      ├─ PUT /api/products/{id}
   │      ├─ DELETE /api/products/{id}
   │      ├─ POST /api/product-items
   │      └─ GET /api/products/search
   │
   ├─► CATEGORY MANAGEMENT (✅ Đã có)
   │      ├─ POST /api/product-categories
   │      ├─ PUT /api/product-categories/{id}
   │      ├─ DELETE /api/product-categories/{id}
   │      └─ GET /api/product-categories/tree
   │
   ├─► VARIATION MANAGEMENT (✅ Đã có)
   │      ├─ POST /api/variations
   │      ├─ POST /api/variation-options
   │      └─ GET /api/variations/category/{categoryId}
   │
   ├─► FILE UPLOAD (✅ Đã có)
   │      └─ POST /api/files/upload
   │            → Upload ảnh sản phẩm
   │            → Lưu vào /uploads/images/
   │
   ├─► ORDER MANAGEMENT ⚠️ CHƯA CÓ API
   │      ├─ GET /api/orders (all orders)
   │      ├─ PUT /api/orders/{id}/status
   │      │     → Cập nhật trạng thái đơn hàng
   │      │     → PENDING → CONFIRMED → SHIPPED → DELIVERED
   │      └─ GET /api/orders/statistics
   │
   ├─► INVENTORY MANAGEMENT ⚠️ CHƯA CÓ API
   │      ├─ GET /api/inventory/low-stock
   │      └─ PUT /api/product-items/{id}/stock
   │            → Cập nhật số lượng tồn kho
   │
   ├─► PROMOTION MANAGEMENT ⚠️ CHƯA CÓ API
   │      ├─ POST /api/promotions
   │      │     {
   │      │       "name": "Summer Sale",
   │      │       "discountRate": 20,
   │      │       "startDate": "2026-06-01",
   │      │       "endDate": "2026-08-31"
   │      │     }
   │      ├─ POST /api/promotions/{id}/categories
   │      │     → Áp dụng promotion cho category
   │      └─ DELETE /api/promotions/{id}
   │
   ├─► USER MANAGEMENT ⚠️ CHƯA CÓ API
   │      ├─ GET /api/users
   │      ├─ PUT /api/users/{id}/status (ban/unban)
   │      └─ GET /api/users/{id}/orders
   │
   └─► ANALYTICS & REPORTS ⚠️ CHƯA CÓ API
          ├─ GET /api/analytics/sales
          ├─ GET /api/analytics/top-products
          └─ GET /api/analytics/revenue
```

---

## 🗂️ CHI TIẾT DATABASE SCHEMA & ENTITIES

### Tất Cả Các Bảng (23 Tables)

```
✅ = Có Entity + Repository
⚠️ = Thiếu Controller/Service

┌─────────────────────────────────────────────────────────────────┐
│  AUTHENTICATION & USER MANAGEMENT                                │
└─────────────────────────────────────────────────────────────────┘
✅ site_user              → SiteUser
✅ user_address           → UserAddress (Composite Key)
✅ address                → Address
✅ country                → Country
✅ user_payment_method    → UserPaymentMethod
✅ payment_type           → PaymentType

Controller/Service: ⚠️ Chỉ có AuthController (login/register)
                    ⚠️ Thiếu API quản lý profile, địa chỉ


┌─────────────────────────────────────────────────────────────────┐
│  PRODUCT CATALOG                                                 │
└─────────────────────────────────────────────────────────────────┘
✅ product_category       → ProductCategory
✅ product                → Product
✅ product_item           → ProductItem
✅ variation              → Variation
✅ variation_option       → VariationOption
✅ product_configuration  → ProductConfiguration (Composite Key)

Controller: ✅ ProductController
            ✅ ProductItemController
            ✅ ProductCategoryController
            ✅ VariationController
            ✅ VariationOptionController
            ✅ ProductConfigurationController
            ✅ FileUploadController


┌─────────────────────────────────────────────────────────────────┐
│  SHOPPING CART                                                   │
└─────────────────────────────────────────────────────────────────┘
✅ shopping_cart          → ShoppingCart
✅ shopping_cart_item     → ShoppingCartItem

Controller/Service: ⚠️ THIẾU HOÀN TOÀN


┌─────────────────────────────────────────────────────────────────┐
│  ORDER MANAGEMENT                                                │
└─────────────────────────────────────────────────────────────────┘
✅ shop_order             → ShopOrder
✅ order_line             → OrderLine
✅ order_status           → OrderStatus
✅ shipping_method        → ShippingMethod

Controller/Service: ⚠️ THIẾU HOÀN TOÀN


┌─────────────────────────────────────────────────────────────────┐
│  PROMOTION & MARKETING                                           │
└─────────────────────────────────────────────────────────────────┘
✅ promotion              → Promotion
✅ promotion_category     → PromotionCategory (Composite Key)

Controller/Service: ⚠️ THIẾU HOÀN TOÀN


┌─────────────────────────────────────────────────────────────────┐
│  REVIEW & RATING                                                 │
└─────────────────────────────────────────────────────────────────┘
✅ user_review            → UserReview

Controller/Service: ⚠️ THIẾU HOÀN TOÀN
```

---

## 🔍 PHÂN TÍCH CHI TIẾT CÁC CHỨC NĂNG

### ✅ CHỨC NĂNG ĐÃ TRIỂN KHAI

#### 1. Authentication & Authorization
**Files:**
- `AuthController.java` - POST /api/auth/register, /login
- `AuthService.java` + `AuthServiceImpl.java`
- `JwtService.java` - Generate/validate JWT
- `JwtAuthenticationFilter.java` - Filter mỗi request
- `SecurityConfig.java` - Spring Security configuration
- `CustomUserDetailsService.java` - Load user từ DB

**Flow:**
1. User đăng ký → hash password (BCrypt) → lưu DB
2. User login → validate credentials → generate JWT
3. Mỗi request → Filter kiểm tra JWT → set SecurityContext
4. Public endpoints: GET products/categories, auth, swagger
5. Protected endpoints: Tất cả POST/PUT/DELETE

**Đánh giá:** ✅ Hoàn chỉnh và đúng chuẩn


#### 2. Product Management
**Entities:** Product, ProductItem, ProductConfiguration

**APIs đã có:**
```
POST   /api/products                  → Tạo sản phẩm
PUT    /api/products/{id}             → Sửa sản phẩm
DELETE /api/products/{id}             → Xóa sản phẩm
GET    /api/products/{id}             → Chi tiết sản phẩm
GET    /api/products                  → Tất cả sản phẩm
GET    /api/products/paged            → Phân trang
GET    /api/products/search           → Tìm kiếm
GET    /api/products/category/{id}    → Lọc theo danh mục

POST   /api/product-items             → Tạo biến thể
PUT    /api/product-items/{id}        → Sửa biến thể
DELETE /api/product-items/{id}        → Xóa biến thể
GET    /api/product-items/{id}        → Chi tiết biến thể
GET    /api/product-items/product/{productId} → Biến thể của sản phẩm

POST   /api/product-configurations    → Gắn thuộc tính
GET    /api/product-configurations/product-item/{id} → Lấy config
```

**Validation logic:**
- Không xóa product nếu có product_item
- Không xóa product_item nếu có trong order_line (⚠️ chưa implement)
- Kiểm tra foreign key (categoryId phải tồn tại)

**Đánh giá:** ✅ CRUD đầy đủ, logic cơ bản đúng


#### 3. Category Management
**Entities:** ProductCategory

**APIs đã có:**
```
POST   /api/product-categories        → Tạo danh mục
PUT    /api/product-categories/{id}   → Sửa danh mục
DELETE /api/product-categories/{id}   → Xóa danh mục
GET    /api/product-categories/{id}   → Chi tiết danh mục
GET    /api/product-categories        → Tất cả danh mục
GET    /api/product-categories/tree   → Cây danh mục
GET    /api/product-categories/root   → Danh mục gốc
```

**Tính năng đặc biệt:**
- Hỗ trợ danh mục phân cấp (parent_category_id)
- API tree để xây dựng menu đa cấp

**Đánh giá:** ✅ Đầy đủ


#### 4. Variation Management
**Entities:** Variation, VariationOption

**APIs đã có:**
```
POST   /api/variations                → Tạo loại thuộc tính
PUT    /api/variations/{id}           → Sửa
DELETE /api/variations/{id}           → Xóa
GET    /api/variations/{id}           → Chi tiết
GET    /api/variations/category/{categoryId} → Lọc theo category

POST   /api/variation-options         → Tạo giá trị
PUT    /api/variation-options/{id}    → Sửa
DELETE /api/variation-options/{id}    → Xóa
GET    /api/variation-options/{id}    → Chi tiết
GET    /api/variation-options/variation/{variationId} → Lọc theo variation
```

**Đánh giá:** ✅ Đầy đủ


#### 5. File Upload
**APIs:**
```
POST /api/files/upload → Upload ảnh sản phẩm
```

**Implementation:**
- Lưu file vào `/uploads/images/`
- Validate file size, type
- Return URL để lưu vào product/product_item

**Đánh giá:** ✅ Cơ bản hoàn chỉnh


#### 6. API Documentation
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Mô tả chi tiết tất cả endpoints
- Hỗ trợ JWT authentication trong Swagger

**Đánh giá:** ✅ Rất tốt


---

### ⚠️ CHỨC NĂNG THIẾU (CRITICAL)

#### 1. Shopping Cart Management ❌ THIẾU
**Entities có:** ShoppingCart, ShoppingCartItem  
**APIs thiếu:**

```java
// Cần implement:
POST   /api/shopping-cart/items        → Thêm vào giỏ
PUT    /api/shopping-cart/items/{id}   → Cập nhật số lượng
DELETE /api/shopping-cart/items/{id}   → Xóa khỏi giỏ
DELETE /api/shopping-cart              → Xóa toàn bộ giỏ
GET    /api/shopping-cart              → Xem giỏ hàng

// Logic cần có:
- Mỗi user có 1 shopping_cart (auto-create khi add item đầu tiên)
- Kiểm tra tồn kho khi add item
- Nếu item đã có trong giỏ → cộng dồn qty
- Tính tổng tiền giỏ hàng (không include shipping)
```

**Ưu tiên:** 🔴 CAO


#### 2. Order Processing ❌ THIẾU
**Entities có:** ShopOrder, OrderLine, OrderStatus, ShippingMethod  
**APIs thiếu:**

```java
// Checkout & Order Creation
POST   /api/orders/checkout
Body: {
  "shippingAddressId": 5,
  "shippingMethodId": 1,
  "paymentTypeId": 2,
  "items": [
    { "productItemId": 1001, "qty": 2 }
  ]
}
Logic:
 1. Validate tồn kho của tất cả items
 2. Tính order_total = sum(item.price * qty) + shipping
 3. Tạo shop_order (status = PENDING)
 4. Tạo order_line cho từng item
 5. Trừ product_item.qty_in_stock
 6. Xóa items khỏi shopping_cart
 7. Return order_id

// Order Management
GET    /api/orders                    → Lịch sử đơn hàng (user)
GET    /api/orders/{id}               → Chi tiết đơn
PUT    /api/orders/{id}/cancel        → Hủy đơn (nếu status = PENDING)

// Admin Order Management
GET    /api/admin/orders              → Tất cả đơn hàng
PUT    /api/admin/orders/{id}/status  → Update trạng thái
Body: { "statusId": 3 }  // PENDING→CONFIRMED→SHIPPED→DELIVERED

// Order Status Master Data
GET    /api/order-statuses            → Danh sách trạng thái
GET    /api/shipping-methods          → Phương thức vận chuyển
```

**Ưu tiên:** 🔴 CAO (Core business logic)


#### 3. User Profile & Address Management ❌ THIẾU
**Entities có:** SiteUser, UserAddress, Address, Country  
**APIs thiếu:**

```java
// User Profile
GET    /api/users/me                  → Thông tin user hiện tại
PUT    /api/users/me                  → Cập nhật profile
PUT    /api/users/me/password         → Đổi mật khẩu

// Address Management
GET    /api/users/me/addresses        → Danh sách địa chỉ
POST   /api/users/me/addresses        → Thêm địa chỉ mới
PUT    /api/users/me/addresses/{id}   → Sửa địa chỉ
DELETE /api/users/me/addresses/{id}   → Xóa địa chỉ
PUT    /api/users/me/addresses/{id}/default → Set địa chỉ mặc định

// Country Master Data
GET    /api/countries                 → Danh sách quốc gia
```

**Ưu tiên:** 🔴 CAO


#### 4. Payment Integration ❌ THIẾU
**Entities có:** UserPaymentMethod, PaymentType  
**APIs thiếu:**

```java
// Payment Methods
GET    /api/users/me/payment-methods  → Danh sách phương thức
POST   /api/users/me/payment-methods  → Thêm thẻ/TK ngân hàng
DELETE /api/users/me/payment-methods/{id}

// Payment Processing
POST   /api/payments/process
Body: {
  "orderId": 500,
  "paymentMethodId": 3
}
Logic:
 1. Tích hợp payment gateway (VNPay, MoMo, Stripe)
 2. Xử lý callback từ gateway
 3. Update order_status = CONFIRMED (nếu thành công)

// Payment Types Master Data
GET    /api/payment-types             → COD, Credit Card, E-Wallet...
```

**Ưu tiên:** 🟡 TRUNG BÌNH (Có thể dùng COD tạm)


#### 5. Promotion & Discount ❌ THIẾU
**Entities có:** Promotion, PromotionCategory  
**APIs thiếu:**

```java
// Admin - Promotion Management
POST   /api/admin/promotions
Body: {
  "name": "Summer Sale",
  "description": "Giảm 20% toàn bộ danh mục Áo",
  "discountRate": 20,
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-08-31T23:59:59"
}

POST   /api/admin/promotions/{id}/categories
Body: { "categoryIds": [1, 2, 3] }

PUT    /api/admin/promotions/{id}
DELETE /api/admin/promotions/{id}

// User - View Promotions
GET    /api/promotions/active         → Khuyến mãi đang chạy
GET    /api/products/{id}/promotion   → Kiểm tra sản phẩm có khuyến mãi?

// Apply trong Checkout
Logic trong POST /api/orders/checkout:
 1. Kiểm tra product.categoryId có promotion active?
 2. Tính discounted_price = price * (1 - discount_rate/100)
 3. Lưu vào order_line
```

**Ưu tiên:** 🟡 TRUNG BÌNH


#### 6. User Reviews & Ratings ❌ THIẾU
**Entities có:** UserReview  
**APIs thiếu:**

```java
// Submit Review
POST   /api/reviews
Body: {
  "orderedProductId": 500,  // order_line.id
  "ratingValue": 5,         // 1-5 stars
  "comment": "Chất lượng tốt"
}
Validate: User phải đã mua sản phẩm (có trong order_line)

// View Reviews
GET    /api/products/{id}/reviews     → Đánh giá của sản phẩm
GET    /api/reviews/{id}              → Chi tiết review
GET    /api/users/me/reviews          → Review của user

// Admin Moderation
DELETE /api/admin/reviews/{id}        → Xóa review spam
```

**Ưu tiên:** 🟢 THẤP (Nice to have)


#### 7. Inventory Management ❌ THIẾU
**Logic thiếu:**

```java
// Low Stock Alert
GET    /api/admin/inventory/low-stock?threshold=10
→ Sản phẩm có qty_in_stock < 10

// Stock History
GET    /api/admin/inventory/history?productItemId=1001
→ Lịch sử nhập/xuất kho (cần thêm bảng inventory_transaction)

// Bulk Update Stock
PUT    /api/admin/product-items/bulk-stock
Body: [
  { "id": 1001, "qty": 100 },
  { "id": 1002, "qty": 50 }
]

// Stock Reservation
Logic khi checkout:
 1. KHÔNG trừ ngay qty_in_stock
 2. Tạo reserved_qty (cần thêm column)
 3. Trừ thật khi order_status = CONFIRMED
 4. Giải phóng nếu hủy đơn
```

**Ưu tiên:** 🟡 TRUNG BÌNH


#### 8. Analytics & Reports ❌ THIẾU

```java
// Admin Dashboard
GET    /api/admin/analytics/sales?from=2026-01-01&to=2026-12-31
Response: {
  "totalRevenue": 50000000,
  "totalOrders": 250,
  "averageOrderValue": 200000
}

GET    /api/admin/analytics/top-products?limit=10
→ Top sản phẩm bán chạy

GET    /api/admin/analytics/revenue-by-category
GET    /api/admin/analytics/daily-sales
```

**Ưu tiên:** 🟢 THẤP


---

## 🐛 VẤN ĐỀ VÀ LỖI TIỀM ẨN

### 1. Relationship Mapping ⚠️
**Vấn đề:** Tất cả entities sử dụng Integer cho foreign keys thay vì @ManyToOne/@OneToMany

**Hiện tại:**
```java
@Column(name = "product_id")
private Integer productId;  // ❌ Không có relationship
```

**Nên là:**
```java
@ManyToOne
@JoinColumn(name = "product_id")
private Product product;    // ✅ JPA relationship
```

**Tác động:**
- Không thể sử dụng JOIN FETCH
- Không tự động cascade delete
- Phải manually join trong query
- Mất lợi thế của ORM

**Đánh giá:** ⚠️ Nên refactor để dễ maintain


### 2. Stock Management Race Condition ⚠️
**Vấn đề:** Khi checkout, nhiều user mua cùng lúc có thể oversell

**Kịch bản:**
```
Product Item có 5 cái tồn kho
User A mua 3 → Check stock: OK → Pending...
User B mua 3 → Check stock: OK → Pending...
→ Cả 2 đều success → Tổng bán 6 cái (oversell!)
```

**Giải pháp:**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public void checkout() {
    // SELECT ... FOR UPDATE
    ProductItem item = repository.findByIdWithLock(id);
    if (item.getQtyInStock() < requestQty) {
        throw new OutOfStockException();
    }
    item.setQtyInStock(item.getQtyInStock() - requestQty);
}
```

**Ưu tiên:** 🔴 CAO (khi implement order)


### 3. Soft Delete ⚠️
**Vấn đề:** Hard delete có thể làm mất dữ liệu lịch sử

**Ví dụ:**
- Admin xóa Product → Đơn hàng cũ không còn thông tin sản phẩm
- User xóa Address → Đơn hàng mất địa chỉ giao hàng

**Giải pháp:** Thêm cột `deleted_at` hoặc `is_active`


### 4. Price History ⚠️
**Vấn đề:** Giá sản phẩm thay đổi theo thời gian

**Hiện tại:**
```java
// order_line lưu price → ✅ Đúng
// Nhưng nếu cần biết giá gốc + giá sau khuyến mãi?
```

**Nên thêm:**
```java
// order_line table
original_price  INT     // Giá gốc
discount_rate   INT     // % giảm giá
final_price     INT     // Giá thực trả (đã giảm)
```


### 5. Image Management ⚠️
**Vấn đề:**
- Xóa Product không xóa file ảnh trên disk
- Không có validation URL ảnh
- Không có image resize/optimization

**Nên cải thiện:**
- Sử dụng cloud storage (S3, Cloudinary)
- CDN để serve ảnh
- Webhook để cleanup orphaned images


---

## 📊 BẢNG ĐÁNH GIÁ TỔNG THỂ

| Module | Entities | Controller | Service | API Count | Status | Priority |
|--------|----------|------------|---------|-----------|--------|----------|
| **Authentication** | ✅ SiteUser | ✅ Auth | ✅ Auth | 2 | ✅ Done | - |
| **Product Catalog** | ✅ 6 entities | ✅ 6 controllers | ✅ 6 services | 40+ | ✅ Done | - |
| **Shopping Cart** | ✅ 2 entities | ❌ Thiếu | ❌ Thiếu | 0/5 | ❌ Missing | 🔴 HIGH |
| **Order Processing** | ✅ 4 entities | ❌ Thiếu | ❌ Thiếu | 0/10 | ❌ Missing | 🔴 HIGH |
| **User Management** | ✅ 4 entities | ❌ Thiếu | ❌ Thiếu | 0/8 | ❌ Missing | 🔴 HIGH |
| **Payment** | ✅ 2 entities | ❌ Thiếu | ❌ Thiếu | 0/5 | ❌ Missing | 🟡 MED |
| **Promotion** | ✅ 2 entities | ❌ Thiếu | ❌ Thiếu | 0/6 | ❌ Missing | 🟡 MED |
| **Reviews** | ✅ 1 entity | ❌ Thiếu | ❌ Thiếu | 0/5 | ❌ Missing | 🟢 LOW |
| **Inventory** | ✅ (trong ProductItem) | ❌ Thiếu | ❌ Thiếu | 0/4 | ❌ Missing | 🟡 MED |
| **Analytics** | ❌ Chưa có | ❌ Thiếu | ❌ Thiếu | 0/5 | ❌ Missing | 🟢 LOW |

**Tổng kết:**
- ✅ Hoàn thành: 25% (Product catalog)
- 🔴 Thiếu quan trọng: 50% (Cart, Order, User)
- 🟡 Thiếu nên có: 20% (Payment, Promotion, Inventory)
- 🟢 Có thể bỏ qua: 5% (Reviews, Analytics)

---

## 🎯 ROADMAP KHUYẾN NGHỊ

### Phase 1: Core E-commerce (2-3 weeks) 🔴
**Mục tiêu:** User có thể mua hàng được

1. **Shopping Cart APIs** (3 days)
   - POST /api/shopping-cart/items
   - GET /api/shopping-cart
   - PUT /api/shopping-cart/items/{id}
   - DELETE /api/shopping-cart/items/{id}

2. **Order Processing** (5 days)
   - POST /api/orders/checkout (with stock validation)
   - GET /api/orders (user orders)
   - GET /api/orders/{id}
   - PUT /api/orders/{id}/cancel

3. **User Profile & Address** (3 days)
   - GET /api/users/me
   - Address CRUD APIs
   - GET /api/countries

4. **Admin Order Management** (2 days)
   - GET /api/admin/orders
   - PUT /api/admin/orders/{id}/status

5. **Testing & Bug Fixing** (2 days)

**Deliverable:** MVP có thể bán hàng (COD payment)


### Phase 2: Payment & Promotion (1-2 weeks) 🟡
1. Payment gateway integration (VNPay/MoMo)
2. Promotion management APIs
3. Apply discount logic trong checkout


### Phase 3: Advanced Features (1-2 weeks) 🟢
1. User reviews & ratings
2. Inventory management & alerts
3. Admin analytics dashboard


### Phase 4: Optimization & Scale 🟢
1. Refactor entities với JPA relationships
2. Implement caching (Redis)
3. Add full-text search (Elasticsearch)
4. Image optimization & CDN
5. API rate limiting

---

## 📝 KẾT LUẬN

### ✅ Điểm Mạnh
1. **Database schema hoàn chỉnh** - Theo chuẩn e-commerce
2. **Kiến trúc sạch** - Tách biệt Controller-Service-Repository
3. **Bảo mật tốt** - JWT authentication đầy đủ
4. **API documentation** - Swagger rất chi tiết
5. **Code quality** - Có validation, exception handling

### ⚠️ Điểm Yếu
1. **Thiếu core business logic** - Không thể checkout, đặt hàng
2. **Không có shopping cart** - User không thể tạo giỏ hàng
3. **Thiếu quản lý đơn hàng** - Admin không theo dõi được orders
4. **Chưa có payment** - Chỉ có entities, không có logic

### 🎯 Đánh Giá Chung
**Điểm: 6/10**
- Backend foundation: ✅ Tốt
- Product catalog: ✅ Hoàn chỉnh
- E-commerce flow: ❌ Chưa có (thiếu 50% chức năng quan trọng)

**Trạng thái:** Đang ở giai đoạn CMS (Content Management) chứ chưa phải E-commerce. Cần bổ sung Shopping Cart và Order Processing để thành một website bán hàng thực sự.

---

**Tài liệu này được tạo bởi GitHub Copilot - March 17, 2026**

