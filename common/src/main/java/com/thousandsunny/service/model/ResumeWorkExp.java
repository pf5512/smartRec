package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("简历_工作经验")
@Table(name = "sr_work_exp")
public class ResumeWorkExp {

    private Long id;
    @Comment("店铺名称")
    private String shopName;
    @Comment("工作名称")
    private String positionName;
    @Comment("开始时间")
    private Date startDate;
    @Comment("结束时间")
    private Date endDate;
    @Comment("具体描述")
    private String description;
    @Comment("简历")
    private Resume resume;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Resume getResume() {
        return resume;
    }

    @Transient
    public String getReadableStartDate() {
        return isNotNull(startDate) ? ISO_DATE_FORMAT.format(startDate) : null;
    }

    @Transient
    public String getReadableEndDate() {
        return isNotNull(endDate) ? ISO_DATE_FORMAT.format(endDate) : null;
    }

    @Column(columnDefinition = TEXT)
    public String getDescription() {
        return description;
    }
}
