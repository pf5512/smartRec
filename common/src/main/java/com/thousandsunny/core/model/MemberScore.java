package com.thousandsunny.core.model;



import com.thousandsunny.common.entity.Comment;

import javax.persistence.*;

import static javax.persistence.GenerationType.AUTO;


/**
 * Created by guitarist on 4/11/16.
 */

@Entity
@Table(name = "core_member_score")
public class MemberScore {

    private Long id;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @Comment("用户")
    private Member member;
    @OneToOne
    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }

    @Comment("会员积分")
    private Integer score = new Integer(0);
    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }

    @Comment("本次阅读获取的积分")
    private Integer myScore;
    @Transient
    public Integer getMyScore(){
        return myScore;
    }
    public void setMyScore(Integer myScore) {
        this.myScore = myScore;
    }

}
