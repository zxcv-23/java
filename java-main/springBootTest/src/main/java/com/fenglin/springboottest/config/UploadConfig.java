package com.fenglin.springboottest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 静态资源映射：把本地上传目录（file.upload.dir）以 /uploads/** 暴露给前端直接访问。
 * 与 FileUploadService 中返回的 URL 前缀保持一致。
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        String location = "file:" + Paths.get(uploadDir).toAbsolutePath() + "/";
        registry.addResourceHandler(prefix + "**")
                .addResourceLocations(location);
    }
}
