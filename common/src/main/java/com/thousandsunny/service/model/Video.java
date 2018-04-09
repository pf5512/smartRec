package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在12/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("视频")
@Table(name = "sr_video")
public class Video {

    private Long id;
    @Comment("视频文件")
    private CloudFile video;
    @Comment("logo")
    private CloudFile logo;
    @Comment("视频简介")
    private String content;
    @Comment("视频主题")
    private String title;
    @Comment("上传时间")
    private Date date;
    @Comment("上传者")
    private Member member;
    @Comment("类型")
    private String videoType;
    @Comment("状态")
    private ModuleKey.BooleanEnum state;
    @Comment("是否删除")
    private ModuleKey.BooleanEnum isDelete = NO;
    @Comment("排序")
    private Integer sord;


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getVideo() {
        return video;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getLogo() {
        return logo;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @Column(columnDefinition = TEXT)
    public String getContent() {
        return content;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsDelete() {
        return isDelete;
    }
}
