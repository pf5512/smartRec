package com.thousandsunny.service.model;


import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 6/24/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Entity
@Comment("视频收藏列表")
@Table(name = "sr_video_collect")
public class VideoCollect {
    private Long id;
    @Comment("视频")
    private Video video;
    @Comment("收藏时间")
    private Date date;
    @Comment("收藏者")
    private Member member;
    @Comment("删除标记")
    private BooleanEnum collectEver = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getCollectEver() {
        return collectEver;
    }

    @OneToOne
    public Video getVideo() {
        return video;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

}
