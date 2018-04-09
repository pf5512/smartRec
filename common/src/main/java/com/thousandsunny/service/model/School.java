package com.thousandsunny.service.model;

import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Delayed;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在11/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("培训学校")
@Table(name = "sr_school")
public class School {

    private Long id;
    @Comment("名称")
    private String name;
    @Comment("标签:美容,美发,化妆......")
    private List<CmsTag> tags;
    @Comment("课程")
    private List<Course> courses;
    @Comment("地点")
    private String address;
    @Comment("负责人")
    private Member member;
    @Comment("经度")
    private Double longitude;
    @Comment("纬度")
    private Double latitude;
    @Comment("省")
    private Region province;
    @Comment("市")
    private Region city;
    @Comment("区")
    private Region area;
    @Comment("创建时间")
    private Date date = new Date();
    @Comment("官网链接")
    private String link;
    @Comment("是否合作学校")
    private BooleanEnum isPartSchool;
    @Comment("联系电话")
    private String telephone;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToMany
    public List<CmsTag> getTags() {
        return tags;
    }

    @OneToMany(mappedBy = "school")
    public List<Course> getCourses() {
        return courses;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne
    public Region getProvince() {
        return province;
    }

    @OneToOne
    public Region getCity() {
        return city;
    }

    @OneToOne
    public Region getArea() {
        return area;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsPartSchool() {
        return isPartSchool;
    }

    @Transient
    public Boolean getIspartSchoolBoolean() {
        return isPartSchool == YES;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsDelete(){
        return isDelete;
    }
}
