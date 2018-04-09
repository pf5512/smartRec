package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/11/16写的;
 * 如果没用，那我就不知道是谁写的了。
 * <p>
 * 公司
 */
@Data
@Entity
@Comment("公司")
@Table(name = "core_company")
public class Company {

    private Long id;
    @Comment("公司名称")
    private String name;
    @Comment("创建人")
    private Long creater;
    @Comment("部门")
    private List<Department> departments;
    @Comment("员工")
    private List<Member> members;
    @Comment("logo")
    private CloudFile logo;
    @Comment("二维码")
    private CloudFile qrCode;
    @Comment("公司是否可见/创建后是否已经审核通过")
    private BooleanEnum visiable;
    @Comment("上班时间 09:00:00")
    private String startTime;
    @Comment("下班时间 18:00:00")
    private String endTime;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToMany
    public List<Member> getMembers() {
        return members;
    }

    @OneToMany
    public List<Department> getDepartments() {
        return departments;
    }

    @OneToOne
    public CloudFile getLogo() {
        return logo;
    }

    @Enumerated(STRING)
    public BooleanEnum getVisiable() {
        return visiable;
    }

    @OneToOne
    public CloudFile getQrCode() {
        return qrCode;
    }
}
