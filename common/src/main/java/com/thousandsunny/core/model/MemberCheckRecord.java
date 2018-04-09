package com.thousandsunny.core.model;


import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.*;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 4/27/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 * <p>
 */
@Data
@Entity
@Comment("考勤记录")
@Table(name = "core_member_check_record")
public class MemberCheckRecord {
    private Long id;
    @Comment("签到人")
    private Member member;
    @Comment("最后签到")
    private Date date;
    @Comment("签到图片")
    private CloudFile pic;
    @Comment("签到地点")
    private String location;
    @Comment("备注")
    private String remark;
    @Comment("是否迟到")
    private BooleanEnum isLate;
    @Comment("是否早退")
    private BooleanEnum isLeaveEarly;
    @Comment("是否外勤")
    private BooleanEnum outOffice;
    @Comment("时间类型")
    private DateType dateType;
    @Comment("签到类型")
    private CheckType type;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @OneToOne
    public CloudFile getPic() {
        return pic;
    }

    @Column(columnDefinition = ModuleKey.TEXT)
    public String getRemark() {
        return remark;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsLate() {
        return isLate;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsLeaveEarly() {
        return isLeaveEarly;
    }

    @Enumerated(STRING)
    public BooleanEnum getOutOffice() {
        return outOffice;
    }

    @Enumerated(STRING)
    public DateType getDateType() {
        return dateType;
    }

    @Enumerated(STRING)
    public CheckType getType() {
        return type;
    }
}
