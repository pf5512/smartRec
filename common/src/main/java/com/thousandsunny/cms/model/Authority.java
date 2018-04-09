package com.thousandsunny.cms.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by Jonathan on 2016/3/28.
 * 角色对应权限表
 */
@Data
@Entity
@Table(name = "cms_authority")
public class Authority implements Serializable {

    private Long id;
    @Comment("角色")
    private Role role;
    @Comment("菜单")
    private Menu menu;
    @Comment("操作行为")
    private List<Operation> operations;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @JsonIgnore
    @ManyToOne
    public Role getRole() {
        return role;
    }

    @OneToOne
    public Menu getMenu(){
        return menu;
    }

    @ManyToMany
    public List<Operation> getOperations() {
        return operations;
    }
}
