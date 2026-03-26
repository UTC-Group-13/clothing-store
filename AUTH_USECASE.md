# 🔐 Use Case: Đăng Ký & Đăng Nhập

> Xem file `AUTH_USECASE.drawio` để import vào [draw.io](https://draw.io)

---

## 📌 Actors

| Actor | Mô tả |
|-------|--------|
| **Guest (Khách)** | Người dùng chưa đăng nhập |
| **User (Thành viên)** | Người dùng đã đăng nhập, có JWT token |
| **System (Hệ thống)** | Spring Boot Backend |
| **Database** | MySQL — bảng `site_users` |

---

## 📋 Use Cases Tổng Quan

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Hệ Thống Auth                                │
│                                                                     │
│  Guest ──────►  UC1: Đăng ký tài khoản (Register)                  │
│                                                                     │
│  Guest ──────►  UC2: Đăng nhập (Login)                             │
│                                                                     │
│  User  ──────►  UC3: Đổi mật khẩu (Change Password)               │
│                                                                     │
│  System ─────►  UC4: Xác thực JWT token (JWT Filter - tự động)     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Luồng Chi Tiết

### UC1 — Đăng Ký (Register)

```mermaid
sequenceDiagram
    actor Guest as 👤 Guest (Client)
    participant API as 🌐 AuthController<br/>/api/auth/register
    participant SVC as ⚙️ AuthServiceImpl
    participant DB as 🗄️ SiteUserRepository<br/>(MySQL)
    participant JWT as 🔑 JwtService

    Guest->>API: POST /api/auth/register<br/>{ username, email, password, phone }

    API->>API: @Valid — Validate input

    alt Validation thất bại
        API-->>Guest: 400 Bad Request<br/>{ errors }
    end

    API->>SVC: register(RegisterRequest)

    SVC->>DB: existsByUsername(username)
    DB-->>SVC: true / false

    alt Username đã tồn tại
        SVC-->>API: throw BusinessException("auth.username.exists")
        API-->>Guest: 400 { "Username đã tồn tại" }
    end

    SVC->>DB: existsByEmailAddress(email)
    DB-->>SVC: true / false

    alt Email đã tồn tại
        SVC-->>API: throw BusinessException("auth.email.exists")
        API-->>Guest: 400 { "Email đã tồn tại" }
    end

    SVC->>SVC: BCrypt.encode(password)
    SVC->>SVC: user.setRole(USER)
    SVC->>DB: save(SiteUser)
    DB-->>SVC: SiteUser saved

    SVC->>JWT: generateToken(userDetails)
    JWT-->>SVC: JWT token (24h)

    SVC-->>API: AuthResponse { token, userId, username, role }
    API-->>Guest: 201 Created<br/>{ success: true, data: { accessToken, username, role } }
```

---

### UC2 — Đăng Nhập (Login)

```mermaid
sequenceDiagram
    actor Guest as 👤 Guest (Client)
    participant API as 🌐 AuthController<br/>/api/auth/login
    participant SVC as ⚙️ AuthServiceImpl
    participant AUTH as 🔒 AuthenticationManager<br/>(Spring Security)
    participant UDS as 📋 CustomUserDetailsService
    participant DB as 🗄️ SiteUserRepository
    participant JWT as 🔑 JwtService

    Guest->>API: POST /api/auth/login<br/>{ username, password }

    API->>API: @Valid — Validate input

    API->>SVC: login(LoginRequest)

    SVC->>AUTH: authenticate(UsernamePasswordAuthenticationToken)

    AUTH->>UDS: loadUserByUsername(username)
    UDS->>DB: findByUsername(username)
    DB-->>UDS: SiteUser entity
    UDS-->>AUTH: UserDetails

    AUTH->>AUTH: BCrypt.matches(password, hashedPassword)

    alt Sai username hoặc password
        AUTH-->>SVC: throw BadCredentialsException
        SVC-->>API: 401 Unauthorized<br/>{ "Sai tài khoản hoặc mật khẩu" }
    end

    SVC->>DB: findByUsername(username)
    DB-->>SVC: SiteUser

    SVC->>JWT: generateToken(userDetails)
    JWT-->>SVC: JWT token (exp: 24h)

    SVC-->>API: AuthResponse
    API-->>Guest: 200 OK<br/>{ accessToken, tokenType: "Bearer", userId, username, role }
```

---

### UC3 — Đổi Mật Khẩu (Change Password)

```mermaid
sequenceDiagram
    actor User as 👤 User (đã đăng nhập)
    participant API as 🌐 AuthController<br/>/api/auth/change-password
    participant SVC as ⚙️ AuthServiceImpl
    participant DB as 🗄️ SiteUserRepository

    User->>API: POST /api/auth/change-password<br/>{ userId, oldPassword, newPassword, verifyPassword }

    API->>SVC: changePassword(PasswordRequest)

    SVC->>DB: findById(userId)
    DB-->>SVC: SiteUser

    alt User không tồn tại
        SVC-->>API: throw BusinessException("auth.user.notFound")
        API-->>User: 404 Not Found
    end

    SVC->>SVC: BCrypt.matches(oldPassword, storedHash)

    alt Mật khẩu cũ sai
        SVC-->>API: throw IllegalArgumentException("pass.old.incorrect")
        API-->>User: 400 Bad Request
    end

    SVC->>SVC: newPassword == verifyPassword?

    alt Xác nhận mật khẩu không khớp
        SVC-->>API: throw IllegalArgumentException("pass.old.verify")
        API-->>User: 400 Bad Request
    end

    SVC->>SVC: BCrypt.encode(newPassword)
    SVC->>DB: save(user with new hashed password)

    SVC-->>API: "Đổi mật khẩu thành công"
    API-->>User: 200 OK { "Đổi mật khẩu thành công" }
```

---

### UC4 — Xác Thực JWT (Mỗi Request Cần Auth)

```mermaid
sequenceDiagram
    actor User as 👤 User (có token)
    participant FILTER as 🔒 JwtAuthenticationFilter
    participant JWT as 🔑 JwtService
    participant UDS as 📋 CustomUserDetailsService
    participant CTX as 🧠 SecurityContext
    participant CTRL as 🌐 Controller

    User->>FILTER: Request + Header:<br/>Authorization: Bearer <token>

    FILTER->>FILTER: Lấy header "Authorization"

    alt Không có token hoặc không bắt đầu "Bearer "
        FILTER-->>User: Tiếp tục (anonymous)<br/>→ Nếu endpoint cần auth → 403
    end

    FILTER->>JWT: extractUsername(token)
    JWT-->>FILTER: username

    FILTER->>UDS: loadUserByUsername(username)
    UDS-->>FILTER: UserDetails

    FILTER->>JWT: isTokenValid(token, userDetails)

    alt Token hết hạn hoặc không hợp lệ
        FILTER-->>User: Tiếp tục (anonymous)<br/>→ 401 Unauthorized
    end

    FILTER->>CTX: setAuthentication(UsernamePasswordAuthenticationToken)

    FILTER->>CTRL: filterChain.doFilter() → Đến Controller

    CTRL-->>User: 200 OK + dữ liệu
```

---

## 📊 Use Case Diagram (Text-based)

```
                    ┌──────────────────────────────────────┐
                    │         <<System>>                   │
                    │     Clothing Store Auth              │
                    │                                      │
  ┌──────────┐      │   ┌──────────────────────┐          │
  │          │      │   │   UC1: Đăng ký       │          │
  │  Guest   │─────►│   │   POST /register     │          │
  │          │      │   └──────────────────────┘          │
  │  (Khách) │      │                                      │
  │          │      │   ┌──────────────────────┐          │
  │          │─────►│   │   UC2: Đăng nhập     │          │
  └──────────┘      │   │   POST /login        │          │
                    │   └──────────────────────┘          │
                    │                                      │
  ┌──────────┐      │   ┌──────────────────────┐          │
  │          │      │   │   UC3: Đổi MK        │          │
  │  User    │─────►│   │   POST /change-pass  │          │
  │          │      │   └──────────────────────┘          │
  │ (Member) │      │                                      │
  │          │      │   ┌──────────────────────┐          │
  │          │─────►│   │   UC4: Dùng API      │          │
  └──────────┘      │   │   + JWT Token        │          │
                    │   └──────────────────────┘          │
                    └──────────────────────────────────────┘
```

---

## 🗂️ Endpoints & HTTP Status

| Endpoint | Method | Auth | Success | Error |
|----------|--------|------|---------|-------|
| `/api/auth/register` | POST | ❌ Public | `201 Created` + JWT | `400` username/email tồn tại |
| `/api/auth/login` | POST | ❌ Public | `200 OK` + JWT | `401` sai credentials |
| `/api/auth/change-password` | POST | ✅ (userId) | `200 OK` | `400` sai mật khẩu cũ |
| `GET /api/products/**` | GET | ❌ Public | `200 OK` | — |
| `POST /api/cart/**` | POST | ✅ JWT Required | `200 OK` | `401` không có token |
| `POST /api/orders` | POST | ✅ JWT Required | `201 Created` | `401` không có token |
| `PATCH /api/orders/{id}/status` | PATCH | ✅ ADMIN only | `200 OK` | `403` không đủ quyền |

---

## 🔑 JWT Token Structure

```
Header:  { "alg": "HS256", "typ": "JWT" }
Payload: { "sub": "username", "iat": 1234567890, "exp": 1234654290 }
         └── exp = iat + 86400 (24 giờ)
Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
```

**Cách dùng:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

