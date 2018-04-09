package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.TalkType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/10/16写的;
 * 如果没用，那我就不知道是谁写的了。
 * <p>
 * 聊天
 */
@Data
@Entity
@Comment("群聊")
@Table(name = "core_group")
public class Group {

    private Long id;
    @Comment("群聊类型")
    private TalkType type;
    @Comment("环信id")
    private String hxGroupId;
    @Comment("群聊名称")
    private String name;
    @Comment("群主")
    private MemberGroup chairMan;
    @Comment("最后发送人")
    private Member lastSender;
    @Comment("会员")
    private List<MemberGroup> memberGroups;
    @Comment("创建日期")
    private Date date = new Date();
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public TalkType getType() {
        return type;
    }

    @OneToMany(mappedBy = "group")
    public List<MemberGroup> getMemberGroups() {
        return memberGroups;
    }

    @OneToOne
    public Member getLastSender() {
        return lastSender;
    }

    @OneToOne
    public MemberGroup getChairMan() {
        return chairMan;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Transient
    public Boolean getIsDeleteBoolean() {
        return isDelete == YES;
    }

}
