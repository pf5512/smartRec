package com.thousandsunny.core.model;


import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * 好友关系
 */
@Data
@Entity
@Table(name = "core_friends")
public class Friends {
    private Long id;

    @Comment("用户id")
    private Member owner;
    @Comment("好友id")
    private Member friend;
    @Comment("添加日期")
    private Date knowDate;
    @Comment("最近联系时间")
    private Date lastDate;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getOwner() {
        return owner;
    }

    @OneToOne
    public Member getFriend() {
        return friend;
    }

}
