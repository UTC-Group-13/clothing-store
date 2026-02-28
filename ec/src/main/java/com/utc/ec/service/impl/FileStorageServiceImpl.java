package com.utc.ec.service.impl;

import com.utc.ec.exception.BusinessException;
import com.utc.ec.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Value("${file.upload.dir:uploads/images}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:/uploads/images}")
    private String urlPrefix;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("File upload directory: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload: " + uploadPath, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("file.upload.empty");
        }

        // Kiểm tra content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("file.upload.invalidType");
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("file.upload.tooLarge");
        }

        // Lấy extension từ tên file gốc
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        // Sinh tên file duy nhất
        String newFilename = UUID.randomUUID() + extension;

        Path targetPath = uploadPath.resolve(newFilename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Đã lưu file: {}", targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + newFilename, e);
        }

        return urlPrefix + "/" + newFilename;
    }

    @Override
    public void deleteFile(String fileName) {
        Path filePath = uploadPath.resolve(fileName).normalize();
        // Ngăn path traversal
        if (!filePath.startsWith(uploadPath)) {
            throw new BusinessException("file.delete.invalid");
        }
        try {
            Files.deleteIfExists(filePath);
            log.info("Đã xóa file: {}", filePath);
        } catch (IOException e) {
            log.warn("Không thể xóa file: {}", filePath, e);
        }
    }
}



