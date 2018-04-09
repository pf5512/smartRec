package com.thousandsunny.thirdparty.easemob.comm.body;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Data
public class ChatGroupBody implements BodyWrapper {
    private String groupName;
    private String desc;
    private Boolean isPublic;
    private Long maxUsers;
    private Boolean approval;
    private String owner;
    private String[] members;

    public ChatGroupBody(String groupName, String desc, Boolean isPublic, Long maxUsers, Boolean approval, String owner, String[] members) {
        this.groupName = groupName;
        this.desc = desc;
        this.isPublic = isPublic;
        this.maxUsers = maxUsers;
        this.approval = approval;
        this.owner = owner;
        this.members = members;
    }

    public ContainerNode<?> getBody() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("groupname", groupName).put("desc", desc).put("public", isPublic).put("approval", approval).put("owner", owner);
        if(null != maxUsers) {
            body.put("maxusers", maxUsers);
        }
        if(ArrayUtils.isNotEmpty(members)) {
            ArrayNode membersNode = body.putArray("members");
            for (String member : members) {
                membersNode.add(member);
            }
        }

        return body;
    }

    public Boolean validate() {
        return StringUtils.isNotBlank(groupName) && StringUtils.isNotBlank(desc) && null != isPublic && null != approval && StringUtils.isNotBlank(owner);
    }
}
