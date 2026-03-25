# 🗺️ Sơ Đồ Use Case Tổng — Clothing Store API

> Cập nhật: 2026-03-25 · Phiên bản API hiện tại: 81 endpoints (15/17 module đã hoàn thành)

## 🎯 Sơ Đồ Use Case Tổng Quan

```mermaid
usecaseDiagram
actor Guest
actor User
actor Admin
actor "Payment Provider" as PayExt
actor "Shipping Provider" as ShipExt

rectangle "Auth" {
  Guest -- (Register)
  Guest -- (Login)
  User -- (Change password)
}

rectangle "Catalog (Public)" {
  Guest -- (Browse products & search)
  Guest -- (View product detail)
  Guest -- (View categories/colors/sizes)
}

rectangle "Cart" {
  User -- (View cart)
  User -- (Add item to cart)
  User -- (Update cart item)
  User -- (Remove cart item)
  User -- (Clear cart)
}

rectangle "Order (User)" {
  User -- (Place order)
  User -- (View order history)
  User -- (View order detail)
  User -- (Cancel order [PENDING])
  (Place order) ..> PayExt : <<uses>> (Bank transfer / QR)
  (Place order) ..> ShipExt : <<notifies>> (Shipping)
}

rectangle "Order (Admin)" {
  Admin -- (View all orders)
  Admin -- (View any order)
  Admin -- (Update order status)
}

rectangle "Profile & Payments" {
  User -- (Manage addresses)
  User -- (Set default address)
  User -- (Manage payment methods)
  User -- (Set default payment method)
}

rectangle "Catalog Mgmt (Admin)" {
  Admin -- (Manage categories)
  Admin -- (Manage colors)
  Admin -- (Manage sizes)
  Admin -- (Manage products)
  Admin -- (Manage product variants)
  Admin -- (Manage variant stocks)
  Admin -- (Upload/delete images)
}

rectangle "Config (Public)" {
  Guest -- (View payment types)
  Guest -- (View shipping methods)
  Guest -- (View order statuses)
  Guest -- (View active shop bank account)
}

rectangle "Future (TBD)" {
  User -- (Write product review)
  Guest -- (View product reviews)
  Admin -- (Manage promotions)
}
```

## 🧭 Cách Đọc
- **Guest**: người dùng chưa đăng nhập (xem catalog, cấu hình public).
- **User**: đã đăng nhập (cart, đặt hàng, địa chỉ, phương thức thanh toán).
- **Admin**: quản trị (quản lý catalog, đơn hàng, cấu hình hệ thống).
- **Payment Provider**: đối tác thanh toán (VietQR/VNPAY; webhook sẽ bổ sung sau).
- **Shipping Provider**: dịch vụ giao hàng (thông báo trạng thái giao hàng).

## 📌 Ghi Chú
- Các use case trong khối **Future (TBD)** chưa có API (Promotion, Review, Payment webhook).
- Sơ đồ phản ánh đúng các endpoint đã triển khai trong `FRONTEND_API_GUIDE.md` (81 APIs).
- Nếu cần chi tiết hơn, có thể tách sơ đồ thành 2 phần: **User-facing** và **Admin-facing** để giảm độ phức tạp.
