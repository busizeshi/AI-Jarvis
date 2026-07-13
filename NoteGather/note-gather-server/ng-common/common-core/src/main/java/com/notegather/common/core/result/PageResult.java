package com.notegather.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应体
 *
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** 当前页码（从1开始） */
    private long page;
    /** 每页条数 */
    private long pageSize;
    /** 总条数 */
    private long total;
    /** 数据列表 */
    private List<T> records;

    public static <T> PageResult<T> empty(long page, long pageSize) {
        return PageResult.<T>builder()
                .page(page)
                .pageSize(pageSize)
                .total(0L)
                .records(Collections.emptyList())
                .build();
    }

    public static <T> PageResult<T> of(long page, long pageSize, long total, List<T> records) {
        return PageResult.<T>builder()
                .page(page)
                .pageSize(pageSize)
                .total(total)
                .records(records)
                .build();
    }

    /** 总页数 */
    public long getTotalPages() {
        if (pageSize == 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }
}
