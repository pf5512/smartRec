package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 */
@Data
@Entity
@Comment("平台活动类别")
@Table(name = "sr_terrace_activity_class")
public class TerraceActivityClass {

    private Long id;
    @Comment("类别名称")
    private String name;
    @Comment("删除标志")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }
}
