package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 作品集里的一条媒体（照片或视频）。
 * 前端按 mediaType 决定渲染 &lt;img&gt; 还是 &lt;video&gt;。
 */
@Data
public class DatingMediaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** image / video */
    private String mediaType;

    /** 访问地址 */
    private String url;
}
