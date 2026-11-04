package com.fenglin.springboottest.config;

import com.fenglin.springboottest.security.AdminJwtInterceptor;
import com.fenglin.springboottest.security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：注册两套互不干扰的 JWT 拦截器。
 *
 * <ul>
 *   <li>前台用户：{@code /api/profile/**}，只接受 scope=user 的凭证</li>
 *   <li>后台管理：{@code /api/manage/**} 与 {@code /api/admin/**}，只接受 scope=admin 的凭证；
 *       {@code /api/admin/login} 放行，否则无法登录</li>
 * </ul>
 * 静态资源（/uploads/**、/center.html 等）由 UploadConfig 处理，不在此拦截。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AdminJwtInterceptor adminJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/profile/**");

        registry.addInterceptor(adminJwtInterceptor)
                .addPathPatterns("/api/manage/**", "/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }
}
