package com.thousandsunny.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by admin on 2016/10/21.
 */
@Data
@Entity
@Comment("工作类别")
@Table(name="sr_job_type")
public class JobType {
    private Long id;
    @Comment("类别名称")
    private String name;
    @Comment("父类别")
    private JobType parentJobType;
//    @Comment("子类别")
//    private List<JobType> childJobTypes;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    @Comment("日期")
    private Date date;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    public JobType getParentJobType(){return parentJobType;}

//    @JsonIgnore
//    @OneToMany(cascade = ALL, mappedBy = "parentJobType")
//    public List<JobType> getChildJobTypes(){return childJobTypes;}

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

}
