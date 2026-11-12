package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 交友联系方式（对外视图）。
 * 只在通过可见性校验后才会被填充，未校验通过时整个对象为 null。
 */
@Data
public class DatingContactInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String wechat;

    private String qq;

    private String phone;

    private String other;

    /** 0-仅私信过来往的人可见 1-所有登录用户可见 */
    private Integer visible;
}
