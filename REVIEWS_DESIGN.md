# 🌟 Module Reviews — Clothing Store API

> Cập nhật: 2026-03-25  
> Trạng thái: **✅ Đã implement hoàn chỉnh** — 5 endpoints, chống fake review qua FK tới `order_line`

---

## 1. Tổng Quan

### Mục tiêu
Cho phép user đánh giá sản phẩm **đã mua thật** (chống fake review thông qua FK tới `order_line`).

### Thiết kế chống fake review

```
user_review.ordered_product_id  ──FK──>  order_line.id
```

User **CHỈ** được review sản phẩm có trong `order_line` của đơn hàng **DELIVERED** của chính mình.

### Business Rules (5 validation)
1. ✅ `order_line` phải tồn tại
2. ✅ `order_line` phải thuộc về user đang login (join qua `shop_order.user_id`)
3. ✅ Đơn hàng phải ở trạng thái **DELIVERED**
4. ✅ User chưa review `order_line` này (1 order_line = 1 review)
5. ✅ `ratingValue` phải từ 1–5 (Jakarta Validation)

---

## 2. API Endpoints (5 endpoints)

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `GET` | `/api/reviews/product/{productId}` | 🌐 Public | Xem reviews theo sản phẩm |
| `GET` | `/api/reviews/product/{productId}/summary` | 🌐 Public | Điểm TB + tổng số reviews |
| `POST` | `/api/reviews` | 🔒 JWT | Tạo review (validate DELIVERED + chưa review + đơn của mình) |
| `GET` | `/api/reviews/my` | 🔒 JWT | Reviews của tôi |
| `DELETE` | `/api/reviews/{id}` | 🔒 JWT | Xóa review của mình |

---

## 3. Chi Tiết API

### 3.1. `POST /api/reviews` — Tạo đánh giá

**Auth:** 🔒 Cần JWT Bearer token

**Request Body:**
```json
{
  "orderedProductId": 10,
  "ratingValue": 5,
  "comment": "Sản phẩm rất đẹp, chất lượng tốt!"
}
```

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
    "id": 1,
    "userId": 10,
    "username": "ngoc123",
    "orderedProductId": 10,
    "ratingValue": 5,
    "comment": "Sản phẩm rất đẹp, chất lượng tốt!",
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
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Lỗi có thể gặp:**

| HTTP Status | Message Key | Mô tả |
|-------------|-------------|-------|
| 404 | `review.orderLineNotFound` | order_line không tồn tại |
| 400 | `review.notYourOrder` | Đơn hàng không thuộc về bạn |
| 400 | `review.orderNotDelivered` | Đơn hàng chưa giao xong |
| 400 | `review.alreadyReviewed` | Đã đánh giá order_line này rồi |
| 400 | Validation error | ratingValue ngoài 1–5, comment > 2000 ký tự |

---

### 3.2. `GET /api/reviews/product/{productId}` — Xem reviews theo sản phẩm

**Auth:** 🌐 Public (không cần token)

**Path Variable:** `productId` — ID sản phẩm

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
      "comment": "Áo đẹp lắm!",
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
      "comment": "Chất lượng OK, giao hàng nhanh",
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

> **Sắp xếp:** Mới nhất trước (ORDER BY `createdAt` DESC)  
> **Enriched fields:** Mỗi review chứa đầy đủ thông tin sản phẩm (name, color, size, SKU) — join qua 6 bảng  
> **Batch load:** Tránh N+1 bằng cách batch load tất cả IDs trước khi build DTOs

**Lỗi có thể gặp:**

| HTTP Status | Message Key | Mô tả |
|-------------|-------------|-------|
| 404 | `product.notFound` | Sản phẩm không tồn tại |

---

### 3.3. `GET /api/reviews/product/{productId}/summary` — Thống kê đánh giá

**Auth:** 🌐 Public (không cần token)

**Path Variable:** `productId` — ID sản phẩm

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

> **avgRating:** Làm tròn 1 chữ số thập phân (ví dụ: 4.5, 3.8)  
> **Nếu chưa có review:** `avgRating = 0.0`, `totalReviews = 0`

**Lỗi có thể gặp:**

| HTTP Status | Message Key | Mô tả |
|-------------|-------------|-------|
| 404 | `product.notFound` | Sản phẩm không tồn tại |

---

### 3.4. `GET /api/reviews/my` — Reviews của tôi

**Auth:** 🔒 Cần JWT Bearer token

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
      "comment": "Áo đẹp lắm!",
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
    }
  ],
  "timestamp": "2026-03-25T10:30:00Z"
}
```

> Trả về tất cả review của user hiện tại, sắp xếp mới nhất trước.

---

### 3.5. `DELETE /api/reviews/{reviewId}` — Xóa review của mình

**Auth:** 🔒 Cần JWT Bearer token

**Path Variable:** `reviewId` — ID đánh giá cần xóa

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xoa danh gia thanh cong.",
  "data": null,
  "timestamp": "2026-03-25T10:30:00Z"
}
```

**Lỗi có thể gặp:**

| HTTP Status | Message Key | Mô tả |
|-------------|-------------|-------|
| 404 | `review.notFound` | Đánh giá không tồn tại |
| 400 | `review.notYourReview` | Đánh giá không thuộc về bạn |

---

## 4. Database Schema

```sql
CREATE TABLE user_review (
    id                 INT AUTO_INCREMENT,
    user_id            INT,               -- FK → site_user(id)   — ai đánh giá
    ordered_product_id INT,               -- FK → order_line(id)  — dòng sản phẩm đã mua
    rating_value       INT,               -- điểm 1–5 sao
    comment            VARCHAR(2000),     -- nội dung đánh giá
    created_at         DATETIME,          -- thời gian tạo (auto-set qua @PrePersist)
    CONSTRAINT pk_review         PRIMARY KEY (id),
    CONSTRAINT fk_review_user    FOREIGN KEY (user_id)            REFERENCES site_user (id),
    CONSTRAINT fk_review_product FOREIGN KEY (ordered_product_id) REFERENCES order_line (id)
);
```

### Quan hệ

```
site_user ──< user_review >── order_line ──> variant_stocks ──> product_variants ──> products
                                   │
                                   └──> shop_order
```

---

## 5. Chuỗi Join (6 bảng)

### Từ `user_review` → tìm thông tin sản phẩm

```
user_review
    │ ordered_product_id
    ▼
order_line
    │ variant_stock_id
    ▼
variant_stocks
    │ variant_id        │ size_id
    ▼                   ▼
product_variants      sizes (label, type)
    │ product_id   │ color_id
    ▼              ▼
products         colors (name, hex)
```

### Query SQL mẫu

```sql
-- Lấy tất cả review của 1 sản phẩm
SELECT
    ur.id, ur.rating_value, ur.comment, ur.created_at,
    su.username,
    c.name AS color_name, c.hex_code AS color_hex,
    s.label AS size_label, s.type AS size_type,
    p.name AS product_name, p.slug AS product_slug,
    vs.sku, pv.color_image_url
FROM user_review ur
    JOIN site_user su        ON ur.user_id             = su.id
    JOIN order_line ol       ON ur.ordered_product_id   = ol.id
    JOIN variant_stocks vs   ON ol.variant_stock_id     = vs.id
    JOIN product_variants pv ON vs.variant_id           = pv.id
    JOIN products p          ON pv.product_id           = p.id
    JOIN colors c            ON pv.color_id             = c.id
    JOIN sizes s             ON vs.size_id              = s.id
WHERE pv.product_id = :productId
ORDER BY ur.created_at DESC;
```

```sql
-- Tính điểm trung bình cho sản phẩm
SELECT
    pv.product_id,
    ROUND(AVG(ur.rating_value), 1) AS avg_rating,
    COUNT(ur.id) AS total_reviews
FROM user_review ur
    JOIN order_line ol       ON ur.ordered_product_id = ol.id
    JOIN variant_stocks vs   ON ol.variant_stock_id   = vs.id
    JOIN product_variants pv ON vs.variant_id         = pv.id
WHERE pv.product_id = :productId
GROUP BY pv.product_id;
```

---

## 6. Luồng Hoạt Động

### 6.1. Tạo Review

```
User đã nhận hàng (DELIVERED)
         │
         ▼
POST /api/reviews
{
  "orderedProductId": 10,      ← order_line.id
  "ratingValue": 5,            ← 1–5 sao
  "comment": "Áo đẹp lắm!"
}
         │
         ▼
┌─────────────────────────────────┐
│  Validation (Service layer)     │
│                                 │
│  1. order_line #10 tồn tại?    │─── Không → 404 "order_line không tồn tại"
│                                 │
│  2. order_line #10 thuộc về     │
│     user đang login?            │─── Không → 400 "không phải đơn của bạn"
│     (join order_line → shop_order│
│      → check shop_order.user_id)│
│                                 │
│  3. Đơn hàng đã DELIVERED?     │─── Chưa  → 400 "đơn chưa giao xong"
│     (shop_order.order_status    │
│      = DELIVERED status id)     │
│                                 │
│  4. User đã review order_line  │─── Rồi   → 400 "đã đánh giá rồi"
│     #10 chưa?                   │
│     (existsByUserIdAndOrderedPro│
│      ductId)                    │
│                                 │
│  5. ratingValue trong 1–5?     │─── Sai   → 400 "rating phải 1-5"
│     (Jakarta Validation)        │
│                                 │
└──────────────┬──────────────────┘
               │ OK
               ▼
┌─────────────────────────────────┐
│  Lưu user_review                │
│  {                              │
│    userId: 10,    ← từ JWT      │
│    orderedProductId: 10,        │
│    ratingValue: 5,              │
│    comment: "Áo đẹp lắm!",     │
│    createdAt: auto              │
│  }                              │
│                                 │
│  → Enrich response với product  │
│    info (name, color, size, SKU)│
└─────────────────────────────────┘
```

### 6.2. Xem Review Theo Sản Phẩm

```
GET /api/reviews/product/{productId}
         │
         ▼
┌─────────────────────────────────────┐
│  1. Validate product tồn tại       │
│  2. Query reviews (JPQL join 4 bảng)│
│  3. Batch load tất cả related IDs  │
│     (tránh N+1 queries)            │
│  4. Build enriched DTOs            │
└──────────────┬──────────────────────┘
               │
               ▼
Response: List<ReviewResponseDTO>
  → Mỗi item có: username, productName, colorName,
    sizeLabel, sku, colorImageUrl, createdAt
```

---

## 7. Ví Dụ Cụ Thể

```
User "Ngọc" đặt đơn DH20260325001 (đã DELIVERED):
  ├─ order_line #10: Áo Nike Đỏ Size M   (variant_stock_id = 5)
  ├─ order_line #11: Quần Adidas Size L   (variant_stock_id = 12)
  └─ order_line #12: Áo Nike Đỏ Size M   (variant_stock_id = 5)  ← mua 2 cái cùng loại

Ngọc CÓ THỂ:
  ✅ Review order_line #10 → 5 sao "Áo đẹp"
  ✅ Review order_line #11 → 4 sao "Quần OK"
  ✅ Review order_line #12 → 3 sao "Cái thứ 2 hơi lỗi"   ← mua 2 lần = review 2 lần

Ngọc KHÔNG THỂ:
  ❌ Review order_line #10 lần 2   → 400 "Đã đánh giá rồi"
  ❌ Review sản phẩm "Giày Puma"   → Không có order_line → 404
  ❌ Review order_line #20 (của user khác) → 400 "Không phải đơn của bạn"
```

---

## 8. Files Đã Implement

| Layer | File | Mô tả |
|-------|------|-------|
| **Entity** | `entity/UserReview.java` | 6 fields: id, userId, orderedProductId, ratingValue, comment, createdAt |
| **Repository** | `repository/UserReviewRepository.java` | 5 custom queries (JPQL join 4 bảng) + 1 existsBy |
| **DTO Request** | `dto/CreateReviewRequest.java` | orderedProductId, ratingValue (1–5), comment — Jakarta Validation |
| **DTO Response** | `dto/ReviewResponseDTO.java` | Enriched: username, productName, colorName, sizeLabel, sku, colorImageUrl... |
| **DTO Summary** | `dto/ReviewSummaryDTO.java` | productId, avgRating, totalReviews |
| **Service** | `service/ReviewService.java` | Interface: 5 methods |
| **Service Impl** | `service/impl/ReviewServiceImpl.java` | Implementation: validation chống fake + batch load tránh N+1 |
| **Controller** | `controller/ReviewController.java` | 5 endpoints (2 public + 3 authenticated) |
| **Messages** | `messages.properties` | 8 review message keys |
| **Security** | `config/security/SecurityConfig.java` | GET `/api/reviews/product/**` = public |
| **Database** | `init_sql/init.sql` | Cột `created_at DATETIME` trong bảng `user_review` |

---

## 9. Message Keys

```properties
review.create.success=Tao danh gia thanh cong.
review.delete.success=Xoa danh gia thanh cong.
review.notFound=Khong tim thay danh gia voi ID: {0}
review.orderLineNotFound=Khong tim thay dong san pham da mua voi ID: {0}
review.notYourOrder=Don hang nay khong thuoc ve ban.
review.orderNotDelivered=Don hang chua giao xong, khong the danh gia.
review.alreadyReviewed=Ban da danh gia san pham nay roi.
review.notYourReview=Danh gia nay khong thuoc ve ban.
```

---

## 10. Performance: Batch Load (Tránh N+1)

Khi load danh sách reviews, **ReviewServiceImpl** sử dụng batch load strategy:

```
1. Query reviews                     → List<UserReview>
2. Batch load userIds               → Map<userId, username>
3. Batch load orderLineIds          → Map<orderLineId, OrderLine>
4. Batch load variantStockIds       → Map<stockId, VariantStock>
5. Batch load variantIds            → Map<variantId, ProductVariant>
6. Batch load productIds            → Map<productId, Product>
7. Batch load colorIds              → Map<colorId, Color>
8. Batch load sizeIds               → Map<sizeId, Size>
9. Build DTOs từ các pre-loaded maps
```

> Thay vì N×7 queries (mỗi review query 7 bảng), chỉ cần **8 queries** cho bất kỳ số lượng reviews nào.

---

## 11. Sơ Đồ Tóm Tắt

```
┌──────────┐    mua hàng    ┌──────────┐   giao hàng   ┌───────────┐
│   User   │───────────────→│  Order   │──────────────→│ DELIVERED │
└──────────┘                └──────────┘               └─────┬─────┘
                                                             │
                                                      viết review
                                                             │
                                                             ▼
                                                     ┌──────────────┐
                                                     │ user_review   │
                                                     │               │
                                                     │ • rating 1–5  │
                                                     │ • comment     │
                                                     │ • → order_line│ ← chống fake
                                                     │ • → user      │
                                                     │ • created_at  │
                                                     └──────┬───────┘
                                                            │
                                                      join ngược
                                                      (6 bảng)
                                                            │
                                                            ▼
                                                     ┌──────────────┐
                                                     │  Hiển thị    │
                                                     │  trên trang  │
                                                     │  sản phẩm    │
                                                     │              │
                                                     │ ★★★★★ 4.5/5 │
                                                     │ 12 đánh giá  │
                                                     └──────────────┘
```
