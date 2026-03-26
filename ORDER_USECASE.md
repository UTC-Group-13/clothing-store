# 🧾 Use Case: Đặt Hàng & Thanh Toán (Order & Payment)

> Xem file `ORDER_USECASE.drawio` để import vào [draw.io](https://draw.io)

---

## 📌 Actors

| Actor | Mô tả |
|-------|--------|
| **User** | Đã đăng nhập, có JWT token, có items trong giỏ |
| **Admin** | Quản trị viên, quản lý và cập nhật trạng thái đơn |
| **System** | Spring Boot Backend |
| **VietQR** | Dịch vụ tạo QR chuyển khoản (`img.vietqr.io`) |
| **Database** | MySQL — 15+ bảng liên quan |

---

## 📋 Use Cases Tổng Quan

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    <<System>> Đặt Hàng & Thanh Toán                      │
│                                                                          │
│  User  ──────► UC1: Đặt Hàng (Place Order)   POST /api/orders           │
│                    ↳ <<include>> Kiểm tra địa chỉ, phương thức          │
│                    ↳ <<include>> Kiểm tra tồn kho toàn bộ cart          │
│                    ↳ <<include>> Tính tổng tiền                          │
│                    ↳ <<include>> Tạo OrderLines + Trừ tồn kho           │
│                    ↳ <<include>> Xóa giỏ hàng                           │
│                    ↳ <<extend>>  Tạo VietQR URL (nếu chuyển khoản)      │
│                                                                          │
│  User  ──────► UC2: Xem Lịch Sử Đơn Hàng     GET  /api/orders          │
│                                                                          │
│  User  ──────► UC3: Xem Chi Tiết Đơn           GET  /api/orders/{id}    │
│                                                                          │
│  User  ──────► UC4: Hủy Đơn Hàng              PATCH /api/orders/{id}/cancel │
│                    ↳ <<include>> Hoàn trả tồn kho                       │
│                    ↳ [chỉ được hủy khi PENDING]                         │
│                                                                          │
│  Admin ──────► UC5: Xem Tất Cả Đơn             GET  /api/orders/admin/all│
│  Admin ──────► UC6: Cập Nhật Trạng Thái        PATCH /api/orders/{id}/status│
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 UC1 — Đặt Hàng (Sequence Chi Tiết)

```mermaid
sequenceDiagram
    actor User as 👤 User (có JWT + giỏ hàng)
    participant FLT  as 🔒 JwtFilter
    participant CTL  as 🌐 OrderController<br/>POST /api/orders
    participant SVC  as ⚙️ OrderServiceImpl
    participant PAYT as 🗄️ PaymentTypeRepo
    participant ADDR as 🗄️ UserAddressRepo
    participant SHIP as 🗄️ ShippingMethodRepo
    participant CART as 🗄️ ShoppingCartRepo
    participant ITEM as 🗄️ CartItemRepo
    participant STCK as 🗄️ VariantStockRepo
    participant STAT as 🗄️ OrderStatusRepo
    participant ORD  as 🗄️ ShopOrderRepo
    participant LINE as 🗄️ OrderLineRepo
    participant QR   as 🌐 VietQR API

    User->>FLT: POST /api/orders<br/>Authorization: Bearer token<br/>Body: { paymentTypeId, shippingAddressId,<br/>shippingMethodId, note }

    FLT->>FLT: Xác thực JWT → lấy username
    alt Token không hợp lệ
        FLT-->>User: 401 Unauthorized
    end

    FLT->>CTL: Request đã xác thực

    CTL->>CTL: @Valid — Kiểm tra required fields
    CTL->>SVC: placeOrder(username, OrderRequest)

    Note over SVC: ① Kiểm tra loại thanh toán
    SVC->>PAYT: findById(paymentTypeId)
    alt Không tìm thấy
        SVC-->>User: 404 payment.typeNotFound
    end

    Note over SVC: ② Kiểm tra địa chỉ thuộc về user
    SVC->>ADDR: existsByUserIdAndAddressId(userId, addressId)
    alt Địa chỉ không thuộc user
        SVC-->>User: 400 order.addressNotBelongToUser
    end

    Note over SVC: ③ Kiểm tra phương thức vận chuyển
    SVC->>SHIP: findById(shippingMethodId)
    alt Không tìm thấy
        SVC-->>User: 404 shipping.notFound
    end

    Note over SVC: ④ Lấy giỏ hàng
    SVC->>CART: findByUserId(userId)
    SVC->>ITEM: findByCartId(cartId)
    alt Giỏ hàng trống
        SVC-->>User: 400 order.cartEmpty
    end

    Note over SVC: ⑤ Validate tồn kho & tính subtotal
    SVC->>STCK: findAllById(stockIds)
    loop Mỗi cart item
        SVC->>SVC: stock.stockQty >= cartItem.qty?
        alt Không đủ hàng
            SVC-->>User: 400 order.insufficientStock
        end
        SVC->>SVC: subtotal += unitPrice × qty
    end

    Note over SVC: ⑥ Lấy trạng thái PENDING
    SVC->>STAT: findByStatus("PENDING")

    Note over SVC: ⑦ Sinh mã đơn hàng
    SVC->>ORD: countByOrderCodeStartingWith("DH" + date)
    SVC->>SVC: orderCode = "DH20260326" + "001"

    Note over SVC: ⑧ Tính orderTotal & tạo ShopOrder
    SVC->>SVC: orderTotal = subtotal + shippingFee
    SVC->>ORD: save(ShopOrder { orderCode, userId, orderTotal, status:PENDING })
    ORD-->>SVC: ShopOrder (id)

    Note over SVC: ⑨ Tạo OrderLines + Trừ tồn kho @Transactional
    loop Mỗi cart item
        SVC->>LINE: save(OrderLine { orderId, variantStockId, qty, price })
        SVC->>STCK: save(stock với stockQty - qty)
    end

    Note over SVC: ⑩ Làm trống giỏ hàng
    SVC->>ITEM: deleteByCartId(cartId)

    Note over SVC: ⑪ Build response + VietQR (nếu chuyển khoản)
    alt Loại thanh toán là "Chuyển khoản"
        SVC->>QR: buildVietQrUrl(bank, amount, orderCode)
        QR-->>SVC: URL QR code image
    end

    SVC-->>CTL: OrderDetailDTO { orderCode, items, total, qrUrl? }
    CTL-->>User: 201 Created<br/>{ success: true, "Đặt hàng thành công",<br/>data: OrderDetailDTO }
```

---

## 🔄 UC4 — Hủy Đơn Hàng (Sequence)

```mermaid
sequenceDiagram
    actor User as 👤 User
    participant CTL  as 🌐 OrderController<br/>PATCH /api/orders/{id}/cancel
    participant SVC  as ⚙️ OrderServiceImpl
    participant ORD  as 🗄️ ShopOrderRepo
    participant STAT as 🗄️ OrderStatusRepo
    participant LINE as 🗄️ OrderLineRepo
    participant STCK as 🗄️ VariantStockRepo

    User->>CTL: PATCH /api/orders/{orderId}/cancel<br/>Authorization: Bearer token

    CTL->>SVC: cancelOrder(username, orderId)

    SVC->>ORD: findByIdAndUserId(orderId, userId)
    alt Đơn không tồn tại / không thuộc user
        SVC-->>User: 404 order.notFound
    end

    SVC->>STAT: findById(order.orderStatus)
    SVC->>SVC: currentStatus == "PENDING"?
    alt Không phải PENDING
        SVC-->>User: 400 order.cannotCancel<br/>("Chỉ hủy được đơn ở trạng thái PENDING")
    end

    Note over SVC: restoreStock() — Hoàn trả tồn kho
    SVC->>LINE: findByOrderId(orderId)
    loop Mỗi OrderLine
        SVC->>STCK: findById(variantStockId)
        SVC->>STCK: save(stock với stockQty + line.qty)
    end

    SVC->>STAT: findByStatus("CANCELLED")
    SVC->>ORD: save(order với status = CANCELLED)
    SVC-->>CTL: OrderDetailDTO { status: CANCELLED }
    CTL-->>User: 200 OK { "Hủy đơn hàng thành công" }
```

---

## 🔄 Flowchart `placeOrder()`

```mermaid
flowchart TD
    A([▶ Start: POST /api/orders]) --> B{JWT hợp lệ?}
    B -- Không --> C[❌ 401 Unauthorized]
    B -- Có --> D[@Valid: paymentTypeId, shippingAddressId, shippingMethodId]
    D --> E{Validation OK?}
    E -- Không --> F[❌ 400 Bad Request]
    E -- Có --> G[Kiểm tra PaymentType tồn tại]
    G --> H{Found?}
    H -- Không --> I[❌ 404 payment.typeNotFound]
    H -- Có --> J[Kiểm tra Address thuộc user]
    J --> K{OK?}
    K -- Không --> L[❌ 400 order.addressNotBelongToUser]
    K -- Có --> M[Kiểm tra ShippingMethod tồn tại]
    M --> N{Found?}
    N -- Không --> O[❌ 404 shipping.notFound]
    N -- Có --> P[Lấy giỏ hàng + cartItems]
    P --> Q{Giỏ hàng trống?}
    Q -- Có --> R[❌ 400 order.cartEmpty]
    Q -- Không --> S[Kiểm tra tồn kho TẤT CẢ items]
    S --> T{Tất cả đủ hàng?}
    T -- Không --> U[❌ 400 order.insufficientStock]
    T -- Có --> V[Tính subtotal + shippingFee = orderTotal]
    V --> W[Lấy trạng thái PENDING]
    W --> X[Sinh mã đơn: DH + yyyyMMdd + 3 số]
    X --> Y[save ShopOrder]
    Y --> Z[Loop: save OrderLine + stock.qty -= qty]
    Z --> AA[deleteByCartId — Xóa giỏ hàng]
    AA --> AB{Thanh toán = Chuyển khoản?}
    AB -- Có --> AC[Tạo VietQR URL]
    AC --> AD([✅ 201 Created: OrderDetailDTO + qrUrl])
    AB -- Không --> AD

    style A fill:#d5e8d4,stroke:#82b366
    style AD fill:#d5e8d4,stroke:#82b366
    style C fill:#f8cecc,stroke:#b85450
    style F fill:#f8cecc,stroke:#b85450
    style I fill:#f8cecc,stroke:#b85450
    style L fill:#f8cecc,stroke:#b85450
    style O fill:#f8cecc,stroke:#b85450
    style R fill:#f8cecc,stroke:#b85450
    style U fill:#f8cecc,stroke:#b85450
```

---

## 💳 Luồng Thanh Toán Theo Loại

### COD (Tiền Mặt Khi Nhận Hàng)
```
Đặt hàng → PENDING → PROCESSING → SHIPPING → COMPLETED
                                              ↑ Xác nhận nhận hàng
```

### Bank Transfer (Chuyển Khoản)
```
Đặt hàng → PENDING + QR Code URL
    ↓
User quét QR → Chuyển khoản với nội dung "DH20260326001"
    ↓
Admin xác nhận → PROCESSING → SHIPPING → COMPLETED
```

### VietQR URL Format
```
https://img.vietqr.io/image/{bankId}-{accountNumber}-compact2.png
    ?amount={orderTotal}
    &addInfo={orderCode}            ← nội dung chuyển khoản
    &accountName={holderName}
```

---

## 📊 Order Status Flow

```
PENDING → PROCESSING → SHIPPING → COMPLETED
   ↓
CANCELLED  (chỉ từ PENDING, user hoặc admin)
```

| Status | Mô tả | Ai thay đổi |
|--------|--------|-------------|
| `PENDING` | Vừa đặt hàng, chờ xác nhận | Hệ thống tự set |
| `PROCESSING` | Đang xử lý / đã xác nhận thanh toán | Admin |
| `SHIPPING` | Đang giao hàng | Admin |
| `COMPLETED` | Đã nhận hàng thành công | Admin |
| `CANCELLED` | Đã hủy (tồn kho được hoàn trả) | User (chỉ PENDING) / Admin |

---

## 📦 Request / Response

### Request — Đặt hàng
```
POST /api/orders
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "paymentTypeId": 2,
  "shippingAddressId": 1,
  "shippingMethodId": 1,
  "note": "Giao giờ hành chính"
}
```

### Response thành công — COD
```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "id": 5,
    "orderCode": "DH20260326001",
    "orderDate": "2026-03-26T10:30:00",
    "statusName": "PENDING",
    "paymentTypeName": "COD",
    "shippingMethodName": "Giao hàng tiêu chuẩn",
    "shippingFee": 30000,
    "subtotal": 500000,
    "orderTotal": 530000,
    "qrUrl": null,
    "bankInfo": null,
    "items": [...]
  }
}
```

### Response thành công — Chuyển khoản
```json
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "orderCode": "DH20260326002",
    "orderTotal": 530000,
    "qrUrl": "https://img.vietqr.io/image/MB-0123456789-compact2.png?amount=530000&addInfo=DH20260326002&accountName=SHOP",
    "bankInfo": {
      "bankName": "MB Bank",
      "accountNumber": "0123456789",
      "accountHolderName": "CLOTHING STORE"
    }
  }
}
```

---

## 📋 Tất Cả Order Endpoints

| Endpoint | Method | Auth | Mô tả |
|----------|--------|------|-------|
| `/api/orders` | POST | ✅ User | Đặt hàng từ giỏ |
| `/api/orders` | GET | ✅ User | Lịch sử đơn hàng của tôi |
| `/api/orders/{id}` | GET | ✅ User | Chi tiết 1 đơn |
| `/api/orders/{id}/cancel` | PATCH | ✅ User | Hủy đơn (chỉ PENDING) |
| `/api/orders/admin/all` | GET | 🔐 ADMIN | Tất cả đơn hàng (phân trang) |
| `/api/orders/admin/{id}` | GET | 🔐 ADMIN | Chi tiết đơn bất kỳ |
| `/api/orders/admin/by-status/{statusId}` | GET | 🔐 ADMIN | Lọc theo trạng thái |
| `/api/orders/{id}/status` | PATCH | 🔐 ADMIN | Cập nhật trạng thái |

