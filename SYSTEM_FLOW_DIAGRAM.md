# 🎨 SƠ ĐỒ LUỒNG HỆ THỐNG - CLOTHING STORE

> Các sơ đồ trực quan hóa luồng hoạt động của hệ thống

---

## 📊 SƠ ĐỒ TỔNG QUAN KIẾN TRÚC

```mermaid
graph TB
    subgraph "CLIENT LAYER"
        WEB[Web Browser]
        MOBILE[Mobile App]
        ADMIN[Admin Panel]
    end

    subgraph "API GATEWAY"
        NGINX[Nginx / API Gateway]
    end

    subgraph "APPLICATION LAYER"
        AUTH[Authentication<br/>JWT Filter]
        CTRL[Controllers<br/>8 REST APIs]
        
        subgraph "Business Services"
            PRODUCT[Product Service]
            CART[Cart Service<br/>❌ MISSING]
            ORDER[Order Service<br/>❌ MISSING]
            USER[User Service<br/>❌ MISSING]
            PAY[Payment Service<br/>❌ MISSING]
        end
    end

    subgraph "DATA LAYER"
        REPO[Repositories<br/>JPA/Hibernate]
        MYSQL[(MySQL Database<br/>23 Tables)]
    end

    subgraph "EXTERNAL SERVICES"
        STORAGE[File Storage<br/>Images]
        PAYMENT[Payment Gateway<br/>VNPay/MoMo<br/>❌ NOT INTEGRATED]
        EMAIL[Email Service<br/>❌ MISSING]
    end

    WEB --> NGINX
    MOBILE --> NGINX
    ADMIN --> NGINX
    
    NGINX --> AUTH
    AUTH --> CTRL
    
    CTRL --> PRODUCT
    CTRL --> CART
    CTRL --> ORDER
    CTRL --> USER
    CTRL --> PAY
    
    PRODUCT --> REPO
    CART --> REPO
    ORDER --> REPO
    USER --> REPO
    PAY --> REPO
    
    REPO --> MYSQL
    
    CTRL --> STORAGE
    PAY --> PAYMENT
    ORDER --> EMAIL

    style CART fill:#ff9999
    style ORDER fill:#ff9999
    style USER fill:#ff9999
    style PAY fill:#ff9999
    style PAYMENT fill:#ff9999
    style EMAIL fill:#ff9999
```

---

## 🔐 LUỒNG AUTHENTICATION

```mermaid
sequenceDiagram
    participant User
    participant Controller as AuthController
    participant Service as AuthService
    participant Security as Spring Security
    participant JWT as JwtService
    participant DB as Database

    Note over User,DB: ĐĂNG KÝ (Registration)
    User->>Controller: POST /api/auth/register<br/>{username, email, password}
    Controller->>Service: register(request)
    Service->>Security: BCrypt.encode(password)
    Security-->>Service: hashedPassword
    Service->>DB: INSERT site_user<br/>(role=USER)
    DB-->>Service: userId
    Service->>JWT: generateToken(userId, username, role)
    JWT-->>Service: accessToken
    Service-->>Controller: AuthResponse{token}
    Controller-->>User: 201 Created<br/>{accessToken, tokenType}

    Note over User,DB: ĐĂNG NHẬP (Login)
    User->>Controller: POST /api/auth/login<br/>{username, password}
    Controller->>Service: login(request)
    Service->>Security: authenticate(username, password)
    Security->>DB: SELECT * FROM site_user<br/>WHERE username=?
    DB-->>Security: UserDetails
    Security->>Security: passwordEncoder.matches()
    Security-->>Service: Authentication
    Service->>JWT: generateToken(user)
    JWT-->>Service: accessToken
    Service-->>Controller: AuthResponse{token}
    Controller-->>User: 200 OK<br/>{accessToken}

    Note over User,DB: SỬ DỤNG API (Protected Endpoint)
    User->>Controller: GET /api/products<br/>Authorization: Bearer {token}
    Controller->>Security: JwtAuthenticationFilter
    Security->>JWT: validateToken(token)
    JWT-->>Security: valid=true, userId=123
    Security->>DB: loadUserByUsername(userId)
    DB-->>Security: UserDetails
    Security->>Security: setSecurityContext(user)
    Security-->>Controller: proceed with userId
    Controller-->>User: 200 OK with data
```

---

## 🛍️ LUỒNG TẠO SẢN PHẨM (Product Creation Flow)

```mermaid
graph TD
    START([Bắt đầu tạo sản phẩm]) --> AUTH{Đã đăng nhập?}
    AUTH -->|No| LOGIN[POST /api/auth/login]
    LOGIN --> AUTH
    AUTH -->|Yes, JWT valid| STEP1

    STEP1[1️⃣ Tạo Category<br/>POST /api/product-categories<br/>{categoryName, parentCategoryId}]
    STEP1 --> CHECK1{Category<br/>tồn tại?}
    CHECK1 -->|No| STEP1
    CHECK1 -->|Yes| STEP2

    STEP2[2️⃣ Tạo Variation<br/>POST /api/variations<br/>{name, categoryId}]
    STEP2 --> STEP3

    STEP3[3️⃣ Tạo Variation Options<br/>POST /api/variation-options<br/>{variationId, value}<br/>Lặp nhiều lần]
    STEP3 --> STEP4

    STEP4[4️⃣ Upload ảnh sản phẩm<br/>POST /api/files/upload<br/>Multipart file]
    STEP4 --> IMG_URL[Nhận URL: /uploads/images/xxx.jpg]
    IMG_URL --> STEP5

    STEP5[5️⃣ Tạo Product<br/>POST /api/products<br/>{name, categoryId, description, productImage}]
    STEP5 --> CHECK5{Validation<br/>OK?}
    CHECK5 -->|categoryId không tồn tại| ERROR1[❌ 404 Not Found]
    CHECK5 -->|OK| STEP6

    STEP6[6️⃣ Tạo Product Item<br/>POST /api/product-items<br/>{productId, sku, price, qtyInStock}]
    STEP6 --> LOOP{Có nhiều<br/>biến thể?}
    LOOP -->|Yes| STEP6
    LOOP -->|No| STEP7

    STEP7[7️⃣ Gắn Configuration<br/>POST /api/product-configurations<br/>{productItemId, variationOptionId}]
    STEP7 --> LOOP2{Mỗi item<br/>có nhiều options?}
    LOOP2 -->|Yes| STEP7
    LOOP2 -->|No| DONE

    DONE([✅ Sản phẩm hoàn chỉnh])

    style AUTH fill:#87CEEB
    style CHECK1 fill:#FFD700
    style CHECK5 fill:#FFD700
    style ERROR1 fill:#FF6B6B
    style DONE fill:#90EE90
```

---

## 🛒 LUỒNG MUA HÀNG (E-commerce Flow) - ⚠️ CHƯA TRIỂN KHAI

```mermaid
sequenceDiagram
    participant User
    participant ProductAPI as Product API<br/>✅ Available
    participant CartAPI as Cart API<br/>❌ Missing
    participant OrderAPI as Order API<br/>❌ Missing
    participant PaymentAPI as Payment API<br/>❌ Missing
    participant DB as Database

    Note over User,DB: PHASE 1: Duyệt sản phẩm
    User->>ProductAPI: GET /api/products/search<br/>?keyword=áo thun&categoryId=2
    ProductAPI->>DB: SELECT products...
    DB-->>ProductAPI: List<Product>
    ProductAPI-->>User: 200 OK với danh sách

    User->>ProductAPI: GET /api/products/100
    ProductAPI->>DB: SELECT product, product_items...
    DB-->>ProductAPI: Product + biến thể
    ProductAPI-->>User: Chi tiết sản phẩm

    Note over User,DB: PHASE 2: Thêm vào giỏ ❌ MISSING
    User->>CartAPI: POST /api/shopping-cart/items<br/>{productItemId:1001, qty:2}
    CartAPI->>DB: SELECT qty_in_stock FROM product_item<br/>WHERE id=1001
    DB-->>CartAPI: qtyInStock=50
    CartAPI->>CartAPI: Validate: 2 <= 50 ✅
    CartAPI->>DB: INSERT/UPDATE shopping_cart_item
    DB-->>CartAPI: Success
    CartAPI-->>User: 201 Created

    User->>CartAPI: GET /api/shopping-cart
    CartAPI->>DB: SELECT cart_items JOIN product_items...
    DB-->>CartAPI: Cart items với price
    CartAPI->>CartAPI: Calculate subtotal
    CartAPI-->>User: {items[], subtotal:300000}

    Note over User,DB: PHASE 3: Thanh toán ❌ MISSING
    User->>OrderAPI: POST /api/orders/checkout<br/>{addressId, shippingMethodId, items}
    OrderAPI->>DB: BEGIN TRANSACTION
    OrderAPI->>DB: SELECT product_item FOR UPDATE<br/>(Lock row)
    DB-->>OrderAPI: productItem
    OrderAPI->>OrderAPI: Validate stock
    OrderAPI->>DB: UPDATE product_item<br/>SET qty_in_stock -= 2
    OrderAPI->>DB: INSERT shop_order<br/>(status=PENDING)
    DB-->>OrderAPI: orderId=500
    OrderAPI->>DB: INSERT order_line
    OrderAPI->>DB: DELETE shopping_cart_item
    OrderAPI->>DB: COMMIT
    DB-->>OrderAPI: Success
    OrderAPI-->>User: {orderId:500, total:320000}

    Note over User,DB: PHASE 4: Xử lý thanh toán ❌ MISSING
    User->>PaymentAPI: POST /api/payments/process<br/>{orderId:500, method:VNPAY}
    PaymentAPI->>PaymentAPI: Generate payment URL
    PaymentAPI-->>User: {paymentUrl}
    User->>PaymentAPI: [External] Payment Gateway callback
    PaymentAPI->>DB: UPDATE shop_order<br/>SET order_status=CONFIRMED
    PaymentAPI-->>User: Redirect đến success page

    Note over User,DB: PHASE 5: Theo dõi đơn hàng ❌ MISSING
    User->>OrderAPI: GET /api/orders/500
    OrderAPI->>DB: SELECT order JOIN order_lines...
    DB-->>OrderAPI: Order details
    OrderAPI-->>User: {order, items, status}

    rect rgb(255, 200, 200)
        Note right of CartAPI: ❌ Chưa có Controller<br/>❌ Chưa có Service
        Note right of OrderAPI: ❌ Chưa có Controller<br/>❌ Chưa có Service
        Note right of PaymentAPI: ❌ Chưa có Controller<br/>❌ Chưa có Service
    end
```

---

## 🗄️ SƠ ĐỒ DATABASE RELATIONSHIPS

```mermaid
erDiagram
    site_user ||--o{ user_address : has
    site_user ||--o{ shopping_cart : owns
    site_user ||--o{ shop_order : places
    site_user ||--o{ user_payment_method : has
    site_user ||--o{ user_review : writes

    address ||--o{ user_address : "used in"
    address }o--|| country : "belongs to"
    address ||--o{ shop_order : "ships to"

    product_category ||--o{ product_category : "parent of"
    product_category ||--o{ product : contains
    product_category ||--o{ variation : "defines"
    product_category ||--o{ promotion_category : "has"

    product ||--o{ product_item : "has variants"
    product_item ||--o{ shopping_cart_item : "in cart"
    product_item ||--o{ order_line : "in order"
    product_item ||--o{ product_configuration : "configured by"

    variation ||--o{ variation_option : "has options"
    variation_option ||--o{ product_configuration : "defines"

    shopping_cart ||--o{ shopping_cart_item : contains

    shop_order ||--o{ order_line : contains
    shop_order }o--|| order_status : has
    shop_order }o--|| shipping_method : uses
    shop_order }o--|| user_payment_method : "paid with"

    order_line ||--o{ user_review : "reviewed in"

    promotion ||--o{ promotion_category : "applies to"
    payment_type ||--o{ user_payment_method : "type of"

    site_user {
        int id PK
        string username UK
        string email UK
        string password
        enum role
    }

    product {
        int id PK
        int category_id FK
        string name
        text description
        string product_image
    }

    product_item {
        int id PK
        int product_id FK
        string sku UK
        int qty_in_stock
        int price
        string product_image
    }

    shop_order {
        int id PK
        int user_id FK
        datetime order_date
        int payment_method_id FK
        int shipping_address FK
        int shipping_method FK
        int order_total
        int order_status FK
    }

    order_line {
        int id PK
        int product_item_id FK
        int order_id FK
        int qty
        int price
    }

    shopping_cart {
        int id PK
        int user_id FK
    }

    shopping_cart_item {
        int id PK
        int cart_id FK
        int product_item_id FK
        int qty
    }
```

---

## 📦 SƠ ĐỒ DATA FLOW - PRODUCT MANAGEMENT

```mermaid
graph LR
    subgraph "INPUT"
        ADMIN[Admin User]
        DATA[Product Data<br/>JSON]
        IMAGE[Image Files<br/>JPG/PNG]
    end

    subgraph "API LAYER"
        AUTH_FILTER[JWT Filter<br/>Validate Token]
        CTRL[ProductController<br/>@PostMapping]
        VALID[Bean Validation<br/>@Valid]
    end

    subgraph "BUSINESS LAYER"
        SVC[ProductServiceImpl]
        
        subgraph "Validations"
            V1[Check categoryId exists]
            V2[Check SKU unique]
            V3[Check price > 0]
        end
        
        MAPPER[Entity ↔ DTO<br/>BeanUtils.copy]
    end

    subgraph "DATA LAYER"
        REPO[ProductRepository<br/>JpaRepository]
        CACHE[❌ Cache<br/>Redis]
        DB[(MySQL)]
    end

    subgraph "OUTPUT"
        RESPONSE[ApiResponse<br/>{success, data, message}]
        STORAGE[File System<br/>/uploads/images/]
    end

    ADMIN --> DATA
    ADMIN --> IMAGE
    
    DATA --> AUTH_FILTER
    IMAGE --> AUTH_FILTER
    
    AUTH_FILTER --> CTRL
    CTRL --> VALID
    VALID --> SVC
    
    SVC --> V1
    SVC --> V2
    SVC --> V3
    
    V1 --> MAPPER
    V2 --> MAPPER
    V3 --> MAPPER
    
    MAPPER --> REPO
    REPO --> CACHE
    CACHE -.->|Not implemented| DB
    REPO --> DB
    
    DB --> REPO
    REPO --> MAPPER
    MAPPER --> RESPONSE
    
    IMAGE --> STORAGE
    STORAGE --> RESPONSE
    
    RESPONSE --> ADMIN

    style CACHE fill:#ff9999,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
```

---

## 🔄 STATE DIAGRAM - ORDER STATUS

```mermaid
stateDiagram-v2
    [*] --> PENDING: User checkout

    PENDING --> CONFIRMED: Admin confirms<br/>Payment received
    PENDING --> CANCELLED: User cancels<br/>Payment failed
    PENDING --> EXPIRED: Auto cancel<br/>after 24h

    CONFIRMED --> PROCESSING: Start preparing
    CONFIRMED --> CANCELLED: Admin cancels<br/>(Out of stock)

    PROCESSING --> SHIPPED: Handover to shipper
    PROCESSING --> CANCELLED: Cannot fulfill

    SHIPPED --> DELIVERED: Customer received
    SHIPPED --> RETURNED: Delivery failed

    DELIVERED --> COMPLETED: After 7 days<br/>(No return)
    DELIVERED --> RETURNED: Customer requests return

    RETURNED --> REFUNDED: Refund processed

    CANCELLED --> [*]
    EXPIRED --> [*]
    COMPLETED --> [*]
    REFUNDED --> [*]

    note right of PENDING
        ⏱️ Chờ thanh toán
        ❌ User có thể hủy
    end note

    note right of CONFIRMED
        💰 Đã thanh toán
        📦 Chuẩn bị hàng
    end note

    note right of SHIPPED
        🚚 Đang giao
        📍 Track shipment
    end note

    note right of DELIVERED
        ✅ Đã nhận hàng
        ⭐ Có thể review
    end note
```

---

## 🏗️ COMPONENT DIAGRAM - BACKEND STRUCTURE

```mermaid
graph TB
    subgraph "com.utc.ec"
        APP[EcApplication.java<br/>@SpringBootApplication]
        
        subgraph "config"
            SEC[SecurityConfig]
            JWT_FILTER[JwtAuthenticationFilter]
            SWAGGER[SwaggerConfig]
            EXCEPTION[GlobalExceptionHandler]
        end
        
        subgraph "controller - 8 controllers"
            AUTH_CTRL[AuthController<br/>✅ /api/auth/*]
            PROD_CTRL[ProductController<br/>✅ /api/products/*]
            ITEM_CTRL[ProductItemController<br/>✅ /api/product-items/*]
            CAT_CTRL[ProductCategoryController<br/>✅ /api/product-categories/*]
            VAR_CTRL[VariationController<br/>✅ /api/variations/*]
            OPT_CTRL[VariationOptionController<br/>✅ /api/variation-options/*]
            CONF_CTRL[ProductConfigurationController<br/>✅ /api/product-configurations/*]
            FILE_CTRL[FileUploadController<br/>✅ /api/files/*]
            
            CART_CTRL[❌ ShoppingCartController<br/>MISSING]
            ORDER_CTRL[❌ OrderController<br/>MISSING]
            USER_CTRL[❌ UserController<br/>MISSING]
        end
        
        subgraph "service + impl"
            AUTH_SVC[AuthService<br/>Login/Register/JWT]
            PROD_SVC[ProductService<br/>CRUD + Search]
            CAT_SVC[CategoryService<br/>Tree structure]
            VAR_SVC[VariationService]
            FILE_SVC[FileStorageService]
            
            CART_SVC[❌ CartService<br/>MISSING]
            ORDER_SVC[❌ OrderService<br/>MISSING]
        end
        
        subgraph "repository - 21 repos"
            REPO[JpaRepository<br/>Spring Data JPA]
            PROD_REPO[ProductRepository]
            USER_REPO[SiteUserRepository]
            ORDER_REPO[ShopOrderRepository]
            CART_REPO[ShoppingCartRepository]
        end
        
        subgraph "entity - 23 entities"
            ENT[JPA Entities<br/>@Entity @Table]
            USER_ENT[SiteUser]
            PROD_ENT[Product]
            ITEM_ENT[ProductItem]
            ORDER_ENT[ShopOrder]
        end
        
        subgraph "dto - 25 DTOs"
            DTO[Data Transfer Objects]
            API_RESP[ApiResponse<T>]
            PAGED_RESP[PagedResponse<T>]
        end
        
        subgraph "exception"
            EX1[ResourceNotFoundException]
            EX2[BusinessException]
        end
    end
    
    APP --> SEC
    APP --> SWAGGER
    
    SEC --> JWT_FILTER
    
    AUTH_CTRL --> AUTH_SVC
    PROD_CTRL --> PROD_SVC
    CAT_CTRL --> CAT_SVC
    VAR_CTRL --> VAR_SVC
    FILE_CTRL --> FILE_SVC
    
    CART_CTRL -.->|Not implemented| CART_SVC
    ORDER_CTRL -.->|Not implemented| ORDER_SVC
    USER_CTRL -.->|Not implemented| AUTH_SVC
    
    AUTH_SVC --> USER_REPO
    PROD_SVC --> PROD_REPO
    ORDER_SVC -.-> ORDER_REPO
    CART_SVC -.-> CART_REPO
    
    PROD_REPO --> PROD_ENT
    USER_REPO --> USER_ENT
    ORDER_REPO --> ORDER_ENT
    
    PROD_CTRL --> API_RESP
    PROD_SVC --> PAGED_RESP
    
    PROD_SVC --> EX1
    PROD_SVC --> EX2
    EXCEPTION --> EX1
    EXCEPTION --> EX2

    style CART_CTRL fill:#ffcccc
    style ORDER_CTRL fill:#ffcccc
    style USER_CTRL fill:#ffcccc
    style CART_SVC fill:#ffcccc
    style ORDER_SVC fill:#ffcccc
```

---

## 🎯 DEPLOYMENT DIAGRAM

```mermaid
graph TB
    subgraph "CLIENT"
        BROWSER[Web Browser]
        MOBILE_APP[Mobile App]
    end

    subgraph "LOAD BALANCER"
        LB[Nginx / ALB]
    end

    subgraph "APPLICATION SERVERS"
        subgraph "Container 1"
            APP1[Spring Boot App<br/>Port 8080]
        end
        subgraph "Container 2"
            APP2[Spring Boot App<br/>Port 8080]
        end
        subgraph "Container 3"
            APP3[Spring Boot App<br/>Port 8080]
        end
    end

    subgraph "DATA LAYER"
        subgraph "Primary"
            DB_MASTER[(MySQL Master<br/>Read/Write)]
        end
        subgraph "Replicas"
            DB_SLAVE1[(MySQL Slave 1<br/>Read Only)]
            DB_SLAVE2[(MySQL Slave 2<br/>Read Only)]
        end
    end

    subgraph "CACHE LAYER - ❌ NOT IMPLEMENTED"
        REDIS[(Redis Cluster)]
    end

    subgraph "FILE STORAGE"
        LOCAL[Local Disk<br/>/uploads/]
        S3[❌ AWS S3<br/>NOT CONFIGURED]
    end

    subgraph "EXTERNAL SERVICES"
        PAYMENT_GW[Payment Gateway<br/>❌ NOT INTEGRATED]
        EMAIL_SVC[Email Service<br/>❌ NOT INTEGRATED]
        SMS_SVC[SMS Service<br/>❌ NOT INTEGRATED]
    end

    BROWSER --> LB
    MOBILE_APP --> LB

    LB --> APP1
    LB --> APP2
    LB --> APP3

    APP1 --> DB_MASTER
    APP2 --> DB_MASTER
    APP3 --> DB_MASTER

    APP1 -.->|Reads| DB_SLAVE1
    APP2 -.->|Reads| DB_SLAVE1
    APP3 -.->|Reads| DB_SLAVE2

    DB_MASTER -.->|Replication| DB_SLAVE1
    DB_MASTER -.->|Replication| DB_SLAVE2

    APP1 -.->|Should use| REDIS
    APP2 -.->|Should use| REDIS
    APP3 -.->|Should use| REDIS

    APP1 --> LOCAL
    APP2 --> LOCAL
    APP3 --> LOCAL

    APP1 -.->|Should use| S3
    APP2 -.->|Should use| S3
    APP3 -.->|Should use| S3

    APP1 -.->|When checkout| PAYMENT_GW
    APP1 -.->|When order confirmed| EMAIL_SVC
    APP1 -.->|OTP verification| SMS_SVC

    style REDIS fill:#ffcccc,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
    style S3 fill:#ffcccc,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
    style PAYMENT_GW fill:#ffcccc,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
    style EMAIL_SVC fill:#ffcccc,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
    style SMS_SVC fill:#ffcccc,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5
```

---

## 📱 USE CASE DIAGRAM

```mermaid
graph TB
    subgraph ACTORS
        GUEST[Guest<br/>Khách vãng lai]
        USER[Registered User<br/>Khách hàng]
        ADMIN[Admin<br/>Quản trị viên]
    end

    subgraph "PUBLIC USE CASES"
        UC1[Browse Products<br/>✅ Implemented]
        UC2[Search Products<br/>✅ Implemented]
        UC3[View Product Details<br/>✅ Implemented]
        UC4[Register Account<br/>✅ Implemented]
        UC5[Login<br/>✅ Implemented]
    end

    subgraph "USER USE CASES"
        UC6[Add to Cart<br/>❌ Missing]
        UC7[View Cart<br/>❌ Missing]
        UC8[Checkout<br/>❌ Missing]
        UC9[Make Payment<br/>❌ Missing]
        UC10[View Orders<br/>❌ Missing]
        UC11[Cancel Order<br/>❌ Missing]
        UC12[Write Review<br/>❌ Missing]
        UC13[Manage Addresses<br/>❌ Missing]
        UC14[Update Profile<br/>❌ Missing]
    end

    subgraph "ADMIN USE CASES"
        UC15[Manage Products<br/>✅ Implemented]
        UC16[Manage Categories<br/>✅ Implemented]
        UC17[Manage Variations<br/>✅ Implemented]
        UC18[Upload Images<br/>✅ Implemented]
        UC19[Manage Orders<br/>❌ Missing]
        UC20[Update Order Status<br/>❌ Missing]
        UC21[Manage Promotions<br/>❌ Missing]
        UC22[View Analytics<br/>❌ Missing]
        UC23[Manage Users<br/>❌ Missing]
        UC24[Manage Inventory<br/>❌ Missing]
    end

    GUEST --> UC1
    GUEST --> UC2
    GUEST --> UC3
    GUEST --> UC4
    GUEST --> UC5

    USER --> UC1
    USER --> UC2
    USER --> UC3
    USER --> UC6
    USER --> UC7
    USER --> UC8
    USER --> UC9
    USER --> UC10
    USER --> UC11
    USER --> UC12
    USER --> UC13
    USER --> UC14

    ADMIN --> UC15
    ADMIN --> UC16
    ADMIN --> UC17
    ADMIN --> UC18
    ADMIN --> UC19
    ADMIN --> UC20
    ADMIN --> UC21
    ADMIN --> UC22
    ADMIN --> UC23
    ADMIN --> UC24

    style UC1 fill:#90EE90
    style UC2 fill:#90EE90
    style UC3 fill:#90EE90
    style UC4 fill:#90EE90
    style UC5 fill:#90EE90
    style UC15 fill:#90EE90
    style UC16 fill:#90EE90
    style UC17 fill:#90EE90
    style UC18 fill:#90EE90

    style UC6 fill:#FF6B6B
    style UC7 fill:#FF6B6B
    style UC8 fill:#FF6B6B
    style UC9 fill:#FF6B6B
    style UC10 fill:#FF6B6B
    style UC11 fill:#FF6B6B
    style UC12 fill:#FF6B6B
    style UC13 fill:#FF6B6B
    style UC14 fill:#FF6B6B
    style UC19 fill:#FF6B6B
    style UC20 fill:#FF6B6B
    style UC21 fill:#FF6B6B
    style UC22 fill:#FF6B6B
    style UC23 fill:#FF6B6B
    style UC24 fill:#FF6B6B
```

---

## ✅ API COVERAGE MAP

```mermaid
pie title API Implementation Status
    "Implemented" : 40
    "Missing - High Priority" : 30
    "Missing - Medium Priority" : 20
    "Missing - Low Priority" : 10
```

---

**Legend:**
- ✅ Implemented (Green)
- ❌ Missing (Red)
- ⚠️ Partial (Yellow)

**Tài liệu này được tạo bởi GitHub Copilot - March 17, 2026**

