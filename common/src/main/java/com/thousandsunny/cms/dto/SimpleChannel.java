package com.thousandsunny.cms.dto;

import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.cms.model.Operation;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在8/5/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class SimpleChannel {

    private Long id;
    private String text;
    private String url;
    private String content;
    private Boolean checked;
    private String level;

    private List<Operation> operations;
    private List<SimpleChannel> children;

    public SimpleChannel(Channel channel) {
        setId(channel.getId());
        setText(channel.getName());
        setOperations(channel.getOperations());
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<SimpleChannel> getChildren() {
        return children;
    }

    public void setChildren(List<SimpleChannel> children) {
        this.children = children;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public Boolean getChecked() {
        return checked;
    }

}
