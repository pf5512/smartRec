package com.thousandsunny.service.model;

import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.awt.print.PrinterAbortException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在11/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("课程")
@Table(name = "sr_course")
public class Course {

    private Long id;
    @Comment("名称")
    private String name;
    @Comment("课程介绍")
    private String introduce;
    @Comment("创建时间")
    private Date createTime = new Date();
    @Comment("刷新时间")
    private Date modifyTime = new Date();
    @Comment("标签:美容,美发,化妆......")
    private CmsTag tag;
    @Comment("学校")
    private School school;
    @Comment("是否平台合作")
    private BooleanEnum isPlatformCourse;
    @Comment("是否是职业规划课程")
    private BooleanEnum isEmploymentPlanning;
    @Comment("价格")
    private BigDecimal price;
    @Comment("平台分成比例")
    private Double platformPercent = 0D;//后台编辑时直接保存(1-100)
    @Comment("红包比例")
    private Double redPacketPercent = 0D;//后台编辑时直接保存(1-100)
    @Comment("课程天数")
    private Integer day;
    @Comment("课程相册")
    private List<CloudFile> photos;
    @Comment("报名时间")
    private List<CourseSignUp> courseSignUps;
    @Comment("是否删除")
    private BooleanEnum isDelete = BooleanEnum.NO;
    @Comment("是否启用")
    private BooleanEnum isEnable = BooleanEnum.YES;
    @Comment("课程排序")
    private Integer sortNO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public CmsTag getTag() {
        return tag;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsPlatformCourse() {
        return isPlatformCourse;
    }

    @Transient
    public Boolean getIsPlatformCourseBoolean() {
        return isPlatformCourse == BooleanEnum.YES;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsEmploymentPlanning() {
        return isEmploymentPlanning;
    }

    @Transient
    public Boolean getIsEmploymentPlanningBoolean() {
        return isEmploymentPlanning == BooleanEnum.YES;
    }

    @OneToOne(fetch = LAZY)
    public School getSchool() {
        return school;
    }

    @OneToMany
    public List<CloudFile> getPhotos() {
        return photos;
    }

    @OneToMany(mappedBy = "course")
    public List<CourseSignUp> getCourseSignUps() {
        return courseSignUps;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsEnable() {
        return isEnable;
    }

    @Transient
    public Boolean getIsEnableBoolean() {
        return isEnable == BooleanEnum.YES;
    }

}
