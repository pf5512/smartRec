package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;

import com.thousandsunny.service.ModuleKey.PhotoType;

import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by 13336 on 2017/2/16.
 */
@Data
@Entity
@Comment("学校相册")
@Table(name = "sr_school_photo")
public class SchoolPhoto {

    private Long id;
    @Comment("学校")
    private School school;
    @Comment("照片类型")
    private PhotoType type;
    @Comment("照片")
    private CloudFile photo;
    @Comment("排序编号")
    private Integer number;
    @Comment("发布时间")
    private Date createTime = new Date();
    @Comment("是否启用")
    private BooleanEnum isEnable;
    @Comment("是否删除")
    private BooleanEnum isDelete;
    @Comment("文字说明")
    private String text;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public School getSchool() {
        return school;
    }

    @Enumerated(EnumType.STRING)
    public PhotoType getType() {
        return type;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public CloudFile getPhoto() {
        return photo;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsEnable() {
        return isEnable;
    }

    @Transient
    public Boolean getIsEnableBoolean() {
        return isEnable == BooleanEnum.YES;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Transient
    public Boolean getIsDeleteBoolean() {
        return isDelete == BooleanEnum.YES;
    }
}
