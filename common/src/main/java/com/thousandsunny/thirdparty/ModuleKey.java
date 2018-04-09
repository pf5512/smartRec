package com.thousandsunny.thirdparty;


import com.thousandsunny.common.entity.TitleEnum;

public class ModuleKey {

    public enum AppliedUserState implements TitleEnum {
        APPLY("报名的"), CHOSEN("被选中的");
        private String title;

        AppliedUserState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum ChargeType implements TitleEnum {
        PAY_IN("入账"), PAY_OUT("出账");
        private String title;

        ChargeType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum RecordType implements TitleEnum {
        BILL_RECHARGE("充值"), BILL_WITHDRAW("提现"), BILL_REFUND("退款"), BILL_PAY_ONLINE("在线付款"),
        BILL_PAY_OFFLINE("线下付款"),
        BILL_INCOME("收益"),//除了平台的收益:合伙人收益，创业者收益，岗位招聘收益
        PLATFORM_INCOME("平台收益"), PLATFORM_IN("平台收入"), PLATFORM_OUT("平台出钱");
        private String title;

        RecordType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum SourceType implements TitleEnum {

        JOB_NEW("新开", "岗位招聘"),
        JOB_ADD("新增", "岗位招聘-增加"),
        JOB_CUT("减少", "岗位招聘-退费"),
        JOB_RENEW("续费", "岗位招聘-续费"),
        JOB_RESIGN("退押金", "岗位招聘-退押金"),
        JOB_REFUND("全额退费", "岗位招聘-退款"),

        PARTNER_APPLY("合伙人申请", "合伙区域费"),
        ENTREPRENEUR_APPLY("创业者申请", "创业费(?)"),
        EMPLOYEE_CAR_FEE("车旅费", "上班好处-100元车旅费"),
        ENTREPRENEUR_AWARD("创业者-创业奖励", "创业奖励"),
        ENTREPRENEUR_REGISTER_AWARD("创业者-注册工作奖励", "注册工作奖励"),
        ENTREPRENEUR_RECOMMEND_AWARD("创业者-推荐工作奖励", "推荐工作奖励"),
        PARTNER_ONCE_AWARD("合伙人-一次性悬赏岗位奖励", "悬赏?元"),
        PARTNER_MONTHLY_AWARD("合伙人-一按月悬赏岗位奖励", "悬赏?元/月"),
        PLATFORM_PROFIT_JOB("平台收益流水", "平台收益流水"),//仅在定时器中用，平台分到的钱的source类型
        DEFAULTS("违约流水", "违约流水"),

        MEMBER_WITHDRAW("提现", "提现"),//个人
        COURSE_APPLY("课程报名", "课程报名费"),
        COURSE_REFUND("课程报名退费", "课程报名退费"),
        SCHOOL_WITHDRAW("学校提现", "学校提现"),
        SCHOOL_OF_PAY("学校付款", "学校账户后台付款");

        private String title;
        private String remark;

        SourceType(String title, String remark) {
            this.title = title;
            this.remark = remark;
        }


        SourceType(String title) {
            this.title = title;
        }

        public String getRemark() {
            return remark;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum OperatorType implements TitleEnum {
        SURE("确定"), CANCEL("取消");
        private String title;

        OperatorType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }


    public enum ApplyFrom implements TitleEnum {
        FROM_ME("我"), FROM_OTHER("其他人");
        private String title;

        ApplyFrom(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum FlowState implements TitleEnum {
        APPROVAL("审核中"), SUCCESS("成功"), FAILED("审核失败");
        private String title;

        FlowState(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum ChatType implements TitleEnum {
        PICTURE("图片"), VOICE("语音"), TEXT("文字");
        private String title;

        ChatType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum PayType implements TitleEnum {
        PAY_BY_WX("微信支付"), PAY_BY_ALIPAY("支付宝支付"), PAY_BY_BALANCE("余额支付"),
        PAY_OFFLINE("线下支付"), PAY_BY_WX_PUB("微信公众号支付"), ALIPAY_PC_DIRECT("支付宝电脑网站支付");
        private String title;

        PayType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum PayOfflineType implements TitleEnum {
        //pay_offline的子项
        PAY_OFFLINE_BANK("银行支付"), PAY_OFFLINE_CASH("现金支付");
        private String title;

        PayOfflineType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }


    public static String pushAlias(String token) {
        return "yzb_" + token;
    }
}
