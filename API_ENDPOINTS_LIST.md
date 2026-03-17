# 📋 DANH SÁCH TẤT CẢ CHỨC NĂNG & API ENDPOINTS

> Phân tách chi tiết tất cả chức năng của dự án Clothing Store

---

## 📊 TỔNG QUAN

| Trạng thái | Số lượng | Phần trăm |
|------------|----------|-----------|
| ✅ Đã triển khai | 42 APIs | 40% |
| ❌ Chưa triển khai | 63 APIs | 60% |
| **TỔNG CỘNG** | **105 APIs** | **100%** |

---

## 1️⃣ AUTHENTICATION & AUTHORIZATION

### ✅ Đã Triển Khai (2/5 APIs)

| Method | Endpoint | Chức năng | Request Body | Response |
|--------|----------|-----------|--------------|----------|
| POST | `/api/auth/register` | Đăng ký tài khoản | `{username, email, password}` | `{accessToken, tokenType}` |
| POST | `/api/auth/login` | Đăng nhập | `{username, password}` | `{accessToken, tokenType}` |

### ❌ Cần Triển Khai (3 APIs)

| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| POST | `/api/auth/refresh` | Refresh JWT token | `{refreshToken}` |
| POST | `/api/auth/logout` | Đăng xuất | - |
| POST | `/api/auth/forgot-password` | Quên mật khẩu | `{email}` |

---

## 2️⃣ USER MANAGEMENT

### ❌ Chưa Triển Khai (10/10 APIs)

#### User Profile
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| GET | `/api/users/me` | Xem thông tin cá nhân | - |
| PUT | `/api/users/me` | Cập nhật thông tin | `{email, phoneNumber}` |
| PUT | `/api/users/me/password` | Đổi mật khẩu | `{oldPassword, newPassword}` |
| DELETE | `/api/users/me` | Xóa tài khoản | `{password}` |

#### Address Management
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| GET | `/api/users/me/addresses` | Danh sách địa chỉ | - |
| POST | `/api/users/me/addresses` | Thêm địa chỉ mới | `{unitNumber, streetNumber, addressLine1, city, region, postalCode, countryId}` |
| PUT | `/api/users/me/addresses/{id}` | Sửa địa chỉ | `{...address fields}` |
| DELETE | `/api/users/me/addresses/{id}` | Xóa địa chỉ | - |
| PUT | `/api/users/me/addresses/{id}/default` | Đặt địa chỉ mặc định | - |
| GET | `/api/countries` | Danh sách quốc gia | - |

**Entities liên quan:** SiteUser, UserAddress, Address, Country

---

## 3️⃣ PRODUCT CATALOG

### ✅ Đã Triển Khai (29/29 APIs)

#### Product Management (8 APIs)
| Method | Endpoint | Chức năng | Auth | Response |
|--------|----------|-----------|------|----------|
| POST | `/api/products` | Tạo sản phẩm | ✅ | `ProductDTO` |
| PUT | `/api/products/{id}` | Cập nhật sản phẩm | ✅ | `ProductDTO` |
| DELETE | `/api/products/{id}` | Xóa sản phẩm | ✅ | `204 No Content` |
| GET | `/api/products/{id}` | Chi tiết sản phẩm | 🔓 | `ProductDTO` |
| GET | `/api/products` | Tất cả sản phẩm | 🔓 | `List<ProductDTO>` |
| GET | `/api/products/paged` | Phân trang | 🔓 | `PagedResponse<ProductDTO>` |
| GET | `/api/products/search` | Tìm kiếm | 🔓 | `PagedResponse<ProductDTO>` |
| GET | `/api/products/category/{categoryId}` | Lọc theo danh mục | 🔓 | `List<ProductDTO>` |

**Request Body (POST/PUT):**
```json
{
  "categoryId": 1,
  "name": "Áo thun cổ tròn",
  "description": "Cotton 100%",
  "productImage": "/uploads/images/ao.jpg"
}
```

#### Product Item Management (6 APIs)
| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|------|
| POST | `/api/product-items` | Tạo biến thể | ✅ |
| PUT | `/api/product-items/{id}` | Cập nhật biến thể | ✅ |
| DELETE | `/api/product-items/{id}` | Xóa biến thể | ✅ |
| GET | `/api/product-items/{id}` | Chi tiết biến thể | 🔓 |
| GET | `/api/product-items` | Tất cả biến thể | 🔓 |
| GET | `/api/product-items/product/{productId}` | Biến thể của sản phẩm | 🔓 |

**Request Body:**
```json
{
  "productId": 100,
  "sku": "ATCT-RED-M",
  "qtyInStock": 50,
  "price": 150000,
  "productImage": "/uploads/images/ao-do-m.jpg"
}
```

#### Category Management (7 APIs)
| Method | Endpoint | Chức năng | Auth |
|--------|----------|-----------|------|
| POST | `/api/product-categories` | Tạo danh mục | ✅ |
| PUT | `/api/product-categories/{id}` | Cập nhật danh mục | ✅ |
| DELETE | `/api/product-categories/{id}` | Xóa danh mục | ✅ |
| GET | `/api/product-categories/{id}` | Chi tiết danh mục | 🔓 |
| GET | `/api/product-categories` | Tất cả danh mục | 🔓 |
| GET | `/api/product-categories/tree` | Cây danh mục | 🔓 |
| GET | `/api/product-categories/root` | Danh mục gốc | 🔓 |

#### Variation Management (4 APIs)
| Method | Endpoint | Chức năng |
|--------|----------|-----------|
| POST | `/api/variations` | Tạo loại thuộc tính |
| PUT | `/api/variations/{id}` | Cập nhật |
| DELETE | `/api/variations/{id}` | Xóa |
| GET | `/api/variations/category/{categoryId}` | Lọc theo category |

#### Variation Option Management (4 APIs)
| Method | Endpoint | Chức năng |
|--------|----------|-----------|
| POST | `/api/variation-options` | Tạo giá trị |
| PUT | `/api/variation-options/{id}` | Cập nhật |
| DELETE | `/api/variation-options/{id}` | Xóa |
| GET | `/api/variation-options/variation/{variationId}` | Lọc theo variation |

**Entities:** Product, ProductItem, ProductCategory, Variation, VariationOption, ProductConfiguration

---

## 4️⃣ SHOPPING CART

### ❌ Chưa Triển Khai (6/6 APIs)

| Method | Endpoint | Chức năng | Request Body | Response |
|--------|----------|-----------|--------------|----------|
| POST | `/api/shopping-cart/items` | Thêm vào giỏ | `{productItemId, qty}` | `ShoppingCartItemDTO` |
| GET | `/api/shopping-cart` | Xem giỏ hàng | - | `{items[], subtotal}` |
| PUT | `/api/shopping-cart/items/{id}` | Cập nhật số lượng | `{qty}` | `ShoppingCartItemDTO` |
| DELETE | `/api/shopping-cart/items/{id}` | Xóa item | - | `204 No Content` |
| DELETE | `/api/shopping-cart` | Xóa toàn bộ giỏ | - | `204 No Content` |
| GET | `/api/shopping-cart/count` | Số lượng items | - | `{count: 5}` |

**Business Logic:**
```java
// POST /api/shopping-cart/items
1. Validate: productItemId tồn tại
2. Validate: qty <= product_item.qty_in_stock
3. Check: User đã có shopping_cart? Nếu chưa → tạo mới
4. Check: Item đã có trong cart?
   - Yes → UPDATE qty = qty + request.qty
   - No → INSERT new cart_item
5. Return: ShoppingCartItemDTO

// GET /api/shopping-cart
1. SELECT cart_items JOIN product_items JOIN products
2. Calculate: subtotal = SUM(item.price * item.qty)
3. Return: {items, subtotal}
```

**Entities:** ShoppingCart, ShoppingCartItem  
**Priority:** 🔴 HIGH

---

## 5️⃣ ORDER MANAGEMENT

### ❌ Chưa Triển Khai (15/15 APIs)

#### Checkout & Order Creation (3 APIs)
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| POST | `/api/orders/checkout` | Đặt hàng | `{shippingAddressId, shippingMethodId, paymentTypeId, items[]}` |
| POST | `/api/orders/{id}/confirm` | Xác nhận đơn (after payment) | - |
| GET | `/api/orders/{id}/summary` | Tổng kết đơn hàng | - |

**Checkout Request:**
```json
{
  "shippingAddressId": 5,
  "shippingMethodId": 1,
  "paymentTypeId": 2,
  "items": [
    {
      "productItemId": 1001,
      "qty": 2
    },
    {
      "productItemId": 1003,
      "qty": 1
    }
  ]
}
```

**Checkout Logic:**
```java
1. BEGIN TRANSACTION
2. Validate: tất cả productItemId tồn tại
3. Lock rows: SELECT * FROM product_item FOR UPDATE
4. Validate: qty <= qty_in_stock cho tất cả items
5. Calculate: 
   - itemsTotal = SUM(price * qty)
   - shippingFee = shipping_method.price
   - discount = apply promotion if exists
   - orderTotal = itemsTotal + shippingFee - discount
6. Create shop_order:
   - user_id = current user
   - order_date = NOW()
   - order_status = PENDING
   - order_total = calculated total
7. Create order_line for each item:
   - Save current price (không phụ thuộc vào giá tương lai)
8. Update: product_item.qty_in_stock -= qty
9. Delete: shopping_cart_item của user
10. COMMIT
11. Return: orderId, total
```

#### User Order Management (5 APIs)
| Method | Endpoint | Chức năng | Query Params |
|--------|----------|-----------|--------------|
| GET | `/api/orders` | Lịch sử đơn hàng | `?page=0&size=10&status=PENDING` |
| GET | `/api/orders/{id}` | Chi tiết đơn hàng | - |
| PUT | `/api/orders/{id}/cancel` | Hủy đơn | `{reason}` |
| GET | `/api/orders/{id}/tracking` | Theo dõi vận chuyển | - |
| POST | `/api/orders/{id}/received` | Xác nhận đã nhận hàng | - |

#### Admin Order Management (7 APIs)
| Method | Endpoint | Chức năng | Request |
|--------|----------|-----------|---------|
| GET | `/api/admin/orders` | Tất cả đơn hàng | `?status=PENDING&from=2026-01-01&to=2026-12-31` |
| GET | `/api/admin/orders/{id}` | Chi tiết đơn | - |
| PUT | `/api/admin/orders/{id}/status` | Cập nhật trạng thái | `{statusId, note}` |
| POST | `/api/admin/orders/{id}/ship` | Giao cho shipper | `{trackingNumber, shipperName}` |
| PUT | `/api/admin/orders/{id}/cancel` | Admin hủy đơn | `{reason}` |
| GET | `/api/admin/orders/statistics` | Thống kê đơn hàng | `?from=&to=` |
| POST | `/api/admin/orders/{id}/refund` | Hoàn tiền | `{amount, reason}` |

**Order Status Flow:**
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → COMPLETED
    ↓         ↓            ↓           ↓          ↓
 CANCELLED  CANCELLED  CANCELLED   RETURNED   RETURNED
                                      ↓
                                  REFUNDED
```

**Entities:** ShopOrder, OrderLine, OrderStatus, ShippingMethod  
**Priority:** 🔴 HIGH

---

## 6️⃣ PAYMENT MANAGEMENT

### ❌ Chưa Triển Khai (8/8 APIs)

#### Payment Methods (4 APIs)
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| GET | `/api/users/me/payment-methods` | Danh sách phương thức | - |
| POST | `/api/users/me/payment-methods` | Thêm thẻ/TK | `{paymentTypeId, provider, accountNumber, expiryDate}` |
| PUT | `/api/users/me/payment-methods/{id}/default` | Đặt mặc định | - |
| DELETE | `/api/users/me/payment-methods/{id}` | Xóa phương thức | - |

#### Payment Processing (4 APIs)
| Method | Endpoint | Chức năng | Request |
|--------|----------|-----------|---------|
| POST | `/api/payments/process` | Xử lý thanh toán | `{orderId, paymentMethodId}` |
| GET | `/api/payments/{orderId}/status` | Trạng thái thanh toán | - |
| POST | `/api/payments/vnpay/callback` | VNPay callback | (from gateway) |
| POST | `/api/payments/momo/callback` | MoMo callback | (from gateway) |

**Payment Flow:**
```
1. User checkout → order created (status=PENDING)
2. POST /api/payments/process
3. Generate payment URL (VNPay/MoMo)
4. Redirect user to gateway
5. User pays
6. Gateway calls /api/payments/{provider}/callback
7. Validate signature
8. Update order_status = CONFIRMED
9. Send email confirmation
10. Return success/failure to user
```

**Payment Types:**
- COD (Cash on Delivery)
- Credit Card
- Debit Card
- E-Wallet (MoMo, ZaloPay)
- Bank Transfer
- VNPay

**Entities:** UserPaymentMethod, PaymentType  
**Priority:** 🟡 MEDIUM (có thể dùng COD tạm)

---

## 7️⃣ PROMOTION & DISCOUNT

### ❌ Chưa Triển Khai (10/10 APIs)

#### Admin Promotion Management (7 APIs)
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| POST | `/api/admin/promotions` | Tạo khuyến mãi | `{name, description, discountRate, startDate, endDate}` |
| PUT | `/api/admin/promotions/{id}` | Cập nhật | `{...}` |
| DELETE | `/api/admin/promotions/{id}` | Xóa | - |
| GET | `/api/admin/promotions` | Danh sách | `?status=ACTIVE` |
| POST | `/api/admin/promotions/{id}/categories` | Áp dụng cho categories | `{categoryIds[]}` |
| DELETE | `/api/admin/promotions/{id}/categories/{categoryId}` | Gỡ category | - |
| PUT | `/api/admin/promotions/{id}/activate` | Kích hoạt/Tắt | `{isActive}` |

**Promotion Request:**
```json
{
  "name": "Summer Sale 2026",
  "description": "Giảm 20% toàn bộ áo thun",
  "discountRate": 20,
  "startDate": "2026-06-01T00:00:00",
  "endDate": "2026-08-31T23:59:59"
}
```

#### User View Promotions (3 APIs)
| Method | Endpoint | Chức năng |
|--------|----------|-----------|
| GET | `/api/promotions/active` | Khuyến mãi đang chạy |
| GET | `/api/products/{id}/promotion` | Kiểm tra sản phẩm có KM? |
| GET | `/api/categories/{id}/promotion` | KM của category |

**Apply Logic (trong checkout):**
```java
1. Lấy categoryId của product
2. Check: có promotion active cho category này?
3. If yes:
   - discounted_price = price * (100 - discount_rate) / 100
   - Lưu vào order_line: original_price, discount_rate, final_price
```

**Entities:** Promotion, PromotionCategory  
**Priority:** 🟡 MEDIUM

---

## 8️⃣ PRODUCT REVIEW & RATING

### ❌ Chưa Triển Khai (8/8 APIs)

#### User Reviews (5 APIs)
| Method | Endpoint | Chức năng | Request Body |
|--------|----------|-----------|--------------|
| POST | `/api/reviews` | Viết đánh giá | `{orderedProductId, ratingValue, comment}` |
| PUT | `/api/reviews/{id}` | Sửa đánh giá | `{ratingValue, comment}` |
| DELETE | `/api/reviews/{id}` | Xóa đánh giá | - |
| GET | `/api/products/{id}/reviews` | Đánh giá của sản phẩm | `?page=0&rating=5` |
| GET | `/api/users/me/reviews` | Đánh giá của tôi | - |

**Review Request:**
```json
{
  "orderedProductId": 500,  // order_line.id
  "ratingValue": 5,         // 1-5 stars
  "comment": "Chất lượng rất tốt, đúng mô tả"
}
```

**Validation:**
- User phải đã mua sản phẩm (có trong order_line)
- Order_status phải là DELIVERED hoặc COMPLETED
- Mỗi order_line chỉ review được 1 lần

#### Admin Moderation (3 APIs)
| Method | Endpoint | Chức năng |
|--------|----------|-----------|
| GET | `/api/admin/reviews` | Tất cả reviews |
| DELETE | `/api/admin/reviews/{id}` | Xóa review spam |
| PUT | `/api/admin/reviews/{id}/hide` | Ẩn/hiện review |

**Entities:** UserReview  
**Priority:** 🟢 LOW (Nice to have)

---

## 9️⃣ INVENTORY MANAGEMENT

### ❌ Chưa Triển Khai (6/6 APIs)

| Method | Endpoint | Chức năng | Query Params |
|--------|----------|-----------|--------------|
| GET | `/api/admin/inventory/low-stock` | Sản phẩm sắp hết | `?threshold=10` |
| GET | `/api/admin/inventory/out-of-stock` | Hết hàng | - |
| PUT | `/api/admin/product-items/{id}/stock` | Cập nhật tồn kho | `{qty, note}` |
| POST | `/api/admin/inventory/bulk-update` | Cập nhật hàng loạt | `[{id, qty}]` |
| GET | `/api/admin/inventory/history` | Lịch sử xuất nhập | `?productItemId=` |
| POST | `/api/admin/inventory/import` | Nhập hàng | `{items[], supplierId}` |

**Low Stock Response:**
```json
{
  "data": [
    {
      "id": 1001,
      "sku": "ATCT-RED-M",
      "productName": "Áo thun cổ tròn",
      "qtyInStock": 5,
      "reorderLevel": 10,
      "status": "LOW_STOCK"
    }
  ]
}
```

**Inventory Transaction Log (cần thêm bảng):**
```sql
CREATE TABLE inventory_transaction (
  id INT PRIMARY KEY AUTO_INCREMENT,
  product_item_id INT,
  transaction_type ENUM('IN', 'OUT', 'ADJUST'),
  qty_change INT,  -- Dương: nhập, Âm: xuất
  qty_before INT,
  qty_after INT,
  reference_type VARCHAR(50),  -- 'ORDER', 'IMPORT', 'MANUAL'
  reference_id INT,
  note VARCHAR(500),
  created_by INT,
  created_at DATETIME
);
```

**Priority:** 🟡 MEDIUM

---

## 🔟 ANALYTICS & REPORTS

### ❌ Chưa Triển Khai (10/10 APIs)

#### Sales Analytics (5 APIs)
| Method | Endpoint | Chức năng | Query Params |
|--------|----------|-----------|--------------|
| GET | `/api/admin/analytics/sales` | Doanh thu | `?from=2026-01-01&to=2026-12-31` |
| GET | `/api/admin/analytics/revenue-by-category` | Doanh thu theo danh mục | - |
| GET | `/api/admin/analytics/revenue-by-month` | Doanh thu theo tháng | `?year=2026` |
| GET | `/api/admin/analytics/daily-sales` | Doanh thu hàng ngày | `?from=&to=` |
| GET | `/api/admin/analytics/top-products` | Sản phẩm bán chạy | `?limit=10&from=&to=` |

**Sales Response:**
```json
{
  "data": {
    "totalRevenue": 50000000,
    "totalOrders": 250,
    "averageOrderValue": 200000,
    "totalCustomers": 180,
    "conversionRate": 3.5,
    "chartData": [
      {"date": "2026-01-01", "revenue": 1500000, "orders": 10},
      {"date": "2026-01-02", "revenue": 2000000, "orders": 15}
    ]
  }
}
```

#### Product Analytics (3 APIs)
| Method | Endpoint | Chức năng |
|--------|----------|-----------|
| GET | `/api/admin/analytics/product-performance` | Hiệu suất sản phẩm |
| GET | `/api/admin/analytics/category-performance` | Hiệu suất danh mục |
| GET | `/api/admin/analytics/inventory-turnover` | Vòng quay hàng tồn |

#### Customer Analytics (2 APIs)
| Method | Endpoint | Chức năng |
|--------|----------|-----------|
| GET | `/api/admin/analytics/customer-lifetime-value` | Giá trị khách hàng |
| GET | `/api/admin/analytics/customer-segments` | Phân khúc khách hàng |

**Priority:** 🟢 LOW (sau khi có data)

---

## 1️⃣1️⃣ FILE MANAGEMENT

### ✅ Đã Triển Khai (1/3 APIs)

| Method | Endpoint | Chức năng | Status |
|--------|----------|-----------|--------|
| POST | `/api/files/upload` | Upload ảnh | ✅ Done |
| DELETE | `/api/files/{filename}` | Xóa file | ❌ Missing |
| GET | `/uploads/images/{filename}` | Serve static file | ✅ Done |

**Upload Request:**
```
POST /api/files/upload
Content-Type: multipart/form-data

file: (binary)
```

**Upload Response:**
```json
{
  "success": true,
  "data": {
    "fileName": "1234567890_ao-thun.jpg",
    "fileUrl": "/uploads/images/1234567890_ao-thun.jpg",
    "fileSize": 125600,
    "contentType": "image/jpeg"
  }
}
```

**Current Implementation:**
- Lưu local disk: `/uploads/images/`
- Max file size: 10MB
- Allowed types: JPG, PNG, GIF, WEBP

**Should Improve:**
- ❌ Upload to cloud storage (S3, Cloudinary)
- ❌ Image resize/optimization
- ❌ CDN integration
- ❌ Cleanup orphaned images

---

## 1️⃣2️⃣ ADMIN USER MANAGEMENT

### ❌ Chưa Triển Khai (7/7 APIs)

| Method | Endpoint | Chức năng | Request |
|--------|----------|-----------|---------|
| GET | `/api/admin/users` | Danh sách users | `?role=USER&page=0` |
| GET | `/api/admin/users/{id}` | Chi tiết user | - |
| PUT | `/api/admin/users/{id}/role` | Thay đổi role | `{role: "ADMIN"}` |
| PUT | `/api/admin/users/{id}/status` | Ban/Unban user | `{isActive: false}` |
| GET | `/api/admin/users/{id}/orders` | Đơn hàng của user | - |
| DELETE | `/api/admin/users/{id}` | Xóa user | - |
| POST | `/api/admin/users` | Tạo user (admin) | `{username, email, password, role}` |

**User List Response:**
```json
{
  "data": {
    "content": [
      {
        "id": 123,
        "username": "john_doe",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true,
        "totalOrders": 15,
        "totalSpent": 3000000,
        "createdAt": "2025-01-15T10:30:00"
      }
    ],
    "totalElements": 250,
    "totalPages": 25
  }
}
```

**Priority:** 🟡 MEDIUM

---

## 1️⃣3️⃣ MASTER DATA APIs

### ❌ Chưa Triển Khai (5/5 APIs)

| Method | Endpoint | Chức năng | Response |
|--------|----------|-----------|----------|
| GET | `/api/order-statuses` | Danh sách trạng thái đơn | `[{id, status}]` |
| GET | `/api/shipping-methods` | Phương thức vận chuyển | `[{id, name, price}]` |
| GET | `/api/payment-types` | Loại thanh toán | `[{id, value}]` |
| GET | `/api/countries` | Danh sách quốc gia | `[{id, countryName}]` |
| GET | `/api/provinces` | Tỉnh/Thành phố | `?countryId=1` |

**Entities:** OrderStatus, ShippingMethod, PaymentType, Country  
**Priority:** 🔴 HIGH (cần cho checkout)

---

## 📊 BẢNG TỔNG HỢP

### Theo Module

| Module | Implemented | Missing | Total | Completion |
|--------|-------------|---------|-------|------------|
| Authentication | 2 | 3 | 5 | 40% |
| User Management | 0 | 10 | 10 | 0% |
| Product Catalog | 29 | 0 | 29 | 100% ✅ |
| Shopping Cart | 0 | 6 | 6 | 0% |
| Order Management | 0 | 15 | 15 | 0% |
| Payment | 0 | 8 | 8 | 0% |
| Promotion | 0 | 10 | 10 | 0% |
| Reviews | 0 | 8 | 8 | 0% |
| Inventory | 0 | 6 | 6 | 0% |
| Analytics | 0 | 10 | 10 | 0% |
| File Management | 1 | 2 | 3 | 33% |
| Admin Users | 0 | 7 | 7 | 0% |
| Master Data | 0 | 5 | 5 | 0% |
| **TOTAL** | **42** | **63** | **105** | **40%** |

### Theo Priority

| Priority | APIs Count | Modules |
|----------|------------|---------|
| 🔴 HIGH | 36 APIs | Shopping Cart, Order Management, User Profile/Address, Master Data |
| 🟡 MEDIUM | 27 APIs | Payment, Promotion, Inventory, Admin Users |
| 🟢 LOW | 18 APIs | Reviews, Analytics |

---

## 🎯 IMPLEMENTATION ROADMAP

### Sprint 1: Core E-commerce (2 weeks) 🔴
**Mục tiêu:** User có thể mua hàng

1. **Shopping Cart** (6 APIs)
2. **Order Checkout** (3 APIs)
3. **User Address** (6 APIs)
4. **Master Data** (5 APIs)
5. **User Profile** (3 APIs)

**Total:** 23 APIs

### Sprint 2: Order Management (1 week) 🔴
1. **User Order Management** (5 APIs)
2. **Admin Order Management** (7 APIs)

**Total:** 12 APIs

### Sprint 3: Payment (1 week) 🟡
1. **Payment Methods** (4 APIs)
2. **Payment Processing** (4 APIs)

**Total:** 8 APIs

### Sprint 4: Promotion & Reviews (1 week) 🟡
1. **Promotion** (10 APIs)
2. **Reviews** (8 APIs)

**Total:** 18 APIs

### Sprint 5: Advanced Features (1 week) 🟢
1. **Inventory** (6 APIs)
2. **Admin Users** (7 APIs)
3. **Analytics** (10 APIs)

**Total:** 23 APIs

---

## 🔧 TECHNICAL REQUIREMENTS

### Dependencies cần thêm
```xml
<!-- Redis Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- AWS S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
</dependency>

<!-- Scheduling -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### Database migrations cần thêm
```sql
-- Thêm columns
ALTER TABLE product_item ADD COLUMN reserved_qty INT DEFAULT 0;
ALTER TABLE product_item ADD COLUMN reorder_level INT DEFAULT 10;
ALTER TABLE site_user ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- Thêm indexes
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_order_user ON shop_order(user_id);
CREATE INDEX idx_order_status ON shop_order(order_status);
CREATE INDEX idx_order_date ON shop_order(order_date);

-- Thêm bảng mới
CREATE TABLE inventory_transaction (...);
CREATE TABLE email_log (...);
CREATE TABLE payment_transaction (...);
```

---

**Tài liệu này được tạo bởi GitHub Copilot - March 17, 2026**

