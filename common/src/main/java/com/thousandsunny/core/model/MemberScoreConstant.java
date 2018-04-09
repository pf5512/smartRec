package com.thousandsunny.core.model;


import com.thousandsunny.common.entity.Comment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 5/4/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Entity
@Table(name = "core_member_score_constant")
public class MemberScoreConstant {

    private Long id;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @Comment("积分")
    private Integer score;
    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }

    @Comment("积分类型")
    private String scoreType;
    public String getScoreType() {
        return scoreType;
    }
    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }
}
