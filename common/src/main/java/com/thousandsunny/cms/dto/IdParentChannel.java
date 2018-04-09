package com.thousandsunny.cms.dto;

/**
 * 如果这些代码有用，那它们是guitarist在7/30/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class IdParentChannel {
    private Long id;
    private IdParentChannel parentChannel;

    public IdParentChannel(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdParentChannel getParentChannel() {
        return parentChannel;
    }

    public void setParentChannel(IdParentChannel parentChannel) {
        this.parentChannel = parentChannel;
    }
}
