package com.thousandsunny.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Moments;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static java.util.Objects.isNull;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.AUTO;

@Data
@Comment("评论")
@Entity
@Table(name = "cms_commentary")
public class Commentary {
    private Long id;
    @Comment("评论哪条新闻")
    private Article article;
    @Comment("评论人")
    private Member commentator;
    @Comment("评论内容")
    private String content;
    @Comment("说说")
    private Moments moments;
    @Comment("回复的评论")
    private Commentary parentCommentary;
    @Comment("创建时间")
    private Date createTime = new Date();
    @Comment("ip地址")
    private String ipAddress;
    @Comment("子评论")
    private List<Commentary> childCommentaries;
    @Comment("启用")
    private BooleanEnum state = YES;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @JsonIgnore
    @OneToOne
    public Commentary getParentCommentary() {
        return parentCommentary;
    }

    @JsonIgnore
    @OneToOne
    public Article getArticle() {
        return article;
    }

    @OneToOne
    public Member getCommentator() {
        return commentator;
    }

    @JsonIgnore
    @Column(columnDefinition = TEXT)
    public String getContent() {
        return content;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getState() {
        return state;
    }

    @Transient
    public String getRawContent() {
        return isNull(content) ? null : content;
    }

    @OneToOne
    public Moments getMoments() {
        return moments;
    }

    @JsonIgnore
    @OneToMany(cascade = ALL, mappedBy = "parentCommentary")
    public List<Commentary> getChildCommentaries() {
        return childCommentaries;
    }

}
