package com.thousandsunny.common.entity;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class BackPageRequest extends PageRequest {

    private static final long serialVersionUID = 2744982981308760412L;

    public BackPageRequest() {
        this(1, 20);
    }

    public BackPageRequest(Integer page, Integer size) {
        super(page - 1, size);
    }

    public BackPageRequest(int page, int size) {
        super(page - 1, size);
    }

    public BackPageRequest(int page, int size, Direction direction, String... properties) {
        super(page - 1, size, direction, properties);
    }

    public BackPageRequest(int page, int size, Sort sort) {
        super(page - 1, size, sort);
    }

    @Override
    public int getPageNumber() {
        return super.getPageNumber() + 1;
    }

    public int getFirstResult() {
        return (getPageNumber() - 1) * getPageSize();
    }

    public int getMaxResults() {
        return getPageSize();
    }

}
