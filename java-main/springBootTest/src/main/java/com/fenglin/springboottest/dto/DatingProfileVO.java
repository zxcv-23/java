package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 嘉宾主页聚合数据：一次请求拿齐「资料 + 联系方式可见性 + 作品集」，
 * 避免前端进主页要发三四个请求。
 */
@Data
public class DatingProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 嘉宾资料（复用候选人视图，已脱敏） */
    private DatingCandidateVO profile;

    /** 是否是自己本人 */
    private Boolean mine;

    /** 是否已登录（未登录时联系方式一律隐藏） */
    private Boolean loggedIn;

    /** 联系方式是否对当前请求者可见 */
    private Boolean contactVisible;

    /** 联系方式（不可见时为 null，前端显示「发条私信后才可见」） */
    private DatingContactInfo contact;

    /** 作品列表（按时间倒序） */
    private List<DatingWorkVO> works;
}
