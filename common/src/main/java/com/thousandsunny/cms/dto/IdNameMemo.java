package com.thousandsunny.cms.dto;

import com.thousandsunny.cms.model.Role;

/**
 * 如果这些代码有用，那它们是guitarist在7/26/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class IdNameMemo {
    private Long id;

    private String name;
    private String memo;

    public IdNameMemo(Role role) {
        setId(role.getId());
        setName(role.getName());
        setMemo(role.getMemo());
    }

    public IdNameMemo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
