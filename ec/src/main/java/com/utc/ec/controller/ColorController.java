package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.ColorDTO;
import com.utc.ec.service.ColorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
@Tag(name = "Color", description = "API quản lý màu sắc")
public class ColorController {

    private final ColorService service;
    private final MessageSource messageSource;

    @Operation(summary = "Tạo màu sắc")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ColorDTO> create(@Valid @RequestBody ColorDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("color.create.success", null, LocaleContextHolder.getLocale()),
                service.create(dto));
    }

    @Operation(summary = "Cập nhật màu sắc")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ApiResponse<ColorDTO> update(
            @Parameter(description = "ID màu") @PathVariable Integer id,
            @Valid @RequestBody ColorDTO dto) {
        return ApiResponse.success(
                messageSource.getMessage("color.update.success", null, LocaleContextHolder.getLocale()),
                service.update(id, dto));
    }

    @Operation(summary = "Xóa màu sắc", description = "Không xóa được nếu đang có biến thể sử dụng.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "ID màu") @PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.success(
                messageSource.getMessage("color.delete.success", null, LocaleContextHolder.getLocale()),
                null);
    }

    @Operation(summary = "Lấy màu sắc theo ID")
    @GetMapping("/{id}")
    public ApiResponse<ColorDTO> getById(@Parameter(description = "ID màu") @PathVariable Integer id) {
        return ApiResponse.success(null, service.getById(id));
    }

    @Operation(summary = "Lấy tất cả màu sắc")
    @GetMapping
    public ApiResponse<List<ColorDTO>> getAll() {
        return ApiResponse.success(null, service.getAll());
    }
}

