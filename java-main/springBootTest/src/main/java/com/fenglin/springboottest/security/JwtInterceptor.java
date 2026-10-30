package com.fenglin.springboottest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * JWT 拦截器：校验请求头 Authorization: Bearer <token>，
 * 并比对 token 中的令牌版本号与数据库中当前版本号（不一致说明已改密/登出，token 作废）。
 * 校验通过后把用户ID写入 request 属性 authUserId，供 Controller 使用；
 * 校验失败直接返回 401（JSON），不进入 Controller。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

    public static final String ATTR_USER_ID = "authUserId";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        // 预检请求直接放行（CORS 已配置，这里避免拦截 OPTIONS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        String token = auth.substring(7).trim();
        try {
            JwtService.JwtPayload payload = jwtService.parse(token);
            User user = userMapper.selectById(payload.userId);
            if (user == null) {
                writeUnauthorized(response, "用户不存在或已被删除");
                return false;
            }
            // 账号被禁用 → 即使 token 有效也拒绝访问
            if (user.getStatus() == null || user.getStatus() != 1) {
                writeUnauthorized(response, "账号已被封印，请联系管理员");
                return false;
            }
            // 版本号不一致 → token 已被改密/登出作废
            Long currentVer = user.getTokenVersion() == null ? 0L : user.getTokenVersion();
            if (!currentVer.equals(payload.version)) {
                writeUnauthorized(response, "登录凭证已失效，请重新登录");
                return false;
            }
            request.setAttribute(ATTR_USER_ID, payload.userId);
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
