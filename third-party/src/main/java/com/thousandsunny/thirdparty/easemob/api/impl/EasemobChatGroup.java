package com.thousandsunny.thirdparty.easemob.api.impl;

import com.thousandsunny.thirdparty.easemob.api.ChatGroupAPI;
import com.thousandsunny.thirdparty.easemob.api.EasemobRestAPI;
import com.thousandsunny.thirdparty.easemob.comm.body.ModifyChatGroupBody;
import com.thousandsunny.thirdparty.easemob.comm.constant.HTTPMethod;
import com.thousandsunny.thirdparty.easemob.comm.helper.HeaderHelper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.HeaderWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.QueryWrapper;
import org.apache.commons.lang3.StringUtils;

import static com.thousandsunny.thirdparty.easemob.comm.constant.HTTPMethod.METHOD_DELETE;
import static com.thousandsunny.thirdparty.easemob.comm.constant.HTTPMethod.METHOD_GET;
import static com.thousandsunny.thirdparty.easemob.comm.constant.HTTPMethod.METHOD_POST;
import static java.lang.System.currentTimeMillis;

public class EasemobChatGroup extends EasemobRestAPI implements ChatGroupAPI {
    private static final String ROOT_URI = "/chatgroups";

    public Object getChatGroups(Long limit, String cursor) {
        String url = getContext().getSeriveURL() + getResourceRootURI();
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        QueryWrapper query = QueryWrapper.newInstance().addLimit(limit).addCursor(cursor);

        return getInvoker().sendRequest(METHOD_GET, url, header, null, query);
    }

    public Object getChatGroupDetails(String[] groupIds) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + StringUtils.join(groupIds, ",");
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_GET, url, header, null, null);
    }

    public Object createChatGroup(Object payload) {
        String url = getContext().getSeriveURL() + getResourceRootURI();
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = (BodyWrapper) payload;

        return getInvoker().sendRequest(METHOD_POST, url, header, body, null);
    }

    public Object modifyChatGroup(String groupId, Object payload) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = (BodyWrapper) payload;

        return getInvoker().sendRequest(HTTPMethod.METHOD_PUT, url, header, body, null);
    }

    public Object deleteChatGroup(String groupId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_DELETE, url, header, null, null);
    }

    public Object getChatGroupUsers(String groupId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/users";
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_GET, url, header, null, null);
    }

    public Object addSingleUserToChatGroup(String groupId, String userId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/users/" + userId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = new ModifyChatGroupBody("" + currentTimeMillis(), "ASC", 200l);
        return getInvoker().sendRequest(METHOD_POST, url, header, body, null);
    }

    public Object addBatchUsersToChatGroup(String groupId, Object payload) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/users";
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = (BodyWrapper) payload;

        return getInvoker().sendRequest(METHOD_POST, url, header, body, null);
    }

    public Object removeSingleUserFromChatGroup(String groupId, String userId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/users/" + userId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_DELETE, url, header, null, null);
    }

    public Object removeBatchUsersFromChatGroup(String groupId, String[] userIds) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/users/" + StringUtils.join(userIds, ",");
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = new ModifyChatGroupBody("" + currentTimeMillis(), "ASC", 200l);
        return getInvoker().sendRequest(METHOD_DELETE, url, header, body, null);
    }

    public Object transferChatGroupOwner(String groupId, Object payload) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = (BodyWrapper) payload;

        return getInvoker().sendRequest(HTTPMethod.METHOD_PUT, url, header, body, null);
    }

    public Object getChatGroupBlockUsers(String groupId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/blocks/users";
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_GET, url, header, null, null);
    }

    public Object addSingleBlockUserToChatGroup(String groupId, String userId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/blocks/users/" + userId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_POST, url, header, null, null);
    }

    public Object addBatchBlockUsersToChatGroup(String groupId, Object payload) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/blocks/users";
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = (BodyWrapper) payload;

        return getInvoker().sendRequest(METHOD_POST, url, header, body, null);
    }

    public Object removeSingleBlockUserFromChatGroup(String groupId, String userId) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/blocks/users/" + userId;
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_DELETE, url, header, null, null);
    }

    public Object removeBatchBlockUsersFromChatGroup(String groupId, String[] userIds) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + groupId + "/blocks/users/" + StringUtils.join(userIds, ",");
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();

        return getInvoker().sendRequest(METHOD_DELETE, url, header, null, null);
    }

    @Override
    public String getResourceRootURI() {
        return ROOT_URI;
    }
}
