package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.MemberMsgType;
import static javax.persistence.EnumType.STRING;

/**
 * Created by guitarist on 4/25/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@MappedSuperclass
public class BaseMsg {

    @Comment("消息接受者")
    private Member receiver;

    @Comment("消息标题")
    private String title;

    @Comment("消息简介")
    private String summary;

    @Comment("消息链接")
    private String url;

    @Comment("推送时间")
    private Date date = new Date();

    @Comment("是否已读")
    private BooleanEnum isRead = BooleanEnum.NO;

    @Comment("置顶")
    private BooleanEnum isTop = BooleanEnum.NO;

    @Comment("勿扰")
    private BooleanEnum isNoDisturb = BooleanEnum.NO;

    @Comment("logo")
    private CloudFile pic;

    @Comment("消息类型")
    private MemberMsgType type;

    @Comment("内容")
    private String content;

    @Comment("消息数量")
    private Integer messageCount=0;

    @OneToOne
    @JsonIgnore
    public Member getReceiver() {
        return receiver;
    }

    @Column(columnDefinition = ModuleKey.TEXT)
    public String getContent() {
        return content;
    }

    @Enumerated(STRING)
    public MemberMsgType getType() {
        return type;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsTop() {
        return isTop;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsRead() {
        return isRead;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsNoDisturb() {
        return isNoDisturb;
    }

    @OneToOne
    public CloudFile getPic() {
        return pic;
    }

    @Transient
    public Boolean getIsTopBoolean() {
        return isTop == BooleanEnum.YES ? true : false;
    }

    @Transient
    public Boolean getIsNoDisturbBoolean() {
        return isNoDisturb == BooleanEnum.YES ? true : false;
    }

    @Transient
    public Boolean getIsReadBoolean() {
        return isRead == BooleanEnum.YES ? true : false;
    }
}
