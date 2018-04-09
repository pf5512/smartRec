package com.thousandsunny.cms.dto;

import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.Comment;

import static com.thousandsunny.cms.ModuleKey.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * 如果这些代码有用，那它们是guitarist在8/22/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class TypicalChannel {
    private String name;

    @Comment("页面类型")
    private ChannelTypeEnum channelType;

    @Comment("页面属性")
    private ContentType contentType;

    private Boolean hasImg;

    private ImgDirection imgDirection;

    public TypicalChannel(Channel channel) {
        setName(channel.getName());
        setChannelType(channel.getChannelType());
        setContentType(channel.getContentType());
        setHasImg(channel.getHasImg() == YES);
        setImgDirection(channel.getImgDirection());
    }

    public ImgDirection getImgDirection() {
        return imgDirection;
    }

    public void setImgDirection(ImgDirection imgDirection) {
        this.imgDirection = imgDirection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelTypeEnum getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelTypeEnum channelType) {
        this.channelType = channelType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public Boolean getHasImg() {
        return hasImg;
    }

    public void setHasImg(Boolean hasImg) {
        this.hasImg = hasImg;
    }
}
