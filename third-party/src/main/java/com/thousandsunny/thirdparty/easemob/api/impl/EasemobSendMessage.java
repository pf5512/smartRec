package com.thousandsunny.thirdparty.easemob.api.impl;

import com.thousandsunny.thirdparty.easemob.api.EasemobRestAPI;
import com.thousandsunny.thirdparty.easemob.api.SendMessageAPI;
import com.thousandsunny.thirdparty.easemob.comm.constant.HTTPMethod;
import com.thousandsunny.thirdparty.easemob.comm.helper.HeaderHelper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.HeaderWrapper;

public class EasemobSendMessage extends EasemobRestAPI implements SendMessageAPI {
    private static final String ROOT_URI = "/messages";

    @Override
    public String getResourceRootURI() {
        return ROOT_URI;
    }

    public Object sendMessage(Object payload) {
        String  url = getContext().getSeriveURL() + getResourceRootURI();
        HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
        BodyWrapper body = (BodyWrapper) payload;

        return getInvoker().sendRequest(HTTPMethod.METHOD_POST, url, header, body, null);
    }
}
