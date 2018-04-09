package com.thousandsunny.cms.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在8/8/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "cms_operations")
public class Operation {

    private long id;
    private String name;
    private String code;
    @Comment("是否选中")
    private Boolean checked = false;


    public Operation(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Transient
    public Boolean getChecked() {
        return checked;
    }
}
