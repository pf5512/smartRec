package com.thousandsunny.thirdparty.easemob.api.impl;

import com.thousandsunny.thirdparty.easemob.api.AuthTokenAPI;
import com.thousandsunny.thirdparty.easemob.api.EasemobRestAPI;
import com.thousandsunny.thirdparty.easemob.comm.body.AuthTokenBody;
import com.thousandsunny.thirdparty.easemob.comm.constant.HTTPMethod;
import com.thousandsunny.thirdparty.easemob.comm.helper.HeaderHelper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.HeaderWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasemobAuthToken extends EasemobRestAPI implements AuthTokenAPI{
	
	public static final String ROOT_URI = "/token";
	
	private static final Logger log = LoggerFactory.getLogger(EasemobAuthToken.class);
	
	@Override
	public String getResourceRootURI() {
		return ROOT_URI;
	}

	public Object getAuthToken(String clientId, String clientSecret) {
		String url = getContext().getSeriveURL() + getResourceRootURI();
		BodyWrapper body = new AuthTokenBody(clientId, clientSecret);
		HeaderWrapper header = HeaderHelper.getDefaultHeader();
		
		return getInvoker().sendRequest(HTTPMethod.METHOD_POST, url, header, body, null);
	}
}
