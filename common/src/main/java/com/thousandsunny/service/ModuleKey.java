package com.thousandsunny.service;

import com.thousandsunny.common.entity.TitleEnum;

import java.math.BigDecimal;

/**
 * 如果这些代码有用，那它们是guitarist在11/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class ModuleKey {
    public static final String SCOPE_DEFAULT = "meiye";

    public static final String SMS_RESET_SUC = "SMS_33670215";
    public static final String SMS_INIT = "SMS_33250097";

    public enum KeyPercentage implements TitleEnum {
        CONSTANT_CAR_FEE(new BigDecimal("100"), "上班好处-100元车旅费"),

        CONSTANT_PARTNER_FEE(new BigDecimal("50000"), "合伙人加盟费"),

        CONSTANT_REC_FRIEND(new BigDecimal("0.3"), "推荐-朋友"),
        CONSTANT_REC_ACQUAINTANCE(new BigDecimal("0.1"), "推荐-熟人"),
        CONSTANT_REC_CONTACTS(new BigDecimal("0.05"), "推荐-人脉"),

        CONSTANT_REG_FRIEND(new BigDecimal("0.1"), "注册-朋友"),
        CONSTANT_REG_ACQUAINTANCE(new BigDecimal("0.05"), "注册-熟人"),
        CONSTANT_REG_CONTACTS(new BigDecimal("0.05"), "注册-人脉"),

        CONSTANT_PARTNER(new BigDecimal("0.05"), "合伙人");
        private String title;
        private BigDecimal val;


        KeyPercentage(BigDecimal val, String title) {
            this.val = val;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        public BigDecimal val() {
            return val;
        }

        public BigDecimal needToPay(BigDecimal val) {
            return val.multiply(this.val());
        }
    }

    public enum TalkType implements TitleEnum {

        PROJECT_GROUP_TALK("项目群聊"), NORMAL_GROUP_TALK("普通群聊");

        private String title;

        TalkType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum JobConstantEnum implements TitleEnum {
        SALARY("薪资"), EXPERIENCE("工作经验");
        private String title;

        JobConstantEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum WorkState implements TitleEnum {
        WORKING("上班中"), WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN("待确认离职"), ALREADY_RESIGN("已离职"), WORK_FAIL("上班失败");

        private String title;

        WorkState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum FindJobState implements TitleEnum {
        WORKING_NOT_CONSIDER("在职暂不考虑"), WORKING_FIND_JOB("在职寻找工作"), RESIGN_FIND_JOB("离职寻找工作"), NEWCOMER_FIND_JOB("新手寻找工作");

        private String title;

        FindJobState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum ResignEnum implements TitleEnum {
        QUIT("辞职"), DISMISS("辞退");
        private String title;

        ResignEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RecruitmentType implements TitleEnum {

        FREE("免费"), ONCE("一次性悬赏"), MONTHLY("按月悬赏");

        private String title;

        RecruitmentType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RecruitmentState implements TitleEnum {
        WAIT_FOR_PAY("待付款"),
        ADD_PEOPLE_FOR_PAY("增加招聘人数待付款"),
        PAY_OFFLINE("线下付款待确认"),
        NORMAL("正常"),
        PAUSE("暂停"),
        STOP("停止"),
        FROZEN("冻结"),
        DELETE("删除");
        private String title;

        RecruitmentState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RecState implements TitleEnum {

        NOT_WORK("未上班"),
        STORE_RETURN("店家退回"),

        WAIT_FOR_STORE_CONFIRM_WORK("待店家确认上班"),
        WORKING("上班中"),
        WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN("待员工确认离职"),
        ALREADY_RESIGN("离职"),
        WORK_FAIL("上班失败");

        private String title;

        RecState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }


    public enum PositionEnum implements TitleEnum {
        BOSS("老板"), HR_MANAGER("人事经理"), AREA_MANAGER("区域经理"), STORE_FOREMAN("店铺领班");
        private String title;

        PositionEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum BenefitType implements TitleEnum {
        CAR_FEE("车费"),
        WORK_INSURANCE("工作保险"),
        FREE_TRAING("免费培训"),
        SALARY_PROTECTION("工资保障"),
        QUICK_LOAN("快速贷款");
        private String title;

        BenefitType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum BenefitItemType implements TitleEnum {
        FREE_TRAIN_EMPLOYMENT_PLANNING("职业规划"),
        FREE_TRAIN_NORMAL("免费培训");
        private String title;

        BenefitItemType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum BenefitApplyState implements TitleEnum {
        NONE("未申请"), REVIEW("审核中"), SUCCESS("申请成功"), FAILED("申请失败");
        private String title;

        BenefitApplyState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum BenefitItemState implements TitleEnum {
        NORMAL("正常"), USED("已使用"), EXPIRE("已过期");
        private String title;

        BenefitItemState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum RefundEnum implements TitleEnum {
        HAS_REFUNDED("已退款"), HAS_NOT_REFUNDED("未退款");
        private String title;

        RefundEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum ApplyEnum implements TitleEnum {
        NONE("当前没有申请"), IN_REVIEW("审核中"), REVIEW_SUCCESS("审核成功"), REVIEW_FAILED("审核失败"), OFFLINE_PAY_CONFIRM("线下付款确认中"),
        SUCCESS("申请已处理完成（付款成功）");

        private String title;

        ApplyEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum ApplyType implements TitleEnum {
        JOB_REFUND("离职退款"), JOB_CUT("岗位减少人数退款"), JOB_RESIGN("退押金"), JOB_ALL("暂停招聘，全额退款");
        private String title;

        ApplyType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum RenewType implements TitleEnum {
        SUCCESS("支付成功"), FAILED("支付失败"), WAIT("线下支付待确认");

        private String title;

        RenewType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

/*    public enum StateEnum implements TitleEnum {
        NONE("当前没有申请"), IN_REVIEW("审核中"), REVIEW_SUCCESS("审核成功"), REVIEW_FAILED("审核失败"), OFFLINE_PAY_CONFIRM("线下付款确认中"), SUCCESS("申请已处理完成（付款成功）");
        private String title;

        StateEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }*/

    public enum EntrepreneursType implements TitleEnum {

        APPLY_JUNIOR("初级创业者"), APPLY_SENIOR("高级创业者"), APPLY_JUNIOR_TO_SENIOR("初级升高级");


        private String title;

        EntrepreneursType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum EarningType implements TitleEnum {
        ENTREPRENEUR_AWARD("创业者-创业奖励"), ENTREPRENEUR_REGISTER_AWARD("创业者-注册工作奖励"), ENTREPRENEUR_RECOMMEND_AWARD("创业者-推荐工作奖励"),

        PARTNER_ONCE_AWARD("合伙人-一次性悬赏岗位奖励"), PARTNER_MONTHLY_AWARD("合伙人-按月悬赏岗位奖励");

        private String title;

        EarningType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum WorkerQueryType implements TitleEnum {
        ONCE_AWARD_LESS_THAN_ONE_MONTH("一次性悬赏-上班未满一个月"), ONCE_AWARD_MORE_THAN_ONE_MONTH("一次性悬赏-上班已满一个月"),
        ONCE_AWARD_RESIGN("一次性悬赏-上班结束"), ONCE_AWARD_WORK_FAILED("一次性悬赏-上班失败"),
        MONTHLY_AWARD_LESS_THAN_ONE_MONTH("按月悬赏-上班未满一个月"), MONTHLY_AWARD_MORE_THAN_ONE_MONTH("按月悬赏-上班已满一个月"),
        MONTHLY_AWARD_RESIGN("按月悬赏-上班结束"), MONTHLY_AWARD_WORK_FAILED("按月悬赏-上班失败");

        private String title;

        WorkerQueryType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum WithdrawType implements TitleEnum {
        WITHDRAW_ACCOUNT_ALIPAY("支付宝"), WITHDRAW_ACCOUNT_WX("微信"), WITHDRAW_ACCOUNT_BANK_CARD("银行卡");

        private String title;

        WithdrawType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum ComplainType implements TitleEnum {
        COMPLAINT_TO_STORE("创业者-投诉店铺"), COMPLAINT_TO_USER("创业者-投诉个人"), COMPLAINT_TO_SCHOOL("投诉学校"),
        USER_COMPLAINT_TO_STORE_UNCONFIRM_WORK("员工投诉店铺未确认上班"), STORE_COMPLAINT_TO_USER_UNCONFIRM_RESIGN("店铺投诉员工未确认离职"),
        ENTREPRENEUR_COMPLAINT_WORK("合伙人管理投诉"), PARTNER_COMPLAINT_WORK("创业者管理投诉");

        private String title;

        ComplainType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum MomentsType implements TitleEnum {

        TELETEXT("图文"), LINK("链接");
        private String title;

        MomentsType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum StateEnum implements TitleEnum {
        YES("是"), NO("否"), HIDE("隐藏");
        private String title;

        StateEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum AdCategoryEnum implements TitleEnum {
        AD_APP_START("APP启动广告"), AD_INDEX("首页广告");
        private String title;

        AdCategoryEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum AdStatusEnum implements TitleEnum {
        AD_INNER("内部广告"), AD_OUTER("外部广告"), AD_PICTURE("图片展示");
        private String title;

        AdStatusEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum AdTypeEnum implements TitleEnum {
        AD_VIP("会员广告"), AD_SHOP("店铺广告"), AD_JOB("岗位广告"), AD_COUPON("卡券广告"), AD_ARTICLE("咨讯广告"),
        AD_VIDEO("视频广告"), AD_SCHOOL("学校广告"), AD_COURSE("课程广告");
        private String title;

        AdTypeEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum AdShowWayEnum implements TitleEnum {
        AD_APP("APP端"), AD_WX("微信端");
        private String title;

        AdShowWayEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RewardEnum implements TitleEnum {
        HAS_REWARD("已支付"), HAS_NOT_REWARD("未满足支付条件");
        private String title;

        RewardEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum SrAccountApplyRecordType implements TitleEnum {
        PUBLISH_JOB("发布岗位"),
        JOB_ADD_EMPLOYEE_COUNT("岗位增加招聘人数"),
        JOB_PAY("待付款岗位付款"),
        RENEW_JOB("岗位续费"),
        ENTREPRENEUR_PAY("创业者付款"),
        PARTNER_PAY("合伙人付款"),
        COURSE_PAY("课程报名付款"),
        SCHOOL_PAY("学校付款");
        private String title;

        SrAccountApplyRecordType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RenewalsDealType implements TitleEnum {
        DO_NOT_DEAL("暂未处理"),
        DEAL_BY_RENEWAL("续费"),
        DEAL_BY_RETURN("退押金"),
        DEAL_BY_PLATFORM("平台介入");
        private String title;

        RenewalsDealType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    /**
     * 合作/转让 操作枚举
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public enum OperateType implements TitleEnum {
        COOPERATE("合作"),
        TRANSFER("转让");
        private String title;

        OperateType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    /**
     * 卡券枚举
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public enum CardCouponType implements TitleEnum {
        CARD("卡"),
        COUPON("券");
        private String title;

        CardCouponType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    /**
     * 卡券状态枚举
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public enum CardCouponState implements TitleEnum {
        NORMAL("正常"),
        PAUSE("暂停"),
        EXPIRE("过期");
        private String title;

        CardCouponState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    /**
     * 卡券领取状态枚举
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public enum CardCouponReceiveState implements TitleEnum {
        NOT_RECEIVING("未领取"),
        RECEIVED("已领取未使用未过期"),
        RECEIVED_EXPIRE("已领取未使用已过期"),
        RECEIVED_USED("已领取已使用");
        private String title;

        CardCouponReceiveState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum CourseApplyState implements TitleEnum {
        COURSE_ORDER_WAIT_FOR_PAY("待付款"),
        COURSE_ORDER_OFFLINE_PAY_CONFIRM("线下付款待确认"),
        COURSE_ORDER_CLOSED("订单关闭"),
        COURSE_ORDER_PAID("已付款"),
        COURSE_ORDER_TRAINED_UNCOMMENT("已培训未评价"),
        COURSE_ORDER_TRAINED_COMMENTED("已培训已评价"),
        COURSE_ORDER_REFUNDING("退款中"),
        COURSE_ORDER_REFUNDED("已退款"),
        COURSE_ORDER_REFUND_FAIL("退款失败"),
        COURSE_ORDER_REFUND_CANCEL("取消退款");

        private String title;

        CourseApplyState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum CourseRefundReason implements TitleEnum {
        CAN_NOT_GO("临时有事无法去参加了"),
        SCHOOL_CAN_NOT_TRAING("培训学校无法按计划实施培训"),
        ERROR_APPLY("报名错误，退款后重新报名"),
        HAVE_FEE_TRAING("我有免费培训资格了，要申请退款");
        private String title;

        CourseRefundReason(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum CourseRefundWay implements TitleEnum {
        SCHOOL_BALANCE("学校账户余额"),//其实就是学校冻结金额中的钱退款，当courseApply.state = COURSE_ORDER_PAID
        SCHOOL_BALANCE_WITHDRAW("学校可提现账户余额"), //其实就是学校余额-冻结金额，当courseApply.state = 已培训两种状态时
        SCHOOL_PAY_ONLINE("网上支付");//学校的可提现金额不足，在后台进行付款时，为该状态
        private String title;

        CourseRefundWay(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RedPacketCategory implements TitleEnum {
        RED_PACKET_COURSE_TRAIN("课程培训红包"), RED_PACKET_ONE_YUAN("一元购红包");

        private String title;

        RedPacketCategory(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RedPacketSendType implements TitleEnum {
        RED_PACKET_SEND_ALL("所有用户"), RED_PACKET_SEND_ONE("单个用户"), RED_PACKET_SEND_SPECIAL("特定用户");

        private String title;

        RedPacketSendType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum RedPacketSpecialType implements TitleEnum {
        RED_PACKET_SPECIAL_REGISTER_MEMBER(1, "注册用户"),
        RED_PACKET_SPECIAL_REG(2, "注册用户的推荐人"),
        RED_PACKET_SPECIAL_REC(3, "推荐用户去上班"),
        RED_PACKET_SPECIAL_RECED(4, "被推荐去上班的用户"),
        RED_PACKET_SPECIAL_PARTNER(5, "合伙人"),
        RED_PACKET_SPECIAL_JUNIOR_ENTREPRENEURS(6, "初级创业者"),
        RED_PACKET_SPECIAL_SENIOR_ENTREPRENEURS(7, "高级创业者");

        private Integer num;
        private String title;

        RedPacketSpecialType(Integer num, String title) {
            this.num = num;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        public Integer getNum(){
            return num;
        }
    }

    public enum RedPacketState implements TitleEnum {
        NORMAL("正常"), EXPIRE("已过期"), USED("已使用");

        private String title;

        RedPacketState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum PhotoType implements TitleEnum {
        ENVIRONMENT("学校环境"),  PRODUCTION("学校作品");

        private String title;

        PhotoType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }
}
