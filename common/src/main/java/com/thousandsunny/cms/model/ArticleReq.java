package com.thousandsunny.cms.model;

import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * 如果这些代码有用，那它们是guitarist在7/31/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class ArticleReq {
    private String channelClassy;
    private MultipartFile coverImageFile;
    private MultipartFile rawDownFile;
    private String arrId;
    private String arrText;
    private Date publishTime_;

    public String getChannelClassy() {
        return channelClassy;
    }

    public void setChannelClassy(String channelClassy) {
        this.channelClassy = channelClassy;
    }

    public MultipartFile getCoverImageFile() {
        return coverImageFile;
    }

    public void setCoverImageFile(MultipartFile coverImageFile) {
        this.coverImageFile = coverImageFile;
    }

    public MultipartFile getRawDownFile() {
        return rawDownFile;
    }

    public void setRawDownFile(MultipartFile rawDownFile) {
        this.rawDownFile = rawDownFile;
    }

    public String getArrId() {
        return arrId;
    }

    public void setArrId(String arrId) {
        this.arrId = arrId;
    }

    public String getArrText() {
        return arrText;
    }

    public void setArrText(String arrText) {
        this.arrText = arrText;
    }

    public Date getPublishTime_() {
        return publishTime_;
    }

    public void setPublishTime_(String publishTime_) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATETIME_FORMAT.getPattern());
        try {
            this.publishTime_ = sdf.parse(publishTime_);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
