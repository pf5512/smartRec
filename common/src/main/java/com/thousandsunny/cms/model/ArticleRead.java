package com.thousandsunny.cms.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by 13336 on 2017/3/22.
 */
@Data
@Entity
@Table(name = "cms_article_read")
public class ArticleRead {
    private Long id;
    @Comment("用户")
    private Member member;
    @Comment("资讯")
    private Article article;
    @Comment("是否已读")
    private ModuleKey.BooleanEnum isRead = ModuleKey.BooleanEnum.YES;
    @Comment("读取时间")
    private Date readTime = new Date();
    @Comment("是否删除")
    private ModuleKey.BooleanEnum isDelete = ModuleKey.BooleanEnum.NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = LAZY)
    public Article getArticle() {
        return article;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsRead() {
        return isRead;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsDelete() {
        return isDelete;
    }
}
