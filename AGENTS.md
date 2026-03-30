# AGENTS.md — Hướng Dẫn Cho AI Coding Agent

> **Clothing Store E-commerce API** — Kiến thức cần thiết cho AI agent làm việc với dự án Spring Boot 3.5.11 + Java 21 này.

> Xem nhanh sơ đồ use case tổng: [USE_CASES.md](USE_CASES.md)

---

## 🏗️ Kiến Trúc Tổng Thể

### Mô Hình Sản Phẩm 3 Tầng (★★★ CỰC KỲ QUAN TRỌNG)

```
Product (sản phẩm gốc: tên, slug, giá cơ sở, thương hiệu, chất liệu)
    ↓
ProductVariant (biến thể = sản phẩm × màu sắc, lưu colorImageUrl + images JSON)
    ↓
VariantStock (tồn kho = biến thể × size, có SKU riêng, stock_qty, price_override)
```

**Ví dụ thực tế:**
```
Áo thun Nike (Product)
  ├─ Màu Đỏ (ProductVariant) → colorImageUrl + ["img1.jpg", "img2.jpg"]
  │    ├─ Size S (VariantStock) → SKU: NIKE-RED-S, qty: 10, giá: 250.000đ
  │    ├─ Size M (VariantStock) → SKU: NIKE-RED-M, qty: 5, giá: 250.000đ
  │    └─ Size L (VariantStock) → SKU: NIKE-RED-L, qty: 0, giá: 250.000đ
  └─ Màu Xanh (ProductVariant)
       ├─ Size S (VariantStock) → SKU: NIKE-BLU-S, qty: 8, giá: 280.000đ (override)
       └─ Size M (VariantStock) → SKU: NIKE-BLU-M, qty: 3, giá: 280.000đ (override)
```

**Ràng Buộc FK (Foreign Key) - BẮT BUỘC PHẢI NHỚ:**
- ❌ **KHÔNG xóa được Product** nếu đang có ProductVariant
- ❌ **KHÔNG xóa được Color** nếu đang được dùng trong ProductVariant
- ❌ **KHÔNG xóa được Size** nếu đang được dùng trong VariantStock
- ❌ **KHÔNG xóa được Category** nếu đang có Product hoặc có Category con
- ⚠️ Service layer phải check trước khi xóa, throw `BusinessException` nếu vi phạm

**Thứ tự tạo sản phẩm hoàn chỉnh:**
```
1. Tạo Category     → POST /api/categories
2. Tạo Color        → POST /api/colors
3. Tạo Size         → POST /api/sizes
4. Tạo Product      → POST /api/products (cần categoryId)
5. Tạo Variant      → POST /api/product-variants (cần productId + colorId)
6. Tạo Stock        → POST /api/variant-stocks (cần variantId + sizeId)
```

Xem chi tiết: `PRODUCT_FLOW.md`

---

## 🔐 Xác Thực & Phân Quyền

### JWT Token (stateless, hết hạn sau 24h)

**Public endpoints (không cần token):**
- `POST /api/auth/**` (register, login)
- `POST /api/chat/message` (AI chatbot — không cần đăng nhập)
- `GET /api/products/**`, `/api/categories/**`, `/api/colors/**`, `/api/sizes/**`
- `GET /api/payment-types/**`, `/api/shipping-methods/**`, `/api/order-statuses/**`
- `GET /api/shop-bank-accounts/active` (user xem để chuyển khoản)
- `GET /api/reviews/product/**` (xem reviews sản phẩm)
- `/swagger-ui/**`, `/v3/api-docs/**`, `/uploads/images/**`
- `/actuator/**` (health, info, metrics)

**Endpoints cần xác thực (JWT Bearer token):**
- `/api/cart/**` (tất cả methods)
- `/api/orders/**` (đặt hàng, xem đơn, hủy đơn)
- `/api/addresses/**` (quản lý địa chỉ giao hàng)
- `/api/payment-methods/**` (quản lý phương thức thanh toán)
- Tất cả `POST/PUT/DELETE` cho products, categories, colors, sizes

**Endpoints ADMIN only:**
- `GET /api/orders/admin/**` (xem tất cả đơn, lọc theo trạng thái)
- `PATCH /api/orders/admin/{orderId}/status` (cập nhật trạng thái đơn)
- `POST/PUT /api/products/full/**` (tạo/cập nhật sản phẩm đầy đủ)
- Annotation: `@PreAuthorize("hasRole('ADMIN')")`

### Cách lấy user hiện tại trong Controller

```java
@PostMapping
public ApiResponse<T> someMethod(@AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername(); // Username duy nhất
    // ... logic
}
```

**Flow xác thực:**
```
Request → JwtAuthenticationFilter (trích username từ token)
       → CustomUserDetailsService (load SiteUser từ DB)
       → SecurityContext (lưu UserDetails)
       → Controller (inject qua @AuthenticationPrincipal)
```

---

## 🎯 Code Patterns Bắt Buộc

### 1. Response Format Chuẩn

**Tất cả controller PHẢI trả về `ApiResponse<T>`:**
```java
// Success
return ApiResponse.success("Thông báo thành công", data);

// Success với pagination
Page<EntityDTO> page = service.getAll(pageable);
return ApiResponse.success(null, buildPagedResponse(page));
```

`ApiResponse` có cấu trúc:
```json
{
  "success": true,
  "message": "Thông báo",
  "data": { ... },
  "timestamp": "2026-03-25T10:30:00Z"
}
```

### 2. Entity ↔ DTO Mapping (MapStruct)

**Tất cả Mapper phải có config:**
```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    ProductDTO toDto(Product entity);
    Product toEntity(ProductDTO dto);
    List<ProductDTO> toDtoList(List<Product> entities);
    
    // Dùng cho UPDATE - giữ nguyên field null trong DTO
    void updateEntityFromDto(ProductDTO dto, @MappingTarget Product entity);
}
```

**Pattern trong Service:**
```java
// CREATE
Product entity = mapper.toEntity(dto);
entity.setId(null); // Đảm bảo tạo mới
return mapper.toDto(repository.save(entity));

// UPDATE
Product entity = repository.findById(id).orElseThrow(...);
mapper.updateEntityFromDto(dto, entity); // Chỉ update field không null
entity.setId(id); // Giữ nguyên ID
return mapper.toDto(repository.save(entity));
```

### 3. Xử Lý Lỗi (i18n với message key)

**Throw exception đúng cách:**
```java
// 404 - Resource not found
throw new ResourceNotFoundException("product.notFound", productId);

// 400 - Business logic error
throw new BusinessException("product.slugExists", slug);
throw new BusinessException("cart.notEnoughStock", sku, stockQty);
```

**Message key convention:** `{entity}.{action}.{outcome}`
- `product.notFound` = "Không tìm thấy sản phẩm với ID: {0}"
- `color.slugExists` = "Slug màu sắc đã tồn tại: {0}"
- `cart.notEnoughStock` = "Không đủ hàng cho SKU {0}, chỉ còn {1}"

**Thêm message mới vào `messages.properties`:**
```properties
# Pattern: {entity}.{action}.{outcome}
productReview.create.success=Tạo đánh giá sản phẩm thành công.
productReview.notFound=Không tìm thấy đánh giá với ID: {0}
productReview.alreadyReviewed=Bạn đã đánh giá sản phẩm này rồi.
```

### 4. Validation Input

**Dùng Jakarta Validation trên DTO:**
```java
public class ProductDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên không quá 200 ký tự")
    private String name;
    
    @NotNull(message = "ID danh mục bắt buộc")
    private Integer categoryId;
    
    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    private BigDecimal basePrice;
}
```

**Controller nhận với `@Valid`:**
```java
@PostMapping
public ApiResponse<ProductDTO> create(@Valid @RequestBody ProductDTO dto) {
    // Nếu validation fail → GlobalExceptionHandler tự động trả 400
}
```

---

## 🔧 Quy Trình Làm Việc

### Chạy Project Local (PowerShell)

```powershell
# Option 1: Chạy trực tiếp bằng Maven
cd C:\Users\Admin\Desktop\LOC\clothing-store\ec
mvn clean install                    # Build toàn bộ + chạy tests
mvn spring-boot:run                  # Khởi động app trên localhost:8080

# Option 2: Dùng Docker Compose (khuyến nghị)
cd C:\Users\Admin\Desktop\LOC\clothing-store
docker-compose up -d                 # MySQL (3307) + App (8080)
docker-compose logs -f app           # Xem logs real-time
docker-compose down                  # Dừng toàn bộ
```

### Testing API

**Swagger UI (khuyến nghị):**
- URL: `http://localhost:8080/swagger-ui.html`
- Có nút "Authorize" để nhập JWT token
- Test được tất cả endpoints với UI trực quan

**Import vào Postman:**
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

### Upload File Ảnh

**Cấu hình:** `application.yml` → `file.upload.dir: C:\CODE\uploads\images`

**API:**
```bash
POST /api/files/upload
Content-Type: multipart/form-data
Body: file = [chọn ảnh]

Response:
{
  "success": true,
  "data": {
    "url": "/uploads/images/1234567890_product.jpg"
  }
}
```

**Lưu ý:** File được serve public tại `/uploads/images/**`, frontend dùng URL này để hiển thị ảnh.

---

## 📊 Trạng Thái Dự Án (Cập nhật: 2026-03-30)

### ✅ ĐÃ TRIỂN KẢI HOÀN CHỈNH (70%)

#### 1. Authentication & Security
- ✅ Đăng ký tài khoản (`POST /api/auth/register`)
- ✅ Đăng nhập, nhận JWT token (`POST /api/auth/login`)
- ✅ Đổi mật khẩu (`POST /api/auth/change-password`)
- ✅ JWT Filter + SecurityConfig với phân quyền chi tiết

#### 2. Product Catalog (CRUD đầy đủ)
- ✅ Categories (hỗ trợ cây cha/con, 7 endpoints)
- ✅ Colors (6 endpoints, slug unique)
- ✅ Sizes (6 endpoints, hỗ trợ 3 loại: clothing/numeric/shoes)
- ✅ Products (8 endpoints, search/filter/pagination)
- ✅ ProductVariants (7 endpoints, quản lý biến thể theo màu)
- ✅ VariantStocks (8 endpoints, quản lý tồn kho theo size + SKU)

#### 3. Shopping Cart (5 endpoints)
- ✅ Xem giỏ hàng (`GET /api/cart`)
- ✅ Thêm sản phẩm vào giỏ (`POST /api/cart/items`)
- ✅ Cập nhật số lượng (`PUT /api/cart/items/{itemId}`)
- ✅ Xóa item (`DELETE /api/cart/items/{itemId}`)
- ✅ Làm trống giỏ (`DELETE /api/cart`)

#### 4. Order Management (8 endpoints)
- ✅ Đặt hàng từ giỏ (`POST /api/orders`) - tự động trừ tồn kho
- ✅ Lịch sử đơn hàng (`GET /api/orders`)
- ✅ Chi tiết đơn hàng (`GET /api/orders/{id}`)
- ✅ Hủy đơn hàng (`PATCH /api/orders/{id}/cancel`) - hoàn trả tồn kho
- ✅ [ADMIN] Xem tất cả đơn (`GET /api/orders/admin/all`)
- ✅ [ADMIN] Lọc đơn theo trạng thái (`GET /api/orders/admin/by-status/{statusId}`)
- ✅ [ADMIN] Chi tiết đơn bất kỳ (`GET /api/orders/admin/{id}`)
- ✅ [ADMIN] Cập nhật trạng thái đơn (`PATCH /api/orders/admin/{id}/status`)

#### 5. User Address Management (5 endpoints)
- ✅ Danh sách địa chỉ (`GET /api/addresses`)
- ✅ Thêm địa chỉ mới (`POST /api/addresses`)
- ✅ Cập nhật địa chỉ (`PUT /api/addresses/{id}`)
- ✅ Xóa địa chỉ (`DELETE /api/addresses/{id}`)
- ✅ Đặt địa chỉ mặc định (`PATCH /api/addresses/{id}/default`)

#### 6. Payment & Shipping Config
- ✅ Payment Types (CRUD đầy đủ: COD, Bank Transfer, VNPAY...)
- ✅ Payment Methods của user (thẻ tín dụng, ví điện tử...)
- ✅ Shipping Methods (CRUD: Standard, Express, Same Day...)
- ✅ Order Statuses (CRUD: PENDING, PROCESSING, COMPLETED...)
- ✅ Shop Bank Accounts (tài khoản NH shop để nhận tiền)

#### 7. Utilities
- ✅ File Upload (`POST /api/files/upload`) - lưu `C:\CODE\uploads\images`
- ✅ Sample Data Generator (`POST /api/sample-data/generate`)
- ✅ Swagger UI Documentation
- ✅ CORS Configuration (hỗ trợ frontend)
- ✅ Global Exception Handler với i18n
- ✅ CI/CD Pipeline (`.github/workflows/ci-cd.yml`) — build, test, Docker push

#### 8. Product Reviews & Ratings (5 endpoints) ✨ MỚI
- ✅ Tạo đánh giá (`POST /api/reviews`) — chỉ sau khi mua hàng
- ✅ Xem reviews theo sản phẩm (`GET /api/reviews/product/{productId}`)
- ✅ Xem reviews của tôi (`GET /api/reviews/my`)
- ✅ Thống kê đánh giá (`GET /api/reviews/product/{productId}/summary`)
- ✅ Xóa đánh giá (`DELETE /api/reviews/{id}`)

#### 9. AI Chatbot Shopping Assistant (2 endpoints) ✨ MỚI
- ✅ Gửi tin nhắn (`POST /api/chat/message`) — **không cần đăng nhập**
- ✅ Xóa session (`DELETE /api/chat/session/{sessionId}`)
- 🔌 Dual AI provider: **GitHub Models** (primary, miễn phí) → **Claude** (fallback)
- 🧠 In-memory session (tối đa 20 tin nhắn, TTL 2 giờ)
- 🔍 Tự động tìm sản phẩm liên quan từ DB làm context cho AI (dùng JPA Specification)

#### 10. Email Notifications ✨ MỚI
- ✅ Gửi email xác nhận đặt hàng (async, không block response)
- ✅ HTML email template (`templates/email/order-confirmation.html`)
- ✅ Async thread pool riêng (`AsyncConfig` → `emailTaskExecutor`)
- 📧 SMTP Gmail (`spring-boot-starter-mail`)

#### 11. Product Full Management (3 endpoints) ✨ MỚI
- ✅ Tạo sản phẩm đầy đủ (`POST /api/products/full`) — Product + Variants + Stocks trong 1 request [ADMIN]
- ✅ Cập nhật đầy đủ (`PUT /api/products/full/{id}`) — id có → update, id null → tạo mới [ADMIN]
- ✅ Xem đầy đủ (`GET /api/products/full/{id}`) — public
- 📝 DTO: `ProductFullRequest` (nested `VariantRequest` + `StockRequest`) → `ProductDetailResponse`

### ❌ CHƯA TRIỂN KHAI (6%)

#### 1. Promotion System (0%)
- ❌ CRUD khuyến mãi (discount %, fixed amount)
- ❌ Áp dụng mã giảm giá vào đơn hàng
- ❌ Khuyến mãi theo category
- 📝 Entity đã có (`Promotion`, `PromotionCategory`), `PromotionService` interface rỗng — chưa có logic/Controller

#### 2. Advanced Features
- ❌ Payment Gateway Integration (VNPAY, MoMo, ZaloPay)
- ❌ Forgot/Reset Password (DTO đã có: `ForgotPasswordRequest`, `ResetPasswordRequest` — chưa có endpoint)
- ❌ Inventory alerts (cảnh báo hết hàng)
- ❌ Admin Analytics Dashboard
- ❌ Product search nâng cao (full-text search, Elasticsearch)

---

## 🛠️ Các Pattern Code Bắt Buộc

### Controller Pattern

```java
@RestController
@RequestMapping("/api/entities")
@RequiredArgsConstructor
@Tag(name = "Entity", description = "API mô tả")
@SecurityRequirement(name = "bearerAuth") // Nếu cần auth
public class EntityController {
    
    private final EntityService service;
    
    @Operation(summary = "Tóm tắt", description = "Chi tiết")
    @GetMapping
    public ApiResponse<List<EntityDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EntityDTO> create(@Valid @RequestBody EntityDTO dto) {
        return ApiResponse.success("Tạo thành công", service.create(dto));
    }
    
    // Pattern cho user-specific data
    @GetMapping("/my")
    public ApiResponse<List<EntityDTO>> getMyData(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(null, 
            service.getByUsername(userDetails.getUsername()));
    }
}
```

### Service Pattern

```java
public interface EntityService {
    EntityDTO create(EntityDTO dto);
    EntityDTO update(Integer id, EntityDTO dto);
    void delete(Integer id);
    EntityDTO getById(Integer id);
    List<EntityDTO> getAll();
}

@Service
@RequiredArgsConstructor
public class EntityServiceImpl implements EntityService {
    
    private final EntityRepository repository;
    private final EntityMapper mapper;
    
    @Override
    @Transactional
    public EntityDTO create(EntityDTO dto) {
        // 1. Validate business rules
        if (repository.existsBySlug(dto.getSlug())) {
            throw new BusinessException("entity.slugExists", dto.getSlug());
        }
        
        // 2. Map DTO → Entity
        Entity entity = mapper.toEntity(dto);
        entity.setId(null); // Force insert
        
        // 3. Save & return
        return mapper.toDto(repository.save(entity));
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        // 1. Check exists
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("entity.notFound", id);
        }
        
        // 2. Check FK constraints
        if (childRepository.existsByParentId(id)) {
            throw new BusinessException("entity.hasChildren");
        }
        
        // 3. Delete
        repository.deleteById(id);
    }
}
```

### Entity Pattern

```java
@Data
@Entity
@Table(name = "entities")
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 🔍 Tìm Hiểu Code Nhanh

### 20 Controllers Hiện Có

| Controller | Endpoints | Mô tả |
|------------|-----------|-------|
| `AuthController` | 3 | Đăng ký, đăng nhập, đổi mật khẩu |
| `CategoryController` | 7 | CRUD danh mục (hỗ trợ cây) |
| `ColorController` | 6 | CRUD màu sắc |
| `SizeController` | 6 | CRUD kích cỡ |
| `ProductController` | 8 | CRUD sản phẩm, search, filter |
| `ProductManagementController` | 3 | Tạo/cập nhật/xem sản phẩm đầy đủ [ADMIN] ✨ |
| `ProductVariantController` | 7 | CRUD biến thể theo màu |
| `VariantStockController` | 8 | CRUD tồn kho theo size |
| `CartController` | 5 | Quản lý giỏ hàng |
| `OrderController` | 8 | Đặt hàng, xem đơn, hủy đơn, ADMIN quản lý |
| `UserAddressController` | 5 | Quản lý địa chỉ giao hàng |
| `PaymentMethodController` | 5 | Quản lý phương thức thanh toán |
| `PaymentTypeController` | 6 | CRUD loại thanh toán (COD, Bank...) |
| `ShippingMethodController` | 6 | CRUD phương thức vận chuyển |
| `OrderStatusController` | 6 | CRUD trạng thái đơn hàng |
| `ShopBankAccountController` | 6 | Tài khoản NH shop (ADMIN) |
| `FileUploadController` | 1 | Upload ảnh sản phẩm |
| `SampleDataController` | 1 | Tạo dữ liệu mẫu để test |
| `ChatController` | 2 | AI chatbot gợi ý sản phẩm |
| `ReviewController` | 5 | Đánh giá sản phẩm |

### Key Services Implemented

✅ **CartServiceImpl.java**: Logic giỏ hàng phức tạp
- `getOrCreateCart()` - tự động tạo cart cho user nếu chưa có
- `addItem()` - check tồn kho, merge quantity nếu item đã tồn tại
- `buildCartSummary()` - join 7 bảng để trả về full info (product, variant, stock, color, size, price)

✅ **OrderServiceImpl.java**: Logic đặt hàng phức tạp
- `placeOrder()` - validate cart, address, payment → trừ tồn kho → clear cart
- `generateOrderCode()` - format: `DH20260325001` (DH + date + counter)
- `generateVietQRUrl()` - tạo QR code chuyển khoản tự động
- `cancelOrder()` - chỉ hủy được PENDING, hoàn trả tồn kho

✅ **UserAddressServiceImpl.java**: Quản lý địa chỉ
- Địa chỉ đầu tiên tự động đặt làm mặc định
- `setDefault()` - chỉ 1 địa chỉ mặc định, tự động bỏ cái cũ

### Database & Repository

**Connection:** MySQL 8.0, port 3307, database `clothing_db`

**Init script:** `ec/src/main/resources/init_sql/init.sql` (21 bảng)

**Repository naming:** `{Entity}Repository extends JpaRepository<Entity, Integer>`

**Custom queries:** Dùng method naming hoặc `@Query`
```java
// Method naming
List<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId);

// Custom query
@Query("SELECT p FROM Product p WHERE p.basePrice BETWEEN :min AND :max")
List<Product> findByPriceRange(BigDecimal min, BigDecimal max);
```

---

## 🎓 Khi Thêm Tính Năng Mới

### Checklist

1. ✅ **Tạo Entity** (nếu chưa có) - thêm `@PrePersist`, `@PreUpdate`
2. ✅ **Tạo Repository** extends `JpaRepository<Entity, Integer>`
3. ✅ **Tạo DTO** với validation annotations
4. ✅ **Tạo Mapper** (MapStruct với config chuẩn)
5. ✅ **Tạo Service interface** + Impl
6. ✅ **Tạo Controller** với Swagger annotations
7. ✅ **Thêm message keys** vào `messages.properties`
8. ✅ **Test qua Swagger UI**

### Ví Dụ: Thêm Product Review

```java
// 1. Entity đã có: UserReview.java
// 2. Repository đã có: UserReviewRepository.java
// 3. DTO đã có: UserReviewDTO.java

// 4. Tạo ReviewMapper.java
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReviewMapper {
    UserReviewDTO toDto(UserReview entity);
    UserReview toEntity(UserReviewDTO dto);
    void updateEntityFromDto(UserReviewDTO dto, @MappingTarget UserReview entity);
}

// 5. Tạo ReviewService.java + ReviewServiceImpl.java
// 6. Tạo ReviewController.java
// 7. Thêm vào messages.properties:
//    review.create.success=Tạo đánh giá thành công.
//    review.notFound=Không tìm thấy đánh giá với ID: {0}
```

---

## 📦 Cấu Trúc Thư Mục Quan Trọng

```
ec/
├── src/main/java/com/utc/ec/
│   ├── config/
│   │   ├── security/
│   │   │   ├── JwtAuthenticationFilter.java   ★ Extract JWT token
│   │   │   ├── SecurityConfig.java            ★ Phân quyền endpoints
│   │   │   ├── JwtService.java                ★ Generate/validate token
│   │   │   └── CustomUserDetailsService.java
│   │   ├── AsyncConfig.java                   ★ @EnableAsync + emailTaskExecutor
│   │   ├── GlobalExceptionHandler.java        ★ Xử lý lỗi tập trung
│   │   ├── WebMvcConfig.java                  ★ CORS + static resources
│   │   ├── SwaggerConfig.java
│   │   └── MessageConfig.java                 ★ i18n setup
│   ├── controller/          (20 controllers)
│   ├── service/             (23 interfaces)
│   ├── service/impl/        (21 implementations)
│   ├── repository/          (23 repositories)
│   │   └── spec/            ★ JPA Specifications (ProductSpecification)
│   ├── entity/              (24 entities)
│   ├── dto/
│   │   ├── auth/            ★ LoginRequest, RegisterRequest, AuthResponse, PasswordRequest
│   │   ├── chat/            ★ ChatRequest, ChatResponse, ProductSuggestionDTO
│   │   └── (35+ other DTOs)
│   ├── mapper/              (6 MapStruct mappers)
│   └── exception/           (2 custom exceptions)
├── src/main/resources/
│   ├── application.yml      ★ Cấu hình DB, JWT, upload, AI (dual provider), Mail
│   ├── messages.properties  ★ Thông báo i18n
│   ├── init_sql/init.sql    ★ Schema database
│   └── templates/email/     ★ HTML email templates
│       └── order-confirmation.html
└── pom.xml                  ★ Dependencies

Thư mục gốc:
├── docker-compose.yml       ★ Local dev stack
├── .env.example             ★ Template biến môi trường
├── .github/workflows/ci-cd.yml ★ CI/CD Pipeline (build, test, Docker)
├── AGENTS.md               (file này)
├── README.md
├── PRODUCT_FLOW.md          ★ Hướng dẫn tạo sản phẩm từng bước
├── FRONTEND_API_GUIDE.md    ★ API reference cho FE (có hướng dẫn Chat AI)
├── DATABASE_ANALYSIS.md     ★ Phân tích schema & FK
├── PAYMENT_FLOW.md          ★ Luồng thanh toán chi tiết
└── *_USECASE.md             ★ Use case diagrams (Auth, Cart, Order, Review)
```

---

## 🚨 Lưu Ý Đặc Biệt

### 1. Luồng Đặt Hàng Tự Động

**OrderServiceImpl.placeOrder()** thực hiện tuần tự:
1. Validate địa chỉ giao hàng thuộc về user
2. Validate phương thức thanh toán, vận chuyển
3. Lấy giỏ hàng, check không rỗng
4. Validate tồn kho cho TẤT CẢ items (fail → rollback)
5. Tính tổng tiền (subtotal + shipping fee)
6. Tạo ShopOrder với status PENDING
7. Tạo OrderLine cho mỗi item trong giỏ
8. **TRỪ TỒN KHO** cho mỗi VariantStock
9. **XÓA SẠCH GIỎ HÀNG**
10. Generate VietQR URL cho bank transfer
11. Build `OrderDetailDTO` response
12. **GỬI EMAIL XÁC NHẬN** (async, không block response)

**⚠️ Toàn bộ dùng `@Transactional` - rollback nếu bất kỳ bước nào fail!**

### 2. Giá Sản Phẩm (Price Resolution)

**Thứ tự ưu tiên:**
1. `VariantStock.priceOverride` (nếu != null) → dùng giá này
2. Nếu null → dùng `Product.basePrice`

**Ví dụ:**
- Product: "Áo Nike", basePrice = 250.000đ
- Variant: Màu Đỏ, Size S → priceOverride = null → giá = 250.000đ
- Variant: Màu Xanh, Size M → priceOverride = 300.000đ → giá = 300.000đ (limited edition)

### 3. Slug Phải Unique

**Tất cả entity có slug PHẢI validate unique:**
```java
// Create
if (repository.existsBySlug(dto.getSlug())) {
    throw new BusinessException("entity.slugExists", dto.getSlug());
}

// Update
if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())
        && repository.existsBySlugAndIdNot(dto.getSlug(), id)) {
    throw new BusinessException("entity.slugExists", dto.getSlug());
}
```

### 4. VietQR Integration (Orders)

**Khi đặt hàng với Bank Transfer:**
```java
String qrUrl = generateVietQRUrl(shopBankAccount, order, orderTotal);
// → "https://img.vietqr.io/image/MB-0123456789-compact2.png?amount=500000&addInfo=DH20260325001&accountName=SHOP"
```

User quét QR → tự động điền số tiền + nội dung chuyển khoản (order code).

### 5. AI Chatbot — Kiến Trúc & Lưu Ý

**Files liên quan:**
```
dto/chat/ChatRequest.java          ← Input: message + sessionId + productId
dto/chat/ChatResponse.java         ← Output: message + sessionId + suggestions[]
dto/chat/ProductSuggestionDTO.java ← Gợi ý sản phẩm kèm thumbnail
service/ChatService.java           ← Interface
service/impl/ChatServiceImpl.java  ← Logic chính
controller/ChatController.java     ← 2 endpoints
repository/spec/ProductSpecification.java ← JPA Specification cho search
```

**Luồng xử lý trong `ChatServiceImpl.chat()`:**
```
1. cleanupOldSessions()             — dọn session hết hạn (TTL 2h)
2. resolveSessionId()               — lấy/tạo UUID session
3. searchRelevantProducts()         — query DB tìm sản phẩm liên quan (JPA Specification)
4. buildCategoryMap()               — map categoryId → categoryName
5. buildThumbnailMap()              — map productId → thumbnail URL
6. Thêm user message vào history
7. buildSystemPrompt()              — tạo prompt với danh sách sản phẩm thực
8. callAI()                         — GitHub Models → Claude fallback (circuit breaker)
9. Lưu AI response vào history      — trimHistory() nếu > 20 tin nhắn
10. buildSuggestions()              — trả về top 5 sản phẩm gợi ý
```

**Dual AI Provider — Cấu hình** (`application.yml` / env vars):
```yaml
# Primary: GitHub Models (miễn phí, dùng GitHub PAT token)
ai:
  provider: ${AI_PROVIDER:github}       # "github" hoặc "claude"
  api:
    key: ${AI_API_KEY:}                 # GitHub PAT token
    model: ${AI_MODEL:gpt-4o-mini}      # gpt-4o-mini: 150 req/day miễn phí
    max-tokens: ${AI_MAX_TOKENS:1024}

# Fallback: Claude (cần credits Anthropic)
claude:
  api:
    key: ${CLAUDE_API_KEY:}
    model: ${CLAUDE_MODEL:claude-3-haiku-20240307}
    max-tokens: ${CLAUDE_MAX_TOKENS:1024}
```

**Circuit breaker:** Khi GitHub Models trả 429 (quota exhausted), tự chuyển sang Claude trong 1 giờ.

**Fallback khi không có API key:**
- Bot vẫn trả về danh sách sản phẩm gợi ý từ DB
- Message fallback: "Xin chào! Bạn có thể xem sản phẩm bên dưới..."

**⚠️ Session lưu in-memory (ConcurrentHashMap) — không persist qua restart!**

---

## 📚 Tài Liệu Tham Khảo

| File | Mục đích |
|------|----------|
| `README.md` | Hướng dẫn setup, quick start, tech stack |
| `PRODUCT_FLOW.md` | Chi tiết luồng tạo sản phẩm 6 bước (★ ĐỌC TRƯỚC KHI SỬA PRODUCT) |
| `FRONTEND_API_GUIDE.md` | API reference đầy đủ với request/response examples |
| `DATABASE_ANALYSIS.md` | Schema, relationships, constraints |
| `PROJECT_SUMMARY.md` | Tổng quan tech stack, dependencies, deployment |
| `PAYMENT_FLOW.md` | Luồng thanh toán chi tiết (VietQR, bank transfer) |

**Swagger UI:** `http://localhost:8080/swagger-ui.html` - Test API trực quan

---

## 💡 Tips Khi Code

1. **Đọc message keys có sẵn** trong `messages.properties` trước khi tạo mới
2. **Check FK constraints** trước khi xóa - tham khảo các service đã có
3. **Dùng `@Transactional`** cho methods thay đổi nhiều bảng (cart, order)
4. **Test bằng Swagger** sau khi implement - có sẵn examples
5. **Follow naming convention:** `entity.action.outcome` cho message keys
6. **Luôn validate business rules** trước khi save database
7. **Dùng `@AuthenticationPrincipal`** thay vì tự parse JWT token
8. **Pagination:** Dùng `Pageable` + `PagedResponse<T>` cho list lớn

