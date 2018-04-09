package com.thousandsunny.service;

import com.thousandsunny.common.entity.ModuleTip;

/**
 * 如果这些代码有用，那它们是guitarist在11/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public enum ModuleTips implements ModuleTip {

    TIP_NO_AUTHORITY("4001", "无权限操作"),
    TIP_JOIN_NO_GROUP("4002", "该群不存在!"),

    TIP_JOIN_A_GROUP_BEFORE("4003", "你未加入该群!"),

    TIP_JOIN_B_GROUP_BEFORE("4004", "被转让之人未加入该群!"),

    TIP_JOIN_C_GROUP_BEFORE("4005", "你已经加入过该群!"),

    TIP_JOIN_D_GROUP_BEFORE("4006", "添加的人员中存在已经加入过该群的!"),

    TIP_HAS_NOT_PERMISSION_TO_VIEW("4007", "没有权限查看！"),

    TIP_NO_COMMENTS("4008", "该说说不存在"),

    TIP_NO_MEMBER("4009", "用户不存在"),

    TIP_NO_COMMENTARY("4010", "评论不存在"),

    TIP_FAVORITE("4011", "已经点赞"),

    TIP_NOT_FAVORITE("4012", "还未点赞"),

    TIP_CANCEL_FAVORITE("4013", "已经取消点赞"),

    TIP_NO_VIDEO("4014", "视频不存在"),

    TIP_COLLECT("4015", "已经收藏"),

    TIP_NOT_COLLECT("4016", "还未收藏"),

    TIP_CANCEL_COLLECT("4017", "已经取消收藏"),

    TIP_NO_FIND("4018", "获取群聊失败"),

    TIP_NO_FINDSINGLE("4019", "获取单聊失败"),

    TIP_NO_CHANSHUERROR("4020", "没有该数据信息或参数出错"),

    TIP_JOIN_A_GROUP_NOUSER("4021", "该群没有成员了已经解散!"),

    TIP_OPEN_ID_IS_NULL("1111", "openId为空"),

    TIP_DEMO_PAIR("9999", "4234234"),

    TIP_YES_BLOCKED("4022", "您已屏蔽过了"),

    TIP_NOT_PUBLISH_JOB("4023", "未发布该职位招聘"),

    TIP_CANNOT_CHANGE_STATE("4024", "招聘当前状态无法改变"),

    TIP_PAUSE("4025", "招聘已经暂停"),

    TIP_RESUME("4026", "招聘已经恢复"),

    TIP_NO_BLOCKED("4027", "该用户不在您的屏蔽列表中"),

    TIP_NOT_NEED_PAY("4028", "岗位无需支付"),

    TIP_NO_JOB("4029", "该职位不存在"),

    TIP_HAS_WORKER_JOB("4030", "该岗位已经有员工"),

    TIP_HAS_REFUND_JOB("4031", "该岗位还需要退费"),

    TIP_COUNT_OUT_OF_RANGE("4032", "人数超过范围"),

    TIP_JOB_ERROR_TYPE("4033", "岗位类型错误"),

    TIP_NO_ACCOUNT("4034", "账户不存在"),

    TIP_NO_JOB_RECORD("4035", "岗位推荐记录不存在"),

    TIP_ABNORMAL_STATUS("4036", "推荐记录状态不正常"),

    TIP_NO_MEMBER_JOB_RECORD("4037", "没有推荐上班记录"),

    TIP_NO_NEED_RENEW("4038", "无需续费"),

    TIP_ERROR_PAY_PASSWORD("4039", "账户支付密码错误"),

    TIP_NO_RESUME("4040", "用户简历为空"),

    TIP_HAS_JOBRECORD("4041", "已经接受过该岗位推荐"),

    TIP_HAS_WORKING("4042", "该状态不能解除岗位推荐"),

    TIP_CAN_NOT_RAISE_REQUEST("4043", "该状态不能发起上班请求"),

    TIP_NOT_LEAVEWORKING("4044", "该状态不能确认离职"),

    TIP_HAS_APPLY("4045", "您已成为高级合伙人或正在升为高级合伙人"),

    TIP_NO_APPLY("4046", "该用户没有申请记录"),

    TIP_NO_ENTREPRENEURS("4047", "该用户不是创业者"),

    TIP_NO_PAY("4048", "该状态下不能进行付款"),

    TIP_SHOP_NOT_EXIST("4053", "店铺不存在"),

    TIP_REGION_OUT_OF_RANGE("4054", "申请区域数超出范围"),

    TIP_NO_PARTNER_APPLY("4055", "未申请合伙人"),

    TIP_ERROR_PARTNER_APPLY_STATUS("4056", "申请状态不符合"),

    TIP_UNSUPPORTED_PAY_TYPE("4057", "支付方式不支持"),

    TIP_PARTNER_WRONG_AREA("4058", "未申请该地区"),

    TIP_NO_WITHDRAW_ACCOUNT("4059", "提现账户不存在"),

    TIP_CAN_NOT_REFRESH("4060", "刷新时间未到"),

    TIP_CAN_NOT_EDIT_JOB("4061", "已有招聘，无法编辑岗位"),

    TIP_HAS_SUBMIT("4062", "已经发送过请求"),

    TIP_NO_SUBMIT("4063", "没有添加请求记录"),

    TIP_INVITER_NOT_EXIST("4066", "邀请人不存在或邀请码填写错误!"),

    TIP_NO_JOB_TYPE("4067", "工作类型不存在"),

    TIP_NO_ACCOUNT_FLOW("4068", "账单流水不存在"),

    TIP_TOFAY_HAS_FRESHED("4069", "今天已经刷新过一次了!"),

    TIP_NO_ENOUGH_BALANCE("4070", "账户余额不足"),

    TIP_JOB_CANNOT_DELETE("4071", "不允许删除"),

    TIP_CANT_REVIEW("4072", "已经审批"),

    TIP_NO_PAY_JOB("4073", "不存在需要付款的岗位"),

    TIP_CAN_NOT_CALLBACK("4074", "不能撤回"),

    TIP_NOT_ACCEPT_REC("4075", "没有接收推荐"),

    TIP_NO_APPLY_RECORD("4076", "不存在符合的申请记录"),

    TIP_HAS_PARTNER_APPLY("4078", "申请合伙人地区不得超过两个"),

    TIP_ERROR_IDENTITY("4079", "身份审核有误"),

    TIP_APPROVING("4080", "审批中"),

    TIP_OUT_RANGE("4081", "薪资超出范围"),

    TIP_NO_PICTURE("4082", "图片不存在"),

    TIP_NO_MEMBEREXTINFO("4083", "身份审核信息不存在"),

    TIP_NO_NEXT_JOB("4084", "没有更多的岗位"),

    TIP_NO_INTENTION("4085", "求职意向未填写"),

    TIP_IS_ENTREPRENEURS("4086", "该用户已经是创业者"),

    TIP_NO_JOBRECORD("4087", "该状态不能接受岗位推荐"),

    TIP_NO_BINDING("4088", "已经存在三者关系或上班中"),

    TIP_HAS_PARTNER("4089", "该地区已经存在合伙人"),

    TIP_NO_REAPPLY("4090", "您之前的申请还未通过审核或还未付款"),

    TIP_INVALID_DATA("4091", "非法数据"),

    TIP_NO_VERIFY_CODE("4092", "该验证码不存在"),

    TIP_VERIFY_CODE_FALSE("4093", "验证失败 "),

    TIP_TIME_OUT("4094", "距离您成为初级创业者时间超过一个月了，不能升为高级了"),

    TIP_NOT_APPLY("4095", "您是初级创业者只能申请初级升高级"),

    TIP_PLACE_FALSE("4096", "省市区格式错误"),

    TIP_ACCOUNT_FLOW_TYPE_FALSE("4097", "账单流水类型错误"),

    TIP_NO_PROVINCE("4098", "不存在该省"),

    TIP_MORE_THAN_ONE_PROVINCE("4099", "无法匹配唯一城市"),

    TIP_PARAM_FALSE("4100", "参数有误"),

    TIP_NO_RENEWAL_RECORD("4101", "没有续费记录!"),

    TIP_NO_UNREAD_MSG("4102", "暂无未读消息"),

    TIP_NO_REG_REL("4103", "没有推荐注册关系!"),

    TIP_NO_REC_REL("4104", "没有推荐工作关系!"),

    TIP_HAS_APPLYED("4105", "已经发起过申请了，请等待审核结果"),

    TIP_NO_EARNING("4106", "收益不存在"),

    TIP_NO_BENEFITREL("4107", "好处不存在"),

    TIP_NOT_REC_MYSELF("4108", "自己不能推荐自己"),

    TIP_NO_SHOP_COOPERATE("4109", "店铺合作信息不存在"),

    TIP_NO_SHOP_TRANSFER("4110", "店铺转让信息不存在"),

    TIP_ONE_DAY_ONE_TIMES("4111", "一天只能刷新一次哦"),

    TIP_COUPON_CARD_RECEIVED("4112", "卡劵已经被领取,不能编辑"),

    TIP_COUPON_CARD_CANNOT_DELETE("4113", "卡劵已经被领取,不能删除"),

    TIP_CARD_NOT_EXIST("4114", "卡或券不存在"),

    TIP_COUPON_NOT_EXIST("4115", "劵不存在"),

    TIP_SHOP_EXIST("4116", "店铺已经存在"),

    TIP_NO_SCHOOL("4117", "学校不存在"),

    TIP_NO_COURSE("4118", "课程不存在"),

    TIP_COUPON_CARD_CANNOT_RECEIVED_AGAIN("4119", "你已经领取,不能重复领取"),

    TIP_COURSE_HASNT_SIGN_DATE("4120", "课程暂未设置报名时间"),

    TIPS_NOT_HAS_CARD_COUPON("4121", "该用户没有该卡劵"),

    TIPS_SHOP_CARD_COUPON_NOT_EXIST("4122", "店家没有发这个卡劵"),

    TIPS_CARD_COUPON_OUT_OF_DATE("4123", "卡劵已过期"),

    TIP_NO_COURSEAPPLY("4124", "课程订单不存在"),

    TIP_COURSE_CAN_NOT_CANCEL_UNDER_LINE("4125", "当前状态下不能取消线下付款"),

    TIP_COURSE_CAN_NOT_CANCEL_TRAIN("4126", "当前状态下不能取消培训"),

    TIP_COURSE_CAN_NOT_FINISH_TRAIN("4127", "暂不能完成培训"),

    TIP_NO_SCHOOLAPPLY("4128", "学校申请不存在"),

    TIP_NO_SCHOOL_PHOTO("4129", "该学校照片不存在"),

    TIP_CAN_NOT_UPLOAD_IMG("4130", "未完成培训，暂不能上传证书"),

    TIP_NO_CERTIFICATE("4131", "暂无培训证书"),

    TIP_NO_COURSE_EVALUATION("4132", "该课程评论不存在"),

    TIP_CAN_NOT_REFUND("4133", "不满足条件，不能申请退款"),

    TIP_REFUND_APPLY_NOT_EXIST("4134", "该申请退款不存在"),

    TIP_COURSE_REFUND_STATE_ERROR("4135", "该状态不能取消"),

    TIP_INFORMATION_IS_WRONG("4136", "填写信息有误"),

    TIP_CAN_NOT_REVIEW("4137", "当前不能审核"),

    TIP_CAN_NOT_DELETE_COURSE("4138", "不能删除该课程"),

    TIP_NO_COURSE_PHOTOS("4139", "请先上传课程图片"),

    TIP_NO_COURSE_SIGN_UP("4140", "报名时间不存在"),

    TIP_CAN_NOT_DELETE_SIGN_UP_DATE("4141", "不能删除报名时间"),

    TIP_CAN_NOT_DOWN_SIGNED_NUM("4142", "不能小于已报名人数"),

    TIP_REFUND_AMOUNT_ERROR("4143", "申请金额超出"),

    TIP_HAS_NOT_WRITE_DATE("4144", "请先填写时间"),

    TIP_NO_ROLE("4145", "角色不存在"),

    TIP_CAN_NOT_SIGN_UP("4146", "当天报名人数已满"),

    TIP_SCAN_IN_TEN_NUM("4147", "评价分数不能超过10分"),

    TIP_HAS_COLLECT_OR_CANCEL("4148", "你已经收藏或者取消收藏了"),

    TIP_NO_REDPACKETRECEIVE("4149", "红包不存在"),

    TIP_NO_BANK("4150", "银行不存在"),

    TIP_SCHOOL_APPLY_EXIST("4151", "你已经申请,请等待审核");

    private String code;
    private String message;

    ModuleTips(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
