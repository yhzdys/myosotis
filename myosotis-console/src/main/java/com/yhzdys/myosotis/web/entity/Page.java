package com.yhzdys.myosotis.web.entity;

import java.util.ArrayList;
import java.util.List;

public class Page {

    private static final Page empty_page = new Page(new ArrayList<>(), 1, 0L, true);

    private List<?> list;
    private Integer page;
    private Long count;
    private Boolean end;

    public Page(List<?> list, Integer page, Long count, Boolean end) {
        this.list = list;
        this.page = page;
        this.count = count;
        this.end = end;
    }

    public static Page empty() {
        return empty_page;
    }

    public List<?> getList() {
        return list;
    }

    public void setList(List<?> list) {
        this.list = list;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Boolean getEnd() {
        return end;
    }

    public void setEnd(Boolean end) {
        this.end = end;
    }
}
