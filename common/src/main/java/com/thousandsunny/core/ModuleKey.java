package com.thousandsunny.core;


import com.thousandsunny.common.entity.TitleEnum;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

public class ModuleKey {

    public static final String SCORE_TYPE_SIGN_IN = "SIGN_IN";
    public static final String UPLOAD_FOLDER = "upload";
    public static final String IMAGE_FOLDER = "image";
    public static final String DOCUMENT_FOLDER = "document";
    public static final String CHAT_RECORD_FOLDER = "chat_record";
    public static final String TEXT = "TEXT";
    public static final String BLOB = "BLOB";
    public static final ResponseEntity OK = ok().body(newHashMap());
    public static final ResponseEntity BAD_REQUEST = badRequest().build();

    public enum ApplyState implements TitleEnum {
        APPLY("等待中"), AGREE("接受"), REJECT("拒绝"), APPROVAL("审核"), END("已结束"), PAUSE("暂停"), CANCEL("取消");
        private String title;

        ApplyState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }


    public enum ToggleAction implements TitleEnum {

        BIND("绑定"), UNBIND("解绑");

        private String title;

        ToggleAction(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum EntityStatus implements TitleEnum {

        NORMAL("正常"), LOCKED("禁用");

        private String title;

        EntityStatus(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum GenderEnum implements TitleEnum {
        MALE("男"), FEMALE("女");
        private String name;

        GenderEnum(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum BooleanEnum implements TitleEnum {

        YES("是"), NO("否");
        private String name;

        BooleanEnum(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }

        public Boolean getBool() {
            return this == YES;
        }

    }

    public enum Valid implements TitleEnum {

        PRE_VALID("尚未有效"), VALID("有效"), INVALID("已经无效");
        private String name;

        Valid(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum AccountEnum implements TitleEnum {
        SCHOOL("学校用户"), SHOP("店铺"), EMPLOYEE("雇员"), MANAGER("系统管理员");
        private String title;

        AccountEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum DictDataTypeEnum implements TitleEnum {

        SINGLE("单一值"), LIST("列表值");
        private String name;

        DictDataTypeEnum(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum AuthorityTypeEnum implements TitleEnum {

        MODULE("模块"), MENU("菜单"), OPERATION("功能");
        private String name;

        AuthorityTypeEnum(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum FileType implements TitleEnum {
        IMAGE("图片"), DOCUMENT("文档"), CHAT_RECORD("聊天记录"), VIDEO("视频");

        private String name;

        FileType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum SmsType implements TitleEnum {
        CREATE_USER("创建用户", "SMS_33210095", TRUE),
        SYS_EVENT("系统事件", "", FALSE),
        LOGIN("登陆", "SMS_33315068", TRUE),
        RESET_PWD("重置密码", "SMS_33255052", TRUE),
        FORGET_PWD("忘记密码", "SMS_33255052", TRUE),
        BIND_MOBILE("绑定手机", "SMS_33255052", TRUE),
        INIT_MEMBER("初始化", "SMS_33250097", FALSE),
        WITHDRAW_VERIFY("获取提现验证码", "SMS_33315068", TRUE),
        SET_PAY_PWD("设置支付密码", "SMS_33380036", TRUE),
        CHANGE_PWD("更换手机号", "SMS_33245055", TRUE);
        private String name;
        private Boolean needValCode;
        private String templateCode;

        SmsType(String name, String templateCode, Boolean needValCode) {
            this.name = name;
            this.templateCode = templateCode;
            this.needValCode = needValCode;
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public Boolean getNeedValCode() {
            return needValCode;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }


    public enum ThirdPartyPayAccountType implements TitleEnum {
        WXWALLET("微信支付"), ALIPAY("支付宝");

        private String title;

        ThirdPartyPayAccountType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum ThirdPartySocialAccountType implements TitleEnum {
        WX("微信"), WB("微博"), QQ("QQ"),WX_PUB("微信公众号");

        private String title;

        ThirdPartySocialAccountType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum MemberMsgType implements TitleEnum {
        TALK_AT_REMIND("说说AT提醒"),
        COMMENT_MOMENTS("评论说说"),
        LIKE_MOMENTS("点赞"),
        JOB_PREPAY_REMIND("岗位招聘预付款项提醒"),
        JOB_CHARGEBACK_REMIND("岗位招聘余额扣款提醒"),
        JOB_DEFAULT_REMIND("岗位招聘违约提示提醒"),
        FRIEND_WORK_REMIND("好友上班提醒"),
        WORK_STATE_CONFIRM_REMIND("工作状态确认提醒"),
        H_FRIEND_APPLY_REMIND("慧友请求提醒"),
        CHAT("聊天信息"), READ_COOPERATE("店铺合作"),
        READ_TRANSFER("店铺转让"),
        READ_CARD_AND_COUPON("店铺卡券"),
        READ_PLATFORM_ACTIVITY("平台活动"),
        GROUP_TALK("群聊"),
        GROUP_MEMBER_ADD("群成员添加");

        private String title;

        MemberMsgType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RemindMsgType implements TitleEnum {
        WORK_STATE_CONFIRM_REMIND_TO_EMPLOYEE("工作状态确认提醒(确认离职)"),
        WORK_STATE_CONFIRM_REMIND_TO_STORE("工作状态确认提醒(确认上班)");
        private String title;

        RemindMsgType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum DateType implements TitleEnum {

        DAY("当日"), WEEK("当周"), MONTH("当月");

        private String title;

        DateType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum CheckType implements TitleEnum {
        SIGN_IN("签到"), SIGN_OUT("签退");
        private String title;

        CheckType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum PhoneType implements TitleEnum {
        IOS("苹果"), ANDROID("安卓"), WEB("微信");
        private String title;

        PhoneType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum VisitState implements TitleEnum {
        HALF_HOUR("半小时"), ONE_HOUR("一小时");
        private String title;

        VisitState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum PositionType implements TitleEnum {

        DESINGER("设计部"), SALE_MAN("业务部"), ENGINEER("工程部");

        private String title;

        PositionType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum DepartmentType implements TitleEnum {

        SJB("设计部"), YWB("业务部"), GCB("工程部"), XXB("信息部"), CWB("财务部");

        private String title;

        DepartmentType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum FriendType implements TitleEnum {

        REGISTER("注册"), RECOMMEND("找工作推荐");
        private String title;

        FriendType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum SubLevelType implements TitleEnum {

        SUB_LEVEL_ONE(1, "朋友"), SUB_LEVEL_TWO(2, "熟人"), SUB_LEVEL_THREE(3, "人脉");
        private String title;
        private Integer level;

        SubLevelType(Integer level, String title) {
            this.level = level;
            this.title = title;
        }

        public Integer getLevel() {
            return level;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum IdentityType implements TitleEnum {

        NONE("非创业者", BigDecimal.ZERO),//不用做最高收益判断
        JUNIOR("初级创业者", new BigDecimal(1000000)),
        SENIOR("高级创业者", new BigDecimal(2000000));
        private String title;
        private BigDecimal income;//最高收益

        IdentityType(String title, BigDecimal income) {
            this.title = title;
            this.income = income;
        }

        @Override
        public String getTitle() {
            return title;
        }

        public BigDecimal getIncome() {
            return income;
        }
    }

    public enum CategoryType implements TitleEnum {
        POSITION("职位");

        private String title;


        CategoryType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


}
