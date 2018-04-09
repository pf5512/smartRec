package com.thousandsunny.thirdparty;

import com.thousandsunny.common.entity.ModuleTip;

/**
 * 如果这些代码有用，那它们是guitarist在8/11/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public enum ModuleTips implements ModuleTip {
    TIP_TP_NOT_BIND("3001", "第三方尚未绑定!"),
    TIP_TP_HAS_BIND("3002", "已经绑定"),
    TIP_HAS_UNBINDED("3003", "已经解绑"),
    TIP_MEMBER_NOT_EXISTED("3004", "用户不存在"),
    TIP_PWD_RESET_SUC("3005", "密码修改成功！"),
    TP_MOBILE_ILLEAGL("3010", "手机号码格式不正确!"),
    TIP_NOT_JOIN_DEPARTMENT("3011", "尚未所属一个部门!"),
    TIP_MEMBER_ACCOUNT_NOT_EXIST("2354", "该会员无账户"),
    TIP_SCHOOL_ACCOUNT_NOT_EXIST("2355","该学校账户不存在"),
    TIP_WITHDRAW_ACCOUNT_NOT_EXIST("2356","该提现账户不存在"),
    TIP_BALANCE_NOT_ENOUGH("2342", "账户可提现金额不足"),
    TIP_ACCOUNT_NOT_ACTIVE("2343", "无激活的第三方账户"),
    TIP_MOBILE_NOT_BIND("3021", "手机号没绑定用户"),
    TIP_MOBILE_BINDED("3021", "手机号已经绑定用户"),
    TIP_PWD_WRONG("3036", "密码错误"),
    TIPS_SCHOOL_ACCOUNT_BALANCE_ENOUGH("3037","学校余额不足"),
    TIPS_ERROR("3038","error");

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
