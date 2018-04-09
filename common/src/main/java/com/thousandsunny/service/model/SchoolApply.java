package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.core.ModuleKey.ApplyState;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.AUTO;

/**
 * 学校申请表
 * Created by mu.jie on 2017/2/13.
 */
@Data
@Entity
@Comment("培训学校")
@Table(name = "sr_school_apply")
public class SchoolApply {
    private Long id;
    @Comment("学校名称")
    private String name;
    @Comment("申请时间")
    private Date createTime = new Date();
    @Comment("填写姓名")
    private String userName;
    @Comment("填写电话号码")
    private String phoneNumber;
    @Comment("申请人")
    private Member member;
    @Comment("省")
    private Region province;
    @Comment("市")
    private Region city;
    @Comment("区")
    private Region area;
    @Comment("地址")
    private String address;
    @Comment("学校网址")
    private String webSiteUrl;
    @Comment("环境照片")
    private List<CloudFile> photos;
    @Comment("审核状态")
    private ApplyState state;//AGREE("接受"), REJECT("拒绝"), APPROVAL("审核")
    @Comment("审核备注")
    private String remark;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
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

    @OneToMany
    public List<CloudFile> getPhotos() {
        return photos;
    }

    @Enumerated(EnumType.STRING)
    public ApplyState getState() {
        return state;
    }

}
