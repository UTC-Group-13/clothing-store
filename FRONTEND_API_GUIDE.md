# 📖 Frontend API Integration Guide – Clothing Store

> **Base URL:** `http://localhost:8080`  
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`  
> **Content-Type:** `application/json` (trừ upload file)

---

## 📋 Mục lục

1. [Cấu trúc Response chung](#1-cấu-trúc-response-chung)
2. [Xác thực JWT](#2-xác-thực-jwt)
3. [Authentication API](#3-authentication-api)
4. [Category API](#4-category-api)
5. [Color API](#5-color-api)
6. [Size API](#6-size-api)
7. [Product API](#7-product-api)
8. [Product Variant API](#8-product-variant-api)
9. [Variant Stock API](#9-variant-stock-api)
10. [File Upload API](#10-file-upload-api)
11. [Shopping Cart API](#11-shopping-cart-api)
12. [Payment Type API](#12-payment-type-api)
13. [Payment Method API](#13-payment-method-api)
14. [Shipping Method API](#14-shipping-method-api)
15. [Order Status API](#15-order-status-api)
16. [Order API](#16-order-api)
17. [Luồng tích hợp FE điển hình](#17-luồng-tích-hợp-fe-điển-hình)
18. [Xử lý lỗi](#18-xử-lý-lỗi)

---

## 1. Cấu trúc Response chung

**Tất cả API** đều trả về cùng 1 cấu trúc `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Thông báo kết quả",
  "errorCode": null,
  "data": { ... },
  "timestamp": "2026-03-24T10:00:00Z"
}
```

| Field | Type | Ý nghĩa |
|-------|------|---------|
| `success` | `boolean` | `true` = thành công, `false` = lỗi |
| `message` | `string` | Thông báo cho người dùng |
| `errorCode` | `string \| null` | Code lỗi khi thất bại (ví dụ: `"NOT_FOUND"`) |
| `data` | `T \| null` | Dữ liệu trả về (null nếu lỗi) |
| `timestamp` | `string` | Thời điểm xử lý |

**Response có phân trang** (`PagedResponse<T>`):

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

---

## 2. Xác thực JWT

### Cách lấy token
Gọi API `POST /api/auth/login` → lấy `data.accessToken`.

### Cách đính kèm token
Thêm header vào **mọi request cần xác thực**:

```
Authorization: Bearer <accessToken>
Bypass-Auth: false
```

> ⚠️ **Quan trọng:** Header `Bypass-Auth: false` **bắt buộc phải có** để JWT được xử lý.

### Ví dụ với Axios (JavaScript):

```javascript
// Cấu hình Axios interceptor
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
    config.headers['Bypass-Auth'] = 'false';
  }
  return config;
});
```

### Phân quyền
| Role | Quyền |
|------|-------|
| `USER` | Xem sản phẩm, quản lý giỏ hàng, đặt hàng, xem đơn hàng của mình |
| `ADMIN` | Tất cả quyền USER + CRUD sản phẩm/danh mục/kho + quản lý tất cả đơn hàng |

---

## 3. Authentication API

### 3.1 Đăng ký

```
POST /api/auth/register
```

**Request Body:**
```json
{
  "username": "john_doe",
  "emailAddress": "john@example.com",
  "phoneNumber": "0901234567",
  "password": "password123"
}
```

**Validation:**
- `username`: bắt buộc, 3–50 ký tự
- `emailAddress`: format email hợp lệ
- `password`: bắt buộc, tối thiểu 6 ký tự

**Response (201):**
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

---

### 3.2 Đăng nhập

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (200):** *(cấu trúc giống đăng ký)*

---

### 3.3 Đổi mật khẩu

```
POST /api/auth/change-password
Authorization: Bearer <token>
Bypass-Auth: false
```

**Request Body:**
```json
{
  "userId": 1,
  "username": "john_doe",
  "oldPassword": "password123",
  "newPassword": "newpass456",
  "verifyPassword": "newpass456"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công",
  "data": ""
}
```

---

## 4. Category API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: cần token ADMIN

### 4.1 Lấy tất cả danh mục

```
GET /api/categories
```

**Response:**
```json
{
  "data": [
    { "id": 1, "name": "Áo", "slug": "ao", "parentId": null, "description": "..." },
    { "id": 2, "name": "Áo thun", "slug": "ao-thun", "parentId": 1, "description": "..." }
  ]
}
```

### 4.2 Lấy danh mục gốc (không có parent)

```
GET /api/categories/roots
```

### 4.3 Lấy danh mục con

```
GET /api/categories/{id}/children
```

### 4.4 Lấy theo slug

```
GET /api/categories/slug/{slug}
```

### 4.5 Tạo danh mục *(ADMIN)*

```
POST /api/categories
```

**Request Body:**
```json
{
  "name": "Quần",
  "slug": "quan",
  "parentId": null,
  "description": "Danh mục quần các loại"
}
```

### 4.6 Cập nhật *(ADMIN)*

```
PUT /api/categories/{id}
```

### 4.7 Xóa *(ADMIN)*

```
DELETE /api/categories/{id}
```
> ❌ Không xóa được nếu có danh mục con hoặc sản phẩm

---

## 5. Color API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN

### 5.1 Lấy tất cả màu sắc

```
GET /api/colors
```

**Response:**
```json
{
  "data": [
    { "id": 1, "name": "Đỏ", "hexCode": "#FF0000", "slug": "do" },
    { "id": 2, "name": "Xanh navy", "hexCode": "#001F5B", "slug": "xanh-navy" }
  ]
}
```

### 5.2 Lấy theo ID

```
GET /api/colors/{id}
```

### 5.3 Tạo màu sắc *(ADMIN)*

```
POST /api/colors
```

**Request Body:**
```json
{
  "name": "Vàng",
  "hexCode": "#FFD700",
  "slug": "vang"
}
```

> Validation: `hexCode` phải đúng dạng `#RRGGBB`

---

## 6. Size API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN

### 6.1 Lấy tất cả sizes

```
GET /api/sizes
```

**Response:**
```json
{
  "data": [
    { "id": 1, "label": "S", "type": "clothing", "sortOrder": 1 },
    { "id": 2, "label": "M", "type": "clothing", "sortOrder": 2 },
    { "id": 3, "label": "L", "type": "clothing", "sortOrder": 3 },
    { "id": 4, "label": "30", "type": "numeric", "sortOrder": 1 }
  ]
}
```

### 6.2 Lấy sizes theo loại

```
GET /api/sizes/type/{type}
```

| type | Ý nghĩa |
|------|---------|
| `clothing` | S, M, L, XL, XXL |
| `numeric` | 28, 30, 32 (quần jeans) |
| `shoes` | 36, 37, 38 (giày dép) |

### 6.3 Tạo size *(ADMIN)*

```
POST /api/sizes
```

**Request Body:**
```json
{
  "label": "XL",
  "type": "clothing",
  "sortOrder": 4
}
```

---

## 7. Product API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN

### 7.1 Lấy tất cả sản phẩm

```
GET /api/products
```

### 7.2 Lấy sản phẩm có phân trang

```
GET /api/products/paged?page=0&size=10&sortBy=id&direction=ASC
```

**Query params:**
| Param | Default | Ý nghĩa |
|-------|---------|---------|
| `page` | `0` | Số trang (bắt đầu từ 0) |
| `size` | `10` | Số sản phẩm/trang |
| `sortBy` | `id` | Trường sắp xếp: `id`, `name`, `basePrice`, `createdAt` |
| `direction` | `ASC` | `ASC` hoặc `DESC` |

### 7.3 Tìm kiếm sản phẩm *(Quan trọng nhất)*

```
GET /api/products/search
```

**Query params:**
| Param | Type | Ý nghĩa |
|-------|------|---------|
| `name` | `string` | Tìm theo tên (chứa chuỗi) |
| `categoryIds` | `int[]` | Lọc theo nhiều danh mục: `?categoryIds=1&categoryIds=2` |
| `colorIds` | `int[]` | Lọc theo nhiều màu sắc |
| `minPrice` | `number` | Giá tối thiểu |
| `maxPrice` | `number` | Giá tối đa |
| `isActive` | `boolean` | Trạng thái: `true`/`false` |
| `page` | `int` | Số trang (từ 0) |
| `size` | `int` | Số item/trang |
| `sortBy` | `string` | Trường sắp xếp |
| `direction` | `string` | `ASC`/`DESC` |

**Ví dụ:**
```
GET /api/products/search?name=thun&categoryIds=1&colorIds=2&minPrice=100000&maxPrice=500000&page=0&size=12&sortBy=basePrice&direction=ASC
```

**Response data (ProductDTO):**
```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Áo Thun Basic Cotton",
        "slug": "ao-thun-basic-cotton",
        "description": "Áo thun cotton 100%",
        "categoryId": 2,
        "categoryName": "Áo thun",
        "basePrice": 199000,
        "brand": "Uniqlo",
        "material": "Cotton 100%",
        "isActive": true,
        "thumbnailUrl": "/uploads/images/ao-thun-trang.jpg",
        "createdAt": "2026-03-01T10:00:00",
        "updatedAt": "2026-03-20T08:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 12,
    "totalElements": 45,
    "totalPages": 4,
    "first": true,
    "last": false
  }
}
```

### 7.4 Lấy sản phẩm theo ID

```
GET /api/products/{id}
```

### 7.5 Lấy sản phẩm theo slug *(dùng cho trang chi tiết)*

```
GET /api/products/slug/{slug}
```

### 7.6 Lấy sản phẩm theo danh mục

```
GET /api/products/category/{categoryId}
```

### 7.7 Tạo sản phẩm *(ADMIN)*

```
POST /api/products
```

**Request Body:**
```json
{
  "name": "Áo Thun Basic Cotton",
  "slug": "ao-thun-basic-cotton",
  "description": "Áo thun cotton 100%, thoáng mát",
  "categoryId": 2,
  "basePrice": 199000,
  "brand": "Uniqlo",
  "material": "Cotton 100%",
  "isActive": true
}
```

### 7.8 Cập nhật / Xóa *(ADMIN)*

```
PUT  /api/products/{id}
DELETE /api/products/{id}
```

> ❌ Không xóa được nếu đang có biến thể

---

## 8. Product Variant API

> Biến thể = 1 Sản phẩm × 1 Màu sắc

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN

### 8.1 Lấy biến thể theo sản phẩm *(dùng cho trang chi tiết SP)*

```
GET /api/product-variants/product/{productId}
```

**Response data (ProductVariantDTO):**
```json
[
  {
    "id": 1,
    "productId": 1,
    "colorId": 1,
    "colorName": "Trắng",
    "colorHexCode": "#FFFFFF",
    "colorImageUrl": "/uploads/images/ao-thun-trang.jpg",
    "images": "[\"url1\",\"url2\"]",
    "isDefault": true
  },
  {
    "id": 2,
    "productId": 1,
    "colorId": 2,
    "colorName": "Đen",
    "colorHexCode": "#000000",
    "colorImageUrl": "/uploads/images/ao-thun-den.jpg",
    "images": null,
    "isDefault": false
  }
]
```

### 8.2 Tạo biến thể *(ADMIN)*

```
POST /api/product-variants
```

**Request Body:**
```json
{
  "productId": 1,
  "colorId": 3,
  "colorImageUrl": "/uploads/images/ao-thun-do.jpg",
  "images": "[\"/uploads/images/do-1.jpg\",\"/uploads/images/do-2.jpg\"]",
  "isDefault": false
}
```

---

## 9. Variant Stock API

> Tồn kho = 1 Biến thể (màu) × 1 Size

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN

### 9.1 Lấy tồn kho theo biến thể *(dùng để hiển thị size có sẵn)*

```
GET /api/variant-stocks/variant/{variantId}
```

**Response data (VariantStockDTO):**
```json
[
  {
    "id": 1,
    "variantId": 1,
    "sizeId": 1,
    "sizeLabel": "S",
    "sizeType": "clothing",
    "stockQty": 15,
    "priceOverride": null,
    "sku": "ATB-TRANG-S"
  },
  {
    "id": 2,
    "variantId": 1,
    "sizeId": 2,
    "sizeLabel": "M",
    "sizeType": "clothing",
    "stockQty": 8,
    "priceOverride": 219000,
    "sku": "ATB-TRANG-M"
  }
]
```

> **Lưu ý giá:** `priceOverride != null` → dùng `priceOverride`. Ngược lại dùng `basePrice` từ Product.

### 9.2 Tìm kiếm tồn kho (phân trang)

```
GET /api/variant-stocks/search?keyword=ATB&variantId=1&page=0&size=10
```

### 9.3 Tạo tồn kho *(ADMIN)*

```
POST /api/variant-stocks
```

**Request Body:**
```json
{
  "variantId": 1,
  "sizeId": 3,
  "stockQty": 20,
  "priceOverride": null,
  "sku": "ATB-TRANG-L"
}
```

---

## 10. File Upload API

> 🔒 Cần xác thực

### 10.1 Upload 1 ảnh

```
POST /api/files/image
Content-Type: multipart/form-data
Authorization: Bearer <token>
Bypass-Auth: false
```

**Form data:**
| Key | Type | Mô tả |
|-----|------|-------|
| `file` | File | File ảnh (jpeg, png, gif, webp, svg, max 10MB) |

**Response:**
```json
{
  "data": {
    "fileName": "a1b2c3d4-uuid.jpg",
    "fileUrl": "/uploads/images/a1b2c3d4-uuid.jpg",
    "contentType": "image/jpeg",
    "size": 245678
  }
}
```

### 10.2 Upload nhiều ảnh cùng lúc

```
POST /api/files/images
Content-Type: multipart/form-data
```

**Form data:** field `files[]` chứa nhiều file

**Response:**
```json
{
  "data": [
    { "fileName": "uuid1.jpg", "fileUrl": "/uploads/images/uuid1.jpg", ... },
    { "fileName": "uuid2.jpg", "fileUrl": "/uploads/images/uuid2.jpg", ... }
  ]
}
```

### 10.3 Truy cập ảnh (public)

```
GET /uploads/images/{filename}
```

> Ảnh được truy cập công khai, không cần token.

---

## 11. Shopping Cart API

> 🔒 Cần xác thực (USER)  
> Giỏ hàng tự động tạo khi user lần đầu truy cập.

### 11.1 Xem giỏ hàng

```
GET /api/cart
Authorization: Bearer <token>
Bypass-Auth: false
```

**Response data (CartSummaryDTO):**
```json
{
  "data": {
    "cartId": 5,
    "userId": 1,
    "items": [
      {
        "id": 12,
        "cartId": 5,
        "variantStockId": 2,
        "sku": "ATB-TRANG-M",
        "qty": 2,
        "unitPrice": 219000,
        "subtotal": 438000,
        "availableStock": 8,
        "productId": 1,
        "productName": "Áo Thun Basic Cotton",
        "productSlug": "ao-thun-basic-cotton",
        "variantId": 1,
        "colorName": "Trắng",
        "colorHex": "#FFFFFF",
        "colorImageUrl": "/uploads/images/ao-thun-trang.jpg",
        "sizeLabel": "M",
        "sizeType": "clothing"
      }
    ],
    "totalItems": 2,
    "totalAmount": 438000
  }
}
```

### 11.2 Thêm sản phẩm vào giỏ

```
POST /api/cart/items
```

**Request Body:**
```json
{
  "variantStockId": 2,
  "qty": 1
}
```

> Nếu sản phẩm đã có trong giỏ → **cộng thêm** số lượng.  
> Nếu tổng qty > `availableStock` → trả lỗi 400.

### 11.3 Cập nhật số lượng item

```
PUT /api/cart/items/{itemId}
```

**Request Body:**
```json
{
  "variantStockId": 2,
  "qty": 3
}
```

> `qty` phải >= 1 và <= tồn kho hiện tại.

### 11.4 Xóa 1 item khỏi giỏ

```
DELETE /api/cart/items/{itemId}
```

### 11.5 Làm trống giỏ hàng

```
DELETE /api/cart
```

---

## 12. Payment Type API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN  
> Dữ liệu tham chiếu: COD, Visa, MoMo...

### 12.1 Lấy danh sách loại thanh toán

```
GET /api/payment-types
```

**Response:**
```json
{
  "data": [
    { "id": 1, "value": "COD" },
    { "id": 2, "value": "Visa/Mastercard" },
    { "id": 3, "value": "MoMo" },
    { "id": 4, "value": "ZaloPay" }
  ]
}
```

---

## 13. Payment Method API

> 🔒 Cần xác thực (USER)  
> Quản lý phương thức thanh toán đã lưu của user.

### 13.1 Lấy danh sách PTTT của tôi

```
GET /api/payment-methods
```

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "userId": 1,
      "paymentTypeId": 2,
      "provider": "Vietcombank",
      "accountNumber": "4111111111111111",
      "expiryDate": "2028-12-01",
      "isDefault": 1
    }
  ]
}
```

### 13.2 Thêm phương thức thanh toán

```
POST /api/payment-methods
```

**Request Body:**
```json
{
  "paymentTypeId": 3,
  "provider": "MoMo",
  "accountNumber": "0901234567",
  "expiryDate": null
}
```

> Phương thức đầu tiên tự động được đặt làm **mặc định**.

### 13.3 Cập nhật

```
PUT /api/payment-methods/{id}
```

### 13.4 Xóa

```
DELETE /api/payment-methods/{id}
```

> ❌ Không xóa được nếu đã dùng trong đơn hàng.

### 13.5 Đặt làm mặc định

```
PATCH /api/payment-methods/{id}/default
```

---

## 14. Shipping Method API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN

### 14.1 Lấy danh sách phương thức vận chuyển

```
GET /api/shipping-methods
```

**Response:**
```json
{
  "data": [
    { "id": 1, "name": "Giao hàng tiêu chuẩn", "price": 20000 },
    { "id": 2, "name": "Giao hàng nhanh", "price": 35000 },
    { "id": 3, "name": "Giao hàng hỏa tốc", "price": 60000 }
  ]
}
```

---

## 15. Order Status API

> 🔓 GET: public | 🔒 POST/PUT/DELETE: ADMIN  
> 5 trạng thái mặc định được **tự động tạo** khi app khởi động.

### 15.1 Lấy danh sách trạng thái

```
GET /api/order-statuses
```

**Response:**
```json
{
  "data": [
    { "id": 1, "status": "PENDING" },
    { "id": 2, "status": "PROCESSING" },
    { "id": 3, "status": "SHIPPED" },
    { "id": 4, "status": "DELIVERED" },
    { "id": 5, "status": "CANCELLED" }
  ]
}
```

| Status | Ý nghĩa |
|--------|---------|
| `PENDING` | Chờ xác nhận |
| `PROCESSING` | Đang xử lý / đóng gói |
| `SHIPPED` | Đang giao hàng |
| `DELIVERED` | Đã giao thành công |
| `CANCELLED` | Đã hủy |

---

## 16. Order API

> 🔒 Cần xác thực (USER / ADMIN)

### 16.1 Đặt hàng

```
POST /api/orders
Authorization: Bearer <token>
Bypass-Auth: false
```

**Trước khi gọi cần có:**
- ✅ Giỏ hàng không trống
- ✅ `paymentMethodId` thuộc về user
- ✅ `shippingAddressId` thuộc về user (có trong bảng `user_address`)
- ✅ `shippingMethodId` tồn tại

**Request Body:**
```json
{
  "paymentMethodId": 1,
  "shippingAddressId": 3,
  "shippingMethodId": 2
}
```

**Response (201) – OrderDetailDTO:**
```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "id": 101,
    "userId": 1,
    "orderDate": "2026-03-24T14:30:00",
    "statusId": 1,
    "statusName": "PENDING",
    "paymentMethodId": 1,
    "shippingMethodId": 2,
    "shippingMethodName": "Giao hàng nhanh",
    "shippingFee": 35000,
    "shippingAddressId": 3,
    "shippingAddressDetail": {
      "id": 3,
      "unitNumber": "A1",
      "streetNumber": "12",
      "addressLine1": "Đường Lê Lợi",
      "addressLine2": "Phường Bến Nghé",
      "city": "TP. Hồ Chí Minh",
      "region": "Hồ Chí Minh",
      "postalCode": "70000",
      "countryId": 1
    },
    "subtotal": 438000,
    "orderTotal": 473000,
    "items": [
      {
        "id": 1,
        "variantStockId": 2,
        "sku": "ATB-TRANG-M",
        "qty": 2,
        "price": 219000,
        "subtotal": 438000,
        "productId": 1,
        "productName": "Áo Thun Basic Cotton",
        "productSlug": "ao-thun-basic-cotton",
        "variantId": 1,
        "colorName": "Trắng",
        "colorHex": "#FFFFFF",
        "colorImageUrl": "/uploads/images/ao-thun-trang.jpg",
        "sizeLabel": "M",
        "sizeType": "clothing"
      }
    ]
  }
}
```

### 16.2 Lịch sử đơn hàng của tôi

```
GET /api/orders
```

**Response:** `data` là mảng `OrderDetailDTO[]`, sắp xếp mới nhất trước.

### 16.3 Chi tiết 1 đơn hàng

```
GET /api/orders/{orderId}
```

### 16.4 Hủy đơn hàng

```
PATCH /api/orders/{orderId}/cancel
```

> ✅ Chỉ hủy được khi status = `PENDING`  
> Tự động hoàn trả tồn kho sau khi hủy.

**Response:** `OrderDetailDTO` với `statusName: "CANCELLED"`

---

### 16.5 [ADMIN] Lấy tất cả đơn hàng

```
GET /api/orders/admin/all?page=0&size=20
```

### 16.6 [ADMIN] Lọc đơn theo trạng thái

```
GET /api/orders/admin/by-status/{statusId}?page=0&size=20
```

### 16.7 [ADMIN] Xem bất kỳ đơn hàng

```
GET /api/orders/admin/{orderId}
```

### 16.8 [ADMIN] Cập nhật trạng thái đơn hàng

```
PATCH /api/orders/admin/{orderId}/status
```

**Request Body:**
```json
{
  "statusId": 2
}
```

---

## 17. Luồng tích hợp FE điển hình

### 🛒 Luồng khách hàng mua hàng

```
1. TRANG CHỦ / DANH MỤC
   GET /api/categories/roots              → Hiển thị menu danh mục
   GET /api/products/search?...           → Danh sách sản phẩm (lọc + phân trang)

2. TRANG CHI TIẾT SẢN PHẨM
   GET /api/products/slug/{slug}          → Thông tin cơ bản sản phẩm
   GET /api/product-variants/product/{id} → Danh sách màu có sẵn
   GET /api/variant-stocks/variant/{id}   → Tồn kho theo size cho từng màu

3. THÊM VÀO GIỎ (cần đăng nhập)
   POST /api/cart/items                   → Thêm { variantStockId, qty }

4. TRANG GIỎ HÀNG
   GET /api/cart                          → Xem giỏ (ảnh, tên, màu, size, giá)
   PUT /api/cart/items/{id}               → Sửa số lượng
   DELETE /api/cart/items/{id}            → Xóa item

5. TRANG THANH TOÁN
   GET /api/shipping-methods              → Chọn hãng vận chuyển
   GET /api/payment-methods               → Chọn PTTT đã lưu
   GET /api/payment-types                 → (nếu muốn thêm PTTT mới)

6. ĐẶT HÀNG
   POST /api/orders                       → { paymentMethodId, shippingAddressId, shippingMethodId }

7. TRANG ĐƠN HÀNG
   GET /api/orders                        → Lịch sử đơn
   GET /api/orders/{id}                   → Chi tiết đơn
   PATCH /api/orders/{id}/cancel          → Hủy đơn (nếu PENDING)
```

### 🔧 Luồng Admin quản lý

```
1. QUẢN LÝ DANH MỤC
   POST /api/categories                   → Tạo danh mục
   PUT /api/categories/{id}               → Cập nhật
   DELETE /api/categories/{id}            → Xóa

2. QUẢN LÝ SẢN PHẨM
   POST /api/files/image                  → Upload ảnh trước
   POST /api/products                     → Tạo sản phẩm
   POST /api/product-variants             → Thêm màu sắc
   POST /api/variant-stocks               → Thêm tồn kho từng size

3. QUẢN LÝ ĐƠN HÀNG
   GET /api/orders/admin/all              → Xem tất cả đơn
   GET /api/orders/admin/by-status/1      → Lọc đơn PENDING
   PATCH /api/orders/admin/{id}/status    → Cập nhật trạng thái
```

---

## 18. Xử lý lỗi

### Các mã lỗi thường gặp

| HTTP | `errorCode` | Nguyên nhân |
|------|-------------|-------------|
| `400` | `BAD_REQUEST` | Request sai format / validation |
| `401` | `UNAUTHORIZED` | Chưa đăng nhập / token hết hạn |
| `403` | `FORBIDDEN` | Không có quyền (cần ADMIN) |
| `404` | `NOT_FOUND` | Không tìm thấy resource |
| `500` | `INTERNAL_SERVER_ERROR` | Lỗi server |

### Response lỗi mẫu

```json
{
  "success": false,
  "message": "Không tìm thấy sản phẩm với ID: 999",
  "errorCode": "NOT_FOUND",
  "data": null,
  "timestamp": "2026-03-24T10:00:00Z"
}
```

### Lỗi validation (400)

```json
{
  "success": false,
  "message": "Dữ liệu đầu vào không hợp lệ",
  "errorCode": "VALIDATION_ERROR",
  "data": "username: Username không được để trống, password: Password phải có ít nhất 6 ký tự",
  "timestamp": "2026-03-24T10:00:00Z"
}
```

### Ví dụ xử lý lỗi với Axios

```javascript
const api = axios.create({ baseURL: 'http://localhost:8080' });

api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
    config.headers['Bypass-Auth'] = 'false';
  }
  return config;
});

api.interceptors.response.use(
  response => response.data,  // Trả về ApiResponse<T>
  error => {
    const { status, data } = error.response;
    if (status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(data); // { success: false, message, errorCode }
  }
);
```

### Ví dụ gọi API với error handling

```javascript
// Đặt hàng
async function placeOrder(orderData) {
  try {
    const response = await api.post('/api/orders', orderData);
    if (response.success) {
      console.log('Đặt hàng thành công:', response.data);
      return response.data; // OrderDetailDTO
    }
  } catch (error) {
    // error = { success: false, message: "...", errorCode: "..." }
    alert(error.message); // Ví dụ: "Sản phẩm ATB-TRANG-M chỉ còn 3 cái trong kho."
  }
}
```

---

## 📌 Ghi chú quan trọng cho FE

1. **Token lưu ở đâu?** Recommend `localStorage` hoặc `httpOnly cookie`.

2. **Giá sản phẩm:** Kiểm tra `priceOverride` trước:
   ```javascript
   const price = stock.priceOverride ?? product.basePrice;
   ```

3. **Ảnh:** URL từ API là đường dẫn tương đối `/uploads/images/...`
   ```javascript
   const imgUrl = `${BASE_URL}${product.thumbnailUrl}`;
   // → http://localhost:8080/uploads/images/uuid.jpg
   ```

4. **Phân trang:** Trang bắt đầu từ `0` (không phải `1`).

5. **Hủy đơn:** Chỉ được hủy khi `statusName === 'PENDING'`.

6. **Địa chỉ giao hàng:** Cần được thêm vào bảng `user_address` trước khi đặt hàng.

7. **Auto-create Cart:** Giỏ hàng tự động tạo lần đầu khi `GET /api/cart`.

