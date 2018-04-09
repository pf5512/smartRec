package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.FileType;
import static com.thousandsunny.core.ModuleKey.FileType.DOCUMENT;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 4/10/16.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "core_document_file")
public class DocumentFile {

    private Long id;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    public DocumentFile(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public DocumentFile(String localPath) {
        path = localPath;
        url = localPath.substring(localPath.indexOf(ModuleKey.UPLOAD_FOLDER) - 1).replaceAll("\\\\", "/");
        thumbnailUrl = url.replaceAll("\\.", "_thumb\\.");
        thumbnailPath = path.replaceAll("\\.", "_thumb\\.");
    }

    @Comment("名称")
    private String name;

    @Comment("文件本地路径")
    private String path;

    @JsonIgnore
    public String getPath() {
        return path;
    }

    @Comment("缩率图本地路径")
    private String thumbnailPath;

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    @Comment("URL路径")
    private String url;

    @Comment("缩率图URL路径")
    private String thumbnailUrl;

    @Comment("文件类型")
    private FileType fileType = DOCUMENT;

    @Column(name = "file_type")
    public FileType getFileType() {
        return fileType;
    }

    @Comment("文件描述")
    private String description;

    @Column(columnDefinition = TEXT)
    public String getDescription() {
        return description;
    }

}