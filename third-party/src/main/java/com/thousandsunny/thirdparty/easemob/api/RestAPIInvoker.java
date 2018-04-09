package com.thousandsunny.thirdparty.easemob.api;

import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.HeaderWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.QueryWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.ResponseWrapper;

import java.io.File;

public interface RestAPIInvoker {
	ResponseWrapper sendRequest(String method, String url, HeaderWrapper header, BodyWrapper body, QueryWrapper query);
	ResponseWrapper uploadFile(String url, HeaderWrapper header, File file);
    ResponseWrapper downloadFile(String url, HeaderWrapper header, QueryWrapper query);
}
