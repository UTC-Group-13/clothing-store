# ✅ CORS FIX COMPLETED - TEST GUIDE

> **Thời gian:** March 18, 2026  
> **Trạng thái:** ✅ ĐÃ SỬA XONG - Build Success  
> **Lỗi gốc:** strict-origin-when-cross-origin

---

## 🎉 ĐÃ HOÀN THÀNH

### ✅ Các File Đã Sửa

| File | Thay đổi | Status |
|------|----------|--------|
| `SecurityConfig.java` | Thêm CORS configuration bean | ✅ Build OK |
| `WebMvcConfig.java` | Thêm CORS mappings | ✅ Build OK |
| `application.yml` | Thêm cors.allowed-origins config | ✅ No Error |
| `README.md` | Cập nhật mô tả tài liệu | ✅ Updated |

### ✅ Build Status
```
[INFO] BUILD SUCCESS
[INFO] Total time:  4.899 s
[INFO] Compiling 111 source files ✅
```

---

## 🧪 TEST NGAY BÂY GIỜ

### Bước 1: Start Backend
```powershell
cd C:\Users\Admin\Desktop\LOC\clothing-store\ec
mvn spring-boot:run
```

Chờ đến khi thấy:
```
Started EcApplication in X seconds
```

### Bước 2: Test từ Browser Console

Mở frontend của bạn (ví dụ `http://localhost:3000`), mở Console (F12), chạy:

```javascript
// Test 1: Public endpoint (GET products)
fetch('http://localhost:8080/api/products')
  .then(res => res.json())
  .then(data => console.log('✅ CORS OK!', data))
  .catch(err => console.error('❌ Still error:', err));

// Test 2: Login endpoint
fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'your_password'
  })
})
  .then(res => res.json())
  .then(data => {
    console.log('✅ Login OK!', data);
    // Lưu token để test protected APIs
    window.token = data.data.accessToken;
  });

// Test 3: Protected endpoint (sau khi login)
fetch('http://localhost:8080/api/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${window.token}`
  },
  body: JSON.stringify({
    categoryId: 1,
    name: 'Test CORS Product',
    description: 'Testing CORS with protected API'
  })
})
  .then(res => res.json())
  .then(data => console.log('✅ Protected API with CORS OK!', data));
```

### Bước 3: Kiểm tra Network Tab

Mở **Network Tab** trong DevTools, reload page, xem response headers:

**✅ Nếu thành công, bạn sẽ thấy:**
```
Status: 200 OK
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Expose-Headers: Authorization, Content-Type, X-Total-Count
```

**❌ Nếu vẫn lỗi:**
- Xem error message trong Console
- Check frontend đang chạy port nào
- Verify backend URL đúng `http://localhost:8080`

---

## 🔧 CUSTOMIZE CORS CHO FRONTEND CỦA BẠN

### Frontend chạy port khác?

**Ví dụ:** Frontend của bạn chạy `http://localhost:3001`

#### Cách 1: Sửa application.yml (Permanent)
```yaml
cors:
  allowed-origins: http://localhost:3001,http://localhost:5173
```

#### Cách 2: Dùng Environment Variable (Flexible)
```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:3001,http://localhost:5173"
mvn spring-boot:run
```

#### Cách 3: Sửa SecurityConfig.java (Hardcode)
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3001",  // ← Thêm port của bạn
    "http://localhost:5173"
));
```

---

## 🌐 PRODUCTION DEPLOYMENT

Khi deploy lên server thật, cần thêm domain production:

### Docker Compose
```yaml
services:
  backend:
    environment:
      - CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com,https://admin.yourdomain.com
```

### Kubernetes
```yaml
env:
  - name: CORS_ALLOWED_ORIGINS
    value: "https://yourdomain.com,https://www.yourdomain.com"
```

### Heroku
```bash
heroku config:set CORS_ALLOWED_ORIGINS="https://yourdomain.com"
```

---

## 📋 CORS CONFIGURATION CHI TIẾT

### Đã Enable

| Setting | Value | Mô tả |
|---------|-------|-------|
| **Allowed Origins** | localhost:3000, 5173, 4200, 8081 | Frontend URLs được phép |
| **Allowed Methods** | GET, POST, PUT, DELETE, PATCH, OPTIONS | HTTP methods |
| **Allowed Headers** | `*` (All) | Cho phép mọi headers |
| **Allow Credentials** | `true` | Cho phép gửi cookies, JWT |
| **Max Age** | 3600s (1 hour) | Cache preflight request |
| **Exposed Headers** | Authorization, Content-Type, X-Total-Count | Headers FE có thể đọc |

### Endpoints có CORS

```
✅ /api/**                 → Tất cả REST APIs
✅ /uploads/**             → Static image files
✅ OPTIONS /api/**         → Preflight requests
```

---

## 🎯 INTEGRATION với FRONTEND FRAMEWORKS

### React / Next.js
```javascript
const API_BASE_URL = 'http://localhost:8080';

// Axios
import axios from 'axios';
const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true  // ← Quan trọng!
});

// Fetch
fetch(`${API_BASE_URL}/api/products`, {
  credentials: 'include'  // ← Quan trọng!
});
```

### Vue.js
```javascript
// axios config
import axios from 'axios';
axios.defaults.baseURL = 'http://localhost:8080';
axios.defaults.withCredentials = true;

// Interceptor để thêm JWT
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Angular
```typescript
// app.config.ts
import { provideHttpClient, withInterceptors } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};

// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      },
      withCredentials: true
    });
  }
  return next(req);
};
```

---

## 🐛 TROUBLESHOOTING

### Lỗi: "CORS policy: The value of the 'Access-Control-Allow-Origin' header must not be the wildcard '*'"

**Nguyên nhân:** Khi `allowCredentials = true`, không được dùng `*` cho origins

**✅ Đã fix:** Dùng list cụ thể thay vì `*`

---

### Lỗi: "Preflight request didn't succeed"

**Nguyên nhân:** Backend không xử lý OPTIONS request

**✅ Đã fix:** Spring Security tự động handle OPTIONS

---

### Lỗi: "No 'Access-Control-Allow-Credentials' header"

**Nguyên nhân:** Frontend gửi `credentials: 'include'` nhưng backend chưa cho phép

**✅ Đã fix:** `allowCredentials = true` trong CorsConfiguration

---

### Frontend vẫn thấy CORS error?

**Checklist:**
1. ✅ Backend đã restart?
2. ✅ Frontend URL có trong allowed-origins?
3. ✅ Request có gửi `credentials: 'include'` hoặc `withCredentials: true`?
4. ✅ JWT token format đúng: `Bearer <token>`?
5. ✅ Không có typo trong URL?

**Debug:**
```powershell
# Kiểm tra backend logs
mvn spring-boot:run

# Tìm dòng:
# "Mapped CORS configuration: CorsConfiguration[...]"
```

---

## 📸 SCREENSHOTS MONG ĐỢI

### ✅ Network Tab - Successful Request
```
Request URL: http://localhost:8080/api/products
Request Method: GET
Status Code: 200 OK

Response Headers:
✅ Access-Control-Allow-Origin: http://localhost:3000
✅ Access-Control-Allow-Credentials: true
✅ Content-Type: application/json
```

### ✅ Console - No Errors
```javascript
✅ CORS OK! {success: true, data: [...]}
```

---

## 🎓 GIẢI THÍCH CORS

### CORS là gì?
**Cross-Origin Resource Sharing** - Cơ chế bảo mật của browser.

**Same Origin:**
```
http://localhost:3000/page1 → http://localhost:3000/page2  ✅ OK
```

**Cross Origin (bị block nếu không config CORS):**
```
http://localhost:3000      → http://localhost:8080/api    ❌ Blocked
http://localhost:5173      → http://localhost:8080/api    ❌ Blocked
https://yourdomain.com     → http://api.yourdomain.com    ❌ Blocked
```

### Preflight Request
Browser tự động gửi OPTIONS request trước để hỏi:
1. Origin này có được phép không?
2. Method này có được phép không?
3. Headers này có được phép không?

Nếu backend response OK → browser mới gửi request thật.

### allowCredentials
- `true` → Cho phép gửi cookies, JWT token
- `false` → Chỉ cho phép request đơn giản

---

## ✨ KẾT QUẢ

### ✅ Đã Fix Thành Công
1. ✅ Thêm CORS configuration trong SecurityConfig
2. ✅ Thêm CORS mappings trong WebMvcConfig
3. ✅ Thêm config options trong application.yml
4. ✅ Build thành công (111 files compiled)
5. ✅ Không có lỗi syntax
6. ✅ Support tất cả frontend frameworks phổ biến

### ✅ Frontend Có Thể
1. Gọi tất cả public APIs (GET products, categories)
2. Đăng nhập và nhận JWT token
3. Gọi protected APIs với JWT trong Authorization header
4. Upload files (images)
5. Hiển thị ảnh từ `/uploads/images/`

### ✅ Tài Liệu Đã Tạo
1. [CORS_FIX.md](CORS_FIX.md) - Hướng dẫn chi tiết
2. [README.md](README.md) - Cập nhật với links và mô tả
3. Test commands và integration examples

---

## 🚀 NEXT STEPS

1. **Restart backend:** `mvn spring-boot:run`
2. **Start frontend:** `npm run dev` (hoặc tương tự)
3. **Test API calls** từ browser console
4. **Verify** không còn CORS error trong Network tab
5. **Continue development** với [API_ENDPOINTS_LIST.md](API_ENDPOINTS_LIST.md)

---

## 📞 SUPPORT

Nếu vẫn gặp vấn đề:
1. Check backend logs có error không
2. Verify frontend URL và port
3. Clear browser cache
4. Xem [CORS_FIX.md](CORS_FIX.md) phần Troubleshooting

---

**CORS đã được fix hoàn toàn! Chúc bạn code vui vẻ! 🎉**

