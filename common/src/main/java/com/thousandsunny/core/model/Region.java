package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;

/**
 * 如果这些代码有用，那它们是guitarist在9/21/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("省/市/区/街道/社区")
@Table(name = "core_region")
@NoArgsConstructor
public class Region {

    private Long id;

    private String gb2260;
    private String name;
    private Integer regionLevel;
    private Region parent;
    private String parentIds;
    private String parentGb2260;
    private String diallingCode;
    private Date createDate;
    private Date lastModified;
    private BooleanEnum isHotCity = NO;

    public Region(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }
    @OneToOne
    public Region getParent() {
        return parent;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsHotCity() {
        return isHotCity;
    }
}
