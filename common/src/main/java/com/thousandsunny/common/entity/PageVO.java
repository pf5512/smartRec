package com.thousandsunny.common.entity;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Data
public class PageVO {

    private Integer pageSize = 10;

    private Integer pageNo = 0;

    private Direction direction;

    private String order;

    public PageVO() {
    }

    public PageVO(Integer pageSize, Integer pageNo) {
        this.pageSize = pageSize;
        this.pageNo = pageNo;
    }

    public PageRequest pageRequest() {
        if (isNull(direction))
            return new PageRequest(pageNo, pageSize);
        else
            return new PageRequest(pageNo, pageSize, new Sort(direction, isBlank(order) ? "date" : order));
    }

    public PageRequest pageRequest(Direction direction, String... orders) {
        return new PageRequest(pageNo, pageSize, new Sort(direction, orders));
    }

    public PageRequest descPageRequest(String... orders) {
        return new PageRequest(pageNo, pageSize, new Sort(DESC, orders));
    }

//    public CustomPageRequest getPageRequest() {
//        if (isNull(direction))
//            return new CustomPageRequest(pageNo, pageSize);
//        else
//            return new CustomPageRequest(pageNo, pageSize, new Sort(direction, order));
//    }
}
