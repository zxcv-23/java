package com.fenglin.springboottest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.entity.Admin;
import com.fenglin.springboottest.mapper.AdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * 后台管理员 JWT 拦截器：保护 /api/manage/** 与 /api/admin/**（登录接口除外）。
 *
 * <p>校验顺序：Bearer 头 → 签名与有效期 → scope=admin → 管理员是否存在 →
 * 账号是否被禁用 → 令牌版本号是否一致（改密 / 登出后立即作废旧 token）。
 * 全部通过后把管理员ID与账号写入 request 属性，供 Controller 使用。</p>
 *
 * <p>校验失败统一返回 401 JSON，前端 adminApi 会据此清除会话并跳回后台登录页。</p>
 */
@Component
public class AdminJwtInterceptor implements HandlerInterceptor {

    public static final String ATTR_ADMIN_ID = "authAdminId";
    public static final String ATTR_ADMIN_ACCOUNT = "authAdminAccount";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        // 预检请求直接放行（CORS 已配置）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "请先登录判官台");
            return false;
        }

        try {
            JwtService.JwtPayload payload = jwtService.parse(auth.substring(7).trim());
            // 前台用户 token 不能用于后台接口
            if (!JwtService.SCOPE_ADMIN.equals(payload.scope)) {
                writeUnauthorized(response, "登录凭证类型不匹配");
                return false;
            }

            // @TableLogic 会自动过滤已删除的管理员
            Admin admin = adminMapper.selectById(payload.userId);
            if (admin == null) {
                writeUnauthorized(response, "管理员不存在或已被删除");
                return false;
            }
            if (admin.getStatus() == null || admin.getStatus() != 1) {
                writeUnauthorized(response, "此判官之印已被封存，无法入台");
                return false;
            }
            Long currentVer = admin.getTokenVersion() == null ? 0L : admin.getTokenVersion();
            if (!currentVer.equals(payload.version)) {
                writeUnauthorized(response, "登录凭证已失效，请重新登录");
                return false;
            }

            request.setAttribute(ATTR_ADMIN_ID, payload.userId);
            request.setAttribute(ATTR_ADMIN_ACCOUNT, admin.getUsername());
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "登录凭证无效或已过期");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of("code", 401, "message", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
