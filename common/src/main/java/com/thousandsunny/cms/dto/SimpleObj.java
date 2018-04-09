package com.thousandsunny.cms.dto;

import java.util.List;

/**
 * Created by guitarist on 5/27/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class SimpleObj {

    private Long id;
    private String text;

    private List children;

    public SimpleObj() {
    }

    public SimpleObj(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public SimpleObj(Long id, String text, List children) {
        this.id = id;
        this.text = text;
        this.children = children;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List getChildren() {
        return children;
    }

    public void setChildren(List children) {
        this.children = children;
    }

}
