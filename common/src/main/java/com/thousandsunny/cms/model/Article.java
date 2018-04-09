package com.thousandsunny.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.cms.dto.TypicalChannel;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.DocumentFile;
import com.thousandsunny.core.model.Member;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static java.util.Objects.isNull;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 文章基类表
 */
@Data
@Entity
@Table(name = "cms_article")
@NoArgsConstructor
public class Article {

    private Long id;
    @Comment("属性，由ContentPropertyEnum构成，逗号分隔")
    private String property;
    @Comment("缩略图,图片文件路径")
    private String thumbnail;
    @Comment("所属栏目")
    private Channel channel;
    @Comment("删除时间")
    private Date deleteDate;
    private List<Commentary> commentaries;
    @Comment("发布状态")
    private BooleanEnum status = NO;
    @Comment("发布时间")
    private Date publishTime = new Date();
    @Comment("发布人姓名")
    private String publishorName;
    @Comment("权值")
    private Long weight;
    @Comment("标签, ','号分开，单个标签小于12字节")
    private List<CmsTag> tags;
    @Comment("文章标题")
    private String title;
    @Comment("短标题")
    private String shortTitle;
    @Comment("关键字, 以逗号分隔")
    private String keywords;
    @Comment("摘要")
    private String summary;
    @Comment("文章来源")
    private String source;
    @Comment("作者")
    private Member author;
    @Comment("logo")
    private CloudFile logo;
    @Comment("")
    private Date date;
    @Comment("介绍")
    private String introduction;
    @Comment("点击量")
    private Integer clickCount;
    @Comment("管理机构")
    private String mechanism;
    @Comment("封面图片")
    private DocumentFile coverImage;
    @Comment("下载的文件")
    public List<DocumentFile> downloadFile;
    @Comment("内容")
    private String content;
    @Comment("链接地址")
    private String url;
    @Comment("附件")
    private List<DocumentFile> accessory;
    @Comment("是否可评论")
    private BooleanEnum isOpen = NO;
    @Comment("是否审核")
    private BooleanEnum audited = NO;
    @Comment("推荐")
    private BooleanEnum recommend = NO;
    @Comment("置顶")
    private BooleanEnum top = NO;
    @Comment("是否已删除")
    private BooleanEnum isDelete = NO;


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @JsonIgnore
    @ManyToOne
    public Channel getChannel() {
        return channel;
    }

    @Transient
    public TypicalChannel getTypicalChannel() {
        if (isNull(channel)) return null;
        return new TypicalChannel(channel);
    }

    @Enumerated(value = STRING)
    public BooleanEnum getStatus() {
        return status;
    }

    @OneToMany
    public List<CmsTag> getTags() {
        return tags;
    }

    @Column(length = 100, nullable = false)
    public String getTitle() {
        return title;
    }

    @Column(length = 50)
    public String getShortTitle() {
        return shortTitle;
    }


    @Column(length = 100)
    public String getSource() {
        return source;
    }

    @OneToOne
    public CloudFile getLogo() {
        return logo;
    }

    @OneToOne
    public Member getAuthor() {
        return author;
    }

    @Column(columnDefinition = TEXT)
    public String getSummary() {
        return summary;
    }

    @Column(columnDefinition = TEXT)
    public String getIntroduction() {
        return introduction;
    }

    @OneToOne
    public DocumentFile getCoverImage() {
        return coverImage;
    }

    @OneToMany
    public List<DocumentFile> getDownloadFile() {
        return downloadFile;
    }

    @OneToMany
    public List<DocumentFile> getAccessory() {
        return accessory;
    }

    @OneToMany(mappedBy = "article")
    public List<Commentary> getCommentaries() {
        return commentaries;
    }

    @Basic(fetch = LAZY)
    @Column(columnDefinition = TEXT)
    public String getContent() {
        return content;
    }

    @Enumerated(value = STRING)
    public BooleanEnum getIsOpen() {
        return isOpen;
    }

    @Enumerated(value = STRING)
    public BooleanEnum getAudited() {
        return audited;
    }

    @Enumerated(value = STRING)
    public BooleanEnum getRecommend() {
        return recommend;
    }

    @Enumerated(value = STRING)
    public BooleanEnum getTop() {
        return top;
    }

    @Enumerated(value = STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    public Article setCommentaries(List<Commentary> commentaries) {
        this.commentaries = commentaries;
        return this;
    }

}
