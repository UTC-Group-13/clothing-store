# LUỒNG TẠO VÀ XEM, LỌC SẢN PHẨM

> **Clothing Store API** — Tài liệu luồng hoạt động Product Catalog  
> Cập nhật: 2026-03-23

---

## 📊 MÔ HÌNH DỮ LIỆU

```
categories (danh mục cha/con)
    │
    └──► products (sản phẩm: tên, slug, giá gốc, brand, chất liệu)
            │
            └──► product_variants (biến thể = sản phẩm × màu sắc)
                    │
                    └──► variant_stocks (tồn kho = biến thể × size + SKU + giá riêng)

colors  ──────────► product_variants.color_id
sizes   ──────────► variant_stocks.size_id
```

### Sơ đồ ER (Product Domain)

```
┌──────────────┐       ┌──────────────────────────────────────────────┐
│  categories  │       │                  products                    │
├──────────────┤       ├──────────────────────────────────────────────┤
│ id       PK  │◄──┐   │ id            PK                             │
│ name         │   │   │ name                                         │
│ slug     UQ  │   │   │ slug          UQ                             │
│ parent_id FK─┼───┘   │ description                                  │
│ description  │       │ category_id   FK ──► categories.id           │
│ created_at   │       │ base_price    DECIMAL(12,2)                  │
└──────────────┘       │ brand                                        │
                       │ material                                     │
┌──────────────┐       │ is_active     BOOLEAN                        │
│    colors    │       │ created_at                                   │
├──────────────┤       │ updated_at                                   │
│ id       PK  │       └──────────────────────────────────────────────┘
│ name         │                        │
│ hex_code     │                        │ 1:N
│ slug     UQ  │                        ▼
└──────┬───────┘       ┌──────────────────────────────────────────────┐
       │               │            product_variants                  │
       │               ├──────────────────────────────────────────────┤
       │               │ id              PK                           │
       └───────FK──────│ product_id      FK ──► products.id           │
                       │ color_id        FK ──► colors.id             │
                       │ color_image_url VARCHAR(500)                  │
                       │ images          JSON                         │
┌──────────────┐       │ is_default      BOOLEAN                      │
│    sizes     │       │ UQ(product_id, color_id)                     │
├──────────────┤       └──────────────────────────────────────────────┘
│ id       PK  │                        │
│ label        │                        │ 1:N
│ type         │                        ▼
│ sort_order   │       ┌──────────────────────────────────────────────┐
│ UQ(label,    │       │            variant_stocks                    │
│    type)     │       ├──────────────────────────────────────────────┤
└──────┬───────┘       │ id              PK                           │
       │               │ variant_id      FK ──► product_variants.id   │
       └───────FK──────│ size_id         FK ──► sizes.id              │
                       │ stock_qty       INT                          │
                       │ price_override  DECIMAL(12,2) nullable       │
                       │ sku             UQ                           │
                       │ UQ(variant_id, size_id)                      │
                       └──────────────────────────────────────────────┘
```

---

## 🔐 PHÂN QUYỀN API

| Hành động | Quyền | Ghi chú |
|-----------|-------|---------|
| `GET` tất cả endpoints sản phẩm | **Public** | Không cần token |
| `POST / PUT / DELETE` | **Authenticated** | Cần Bearer Token (JWT) |

---

## 1️⃣ LUỒNG TẠO SẢN PHẨM (Admin)

### Thứ tự tạo bắt buộc (theo FK dependency)

```
Bước 1: Tạo Category ──► Bước 2: Tạo Color ──► Bước 3: Tạo Size
                │                   │                    │
                └───────┬───────────┘                    │
                        ▼                                │
              Bước 4: Tạo Product                        │
                        │                                │
                        ▼                                │
              Bước 5: Tạo Product Variant ◄──────────────┘
                        │                                │
                        ▼                                │
              Bước 6: Tạo Variant Stock  ◄───────────────┘
```

### Bước 1 — Tạo danh mục (`POST /api/categories`)

**Header:** `Authorization: Bearer <token>`

```json
// Tạo danh mục gốc
{
  "name": "Áo",
  "slug": "ao",
  "parentId": null,
  "description": "Tất cả các loại áo"
}
```

```json
// Tạo danh mục con
{
  "name": "Áo Thun",
  "slug": "ao-thun",
  "parentId": 1,
  "description": "Áo thun nam nữ"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Tao danh muc thanh cong.",
  "data": {
    "id": 1,
    "name": "Áo",
    "slug": "ao",
    "parentId": null,
    "description": "Tất cả các loại áo",
    "createdAt": "2026-03-23T10:00:00"
  }
}
```

**Validation:**
- `name` + `parentId` phải duy nhất
- `slug` phải duy nhất toàn bảng
- `parentId` (nếu có) phải tồn tại

---

### Bước 2 — Tạo màu sắc (`POST /api/colors`)

```json
{
  "name": "Đen",
  "hexCode": "#000000",
  "slug": "den"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Tao mau sac thanh cong.",
  "data": {
    "id": 1,
    "name": "Đen",
    "hexCode": "#000000",
    "slug": "den"
  }
}
```

**Validation:**
- `slug` phải duy nhất
- `hexCode` phải đúng format `#RRGGBB`

---

### Bước 3 — Tạo size (`POST /api/sizes`)

```json
{
  "label": "M",
  "type": "clothing",
  "sortOrder": 2
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 3,
    "label": "M",
    "type": "clothing",
    "sortOrder": 2
  }
}
```

**Validation:**
- Cặp `(label, type)` phải duy nhất
- `type` nên là: `clothing`, `numeric`, hoặc `shoes`

---

### Bước 4 — Tạo sản phẩm (`POST /api/products`)

```json
{
  "name": "Áo Thun Trơn Cotton Basic",
  "slug": "ao-thun-tron-cotton-basic",
  "description": "Áo thun cổ tròn cotton 100%, mềm mại thoáng mát",
  "categoryId": 6,
  "basePrice": 149000,
  "brand": "YODY",
  "material": "Cotton 100%",
  "isActive": true
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Tao san pham thanh cong.",
  "data": {
    "id": 1,
    "name": "Áo Thun Trơn Cotton Basic",
    "slug": "ao-thun-tron-cotton-basic",
    "description": "Áo thun cổ tròn cotton 100%...",
    "categoryId": 6,
    "basePrice": 149000,
    "brand": "YODY",
    "material": "Cotton 100%",
    "isActive": true,
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
}
```

**Validation:**
- `categoryId` phải tồn tại trong bảng `categories`
- `slug` phải duy nhất toàn bảng `products`

---

### Bước 5 — Tạo biến thể theo màu (`POST /api/product-variants`)

> Mỗi sản phẩm có nhiều biến thể màu. Mỗi cặp `(productId, colorId)` là duy nhất.

```json
{
  "productId": 1,
  "colorId": 1,
  "colorImageUrl": "https://images.unsplash.com/photo-xxx?w=600",
  "images": "[\"https://img1.jpg\", \"https://img2.jpg\"]",
  "isDefault": true
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "colorId": 1,
    "colorImageUrl": "https://...",
    "images": "[\"https://img1.jpg\", \"https://img2.jpg\"]",
    "isDefault": true
  }
}
```

**Validation:**
- `productId` phải tồn tại
- `colorId` phải tồn tại
- Cặp `(productId, colorId)` phải duy nhất (không trùng màu cho cùng SP)

---

### Bước 6 — Tạo tồn kho theo size (`POST /api/variant-stocks`)

> Mỗi biến thể có nhiều size. Mỗi cặp `(variantId, sizeId)` là duy nhất = 1 SKU.

```json
{
  "variantId": 1,
  "sizeId": 3,
  "stockQty": 50,
  "priceOverride": null,
  "sku": "ATB-DEN-M"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "variantId": 1,
    "sizeId": 3,
    "stockQty": 50,
    "priceOverride": null,
    "sku": "ATB-DEN-M"
  }
}
```

**Validation:**
- `variantId` phải tồn tại
- `sizeId` phải tồn tại
- Cặp `(variantId, sizeId)` phải duy nhất
- `sku` phải duy nhất toàn bảng
- `priceOverride = null` → dùng `base_price` của product

---

### Tạo nhanh 50 sản phẩm mẫu

```
POST /api/sample-data/generate
```

> ⚠️ Xóa sạch dữ liệu product cũ rồi tạo mới 50 sản phẩm đầy đủ.  
> Không cần authentication.

---

## 2️⃣ LUỒNG XEM SẢN PHẨM (Public — không cần token)

### 2.1 Xem tất cả sản phẩm

```
GET /api/products
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Áo Thun Trơn Cotton Basic",
      "slug": "ao-thun-tron-cotton-basic",
      "description": "...",
      "categoryId": 6,
      "basePrice": 149000,
      "brand": "YODY",
      "material": "Cotton 100%",
      "isActive": true,
      "createdAt": "...",
      "updatedAt": "..."
    }
  ]
}
```

### 2.2 Xem sản phẩm theo ID

```
GET /api/products/{id}
```

### 2.3 Xem sản phẩm theo slug (SEO-friendly)

```
GET /api/products/slug/{slug}
```

**Ví dụ:** `GET /api/products/slug/ao-thun-tron-cotton-basic`

### 2.4 Xem sản phẩm theo danh mục

```
GET /api/products/category/{categoryId}
```

**Ví dụ:** `GET /api/products/category/6` → Lấy tất cả áo thun

### 2.5 Xem chi tiết sản phẩm (biến thể + tồn kho)

FE cần gọi 3 API liên tiếp để lấy đầy đủ thông tin 1 sản phẩm:

```
Bước 1:  GET /api/products/{id}                      → Thông tin sản phẩm
Bước 2:  GET /api/product-variants/product/{id}       → Danh sách màu sắc
Bước 3:  GET /api/variant-stocks/variant/{variantId}  → Tồn kho theo size của mỗi màu
```

**Ví dụ luồng FE:**

```
1. GET /api/products/1
   → { name: "Áo Thun Basic", basePrice: 149000, brand: "YODY", ... }

2. GET /api/product-variants/product/1
   → [
       { id: 1, colorId: 1, isDefault: true,  images: [...] },   // Đen
       { id: 2, colorId: 2, isDefault: false, images: [...] },   // Trắng
       { id: 3, colorId: 3, isDefault: false, images: [...] }    // Xám
     ]

3. User chọn màu Đen (variantId = 1):
   GET /api/variant-stocks/variant/1
   → [
       { sizeId: 2, sku: "P1-V1-S",  stockQty: 45, priceOverride: null    },
       { sizeId: 3, sku: "P1-V1-M",  stockQty: 60, priceOverride: null    },
       { sizeId: 4, sku: "P1-V1-L",  stockQty: 30, priceOverride: 159000  },
       { sizeId: 5, sku: "P1-V1-XL", stockQty: 20, priceOverride: null    }
     ]
```

> **Logic giá hiển thị:**  
> `giá = priceOverride != null ? priceOverride : product.basePrice`

---

## 3️⃣ LUỒNG LỌC & TÌM KIẾM SẢN PHẨM

### 3.1 Tìm kiếm nâng cao (có phân trang)

```
GET /api/products/search?keyword=...&categoryId=...&isActive=...&page=...&size=...&sortBy=...&direction=...
```

| Param | Type | Bắt buộc | Mặc định | Mô tả |
|-------|------|----------|----------|-------|
| `keyword` | String | ❌ | - | Tìm trong `name`, `description`, `brand` |
| `categoryId` | Integer | ❌ | - | Lọc theo danh mục |
| `isActive` | Boolean | ❌ | - | `true` = đang bán, `false` = ngừng bán |
| `page` | int | ❌ | `0` | Số trang (bắt đầu từ 0) |
| `size` | int | ❌ | `10` | Số sản phẩm mỗi trang |
| `sortBy` | String | ❌ | `id` | Trường sắp xếp: `id`, `name`, `basePrice`, `createdAt` |
| `direction` | String | ❌ | `ASC` | `ASC` hoặc `DESC` |

**Ví dụ các trường hợp:**

```
# Tìm áo thun
GET /api/products/search?keyword=áo thun

# Lọc theo danh mục "Quần Jean" (categoryId=12)
GET /api/products/search?categoryId=12

# Sản phẩm đang bán, sắp xếp theo giá giảm dần
GET /api/products/search?isActive=true&sortBy=basePrice&direction=DESC

# Tìm brand "YODY" trong danh mục áo thun, trang 2
GET /api/products/search?keyword=YODY&categoryId=6&page=1&size=10

# Kết hợp tất cả bộ lọc
GET /api/products/search?keyword=cotton&categoryId=6&isActive=true&page=0&size=20&sortBy=basePrice&direction=ASC
```

**Response (phân trang):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Áo Thun Trơn Cotton Basic",
        "slug": "ao-thun-tron-cotton-basic",
        "categoryId": 6,
        "basePrice": 149000,
        "brand": "YODY",
        "material": "Cotton 100%",
        "isActive": true,
        "createdAt": "...",
        "updatedAt": "..."
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

### 3.2 Phân trang đơn giản (không lọc)

```
GET /api/products/paged?page=0&size=10&sortBy=id&direction=ASC
```

### 3.3 Lọc danh mục

```
# Lấy tất cả danh mục
GET /api/categories

# Lấy danh mục gốc (Áo, Quần, Váy & Đầm, Đồ Lót, Phụ Kiện)
GET /api/categories/roots

# Lấy danh mục con của "Áo" (id=1)
GET /api/categories/1/children
→ [Áo Thun, Áo Sơ Mi, Áo Khoác, Áo Polo, Hoodie & Sweater, Áo Len]

# Lấy danh mục theo slug
GET /api/categories/slug/ao-thun
```

### 3.4 Lấy danh sách màu sắc & size (cho bộ lọc FE)

```
# Tất cả màu sắc
GET /api/colors

# Tất cả sizes
GET /api/sizes

# Sizes theo loại
GET /api/sizes/type/clothing    → [XS, S, M, L, XL, XXL, 3XL]
GET /api/sizes/type/numeric     → [26, 27, 28, 29, 30, 31, 32, 33, 34, 36]
GET /api/sizes/type/shoes       → [36, 37, 38, ...]
```

### 3.5 Tìm kiếm tồn kho theo SKU

```
GET /api/variant-stocks/search?keyword=P1-V&page=0&size=20
```

---

## 4️⃣ LUỒNG FE TỔNG HỢP — TRANG DANH SÁCH SẢN PHẨM

```
┌─────────────────────────────────────────────────────────────────┐
│                    TRANG DANH SÁCH SẢN PHẨM                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─── Sidebar ───┐   ┌─── Content ────────────────────────┐    │
│  │               │   │                                     │    │
│  │ [1] Load cate │   │  [3] Hiển thị sản phẩm              │    │
│  │ GET /api/     │   │      từ kết quả search               │    │
│  │ categories/   │   │                                     │    │
│  │ roots         │   │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │    │
│  │               │   │  │ SP1 │ │ SP2 │ │ SP3 │ │ SP4 │  │    │
│  │ Click cha     │   │  │     │ │     │ │     │ │     │  │    │
│  │ → GET /api/   │   │  │149K │ │199K │ │129K │ │179K │  │    │
│  │ categories/   │   │  └─────┘ └─────┘ └─────┘ └─────┘  │    │
│  │ {id}/children │   │                                     │    │
│  │               │   │  ┌─── Pagination ──────────────┐    │    │
│  │ [2] Load màu  │   │  │  ◄ 1  2  3  4  5 ►         │    │    │
│  │ GET /api/     │   │  └─────────────────────────────┘    │    │
│  │ colors        │   │                                     │    │
│  │               │   └─────────────────────────────────────┘    │
│  │ [2] Load size │                                              │
│  │ GET /api/     │   ┌─── Search & Sort ──────────────────┐    │
│  │ sizes         │   │ 🔍 [keyword____]  Sắp xếp: [Giá ▼] │    │
│  └───────────────┘   └─────────────────────────────────────┘    │
│                                                                 │
│  [3] Khi user thay đổi bộ lọc/search/sort/page:                │
│      GET /api/products/search                                   │
│         ?keyword={input}                                        │
│         &categoryId={selected}                                  │
│         &isActive=true                                          │
│         &page={currentPage}                                     │
│         &size=20                                                │
│         &sortBy=basePrice                                       │
│         &direction=ASC                                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5️⃣ LUỒNG FE TỔNG HỢP — TRANG CHI TIẾT SẢN PHẨM

```
┌─────────────────────────────────────────────────────────────────┐
│              TRANG CHI TIẾT SẢN PHẨM                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [1] Load sản phẩm                                              │
│      GET /api/products/{id}                                     │
│      hoặc GET /api/products/slug/{slug}                         │
│      → name, basePrice, brand, material, description            │
│                                                                 │
│  [2] Load biến thể màu                                          │
│      GET /api/product-variants/product/{productId}              │
│      → danh sách màu, ảnh mỗi màu, isDefault                   │
│                                                                 │
│  [3] Hiển thị ảnh của màu mặc định (isDefault=true)             │
│      Hiển thị các nút chọn màu: ● ● ● ●                        │
│                                                                 │
│  [4] Khi user chọn 1 màu → Load tồn kho                        │
│      GET /api/variant-stocks/variant/{variantId}                │
│      → danh sách size + stockQty + priceOverride                │
│                                                                 │
│  [5] Hiển thị:                                                  │
│      ┌─────────────────────────────────────────┐                │
│      │  🖼️ Ảnh sản phẩm (từ variant.images)   │                │
│      │                                         │                │
│      │  Áo Thun Trơn Cotton Basic              │                │
│      │  Brand: YODY | Chất liệu: Cotton 100%  │                │
│      │                                         │                │
│      │  💰 149.000₫                            │                │
│      │  (hoặc priceOverride nếu có)            │                │
│      │                                         │                │
│      │  Màu: [⚫] [⚪] [🔵] [🔴]              │                │
│      │                                         │                │
│      │  Size: [S] [M✓] [L] [XL] [XXL-hết]     │                │
│      │  (disable nếu stockQty = 0)             │                │
│      │                                         │                │
│      │  [🛒 Thêm vào giỏ hàng]                │                │
│      └─────────────────────────────────────────┘                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6️⃣ BẢNG TỔNG HỢP TẤT CẢ API ENDPOINTS

### Category (`/api/categories`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/api/categories` | ❌ | Lấy tất cả |
| `GET` | `/api/categories/{id}` | ❌ | Lấy theo ID |
| `GET` | `/api/categories/slug/{slug}` | ❌ | Lấy theo slug |
| `GET` | `/api/categories/roots` | ❌ | Lấy danh mục gốc |
| `GET` | `/api/categories/{id}/children` | ❌ | Lấy danh mục con |
| `POST` | `/api/categories` | ✅ | Tạo mới |
| `PUT` | `/api/categories/{id}` | ✅ | Cập nhật |
| `DELETE` | `/api/categories/{id}` | ✅ | Xóa |

### Color (`/api/colors`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/api/colors` | ❌ | Lấy tất cả |
| `GET` | `/api/colors/{id}` | ❌ | Lấy theo ID |
| `POST` | `/api/colors` | ✅ | Tạo mới |
| `PUT` | `/api/colors/{id}` | ✅ | Cập nhật |
| `DELETE` | `/api/colors/{id}` | ✅ | Xóa |

### Size (`/api/sizes`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/api/sizes` | ❌ | Lấy tất cả |
| `GET` | `/api/sizes/{id}` | ❌ | Lấy theo ID |
| `GET` | `/api/sizes/type/{type}` | ❌ | Lấy theo loại (clothing/numeric/shoes) |
| `POST` | `/api/sizes` | ✅ | Tạo mới |
| `PUT` | `/api/sizes/{id}` | ✅ | Cập nhật |
| `DELETE` | `/api/sizes/{id}` | ✅ | Xóa |

### Product (`/api/products`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/api/products` | ❌ | Lấy tất cả |
| `GET` | `/api/products/{id}` | ❌ | Lấy theo ID |
| `GET` | `/api/products/slug/{slug}` | ❌ | Lấy theo slug |
| `GET` | `/api/products/category/{categoryId}` | ❌ | Lấy theo danh mục |
| `GET` | `/api/products/paged?...` | ❌ | Phân trang |
| `GET` | `/api/products/search?...` | ❌ | Tìm kiếm + lọc + phân trang |
| `POST` | `/api/products` | ✅ | Tạo mới |
| `PUT` | `/api/products/{id}` | ✅ | Cập nhật |
| `DELETE` | `/api/products/{id}` | ✅ | Xóa |

### Product Variant (`/api/product-variants`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/api/product-variants` | ❌ | Lấy tất cả |
| `GET` | `/api/product-variants/{id}` | ❌ | Lấy theo ID |
| `GET` | `/api/product-variants/product/{productId}` | ❌ | Lấy theo sản phẩm |
| `POST` | `/api/product-variants` | ✅ | Tạo mới |
| `PUT` | `/api/product-variants/{id}` | ✅ | Cập nhật |
| `DELETE` | `/api/product-variants/{id}` | ✅ | Xóa |

### Variant Stock (`/api/variant-stocks`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `GET` | `/api/variant-stocks` | ❌ | Lấy tất cả |
| `GET` | `/api/variant-stocks/{id}` | ❌ | Lấy theo ID |
| `GET` | `/api/variant-stocks/variant/{variantId}` | ❌ | Lấy theo biến thể |
| `GET` | `/api/variant-stocks/search?...` | ❌ | Tìm kiếm SKU + phân trang |
| `POST` | `/api/variant-stocks` | ✅ | Tạo mới |
| `PUT` | `/api/variant-stocks/{id}` | ✅ | Cập nhật |
| `DELETE` | `/api/variant-stocks/{id}` | ✅ | Xóa |

### Sample Data (`/api/sample-data`)

| Method | URL | Auth | Mô tả |
|--------|-----|------|-------|
| `POST` | `/api/sample-data/generate` | ❌ | Tạo 50 sản phẩm mẫu |

---

## 7️⃣ QUY TẮC XÓA (CASCADE & RESTRICT)

```
Xóa Category:
  ├── ❌ Bị chặn nếu có danh mục con (hasChildren)
  └── ❌ Bị chặn nếu có sản phẩm (hasProducts)

Xóa Color:
  └── ❌ Bị chặn nếu có product_variant sử dụng (hasVariants)

Xóa Size:
  └── ❌ Bị chặn nếu có variant_stock sử dụng (hasStocks)

Xóa Product:
  └── ❌ Bị chặn nếu có product_variant (hasVariants)
      (Phải xóa tất cả variant trước)

Xóa Product Variant:
  └── ❌ Bị chặn nếu có variant_stock (hasStocks)
      (Phải xóa tất cả stock trước)
      💡 Trong DB: ON DELETE CASCADE, nhưng Service layer chặn trước

Xóa Variant Stock:
  └── ✅ Xóa trực tiếp được
```

---

## 8️⃣ SWAGGER UI

Truy cập: **http://localhost:8080/swagger-ui.html**

Tất cả API đều có Swagger documentation đầy đủ với:
- Mô tả từng endpoint
- Request/Response schema
- Ví dụ dữ liệu
- Nút "Try it out" để test trực tiếp

