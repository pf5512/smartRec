package com.thousandsunny.thirdparty.wechat.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by guitarist on 6/17/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
public class WXUserInfo {

    /**
     * subscribe : 1
     * openid : otCP_ju-BjTwgunLTXO-NFg6SwvI
     * nickname : Null
     * sex : 1
     * language : en
     * city : 杭州
     * province : 浙江
     * country : 中国
     * headimgurl : http://wx.qlogo.cn/mmopen/WmwqjsSBsZJpkMHwUQQ5YHg2vlAdt5crJechwQUcs2ChernQNHEB5tiaXaHXTkkcOGEBQec2LJ4lv9icZr1D1sjBLKHNXM4Gmq/0
     * subscribe_time : 1466072152
     * remark :
     * groupid : 0
     * tagid_list : []
     */

    private int subscribe;
    private String openid;
    private String nickname;
    private int sex;
    private String language;
    private String city;
    private String province;
    private String country;
    private String headimgurl;
    private Long subscribe_time;
    private String remark;
    private int groupid;
    private List<?> tagid_list;

}
