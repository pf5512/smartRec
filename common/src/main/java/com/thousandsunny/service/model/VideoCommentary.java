package com.thousandsunny.service.model;


import com.thousandsunny.cms.model.Commentary;
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
@Comment("视频评论")
@Table(name = "sr_video_commentary")
public class VideoCommentary {

    private Long id;
    @Comment("视频")
    private Video video;
    @Comment("日期")
    private Date date;
    @Comment("评论")
    private Commentary commentary;
    @Comment("会员")
    private Member member;
    @Comment("删除标记")
    private BooleanEnum favoriteEver = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getFavoriteEver() {
        return favoriteEver;
    }

    @OneToOne
    public Commentary getCommentary() {
        return commentary;
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
