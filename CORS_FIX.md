# 🔧 HƯỚNG DẪN SỬA LỖI CORS - CLOTHING STORE API

> **Lỗi:** `strict-origin-when-cross-origin`  
> **Nguyên nhân:** Backend chưa cho phép frontend từ domain khác gọi API  
> **Giải pháp:** ✅ Đã cấu hình CORS

---

## ✅ CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 1️⃣ **SecurityConfig.java** - Thêm CORS trong Spring Security

**File:** `ec/src/main/java/com/utc/ec/config/security/SecurityConfig.java`

**Thay đổi:**
```java
// ✅ THÊM MỚI: Import CORS classes
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// ✅ THÊM MỚI: Enable CORS trong SecurityFilterChain
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // ← THÊM DÒNG NÀY
            .csrf(AbstractHttpConfigurer::disable)
            // ...existing code...
}

// ✅ THÊM MỚI: CORS Configuration Bean
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Cho phép các frontend URLs
    configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // React
            "http://localhost:5173",      // Vite
            "http://localhost:4200",      // Angular
            "http://localhost:8081",      // Vue
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
    ));
    
    // Cho phép tất cả HTTP methods
    configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));
    
    // Cho phép tất cả headers
    configuration.setAllowedHeaders(List.of("*"));
    
    // Cho phép gửi credentials (JWT token)
    configuration.setAllowCredentials(true);
    
    // Cache preflight request 1 hour
    configuration.setMaxAge(3600L);
    
    // Expose headers để FE đọc được
    configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
    ));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
}
```

---

### 2️⃣ **WebMvcConfig.java** - Thêm CORS mapping

**File:** `ec/src/main/java/com/utc/ec/config/WebMvcConfig.java`

**Thay đổi:**
```java
// ✅ THÊM MỚI: Import
import org.springframework.web.servlet.config.annotation.CorsRegistry;

// ✅ THÊM MỚI: Inject allowed origins từ config
@Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
private String[] allowedOrigins;

// ✅ THÊM MỚI: Configure CORS mappings
@Override
public void addCorsMappings(CorsRegistry registry) {
    // CORS cho API endpoints
    registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);

    // CORS cho static files (images)
    registry.addMapping("/uploads/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET")
            .allowedHeaders("*")
            .maxAge(3600);
}
```

---

### 3️⃣ **application.yml** - Thêm cấu hình CORS

**File:** `ec/src/main/resources/application.yml`

**Thay đổi:**
```yaml
# ✅ THÊM MỚI: CORS configuration
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,http://localhost:4200,http://localhost:8081,http://127.0.0.1:3000,http://127.0.0.1:5173}
```

**Giải thích:**
- Biến môi trường `CORS_ALLOWED_ORIGINS` để override khi deploy
- Default support: React (3000), Vite (5173), Angular (4200), Vue (8081)
- Format: danh sách URLs phân cách bởi dấu phẩy

---

## 🚀 CÁCH SỬ DỤNG

### Option 1: Dùng Default Config (Development)
Không cần làm gì thêm! Backend đã support các port phổ biến:
- ✅ `http://localhost:3000` (React)
- ✅ `http://localhost:5173` (Vite)
- ✅ `http://localhost:4200` (Angular)
- ✅ `http://localhost:8081` (Vue)

### Option 2: Custom Origins (Production)
Thêm biến môi trường khi chạy:

**Windows PowerShell:**
```powershell
$env:CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
java -jar ec-0.0.1-SNAPSHOT.jar
```

**Linux/Mac:**
```bash
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
java -jar ec-0.0.1-SNAPSHOT.jar
```

**Docker:**
```yaml
environment:
  - CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

---

## 🧪 KIỂM TRA LỖI CORS ĐÃ FIX

### Bước 1: Build lại application
```powershell
cd C:\Users\Admin\Desktop\LOC\clothing-store\ec
mvn clean install -DskipTests
```

### Bước 2: Restart Spring Boot
```powershell
mvn spring-boot:run
```

### Bước 3: Test từ Frontend
```javascript
// React/Vue/Angular
fetch('http://localhost:8080/api/products', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log('✅ CORS OK!', data))
.catch(error => console.error('❌ Still error:', error));
```

### Bước 4: Test với JWT Token
```javascript
// Login first
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: 'your_password' })
});
const { data } = await loginResponse.json();
const token = data.accessToken;

// Call protected API
const response = await fetch('http://localhost:8080/api/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`  // ← JWT token
  },
  body: JSON.stringify({
    categoryId: 1,
    name: 'Test Product',
    description: 'Test'
  })
});

console.log('✅ Protected API với CORS OK!', await response.json());
```

---

## 🔍 KIỂM TRA BROWSER CONSOLE

### Trước khi fix (Lỗi):
```
Access to fetch at 'http://localhost:8080/api/products' from origin 'http://localhost:3000' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present.
```

### Sau khi fix (Success):
```
✅ Status: 200 OK
✅ Access-Control-Allow-Origin: http://localhost:3000
✅ Access-Control-Allow-Credentials: true
✅ Data received successfully
```

---

## 📊 CORS HEADERS ĐƯỢC TRẢI RA

Khi frontend gọi API, backend sẽ trả về các headers:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
Access-Control-Expose-Headers: Authorization, Content-Type, X-Total-Count
```

---

## 🎯 PREFLIGHT REQUEST (OPTIONS)

Browser tự động gửi OPTIONS request trước khi gọi API thực sự:

```
OPTIONS /api/products HTTP/1.1
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: authorization,content-type

→ Backend response:
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: authorization, content-type
Access-Control-Max-Age: 3600

→ Sau đó browser mới gửi request thật:
POST /api/products HTTP/1.1
Authorization: Bearer eyJhbGc...
Content-Type: application/json
```

---

## 🛡️ BẢO MẬT VỚI CORS

### ✅ Đã làm đúng:
1. **allowCredentials = true** → Cho phép gửi JWT token
2. **Chỉ list origins cụ thể** → Không dùng `*` (wildcard)
3. **Giới hạn methods** → Chỉ GET, POST, PUT, DELETE, OPTIONS
4. **MaxAge = 3600** → Cache preflight 1 hour (giảm requests)

### ⚠️ Khi Deploy Production:
```yaml
# Thêm domain thật vào application.yml
cors:
  allowed-origins: https://yourdomain.com,https://www.yourdomain.com

# Hoặc dùng biến môi trường
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

**⚠️ KHÔNG BAO GIỜ DÙNG:**
```java
configuration.setAllowedOrigins(List.of("*"));  // ❌ Rất nguy hiểm!
```

---

## 🐛 NẾU VẪN LỖI CORS

### Checklist:
- [ ] Đã restart Spring Boot server?
- [ ] Frontend đang chạy đúng port? (3000, 5173, 4200, 8081)
- [ ] URL trong fetch có đúng `http://localhost:8080`?
- [ ] Có đang dùng proxy trong frontend config không?

### Frontend đang chạy port khác?
**Ví dụ:** Frontend chạy port 3001

**Cách 1: Thêm vào application.yml**
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001,http://localhost:5173
```

**Cách 2: Dùng biến môi trường**
```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:3001"
mvn spring-boot:run
```

### Dùng Vite Proxy (Alternative)
Nếu không muốn config CORS, có thể dùng proxy trong `vite.config.js`:

```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

Nhưng **khuyến khích dùng CORS** vì:
- Dễ deploy production
- Hỗ trợ nhiều frontend cùng lúc
- Không phụ thuộc vào dev server

---

## 📝 TESTING CHECKLIST

### ✅ Kiểm tra các endpoints:

| Endpoint | Method | Auth | CORS | Status |
|----------|--------|------|------|--------|
| `/api/auth/login` | POST | Public | ✅ | Should work |
| `/api/auth/register` | POST | Public | ✅ | Should work |
| `/api/products` | GET | Public | ✅ | Should work |
| `/api/products` | POST | Protected | ✅ | Need JWT |
| `/uploads/images/test.jpg` | GET | Public | ✅ | Should work |
| `/swagger-ui.html` | GET | Public | ✅ | Should work |

---

## 🎉 KẾT QUẢ MONG ĐỢI

Sau khi apply các thay đổi:

### ✅ Frontend có thể:
1. Gọi API đăng ký/đăng nhập
2. Gọi API lấy danh sách sản phẩm
3. Gửi JWT token trong Authorization header
4. Upload và hiển thị ảnh sản phẩm
5. Gọi tất cả protected APIs khi đã login

### ✅ Backend sẽ:
1. Tự động xử lý OPTIONS preflight requests
2. Trả về Access-Control-Allow-Origin header
3. Accept requests từ các origins được phép
4. Validate JWT token như bình thường

---

## 📞 NEXT STEPS

1. **Build & Run:**
   ```powershell
   cd C:\Users\Admin\Desktop\LOC\clothing-store\ec
   mvn clean install
   mvn spring-boot:run
   ```

2. **Test từ Browser Console:**
   - Mở `http://localhost:3000` (frontend)
   - F12 → Console
   - Run fetch command
   - Kiểm tra Network tab

3. **Nếu OK:**
   - ✅ Không còn CORS error
   - ✅ Response status 200
   - ✅ Data hiển thị đúng

4. **Nếu vẫn lỗi:**
   - Kiểm tra frontend đang chạy port nào
   - Xem error message trong console
   - Check Network tab → Headers → xem có `Access-Control-Allow-Origin` không

---

## 🔗 TÀI LIỆU LIÊN QUAN

- [PROJECT_ANALYSIS.md](PROJECT_ANALYSIS.md) - Phân tích tổng quan
- [SYSTEM_FLOW_DIAGRAM.md](SYSTEM_FLOW_DIAGRAM.md) - Sơ đồ luồng
- [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md) - Danh sách APIs
- [Spring CORS Documentation](https://spring.io/guides/gs/rest-service-cors/)

---

**Lỗi CORS đã được fix! 🎉**  
**Tài liệu này được tạo bởi GitHub Copilot - March 18, 2026**

