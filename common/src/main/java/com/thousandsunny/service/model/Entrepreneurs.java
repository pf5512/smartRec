package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.model.Account;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在24/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("创业者")
@Table(name = "sr_entrepreneurs")
public class Entrepreneurs {

    private Long id;
    private Member member;
    private String name;
    private String mobile;
    private String idCardNo;
    private List<CloudFile> photos;
    private Date date = new Date();
    @Comment("创业奖励收益（一级创业者好友提成）")
    private BigDecimal entrepreneurRewardIncome = new BigDecimal(0);
    @Comment("收益和")
    private BigDecimal income = new BigDecimal(0);
    private Account account;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToMany
    public List<CloudFile> getPhotos() {
        return photos;
    }

    @OneToOne(fetch = LAZY)
    public Account getAccount() {
        return account;
    }
}
