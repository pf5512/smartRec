package com.thousandsunny.service.model;


import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
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
@Comment("学校点赞点赞列表")
@Table(name = "sr_school_favorite")
public class SchoolFavorites {
    private Long id;
    @Comment("学校")
    private School school;
    @Comment("点赞时间")
    private Date favoriteDate;
    @Comment("点赞者")
    private Member member;
    private BooleanEnum favoriteEver;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public School getSchool() {
        return school;
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
