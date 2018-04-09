package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 4/25/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Entity
@Table(name = "core_user_msg")
public class UserMsg {

    private Long id;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Comment("消息接受者")
    private User receiver;

    @OneToOne
    @JsonIgnore
    public User getReceiver() {
        return receiver;
    }

    @Comment("消息标题")
    private String title;

    public String getTitle() {
        return title;
    }

    @Comment("消息链接")
    private String url;

    public String getUrl() {
        return url;
    }

    @Comment("推送时间")
    private Date publishTime;

    public Date getPublishTime() {
        return publishTime;
    }

    @Comment("是否已读")
    private BooleanEnum isRead;

    @Enumerated(STRING)
    public BooleanEnum getIsRead() {
        return isRead;
    }
}
