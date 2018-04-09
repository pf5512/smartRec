package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.PhoneType;
import com.thousandsunny.core.ModuleKey.VisitState;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2016/12/22.
 */
@Data
@Entity
@Comment("会员访问次数")
@Table(name = "core_member_visit")
public class MemberVisit {
    private Long id;
    @Comment("访问时间")
    private Date createTime;
    @Comment("访问人")
    private Member member;
    @Comment("访问设备")
    private String deviceVersion;
    @Comment("访问次数")
    private Integer visitCount = 0;
    @Comment("访问平台")
    private PhoneType platformType;
    @Comment("访问间隔")
    private VisitState type;
    @Comment("最后访问ip")
    private String ip;
    @Comment("城市")
    private Region city;
    @Comment("省份")
    private Region province;
    @Comment("区")
    private Region area;
    @Comment("经度")
    private Double lng;
    @Comment("纬度")
    private Double lat;
    @Comment("是否是美业人")
    private BooleanEnum isMeiyeren = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @Enumerated(STRING)
    public PhoneType getPlatformType() {
        return platformType;
    }

    @Enumerated(STRING)
    public VisitState getType() {
        return type;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsMeiyeren(){
        return isMeiyeren;
    }

    @OneToOne
    public Region getCity() {
        return city;
    }

    @OneToOne
    public Region getProvince() {
        return province;
    }

    @OneToOne
    public Region getArea() {
        return area;
    }
}
