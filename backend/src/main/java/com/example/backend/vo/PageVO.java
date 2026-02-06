package com.example.backend.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private List<T> records;

    private long page;

    private long size;

    private long total;

    public static <T> PageVO<T> of(List<T> records, long page, long size, long total) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(records);
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(total);
        return vo;
    }
}
