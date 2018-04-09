package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("反馈")
@Table(name = "sr_feed_back")
public class ShopFeedback {
    private Long id;
    @Comment("会员")
    private Member member;
    @Comment("被投诉会员")
    private Member badMan;
    private Shop shop;
    @Comment("内容")
    private String content;
    @Comment("时间")
    private Date date = new Date();
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getBadMan() {
        return badMan;
    }

    @OneToOne
    public Shop getShop() {
        return shop;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @Column(columnDefinition = ModuleKey.TEXT)
    public String getContent() {
        return content;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }
}

