# 💳 Luồng Thanh Toán & Đặt Hàng — Clothing Store API

> Cập nhật: 2026-03-25  
> Source: `OrderServiceImpl.java` (467 dòng) · `OrderController.java` (158 dòng)

---

## 1. Tổng Quan

Hệ thống hiện tại hỗ trợ **thanh toán offline** — không tích hợp payment gateway tự động.  
Có 2 luồng thanh toán chính:

| Loại | Cách hoạt động | QR Code |
|------|---------------|---------|
| **COD** (Thanh toán khi nhận hàng) | User đặt hàng → Admin xác nhận → Giao hàng → Thu tiền | ❌ |
| **Chuyển khoản ngân hàng** | User đặt hàng → Nhận QR VietQR → Tự chuyển khoản → Admin xác nhận | ✅ |

> ⚠️ Chưa có: VNPAY, MoMo, ZaloPay webhooks (payment gateway tự động).

---

## 2. Sơ Đồ Tổng Quan

```
┌──────────┐                                              ┌───────────┐
│  User    │                                              │   Admin   │
└────┬─────┘                                              └─────┬─────┘
     │                                                          │
     │  1. Chọn sản phẩm → Thêm vào giỏ                       │
     │  2. Vào trang Checkout                                   │
     │                                                          │
     │  POST /api/orders                                        │
     │  {                                                       │
     │    paymentTypeId: 1,      ← COD hoặc Chuyển khoản      │
     │    shippingAddressId: 5,  ← địa chỉ giao hàng          │
     │    shippingMethodId: 2,   ← Standard / Express          │
     │    note: "Giao giờ HC"    ← ghi chú (tuỳ chọn)         │
     │  }                                                       │
     │         │                                                │
     │         ▼                                                │
     │  ┌─────────────────────────────────────┐                │
     │  │  OrderServiceImpl.placeOrder()      │                │
     │  │  (10 bước — xem Section 3)          │                │
     │  └──────────────┬──────────────────────┘                │
     │                 │                                        │
     │                 ▼                                        │
     │  ┌─────────────────────────────────────┐                │
     │  │  Đơn hàng: PENDING                  │                │
     │  │  Mã đơn: DH20260325001             │                │
     │  │                                     │                │
     │  │  Nếu Chuyển khoản:                 │                │
     │  │    → qrUrl (VietQR)                │                │
     │  │    → bankInfo (TK NH shop)         │                │
     │  └──────────────┬──────────────────────┘                │
     │                 │                                        │
     │    ┌────────────┴────────────┐                          │
     │    │                         │                          │
     │    ▼ (COD)                   ▼ (Chuyển khoản)           │
     │                                                          │
     │  Chờ giao hàng          User quét QR VietQR             │
     │                         → Chuyển tiền vào TK shop       │
     │                         → Nội dung: DH20260325001       │
     │                                                          │
     │                              │                          │
     │                              ▼                          │
     │                         Admin kiểm tra                  │
     │                         nhận tiền trong                 │
     │                         app ngân hàng ────────────────→ │
     │                                                          │
     │                                    PATCH /api/orders/admin/{id}/status
     │                                    { statusId: 2 }  ← PROCESSING
     │                                          │
     │                                          ▼
     │                                    PATCH .../status
     │                                    { statusId: 3 }  ← SHIPPED
     │                                          │
     │                                          ▼
     │                                    PATCH .../status
     │                                    { statusId: 4 }  ← DELIVERED
     │                                                          │
     │  ← ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ Nhận hàng ─ ─ ─ ─ ─ ─ ─ ─ ┘
     │
     ▼
   DONE
```

---

## 3. Chi Tiết 10 Bước Đặt Hàng (`placeOrder`)

Toàn bộ 10 bước chạy trong **1 transaction** (`@Transactional`) — nếu bất kỳ bước nào fail, **rollback tất cả**.

```
POST /api/orders
     │
     ▼
┌─── BƯỚC 1: Validate loại thanh toán ──────────────────────┐
│                                                            │
│  paymentTypeRepository.findById(request.paymentTypeId)     │
│                                                            │
│  Ví dụ payment_type:                                       │
│    id=1  value="COD"                                       │
│    id=2  value="Chuyển khoản ngân hàng"                   │
│    id=3  value="VNPAY"                                     │
│                                                            │
│  → Không tìm thấy: 404 "payment.typeNotFound"             │
└────────────────────────┬───────────────────────────────────┘
                         │ OK
                         ▼
┌─── BƯỚC 2: Validate địa chỉ giao hàng ───────────────────┐
│                                                            │
│  userAddressRepository.existsByUserIdAndAddressId(         │
│      user.id, request.shippingAddressId)                   │
│                                                            │
│  → Địa chỉ không thuộc user: 400 "order.addressNot..."    │
└────────────────────────┬───────────────────────────────────┘
                         │ OK
                         ▼
┌─── BƯỚC 3: Validate phương thức vận chuyển ───────────────┐
│                                                            │
│  shippingMethodRepository.findById(request.shippingMethod) │
│                                                            │
│  Ví dụ shipping_method:                                    │
│    id=1  name="Standard"  price=30000                      │
│    id=2  name="Express"   price=50000                      │
│                                                            │
│  → Không tìm thấy: 404 "shipping.notFound"                │
└────────────────────────┬───────────────────────────────────┘
                         │ OK
                         ▼
┌─── BƯỚC 4: Lấy giỏ hàng ─────────────────────────────────┐
│                                                            │
│  cart = cartRepository.findByUserId(user.id)               │
│  cartItems = cartItemRepository.findByCartId(cart.id)      │
│                                                            │
│  → Giỏ rỗng: 400 "order.cartEmpty"                        │
└────────────────────────┬───────────────────────────────────┘
                         │ OK
                         ▼
┌─── BƯỚC 5: Validate tồn kho + Tính subtotal ─────────────┐
│                                                            │
│  Duyệt TẤT CẢ items trong giỏ:                           │
│                                                            │
│  for (cartItem : cartItems) {                              │
│    stock = stockMap.get(cartItem.variantStockId)            │
│                                                            │
│    if (stock.stockQty < cartItem.qty)                      │
│      → 400 "Không đủ tồn kho SKU: ..., chỉ còn: ..."     │
│                                                            │
│    unitPrice = stock.priceOverride ?? product.basePrice     │
│    subtotal += unitPrice × cartItem.qty                    │
│  }                                                         │
│                                                            │
│  ⚠️ Check TẤT CẢ items trước khi tạo đơn                │
│     (không phải check từng cái rồi trừ luôn)              │
└────────────────────────┬───────────────────────────────────┘
                         │ OK
                         ▼
┌─── BƯỚC 6: Lấy trạng thái PENDING ───────────────────────┐
│                                                            │
│  orderStatusRepository.findByStatus("PENDING")             │
│                                                            │
│  → Chưa cấu hình: 400 "order.pendingStatusNotConfigured"  │
│    (tự động tạo khi app khởi động — PostConstruct)        │
└────────────────────────┬───────────────────────────────────┘
                         │ OK
                         ▼
┌─── BƯỚC 7: Sinh mã đơn hàng ─────────────────────────────┐
│                                                            │
│  Format: DH + yyyyMMdd + 3 chữ số                         │
│                                                            │
│  Ví dụ: DH20260325001, DH20260325002, ...                 │
│                                                            │
│  Logic:                                                    │
│    datePrefix = "DH" + "20260325"                          │
│    count = orderRepository.countByOrderCodeStartingWith(…) │
│    orderCode = datePrefix + String.format("%03d", count+1) │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌─── BƯỚC 8: Tạo đơn hàng (ShopOrder) ─────────────────────┐
│                                                            │
│  orderTotal = subtotal + shippingMethod.price              │
│                                                            │
│  ShopOrder {                                               │
│    userId:          10                                     │
│    orderDate:       2026-03-25T14:30:00                    │
│    orderCode:       "DH20260325001"                        │
│    paymentTypeId:   2        ← Chuyển khoản               │
│    paymentNote:     "Giao giờ HC"                          │
│    shippingAddress: 5        ← address.id                  │
│    shippingMethod:  1        ← shipping_method.id          │
│    orderTotal:      530000   ← subtotal + ship fee         │
│    orderStatus:     1        ← PENDING                     │
│  }                                                         │
│                                                            │
│  → orderRepository.save(order)                             │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌─── BƯỚC 9: Tạo order lines + TRỪ TỒN KHO ───────────────┐
│                                                            │
│  for (cartItem : cartItems) {                              │
│                                                            │
│    OrderLine {                                             │
│      orderId:        order.id                              │
│      variantStockId: cartItem.variantStockId               │
│      qty:            cartItem.qty                          │
│      price:          unitPrice   ← GIÁ TẠI THỜI ĐIỂM MUA │
│    }                                                       │
│    → orderLineRepository.save(line)                        │
│                                                            │
│    ⭐ TRỪ TỒN KHO:                                       │
│    stock.stockQty -= cartItem.qty                          │
│    → variantStockRepository.save(stock)                    │
│  }                                                         │
│                                                            │
│  ⚠️ Nếu fail ở đây → rollback toàn bộ (@Transactional)  │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌─── BƯỚC 10: Xóa giỏ hàng ────────────────────────────────┐
│                                                            │
│  cartItemRepository.deleteByCartId(cart.id)                │
│                                                            │
│  → Giỏ hàng trống sau khi đặt hàng thành công            │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
              buildOrderDetail(order)
                         │
                         ▼
            ┌────────────────────────┐
            │  Response 201 CREATED  │
            │  OrderDetailDTO        │
            │  (xem Section 5)       │
            └────────────────────────┘
```

---

## 4. Luồng VietQR (Chuyển Khoản)

Khi `paymentType.value` chứa `"chuyển khoản"` / `"chuyen khoan"` / `"bank transfer"` (case-insensitive):

```
┌─── buildOrderDetail() ───────────────────────────────────┐
│                                                           │
│  isTransferPayment(paymentType.value) = true?             │
│     │                                                     │
│     ▼ YES                                                 │
│  shopBankAccountRepository.findByIsActiveTrue()            │
│     │                                                     │
│     ▼                                                     │
│  ShopBankAccount {                                        │
│    bankId:            "MB"                                │
│    bankName:          "MB Bank"                           │
│    accountNumber:     "0123456789"                        │
│    accountHolderName: "NGUYEN VAN A"                      │
│    logoUrl:           "https://..."                       │
│    isActive:          true                                │
│  }                                                        │
│     │                                                     │
│     ▼                                                     │
│  buildVietQrUrl(bank, orderTotal, orderCode)              │
│     │                                                     │
│     ▼                                                     │
│  URL = "https://img.vietqr.io/image/                     │
│         MB-0123456789-compact2.png                        │
│         ?amount=530000                                    │
│         &addInfo=DH20260325001                            │
│         &accountName=NGUYEN+VAN+A"                        │
│                                                           │
│  → dto.qrUrl = URL                                        │
│  → dto.bankInfo = ShopBankAccountDTO                      │
└───────────────────────────────────────────────────────────┘
```

### Frontend hiển thị QR:

```
┌───────────────────────────────────────┐
│  Thanh toán chuyển khoản              │
│                                       │
│  ┌───────────────────┐               │
│  │                   │               │
│  │   [QR CODE IMG]   │  ← <img src={qrUrl} />
│  │                   │               │
│  └───────────────────┘               │
│                                       │
│  Ngân hàng:   MB Bank                │  ← bankInfo.bankName
│  Số TK:       0123456789             │  ← bankInfo.accountNumber
│  Chủ TK:      NGUYEN VAN A          │  ← bankInfo.accountHolderName
│  Số tiền:     530,000 VNĐ           │  ← orderTotal
│  Nội dung CK: DH20260325001         │  ← orderCode
│                                       │
│  ⚠️ Quét QR sẽ tự động điền         │
│     số tiền + nội dung chuyển khoản  │
└───────────────────────────────────────┘
```

User quét QR bằng app ngân hàng → app tự điền: số tài khoản, số tiền, nội dung CK → User bấm xác nhận → Tiền chuyển vào TK shop.

---

## 5. Response Sau Khi Đặt Hàng

```json
{
  "success": true,
  "message": "Dat hang thanh cong",
  "data": {
    "id": 1,
    "orderCode": "DH20260325001",
    "userId": 10,
    "orderDate": "2026-03-25T14:30:00",

    "statusId": 1,
    "statusName": "PENDING",

    "paymentTypeId": 2,
    "paymentTypeName": "Chuyển khoản ngân hàng",
    "paymentNote": "Giao giờ HC",

    "qrUrl": "https://img.vietqr.io/image/MB-0123456789-compact2.png?amount=530000&addInfo=DH20260325001&accountName=NGUYEN+VAN+A",
    "bankInfo": {
      "id": 1,
      "bankId": "MB",
      "bankName": "MB Bank",
      "accountNumber": "0123456789",
      "accountHolderName": "NGUYEN VAN A",
      "logoUrl": "https://..."
    },

    "shippingMethodId": 1,
    "shippingMethodName": "Standard",
    "shippingFee": 30000,

    "shippingAddressId": 5,
    "shippingAddressDetail": {
      "id": 5,
      "addressLine1": "123 Nguyễn Huệ",
      "city": "TP.HCM",
      "region": "Quận 1"
    },

    "subtotal": 500000,
    "orderTotal": 530000,

    "items": [
      {
        "id": 1,
        "variantStockId": 5,
        "sku": "NIKE-RED-M",
        "qty": 2,
        "price": 250000,
        "subtotal": 500000,
        "productId": 1,
        "productName": "Áo Nike",
        "productSlug": "ao-nike",
        "colorName": "Đỏ",
        "colorHex": "#FF0000",
        "colorImageUrl": "/uploads/images/nike-red.jpg",
        "sizeLabel": "M",
        "sizeType": "clothing"
      }
    ]
  }
}
```

> **Lưu ý:** `qrUrl` và `bankInfo` chỉ có khi `paymentType` = Chuyển khoản. Với COD thì 2 field này là `null`.

---

## 6. Luồng Hủy Đơn

```
PATCH /api/orders/{orderId}/cancel
         │
         ▼
┌────────────────────────────────────────┐
│  1. Tìm đơn hàng theo ID + userId     │
│     → 404 nếu không tìm thấy          │
│     → 404 nếu không phải đơn của mình │
│                                        │
│  2. Check trạng thái = PENDING?        │
│     → 400 "order.cannotCancel"         │
│     (chỉ hủy được PENDING)            │
│                                        │
│  3. HOÀN TRẢ TỒN KHO ⭐              │
│     for (orderLine : lines) {          │
│       stock.stockQty += line.qty       │
│     }                                  │
│                                        │
│  4. Đổi trạng thái → CANCELLED        │
└────────────────────────────────────────┘
```

**Không thể hủy khi:** PROCESSING, SHIPPED, DELIVERED, CANCELLED.

---

## 7. Luồng Trạng Thái Đơn Hàng (Admin)

```
                    ┌─────────┐
     Đặt hàng ────→│ PENDING │────→ User hủy ────→ ┌───────────┐
                    └────┬────┘                     │ CANCELLED │
                         │                          └───────────┘
               Admin xác nhận                              ▲
                         │                                 │
                         ▼                          Admin hủy (bất kỳ lúc nào)
                   ┌────────────┐                          │
                   │ PROCESSING │──────────────────────────┘
                   └─────┬──────┘
                         │
                Admin gửi hàng
                         │
                         ▼
                    ┌─────────┐
                    │ SHIPPED │
                    └────┬────┘
                         │
                  Giao hàng thành công
                         │
                         ▼
                   ┌───────────┐
                   │ DELIVERED │
                   └───────────┘
```

### API Admin cập nhật trạng thái:

```
PATCH /api/orders/admin/{orderId}/status
{
  "statusId": 2    ← PROCESSING / SHIPPED / DELIVERED / CANCELLED
}
```

> ⚠️ Hiện tại API admin **cho phép chuyển sang bất kỳ trạng thái nào** — chưa có validation luồng (VD: không validate phải đi PENDING → PROCESSING → SHIPPED → DELIVERED theo thứ tự).

---

## 8. Cách Tính Giá

```
┌─────────────────────────────────────────────────────┐
│  resolveUnitPrice(stock, productByVariantId)         │
│                                                      │
│  if (stock.priceOverride != null)                    │
│      → dùng priceOverride                            │  ← Giá riêng theo size/màu
│  else                                                │
│      → dùng product.basePrice                        │  ← Giá gốc sản phẩm
│                                                      │
│  subtotal = Σ (unitPrice × qty)  cho tất cả items   │
│  orderTotal = subtotal + shippingMethod.price        │
└─────────────────────────────────────────────────────┘
```

**Ví dụ:**

```
Giỏ hàng:
  Item 1: Áo Nike Đỏ Size M  × 2 = 250,000 × 2 = 500,000đ   (dùng basePrice)
  Item 2: Áo Nike Xanh Size L × 1 = 280,000 × 1 = 280,000đ   (dùng priceOverride)

subtotal    = 500,000 + 280,000 = 780,000đ
shippingFee = 30,000đ (Standard)
orderTotal  = 780,000 + 30,000  = 810,000đ
```

---

## 9. Bảng Entities Tham Gia

| Entity | Vai trò trong luồng thanh toán |
|--------|-------------------------------|
| `ShoppingCart` | Giỏ hàng của user (1:1 với user) |
| `ShoppingCartItem` | Items trong giỏ → `variantStockId` + `qty` |
| `VariantStock` | Tồn kho — bị trừ khi đặt, hoàn khi hủy |
| `Product` | Lấy `basePrice` nếu không có `priceOverride` |
| `PaymentType` | Loại thanh toán: COD, Chuyển khoản, VNPAY... |
| `ShopBankAccount` | TK NH shop — dùng tạo VietQR URL (`isActive=true`) |
| `ShippingMethod` | Phương thức vận chuyển + `price` (phí ship) |
| `ShopOrder` | Đơn hàng chính |
| `OrderLine` | Chi tiết sản phẩm trong đơn — lưu giá tại thời điểm mua |
| `OrderStatus` | Trạng thái: PENDING → PROCESSING → SHIPPED → DELIVERED / CANCELLED |
| `Address` | Địa chỉ giao hàng |
| `UserAddress` | Mapping user ↔ address (validate địa chỉ thuộc user) |

---

## 10. Những Gì Chưa Có (Future)

| Feature | Mô tả | Trạng thái |
|---------|-------|-----------|
| **Payment Gateway Webhook** | VNPAY/MoMo gọi callback khi user thanh toán xong → tự động cập nhật trạng thái | ❌ |
| **Email Notification** | Gửi email khi đặt hàng / thay đổi trạng thái | ❌ |
| **Validate luồng trạng thái** | PENDING → PROCESSING → SHIPPED → DELIVERED (không cho nhảy cóc) | ❌ |
| **Auto-cancel đơn quá hạn** | Đơn PENDING quá 24h chưa thanh toán → tự động hủy + hoàn tồn kho | ❌ |
| **Refund** | Hoàn tiền khi admin hủy đơn đã thanh toán | ❌ |
| **Lưu payment proof** | User upload ảnh chụp màn hình chuyển khoản | ❌ |

