package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/12/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("反馈")
@Table(name = "core_feed_back")
public class FeedBack {
    private Long id;
    @Comment("会员")
    private Member member;
    @Comment("联系电话")
    private String phoneNumber;
    @Comment("内容")
    private String content;
    @Comment("时间")
    private Date date = new Date();
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    @Comment("是否处理")
    private BooleanEnum isDeal = NO;
    @Comment("处理意见")
    public String opinion;


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Column(columnDefinition = ModuleKey.TEXT)
    public String getContent() {
        return content;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDeal() {
        return isDeal;
    }
}
