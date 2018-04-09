package com.thousandsunny.service.model;

import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey.StateEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BLOB;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static com.thousandsunny.service.ModuleKey.MomentsType;
import static com.thousandsunny.service.ModuleKey.StateEnum.NO;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Comment("圈子,说说,朋友圈")
@Entity
@NoArgsConstructor
@Table(name = "sr_moments")
public class Moments {

    private Long id;
    @Comment("内容")
    private byte[] content;
    @Comment("用户")
    private Member author;
    @Comment("位置")
    private String position;
    @Comment("图片")
    private List<CloudFile> pics;
    @Comment("类型")
    private MomentsType contentType;
    @Comment("at用户列表")
    private List<Member> mentioned;
    @Comment("是否全部可见")
    private BooleanEnum publicVisible;
    @Comment("发布时间")
    private Date publishTime = new Date();
    @Comment("点赞")
    public List<MomentsFavorites> memberFavorites;
    @Comment("评论")
    public List<Commentary> commentaries;
    @Comment("资讯")
    public Article article;
    public StateEnum isDelete = NO;
    @Comment("经度")
    private Double longitude;
    @Comment("纬度")
    private Double latitude;
    @Comment("店铺")
    private Job job;
    @Comment("省")
    private Region province;
    @Comment("市")
    private Region city;
    @Comment("区")
    private Region area;


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public StateEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(STRING)
    public MomentsType getContentType() {
        return contentType;
    }

    @Enumerated(STRING)
    public BooleanEnum getPublicVisible() {
        return publicVisible;
    }

    @ManyToMany
    public List<Member> getMentioned() {
        return mentioned;
    }

//    @Column(columnDefinition = TEXT)
//    public String getContent() {
//        return content;
//    }

    @Column(columnDefinition = BLOB)
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @OneToMany(cascade = ALL)
    public List<CloudFile> getPics() {
        return pics;
    }

    @OneToOne
    public Member getAuthor() {
        return author;
    }

    @OneToMany(cascade = ALL, mappedBy = "moments")
    public List<MomentsFavorites> getMemberFavorites() {
        return memberFavorites;
    }

    @OneToMany(cascade = ALL, mappedBy = "moments")
    public List<Commentary> getCommentaries() {
        return commentaries;
    }

    @OneToOne
    public Article getArticle() {
        return article;
    }

    @OneToOne
    public Job getJob() {
        return job;
    }

    @OneToOne
    public Region getProvince() {
        return province;
    }

    @OneToOne
    public Region getCity() {
        return city;
    }

    @OneToOne
    public Region getArea() {
        return area;
    }

}
