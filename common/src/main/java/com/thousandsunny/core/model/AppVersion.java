package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.PhoneType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2016/9/26.
 */
@Data
@Entity
@Comment("手机版本")
@Table(name = "core_app_version")
public class AppVersion {
    private Long id;
    @Comment("手机类型")
    private PhoneType phoneType;
    @Comment("版本号")
    private String version;
    @Comment("最低版本号")
    private String minVersion;
    @Comment("更新时间")
    private Date updateDate;
    @Comment("更新日志")
    private String updateLog;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public PhoneType getPhoneType() {
        return phoneType;
    }
}
