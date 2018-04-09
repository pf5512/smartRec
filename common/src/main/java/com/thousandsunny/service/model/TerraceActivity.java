package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 */
@Data
@Entity
@Comment("平台活动")
@Table(name = "sr_terrace_activity")
public class TerraceActivity {

    private Long id;
    @Comment("活动类别")
    private TerraceActivityClass taClass;
    @Comment("活动名称")
    private String name;
    @Comment("开始时间")
    private Date startDate;
    @Comment("结束时间")
    private Date endDate;
    @Comment("活动内容")
    private String content;
    @Comment("发布时间")
    private Date date = new Date();
    @Comment("活动图片")
    private CloudFile logo;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public TerraceActivityClass getTaClass() {
        return taClass;
    }

    public CloudFile getLogo() {
        return logo;
    }

}
