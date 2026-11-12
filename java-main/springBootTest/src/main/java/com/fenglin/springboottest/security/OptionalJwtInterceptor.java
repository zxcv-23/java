package com.fenglin.springboottest.security;

import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 「可选登录」拦截器：带了合法 token 就把用户ID放到 request 属性里，
 * 没带或 token 失效则当作游客放行，<strong>绝不返回 401</strong>。
 *
 * <p>为什么需要它：姻缘阁的浏览接口（嘉宾列表、主页、作品列表）对游客开放，
 * 但登录后要能额外显示「我点过赞」「联系方式是否可见」等个性化信息。
 * 用严格版 {@link JwtInterceptor} 会挡住游客，不用拦截器又拿不到登录身份，
 * 所以单独做一个宽松版，只做「尽力识别」，不做「拦截」。</p>
 *
 * <p>写操作（发作品、点赞、私信）仍然走严格版拦截器，游客无法绕过。</p>
 */
@Component
public class OptionalJwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

    /** 与 JwtInterceptor 共用同一个 request 属性名，Controller 取值方式保持一致 */
    public static final String ATTR_USER_ID = JwtInterceptor.ATTR_USER_ID;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 预检请求无需识别身份
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return true;
        }
        try {
            JwtService.JwtPayload payload = jwtService.parse(auth.substring(7).trim());
            if (!JwtService.SCOPE_USER.equals(payload.scope)) {
                return true;
            }
            User user = userMapper.selectById(payload.userId);
            if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                return true;
            }
            Long currentVer = user.getTokenVersion() == null ? 0L : user.getTokenVersion();
            if (!currentVer.equals(payload.version)) {
                return true;
            }
            request.setAttribute(ATTR_USER_ID, payload.userId);
        } catch (Exception ignored) {
            // 游客视角继续，不因为一个过期 token 让浏览页报错
        }
        return true;
    }
}
