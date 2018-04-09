package com.thousandsunny.core.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.common.RandomNumberUtil.randomUUIDString;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.GenderEnum;
import static java.util.Objects.isNull;
import static javax.persistence.EnumType.STRING;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

/**
 * 用户表
 */
@Data
@MappedSuperclass
public abstract class Human {

    private String token = randomUUIDString();
    @Comment("用户名")
    private String username;
    @Comment("MD5密码")
    private String password;
    @Comment("密码盐")
    private String salt;
    @Comment("真实姓名")
    private String realName;
    @Comment("性别")
    private GenderEnum gender;
    @Comment("出生年月")
    private Date birthday;
    @Comment("身高")
    private String height;
    @Comment("城市")
    private Region city;
    @Comment("省份")
    private Region province;
    @Comment("区")
    private Region area;
    @Comment("年龄")
    private Integer age;
    @Comment("体重")
    private String weight;
    @Comment("手机")
    private String mobile;
    @Comment("固定电话")
    private String telephone;
    @Comment("邮箱")
    private String email;
    @Comment("QQ")
    private String qq;
    @Comment("是否启用")
    private BooleanEnum valid = YES;
    @Comment("个人简介")
    private String resume;
    private Date modifyTime = new Date();
    private Date createTime = new Date();

    @Enumerated(STRING)
    public GenderEnum getGender() {
        return gender;
    }

    @JsonIgnore
    public String getSalt() {
        return salt;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Enumerated(value = STRING)
    public BooleanEnum getValid() {
        return valid;
    }

    @Transient
    public Boolean getValidBoolean(){
        return valid == YES;
    }

    @Column(updatable = false)
    public Date getCreateTime() {
        return createTime;
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

    @Column(unique = true)
    public String getMobile() {
        return mobile;
    }

    @Transient
    public String getReadableCreateTime() {
        return isNull(createTime) ? null : ISO_DATE_FORMAT.format(createTime);
    }

    @Transient
    public String getReadableBirthday() {
        return isNull(birthday) ? null : ISO_DATE_FORMAT.format(birthday);
    }

}
