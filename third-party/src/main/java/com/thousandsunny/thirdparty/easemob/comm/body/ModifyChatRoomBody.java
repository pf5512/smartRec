package com.thousandsunny.thirdparty.easemob.comm.body;

import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import org.apache.commons.lang3.StringUtils;

public class ModifyChatRoomBody implements BodyWrapper {
    private String name;
    private String desc;
    private Long maxUsers;

    public ModifyChatRoomBody(String name, String desc, Long maxUsers) {
        this.name = name;
        this.desc = desc;
        this.maxUsers = maxUsers;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Long getMaxUsers() {
        return maxUsers;
    }

    public ContainerNode<?> getBody() {
        return JsonNodeFactory.instance.objectNode().put("name", name).put("description", desc).put("maxusers", maxUsers);
    }

    public Boolean validate() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(desc) && null != maxUsers;
    }
}
