package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.DepartmentType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/11/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("部门")
@Table(name = "core_department")
public class Department {
    private Long id;
    @Comment("部门名称")
    private String name;
    @Comment("父部门")
    private Department parent;
    @Comment("部门电话")
    private String telephone;
    @Comment("子部门")
    private List<Department> childs;
    @Comment("部门经理")
    private Long manager;
    @Comment("创建时间")
    private Date date;
    @Comment("是否已经删除")
    private BooleanEnum isDelete;
    @Comment("部门类型")
    private DepartmentType departmentType;

    @Id
    @GeneratedValue(strategy = AUTO)
    @JsonIgnore
    public Long getId() {
        return id;
    }

    @OneToOne
    public Department getParent() {
        return parent;
    }

    @OneToMany(mappedBy = "parent")
    public List<Department> getChilds() {
        return childs;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(STRING)
    public DepartmentType getDepartmentType() {
        return departmentType;
    }

}
