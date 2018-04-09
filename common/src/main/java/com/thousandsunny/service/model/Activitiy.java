package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.TEXT;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("活动")
@Table(name = "sr_activities")
public class Activitiy {

    private Long id;
    @Comment("店铺")
    private Shop shop;
    @Comment("活动时间")
    private String name;
    @Comment("开始时间")
    private Date startDate;
    @Comment("结束时间")
    private Date endDate;
    private CloudFile logo;
    @Comment("发布时间")
    private Date date;
    @Comment("简介")
    private String content;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getLogo() {
        return logo;
    }

    @Column(columnDefinition = TEXT)
    public String getContent() {
        return content;
    }
}
