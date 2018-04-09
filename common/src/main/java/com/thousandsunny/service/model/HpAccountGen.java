package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在10/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("邀请码生成器")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sr_hp_account_gen")
public class HpAccountGen {

    private Long id;
    private Date date = new Date();
    private Long seq = 1L;
    private Member member;

    public HpAccountGen(Long seq) {
        this.seq = seq;
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }
}
