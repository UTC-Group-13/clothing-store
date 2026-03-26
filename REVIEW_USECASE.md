# ⭐ Use Case: Đánh Giá Sản Phẩm (Product Review)

> Xem file `REVIEW_USECASE.drawio` để import vào [draw.io](https://draw.io)

---

## 📌 Actors & Điều Kiện Quan Trọng

| Actor | Mô tả |
|-------|--------|
| **Guest** | Xem đánh giá, xem thống kê — không cần đăng nhập |
| **User** | Tạo / xem / xóa đánh giá — cần JWT token |
| **System** | Spring Boot Backend |

> ⚠️ **Ràng buộc nghiệp vụ khi tạo đánh giá:**
> 1. `orderedProductId` phải là ID của `order_line` hợp lệ
> 2. `order_line` đó phải thuộc đơn hàng của **chính user đó**
> 3. Đơn hàng phải ở trạng thái **DELIVERED**
> 4. Mỗi `order_line` chỉ được đánh giá **1 lần duy nhất**
> 5. `ratingValue` từ **1 đến 5** sao

---

## 📋 Use Cases Tổng Quan

```
┌───────────────────────────────────────────────────────────────────────┐
│                   <<System>> Đánh Giá Sản Phẩm                        │
│                                                                       │
│  Guest ──────► UC1: Xem Đánh Giá Theo Sản Phẩm                       │
│                      GET /api/reviews/product/{productId}             │
│                                                                       │
│  Guest ──────► UC2: Xem Thống Kê Đánh Giá                           │
│                      GET /api/reviews/product/{productId}/summary     │
│                                                                       │
│  User  ──────► UC3: Tạo Đánh Giá                                     │
│                      POST /api/reviews                                │
│                      ↳ <<include>> Kiểm tra OrderLine hợp lệ         │
│                      ↳ <<include>> Kiểm tra đơn hàng là DELIVERED    │
│                      ↳ <<include>> Kiểm tra chưa đánh giá lần nào   │
│                                                                       │
│  User  ──────► UC4: Xem Đánh Giá Của Tôi                            │
│                      GET /api/reviews/my                              │
│                                                                       │
│  User  ──────► UC5: Xóa Đánh Giá                                     │
│                      DELETE /api/reviews/{id}                         │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 UC3 — Tạo Đánh Giá (Sequence Chi Tiết)

```mermaid
sequenceDiagram
    actor User as 👤 User (đã mua hàng, đã nhận hàng)
    participant FLT  as 🔒 JwtFilter
    participant CTL  as 🌐 ReviewController<br/>POST /api/reviews
    participant SVC  as ⚙️ ReviewServiceImpl
    participant UREPO as 🗄️ UserRepository
    participant LREPO as 🗄️ OrderLineRepository
    participant OREPO as 🗄️ OrderRepository
    participant SREPO as 🗄️ OrderStatusRepository
    participant RREPO as 🗄️ UserReviewRepository

    User->>FLT: POST /api/reviews<br/>Authorization: Bearer token<br/>Body: { orderedProductId, ratingValue(1-5), comment }

    FLT->>FLT: Xác thực JWT → lấy username
    alt Token không hợp lệ
        FLT-->>User: 401 Unauthorized
    end

    FLT->>CTL: Request đã xác thực

    CTL->>CTL: @Valid — ratingValue 1-5, orderedProductId NotNull
    alt Validation thất bại
        CTL-->>User: 400 Bad Request { errors }
    end

    CTL->>SVC: createReview(username, CreateReviewRequest)

    Note over SVC: ① Lấy thông tin user
    SVC->>UREPO: findByUsername(username)
    UREPO-->>SVC: SiteUser { id }

    Note over SVC: ② Kiểm tra OrderLine tồn tại
    SVC->>LREPO: findById(orderedProductId)
    alt Không tìm thấy
        SVC-->>User: 404 review.orderLineNotFound
    end
    LREPO-->>SVC: OrderLine { orderId, variantStockId }

    Note over SVC: ③ Kiểm tra Order thuộc về user
    SVC->>OREPO: findById(orderLine.orderId)
    OREPO-->>SVC: ShopOrder { userId, orderStatus }
    SVC->>SVC: order.userId == user.id?
    alt Đơn không thuộc user
        SVC-->>User: 400 review.notYourOrder
    end

    Note over SVC: ④ Kiểm tra trạng thái DELIVERED
    SVC->>SREPO: findById(order.orderStatus)
    SREPO-->>SVC: OrderStatus { status }
    SVC->>SVC: status == "DELIVERED"?
    alt Chưa nhận hàng
        SVC-->>User: 400 review.orderNotDelivered<br/>("Chỉ đánh giá được đơn hàng đã giao")
    end

    Note over SVC: ⑤ Kiểm tra chưa đánh giá
    SVC->>RREPO: existsByUserIdAndOrderedProductId(userId, orderedProductId)
    alt Đã đánh giá rồi
        SVC-->>User: 400 review.alreadyReviewed<br/>("Bạn đã đánh giá sản phẩm này rồi")
    end
    RREPO-->>SVC: false (chưa có)

    Note over SVC: ⑥ Lưu đánh giá
    SVC->>RREPO: save(UserReview { userId, orderedProductId, ratingValue, comment })
    RREPO-->>SVC: UserReview { id, createdAt }

    Note over SVC: ⑦ Build response + enrich thông tin sản phẩm
    SVC->>SVC: enrichFromOrderLine(orderLine)<br/>→ load VariantStock → ProductVariant → Product/Color/Size

    SVC-->>CTL: ReviewResponseDTO { id, username, productName, colorName, sizeLabel, ratingValue, comment }
    CTL-->>User: 201 Created<br/>{ success: true, message: "Tạo đánh giá thành công",<br/>data: ReviewResponseDTO }
```

---

## 🔄 Flowchart `createReview()`

```mermaid
flowchart TD
    A([▶ POST /api/reviews]) --> B{JWT hợp lệ?}
    B -- Không --> C[❌ 401 Unauthorized]
    B -- Có --> D[@Valid: orderedProductId, ratingValue 1-5]
    D --> E{Validation OK?}
    E -- Không --> F[❌ 400 Bad Request]
    E -- Có --> G[findById orderedProductId\northerwise order_line]
    G --> H{OrderLine\ntồn tại?}
    H -- Không --> I[❌ 404 review.orderLineNotFound]
    H -- Có --> J[findById order_line.orderId]
    J --> K{Order thuộc\nuser này?}
    K -- Không --> L[❌ 400 review.notYourOrder]
    K -- Có --> M[Lấy OrderStatus]
    M --> N{status ==\nDELIVERED?}
    N -- Không --> O[❌ 400 review.orderNotDelivered]
    N -- Có --> P[existsByUserIdAndOrderedProductId]
    P --> Q{Đã đánh giá\nrồi?}
    Q -- Có --> R[❌ 400 review.alreadyReviewed]
    Q -- Không --> S[save UserReview]
    S --> T[enrichFromOrderLine\n→ load Product/Color/Size]
    T --> U([✅ 201 Created: ReviewResponseDTO])

    style A fill:#d5e8d4,stroke:#82b366
    style U fill:#d5e8d4,stroke:#82b366
    style C fill:#f8cecc,stroke:#b85450
    style F fill:#f8cecc,stroke:#b85450
    style I fill:#f8cecc,stroke:#b85450
    style L fill:#f8cecc,stroke:#b85450
    style O fill:#f8cecc,stroke:#b85450
    style R fill:#f8cecc,stroke:#b85450
```

---

## 📊 Request / Response

### UC3 — Tạo đánh giá
```
POST /api/reviews
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "orderedProductId": 12,
  "ratingValue": 5,
  "comment": "Sản phẩm rất đẹp, chất lượng tốt!"
}
```

### Response thành công
```json
{
  "success": true,
  "message": "Tạo đánh giá thành công",
  "data": {
    "id": 3,
    "userId": 7,
    "username": "nguyen_loc",
    "orderedProductId": 12,
    "ratingValue": 5,
    "comment": "Sản phẩm rất đẹp, chất lượng tốt!",
    "createdAt": "2026-03-26T14:00:00",
    "productId": 2,
    "productName": "Áo thun Nike",
    "productSlug": "ao-thun-nike",
    "colorName": "Đỏ",
    "colorHex": "#FF0000",
    "sizeLabel": "M",
    "sku": "NIKE-RED-M"
  }
}
```

### UC2 — Thống kê đánh giá
```json
{
  "success": true,
  "data": {
    "productId": 2,
    "avgRating": 4.5,
    "totalReviews": 18
  }
}
```

---

## 🗺️ Quan Hệ Bảng Dữ Liệu

```
user_review
  ├─ userId      → site_users.id
  ├─ orderedProductId → order_line.id
  │       ├─ orderId          → shop_order.id  (kiểm tra DELIVERED + thuộc user)
  │       └─ variantStockId   → variant_stock.id
  │               └─ variantId → product_variant.id
  │                       ├─ productId → products (tên, slug)
  │                       └─ colorId   → colors (tên, hex)
  │               └─ sizeId   → sizes (label, type)
  ├─ ratingValue  (1–5)
  └─ comment      (max 2000 ký tự)
```

---

## 📋 Tất Cả Review Endpoints

| Endpoint | Method | Auth | Mô tả |
|----------|--------|------|-------|
| `/api/reviews/product/{productId}` | GET | ❌ Public | Xem tất cả đánh giá của sản phẩm |
| `/api/reviews/product/{productId}/summary` | GET | ❌ Public | Thống kê: avg, tổng số đánh giá |
| `/api/reviews` | POST | ✅ User | **Tạo đánh giá** (cần DELIVERED) |
| `/api/reviews/my` | GET | ✅ User | Danh sách đánh giá của tôi |
| `/api/reviews/{id}` | DELETE | ✅ User | Xóa đánh giá của mình |

