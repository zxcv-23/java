package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发布作品的请求体。
 *
 * <p>媒体不是走 multipart 一起提交，而是先调上传接口拿到 URL，再随本对象提交，
 * 这样视频（几十 MB）和文字可以分开上传，失败时不用整单重传。</p>
 */
@Data
public class DatingWorkRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "给作品起个标题吧")
    @Size(max = 80, message = "标题最多 80 个字")
    private String title;

    @Size(max = 1000, message = "文案最多 1000 个字")
    private String content;

    /** 照片 / 视频 URL 列表，顺序即展示顺序；第一位若非视频则作为封面 */
    private List<String> images;

    /** 视频 URL（可选，最多一个） */
    private String videoUrl;

    /** BGM 地址（可选） */
    @Size(max = 500, message = "BGM 地址过长")
    private String bgmUrl;

    /** BGM 名称（可选） */
    @Size(max = 100, message = "BGM 名称过长")
    private String bgmName;
}
