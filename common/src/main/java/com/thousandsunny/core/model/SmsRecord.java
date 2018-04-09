package com.thousandsunny.core.model;


import com.thousandsunny.common.entity.Comment;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.SmsType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 4/20/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Entity
@Table(name = "core_sms_record")
public class SmsRecord {

    private Long id;
    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @Comment("短信内容")
    private String content;
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Comment(" 短信的验证码")
    private Integer code;
    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    @Comment("发送日期")
    private Date sendDate=new Date();
    public Date getSendDate() {
        return sendDate;
    }
    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    @Comment("接受者id")
    private String receiver;
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Comment("短信类型")
    private SmsType smsType;
    @Enumerated(STRING)
    public SmsType getSmsType() {
        return smsType;
    }
    public void setSmsType(SmsType smsType) {
        this.smsType = smsType;
    }
}
