package com.thousandsunny.cms.dto;

import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;

import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * Created by Administrator on 2016/7/28 0028.
 */
public class SysArticle {
    private Long id;
    private String title;
    private Long weight;
    private String pubtime;
    private String type;
    private String letterCompany;
    private String confidential;
    private boolean totop;
    private boolean audit;
    private boolean recommend;
    private Channel channelParent;
    private String channelName;
    private String deleteDate;
    private Member author;
    private String publishorName;

    @Comment("开始时间")
    private String startTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Comment("结束时间")
    private String endTime;

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public String getPubtime() {
        return pubtime;
    }

    public void setPubtime(String pubtime) {
        this.pubtime = pubtime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLetterCompany() {
        return letterCompany;
    }

    public void setLetterCompany(String letterCompany) {
        this.letterCompany = letterCompany;
    }

    public String getConfidential() {
        return confidential;
    }

    public void setConfidential(String confidential) {
        this.confidential = confidential;
    }

    public boolean isTotop() {
        return totop;
    }

    public void setTotop(boolean totop) {
        this.totop = totop;
    }

    public boolean isAudit() {
        return audit;
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    public boolean isRecommend() {
        return recommend;
    }

    public void setRecommend(boolean recommend) {
        this.recommend = recommend;
    }

    public Channel getChannelParent() {
        return channelParent;
    }

    public void setChannelParent(Channel channelParent) {
        this.channelParent = channelParent;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(String deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Member getAuthor() {
        return author;
    }

    public void setAuthor(Member author) {
        this.author = author;
    }

    public String getPublishorName() {
        return publishorName;
    }

    public void setPublishorName(String publishorName) {
        this.publishorName = publishorName;
    }

    public SysArticle(Article article) {
        setId(article.getId());
        setWeight(article.getWeight());
        setLetterCompany(article.getSource());
        setTitle(article.getTitle());
        setAuthor(article.getAuthor());
        setPublishorName(article.getPublishorName());

        ifNotNullThen(article.getPublishTime(), c -> setPubtime(ISO_DATETIME_FORMAT.format(c)));
        ifNotNullThen(article.getChannel(), c -> setType(c.getName()));
        setTotop(article.getTop() == YES);
        setAudit(article.getAudited() == YES);
        setRecommend(article.getRecommend() == YES);
        ifNotNullThen(article.getChannel(), c -> setChannelParent(c.getParentChannel()));
        ifNotNullThen(article.getChannel(), c -> setType(c.getName()));
        ifNotNullThen(article.getChannel(), c -> setChannelName(c.getName()));
        ifNotNullThen(article.getDeleteDate(), d -> setDeleteDate(ISO_DATETIME_FORMAT.format(d)));
    }
}

