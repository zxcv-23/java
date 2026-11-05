package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.PartnerSiteVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 外部站点联动：后台管理页跳转目标的配置来源，以及一个可直接用的 302 跳转端点。
 *
 * <p>两个端点均为公开接口：{@code /api/partner/**} 与 {@code /go/**} 都不在
 * JwtInterceptor（/api/profile/**）或 AdminJwtInterceptor（/api/manage/**、/api/admin/**）
 * 的拦截范围内，但仍受 CorsConfig 的跨域白名单保护。</p>
 */
@RestController
public class PartnerController {

    @Value("${partner.site.name:帅哥美女相亲阁}")
    private String name;

    @Value("${partner.site.url:http://127.0.0.1:5173/}")
    private String url;

    @Value("${partner.site.enabled:true}")
    private boolean enabled;

    /**
     * 后台管理页读取跳转目标。前端带兜底默认值，后端不可用时入口依然能跳。
     */
    @GetMapping("/api/partner/site")
    public Result<PartnerSiteVO> site() {
        PartnerSiteVO vo = new PartnerSiteVO();
        vo.setName(name);
        vo.setUrl(url);
        vo.setEnabled(enabled);
        return Result.success("联动站点已就绪 ♥", vo);
    }

    /**
     * 直接 302 到目标站，便于浏览器书签、或外部站点做回链。
     * 关闭开关时返回 404，避免产生死链。
     */
    @GetMapping("/go/partner")
    public ResponseEntity<Void> redirect() {
        if (!enabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
