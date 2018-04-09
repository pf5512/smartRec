package com.thousandsunny.cms.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by Jonathan on 2016/3/28.
 * 角色表
 */
@Data
@Entity
@Table(name = "cms_role")
public class Role {

    private Long id;
    @Comment("角色名称")
    private String name;
    @Comment("角色注解")
    private String memo;
    @Comment("删除标记")
    private BooleanEnum isDelete;

    public Role() {
    }

    public Role(Long id) {
        setId(id);
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Column(nullable = false, length = 50)
    public String getName() {
        return name;
    }

    public String getMemo() {
        return memo;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Transient
    public Boolean getIsDeleteBoolean() {
        return isDelete == YES;
    }


}
