package com.fenglin.springboottest.config;

import com.fenglin.springboottest.security.AdminJwtInterceptor;
import com.fenglin.springboottest.security.JwtInterceptor;
import com.fenglin.springboottest.security.OptionalJwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：注册三套互不干扰的 JWT 拦截器。
 *
 * <ul>
 *   <li>前台用户（严格）：{@code /api/profile/**}、{@code /api/dating/me/**}，
 *       只接受 scope=user 的凭证，没有 token 直接 401</li>
 *   <li>姻缘阁浏览（宽松）：{@code /api/dating/**} 只做「尽力识别登录身份」，
 *       游客照常访问，登录后额外显示点赞状态与联系方式</li>
 *   <li>后台管理：{@code /api/manage/**} 与 {@code /api/admin/**}，只接受 scope=admin 的凭证；
 *       {@code /api/admin/login} 放行，否则无法登录</li>
 * </ul>
 *
 * <p>注意注册顺序：宽松拦截器先注册、严格拦截器后注册。Spring 按注册顺序执行，
 * 同一路径上先被宽松版写入 userId，再由严格版做真正的鉴权；
 * 因为严格版匹配的是 {@code /api/dating/me/**} 这类子路径，两者不会互相覆盖。</p>
 *
 * 静态资源（/uploads/**、/dating.html 等）由 UploadConfig 处理，不在此拦截。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AdminJwtInterceptor adminJwtInterceptor;

    @Autowired
    private OptionalJwtInterceptor optionalJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 宽松版：只认身份不拦截，游客也能浏览姻缘阁
        registry.addInterceptor(optionalJwtInterceptor)
                .addPathPatterns("/api/dating/**");

        // 严格版：个人中心 + 姻缘阁的「我的」写操作
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/profile/**", "/api/dating/me/**");

        registry.addInterceptor(adminJwtInterceptor)
                .addPathPatterns("/api/manage/**", "/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }
}
