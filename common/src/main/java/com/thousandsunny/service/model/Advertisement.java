package com.thousandsunny.service.model;

import com.thousandsunny.cms.model.Article;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey.AdCategoryEnum;
import com.thousandsunny.service.ModuleKey.AdShowWayEnum;
import com.thousandsunny.service.ModuleKey.AdStatusEnum;
import com.thousandsunny.service.ModuleKey.AdTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2016/11/23.
 */
@Data
@NoArgsConstructor
@Entity
@Comment("广告")
@Table(name = "sr_advertisement")
public class Advertisement {
    private Long id;
    @Comment("名称")
    private String name;
    @Comment("广告类别")
    private AdCategoryEnum category;
    @Comment("广告属性")
    private AdStatusEnum status;
    @Comment("广告类型,属性为内部广告才有")
    private AdTypeEnum type;
    @Comment("广告展示途径,属性为外部链接才有")
    private AdShowWayEnum showWay;
    @Comment("广告链接地址，属性为外部链接才有")
    private String link;
    @Comment("省")
    private Region province;
    @Comment("市")
    private Region city;
    @Comment("区")
    private Region area;
    @Comment("排序权重")
    private Long weight;
    @Comment("开始时间")
    private Date startTime;
    @Comment("结束时间")
    private Date endTime;
    @Comment("是否启用")
    private BooleanEnum valid;
    @Comment("图片")
    private CloudFile pic;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    //广告对应类型所对应下面对象
    @Comment("会员")
    private Member member;
    @Comment("店铺")
    private Shop shop;
    @Comment("岗位")
    private Job job;
    @Comment("咨讯")
    private Article article;
    @Comment("视频")
    private Video video;
    @Comment("学校")
    private School school;
    @Comment("课程")
    private Course course;
    //TODO:缺少卡劵，卡劵类还没有定义，后期补充

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public AdCategoryEnum getCategory() {
        return category;
    }

    @Enumerated(STRING)
    public AdStatusEnum getStatus() {
        return status;
    }

    @Enumerated(STRING)
    public AdTypeEnum getType() {
        return type;
    }

    @Enumerated(STRING)
    public AdShowWayEnum getShowWay() {
        return showWay;
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
    public BooleanEnum getValid() {
        return valid;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getPic() {
        return pic;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @OneToOne(fetch = LAZY)
    public Job getJob() {
        return job;
    }

    @OneToOne(fetch = LAZY)
    public Article getArticle() {
        return article;
    }

    @OneToOne(fetch = LAZY)
    public Video getVideo() {
        return video;
    }

    @OneToOne(fetch = LAZY)
    public School getSchool() {
        return school;
    }

    @OneToOne(fetch = LAZY)
    public Course getCourse() {
        return course;
    }

}
