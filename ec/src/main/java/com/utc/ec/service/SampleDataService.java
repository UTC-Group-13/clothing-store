package com.utc.ec.service;

public interface SampleDataService {
    /**
     * Tạo toàn bộ dữ liệu mẫu: categories, colors, sizes, products, variants, stocks.
     * Nếu đã có dữ liệu thì xóa sạch và tạo lại.
     *
     * @return Thông báo kết quả
     */
    String generateSampleData();
}

