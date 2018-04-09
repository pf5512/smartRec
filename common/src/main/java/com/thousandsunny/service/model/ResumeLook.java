package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("简历被shop查看记录")
@Table(name = "sr_resume_look")
public class ResumeLook {

    private Long id;
    @Comment("简历")
    private Resume resume;
    @Comment("查看商家")
    private Shop shop;
    @Comment("查看时间")
    private Date date;
    @Comment("查看标记")
    private BooleanEnum isRead = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Resume getResume() {
        return resume;
    }

    @OneToOne
    public Shop getShop() {
        return shop;
    }


}
