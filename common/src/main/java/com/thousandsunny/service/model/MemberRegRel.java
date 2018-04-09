package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("注册关系")
@Table(name = "sr_member_reg_rel")
public class MemberRegRel {

    private Long id;
    private Member member;
    private Long p1;
    private Long p2;
    private Long p3;

    private Date date = new Date();
    private BooleanEnum valid;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @Enumerated(STRING)
    public BooleanEnum getValid() {
        return valid;
    }
}
