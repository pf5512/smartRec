package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.thirdparty.model.Account;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在21/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("合伙人(若申请的区域有两个，则存在两条记录)")
@Table(name = "sr_partner")
public class Partner {

    private Long id;
    private Member member;
    private String name;
    private String mobile;
    private String idCardNo;
    @Comment("省份")
    private Region province;
    @Comment("城市")
    private Region city;
    @Comment("区")
    private Region area;
    private List<CloudFile> photos;
    private Date date = new Date();
    @Comment("收益")
    private BigDecimal income=new BigDecimal(0);
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
    public Region getProvince() {
        return province;
    }

    @OneToOne(fetch = LAZY)
    public Region getCity() {
        return city;
    }

    @OneToOne(fetch = LAZY)
    public Account getAccount() {
        return account;
    }

    @OneToOne(fetch = LAZY)
    public Region getArea() {
        return area;
    }
}
