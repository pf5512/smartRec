package com.thousandsunny.cms.dto;

import com.thousandsunny.cms.model.Channel;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在8/1/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class IdNameList {
    private Long id;

    private String name;

    private List list;

    public IdNameList(Channel channel) {
        setId(channel.getId());
        setName(channel.getName());
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

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
