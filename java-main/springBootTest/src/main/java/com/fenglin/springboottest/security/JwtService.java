package com.fenglin.springboottest.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：基于 HS256 生成与解析 token。
 *
 * <p>token 的 subject 存放账号ID，claim 中附带 username、令牌版本号 ver 以及
 * <strong>scope（凭证作用域）</strong>。前台用户与后台管理员共用同一签名密钥，
 * 但通过 scope 严格区分：{@code user} / {@code admin}。各自的拦截器只接受自己
 * 作用域的 token，避免「管理员ID=1」被当成「用户ID=1」造成身份混淆。</p>
 *
 * <p>升级兼容：旧版本签发的前台 token 没有 scope 声明，解析时按 {@code user} 处理，
 * 已登录用户无需重新登录。</p>
 */
@Component
public class JwtService {

    /** 前台用户凭证作用域 */
    public static final String SCOPE_USER = "user";

    /** 后台管理员凭证作用域 */
    public static final String SCOPE_ADMIN = "admin";

    @Value("${jwt.secret}")
    private String secret;

    /** 前台用户 token 有效期（毫秒），默认 24 小时 */
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /** 后台管理员 token 有效期（毫秒），默认 8 小时，权限更高故更短 */
    @Value("${jwt.admin-expiration:28800000}")
    private long adminExpiration;

    private SecretKey getKey() {
        // HS256 要求密钥至少 256 位（32 字节）
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发前台用户 token
     *
     * @param userId       用户ID（写入 subject）
     * @param username     用户名（写入 claim，便于排查）
     * @param tokenVersion 令牌版本号（写入 ver，改密/登出时自增）
     */
    public String generateToken(Long userId, String username, Long tokenVersion) {
        return build(userId, username, tokenVersion, SCOPE_USER, expiration);
    }

    /**
     * 签发后台管理员 token（带 scope=admin，只能被 AdminJwtInterceptor 接受）
     *
     * @param adminId      管理员ID
     * @param username     管理员账号
     * @param tokenVersion 管理员令牌版本号
     */
    public String generateAdminToken(Long adminId, String username, Long tokenVersion) {
        return build(adminId, username, tokenVersion, SCOPE_ADMIN, adminExpiration);
    }

    private String build(Long id, String username, Long version, String scope, long ttl) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("username", username)
                .claim("ver", version == null ? 0L : version)
                .claim("scope", scope)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token，返回账号ID、令牌版本号与作用域
     *
     * @throws io.jsonwebtoken.JwtException 当 token 非法或已过期
     */
    public JwtPayload parse(String token) {
        var payload = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(payload.getSubject());
        // 版本号在 JWT 中可能以 Integer 存储，统一转 Long
        Number ver = payload.get("ver", Number.class);
        Long version = ver == null ? 0L : ver.longValue();
        String scope = payload.get("scope", String.class);
        // 兼容升级前签发、没有 scope 声明的旧前台 token
        if (scope == null || scope.isBlank()) {
            scope = SCOPE_USER;
        }
        return new JwtPayload(userId, version, scope);
    }

    /** token 解析结果 */
    public static class JwtPayload {
        /** 账号ID：scope=user 时为用户ID，scope=admin 时为管理员ID */
        public final Long userId;
        public final Long version;
        /** 凭证作用域：user / admin */
        public final String scope;

        public JwtPayload(Long userId, Long version, String scope) {
            this.userId = userId;
            this.version = version;
            this.scope = scope;
        }
    }
}
