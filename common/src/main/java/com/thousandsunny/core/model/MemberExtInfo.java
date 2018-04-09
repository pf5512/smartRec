package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.PhoneType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Table(name = "core_member_ext_info")
public class MemberExtInfo {
    private Long id;
    @Comment("会员")
    private Member member;
    @Comment("app是否激活")
    private Boolean isActivationAPP = TRUE;
    @Comment("微信是否激活")
    private Boolean isActivationWX = FALSE;
    @Comment("推荐人")
    private Member recommendUser;
    @Comment("身份证号")
    private String idCardNo;
    @Comment("身份证")
    private CloudFile idCard;
    @Comment("半身像")
    private CloudFile half;
    @Comment("注册时间")
    private Date registerTime = new Date();
    @Comment("最后访问时间")
    private Date lastVisitTime = new Date();
    @Comment("访问总次数")
    private Integer visitCount = 0;
    @Comment("上次登录IP")
    private String lastIp;
    @Comment("上次登录时间")
    private Date lastLoginTime = new Date();
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
    @Comment("旗下有多少人成为创业者")
    private Integer entrepreneursCount = 0;
    @Comment("旗下有多少高级创业者")
    private Integer seniorEntrepreneursCount = 0;
    @Comment("旗下有多少初级创业者")
    private Integer juniorEntrepreneursCount = 0;
    @Comment("注册来源：IOS")
    private PhoneType phoneType;
    @Comment("注册设备号")
    private String version;


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
    public Member getRecommendUser() {
        return recommendUser;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getIdCard() {
        return idCard;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getHalf() {
        return half;
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

    @Enumerated(EnumType.STRING)
    public PhoneType getPhoneType() {
        return phoneType;
    }
}
