package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by admin on 2016/10/27.
 */
@Data
@Comment("图片收藏")
@Entity
@NoArgsConstructor
@Table(name = "sr_picture_collect")
public class PictureCollect {
    private Long id;
    @Comment("图片")
    private CloudFile picture;
    @Comment("收藏者")
    private Member member;
    @Comment("所有者")
    private Member owner;
    @Comment("收藏时间")
    private Date date = new Date();
    @Comment("删除")
    private ModuleKey.BooleanEnum collectEver = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getCollectEver() {
        return collectEver;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @OneToOne
    public Member getOwner() {
        return owner;
    }

    @OneToOne
    public CloudFile getPicture() {
        return picture;
    }

}
