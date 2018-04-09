package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("简历_培训经验")
@Table(name = "sr_train_exp")
public class ResumeTrainExp {

    private Long id;
    @Comment("培训机构名称")
    private String institutionName;
    @Comment("课程名称")
    private String courseName;
    @Comment("开始时间")
    private Date startDate;
    @Comment("结束时间")
    private Date endDate;
    @Comment("简历")
    private Resume resume;
    @Comment("培训证书")
    private List<CloudFile> certification;
    @Comment("培训课程")
    private Course course;
    @Comment("是否平台添加")
    private BooleanEnum isPlatformAdd;// 允许修改的为个人编辑的培训经验，不允许修改的为平台增加的培训经验


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Resume getResume() {
        return resume;
    }

    @OneToMany
    public List<CloudFile> getCertification() {
        return certification;
    }

    @OneToOne(fetch = LAZY)
    public Course getCourse() {
        return course;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsPlatformAdd() {
        return isPlatformAdd;
    }

    @Transient
    public Boolean getIsPlatformAddBoolean() {
        return isPlatformAdd == YES;
    }

    @Transient
    public String getReadableStartDate() {
        return isNotNull(startDate) ? ISO_DATE_FORMAT.format(startDate) : null;
    }

    @Transient
    public String getReadableEndDate() {
        return isNotNull(endDate) ? ISO_DATE_FORMAT.format(endDate) : null;
    }

}
