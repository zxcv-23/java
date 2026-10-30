package com.fenglin.springboottest.config;

import com.fenglin.springboottest.security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：注册 JWT 拦截器，保护个人中心相关接口。
 * 静态资源（/uploads/**、/center.html 等）由 UploadConfig 处理，这里只拦截 /api/profile/**。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/profile/**");
    }
}
