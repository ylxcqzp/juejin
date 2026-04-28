package com.juejin.common.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private Long total;
    private Integer page;
    private Integer size;
    private Long pages;
    private Boolean hasNext;
    private Long cursor;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setList(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage((int) page.getCurrent());
        result.setSize((int) page.getSize());
        result.setPages(page.getPages());
        result.setHasNext(page.getCurrent() < page.getPages());
        return result;
    }

    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        result.setHasNext((long) page * size < total);
        return result;
    }

    public static <T> PageResult<T> of(List<T> list, Long cursor) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setCursor(cursor);
        return result;
    }

}
