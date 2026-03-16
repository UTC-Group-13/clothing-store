# 🛍️ Luồng Tạo Sản Phẩm — Clothing Store API

> Tài liệu mô tả toàn bộ luồng API để tạo một sản phẩm hoàn chỉnh,  
> từ lúc đăng nhập đến khi sản phẩm có đầy đủ biến thể và thuộc tính.

---

## 📐 Sơ Đồ Quan Hệ Dữ Liệu

```
product_category  ◄──────────────────  variation
      │  (1:N)                              │  (1:N)
      ▼                                     ▼
   product                          variation_option
      │  (1:N)                              │
      ▼                                     │  (N:M)
  product_item  ◄────────────────────────── ┘
                    product_configuration
              (product_item_id + variation_option_id)
```

**Quy tắc quan trọng:**
- `product` — thông tin chung, **không có giá**
- `product_item` — biến thể cụ thể, **có giá, SKU, tồn kho**
- `variation` — loại thuộc tính (Màu sắc, Size...) gắn với **category**
- `product_configuration` — bảng nối, xác định biến thể có thuộc tính gì

---

## 🔑 Bước 0 — Đăng Nhập Lấy JWT Token

> Tất cả API tạo/sửa/xóa đều yêu cầu JWT token trong header.

### Request
```http
POST /api/auth/login
Content-Type: application/json
```
```json
{
  "username": "admin",
  "password": "your_password"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  }
}
```

### Sử dụng token
Thêm vào header của tất cả request tiếp theo:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 📋 Tổng Quan 6 Bước

```
[Bước 1]  Tạo product_category (danh mục)
              ↓
[Bước 2]  Tạo variation (loại thuộc tính: Màu Sắc, Size)
              ↓
[Bước 3]  Tạo variation_option (giá trị: Đỏ, Xanh, S, M, XL)
              ↓
[Bước 4]  Tạo product (sản phẩm gốc)
              ↓
[Bước 5]  Tạo product_item (biến thể: SKU, giá, tồn kho)
              ↓
[Bước 6]  Tạo product_configuration (gắn biến thể với thuộc tính)
```

> ⚠️ **Thứ tự bắt buộc** — Tạo sai thứ tự sẽ lỗi Foreign Key Constraint.

---

## 🔵 Bước 1 — Tạo Danh Mục Sản Phẩm

### 1a. Tạo danh mục gốc
```http
POST /api/product-categories
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "categoryName": "Áo",
  "parentCategoryId": null
}
```
```json
{
  "success": true,
  "message": "Tạo danh mục sản phẩm thành công.",
  "data": {
    "id": 1,
    "categoryName": "Áo",
    "parentCategoryId": null
  }
}
```

### 1b. Tạo danh mục con
```http
POST /api/product-categories
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "categoryName": "Áo Thun",
  "parentCategoryId": 1
}
```
```json
{
  "success": true,
  "message": "Tạo danh mục sản phẩm thành công.",
  "data": {
    "id": 2,
    "categoryName": "Áo Thun",
    "parentCategoryId": 1
  }
}
```

### Validation
| Rule | Mô tả |
|---|---|
| `categoryName` bắt buộc | Không được để trống |
| `categoryName` ≤ 200 ký tự | Giới hạn độ dài |
| Tên không trùng cùng cấp | Trong cùng `parentCategoryId` không được trùng tên |
| `parentCategoryId` phải tồn tại | Nếu cung cấp, phải là ID hợp lệ |

---

## 🟡 Bước 2 — Tạo Thuộc Tính Biến Thể (Variation)

> Variation gắn với `categoryId` — thuộc tính chỉ áp dụng cho danh mục đó.

### Tạo variation "Màu Sắc"
```http
POST /api/variations
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "categoryId": 2,
  "name": "Màu Sắc"
}
```
```json
{
  "success": true,
  "message": "Tạo thuộc tính biến thể thành công.",
  "data": {
    "id": 1,
    "categoryId": 2,
    "name": "Màu Sắc"
  }
}
```

### Tạo variation "Kích Cỡ"
```json
{
  "categoryId": 2,
  "name": "Kích Cỡ"
}
```
```json
{
  "data": { "id": 2, "categoryId": 2, "name": "Kích Cỡ" }
}
```

### Validation
| Rule | Mô tả |
|---|---|
| `categoryId` bắt buộc | Phải tồn tại trong `product_category` |
| `name` bắt buộc, ≤ 500 ký tự | Không được để trống |
| Tên không trùng trong cùng category | Không thể có 2 variation "Màu Sắc" trong cùng 1 category |

---

## 🟠 Bước 3 — Tạo Giá Trị Thuộc Tính (Variation Option)

### Tạo các giá trị cho "Màu Sắc" (variationId = 1)
```http
POST /api/variation-options
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{ "variationId": 1, "value": "Đỏ" }
```
→ `{ "id": 1, "variationId": 1, "value": "Đỏ" }`

```json
{ "variationId": 1, "value": "Xanh" }
```
→ `{ "id": 2, "variationId": 1, "value": "Xanh" }`

```json
{ "variationId": 1, "value": "Trắng" }
```
→ `{ "id": 3, "variationId": 1, "value": "Trắng" }`

### Tạo các giá trị cho "Kích Cỡ" (variationId = 2)
```json
{ "variationId": 2, "value": "S" }
```
→ `{ "id": 4, "variationId": 2, "value": "S" }`

```json
{ "variationId": 2, "value": "M" }
```
→ `{ "id": 5, "variationId": 2, "value": "M" }`

```json
{ "variationId": 2, "value": "XL" }
```
→ `{ "id": 6, "variationId": 2, "value": "XL" }`

### Validation
| Rule | Mô tả |
|---|---|
| `variationId` bắt buộc | Phải tồn tại trong `variation` |
| `value` bắt buộc, ≤ 200 ký tự | Không được để trống |
| Value không trùng trong cùng variation | Không thể có 2 option "Đỏ" trong cùng 1 variation |

---

## 🟢 Bước 4 — Tạo Sản Phẩm (Product)

> Product là thông tin tổng quát, **chưa có giá và SKU** — đó là nhiệm vụ của Product Item.

```http
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "categoryId": 2,
  "name": "Áo Thun Basic",
  "description": "Áo thun cotton 100%, thoáng mát, phù hợp mặc hàng ngày",
  "productImage": "/uploads/images/ao-thun-basic.jpg"
}
```
```json
{
  "success": true,
  "message": "Tạo sản phẩm thành công.",
  "data": {
    "id": 10,
    "categoryId": 2,
    "name": "Áo Thun Basic",
    "description": "Áo thun cotton 100%, thoáng mát, phù hợp mặc hàng ngày",
    "productImage": "/uploads/images/ao-thun-basic.jpg"
  }
}
```

### Upload ảnh trước khi tạo sản phẩm (nếu cần)
```http
POST /api/files/image
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: [chọn file ảnh]
```
```json
{
  "success": true,
  "data": {
    "fileName": "abc123.jpg",
    "fileUrl": "/uploads/images/abc123.jpg"
  }
}
```
Sau đó dùng `fileUrl` làm giá trị `productImage`.

### Validation
| Rule | Mô tả |
|---|---|
| `name` bắt buộc, ≤ 500 ký tự | Không được để trống |
| `description` ≤ 4000 ký tự | Tùy chọn |
| `categoryId` phải tồn tại | Nếu cung cấp, phải là ID hợp lệ |

---

## 🔴 Bước 5 — Tạo Biến Thể Sản Phẩm (Product Item)

> Mỗi tổ hợp thuộc tính (Màu + Size) = **1 Product Item riêng biệt**.  
> Product Item mới có giá, SKU, số lượng tồn kho.

### Biến thể 1: Đỏ - M (productId = 10)
```http
POST /api/product-items
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "productId": 10,
  "sku": "ATB-RED-M",
  "qtyInStock": 50,
  "price": 150000,
  "productImage": "/uploads/images/ao-thun-do-m.jpg"
}
```
```json
{
  "success": true,
  "message": "Tạo biến thể sản phẩm thành công.",
  "data": {
    "id": 101,
    "productId": 10,
    "sku": "ATB-RED-M",
    "qtyInStock": 50,
    "price": 150000,
    "productImage": "/uploads/images/ao-thun-do-m.jpg"
  }
}
```

### Biến thể 2: Xanh - XL (productId = 10)
```json
{
  "productId": 10,
  "sku": "ATB-BLUE-XL",
  "qtyInStock": 30,
  "price": 155000,
  "productImage": "/uploads/images/ao-thun-xanh-xl.jpg"
}
```
→ `{ "id": 102, ... }`

### Biến thể 3: Trắng - S (productId = 10)
```json
{
  "productId": 10,
  "sku": "ATB-WHITE-S",
  "qtyInStock": 20,
  "price": 145000,
  "productImage": "/uploads/images/ao-thun-trang-s.jpg"
}
```
→ `{ "id": 103, ... }`

### Validation
| Rule | Mô tả |
|---|---|
| `productId` bắt buộc | Phải tồn tại trong `product` |
| `sku` bắt buộc, ≤ 20 ký tự | Không được để trống, phải **duy nhất** toàn hệ thống |
| `price` bắt buộc, ≥ 0 | Không được âm |
| `qtyInStock` ≥ 0 | Không được âm |

---

## ⚫ Bước 6 — Tạo Cấu Hình Sản Phẩm (Product Configuration)

> Bước này **gắn kết** Product Item với các Variation Option.  
> Xác định: "Biến thể 101 có thuộc tính gì?"

```http
POST /api/product-configurations
Authorization: Bearer <token>
Content-Type: application/json
```

### Gắn biến thể 101 (Đỏ - M)
```json
{ "productItemId": 101, "variationOptionId": 1 }
```
→ Gắn với **Màu Đỏ** (option id=1)

```json
{ "productItemId": 101, "variationOptionId": 5 }
```
→ Gắn với **Size M** (option id=5)

### Gắn biến thể 102 (Xanh - XL)
```json
{ "productItemId": 102, "variationOptionId": 2 }
```
→ Gắn với **Màu Xanh** (option id=2)

```json
{ "productItemId": 102, "variationOptionId": 6 }
```
→ Gắn với **Size XL** (option id=6)

### Gắn biến thể 103 (Trắng - S)
```json
{ "productItemId": 103, "variationOptionId": 3 }
```
→ Gắn với **Màu Trắng** (option id=3)

```json
{ "productItemId": 103, "variationOptionId": 4 }
```
→ Gắn với **Size S** (option id=4)

### Response
```json
{
  "success": true,
  "message": "Tạo cấu hình sản phẩm thành công.",
  "data": {
    "productItemId": 101,
    "variationOptionId": 1
  }
}
```

### Validation
| Rule | Mô tả |
|---|---|
| `productItemId` bắt buộc | Phải tồn tại trong `product_item` |
| `variationOptionId` bắt buộc | Phải tồn tại trong `variation_option` |
| Không trùng lặp | Cùng `(productItemId, variationOptionId)` chỉ tạo được 1 lần |

---

## 📊 Kết Quả Sau Khi Hoàn Thành

```
product_category
├── id=1  │ "Áo"                     (root)
└── id=2  │ "Áo Thun"                (parent=1)

variation
├── id=1  │ category_id=2 │ "Màu Sắc"
└── id=2  │ category_id=2 │ "Kích Cỡ"

variation_option
├── id=1  │ variation_id=1 │ "Đỏ"
├── id=2  │ variation_id=1 │ "Xanh"
├── id=3  │ variation_id=1 │ "Trắng"
├── id=4  │ variation_id=2 │ "S"
├── id=5  │ variation_id=2 │ "M"
└── id=6  │ variation_id=2 │ "XL"

product
└── id=10 │ category_id=2 │ "Áo Thun Basic"

product_item
├── id=101 │ product_id=10 │ sku="ATB-RED-M"    │ price=150,000 │ qty=50
├── id=102 │ product_id=10 │ sku="ATB-BLUE-XL"  │ price=155,000 │ qty=30
└── id=103 │ product_id=10 │ sku="ATB-WHITE-S"  │ price=145,000 │ qty=20

product_configuration
├── item=101 → option=1  (Màu Đỏ)
├── item=101 → option=5  (Size M)
├── item=102 → option=2  (Màu Xanh)
├── item=102 → option=6  (Size XL)
├── item=103 → option=3  (Màu Trắng)
└── item=103 → option=4  (Size S)
```

---

## 🔄 Luồng Đọc Lại Sản Phẩm

Sau khi tạo xong, để hiển thị đầy đủ thông tin sản phẩm:

```
1. GET /api/products/10
   → Lấy thông tin sản phẩm gốc

2. GET /api/product-items/product/10
   → Lấy danh sách tất cả biến thể của sản phẩm

3. GET /api/product-configurations/product-item/101
   → Lấy danh sách thuộc tính của biến thể 101

4. GET /api/product-categories/2
   → Lấy thông tin danh mục

5. GET /api/variations/category/2
   → Lấy tất cả loại thuộc tính của danh mục

6. GET /api/variation-options/variation/1
   → Lấy tất cả giá trị của thuộc tính "Màu Sắc"
```

---

## 🗑️ Luồng Xóa Sản Phẩm

> **Phải xóa theo thứ tự ngược lại** vì ràng buộc Foreign Key.

```
[Bước 1]  DELETE /api/product-configurations/{productItemId}/{variationOptionId}
              ↓  (xóa tất cả configuration của từng item)
[Bước 2]  DELETE /api/product-items/{id}
              ↓  (xóa tất cả product item)
[Bước 3]  DELETE /api/products/{id}
              ↓  (xóa product)
[Bước 4]  DELETE /api/variation-options/{id}
              ↓  (xóa variation option nếu không dùng ở nơi khác)
[Bước 5]  DELETE /api/variations/{id}
              ↓  (xóa variation nếu không có option nào)
[Bước 6]  DELETE /api/product-categories/{id}
              ↓  (xóa category nếu không có sản phẩm và không có category con)
```

### Business Rules khi xóa
| Đối tượng | Điều kiện chặn xóa |
|---|---|
| `product_category` | Còn danh mục con **hoặc** còn sản phẩm |
| `product` | Còn product item |
| `product_item` | Còn product configuration |
| `variation` | Còn variation option |
| `variation_option` | Đang được dùng trong product configuration |

---

## ⚠️ Các Lỗi Thường Gặp

### 401 Unauthorized
```json
{ "success": false, "errorCode": "UNAUTHORIZED", "message": "..." }
```
→ Token hết hạn hoặc không có token. Đăng nhập lại.

### 404 Not Found
```json
{ "success": false, "message": "Không tìm thấy sản phẩm với ID: 99" }
```
→ ID không tồn tại trong database.

### 400 Bad Request — Validation
```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Dữ liệu đầu vào không hợp lệ.",
  "data": {
    "name": "Tên sản phẩm không được để trống",
    "price": "Giá không được âm"
  }
}
```
→ Thiếu field bắt buộc hoặc sai định dạng.

### 409 / 422 — Business Rule
```json
{ "success": false, "message": "Mã SKU đã tồn tại: ATB-RED-M" }
```
→ Vi phạm quy tắc nghiệp vụ (trùng SKU, trùng tên...).

---

## 📌 Checklist Tạo Sản Phẩm Hoàn Chỉnh

- [ ] **Bước 0** — Đăng nhập, lưu JWT token
- [ ] **Bước 1** — Tạo/kiểm tra danh mục (root + sub nếu cần)
- [ ] **Bước 2** — Tạo variations cho danh mục (Màu Sắc, Kích Cỡ...)
- [ ] **Bước 3** — Tạo variation options (Đỏ, Xanh, S, M, XL...)
- [ ] **Upload ảnh** — Upload ảnh sản phẩm và ảnh từng biến thể
- [ ] **Bước 4** — Tạo product (tên, mô tả, ảnh đại diện, category)
- [ ] **Bước 5** — Tạo product items (1 item = 1 tổ hợp thuộc tính: SKU, giá, tồn kho, ảnh)
- [ ] **Bước 6** — Tạo product configurations (gắn mỗi item với đúng các options)
- [ ] **Kiểm tra** — `GET /api/product-items/product/{id}` để xác nhận

---

## 🔗 Quick Reference — Tất Cả API Liên Quan

| # | Method | Endpoint | Auth | Mô tả |
|---|---|---|---|---|
| 0 | POST | `/api/auth/login` | Public | Lấy JWT token |
| 1a | POST | `/api/product-categories` | 🔒 | Tạo danh mục |
| 1b | GET | `/api/product-categories/roots` | Public | Xem danh mục gốc |
| 1c | GET | `/api/product-categories/{id}/children` | Public | Xem danh mục con |
| 2a | POST | `/api/variations` | 🔒 | Tạo variation |
| 2b | GET | `/api/variations/category/{categoryId}` | 🔒 | Xem variations theo category |
| 3a | POST | `/api/variation-options` | 🔒 | Tạo variation option |
| 3b | GET | `/api/variation-options/variation/{variationId}` | 🔒 | Xem options theo variation |
| 4a | POST | `/api/files/image` | 🔒 | Upload ảnh |
| 4b | POST | `/api/products` | 🔒 | Tạo sản phẩm |
| 4c | GET | `/api/products/{id}` | Public | Xem sản phẩm |
| 5a | POST | `/api/product-items` | 🔒 | Tạo biến thể |
| 5b | GET | `/api/product-items/product/{productId}` | 🔒 | Xem biến thể theo sản phẩm |
| 6a | POST | `/api/product-configurations` | 🔒 | Gắn biến thể với thuộc tính |
| 6b | GET | `/api/product-configurations/product-item/{id}` | 🔒 | Xem configuration của biến thể |

---

*Tài liệu được tạo dựa trên source code thực tế của project Clothing Store API — March 2026*

