package com.thousandsunny.thirdparty.domain.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.BaseFriendsService;
import com.thousandsunny.core.domain.service.SmsService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartySocialAccountService;
import com.thousandsunny.thirdparty.model.ThirdPartySocialAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static com.thousandsunny.core.ModuleKey.OK;
/**
 * 如果这些代码有用，那它们是guitarist在8/3/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/base/members", produces = APPLICATION_JSON_UTF8_VALUE)
public class BaseMemberController {

    private static final String[] app_member_model = new String[]{
            "token",
            "headImage.path:headerImageUrl",
            "username:userName",
            "gender",
            "mobile:phoneNumber",
            "birthday",
            "province.id:provinceId",
            "city.id:cityId",
            "area.id:areaId",
            "province.name:province",
            "city.name:city",
            "area.name:area",
            "company.id:companyId",
            "company.logo:companyLogo",
            "company.name:companyName",
            "department.id:departmentId",
            "department.name:departmentName",
            "position.id:positionId",
            "position.name:positionName"
    };

    private static final String[] app_simple_member_model = new String[]{
            "token",
            "headImage.path:headerImageUrl",
            "username:userName",
            "birthday",
            "province.name:province",
            "city.name:city",
            "area.name:area",
            "company.id:companyId",
            "department.name:departmentName",
            "position.name:positionName"
    };
    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private ThirdPartySocialAccountService socialAccountService;
    @Autowired
    private BaseFriendsService friendsService;

    /**
     * 临时测试接口，查看验证码倒叙排列
     */
    @RequestMapping(value = "/test/sms", method = GET)
    public ResponseEntity findSmsRecord() {
        return ok(smsService.findSmsRecord());
    }

    /**
     * 向用户发起手机验证码
     */
    @RequestMapping(value = "/sms", method = POST)
    public ResponseEntity sendSms(String phoneNumber, SmsType smsType) {
        baseMemberService.sendValidateCode(phoneNumber, smsType);
        return OK;
    }

    /**
     * 注册
     */
    @RequestMapping(value = "/reg", method = POST)
    public ResponseEntity reg(String phoneNumber, String code, String password,PhoneType platformType,String version) {
        baseMemberService.register(phoneNumber, password, code,platformType,version);
        return OK;
    }

    /**
     * 获取用户信息
     */
    @RequestMapping(value = "/token/{token}", method = GET)
    public ResponseEntity info(@PathVariable String token) {
        Member member = baseMemberService.findByToken(token);
        JSONObject jsonObject = propsFilter(member, app_member_model);
        socialAccountService.wrapThirdPartyInfo(member, jsonObject);
        return ok(jsonObject);
    }

    /**
     * 获取用户基本信息-观察者的身份
     */
    @RequestMapping(value = "/simple/token/{userToken}_{checkedUserToken}", method = GET)
    public ResponseEntity simpleInfo(@PathVariable String userToken, @PathVariable String checkedUserToken) {
        Member member = baseMemberService.findByToken(userToken);
        JSONObject jsonObject = propsFilter(member, app_simple_member_model);
        jsonObject.put("isFriend", friendsService.isFriends(userToken, checkedUserToken));
        return ok(jsonObject);
    }

    /**
     * 修改用户基本信息
     */
    @RequestMapping(method = PUT)
    public ResponseEntity updateBasicInfo(String userToken, Member member, Long provinceId, Long cityId, Long areaId, String userName) {
        ifNotNullThen(userName, t -> member.setUsername(userName));
        ifNotNullThen(provinceId, t -> member.setProvince(new Region(t)));
        ifNotNullThen(cityId, t -> member.setCity(new Region(t)));
        ifNotNullThen(areaId, t -> member.setArea(new Region(t)));
        baseMemberService.updateBasicInfo(userToken, member);
        return OK;
    }

    /**
     * 手机登陆
     */
    @RequestMapping(value = "/mobile/{phoneNumber}", method = POST)
    public ResponseEntity info(@PathVariable String phoneNumber, String password) {
        Member member = baseMemberService.loginByMobile(phoneNumber, password);
        JSONObject jsonObject = propsFilter(member, app_member_model);
        socialAccountService.wrapThirdPartyInfo(member, jsonObject);
        return ok(jsonObject);
    }

    /**
     * 第三方登陆
     */
    @RequestMapping(value = "/tpAccount", method = POST)
    public ResponseEntity tpAccountLogin(ThirdPartySocialAccount socialAccount) {
        Member member = socialAccountService.findByTpAccount(socialAccount);
        JSONObject jsonObject = propsFilter(member, app_member_model);
        socialAccountService.wrapThirdPartyInfo(member, jsonObject);
        return ok(jsonObject);
    }

    /**
     * 绑定/解绑第三方
     */
    @RequestMapping(value = "/tpAccount", method = PUT)
    public ResponseEntity toggleTpAccount(ToggleAction operatorType, String userToken, ThirdPartySocialAccount socialAccount) {
        socialAccountService.bindOrUnbindThirdParty(operatorType, userToken, socialAccount);
        return OK;
    }

    /**
     * 第三方登陆绑定手机
     */
    @RequestMapping(value = "/tpAccount/mobile", method = PUT)
    public ResponseEntity tpAccountBindMobile(ThirdPartySocialAccount socialAccount, String phoneNumber, String code) {
        socialAccountService.thirdPartyBindMobile(socialAccount, phoneNumber, code);
        return OK;
    }

    /**
     * 取用户全部信息
     */
    @RequestMapping(method = GET)
    public ResponseEntity all() {
        return ok(baseMemberService.findAll());
    }

    /**
     * 根据账号获取用户信息
     */
    @RequestMapping(value = "/nickName/{nickName}", method = GET)
    public ResponseEntity infoByNickName(@PathVariable String nickName) {
        return ok(baseMemberService.findByUsername(decodePathVariable(nickName)));
    }

    /**
     * 签到
     */
    @RequestMapping(value = "/signIn", method = POST)
    public ResponseEntity signIn(Long memberId) {
        return ok(baseMemberService.signIn(memberId));
    }

    /**
     * 姓名模糊匹配
     */
    @RequestMapping(value = "/{userNamePattern}", method = GET)
    public Result usernameLike(@PathVariable String userNamePattern, PageVO pageVO) {
        return OK(baseMemberService.usernameLike(decodePathVariable(userNamePattern), pageVO.pageRequest()));
    }

    /**
     * 忘记密码
     */
    @RequestMapping(value = "/forgetPwd", method = PUT)
    public ResponseEntity forgetPwd(String phoneNumber, String code, String password) {
        baseMemberService.forgetPwd(phoneNumber, code, password);
        return OK;
    }


    /**
     * 修改密码
     */
    @RequestMapping(value = "/restPwd", method = PUT)
    public ResponseEntity resetPwd(String userToken, String oldPassword, String password) {
        baseMemberService.resetPwdByOldPwd(userToken, password,oldPassword);
        return OK;
    }

    /**
     * 设置支付密码
     */
    @RequestMapping(value = "/setPayPwd", method = PUT)
    public Result setPayPwd(String userToken, String telCode, String password) {
        baseMemberService.setPayPwd(userToken, password,telCode);
        return OK();
    }


    /**
     * 修改支付密码
     */
    @RequestMapping(value = "/restPayPwd", method = PUT)
    public Result restPayPwd(String userToken, String oldPassword, String password) {
        baseMemberService.restPayPwd(userToken, password,oldPassword);
        return OK();
    }

}
