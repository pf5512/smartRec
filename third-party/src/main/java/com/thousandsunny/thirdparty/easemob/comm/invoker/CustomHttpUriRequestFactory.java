package com.thousandsunny.thirdparty.easemob.comm.invoker;

import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

/**
 * 如果这些代码有用，那它们是guitarist在02/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class CustomHttpUriRequestFactory extends HttpComponentsClientHttpRequestFactory {
    @Override
    protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
        return super.createHttpUriRequest(httpMethod, uri);
    }

    public static HttpUriRequest buildHttpUriRequest(HttpMethod httpMethod, URI uri) {
        return new CustomHttpUriRequestFactory().createHttpUriRequest(httpMethod, uri);
    }
}
