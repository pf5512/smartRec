package com.thousandsunny.common.entity;

import lombok.Data;
import org.springframework.data.domain.Sort;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Data
public class BackPageVo {
    private Integer rows = 10;
    private Integer page = 1;
    private String sidx;
    private String sord;

    public BackPageRequest pageRequest() {
        Direction d = "desc".equals(sord) ? DESC : ASC;
        if (isNotBlank(sidx)) {
            return new BackPageRequest(page, rows, new Sort(d, sidx));
        } else {
            return new BackPageRequest(page, rows);
        }

    }

    public BackPageRequest pageRequest(Direction direction, String... orders) {
        return new BackPageRequest(page, rows, new Sort(direction, orders));
    }

    public BackPageRequest descPageRequest(String... orders) {
        return new BackPageRequest(page, rows, new Sort(DESC, orders));
    }
}
