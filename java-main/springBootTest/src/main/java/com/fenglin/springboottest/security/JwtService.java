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
 * token 的 subject 存放用户ID，claim 中附带 username 与 token 版本号 ver，
 * 拦截器据此校验 token 是否已被「改密 / 登出」作废。
 */
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /** 有效期（毫秒），默认 24 小时 */
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getKey() {
        // HS256 要求密钥至少 256 位（32 字节）
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     *
     * @param userId       用户ID（写入 subject）
     * @param username     用户名（写入 claim，便于排查）
     * @param tokenVersion 令牌版本号（写入 ver，改密/登出时自增）
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(Long userId, String username, Long tokenVersion) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("ver", tokenVersion == null ? 0L : tokenVersion)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token，返回用户ID与令牌版本号
     *
     * @param token JWT 字符串
     * @return 解析出的载荷（userId + version）
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
        return new JwtPayload(userId, version);
    }

    /** token 解析结果 */
    public static class JwtPayload {
        public final Long userId;
        public final Long version;

        public JwtPayload(Long userId, Long version) {
            this.userId = userId;
            this.version = version;
        }
    }
}
