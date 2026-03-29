package com.utc.ec.service;

import com.utc.ec.dto.ProductDetailResponse;
import com.utc.ec.dto.ProductFullRequest;

public interface ProductManagementService {

    /**
     * Tao moi san pham day du: Product + Variants (mau sac) + VariantStocks (size + ton kho).
     * Tat ca trong 1 transaction.
     */
    ProductDetailResponse createFull(ProductFullRequest request);

    /**
     * Cap nhat san pham day du.
     * - Variant co id → cap nhat, khong co id → tao moi.
     * - Stock co id   → cap nhat, khong co id → tao moi.
     * De xoa variant/stock, dung endpoint rieng: DELETE /api/product-variants/{id} hoac DELETE /api/variant-stocks/{id}
     */
    ProductDetailResponse updateFull(Integer productId, ProductFullRequest request);

    /**
     * Lay chi tiet day du san pham kem tat ca bien the va ton kho.
     */
    ProductDetailResponse getFullById(Integer productId);
}

