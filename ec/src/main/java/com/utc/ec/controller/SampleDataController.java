package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.service.SampleDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sample-data")
@RequiredArgsConstructor
@Tag(name = "Sample Data", description = "API tạo dữ liệu mẫu cho testing")
public class SampleDataController {

    private final SampleDataService sampleDataService;

    @Operation(
        summary = "Tạo dữ liệu mẫu",
        description = "Tạo 50 sản phẩm quần áo mẫu gồm đầy đủ: " +
                "categories, colors, sizes, products, product_variants, variant_stocks. " +
                "⚠️ SẼ XÓA SẠCH dữ liệu product cũ trước khi tạo mới!"
    )
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<String> generateSampleData() {
        String result = sampleDataService.generateSampleData();
        return ApiResponse.success("Tạo dữ liệu mẫu thành công!", result);
    }
}

