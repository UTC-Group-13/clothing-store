package com.utc.ec.controller;

import com.utc.ec.dto.ApiResponse;
import com.utc.ec.dto.FileUploadResponse;
import com.utc.ec.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "API upload và quản lý file ảnh")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    // -------------------------------------------------------------------------
    // Upload 1 ảnh
    // -------------------------------------------------------------------------
    @Operation(
            summary = "Upload một ảnh",
            description = "Upload một file ảnh (jpeg, png, gif, webp, svg). "
                    + "Trả về URL để sử dụng cho trường `productImage` hoặc bất kỳ trường ảnh nào."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Upload thành công",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "File không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PostMapping(value = "/image", consumes = "multipart/form-data")
    public ApiResponse<FileUploadResponse> uploadImage(
            @Parameter(description = "File ảnh cần upload", required = true)
            @RequestParam("file") MultipartFile file) {

        String fileUrl = fileStorageService.storeFile(file);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

        FileUploadResponse response = FileUploadResponse.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();

        return ApiResponse.<FileUploadResponse>builder()
                .success(true)
                .message("Upload ảnh thành công.")
                .data(response)
                .build();
    }

    // -------------------------------------------------------------------------
    // Upload nhiều ảnh cùng lúc
    // -------------------------------------------------------------------------
    @Operation(
            summary = "Upload nhiều ảnh",
            description = "Upload nhiều file ảnh cùng lúc (tối đa 10 file). "
                    + "Trả về danh sách URL tương ứng."
    )
    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ApiResponse<List<FileUploadResponse>> uploadImages(
            @Parameter(description = "Danh sách file ảnh (tối đa 10 file)", required = true)
            @RequestParam("files") List<MultipartFile> files) {

        if (files.size() > 10) {
            return ApiResponse.<List<FileUploadResponse>>builder()
                    .success(false)
                    .message("Chỉ được upload tối đa 10 ảnh mỗi lần.")
                    .build();
        }

        List<FileUploadResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = fileStorageService.storeFile(file);
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            results.add(FileUploadResponse.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build());
        }

        return ApiResponse.<List<FileUploadResponse>>builder()
                .success(true)
                .message("Upload " + results.size() + " ảnh thành công.")
                .data(results)
                .build();
    }

    // -------------------------------------------------------------------------
    // Xóa ảnh
    // -------------------------------------------------------------------------
    @Operation(
            summary = "Xóa ảnh",
            description = "Xóa một file ảnh khỏi server theo tên file."
    )
    @DeleteMapping("/image/{fileName}")
    public ApiResponse<Void> deleteImage(
            @Parameter(description = "Tên file cần xóa (ví dụ: a1b2c3d4-uuid.jpg)", required = true)
            @PathVariable String fileName) {

        fileStorageService.deleteFile(fileName);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa ảnh thành công.")
                .build();
    }
}

