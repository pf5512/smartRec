package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.FileType;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 6/13/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Entity
@Table(name = "core_cloud_file")
public class CloudFile implements Serializable {

    private Long id;
    private String title;
    @Comment("路径")
    private String path;
    private String hash;
    private FileType type = IMAGE;
    @Comment("是否由平台添加")
    private BooleanEnum isPlatformAdd = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    @JsonIgnore
    public Long getId() {
        return id;
    }

    @Column(unique = true)
    public String getPath() {
        return path;
    }

    @JsonIgnore
    @Column(unique = true)
    public String getHash() {
        return hash;
    }

    @Enumerated(STRING)
    public FileType getType() {
        return type;
    }

    @Transient
    public Boolean getIsPlatformAddBoolean() {
        return isPlatformAdd == YES;
    }
}
