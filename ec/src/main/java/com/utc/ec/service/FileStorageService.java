package com.utc.ec.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Lưu file upload và trả về URL truy cập công khai.
     *
     * @param file file cần lưu
     * @return URL truy cập file (ví dụ: /uploads/images/abc123.jpg)
     */
    String storeFile(MultipartFile file);

    /**
     * Xóa file theo tên file.
     *
     * @param fileName tên file cần xóa
     */
    void deleteFile(String fileName);
}

