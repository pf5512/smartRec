package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.core.ModuleKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.*;
import java.util.Date;

import static java.util.Objects.isNull;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * 如果这些代码有用，那它们是guitarist在7/27/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Table(name = "core_action_log")
@NoArgsConstructor
public class SysActionLog {

    private Long id;

    private Date createDate;

    private LogLevel logLevel;

    private RequestMethod method;

    private String source;

    private String url;

    private String content;

    private User user;

    public SysActionLog(Long id) {
        setId(id);
    }


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Transient
    public String getDateTime() {
        return isNull(createDate) ? null : ISO_DATETIME_FORMAT.format(createDate);
    }

    @Enumerated(STRING)
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @JsonIgnore
    public Date getCreateDate() {
        return createDate;
    }

    @Enumerated(STRING)
    public RequestMethod getMethod() {
        return method;
    }

    @Column(columnDefinition = ModuleKey.TEXT)
    public String getContent() {
        return content;
    }

    @JsonIgnore
    public String getUrl() {
        return url;
    }

    @JsonIgnore
    @OneToOne
    public User getUser() {
        return user;
    }
}
