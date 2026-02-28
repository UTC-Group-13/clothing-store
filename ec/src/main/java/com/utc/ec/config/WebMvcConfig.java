package com.utc.ec.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Cấu hình serve static file từ thư mục upload trên disk.
 * Ảnh được truy cập qua URL: GET /uploads/images/{fileName}
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/images}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:/uploads/images}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(absolutePath);
    }
}

