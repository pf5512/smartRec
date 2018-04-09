package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Region;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.service.ModuleKey.FindJobState;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("简历_工作意向")
@Table(name = "sr_resume_intention")
public class ResumeIntention {
    private Long id;
    @Comment("工作类型")
    private List<JobType> jobTypes;
    @Comment("薪资待遇")
    private Long salary;
    @Comment("省份")
    private Region province;
    @Comment("城市")
    private Region city;
    @Comment("区")
    private Region area;
    @Comment("地址")
    private String address;
    private Integer workYear;
    private FindJobState findJobState;
    private String type;
    @Comment("编辑保存时间")
    private Date date = new Date();

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }


    @ManyToMany
    public List<JobType> getJobTypes() {
        return jobTypes;
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

    @Enumerated(STRING)
    public FindJobState getFindJobState() {
        return findJobState;
    }
}
