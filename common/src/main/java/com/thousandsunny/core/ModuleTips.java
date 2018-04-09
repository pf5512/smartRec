package com.thousandsunny.core;

import com.thousandsunny.common.entity.ModuleTip;
import org.apache.commons.lang3.tuple.Pair;

import static org.apache.commons.lang3.tuple.Pair.of;

/**
 * 如果这些代码有用，那它们是guitarist在7/29/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public enum ModuleTips implements ModuleTip {
    TIP_COMMON_TIP("0000", ""),
    TIP_MOBILE_NOT_FOUND("1015", "手机号不存在!"),

    TIP_MOBILE_EXISTED("1014", "手机号已存在!"),

    TIP_PWD_RESET("1013", "密码已经重置!"),

    TIP_HAS_SIGNED_IN("1012", "无法再次签到!"),

    TIP_NICK_NAME_EXISTED("1011", "账号已经被注册"),

    TIP_NOT_LOGIN("1010", "尚未登陆"),

    TIP_USER_NAME_EXIST("1006", "该用户名已存在"),
    TIP_MOBILE_ILLEAGL("1088", "手机格式不正确"),
    TIP_MEMBER_EXISTED("1008", "该用户名已存在"),

    TIP_SMS_CODE_WRONG("1007", "验证码不确!"),

    TIP_SMS_CODE_OUTDATE("1005", "过期或不存在!"),

    TIP_HAS_NOT_DEPARTMENT("1004", "所在公司没有该部门!"),

    TIP_OLD_PWD_WRONG("1003", "原密码不正确!"),

    TIP_MSG_NOT_EXIST("1002", "消息不存在!"),

    TIP_CANT_READ_AGAIN("1877", "无法再次阅读"),

    TIP_NO_CORRESPONDING_SPACE_WAS_FOUND("1016", "没有找到对应的空间!"),

    TIP_PARAM_ILLEGAL("1108", "参数错误!"),

    TIP_KAPTCHA_WRONG("1109", "验证码不正确!"),

    TIP_TOKEN_WRONG("1110", "验证信息不全"),

    TIP_HP_ACCOUNT_EXISTED("1111", "慧聘账号已存在！");

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
