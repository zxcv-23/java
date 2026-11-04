package com.fenglin.springboottest.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页返回结构
 *
 * <pre>
 * { "list": [...], "total": 42, "page": 1, "size": 10, "pages": 5 }
 * </pre>
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> list;

    /** 符合条件的总条数 */
    private Long total;

    /** 当前页码（从 1 开始） */
    private Integer page;

    /** 每页条数 */
    private Integer size;

    /** 总页数 */
    private Integer pages;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setPages(size <= 0 ? 0 : (int) ((total + size - 1) / size));
        return result;
    }
}
