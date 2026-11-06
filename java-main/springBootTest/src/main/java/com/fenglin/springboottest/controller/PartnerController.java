package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.PartnerSiteVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 外部站点联动 / 相亲阁跳转。
 *
 * <p>端点均为公开接口：{@code /api/partner/**}、{@code /api/dating/**} 与 {@code /go/**}
 * 都不在 JwtInterceptor（/api/profile/**）或 AdminJwtInterceptor
 * （/api/manage/**、/api/admin/**）的拦截范围内。</p>
 *
 * <p>目标地址由 application-*.properties 的 {@code partner.site.*} 驱动：
 * 既可以写完整 URL（跳到任意外部相亲站），也可以写本站相对路径
 * （如 {@code /dating.html}，端口变化时无需改配置）。</p>
 */
@RestController
public class PartnerController {

    private static final String DEFAULT_NAME = "姻缘阁 · 寻缘";
    private static final String DEFAULT_SITE_URL = "/dating.html";
    private static final String DEFAULT_GOOGLE_URL = "https://www.google.com/search?q=dating+matchmaking+profile";

    @Value("${partner.site.name:}")
    private String name;

    @Value("${partner.site.url:}")
    private String url;

    @Value("${partner.site.enabled:true}")
    private boolean enabled;

    /** 页面上「去 Google 搜相亲资料」外链，单独配置，不影响主跳转 */
    @Value("${partner.site.google-url:}")
    private String googleUrl;

    /**
     * 后台管理页读取跳转目标。
     *
     * <p>若配置的是相对路径，会基于当前请求补全为绝对地址再返回，
     * 这样前端 {@code window.open} 跳到的仍是后端服务而不是 Vite 的 5173。</p>
     */
    @GetMapping("/api/partner/site")
    public Result<PartnerSiteVO> site(HttpServletRequest request) {
        PartnerSiteVO vo = new PartnerSiteVO();
        vo.setName(StringUtils.hasText(name) ? name : DEFAULT_NAME);
        vo.setUrl(absoluteUrl(targetUrl(), request));
        vo.setGoogleUrl(googleTarget());
        vo.setEnabled(enabled);
        return Result.success("联动站点已就绪 ♥", vo);
    }

    /**
     * 302 到相亲阁。支持相对路径（Location: /dating.html）与完整 URL。
     * 关闭开关时返回 404，避免产生死链。
     */
    @GetMapping("/go/partner")
    public ResponseEntity<Void> redirect() {
        return redirectTo(targetUrl(), enabled);
    }

    /**
     * 302 到 Google 搜索相亲资料，供页面外链或书签使用。
     * 提示：Google 在部分网络环境下不可直接访问。
     */
    @GetMapping("/go/google")
    public ResponseEntity<Void> redirectGoogle() {
        return redirectTo(googleTarget(), enabled);
    }

    private ResponseEntity<Void> redirectTo(String target, boolean on) {
        if (!on || !isSafeTarget(target)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(target))
                .build();
    }

    private String targetUrl() {
        return StringUtils.hasText(url) ? url.trim() : DEFAULT_SITE_URL;
    }

    private String googleTarget() {
        return StringUtils.hasText(googleUrl) ? googleUrl.trim() : DEFAULT_GOOGLE_URL;
    }

    /** 只允许本站相对路径与 http(s)，避免配置里混入 javascript: 之类的危险协议 */
    private boolean isSafeTarget(String target) {
        String t = target.trim().toLowerCase();
        if (t.startsWith("/")) {
            // 排除 //evil.com 这类协议相对地址（开放重定向）
            return !t.startsWith("//");
        }
        return t.startsWith("http://") || t.startsWith("https://");
    }

    private String absoluteUrl(String target, HttpServletRequest request) {
        if (!target.startsWith("/")) {
            return target;
        }
        return request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + target;
    }
}
