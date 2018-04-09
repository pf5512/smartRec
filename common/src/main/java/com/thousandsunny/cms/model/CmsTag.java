package com.thousandsunny.cms.model;

import com.thousandsunny.cms.ModuleKey.TagType;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;

/**
 * Created by guitarist on 5/10/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Entity
@Table(name = "cms_tag")
public class CmsTag {
    private Long id;
    @Comment("名称")
    private String name;
    @Comment("备注")
    private String remark;
    @Comment("类型")
    private TagType type;
    @Comment("删除标记")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(EnumType.STRING)
    public TagType getType(){
        return type;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

}
