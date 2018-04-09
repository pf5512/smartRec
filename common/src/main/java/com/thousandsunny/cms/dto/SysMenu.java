package com.thousandsunny.cms.dto;

import com.thousandsunny.cms.model.Menu;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Created by Administrator on 2016/8/20 0020.
 */
public class SysMenu {
    private Long id;
    private String url;
    private String name;
    private List<SysMenu> child;
    private String icon;
    private Boolean checked = false;
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

    public List<SysMenu> getChild() {
        return child;
    }

    public void setChild(List<SysMenu> child) {
        this.child = child;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public SysMenu(Menu menu){
        this.setId(menu.getId());
        this.setUrl(menu.getUrl());
        this.setIcon(menu.getIcon());
        this.setName(menu.getName());
        if (!isEmpty(menu.getChild()))
            setChild(menu.getChild().stream().map(SysMenu::new).collect(toList()));

    }
}
