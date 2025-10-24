package io.dipcoin.sui.perp.model;

import lombok.Data;

import java.util.List;

/**
 * @author : Same
 * @datetime : 2025/10/24 09:56
 * @Description : page response
 */
@Data
public class PageResponse<T> {

    /**
     * data list
     */
    private List<T> data;

    /**
     * total record count
     */
    private Long total;

    /**
     * current page number
     */
    private Integer pageNum;

    /**
     * page size
     */
    private Integer pageSize;

    /**
     * total pages
     */
    private Integer totalPages;

}
