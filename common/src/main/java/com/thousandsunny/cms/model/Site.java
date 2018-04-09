package com.thousandsunny.cms.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.cms.ModuleKey.SiteTypeEnum;
import com.thousandsunny.cms.dto.SimpleObj;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.DocumentFile;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.cms.ModuleKey.SiteTemplateEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
/**
 * Created by Jonathan on 2016/3/1.
 * 站点信息表
 * completed
 */
@Entity
@Table(name = "cms_site")
public class Site {

    public Site() {
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

    @Comment("站点名称")
    private String name;

    @Column(length = 100, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Comment("根网址")
    private String root;

    @Column(length = 100)
    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @Comment("URL")
    private String url;

    @Column(length = 100)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Comment("文档HTML默认保存路径")
    private String htmlPath;

    @Column(length = 100)
    public String getHtmlPath() {
        return htmlPath;
    }

    public void setHtmlPath(String htmlPath) {
        this.htmlPath = htmlPath;
    }

    @Comment("图片/上传文件默认路径")
    private DocumentFile attachPath;

    @OneToOne
    public DocumentFile getAttachPath() {
        return attachPath;
    }

    public void setAttachPath(DocumentFile attachPath) {
        this.attachPath = attachPath;
    }

    @Comment("网站版权信息")
    private String copyright;

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    @Comment("站点默认关键字")
    private String keywords;

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Comment("站点描述")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Comment("网站备案号")
    private String icp;

    @Column(length = 100)
    public String getIcp() {
        return icp;
    }

    public void setIcp(String icp) {
        this.icp = icp;
    }

    @Comment("站点类型")
    private SiteTypeEnum type;

    @Enumerated(STRING)
    public SiteTypeEnum getType() {
        return type;
    }

    public void setType(SiteTypeEnum type) {
        this.type = type;
    }

    @Comment("站点模板")
    private SiteTemplateEnum template;

    public SiteTemplateEnum getTemplate() {
        return template;
    }

    public void setTemplate(SiteTemplateEnum template) {
        this.template = template;
    }

    @Comment("站点状态")
    private BooleanEnum entityStatus;

    @Enumerated(value = STRING)
    public BooleanEnum getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(BooleanEnum entityStatus) {
        this.entityStatus = entityStatus;
    }

    @Comment("创建时间")
    public Date createTime=new Date();

    @Column(updatable = false)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Comment("是否删除")
    private BooleanEnum isDelete;
    @Enumerated(value = STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(BooleanEnum isDelete) {
        this.isDelete = isDelete;
    }

    @JsonIgnore
    @Transient
    public SimpleObj getSimpleSite() {
        return new SimpleObj(id, name);
    }

}
