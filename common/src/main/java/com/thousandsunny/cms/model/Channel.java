package com.thousandsunny.cms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.cms.dto.SimpleObj;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.DocumentFile;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.cms.ModuleKey.*;
import static com.thousandsunny.cms.ModuleKey.BrowseAuthorityEnum.OPEN;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;


/**
 * Created by Jonathan on 2016/3/7.
 * 栏目表
 * complete
 */
@Entity
@Table(name = "cms_channel")
public class Channel {
    public Channel() {
    }

    public Channel(Long id) {
        setId(id);
    }

    private Long id;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Comment("栏目名称")
    private String name;

    @Column(length = 30)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Comment("目录级别")
    private String level;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Comment("归属站点")
    @JsonIgnore
    private Site site;

    @ManyToOne
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @Comment("栏目地址url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Comment("父节点")
    @JsonIgnore
    private Channel parentChannel;

    @ManyToOne
    public Channel getParentChannel() {
        return parentChannel;
    }

    public void setParentChannel(Channel parentChannel) {
        this.parentChannel = parentChannel;
    }

    @Comment("栏目路径 如 ,1,3,4,6 格式")
    private String channelPath;

    public String getChannelPath() {
        return channelPath;
    }

    public void setChannelPath(String channelPath) {
        this.channelPath = channelPath;
    }

    @Comment("别名")
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Comment("模板")
    private String template;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Comment("内容类型")
    private ContentType contentType;

    @Enumerated(value = STRING)
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Comment("菜单类型")
    private MenuType menuType;

    @Enumerated(value = STRING)
    public MenuType getMenuType() {
        return menuType;
    }

    public void setMenuType(MenuType menuType) {
        this.menuType = menuType;
    }

    @Comment("是否隐藏")
    private BooleanEnum hidden = YES;

    @Enumerated(value = STRING)
    public BooleanEnum getHidden() {
        return hidden;
    }

    public void setHidden(BooleanEnum hidden) {
        this.hidden = hidden;
    }

    @Comment("是否可修改")
    private BooleanEnum fixed = YES;

    @Enumerated(value = STRING)
    public BooleanEnum getFixed() {
        return fixed;
    }

    public void setFixed(BooleanEnum fixed) {
        this.fixed = fixed;
    }

    @Comment("浏览权限")
    private BrowseAuthorityEnum browseAuthority = OPEN;

    @Enumerated(value = STRING)
    public BrowseAuthorityEnum getBrowseAuthority() {
        return browseAuthority;
    }

    public void setBrowseAuthority(BrowseAuthorityEnum browseAuthority) {
        this.browseAuthority = browseAuthority;
    }

    @Comment("栏目类型")
    private ChannelTypeEnum channelType;

    @Enumerated(value = STRING)
    public ChannelTypeEnum getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelTypeEnum channelType) {
        this.channelType = channelType;
    }

    @Comment("栏目权值，数值越小越靠前")
    private Long weight;

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    @Comment("是否配图")
    private BooleanEnum hasImg;

    @Enumerated(value = STRING)
    public BooleanEnum getHasImg() {
        return hasImg;
    }

    public void setHasImg(BooleanEnum hasImg) {
        this.hasImg = hasImg;
    }

    @Comment("图片")
    public DocumentFile img;

    @OneToOne
    public DocumentFile getImg() {
        return img;
    }

    public void setImg(DocumentFile img) {
        this.img = img;
    }

    @Comment("发布时间")
    private Date createTime = new Date();
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Comment("发布人姓名")
    private String publishorName;
    public String getPublishorName() {
        return publishorName;
    }
    public void setPublishorName(String publishorName) {
        this.publishorName = publishorName;
    }

    @Comment("配图方向")
    private ImgDirection imgDirection;

    @Enumerated(value = STRING)
    public ImgDirection getImgDirection() {
        return imgDirection;
    }

    public void setImgDirection(ImgDirection imgDirection) {
        this.imgDirection = imgDirection;
    }

    @Comment("是否允许投稿")
    private BooleanEnum allowTg;

    @Enumerated(value = STRING)
    public BooleanEnum getAllowTg() {
        return allowTg;
    }

    public void setAllowTg(BooleanEnum allowTg) {
        this.allowTg = allowTg;
    }

    @Comment("访问类型")
    private VisitType visitType;

    @Enumerated(value = STRING)
    public VisitType getVisitType() {
        return visitType;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType;
    }

    @Comment("栏目内容，只在栏目为单页的情况下有内容")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Enumerated(value = STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(BooleanEnum isDelete) {
        this.isDelete = isDelete;
    }

    @Comment("SEO")
    private String seoTitle;

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }
    @Comment("SEO")
    private String seokeywords;

    public String getSeokeywords() {
        return seokeywords;
    }

    public void setSeokeywords(String seokeywords) {
        this.seokeywords = seokeywords;
    }

    @Comment("SEO")
    private String seoDescribe;

    public String getSeoDescribe() {
        return seoDescribe;
    }

    public void setSeoDescribe(String seoDescribe) {
        this.seoDescribe = seoDescribe;
    }


    @Comment("预览前缀")
    private String preview;

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    private List<Channel> childChannels;

    @OneToMany(fetch = LAZY, mappedBy = "parentChannel")
    public List<Channel> getChildChannels() {
        return childChannels;
    }

    public Channel setChildChannels(List<Channel> childChannels) {
        this.childChannels = childChannels;
        return this;
    }

    private Boolean visiable;

    @Transient
    public Boolean getVisiable() {
        return visiable;
    }

    public void setVisiable(Boolean visiable) {
        this.visiable = visiable;
    }

    private List<Article> articles;

    @JsonIgnore
    @OneToMany(mappedBy = "channel")
    public List<Article> getArticles() {
        return articles;
    }
    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    private List<Article> truncatedArticles;

    @Comment("操作行为")
    private List<Operation>operations;
    @JsonIgnore
    @ManyToMany
    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    @Transient
    public List<Article> getTruncatedArticles() {
        return truncatedArticles;
    }

    public Channel setTruncatedArticles(List<Article> truncatedArticles) {
        this.truncatedArticles = truncatedArticles;
        return this;
    }

    @JsonIgnore
    @Transient
    public SimpleObj simpleChannel() {
        return new SimpleObj(id, name, childChannels);
    }
}


