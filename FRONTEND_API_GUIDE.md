# 📘 HƯỚNG DẪN TÍCH HỢP API - CLOTHING STORE
## Dành cho Frontend Developer

> **Version:** 1.2  
> **Last Updated:** March 29, 2026  
> **Base URL:** `http://160.30.113.40:8080`  
> **Swagger UI:** `http://160.30.113.40:8080/swagger-ui.html`

---

## 📋 MỤC LỤC

1. [Tổng Quan Hệ Thống](#1-tổng-quan-hệ-thống)
2. [Authentication & Authorization](#2-authentication--authorization)
3. [Product Catalog APIs](#3-product-catalog-apis)
4. [Shopping Cart APIs](#4-shopping-cart-apis)
5. [Order Management APIs](#5-order-management-apis)
6. [User Address Management](#6-user-address-management)
7. [Support APIs](#7-support-apis)
8. [Error Handling](#8-error-handling)
9. [Best Practices](#9-best-practices)
10. [Product Reviews APIs](#10-product-reviews-apis) ⭐ NEW
11. [AI Chat Bot APIs](#11-ai-chat-bot-apis) 🤖 NEW
12. [Product Management APIs (Admin)](#12-product-management-apis-admin) 🛠️ NEW

---

## 1. TỔNG QUAN HỆ THỐNG

### 🎯 Kiến Trúc API
- **Framework:** Spring Boot 3.5.11
- **Database:** MySQL 8.0
- **Authentication:** JWT (JSON Web Token)
- **API Style:** RESTful
- **Response Format:** JSON

### 🔐 Phân Quyền
| Loại Endpoint | Quyền Truy Cập |
|---------------|----------------|
| `POST /api/auth/**` | Public (không cần token) |
| `GET /api/products/**` | Public |
| `GET /api/categories/**` | Public |
| `GET /api/colors/**` | Public |
| `GET /api/sizes/**` | Public |
| `GET /api/payment-types/**` | Public |
| `GET /api/shipping-methods/**` | Public |
| `GET /api/reviews/product/**` | Public |
| **`POST /api/chat/message`** | **Public (không cần token) 🤖** |
| `/api/cart/**` | Authenticated USER |
| `/api/orders/**` | Authenticated USER |
| `/api/addresses/**` | Authenticated USER |
| `POST /api/reviews` | Authenticated USER |
| `GET /api/reviews/my` | Authenticated USER |
| `DELETE /api/reviews/{id}` | Authenticated USER |
| `DELETE /api/chat/session/{id}` | Authenticated USER |
| `POST/PUT/DELETE /api/products/**` | Authenticated + ADMIN |
| `POST /api/products/full` | Authenticated + ADMIN |
| `PUT /api/products/full/{id}` | Authenticated + ADMIN |
| `GET /api/products/full/{id}` | Public |
| `GET /api/orders/admin/**` | Authenticated + ADMIN |

### 📦 Cấu Trúc Response Chuẩn

#### Success Response
```json
{
  "success": true,
  "message": "Thành công",
  "data": {
    // Dữ liệu trả về
  }
}
```

#### Paginated Response
```json
{
  "success": true,
  "message": null,
  "data": {
    "content": [...],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

#### Error Response
```json
{
  "success": false,
  "message": "Lỗi chi tiết",
  "data": null
}
```

---

## 2. AUTHENTICATION & AUTHORIZATION

### 🔑 2.1. Đăng Ký Tài Khoản

**Endpoint:** `POST /api/auth/register`  
**Auth Required:** ❌ No

#### Request Body
```json
{
  "username": "john_doe",
  "emailAddress": "john@example.com",
  "phoneNumber": "0901234567",
  "password": "password123"
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "john_doe",
    "emailAddress": "john@example.com",
    "role": "USER"
  }
}
```

#### Validation Rules
- `username`: Bắt buộc, 3-50 ký tự
- `emailAddress`: Email hợp lệ
- `password`: Bắt buộc, tối thiểu 6 ký tự
- `phoneNumber`: Không bắt buộc

---

### 🔓 2.2. Đăng Nhập

**Endpoint:** `POST /api/auth/login`  
**Auth Required:** ❌ No

#### Request Body
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "john_doe",
    "emailAddress": "john@example.com",
    "role": "USER"
  }
}
```

#### Frontend Implementation
```javascript
// 1. Lưu token vào localStorage/sessionStorage
localStorage.setItem('accessToken', response.data.accessToken);
localStorage.setItem('user', JSON.stringify(response.data));

// 2. Thêm token vào mọi request tiếp theo
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

// hoặc với fetch
fetch('/api/cart', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

### 🔒 2.3. Đổi Mật Khẩu

**Endpoint:** `POST /api/auth/change-password`  
**Auth Required:** ✅ Yes

#### Request Body
```json
{
  "oldPassword": "password123",
  "newPassword": "newpassword456"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": ""
}
```

---

## 3. PRODUCT CATALOG APIs

### 🏷️ 3.1. Categories (Danh Mục)

#### 3.1.1. Lấy Tất Cả Danh Mục
**Endpoint:** `GET /api/categories`  
**Auth Required:** ❌ No

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Áo",
      "slug": "ao",
      "parentId": null,
      "description": "Danh mục áo các loại",
      "createdAt": "2026-03-20T10:00:00"
    },
    {
      "id": 2,
      "name": "Áo Thun",
      "slug": "ao-thun",
      "parentId": 1,
      "description": "Áo thun nam nữ",
      "createdAt": "2026-03-20T10:05:00"
    }
  ]
}
```

#### 3.1.2. Lấy Danh Mục Gốc (Root Categories)
**Endpoint:** `GET /api/categories/roots`

**Response:** Danh sách categories có `parentId = null`

#### 3.1.3. Lấy Danh Mục Con
**Endpoint:** `GET /api/categories/{id}/children`

**Example:** `GET /api/categories/1/children` → Lấy tất cả danh mục con của "Áo"

#### 3.1.4. Lấy Danh Mục Theo Slug
**Endpoint:** `GET /api/categories/slug/{slug}`

**Example:** `GET /api/categories/slug/ao-thun`

---

### 🎨 3.2. Colors (Màu Sắc)

#### Lấy Tất Cả Màu Sắc
**Endpoint:** `GET /api/colors`  
**Auth Required:** ❌ No

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Đỏ",
      "hexCode": "#FF0000"
    },
    {
      "id": 2,
      "name": "Xanh Dương",
      "hexCode": "#0000FF"
    }
  ]
}
```

---

### 📏 3.3. Sizes (Kích Cỡ)

#### 3.3.1. Lấy Tất Cả Sizes
**Endpoint:** `GET /api/sizes`  
**Auth Required:** ❌ No

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "S",
      "type": "clothing",
      "sortOrder": 1
    },
    {
      "id": 2,
      "name": "M",
      "type": "clothing",
      "sortOrder": 2
    },
    {
      "id": 5,
      "name": "38",
      "type": "shoes",
      "sortOrder": 1
    }
  ]
}
```

#### 3.3.2. Lấy Sizes Theo Loại
**Endpoint:** `GET /api/sizes/type/{type}`

**Types:** `clothing`, `numeric`, `shoes`

**Example:** `GET /api/sizes/type/clothing` → Chỉ lấy S, M, L, XL, XXL

---

### 🛍️ 3.4. Products (Sản Phẩm)

#### 3.4.1. Lấy Danh Sách Sản Phẩm (Có Phân Trang)
**Endpoint:** `GET /api/products/paged`  
**Auth Required:** ❌ No

**Query Parameters:**
- `page` (int): Số trang, bắt đầu từ 0. Default: 0
- `size` (int): Số sản phẩm mỗi trang. Default: 10
- `sortBy` (string): Trường sắp xếp. Default: "id"
- `direction` (string): "ASC" hoặc "DESC". Default: "ASC"

**Example:** `GET /api/products/paged?page=0&size=12&sortBy=name&direction=ASC`

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Áo Thun Basic Cotton",
        "slug": "ao-thun-basic-cotton",
        "description": "Áo thun cotton 100%, thoáng mát",
        "categoryId": 2,
        "categoryName": "Áo Thun",
        "basePrice": 199000,
        "brand": "Uniqlo",
        "material": "Cotton 100%",
        "isActive": true,
        "thumbnailUrl": "/uploads/images/ao-thun-red.jpg",
        "createdAt": "2026-03-20T10:00:00",
        "updatedAt": "2026-03-21T15:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 12,
    "totalElements": 100,
    "totalPages": 9,
    "first": true,
    "last": false
  }
}
```

#### 3.4.2. Lấy Sản Phẩm Theo ID
**Endpoint:** `GET /api/products/{id}`

**Example:** `GET /api/products/1`

#### 3.4.3. Lấy Sản Phẩm Theo Slug
**Endpoint:** `GET /api/products/slug/{slug}`

**Example:** `GET /api/products/slug/ao-thun-basic-cotton`

#### 3.4.4. Tìm Kiếm Sản Phẩm (Nâng Cao)
**Endpoint:** `GET /api/products/search`  
**Auth Required:** ❌ No

**Query Parameters:**
- `name` (string): Tìm theo tên sản phẩm (tìm gần đúng)
- `categoryIds` (array): Lọc theo nhiều danh mục. VD: `categoryIds=1&categoryIds=2`
- `minPrice` (decimal): Giá tối thiểu
- `maxPrice` (decimal): Giá tối đa
- `colorIds` (array): Lọc theo màu sắc. VD: `colorIds=1&colorIds=3`
- `isActive` (boolean): Lọc sản phẩm active/inactive
- `page`, `size`, `sortBy`, `direction`: Giống phân trang

**Example:**
```
GET /api/products/search?name=áo&categoryIds=1&categoryIds=2&minPrice=100000&maxPrice=500000&colorIds=1&page=0&size=20&sortBy=basePrice&direction=ASC
```

**Response:** Giống như phân trang thông thường

---

### 🎨 3.5. Product Variants (Biến Thể Theo Màu)

#### 3.5.1. Lấy Tất Cả Biến Thể Của Sản Phẩm
**Endpoint:** `GET /api/product-variants/product/{productId}`  
**Auth Required:** ❌ No

**Example:** `GET /api/product-variants/product/1`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Áo Thun Basic Cotton",
      "colorId": 1,
      "colorName": "Đỏ",
      "colorHex": "#FF0000",
      "variantImage": "/uploads/images/ao-thun-red.jpg",
      "isActive": true
    },
    {
      "id": 2,
      "productId": 1,
      "productName": "Áo Thun Basic Cotton",
      "colorId": 2,
      "colorName": "Xanh Dương",
      "colorHex": "#0000FF",
      "variantImage": "/uploads/images/ao-thun-blue.jpg",
      "isActive": true
    }
  ]
}
```

#### 3.5.2. Lấy Biến Thể Theo ID
**Endpoint:** `GET /api/product-variants/{id}`

---

### 📦 3.6. Variant Stocks (Tồn Kho Theo Size)

#### 3.6.1. Lấy Tồn Kho Của Một Biến Thể
**Endpoint:** `GET /api/variant-stocks/variant/{variantId}`  
**Auth Required:** ❌ No

**Example:** `GET /api/variant-stocks/variant/1` (Lấy tồn kho của áo màu đỏ)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "variantId": 1,
      "sizeId": 1,
      "sizeName": "S",
      "sku": "NIKE-RED-S",
      "qtyInStock": 10,
      "priceOverride": null,
      "productName": "Áo Thun Basic Cotton",
      "colorName": "Đỏ",
      "finalPrice": 199000
    },
    {
      "id": 2,
      "variantId": 1,
      "sizeId": 2,
      "sizeName": "M",
      "sku": "NIKE-RED-M",
      "qtyInStock": 5,
      "priceOverride": 220000,
      "productName": "Áo Thun Basic Cotton",
      "colorName": "Đỏ",
      "finalPrice": 220000
    },
    {
      "id": 3,
      "variantId": 1,
      "sizeId": 3,
      "sizeName": "L",
      "sku": "NIKE-RED-L",
      "qtyInStock": 0,
      "priceOverride": null,
      "productName": "Áo Thun Basic Cotton",
      "colorName": "Đỏ",
      "finalPrice": 199000
    }
  ]
}
```

**Lưu ý:**
- `finalPrice` = `priceOverride` nếu có, ngược lại = `product.basePrice`
- Nếu `qtyInStock = 0` → Hiển thị "Hết hàng" và disable nút "Thêm vào giỏ"

#### 3.6.2. Lấy Tồn Kho Theo ID
**Endpoint:** `GET /api/variant-stocks/{id}`

---

### 🎯 3.7. Luồng Hiển Thị Trang Chi Tiết Sản Phẩm

#### Frontend Flow:
```
1. GET /api/products/slug/{slug}
   → Lấy thông tin sản phẩm cơ bản (name, description, basePrice, categoryId)

2. GET /api/product-variants/product/{productId}
   → Lấy tất cả màu sắc có sẵn của sản phẩm
   → Hiển thị color picker

3. Khi user chọn màu (colorId = X):
   GET /api/variant-stocks/variant/{variantId}
   → Lấy tất cả size có sẵn của màu đó
   → Hiển thị size selector với số lượng tồn kho

4. User chọn size → Lấy variantStockId tương ứng
   → Hiển thị giá cuối cùng (finalPrice)
   → Enable nút "Thêm vào giỏ hàng"
```

#### Example Code (React/Vue)
```javascript
// 1. Fetch product
const product = await api.get(`/products/slug/${slug}`);

// 2. Fetch variants (colors)
const variants = await api.get(`/product-variants/product/${product.data.id}`);
setAvailableColors(variants.data);

// 3. When user selects a color
const handleColorChange = async (variantId) => {
  const stocks = await api.get(`/variant-stocks/variant/${variantId}`);
  setAvailableSizes(stocks.data);
};

// 4. When user selects size
const handleSizeChange = (stockId) => {
  const selectedStock = availableSizes.find(s => s.id === stockId);
  setFinalPrice(selectedStock.finalPrice);
  setCanAddToCart(selectedStock.qtyInStock > 0);
};
```

---

## 4. SHOPPING CART APIs

### 🛒 4.1. Xem Giỏ Hàng

**Endpoint:** `GET /api/cart`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "data": {
    "cartId": 1,
    "userId": 10,
    "items": [
      {
        "id": 1,
        "variantStockId": 5,
        "qty": 2,
        "productName": "Áo Thun Basic Cotton",
        "colorName": "Đỏ",
        "sizeName": "M",
        "sku": "NIKE-RED-M",
        "price": 220000,
        "subtotal": 440000,
        "imageUrl": "/uploads/images/ao-thun-red.jpg",
        "availableStock": 5
      }
    ],
    "totalItems": 2,
    "totalAmount": 440000
  }
}
```

---

### ➕ 4.2. Thêm Sản Phẩm Vào Giỏ

**Endpoint:** `POST /api/cart/items`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "variantStockId": 5,
  "qty": 2
}
```

**Response:** Trả về thông tin giỏ hàng sau khi thêm (giống GET /api/cart)

**Validation:**
- `variantStockId`: Phải tồn tại
- `qty`: Phải >= 1
- Tổng số lượng không vượt quá `qtyInStock`

**Behavior:**
- Nếu sản phẩm đã có trong giỏ → Cộng thêm số lượng
- Nếu chưa có → Tạo mới cart item

---

### ✏️ 4.3. Cập Nhật Số Lượng

**Endpoint:** `PUT /api/cart/items/{itemId}`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "variantStockId": 5,
  "qty": 3
}
```

**Response:** Giỏ hàng sau khi cập nhật

**Note:** Thay đổi số lượng sang 0 → Sử dụng DELETE thay vì PUT

---

### 🗑️ 4.4. Xóa Sản Phẩm Khỏi Giỏ

**Endpoint:** `DELETE /api/cart/items/{itemId}`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Xoa san pham khoi gio thanh cong",
  "data": { /* cart summary */ }
}
```

---

### 🧹 4.5. Làm Trống Giỏ Hàng

**Endpoint:** `DELETE /api/cart`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Lam trong gio hang thanh cong",
  "data": null
}
```

---

## 5. ORDER MANAGEMENT APIs

### 📋 5.1. Đặt Hàng (Place Order)

**Endpoint:** `POST /api/orders`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "paymentTypeId": 1,
  "shippingAddressId": 2,
  "shippingMethodId": 1,
  "note": "Giao giờ hành chính"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Dat hang thanh cong",
  "data": {
    "id": 100,
    "orderCode": "ORD-20260324-100",
    "userId": 10,
    "orderDate": "2026-03-24T14:30:00",
    "statusId": 1,
    "statusName": "PENDING",
    "paymentTypeId": 1,
    "paymentTypeName": "Chuyển khoản ngân hàng",
    "qrUrl": "https://img.vietqr.io/image/...",
    "bankInfo": {
      "id": 1,
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "NGUYEN VAN A",
      "branch": "Chi nhánh Hà Nội"
    },
    "shippingMethodId": 1,
    "shippingMethodName": "Giao hàng nhanh",
    "shippingFee": 30000,
    "shippingAddressId": 2,
    "shippingAddressDetail": {
      "id": 2,
      "unitNumber": "101",
      "streetNumber": "25",
      "addressLine1": "Đường Lê Văn Lương",
      "city": "Hà Nội",
      "region": "Thanh Xuân",
      "postalCode": "100000",
      "countryId": 1,
      "isDefault": true
    },
    "subtotal": 440000,
    "orderTotal": 470000,
    "items": [
      {
        "id": 1,
        "productName": "Áo Thun Basic Cotton",
        "colorName": "Đỏ",
        "sizeName": "M",
        "qty": 2,
        "price": 220000,
        "subtotal": 440000
      }
    ]
  }
}
```

**Behavior:**
1. Lấy toàn bộ sản phẩm từ giỏ hàng
2. Tạo đơn hàng với trạng thái PENDING
3. Trừ tồn kho (`qtyInStock -= qty`)
4. Làm trống giỏ hàng
5. Nếu `paymentType = "Chuyển khoản ngân hàng"` → Tạo QR code thanh toán

**QR Code URL Format:**
```
https://img.vietqr.io/image/{bankCode}-{accountNumber}-compact2.jpg?amount={amount}&addInfo={orderCode}&accountName={accountName}
```

---

### 📜 5.2. Xem Lịch Sử Đơn Hàng

**Endpoint:** `GET /api/orders`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 100,
      "orderCode": "ORD-20260324-100",
      "orderDate": "2026-03-24T14:30:00",
      "statusName": "PENDING",
      "orderTotal": 470000,
      "items": [...]
    },
    {
      "id": 99,
      "orderCode": "ORD-20260320-099",
      "orderDate": "2026-03-20T10:15:00",
      "statusName": "DELIVERED",
      "orderTotal": 650000,
      "items": [...]
    }
  ]
}
```

**Note:** Sắp xếp mới nhất trước (ORDER BY orderDate DESC)

---

### 🔍 5.3. Xem Chi Tiết Đơn Hàng

**Endpoint:** `GET /api/orders/{orderId}`  
**Auth Required:** ✅ Yes (USER)

**Example:** `GET /api/orders/100`

**Response:** Giống như response của POST /api/orders

**Security:** User chỉ xem được đơn hàng của chính mình

---

### ❌ 5.4. Hủy Đơn Hàng

**Endpoint:** `PATCH /api/orders/{orderId}/cancel`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Huy don hang thanh cong",
  "data": { /* order detail với statusName = "CANCELLED" */ }
}
```

**Validation:**
- Chỉ hủy được đơn hàng có trạng thái PENDING
- Tự động hoàn trả tồn kho (`qtyInStock += qty`)

---

### 👨‍💼 5.5. Admin APIs (ADMIN ONLY)

#### 5.5.1. Lấy Tất Cả Đơn Hàng (Phân Trang)
**Endpoint:** `GET /api/orders/admin/all?page=0&size=20`  
**Auth Required:** ✅ Yes (ADMIN)

#### 5.5.2. Lọc Đơn Hàng Theo Trạng Thái
**Endpoint:** `GET /api/orders/admin/by-status/{statusId}?page=0&size=20`
**Auth Required:** ✅ Yes (ADMIN)

**Status IDs:**
- 1 = PENDING
- 2 = PROCESSING
- 3 = SHIPPED
- 4 = DELIVERED
- 5 = CANCELLED

#### 5.5.3. Cập Nhật Trạng Thái Đơn Hàng
**Endpoint:** `PATCH /api/orders/admin/{orderId}/status`
**Auth Required:** ✅ Yes (ADMIN)

**Request Body:**
```json
{
  "statusId": 2
}
```

**Response:**
```json
{
  "success": true,
  "message": "Cap nhat trang thai don hang thanh cong",
  "data": { /* order detail */ }
}
```

---

## 6. USER ADDRESS MANAGEMENT

### 📍 6.1. Lấy Danh Sách Địa Chỉ

**Endpoint:** `GET /api/addresses`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "unitNumber": "101",
      "streetNumber": "25",
      "addressLine1": "Đường Lê Văn Lương",
      "addressLine2": null,
      "city": "Hà Nội",
      "region": "Thanh Xuân",
      "postalCode": "100000",
      "countryId": 1,
      "isDefault": true
    },
    {
      "id": 2,
      "unitNumber": "202",
      "streetNumber": "50",
      "addressLine1": "Đường Nguyễn Trãi",
      "addressLine2": null,
      "city": "TP.HCM",
      "region": "Quận 1",
      "postalCode": "700000",
      "countryId": 1,
      "isDefault": false
    }
  ],
  "timestamp": "2026-03-25T10:30:00Z"
}
```

---

### ➕ 6.2. Thêm Địa Chỉ Mới

**Endpoint:** `POST /api/addresses`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "unitNumber": "101",
  "streetNumber": "25",
  "addressLine1": "Đường Lê Văn Lương",
  "addressLine2": "Khu đô thị mới",
  "city": "Hà Nội",
  "region": "Thanh Xuân",
  "postalCode": "100000",
  "countryId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Them dia chi thanh cong",
  "data": { 
    "id": 3,
    "unitNumber": "101",
    "streetNumber": "25",
    "addressLine1": "Đường Lê Văn Lương",
    "addressLine2": "Khu đô thị mới",
    "city": "Hà Nội",
    "region": "Thanh Xuân",
    "postalCode": "100000",
    "countryId": 1,
    "isDefault": true
  },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Note:** Địa chỉ đầu tiên tự động được đặt làm mặc định (`isDefault = true`)

---

### ✏️ 6.3. Cập Nhật Địa Chỉ

**Endpoint:** `PUT /api/addresses/{addressId}`  
**Auth Required:** ✅ Yes (USER)

**Request Body:** Giống như POST

---

### 🗑️ 6.4. Xóa Địa Chỉ

**Endpoint:** `DELETE /api/addresses/{addressId}`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Xoa dia chi thanh cong",
  "data": null,
  "timestamp": "2026-03-25T10:30:00Z"
}
```

---

### ⭐ 6.5. Đặt Địa Chỉ Mặc Định

**Endpoint:** `PATCH /api/addresses/{addressId}/default`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Da dat dia chi mac dinh",
  "data": { 
    "id": 2,
    "unitNumber": "202",
    "streetNumber": "50",
    "addressLine1": "Đường Nguyễn Trãi",
    "addressLine2": null,
    "city": "TP.HCM",
    "region": "Quận 1",
    "postalCode": "700000",
    "countryId": 1,
    "isDefault": true
  },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Note:** Chỉ có 1 địa chỉ mặc định. Địa chỉ cũ tự động bỏ mặc định (`isDefault = false`).

---

## 7. USER PAYMENT METHODS

### 💳 7.1. Lấy Danh Sách Phương Thức Thanh Toán

**Endpoint:** `GET /api/payment-methods`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 10,
      "paymentTypeId": 3,
      "provider": "VNPAY",
      "accountNumber": "0901234567",
      "expiryDate": "12/25",
      "isDefault": true
    }
  ],
  "timestamp": "2026-03-25T10:30:00Z"
}
```

---

### ➕ 7.2. Thêm Phương Thức Thanh Toán

**Endpoint:** `POST /api/payment-methods`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "paymentTypeId": 3,
  "provider": "VNPAY",
  "accountNumber": "0901234567",
  "expiryDate": "12/25"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Them phuong thuc thanh toan thanh cong",
  "data": {
    "id": 1,
    "userId": 10,
    "paymentTypeId": 3,
    "provider": "VNPAY",
    "accountNumber": "0901234567",
    "expiryDate": "12/25",
    "isDefault": true
  },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Note:** Phương thức đầu tiên tự động là default

---

### ✏️ 7.3. Cập Nhật Phương Thức Thanh Toán

**Endpoint:** `PUT /api/payment-methods/{id}`  
**Auth Required:** ✅ Yes (USER)

---

### 🗑️ 7.4. Xóa Phương Thức Thanh Toán

**Endpoint:** `DELETE /api/payment-methods/{id}`  
**Auth Required:** ✅ Yes (USER)

---

### ⭐ 7.5. Đặt Phương Thức Mặc Định

**Endpoint:** `PATCH /api/payment-methods/{id}/default`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Da dat lam phuong thuc mac dinh",
  "data": { /* payment method với isDefault = true */ },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

---

## 8. FILE UPLOAD API

### 📤 8.1. Upload Một Ảnh

**Endpoint:** `POST /api/files/image`  
**Auth Required:** ✅ Yes  
**Content-Type:** `multipart/form-data`

**Request:**
```bash
POST /api/files/image
Content-Type: multipart/form-data

file: [chọn file ảnh]
```

**Supported formats:** JPEG, PNG, GIF, WEBP, SVG  
**Max size:** 10MB

**Response:**
```json
{
  "success": true,
  "message": "Upload anh thanh cong",
  "data": {
    "url": "/uploads/images/1711344600000_product.jpg",
    "fileName": "1711344600000_product.jpg",
    "fileSize": 245678,
    "contentType": "image/jpeg"
  },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Frontend Example (JavaScript):**
```javascript
async function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await axios.post('/api/files/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.data.url; // "/uploads/images/..."
}

// Sử dụng
const imageUrl = await uploadImage(selectedFile);
// Lưu vào product.imageUrl hoặc variant.colorImageUrl
```

---

### 📤 8.2. Upload Nhiều Ảnh

**Endpoint:** `POST /api/files/images`  
**Auth Required:** ✅ Yes  
**Content-Type:** `multipart/form-data`

**Request:**
```bash
POST /api/files/images
Content-Type: multipart/form-data

files: [file1, file2, file3, ...]
```

**Response:**
```json
{
  "success": true,
  "message": "Upload 3 anh thanh cong",
  "data": [
    {
      "url": "/uploads/images/1711344600000_img1.jpg",
      "fileName": "1711344600000_img1.jpg",
      "fileSize": 123456,
      "contentType": "image/jpeg"
    },
    {
      "url": "/uploads/images/1711344600001_img2.jpg",
      "fileName": "1711344600001_img2.jpg",
      "fileSize": 234567,
      "contentType": "image/png"
    }
  ],
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Frontend Example:**
```javascript
async function uploadMultipleImages(files) {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  
  const response = await axios.post('/api/files/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`
    }
  });
  
  // Lấy array các URL
  const urls = response.data.map(item => item.url);
  
  // Lưu vào variant.images dạng JSON string
  const imagesJson = JSON.stringify(urls);
  // VD: "[\"\/uploads\/images\/img1.jpg\",\"\/uploads\/images\/img2.jpg\"]"
  
  return urls;
}
```

**Lưu ý:**
- File được lưu tại `C:\CODE\uploads\images` (local) hoặc `/app/uploads/images` (Docker)
- URL trả về có thể dùng trực tiếp: `<img src="http://localhost:8080${url}" />`
- Tên file được prefix timestamp để tránh trùng lặp

---

## 9. SUPPORT APIs

### 📍 6.1. Lấy Danh Sách Địa Chỉ

**Endpoint:** `GET /api/addresses`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "unitNumber": "101",
      "streetNumber": "25",
      "addressLine1": "Đường Lê Văn Lương",
      "addressLine2": null,
      "city": "Hà Nội",
      "region": "Thanh Xuân",
      "postalCode": "100000",
      "countryId": 1,
      "isDefault": true
    },
    {
      "id": 2,
      "unitNumber": "202",
      "streetNumber": "50",
      "addressLine1": "Đường Nguyễn Trãi",
      "city": "TP.HCM",
      "region": "Quận 1",
      "postalCode": "700000",
      "countryId": 1,
      "isDefault": false
    }
  ]
}
```

---

### ➕ 6.2. Thêm Địa Chỉ Mới

**Endpoint:** `POST /api/addresses`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "unitNumber": "101",
  "streetNumber": "25",
  "addressLine1": "Đường Lê Văn Lương",
  "addressLine2": "Khu đô thị mới",
  "city": "Hà Nội",
  "region": "Thanh Xuân",
  "postalCode": "100000",
  "countryId": 1
}
```

**Response:**
```json
{
  "success": true,
  "message": "Thêm địa chỉ thành công",
  "data": { /* address detail với isDefault = true nếu là địa chỉ đầu tiên */ }
}
```

**Note:** Địa chỉ đầu tiên tự động được đặt làm mặc định

---

### ✏️ 6.3. Cập Nhật Địa Chỉ

**Endpoint:** `PUT /api/addresses/{addressId}`  
**Auth Required:** ✅ Yes (USER)

**Request Body:** Giống như POST

---

### 🗑️ 6.4. Xóa Địa Chỉ

**Endpoint:** `DELETE /api/addresses/{addressId}`  
**Auth Required:** ✅ Yes (USER)

---

### ⭐ 6.5. Đặt Địa Chỉ Mặc Định

**Endpoint:** `PATCH /api/addresses/{addressId}/default`  
**Auth Required:** ✅ Yes (USER)

**Response:**
```json
{
  "success": true,
  "message": "Da dat dia chi mac dinh",
  "data": { 
    "id": 2,
    "unitNumber": "202",
    "streetNumber": "50",
    "addressLine1": "Đường Nguyễn Trãi",
    "addressLine2": null,
    "city": "TP.HCM",
    "region": "Quận 1",
    "postalCode": "700000",
    "countryId": 1,
    "isDefault": true
  },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Note:** Chỉ có 1 địa chỉ mặc định. Địa chỉ cũ tự động bỏ mặc định (`isDefault = false`).

---

## 7. SUPPORT APIs

### 💳 7.1. Payment Types (Phương Thức Thanh Toán)

**Endpoint:** `GET /api/payment-types`  
**Auth Required:** ❌ No

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "value": "Chuyển khoản ngân hàng"
    },
    {
      "id": 2,
      "value": "COD (Tiền mặt khi nhận hàng)"
    },
    {
      "id": 3,
      "value": "VNPAY"
    }
  ],
  "timestamp": "2026-03-25T10:30:00Z"
}
```

---

### 🚚 7.2. Shipping Methods (Phương Thức Vận Chuyển)

**Endpoint:** `GET /api/shipping-methods`  
**Auth Required:** ❌ No

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Giao hàng nhanh",
      "price": 30000,
      "description": "Giao trong 24h"
    },
    {
      "id": 2,
      "name": "Giao hàng tiêu chuẩn",
      "price": 20000,
      "description": "Giao trong 2-3 ngày"
    }
  ]
}
```

---

### 🌍 7.3. Order Statuses (Trạng Thái Đơn Hàng)

**Endpoint:** `GET /api/order-statuses`  
**Auth Required:** ❌ No

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "status": "PENDING",
      "description": "Chờ xử lý"
    },
    {
      "id": 2,
      "status": "PROCESSING",
      "description": "Đang xử lý"
    },
    {
      "id": 3,
      "status": "SHIPPED",
      "description": "Đang giao hàng"
    },
    {
      "id": 4,
      "status": "DELIVERED",
      "description": "Đã giao hàng"
    },
    {
      "id": 5,
      "status": "CANCELLED",
      "description": "Đã hủy"
    }
  ]
}
```

---

## 8. ERROR HANDLING

### 🚨 Common Error Codes

| HTTP Status | Mô Tả | Example |
|-------------|--------|---------|
| 400 | Bad Request - Dữ liệu đầu vào không hợp lệ | Thiếu trường bắt buộc, format sai |
| 401 | Unauthorized - Chưa đăng nhập hoặc token hết hạn | Token không hợp lệ |
| 403 | Forbidden - Không có quyền truy cập | USER cố truy cập endpoint ADMIN |
| 404 | Not Found - Không tìm thấy resource | Product ID không tồn tại |
| 409 | Conflict - Xung đột dữ liệu | Username đã tồn tại |
| 500 | Internal Server Error - Lỗi server | Lỗi database, lỗi code |

### Error Response Format

```json
{
  "success": false,
  "message": "Username đã tồn tại",
  "data": null
}
```

### Frontend Error Handling Example

```javascript
try {
  const response = await api.post('/api/auth/register', userData);
  // Xử lý thành công
} catch (error) {
  if (error.response) {
    const { status, data } = error.response;
    
    if (status === 401) {
      // Token hết hạn → Redirect to login
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    } else if (status === 400) {
      // Validation error → Hiển thị message
      alert(data.message);
    } else if (status === 409) {
      // Conflict → Username đã tồn tại
      alert('Username đã được sử dụng');
    } else {
      // Other errors
      alert('Có lỗi xảy ra, vui lòng thử lại');
    }
  }
}
```

---

## 9. BEST PRACTICES

### 🔒 9.1. Token Management

```javascript
// 1. Lưu token an toàn
localStorage.setItem('accessToken', token);

// 2. Tự động thêm token vào header (Axios)
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 3. Xử lý token hết hạn
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

### 🎯 9.2. Product Display Flow

```javascript
// Trang danh sách sản phẩm
async function loadProducts(page = 0) {
  const response = await api.get(`/products/paged?page=${page}&size=12&sortBy=createdAt&direction=DESC`);
  
  // Hiển thị:
  // - Thumbnail từ thumbnailUrl
  // - Tên sản phẩm
  // - Giá basePrice (có thể có override ở variant)
  // - Badge "Hết hàng" nếu tất cả variants đều hết
  
  return response.data;
}

// Trang chi tiết sản phẩm
async function loadProductDetail(slug) {
  // 1. Load product
  const product = await api.get(`/products/slug/${slug}`);
  
  // 2. Load colors
  const variants = await api.get(`/product-variants/product/${product.data.id}`);
  
  // 3. Load sizes của màu đầu tiên
  if (variants.data.length > 0) {
    const firstVariant = variants.data[0];
    const stocks = await api.get(`/variant-stocks/variant/${firstVariant.id}`);
    
    return {
      product: product.data,
      colors: variants.data,
      sizes: stocks.data,
      selectedColor: firstVariant,
      selectedStock: null
    };
  }
}

// Khi user chọn màu khác
async function onColorChange(variantId) {
  const stocks = await api.get(`/variant-stocks/variant/${variantId}`);
  // Update available sizes
  return stocks.data;
}
```

---

### 🛒 9.3. Cart Management

```javascript
// Thêm vào giỏ
async function addToCart(variantStockId, qty) {
  try {
    const response = await api.post('/cart/items', {
      variantStockId,
      qty
    });
    
    // Update cart badge
    updateCartBadge(response.data.totalItems);
    
    // Show success notification
    showNotification('Đã thêm vào giỏ hàng');
    
  } catch (error) {
    if (error.response?.status === 400) {
      alert('Số lượng vượt quá tồn kho');
    }
  }
}

// Update số lượng
async function updateCartItem(itemId, newQty) {
  const response = await api.put(`/cart/items/${itemId}`, {
    variantStockId: currentVariantStockId, // Giữ nguyên
    qty: newQty
  });
  
  return response.data; // Cart summary mới
}

// Xóa khỏi giỏ
async function removeCartItem(itemId) {
  const response = await api.delete(`/cart/items/${itemId}`);
  return response.data;
}

// Load giỏ hàng
async function loadCart() {
  const response = await api.get('/cart');
  
  // Hiển thị:
  // - Danh sách items (tên, màu, size, giá, số lượng)
  // - Tổng tiền
  // - Nút "Thanh toán"
  
  return response.data;
}
```

---

### 📦 9.4. Checkout Flow

```javascript
async function checkout() {
  // 1. Load danh sách địa chỉ
  const addresses = await api.get('/addresses');
  
  // 2. Load phương thức thanh toán
  const paymentTypes = await api.get('/payment-types');
  
  // 3. Load phương thức vận chuyển
  const shippingMethods = await api.get('/shipping-methods');
  
  // 4. User chọn các option → Submit order
  const orderData = {
    shippingAddressId: selectedAddress.id,
    paymentTypeId: selectedPaymentType.id,
    shippingMethodId: selectedShippingMethod.id,
    note: userNote
  };
  
  const order = await api.post('/orders', orderData);
  
  // 5. Nếu thanh toán chuyển khoản → Hiển thị QR code
  if (order.data.qrUrl) {
    showQRCode(order.data.qrUrl, order.data.bankInfo);
  }
  
  // 6. Redirect to order success page
  window.location.href = `/orders/${order.data.id}`;
}
```

---

### 📊 9.5. Order Tracking

```javascript
// Load lịch sử đơn hàng
async function loadOrders() {
  const response = await api.get('/orders');
  
  // Hiển thị danh sách với:
  // - Mã đơn hàng
  // - Ngày đặt
  // - Trạng thái (với màu sắc khác nhau)
  // - Tổng tiền
  
  return response.data;
}

// Load chi tiết đơn hàng
async function loadOrderDetail(orderId) {
  const response = await api.get(`/orders/${orderId}`);
  
  // Hiển thị:
  // - Thông tin đơn hàng
  // - Địa chỉ giao hàng
  // - Phương thức thanh toán (+ QR code nếu có)
  // - Danh sách sản phẩm
  // - Trạng thái hiện tại
  // - Nút "Hủy đơn" (nếu status = PENDING)
  
  return response.data;
}

// Hủy đơn hàng
async function cancelOrder(orderId) {
  if (confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
    const response = await api.patch(`/orders/${orderId}/cancel`);
    alert('Đã hủy đơn hàng thành công');
    return response.data;
  }
}
```

---

### 🔍 11.6. Search & Filter

```javascript
// Build search URL
function buildSearchUrl(filters) {
  const params = new URLSearchParams();
  
  if (filters.name) params.append('name', filters.name);
  
  // Multiple category IDs
  filters.categoryIds?.forEach(id => params.append('categoryIds', id));
  
  // Multiple color IDs
  filters.colorIds?.forEach(id => params.append('colorIds', id));
  
  if (filters.minPrice) params.append('minPrice', filters.minPrice);
  if (filters.maxPrice) params.append('maxPrice', filters.maxPrice);
  
  params.append('page', filters.page || 0);
  params.append('size', filters.size || 12);
  params.append('sortBy', filters.sortBy || 'createdAt');
  params.append('direction', filters.direction || 'DESC');
  
  return `/products/search?${params.toString()}`;
}

// Example usage
const searchFilters = {
  name: 'áo thun',
  categoryIds: [1, 2],
  colorIds: [1],
  minPrice: 100000,
  maxPrice: 500000,
  page: 0,
  size: 20,
  sortBy: 'basePrice',
  direction: 'ASC'
};

const products = await api.get(buildSearchUrl(searchFilters));
```

---

## 10. PRODUCT REVIEWS APIs ⭐ NEW

> **Chống fake review:** User CHỈ được đánh giá sản phẩm đã mua thật (FK tới `order_line`), đơn hàng phải ở trạng thái **DELIVERED**.

### Tổng Quan Endpoints

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `GET` | `/api/reviews/product/{productId}` | ❌ No | Xem reviews theo sản phẩm |
| `GET` | `/api/reviews/product/{productId}/summary` | ❌ No | Điểm TB + tổng số reviews |
| `POST` | `/api/reviews` | ✅ USER | Tạo review |
| `GET` | `/api/reviews/my` | ✅ USER | Reviews của tôi |
| `DELETE` | `/api/reviews/{id}` | ✅ USER | Xóa review của mình |

---

### 🌟 10.1. Xem Reviews Theo Sản Phẩm

**Endpoint:** `GET /api/reviews/product/{productId}`  
**Auth Required:** ❌ No

**Example:** `GET /api/reviews/product/3`

**Response (200 OK):**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 1,
      "userId": 10,
      "username": "ngoc123",
      "orderedProductId": 10,
      "ratingValue": 5,
      "comment": "Áo đẹp lắm, chất lượng tốt!",
      "createdAt": "2026-03-25T10:30:00",
      "productId": 3,
      "productName": "Áo thun Nike",
      "productSlug": "ao-thun-nike",
      "colorName": "Đỏ",
      "colorHex": "#FF0000",
      "colorImageUrl": "/uploads/images/nike-red.jpg",
      "sizeLabel": "M",
      "sizeType": "CLOTHING",
      "sku": "NIKE-RED-M"
    },
    {
      "id": 2,
      "userId": 11,
      "username": "minh456",
      "orderedProductId": 15,
      "ratingValue": 4,
      "comment": "Giao hàng nhanh, sản phẩm OK",
      "createdAt": "2026-03-24T14:20:00",
      "productId": 3,
      "productName": "Áo thun Nike",
      "productSlug": "ao-thun-nike",
      "colorName": "Xanh",
      "colorHex": "#0000FF",
      "colorImageUrl": "/uploads/images/nike-blue.jpg",
      "sizeLabel": "L",
      "sizeType": "CLOTHING",
      "sku": "NIKE-BLU-L"
    }
  ],
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Note:**
- Sắp xếp **mới nhất trước** (ORDER BY `createdAt` DESC)
- Response enriched: mỗi review gồm thông tin sản phẩm (tên, màu, size, SKU, ảnh)
- Trả về `[]` nếu chưa có review nào
- **404** nếu `productId` không tồn tại

**Response Fields:**

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Integer | ID review |
| `userId` | Integer | ID người đánh giá |
| `username` | String | Tên hiển thị của người đánh giá |
| `orderedProductId` | Integer | ID order_line (dòng sản phẩm đã mua) |
| `ratingValue` | Integer | Điểm đánh giá (1–5 sao) |
| `comment` | String | Nội dung đánh giá (nullable) |
| `createdAt` | DateTime | Thời gian tạo review |
| `productId` | Integer | ID sản phẩm |
| `productName` | String | Tên sản phẩm |
| `productSlug` | String | Slug sản phẩm (SEO) |
| `colorName` | String | Tên màu đã mua |
| `colorHex` | String | Mã hex màu |
| `colorImageUrl` | String | Ảnh màu sắc (nullable) |
| `sizeLabel` | String | Nhãn size (S, M, L, 39, 40...) |
| `sizeType` | String | Loại size (CLOTHING / NUMERIC / SHOES) |
| `sku` | String | Mã SKU của variant stock |

---

### 📊 10.2. Thống Kê Đánh Giá Sản Phẩm

**Endpoint:** `GET /api/reviews/product/{productId}/summary`  
**Auth Required:** ❌ No

**Example:** `GET /api/reviews/product/3/summary`

**Response (200 OK):**
```json
{
  "success": true,
  "message": null,
  "data": {
    "productId": 3,
    "avgRating": 4.5,
    "totalReviews": 12
  },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Note:**
- `avgRating` làm tròn 1 chữ số thập phân (4.5, 3.8...)
- Nếu chưa có review: `avgRating = 0.0`, `totalReviews = 0`
- **404** nếu `productId` không tồn tại

**⭐ Tip FE:** Gọi API này khi render trang chi tiết sản phẩm để hiển thị sao + số lượng reviews ngay phía trên phần reviews.

---

### ✍️ 10.3. Tạo Đánh Giá

**Endpoint:** `POST /api/reviews`  
**Auth Required:** ✅ Yes (USER)

**Request Body:**
```json
{
  "orderedProductId": 10,
  "ratingValue": 5,
  "comment": "Sản phẩm rất đẹp, chất lượng tốt!"
}
```

**Request Fields:**

| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `orderedProductId` | Integer | ✅ | FK → `order_line.id`, phải tồn tại |
| `ratingValue` | Integer | ✅ | Min 1, Max 5 |
| `comment` | String | ❌ | Max 2000 ký tự |

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Tao danh gia thanh cong.",
  "data": {
    "id": 5,
    "userId": 10,
    "username": "ngoc123",
    "orderedProductId": 10,
    "ratingValue": 5,
    "comment": "Sản phẩm rất đẹp, chất lượng tốt!",
    "createdAt": "2026-03-25T15:30:00",
    "productId": 3,
    "productName": "Áo thun Nike",
    "productSlug": "ao-thun-nike",
    "colorName": "Đỏ",
    "colorHex": "#FF0000",
    "colorImageUrl": "/uploads/images/nike-red.jpg",
    "sizeLabel": "M",
    "sizeType": "CLOTHING",
    "sku": "NIKE-RED-M"
  },
  "timestamp": "2026-03-25T15:30:00Z"
}
```

**Validation & Errors:**

| HTTP Status | Message | Khi nào |
|-------------|---------|---------|
| 201 | `Tao danh gia thanh cong.` | Tạo thành công |
| 400 | `Don hang nay khong thuoc ve ban.` | order_line thuộc user khác |
| 400 | `Don hang chua giao xong, khong the danh gia.` | Đơn hàng chưa DELIVERED |
| 400 | `Ban da danh gia san pham nay roi.` | Đã review order_line này rồi |
| 400 | Validation error | ratingValue ngoài 1–5, comment > 2000 |
| 404 | `Khong tim thay dong san pham da mua voi ID: {0}` | order_line không tồn tại |

**⚠️ Quan trọng cho FE:**
- `orderedProductId` là **ID của `order_line`** (dòng sản phẩm trong đơn hàng), KHÔNG phải `productId`
- Lấy `orderedProductId` từ response chi tiết đơn hàng (`GET /api/orders/{id}` → `items[].id`)
- Chỉ hiện nút "Đánh giá" khi đơn hàng có `statusName = "DELIVERED"`

---

### 📝 10.4. Xem Reviews Của Tôi

**Endpoint:** `GET /api/reviews/my`  
**Auth Required:** ✅ Yes (USER)

**Response (200 OK):**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 5,
      "userId": 10,
      "username": "ngoc123",
      "orderedProductId": 10,
      "ratingValue": 5,
      "comment": "Sản phẩm rất đẹp!",
      "createdAt": "2026-03-25T15:30:00",
      "productId": 3,
      "productName": "Áo thun Nike",
      "productSlug": "ao-thun-nike",
      "colorName": "Đỏ",
      "colorHex": "#FF0000",
      "colorImageUrl": "/uploads/images/nike-red.jpg",
      "sizeLabel": "M",
      "sizeType": "CLOTHING",
      "sku": "NIKE-RED-M"
    }
  ],
  "timestamp": "2026-03-25T15:30:00Z"
}
```

**Note:** Sắp xếp mới nhất trước. Trả về `[]` nếu chưa viết review nào.

---

### 🗑️ 10.5. Xóa Review Của Mình

**Endpoint:** `DELETE /api/reviews/{reviewId}`  
**Auth Required:** ✅ Yes (USER)

**Example:** `DELETE /api/reviews/5`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xoa danh gia thanh cong.",
  "data": null,
  "timestamp": "2026-03-25T15:35:00Z"
}
```

**Errors:**

| HTTP Status | Message | Khi nào |
|-------------|---------|---------|
| 404 | `Khong tim thay danh gia voi ID: {0}` | Review không tồn tại |
| 400 | `Danh gia nay khong thuoc ve ban.` | Cố xóa review của người khác |

---

### 💻 10.6. Frontend Implementation — Reviews

#### Trang Chi Tiết Sản Phẩm (hiển thị reviews)

```javascript
// Load reviews + summary cho trang chi tiết sản phẩm
async function loadProductReviews(productId) {
  // Gọi song song 2 API để tối ưu
  const [reviewsRes, summaryRes] = await Promise.all([
    api.get(`/reviews/product/${productId}`),
    api.get(`/reviews/product/${productId}/summary`)
  ]);

  return {
    reviews: reviewsRes.data,    // List<ReviewResponseDTO>
    summary: summaryRes.data     // { productId, avgRating, totalReviews }
  };
}

// Render rating stars
function renderStars(rating) {
  const fullStars = Math.floor(rating);
  const hasHalf = rating % 1 >= 0.5;
  let html = '';
  for (let i = 0; i < fullStars; i++) html += '★';
  if (hasHalf) html += '☆';
  for (let i = fullStars + (hasHalf ? 1 : 0); i < 5; i++) html += '☆';
  return html;
}

// Hiển thị summary: ★★★★☆ 4.5/5 (12 đánh giá)
function renderReviewSummary(summary) {
  return `${renderStars(summary.avgRating)} ${summary.avgRating}/5 (${summary.totalReviews} đánh giá)`;
}

// Hiển thị từng review
function renderReview(review) {
  return `
    <div class="review-item">
      <div class="review-header">
        <strong>${review.username}</strong>
        <span>${renderStars(review.ratingValue)}</span>
        <span class="review-date">${new Date(review.createdAt).toLocaleDateString('vi-VN')}</span>
      </div>
      <div class="review-variant">
        Phân loại: ${review.colorName} / ${review.sizeLabel}
      </div>
      <p class="review-comment">${review.comment || ''}</p>
    </div>
  `;
}
```

#### Trang Chi Tiết Đơn Hàng (nút viết đánh giá)

```javascript
// Kiểm tra và hiển thị nút "Đánh giá" cho từng item trong đơn hàng DELIVERED
async function renderOrderItemsWithReviewButton(order) {
  // Chỉ show nút review khi đơn hàng DELIVERED
  if (order.statusName !== 'DELIVERED') return;

  // Load reviews của user để check đã review chưa
  const myReviews = await api.get('/reviews/my');
  const reviewedOrderLineIds = new Set(
    myReviews.data.map(r => r.orderedProductId)
  );

  order.items.forEach(item => {
    const alreadyReviewed = reviewedOrderLineIds.has(item.id);
    // item.id chính là orderedProductId (order_line.id)

    if (alreadyReviewed) {
      // Đã đánh giá → hiện badge "Đã đánh giá ★"
      showBadge(item.id, 'Đã đánh giá ★');
    } else {
      // Chưa đánh giá → hiện nút "Viết đánh giá"
      showReviewButton(item.id);
    }
  });
}

// Submit review
async function submitReview(orderedProductId, ratingValue, comment) {
  try {
    const response = await api.post('/reviews', {
      orderedProductId,  // ← order_line.id, KHÔNG phải productId
      ratingValue,       // ← 1-5
      comment            // ← optional, max 2000 chars
    });

    alert('Đánh giá thành công!');
    return response.data;

  } catch (error) {
    const msg = error.response?.data?.message;

    if (msg?.includes('khong thuoc ve ban')) {
      alert('Đơn hàng không thuộc về bạn.');
    } else if (msg?.includes('chua giao xong')) {
      alert('Đơn hàng chưa giao xong, không thể đánh giá.');
    } else if (msg?.includes('da danh gia')) {
      alert('Bạn đã đánh giá sản phẩm này rồi.');
    } else {
      alert(msg || 'Có lỗi xảy ra');
    }
  }
}

// Xóa review
async function deleteReview(reviewId) {
  if (!confirm('Bạn có chắc muốn xóa đánh giá này?')) return;

  try {
    await api.delete(`/reviews/${reviewId}`);
    alert('Đã xóa đánh giá');
    location.reload();
  } catch (error) {
    alert(error.response?.data?.message || 'Không thể xóa đánh giá');
  }
}
```

#### Trang "Đánh Giá Của Tôi"

```javascript
// Load tất cả reviews của user hiện tại
async function loadMyReviews() {
  const response = await api.get('/reviews/my');

  // Hiển thị:
  // - Sản phẩm (productName + colorName + sizeLabel)
  // - Rating (1-5 sao)
  // - Comment
  // - Ngày đánh giá
  // - Nút xóa

  return response.data;
}
```

#### Mapping orderedProductId từ Order Detail

```javascript
// ⚠️ QUAN TRỌNG: orderedProductId = order_line.id (KHÔNG phải product.id)
//
// Flow lấy orderedProductId:
//   GET /api/orders/{orderId}
//   → response.data.items[]
//   → mỗi item có .id  ← ĐÂY chính là orderedProductId
//
// Ví dụ:
//   order.items = [
//     { "id": 10, "productName": "Áo Nike", "colorName": "Đỏ", "sizeLabel": "M", "qty": 1 },
//     { "id": 11, "productName": "Quần Adidas", "colorName": "Đen", "sizeLabel": "L", "qty": 2 }
//   ]
//
// Khi review item "Áo Nike Đỏ M" → orderedProductId = 10
// Khi review item "Quần Adidas Đen L" → orderedProductId = 11
```

---

## 📞 SUPPORT & RESOURCES

### 🌐 API Documentation
- **Swagger UI Local:** http://localhost:8080/swagger-ui.html
- **Swagger UI Production:** http://160.30.113.40:8080/swagger-ui.html
- **API Docs JSON:** http://localhost:8080/v3/api-docs

### 🏥 Health Check
- **Endpoint:** `GET /actuator/health`
- **Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

### 📝 Important Notes
1. Tất cả datetime đều dùng format ISO 8601: `2026-03-25T14:30:00`
2. Currency (tiền tệ) đều là VNĐ (Integer, không có phần thập phân)
3. File upload: `POST /api/files/image` (1 ảnh) hoặc `POST /api/files/images` (nhiều ảnh)
   - Response có `fileUrl` để hiển thị ảnh
   - Xem chi tiết tại Section 8 - File Upload API
4. JWT token có thời hạn **24 giờ** (86400000ms)
5. Database timezone: UTC
6. `images` trong ProductVariant là JSON string, cần parse: `JSON.parse(variant.images)`
7. Giá hiển thị = `priceOverride != null ? priceOverride : basePrice`
8. Mã đơn hàng format: `DH + yyyyMMdd + số thứ tự` (VD: DH20260325001)
9. `isDefault` trong UserPaymentMethod là Integer (1 = true, 0 = false)
10. **Chat AI**: Lưu `sessionId` từ response để duy trì ngữ cảnh hội thoại

---

## 11. AI CHAT BOT APIs

> 🤖 **Chatbot AI gợi ý sản phẩm** — Tích hợp Claude API (Anthropic)  
> **Không cần đăng nhập** | Session-based conversation | Tự động gợi ý sản phẩm từ DB

---

### 11.1. Gửi Tin Nhắn Cho Chatbot

**Endpoint:** `POST /api/chat/message`  
**Auth:** ❌ Không cần (Public) — Nếu đã đăng nhập sẽ được cá nhân hóa hơn

**Request Body:**
```json
{
  "message": "Tôi muốn mua áo thun màu đỏ dưới 300k",
  "sessionId": null,
  "productId": null
}
```

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `message` | String | ✅ | Tin nhắn của người dùng (tối đa 1000 ký tự) |
| `sessionId` | String (UUID) | ❌ | ID phiên hội thoại. `null` = tạo mới. Gửi lại ID cũ để AI nhớ ngữ cảnh |
| `productId` | Integer | ❌ | ID sản phẩm đang xem — giúp AI hiểu context và gợi ý liên quan |

**Response thành công (200):**
```json
{
  "success": true,
  "message": "Phan hoi thanh cong",
  "data": {
    "message": "Chào bạn! Tôi tìm thấy một số áo thun phù hợp với ngân sách của bạn:\n\n• Áo Thun Basic Cotton - 250,000 VND\n• Áo Thun Polo Classic - 280,000 VND\n\nCả hai đều có màu đỏ và chất liệu cotton thoáng mát. Bạn muốn biết thêm về sản phẩm nào không?",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "suggestions": [
      {
        "id": 5,
        "name": "Áo Thun Basic Cotton",
        "price": 250000,
        "thumbnailUrl": "/uploads/images/ao-thun-basic.jpg",
        "slug": "ao-thun-basic-cotton",
        "brand": "Uniqlo",
        "material": "Cotton 100%",
        "categoryName": "Áo Thun"
      },
      {
        "id": 8,
        "name": "Áo Thun Polo Classic",
        "price": 280000,
        "thumbnailUrl": "/uploads/images/ao-polo.jpg",
        "slug": "ao-thun-polo-classic",
        "brand": "Lacoste",
        "material": "Cotton Pique",
        "categoryName": "Áo Polo"
      }
    ],
    "timestamp": "2026-03-25T10:30:00Z"
  }
}
```

---

### 11.2. Xóa Lịch Sử Chat

**Endpoint:** `DELETE /api/chat/session/{sessionId}`  
**Auth:** ✅ Bearer Token (tuỳ chọn — chủ yếu dùng khi muốn reset hội thoại)

**Response thành công (200):**
```json
{
  "success": true,
  "message": "Xoa session thanh cong",
  "data": null
}
```

---

### 11.3. Hướng Dẫn Tích Hợp Frontend

#### Bước 1 — Tạo state quản lý chat

```javascript
// React hooks example
const [messages, setMessages] = useState([]);
const [sessionId, setSessionId] = useState(null);
const [isLoading, setIsLoading] = useState(false);
const [suggestions, setSuggestions] = useState([]);
```

#### Bước 2 — Hàm gửi tin nhắn

```javascript
const sendChatMessage = async (userMessage, productId = null) => {
  setIsLoading(true);

  // Thêm tin nhắn user vào UI ngay
  setMessages(prev => [...prev, {
    role: 'user',
    content: userMessage,
    timestamp: new Date()
  }]);

  try {
    const response = await fetch(`${BASE_URL}/api/chat/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        // Gửi token nếu đã đăng nhập (tuỳ chọn)
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      body: JSON.stringify({
        message: userMessage,
        sessionId: sessionId,    // null lần đầu, ID cũ các lần sau
        productId: productId     // ID sản phẩm đang xem (nếu có)
      })
    });

    const result = await response.json();

    if (result.success) {
      const { message, sessionId: newSessionId, suggestions } = result.data;

      // ⚠️ QUAN TRỌNG: Lưu sessionId cho request tiếp theo
      setSessionId(newSessionId);

      // Thêm phản hồi AI vào UI
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: message,
        timestamp: new Date()
      }]);

      // Cập nhật gợi ý sản phẩm
      setSuggestions(suggestions);
    }
  } catch (error) {
    console.error('Chat error:', error);
  } finally {
    setIsLoading(false);
  }
};
```

#### Bước 3 — Render giao diện chat

```jsx
// Chat window component
const ChatWindow = () => {
  const [inputText, setInputText] = useState('');

  return (
    <div className="chat-container">
      {/* Message history */}
      <div className="message-list">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message ${msg.role}`}>
            {msg.role === 'assistant' && (
              <img src="/bot-avatar.png" alt="AI" className="avatar" />
            )}
            <div className="bubble">{msg.content}</div>
          </div>
        ))}
        {isLoading && <div className="typing-indicator">AI đang trả lời...</div>}
      </div>

      {/* Product suggestions */}
      {suggestions.length > 0 && (
        <div className="suggestions">
          <h4>Gợi ý cho bạn:</h4>
          <div className="product-grid">
            {suggestions.map(product => (
              <ProductCard
                key={product.id}
                product={product}
                onView={() => sendChatMessage(
                  `Cho tôi biết thêm về ${product.name}`,
                  product.id
                )}
              />
            ))}
          </div>
        </div>
      )}

      {/* Input */}
      <div className="input-area">
        <input
          value={inputText}
          onChange={e => setInputText(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && sendChatMessage(inputText)}
          placeholder="Hỏi AI về sản phẩm..."
        />
        <button onClick={() => sendChatMessage(inputText)}>Gửi</button>
      </div>
    </div>
  );
};
```

#### Bước 4 — Tích hợp trên trang Product Detail

```javascript
// Khi user đang xem một sản phẩm cụ thể
// Truyền productId để AI hiểu context
const ProductDetailPage = ({ product }) => {

  const handleChatWithContext = (question) => {
    // AI sẽ nhận biết sản phẩm đang xem và tư vấn phù hợp
    sendChatMessage(question, product.id);
  };

  return (
    <div>
      {/* ... product info ... */}
      <button onClick={() => handleChatWithContext("Size này có phù hợp không?")}>
        💬 Hỏi AI về sản phẩm này
      </button>
    </div>
  );
};
```

---

### 11.4. Ví Dụ Câu Hỏi Phổ Biến

| Loại câu hỏi | Ví dụ | AI sẽ làm gì |
|-------------|-------|-------------|
| **Tìm theo ngân sách** | "Áo thun dưới 300k" | Tìm DB → gợi ý products phù hợp |
| **Tư vấn size** | "Tôi cao 1m70, nặng 65kg mặc size gì?" | Tư vấn size theo thông số |
| **Phối đồ** | "Áo này phối với gì trông đẹp?" | Gợi ý cách phối (cần productId) |
| **So sánh** | "Áo cotton và áo polyester khác gì?" | Giải thích chất liệu |
| **Tìm theo màu** | "Có áo khoác màu xanh navy không?" | Query DB theo keyword |
| **Tìm theo thương hiệu** | "Cho tôi xem đồ của Nike" | Filter theo brand trong DB |

---

### 11.5. Lưu Ý Quan Trọng

> ⚠️ **sessionId là cốt lõi để duy trì ngữ cảnh** — Luôn lưu và gửi lại

```javascript
// ✅ ĐÚNG — gửi sessionId từ response trước
{ "message": "...", "sessionId": "550e8400-..." }

// ❌ SAI — gửi null → AI sẽ mất ngữ cảnh hội thoại trước đó
{ "message": "...", "sessionId": null }
```

| Điều cần biết | Chi tiết |
|--------------|---------|
| **Session TTL** | 2 giờ không hoạt động → session tự xóa |
| **Max messages/session** | 20 tin nhắn (tự động trim tin cũ) |
| **Fallback** | Nếu không có CLAUDE_API_KEY → bot vẫn trả sản phẩm từ DB |
| **Max suggestions** | Tối đa 5 sản phẩm mỗi response |
| **Storage** | In-memory → restart server = mất session |
| **Rate limit** | Không có giới hạn phía backend (phụ thuộc Claude quota) |

---

### 11.6. cURL Test

```bash
# Gửi tin nhắn lần đầu (không có sessionId)
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tôi muốn mua áo thun cotton dưới 300k",
    "sessionId": null
  }'

# Tiếp tục hội thoại với sessionId từ response trên
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Áo đó có size L không?",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Hỏi về sản phẩm đang xem (productId = 5)
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Áo này phối với quần gì trông đẹp?",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "productId": 5
  }'

# Xóa session
curl -X DELETE http://localhost:8080/api/chat/session/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 🐛 Common Issues

**Issue 1: CORS Error**
- Backend đã cấu hình CORS cho phép mọi origin
- Nếu vẫn lỗi → Kiểm tra request header

**Issue 2: 401 Unauthorized**
- Token hết hạn → Đăng nhập lại
- Token sai format → Phải có prefix "Bearer "

**Issue 3: Giỏ hàng trống sau khi đặt hàng**
- Đây là behavior đúng, giỏ hàng tự động xóa sau khi đặt hàng thành công

**Issue 4: Không thêm được vào giỏ**
- Kiểm tra `qtyInStock` > 0
- Kiểm tra user đã đăng nhập chưa

---

## 12. PRODUCT MANAGEMENT APIs (ADMIN) 🛠️ NEW

> **Dành cho trang Admin** — Tạo mới và chỉnh sửa sản phẩm đầy đủ (Product + Variants + Stocks) trong **1 request duy nhất**.  
> Yêu cầu **đăng nhập với tài khoản ADMIN** (role = `ADMIN`).

---

### Tổng Quan Endpoints

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `POST` | `/api/products/full` | ✅ ADMIN | Tạo sản phẩm đầy đủ |
| `PUT` | `/api/products/full/{id}` | ✅ ADMIN | Cập nhật sản phẩm đầy đủ |
| `GET` | `/api/products/full/{id}` | ❌ Public | Lấy chi tiết đầy đủ sản phẩm |

---

### 🏗️ Cấu Trúc Dữ Liệu 3 Tầng

```
Product (sản phẩm gốc)
  └─ variants[] — Biến thể theo màu sắc
        └─ stocks[]  — Tồn kho theo size (có SKU + số lượng + giá override)
```

**Ví dụ thực tế:**
```
Áo Thun Nike (Product)
  ├─ Màu Đỏ (variant, colorId=2, isDefault=true)
  │    ├─ Size S (stock, sizeId=1, qty=50, sku="NIKE-DO-S")
  │    └─ Size M (stock, sizeId=2, qty=30, sku="NIKE-DO-M")
  └─ Màu Xanh (variant, colorId=3)
       └─ Size M (stock, sizeId=2, qty=20, sku="NIKE-XANH-M", priceOverride=280000)
```

---

### ➕ 12.1. Tạo Sản Phẩm Đầy Đủ

**Endpoint:** `POST /api/products/full`  
**Auth Required:** ✅ Yes (ADMIN)

#### Request Body

```json
{
  "name": "Áo Thun Nike Basic",
  "slug": "ao-thun-nike-basic",
  "description": "Áo thun cotton 100%, thoáng mát, phù hợp mọi lứa tuổi",
  "categoryId": 1,
  "basePrice": 250000,
  "brand": "Nike",
  "material": "Cotton 100%",
  "isActive": true,
  "variants": [
    {
      "id": null,
      "colorId": 2,
      "colorImageUrl": "/uploads/images/ao-nike-do.jpg",
      "images": "[\"/uploads/images/nike-do-1.jpg\",\"/uploads/images/nike-do-2.jpg\"]",
      "isDefault": true,
      "stocks": [
        {
          "id": null,
          "sizeId": 1,
          "stockQty": 50,
          "priceOverride": null,
          "sku": "NIKE-DO-S"
        },
        {
          "id": null,
          "sizeId": 2,
          "stockQty": 30,
          "priceOverride": null,
          "sku": "NIKE-DO-M"
        },
        {
          "id": null,
          "sizeId": 3,
          "stockQty": 20,
          "priceOverride": null,
          "sku": "NIKE-DO-L"
        }
      ]
    },
    {
      "id": null,
      "colorId": 3,
      "colorImageUrl": "/uploads/images/ao-nike-xanh.jpg",
      "images": null,
      "isDefault": false,
      "stocks": [
        {
          "id": null,
          "sizeId": 2,
          "stockQty": 20,
          "priceOverride": 280000,
          "sku": "NIKE-XANH-M"
        }
      ]
    }
  ]
}
```

#### Request Fields — Product

| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `name` | String | ✅ | Max 200 ký tự |
| `slug` | String | ✅ | Max 200 ký tự, **phải duy nhất** toàn hệ thống |
| `description` | String | ❌ | Không giới hạn |
| `categoryId` | Integer | ✅ | Phải là ID danh mục tồn tại |
| `basePrice` | Decimal | ✅ | >= 0 (VNĐ) |
| `brand` | String | ❌ | Max 100 ký tự |
| `material` | String | ❌ | Max 100 ký tự |
| `isActive` | Boolean | ❌ | Default: `true` |

#### Request Fields — Variant (mỗi phần tử trong `variants[]`)

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `id` | Integer | ❌ | `null` = **tạo mới** khi POST |
| `colorId` | Integer | ✅ | Phải là ID màu sắc tồn tại |
| `colorImageUrl` | String | ❌ | URL ảnh thumbnail đại diện cho màu |
| `images` | String | ❌ | JSON array string: `"[\"/url1.jpg\",\"/url2.jpg\"]"` |
| `isDefault` | Boolean | ❌ | Màu mặc định (chỉ **1 variant được `true`**) |

#### Request Fields — Stock (mỗi phần tử trong `stocks[]`)

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `id` | Integer | ❌ | `null` = **tạo mới** khi POST |
| `sizeId` | Integer | ✅ | Phải là ID size tồn tại |
| `stockQty` | Integer | ❌ | >= 0, default: 0 |
| `priceOverride` | Decimal | ❌ | Ghi đè giá cho size này (`null` = dùng `basePrice`) |
| `sku` | String | ✅ | Max 100 ký tự, **phải duy nhất** toàn hệ thống |

#### Response (201 Created)

```json
{
  "success": true,
  "message": "Tao san pham thanh cong.",
  "data": {
    "id": 10,
    "name": "Áo Thun Nike Basic",
    "slug": "ao-thun-nike-basic",
    "description": "Áo thun cotton 100%, thoáng mát, phù hợp mọi lứa tuổi",
    "categoryId": 1,
    "categoryName": "Áo Thun",
    "basePrice": 250000,
    "brand": "Nike",
    "material": "Cotton 100%",
    "isActive": true,
    "createdAt": "2026-03-29 10:00:00",
    "updatedAt": "2026-03-29 10:00:00",
    "variants": [
      {
        "id": 15,
        "colorId": 2,
        "colorName": "Đỏ",
        "colorHexCode": "#FF0000",
        "colorSlug": "do",
        "colorImageUrl": "/uploads/images/ao-nike-do.jpg",
        "images": "[\"/uploads/images/nike-do-1.jpg\",\"/uploads/images/nike-do-2.jpg\"]",
        "isDefault": true,
        "stocks": [
          {
            "id": 30,
            "sizeId": 1,
            "sizeLabel": "S",
            "sizeType": "clothing",
            "stockQty": 50,
            "priceOverride": null,
            "effectivePrice": 250000,
            "sku": "NIKE-DO-S"
          },
          {
            "id": 31,
            "sizeId": 2,
            "sizeLabel": "M",
            "sizeType": "clothing",
            "stockQty": 30,
            "priceOverride": null,
            "effectivePrice": 250000,
            "sku": "NIKE-DO-M"
          },
          {
            "id": 32,
            "sizeId": 3,
            "sizeLabel": "L",
            "sizeType": "clothing",
            "stockQty": 20,
            "priceOverride": null,
            "effectivePrice": 250000,
            "sku": "NIKE-DO-L"
          }
        ]
      },
      {
        "id": 16,
        "colorId": 3,
        "colorName": "Xanh",
        "colorHexCode": "#0000FF",
        "colorSlug": "xanh",
        "colorImageUrl": "/uploads/images/ao-nike-xanh.jpg",
        "images": null,
        "isDefault": false,
        "stocks": [
          {
            "id": 33,
            "sizeId": 2,
            "sizeLabel": "M",
            "sizeType": "clothing",
            "stockQty": 20,
            "priceOverride": 280000,
            "effectivePrice": 280000,
            "sku": "NIKE-XANH-M"
          }
        ]
      }
    ]
  }
}
```

#### Lỗi Thường Gặp

| HTTP | Message | Nguyên nhân |
|------|---------|-------------|
| 400 | `Slug san pham da ton tai: {slug}` | `slug` đã tồn tại trong DB |
| 400 | `Chi duoc dat 1 bien the lam mac dinh` | Có > 1 variant có `isDefault = true` |
| 400 | `Ma SKU da ton tai: {sku}` | `sku` đã tồn tại trong DB |
| 404 | `Khong tim thay danh muc voi ID: {id}` | `categoryId` không tồn tại |
| 404 | `Khong tim thay mau sac voi ID: {id}` | `colorId` không tồn tại |
| 404 | `Khong tim thay size voi ID: {id}` | `sizeId` không tồn tại |
| 403 | `Forbidden` | Tài khoản không phải ADMIN |

---

### ✏️ 12.2. Cập Nhật Sản Phẩm Đầy Đủ

**Endpoint:** `PUT /api/products/full/{id}`  
**Auth Required:** ✅ Yes (ADMIN)

**Example:** `PUT /api/products/full/10`

#### Quy Tắc Cập Nhật (QUAN TRỌNG)

| `id` trong variant/stock | Hành động |
|--------------------------|-----------|
| `id = null` | **Tạo mới** bản ghi |
| `id = <số>` | **Cập nhật** bản ghi đã tồn tại |
| Không xuất hiện trong request | **Giữ nguyên** (không bị xóa) |

> ⚠️ **Để xóa variant hoặc stock**, dùng endpoint riêng:
> - `DELETE /api/product-variants/{variantId}`
> - `DELETE /api/variant-stocks/{stockId}`

#### Ví Dụ: Cập Nhật Giá + Thêm Size Mới + Thêm Màu Mới

```json
{
  "name": "Áo Thun Nike Basic (Updated)",
  "slug": "ao-thun-nike-basic",
  "basePrice": 260000,
  "categoryId": 1,
  "variants": [
    {
      "id": 15,
      "colorId": 2,
      "stocks": [
        {
          "id": 30,
          "sizeId": 1,
          "stockQty": 45,
          "sku": "NIKE-DO-S"
        },
        {
          "id": null,
          "sizeId": 4,
          "stockQty": 10,
          "priceOverride": null,
          "sku": "NIKE-DO-XL"
        }
      ]
    },
    {
      "id": null,
      "colorId": 5,
      "colorImageUrl": "/uploads/images/ao-nike-den.jpg",
      "isDefault": false,
      "stocks": [
        {
          "id": null,
          "sizeId": 2,
          "stockQty": 15,
          "sku": "NIKE-DEN-M"
        }
      ]
    }
  ]
}
```

> **Giải thích request trên:**
> - `variant id=15` (Màu Đỏ) → **cập nhật** colorId nếu đổi, giữ nguyên isDefault
>   - `stock id=30` (Size S) → **cập nhật** stockQty từ 50 → 45
>   - `stock id=null` (Size XL) → **tạo mới** stock cho Size XL
> - `variant id=null` (Màu Đen) → **tạo mới** variant màu đen
>   - `stock id=null` (Size M) → **tạo mới** stock
> - `variant id=16` (Màu Xanh) không xuất hiện → **giữ nguyên** (không bị xóa)

#### Response (200 OK)

Trả về toàn bộ sản phẩm sau khi cập nhật — giống response của POST `/api/products/full`.

---

### 🔍 12.3. Lấy Chi Tiết Đầy Đủ Sản Phẩm

**Endpoint:** `GET /api/products/full/{id}`  
**Auth Required:** ❌ No (Public)

**Example:** `GET /api/products/full/10`

**Response (200 OK):** Giống response của POST `/api/products/full` — trả về đầy đủ Product + Variants + Stocks.

> 💡 **Khi nào dùng endpoint này vs `/api/products/{id}`?**
> - `/api/products/{id}` → Trang danh sách / card sản phẩm (chỉ cần info cơ bản + thumbnailUrl)
> - `/api/products/full/{id}` → **Trang admin chỉnh sửa sản phẩm** hoặc trang chi tiết cần đủ biến thể + stock

---

### 💻 12.4. Frontend Implementation — Admin Product Form

#### Bước 1 — Load dữ liệu phụ trợ

```javascript
// Trước khi render form tạo/sửa sản phẩm, load danh sách options
async function loadFormData() {
  const [categoriesRes, colorsRes, sizesRes] = await Promise.all([
    api.get('/categories'),
    api.get('/colors'),
    api.get('/sizes')
  ]);
  return {
    categories: categoriesRes.data,
    colors: colorsRes.data,
    sizes: sizesRes.data
  };
}
```

#### Bước 2 — Load sản phẩm để chỉnh sửa (Edit mode)

```javascript
// Chế độ chỉnh sửa: load full product detail
async function loadProductForEdit(productId) {
  const response = await api.get(`/products/full/${productId}`);
  return response.data;
  // response.data.variants[] → render danh sách màu
  // response.data.variants[i].stocks[] → render danh sách size/SKU
}
```

#### Bước 3 — Submit form tạo mới

```javascript
async function createProduct(formData) {
  // Build request body từ form state
  const request = {
    name: formData.name,
    slug: formData.slug,              // Có thể tự sinh từ name
    description: formData.description,
    categoryId: formData.categoryId,
    basePrice: formData.basePrice,
    brand: formData.brand,
    material: formData.material,
    isActive: formData.isActive ?? true,
    variants: formData.variants.map(v => ({
      id: null,                        // Luôn null khi tạo mới
      colorId: v.colorId,
      colorImageUrl: v.colorImageUrl,
      images: v.imageUrls ? JSON.stringify(v.imageUrls) : null,
      isDefault: v.isDefault,
      stocks: v.stocks.map(s => ({
        id: null,                      // Luôn null khi tạo mới
        sizeId: s.sizeId,
        stockQty: s.stockQty ?? 0,
        priceOverride: s.priceOverride || null,
        sku: s.sku
      }))
    }))
  };

  try {
    const response = await api.post('/products/full', request);
    alert('Tạo sản phẩm thành công!');
    return response.data;
  } catch (error) {
    const msg = error.response?.data?.message;
    if (msg?.includes('Slug')) alert('Slug đã tồn tại, vui lòng chọn slug khác');
    else if (msg?.includes('SKU')) alert('SKU đã tồn tại: ' + msg);
    else alert(msg || 'Lỗi khi tạo sản phẩm');
  }
}
```

#### Bước 4 — Submit form cập nhật

```javascript
async function updateProduct(productId, formData) {
  // Đối với update: giữ nguyên id của các item đã có
  // item.id = null → sẽ được tạo mới
  // item.id = <số> → sẽ được cập nhật
  const request = {
    name: formData.name,
    slug: formData.slug,
    description: formData.description,
    categoryId: formData.categoryId,
    basePrice: formData.basePrice,
    brand: formData.brand,
    material: formData.material,
    isActive: formData.isActive,
    variants: formData.variants.map(v => ({
      id: v.id ?? null,               // Nếu là variant mới → null
      colorId: v.colorId,
      colorImageUrl: v.colorImageUrl,
      images: Array.isArray(v.imageUrls)
        ? JSON.stringify(v.imageUrls)
        : v.images,
      isDefault: v.isDefault,
      stocks: v.stocks.map(s => ({
        id: s.id ?? null,             // Nếu là stock mới → null
        sizeId: s.sizeId,
        stockQty: s.stockQty,
        priceOverride: s.priceOverride || null,
        sku: s.sku
      }))
    }))
  };

  try {
    const response = await api.put(`/products/full/${productId}`, request);
    alert('Cập nhật thành công!');
    return response.data;
  } catch (error) {
    alert(error.response?.data?.message || 'Lỗi khi cập nhật sản phẩm');
  }
}
```

#### Bước 5 — Xóa variant / stock

```javascript
// Xóa một màu sắc (variant) khỏi sản phẩm
async function deleteVariant(variantId) {
  if (!confirm('Xóa màu này? Tất cả tồn kho theo size cũng bị xóa.')) return;
  try {
    await api.delete(`/product-variants/${variantId}`);
    // Reload product data
  } catch (error) {
    alert(error.response?.data?.message || 'Không thể xóa variant');
  }
}

// Xóa một dòng tồn kho (size)
async function deleteStock(stockId) {
  if (!confirm('Xóa size này?')) return;
  try {
    await api.delete(`/variant-stocks/${stockId}`);
    // Reload product data
  } catch (error) {
    alert(error.response?.data?.message || 'Không thể xóa stock');
  }
}
```

#### Tiện ích — Tự động sinh slug từ tên sản phẩm

```javascript
function generateSlug(name) {
  const map = {
    'à':'a','á':'a','ả':'a','ã':'a','ạ':'a',
    'ă':'a','ằ':'a','ắ':'a','ẳ':'a','ẵ':'a','ặ':'a',
    'â':'a','ầ':'a','ấ':'a','ẩ':'a','ẫ':'a','ậ':'a',
    'đ':'d',
    'è':'e','é':'e','ẻ':'e','ẽ':'e','ẹ':'e',
    'ê':'e','ề':'e','ế':'e','ể':'e','ễ':'e','ệ':'e',
    'ì':'i','í':'i','ỉ':'i','ĩ':'i','ị':'i',
    'ò':'o','ó':'o','ỏ':'o','õ':'o','ọ':'o',
    'ô':'o','ồ':'o','ố':'o','ổ':'o','ỗ':'o','ộ':'o',
    'ơ':'o','ờ':'o','ớ':'o','ở':'o','ỡ':'o','ợ':'o',
    'ù':'u','ú':'u','ủ':'u','ũ':'u','ụ':'u',
    'ư':'u','ừ':'u','ứ':'u','ử':'u','ữ':'u','ự':'u',
    'ỳ':'y','ý':'y','ỷ':'y','ỹ':'y','ỵ':'y'
  };
  return name
    .toLowerCase()
    .split('')
    .map(c => map[c] || c)
    .join('')
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-');
}

// Ví dụ: generateSlug("Áo Thun Nike Basic") → "ao-thun-nike-basic"
```

---

### 📋 12.5. Luồng Tạo Sản Phẩm Hoàn Chỉnh (Admin)

```
1. Upload ảnh thumbnail + gallery
   POST /api/files/image  (1 ảnh)       → lấy colorImageUrl
   POST /api/files/images (nhiều ảnh)   → lấy images JSON array

2. Load danh sách phụ trợ
   GET /api/categories   → categoryId options
   GET /api/colors       → colorId options
   GET /api/sizes        → sizeId options

3. Admin điền form và submit
   POST /api/products/full
   → Tạo Product + Variants + Stocks trong 1 request

4. Redirect về trang chi tiết sản phẩm
   GET /api/products/full/{newProductId}
```

---

### 🔑 12.6. Field `effectivePrice` trong Response

Mỗi `stock` trong response có field `effectivePrice` — giá thực tế áp dụng cho size đó:

```
effectivePrice = priceOverride != null ? priceOverride : product.basePrice
```

| priceOverride | basePrice | effectivePrice |
|---------------|-----------|----------------|
| `null` | 250,000 | 250,000 |
| 280,000 | 250,000 | **280,000** |
| 0 | 250,000 | **0** (miễn phí!) |

> 💡 FE nên hiển thị `effectivePrice` (đã được tính sẵn từ BE), không cần tự tính lại.

---

### 📤 12.7. Xử Lý Field `images` (JSON String)

`images` trong variant là **chuỗi JSON**, không phải mảng trực tiếp:

```javascript
// Parse khi hiển thị
const imageUrls = variant.images ? JSON.parse(variant.images) : [];
// → ["/uploads/images/img1.jpg", "/uploads/images/img2.jpg"]

// Stringify khi gửi lên
const imagesJson = JSON.stringify(uploadedUrls);
// → "["/uploads/images/img1.jpg","/uploads/images/img2.jpg"]"
```

---

## 🎉 CONCLUSION

Document này cung cấp đầy đủ thông tin để Frontend tích hợp với Backend API. Nếu có vấn đề:

1. Kiểm tra Swagger UI để xem API spec chi tiết
2. Kiểm tra response/error message
3. Liên hệ Backend team để support

**Happy Coding! 🚀**

