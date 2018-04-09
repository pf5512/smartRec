package com.thousandsunny.core.domain.Controller.dto;

import com.thousandsunny.core.model.Org;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;

/**
 * 如果这些代码有用，那它们是guitarist在8/8/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class IdNameChildChannel {

    private Long id;

    private String name;
    private Integer orderCode;
    private List<IdNameChildChannel> childChannels;

    private BooleanEnum isDelete;
    public IdNameChildChannel(Org org) {
        setId(org.getId());
        setName(org.getName());
        setOrderCode(org.getOrderCode());
        setIsDelete(org.getIsDelete());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Integer orderCode) {
        this.orderCode = orderCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IdNameChildChannel> getChildChannels() {
        return childChannels;
    }

    public void setChildChannels(List<IdNameChildChannel> childChannels) {
        this.childChannels = childChannels;
    }

    public BooleanEnum getIsDelete() {
        return NO;
    }

    public void setIsDelete(BooleanEnum isDelete) {
        this.isDelete = isDelete;
    }
}
