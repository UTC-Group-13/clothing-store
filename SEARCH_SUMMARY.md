# 🔍 SEARCH FUNCTIONALITY IMPLEMENTATION

## ✅ Đã hoàn thành

Đã thêm **tính năng tìm kiếm (search/filter)** cho 4 entities với pagination và sorting.

---

## 📊 Search APIs đã thêm

### 1. **Product Search** (`/api/products/search`)
**Tìm kiếm theo:**
- `keyword` - Tên hoặc mô tả sản phẩm (không phân biệt hoa thường)
- `categoryId` - Lọc theo danh mục sản phẩm

**Ví dụ:**
```bash
GET /api/products/search?keyword=áo&categoryId=1&page=0&size=10&sortBy=name&direction=ASC
```

### 2. **Product Item Search** (`/api/product-items/search`)
**Tìm kiếm theo:**
- `keyword` - SKU
- `productId` - Lọc theo sản phẩm
- `minPrice` - Giá tối thiểu
- `maxPrice` - Giá tối đa

**Ví dụ:**
```bash
GET /api/product-items/search?keyword=SKU001&minPrice=100000&maxPrice=500000&page=0&size=10
```

### 3. **Variation Search** (`/api/variations/search`)
**Tìm kiếm theo:**
- `keyword` - Tên variation (Size, Color, etc.)
- `categoryId` - Lọc theo danh mục

**Ví dụ:**
```bash
GET /api/variations/search?keyword=size&categoryId=1&page=0&size=10
```

### 4. **Variation Option Search** (`/api/variation-options/search`)
**Tìm kiếm theo:**
- `keyword` - Giá trị option (XL, Red, etc.)
- `variationId` - Lọc theo variation

**Ví dụ:**
```bash
GET /api/variation-options/search?keyword=xl&variationId=1&page=0&size=10
```

---

## 🎯 Query Parameters

Tất cả search endpoints hỗ trợ:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `keyword` | String | ❌ | null | Từ khóa tìm kiếm |
| `categoryId` | Integer | ❌ | null | ID danh mục (Product, Variation) |
| `productId` | Integer | ❌ | null | ID sản phẩm (ProductItem) |
| `variationId` | Integer | ❌ | null | ID variation (VariationOption) |
| `minPrice` | Integer | ❌ | null | Giá tối thiểu (ProductItem) |
| `maxPrice` | Integer | ❌ | null | Giá tối đa (ProductItem) |
| `page` | int | ❌ | 0 | Số trang |
| `size` | int | ❌ | 10 | Số items mỗi trang |
| `sortBy` | String | ❌ | id | Trường sắp xếp |
| `direction` | String | ❌ | ASC | Hướng sắp xếp |

---

## 💡 Ví dụ chi tiết

### 1. Tìm kiếm sản phẩm có từ "áo" trong tên hoặc mô tả
```bash
GET /api/products/search?keyword=áo
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Áo sơ mi nam",
        "description": "Áo sơ mi công sở cao cấp",
        "categoryId": 1,
        ...
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

### 2. Tìm product items theo khoảng giá
```bash
GET /api/product-items/search?minPrice=200000&maxPrice=500000&sortBy=price&direction=ASC
```

### 3. Tìm variations của category cụ thể
```bash
GET /api/variations/search?categoryId=2&sortBy=name
```

### 4. Tìm variation options có giá trị "L" hoặc "XL"
```bash
GET /api/variation-options/search?keyword=l&page=0&size=20
```

### 5. Kết hợp nhiều filters
```bash
GET /api/product-items/search?productId=5&minPrice=100000&maxPrice=300000&page=0&size=10&sortBy=price&direction=DESC
```

---

## 🔧 Thay đổi kỹ thuật

### 1. **Repository Layer** - JPQL Query với @Query

#### ProductRepository
```java
@Query("SELECT p FROM Product p WHERE " +
       "(:keyword IS NULL OR :keyword = '' OR " +
       "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
       "(:categoryId IS NULL OR p.categoryId = :categoryId)")
Page<Product> searchProducts(@Param("keyword") String keyword,
                              @Param("categoryId") Integer categoryId,
                              Pageable pageable);
```

#### ProductItemRepository
```java
@Query("SELECT pi FROM ProductItem pi WHERE " +
       "(:keyword IS NULL OR :keyword = '' OR " +
       "LOWER(pi.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
       "(:productId IS NULL OR pi.productId = :productId) AND " +
       "(:minPrice IS NULL OR pi.price >= :minPrice) AND " +
       "(:maxPrice IS NULL OR pi.price <= :maxPrice)")
Page<ProductItem> searchProductItems(...);
```

**Đặc điểm:**
- ✅ Case-insensitive search với `LOWER()`
- ✅ Wildcard matching với `LIKE '%...%'`
- ✅ Optional parameters với `IS NULL` check
- ✅ Multiple filters với `AND`
- ✅ Support pagination và sorting

### 2. **Service Layer**

```java
@Override
public Page<ProductDTO> searchProducts(String keyword, Integer categoryId, Pageable pageable) {
    return repository.searchProducts(keyword, categoryId, pageable).map(mapper::toDto);
}
```

### 3. **Controller Layer**

```java
@GetMapping("/search")
public ApiResponse<PagedResponse<ProductDTO>> searchProducts(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Integer categoryId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "ASC") String direction) {
    
    Sort sort = direction.equalsIgnoreCase("DESC") 
        ? Sort.by(sortBy).descending() 
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<ProductDTO> pagedResult = service.searchProducts(keyword, categoryId, pageable);
    
    // Convert to PagedResponse...
}
```

---

## 🎨 Best Practices áp dụng

### 1. **Optional Parameters**
- Tất cả filter parameters đều là `required = false`
- Query tự động bỏ qua null parameters
- Flexible cho client

### 2. **Case-Insensitive Search**
```sql
LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
```
- Không phân biệt hoa thường
- Tìm kiếm chính xác hơn với tiếng Việt

### 3. **Wildcard Matching**
```sql
LIKE '%keyword%'
```
- Tìm ở đầu, giữa, cuối string
- Flexible cho user

### 4. **Range Filtering**
```sql
pi.price >= :minPrice AND pi.price <= :maxPrice
```
- Lọc theo khoảng giá
- Có thể chỉ dùng min hoặc max

### 5. **Combine với Pagination & Sorting**
- Search + paging = tối ưu performance
- Sort theo bất kỳ field nào
- Consistent API design

---

## 📈 Performance Considerations

### 1. **Database Indexes**
Nên tạo indexes cho các trường thường search:

```sql
-- Recommended indexes
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_product_category_id ON product(category_id);
CREATE INDEX idx_product_item_sku ON product_item(sku);
CREATE INDEX idx_product_item_price ON product_item(price);
CREATE INDEX idx_product_item_product_id ON product_item(product_id);
CREATE INDEX idx_variation_name ON variation(name);
CREATE INDEX idx_variation_option_value ON variation_option(value);
```

### 2. **LIKE Performance**
- `LIKE 'keyword%'` - Fast (uses index)
- `LIKE '%keyword%'` - Slower (full scan)
- Trade-off: Flexibility vs Performance

### 3. **Pagination**
- Luôn dùng pagination với search
- Limit kết quả để tránh overload
- Default size = 10

---

## 🆚 So sánh: Simple List vs Search

### Simple GetAll (`/api/products`)
```bash
GET /api/products
```
- ✅ Lấy tất cả records
- ⚠️ Không filter
- ⚠️ Không phù hợp với data lớn

### Search with Filters (`/api/products/search`)
```bash
GET /api/products/search?keyword=áo&categoryId=1&page=0&size=10
```
- ✅ Filter theo nhiều criteria
- ✅ Pagination built-in
- ✅ Sorting flexible
- ✅ Production-ready

---

## 📝 Files đã thay đổi

### Repositories (4 files):
1. ✅ `ProductRepository.java` - Added `searchProducts()`
2. ✅ `ProductItemRepository.java` - Added `searchProductItems()`
3. ✅ `VariationRepository.java` - Added `searchVariations()`
4. ✅ `VariationOptionRepository.java` - Added `searchVariationOptions()`

### Services (4 files):
1. ✅ `ProductService.java` - Added search method
2. ✅ `ProductItemService.java` - Added search method
3. ✅ `VariationService.java` - Added search method
4. ✅ `VariationOptionService.java` - Added search method

### Service Implementations (4 files):
1. ✅ `ProductServiceImpl.java` - Implemented search
2. ✅ `ProductItemServiceImpl.java` - Implemented search
3. ✅ `VariationServiceImpl.java` - Implemented search
4. ✅ `VariationOptionServiceImpl.java` - Implemented search

### Controllers (4 files):
1. ✅ `ProductController.java` - Added `/search` endpoint
2. ✅ `ProductItemController.java` - Added `/search` endpoint
3. ✅ `VariationController.java` - Added `/search` endpoint
4. ✅ `VariationOptionController.java` - Added `/search` endpoint

**Tổng cộng: 16 files updated**

---

## 🎯 Use Cases

### 1. **E-commerce Product Search**
```bash
# User tìm "áo" trong category "Thời trang nam"
GET /api/products/search?keyword=áo&categoryId=1

# User filter theo giá
GET /api/product-items/search?minPrice=200000&maxPrice=500000
```

### 2. **Admin Panel Filtering**
```bash
# Admin tìm product item theo SKU
GET /api/product-items/search?keyword=SKU-2024-001

# Admin filter variations của category
GET /api/variations/search?categoryId=3&sortBy=name
```

### 3. **Autocomplete/Typeahead**
```bash
# User gõ "x" -> suggest "XL", "XXL"
GET /api/variation-options/search?keyword=x&size=5
```

### 4. **Advanced Filtering**
```bash
# Combine multiple filters
GET /api/product-items/search?productId=10&minPrice=100000&maxPrice=300000&keyword=blue&sortBy=price&direction=ASC
```

---

## 🚀 Testing với Swagger

1. Mở Swagger UI: `http://localhost:8080/swagger-ui.html`

2. Navigate to search endpoints:
   - `/api/products/search`
   - `/api/product-items/search`
   - `/api/variations/search`
   - `/api/variation-options/search`

3. Test scenarios:
   - ✅ Search với keyword only
   - ✅ Search với filters only
   - ✅ Combine keyword + filters
   - ✅ Empty search (return all)
   - ✅ Pagination
   - ✅ Sorting

---

## 💪 Tính năng nổi bật

### 1. **Flexible Search**
- Optional parameters
- Mix and match filters
- Works with or without keyword

### 2. **Case-Insensitive**
- "áo" = "ÁO" = "Áo"
- User-friendly
- Vietnamese support

### 3. **Range Filtering**
- Min/max price
- Can use either or both
- Flexible for client

### 4. **Integrated Pagination**
- Always paginated
- No performance issues
- Production-ready

### 5. **Multi-field Search**
- Product: name OR description
- Flexible matching
- Better UX

---

## 🎉 Kết luận

✅ **4 Search APIs** với đầy đủ filters
✅ **Case-insensitive** search  
✅ **Range filtering** (giá)
✅ **Pagination & Sorting** built-in
✅ **Optional parameters** - flexible
✅ **JPQL queries** - database-agnostic
✅ **Production-ready** với indexes recommendation

**Ready for complex search scenarios!** 🔍✨
