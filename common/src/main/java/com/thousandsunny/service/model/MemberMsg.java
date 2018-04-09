package com.thousandsunny.service.model;


import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.ModuleKey.RemindMsgType;
import com.thousandsunny.core.model.BaseMsg;
import com.thousandsunny.core.model.FriendApply;
import com.thousandsunny.core.model.Group;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
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
@Comment("会员消息")
@Table(name = "sr_member_msg")
public class MemberMsg extends BaseMsg {

    private Long id;
    @Comment("单聊")
    private Group singleTalk;
    @Comment("群聊")
    private Group groupTalk;
    @Comment("好友申请")
    private FriendApply friendApply;
    @Comment("说说at")
    private Moments moments;
    @Comment("岗位")
    private Job job;
    @Comment("评论")
    private Commentary commentary;
    @Comment("点赞")
    private MomentsFavorites momentsFavorites;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    @Comment("是否最新")
    private BooleanEnum isNew = YES;//保证同一个接收人同一种类型只有一条最新
    @Comment("提醒类型")
    private RemindMsgType remindType;
    @Comment("岗位推荐")
    private JobApplyRecord jobApplyRecord;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Group getSingleTalk() {
        return singleTalk;
    }

    @OneToOne
    public Group getGroupTalk() {
        return groupTalk;
    }

    @OneToOne
    public FriendApply getFriendApply() {
        return friendApply;
    }

    @OneToOne
    public Moments getMoments() {
        return moments;
    }

    @OneToOne
    public Commentary getCommentary() {
        return commentary;
    }

    @OneToOne
    public MomentsFavorites getMomentsFavorites() {
        return momentsFavorites;
    }

    @OneToOne
    public Job getJob() {
        return job;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsNew() {
        return isNew;
    }

    @Enumerated(STRING)
    public RemindMsgType getRemindType(){
        return remindType;
    }
    
    @OneToOne
    public JobApplyRecord getJobApplyRecord(){
        return jobApplyRecord;
    }

}
