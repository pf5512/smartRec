package com.thousandsunny.service.model;

import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("简历")
@Table(name = "sr_resume")
public class Resume {
    private Long id;
    private List<CmsTag> spots;
    private Member member;
    @Comment("工作意向")
    private ResumeIntention intention;
    @Comment("工作经验")
    private List<ResumeWorkExp> workExps;
    @Comment("训练经验")
    private List<ResumeTrainExp> trainExps;
    private JobConstant period;
    @Comment("修改时间")
    private Date modify = new Date();
    private CloudFile avatar;
    @Comment("刷新时间")
    private Date fresh = new Date();
    private BooleanEnum auth;
    private String highlights;
    @Comment("经度")
    private Double longitude;
    @Comment("纬度")
    private Double latitude;

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
    public CloudFile getAvatar() {
        return avatar;
    }

    @OneToOne(fetch = LAZY)
    public ResumeIntention getIntention() {
        return intention;
    }

    @OneToMany(mappedBy = "resume")
    public List<ResumeWorkExp> getWorkExps() {
        return workExps;
    }

    @OneToMany(mappedBy = "resume")
    public List<ResumeTrainExp> getTrainExps() {
        return trainExps;
    }

    @OneToOne(fetch = LAZY)
    public JobConstant getPeriod() {
        return period;
    }

    @Enumerated(STRING)
    public BooleanEnum getAuth() {
        return auth;
    }

    @OneToMany
    public List<CmsTag> getSpots() {
        return spots;
    }
}
