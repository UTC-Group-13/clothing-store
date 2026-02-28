package com.utc.ec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả upload file ảnh")
public class FileUploadResponse {

    @Schema(description = "Tên file đã lưu", example = "a1b2c3d4-uuid.jpg")
    private String fileName;

    @Schema(description = "URL truy cập công khai của ảnh", example = "/uploads/images/a1b2c3d4-uuid.jpg")
    private String fileUrl;

    @Schema(description = "Loại nội dung (content type)", example = "image/jpeg")
    private String contentType;

    @Schema(description = "Kích thước file (bytes)", example = "204800")
    private Long size;
}

