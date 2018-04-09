package com.thousandsunny.thirdparty.easemob.api.impl;

import com.thousandsunny.thirdparty.easemob.api.EasemobRestAPI;
import com.thousandsunny.thirdparty.easemob.api.FileAPI;
import com.thousandsunny.thirdparty.easemob.comm.helper.HeaderHelper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.HeaderWrapper;

import java.io.File;

public class EasemobFile extends EasemobRestAPI implements FileAPI {
    private static final String ROOT_URI = "/chatfiles";

    @Override
    public String getResourceRootURI() {
        return ROOT_URI;
    }

    public Object uploadFile(Object file) {
        String url = getContext().getSeriveURL() + getResourceRootURI();
        HeaderWrapper header = HeaderHelper.getUploadHeaderWithToken();

        return getInvoker().uploadFile(url, header, (File) file);
    }

    public Object downloadFile(String fileUUID, String shareSecret, Boolean isThumbnail) {
        String url = getContext().getSeriveURL() + getResourceRootURI() + "/" + fileUUID;
        HeaderWrapper header = HeaderHelper.getDownloadHeaderWithToken(shareSecret, isThumbnail);

        return getInvoker().downloadFile(url, header, null);
    }
}
