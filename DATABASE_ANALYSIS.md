# 📊 Phân Tích Thiết Kế Database – Clothing Store

> Cập nhật: 2026-03-23  
> Database: `clothing_db` (MySQL, charset utf8mb4)

---

## 1. Tổng Quan Kiến Trúc

Database được chia thành **6 nhóm chức năng** chính:

```
┌─────────────────────────────────────────────────────────────────┐
│  [Auth/User]  →  [Product Catalog]  →  [Cart]  →  [Order]      │
│                      ↑                                           │
│               [Promotion]    [Review]                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Nhóm 1 – Xác Thực & Quản Lý Người Dùng

### Bảng liên quan
| Bảng | Mô tả |
|------|-------|
| `site_user` | Tài khoản người dùng (username, email, password, role) |
| `country` | Danh sách quốc gia |
| `address` | Địa chỉ cụ thể (số nhà, đường, tỉnh, quốc gia) |
| `user_address` | Mapping nhiều-nhiều giữa user và address (có cờ `is_default`) |

### Sơ đồ quan hệ
```
site_user ──< user_address >── address ──> country
```

### Chức năng đã implement (API)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/api/auth/register` | Đăng ký tài khoản mới |
| `POST` | `/api/auth/login` | Đăng nhập → trả về JWT token |
| `POST` | `/api/auth/change-password` | Đổi mật khẩu |

### Thiết kế đặc biệt
- `role`: Enum `USER` / `ADMIN` → phân quyền JWT
- `user_address.is_default`: Đánh dấu địa chỉ giao hàng mặc định
- Password được **BCrypt** hash trước khi lưu

### ⚠️ Chức năng chưa implement
- API quản lý địa chỉ (CRUD address)
- API quản lý profile người dùng

---

## 3. Nhóm 2 – Danh Mục Sản Phẩm (Product Catalog)

Đây là nhóm **phức tạp nhất** và **đã implement đầy đủ nhất**.

### Bảng liên quan
| Bảng | Mô tả |
|------|-------|
| `categories` | Danh mục, hỗ trợ **cây cha/con** qua `parent_id` |
| `colors` | Màu sắc (tên + mã hex, ví dụ: `#FF5733`) |
| `sizes` | Kích cỡ (S/M/L/XL, 28/30, 36/37...) theo 3 loại: clothing, numeric, shoes |
| `products` | Sản phẩm gốc (tên, slug, giá cơ sở, thương hiệu, chất liệu) |
| `product_variants` | **Biến thể theo màu** – mỗi (product × color) là 1 biến thể |
| `variant_stocks` | **Tồn kho** theo (variant × size) – có SKU riêng, giá override |

### Sơ đồ quan hệ
```
categories (tự tham chiếu: parent_id)
    │
    └──> products
              │
              └──> product_variants (product × color)
                        │
                        └──> variant_stocks (variant × size)
                                  │ SKU duy nhất
                                  │ stock_qty
                                  └ price_override
```

### Cách hoạt động 3 tầng
```
Sản phẩm:    Áo thun Nike
    │
    ├── Biến thể Màu Đỏ  (#FF0000)
    │       ├── Size S  → SKU: NIKE-RED-S  → qty: 10, giá: 250.000
    │       ├── Size M  → SKU: NIKE-RED-M  → qty: 5,  giá: 250.000
    │       └── Size L  → SKU: NIKE-RED-L  → qty: 0,  giá: 250.000
    │
    └── Biến thể Màu Xanh (#0000FF)
            ├── Size S  → SKU: NIKE-BLU-S  → qty: 8,  giá: 280.000 (override)
            └── Size M  → SKU: NIKE-BLU-M  → qty: 3,  giá: 280.000 (override)
```

### Chức năng đã implement (API)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST/PUT/DELETE` | `/api/categories` | CRUD danh mục |
| `GET` | `/api/categories/roots` | Danh mục gốc (không có parent) |
| `GET` | `/api/categories/{id}/children` | Danh mục con |
| `POST/PUT/DELETE` | `/api/colors` | CRUD màu sắc |
| `POST/PUT/DELETE` | `/api/sizes` | CRUD kích cỡ |
| `GET` | `/api/sizes/type/{type}` | Lọc size theo loại |
| `POST/PUT/DELETE` | `/api/products` | CRUD sản phẩm |
| `GET` | `/api/products/search` | **Tìm kiếm** theo: tên, danh mục, màu, giá, trạng thái |
| `GET` | `/api/products/paged` | Phân trang sản phẩm |
| `POST/PUT/DELETE` | `/api/product-variants` | CRUD biến thể |
| `GET` | `/api/product-variants/product/{id}` | Biến thể của 1 sản phẩm |
| `POST/PUT/DELETE` | `/api/variant-stocks` | CRUD tồn kho |
| `GET` | `/api/variant-stocks/search` | Tìm kiếm tồn kho theo SKU |
| `POST` | `/api/files/upload` | Upload ảnh sản phẩm |

### Thiết kế đặc biệt
- `categories.slug` + `products.slug`: URL-friendly, unique → dùng cho SEO
- `product_variants.is_default`: Màu mặc định hiển thị → tự động set `thumbnailUrl` cho `ProductDTO`
- `variant_stocks.price_override`: Cho phép từng size có giá riêng (ví dụ size lớn đắt hơn)
- `products.is_active`: Ẩn/hiện sản phẩm mà không cần xóa
- **Tìm kiếm dùng JPA Specification** (`ProductSpecification`) → lọc linh hoạt nhiều tiêu chí

---

## 4. Nhóm 3 – Khuyến Mãi (Promotion)

### Bảng liên quan
| Bảng | Mô tả |
|------|-------|
| `promotion` | Chương trình KM (tên, mô tả, % giảm, ngày bắt đầu/kết thúc) |
| `promotion_category` | Áp dụng KM cho danh mục nào (nhiều-nhiều) |

### Sơ đồ quan hệ
```
promotion ──< promotion_category >── categories
```

### Chức năng đã implement
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST/PUT/DELETE/GET` | `/api/promotions` | CRUD khuyến mãi (có service, chưa có controller route đầy đủ) |

### ⚠️ Chức năng chưa implement
- Tự động tính giá sau khuyến mãi khi xem sản phẩm
- API lấy sản phẩm đang được giảm giá

---

## 5. Nhóm 4 – Giỏ Hàng & Thanh Toán (Cart & Payment)

### Bảng liên quan
| Bảng | Mô tả |
|------|-------|
| `payment_type` | Loại thanh toán (Visa, MoMo, COD...) |
| `user_payment_method` | Phương thức thanh toán đã lưu của user |
| `shopping_cart` | Giỏ hàng của user (mỗi user 1 giỏ) |
| `shopping_cart_item` | Chi tiết item trong giỏ → liên kết `variant_stocks` + số lượng |

### Sơ đồ quan hệ
```
site_user ──> shopping_cart ──< shopping_cart_item ──> variant_stocks
site_user ──< user_payment_method ──> payment_type
```

### ⚠️ Chức năng chưa implement (chưa có Controller)
- API thêm/xóa/sửa item trong giỏ hàng
- API xem giỏ hàng
- API quản lý phương thức thanh toán

---

## 6. Nhóm 5 – Đặt Hàng (Order)

### Bảng liên quan
| Bảng | Mô tả |
|------|-------|
| `shipping_method` | Phương thức vận chuyển (tên, phí) |
| `order_status` | Trạng thái đơn hàng (Pending, Processing, Shipped, Delivered, Cancelled) |
| `shop_order` | Đơn hàng chính (user, ngày, địa chỉ, phí ship, tổng tiền, trạng thái) |
| `order_line` | Chi tiết từng mặt hàng trong đơn → `variant_stocks` + số lượng + giá tại thời điểm mua |

### Sơ đồ quan hệ
```
site_user ──< shop_order ──< order_line ──> variant_stocks
shop_order ──> address           (địa chỉ giao hàng)
shop_order ──> user_payment_method
shop_order ──> shipping_method
shop_order ──> order_status
```

### Thiết kế đặc biệt
- `order_line.price`: Lưu **giá tại thời điểm mua** → không bị ảnh hưởng khi sản phẩm thay đổi giá sau này

### ⚠️ Chức năng chưa implement (chưa có Controller)
- API tạo đơn hàng
- API cập nhật trạng thái đơn hàng
- API xem lịch sử đơn hàng
- Kiểm tra tồn kho khi đặt hàng (trừ `stock_qty`)

---

## 7. Nhóm 6 – Đánh Giá Sản Phẩm (Review)

### Bảng liên quan
| Bảng | Mô tả |
|------|-------|
| `user_review` | Đánh giá của user cho sản phẩm đã mua (rating + comment) |

### Sơ đồ quan hệ
```
site_user ──< user_review ──> order_line
```

### Thiết kế đặc biệt
- Chỉ review được khi đã **mua thực sự** (`order_line`) → chống fake review

### ⚠️ Chức năng chưa implement
- API gửi đánh giá
- API lấy đánh giá theo sản phẩm

---

## 8. Tổng Hợp Trạng Thái Implement

| Nhóm | Database | Entity | Repository | Service | Controller | Trạng thái |
|------|----------|--------|------------|---------|------------|------------|
| Auth/User | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| Category | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| Color | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| Size | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| Product | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| ProductVariant | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| VariantStock | ✅ | ✅ | ✅ | ✅ | ✅ | **Hoàn chỉnh** |
| FileUpload | – | – | – | ✅ | ✅ | **Hoàn chỉnh** |
| Promotion | ✅ | ✅ | ✅ | ✅ | ⚠️ | **Thiếu Controller** |
| Address | ✅ | ✅ | ✅ | ❌ | ❌ | **Chưa làm** |
| ShoppingCart | ✅ | ✅ | ✅ | ❌ | ❌ | **Chưa làm** |
| Order | ✅ | ✅ | ✅ | ❌ | ❌ | **Chưa làm** |
| Review | ✅ | ✅ | ✅ | ❌ | ❌ | **Chưa làm** |
| Payment | ✅ | ✅ | ✅ | ❌ | ❌ | **Chưa làm** |

---

## 9. Điểm Mạnh Trong Thiết Kế

1. **Mô hình 3 tầng sản phẩm** (Product → Variant → Stock): Linh hoạt, hỗ trợ nhiều màu + size
2. **Slug trên Category & Product**: Hỗ trợ SEO, URL thân thiện
3. **Danh mục cây cha/con**: Hỗ trợ cấu trúc phân cấp (Áo → Áo thun, Áo sơ mi...)
4. **price_override trên VariantStock**: Linh hoạt định giá theo kích cỡ
5. **Lưu giá tại thời điểm mua (order_line.price)**: Đảm bảo tính toàn vẹn lịch sử đơn hàng
6. **Chống fake review**: Review chỉ từ order_line → xác minh đã mua
7. **JWT + BCrypt**: Bảo mật chuẩn

---

## 10. Đề Xuất Phát Triển Tiếp Theo

Theo thứ tự ưu tiên:

1. **Shopping Cart API** – Thêm/sửa/xóa giỏ hàng
2. **Order API** – Tạo đơn hàng, cập nhật trạng thái, lịch sử mua
3. **Address API** – Quản lý địa chỉ giao hàng của user
4. **Review API** – Đánh giá sản phẩm
5. **Promotion Engine** – Tính toán giá sau KM theo danh mục
6. **Payment API** – Lưu phương thức thanh toán

