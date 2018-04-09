package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.PositionType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/19/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("职位")
@Table(name = "core_position")
public class Position {

    private Long id;
    @Comment("公司")
    private Company company;
    @Comment("名称")
    private String name;
    @Comment("日期")
    private Date date;
    @Comment("类型")
    PositionType type;
    @Comment("父部门")
    private Position parent;
    @Comment("子部门")
    List<Position> childs;
    @Comment("是否是部门管理员")
    private BooleanEnum isManager;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public PositionType getType() {
        return type;
    }

    @OneToOne
    public Company getCompany() {
        return company;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsManager() {
        return isManager;
    }

    @OneToMany(mappedBy = "parent")
    public List<Position> getChilds() {
        return childs;
    }

    @OneToOne
    public Position getParent() {
        return parent;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }
}
