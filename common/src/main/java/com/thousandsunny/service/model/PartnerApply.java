package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import lombok.Data;
import org.junit.Test;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.common.lambda.LambdaUtil.joiner;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static java.util.Objects.isNull;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 如果这些代码有用，那它们是guitarist在21/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("合伙人申请")
@Table(name = "sr_partner_apply")
public class PartnerApply {

    private Long id;
    private Member member;
    @Comment("姓名")
    private String name;
    @Comment("手机号")
    private String mobile;
    @Comment("身份证号")
    private String idCardNo;
    @Comment("省份")
    private Region province;
    @Comment("城市")
    private Region city;
    @Comment("区")
    private Region area;
    @Comment("省份2")
    private Region province2;
    @Comment("城市2")
    private Region city2;
    @Comment("区2")
    private Region area2;
    @Comment("申请时间")
    private Date date = new Date();
    @Comment("更新时间")
    private Date updateDate = new Date();
    @Comment("身份证")
    private CloudFile idCard;
    @Comment("半身像")
    private CloudFile half;
    @Comment("认证状态")
    private ApplyEnum state;
    @Comment("入伙费")
    private BigDecimal joinMoney = new BigDecimal(0);
    @Comment("备注")
    private String notes;


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = LAZY)
    public Region getProvince() {
        return province;
    }

    @OneToOne(fetch = LAZY)
    public Region getCity() {
        return city;
    }

    @OneToOne(fetch = LAZY)
    public Region getArea() {
        return area;
    }

    @OneToOne(fetch = LAZY)
    public Region getProvince2() {
        return province2;
    }

    @OneToOne(fetch = LAZY)
    public Region getCity2() {
        return city2;
    }

    @OneToOne(fetch = LAZY)
    public Region getArea2() {
        return area2;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getIdCard() {
        return idCard;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getHalf() {
        return half;
    }

    @Enumerated(STRING)
    public ApplyEnum getState() {
        return state;
    }

    @Transient
    public String getRegionName(Region region) {
        if (!isNull(region))
            return region.getName();
        else
            return null;
    }

    @Transient
    public String getProvinceName() {
        return getRegionName(province);
    }

    @Transient
    public String getCityName() {
        return getRegionName(city);
    }

    @Transient
    public String getAreaName() {
        return getRegionName(area);
    }

    @Transient
    public String getProvince2Name() {
        return getRegionName(province2);
    }

    @Transient
    public String getCity2Name() {
        return getRegionName(city2);
    }

    @Transient
    public String getArea2Name() {
        return getRegionName(area2);
    }

    @Transient
    public String getPlace() {
        return getProvinceName() + getCityName() + getAreaName();
    }

    @Transient
    public String getApplyArea() {
        String area1 = joiner("-", getProvinceName(), getCityName(), getAreaName());
        String area2 = joiner("-", getProvince2Name(), getCity2Name(), getArea2Name());
        if (isNotBlank(area1) && isNotBlank(area2))
            return joiner(",", area1, area2);
        else if (isNotBlank(area1) && !isNotBlank(area2))
            return area1;
        else if (!isNotBlank(area1) && isNotBlank(area2))
            return area2;
        else return "";
    }

    @Test
    public void testGetApplyArea() {
        String area1 = joiner("-", 1 + "", 1 + "", 1 + "");
        String area2 = joiner("-", 2 + "", 2 + "", 2 + "");
        System.out.println(joiner(",", area1, area2));
    }
}
