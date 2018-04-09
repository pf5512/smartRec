package com.thousandsunny.service.model;

import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.cms.model.Operation;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static com.thousandsunny.service.ModuleKey.PositionEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * 如果这些代码有用，那它们是guitarist在11/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("店铺")
@Table(name = "sr_shop")
public class Shop {

    private Long id;
    @Comment("名称")
    private String name;
    @Comment("标签:美容,美发,化妆......")
    private List<CmsTag> tags;
    @Comment("地点")
    private String location;
    private CloudFile logo;
    @Comment("店铺照片")
    private List<CloudFile> photos;
    @Comment("登记人")
    private Member owner;
    @Comment("登记人说明")
    private PositionEnum ownerPosition;
    @Comment("省份")
    private Region province;
    @Comment("城市")
    private Region city;
    @Comment("区")
    private Region area;
    @Comment("地址")
    private String address;
    @Comment("店铺管理员是否设置电话免打扰")
    private BooleanEnum isNoDisturb = NO;
    @Comment("经度")
    private Double longitude;
    @Comment("纬度")
    private Double latitude;
    @Comment("店铺亮点")
    private String brightSpots;
    @Comment("创建时间")
    private Date date = new Date();
    @Comment("寻求合作状态")
    private Operation findHelp;
    @Comment("转让状态")
    private Operation isTransfer;
    @Comment("是否启用")
    private BooleanEnum state = YES;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    private List<Operation> sterns;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToMany
    public List<CmsTag> getTags() {
        return tags;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getLogo() {
        return logo;
    }

    @OneToMany
    public List<CloudFile> getPhotos() {
        return photos;
    }

    @OneToOne(fetch = LAZY)
    public Member getOwner() {
        return owner;
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
    public Region getArea() {
        return area;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsNoDisturb() {
        return isNoDisturb;
    }

    @Enumerated(STRING)
    public PositionEnum getOwnerPosition() {
        return ownerPosition;
    }

    @Transient
    public String getShopFirstPhoto() {
        return isEmpty(photos) ? null : photos.get(0).getPath();
    }

    @Column(columnDefinition = TEXT)
    public String getBrightSpots() {
        return brightSpots;
    }

    @Transient
    public Boolean getIsNoDisturbBool() {
        return isNoDisturb == YES;
    }

    @OneToOne
    public Operation getFindHelp() {
        return findHelp;
    }

    @OneToOne
    public Operation getIsTransfer() {
        return isTransfer;
    }

    @Enumerated(STRING)
    public BooleanEnum getState() {
        return state;
    }

    @ManyToMany
    public List<Operation> getSterns() {
        return sterns;
    }

}
