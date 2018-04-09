package com.thousandsunny.cms.model;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在7/26/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class SiteRootChannels {
    public Site site;

    public List<Channel> channels;

    public SiteRootChannels(Site site, List<Channel> channels) {
        this.site = site;
        this.channels = channels;
    }

    public Site getSite() {

        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }
}
