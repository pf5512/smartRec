package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import static com.thousandsunny.core.ModuleKey.AccountEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static java.util.Objects.isNull;
import static javax.persistence.EnumType.STRING;

/**
 * 如果这些代码有用，那它们是guitarist在03/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@MappedSuperclass
public class BaseMember extends Human {

    private BooleanEnum isDelete = NO;
    @Comment("头像")
    private CloudFile headImage;
    @Comment("部门")
    private Department department;
    @Comment("公司")
    private Company company;
    @Comment("工作,职位")
    private Position position;
    @Comment("微信openId")
    private String wxOpenId;
    @Comment("电话免打扰")
    private BooleanEnum isNoDisturb = NO;

    @OneToOne
    public Position getPosition() {
        return position;
    }

    @OneToOne
    public Company getCompany() {
        return company;
    }

    @OneToOne
    public CloudFile getHeadImage() {
        return headImage;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @JsonIgnore
    @OneToOne
    public Department getDepartment() {
        return department;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsNoDisturb() {
        return isNoDisturb;
    }
}
