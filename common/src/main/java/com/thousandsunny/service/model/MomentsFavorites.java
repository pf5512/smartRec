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
@Comment("说说点赞列表")
@Table(name = "sr_moments_favorite")
public class MomentsFavorites {
    private Long id;
    @Comment("说说")
    private Moments moments;
    @Comment("点赞时间")
    private Date favoriteDate;
    @Comment("点赞者")
    private Member member;
    @Comment("删除标记")
    private BooleanEnum favoriteEver=NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Moments getMoments() {
        return moments;
    }

    @Enumerated(STRING)
    public void getFavoriteEver(BooleanEnum favoriteEver) {
        this.favoriteEver = favoriteEver;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

}
