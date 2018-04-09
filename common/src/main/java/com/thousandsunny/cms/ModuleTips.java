package com.thousandsunny.cms;

import com.thousandsunny.common.entity.ModuleTip;

/**
 * 如果这些代码有用，那它们是guitarist在7/22/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public enum ModuleTips implements ModuleTip {

    TIP_ROLE_SAVE_SUC("2001", "角色保存成功!"),

    TIP_AUTHORITY_SAVE_SUC("2002", "保存成功"),

    TIP_NOT_CHANNEL("2003", "没有这个栏目"),

    TIP_NO_TAG("2004","标签不存在");

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
