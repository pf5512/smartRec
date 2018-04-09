package com.thousandsunny.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在8/11/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Entity
@Table(name = "cms_menu")
public class Menu {
    private Long id;
    @Comment("链接地址")
    private String url;

    @Comment("菜单名称")
    private String name;

    private Menu parent;

    private List<Menu> child;

    @Comment("图标")
    private String icon;
    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    @OneToOne
    public Menu getParent() {
        return parent;
    }

    public void setParent(Menu parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "parent")
    public List<Menu> getChild() {
        return child;
    }

    public void setChild(List<Menu> child) {
        this.child = child;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
