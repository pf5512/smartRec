package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.ModuleKey.VisitState;
import com.thousandsunny.core.ModuleKey.PhoneType;
import com.thousandsunny.core.ModuleKey.SubLevelType;
import com.thousandsunny.core.domain.service.BaseFriendsService;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.model.Resume;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.repository.ShopRepository;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartySocialAccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.ThirdPartySocialAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.google.common.collect.ImmutableMap.of;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.JsonUtil.valueIsNull;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.core.ModuleKey.SmsType.BIND_MOBILE;
import static com.thousandsunny.service.ModuleTips.TIP_NO_INTENTION;
import static com.thousandsunny.service.ModuleTips.TIP_NO_RESUME;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_ACCOUNT_NOT_ACTIVE;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static com.thousandsunny.core.ModuleKey.SmsType;

/**
 * 如果这些代码有用，那它们是guitarist在03/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/portal/members", produces = APPLICATION_JSON_UTF8_VALUE)
public class MemberController {

    private static final String[] MEMBER_MODEL = {
            "token",
            "headImage.path:headerImageUrl",
            "realName:realName",
            "username:nickName",
            "hpAccount",
            "accountValid",
            "gender",
            "mobile:phoneNumber",
            "readableBirthday:birthday",
            "province.id:provinceId",
            "isNoDisturb.bool:isPhoneNumberNoDisturb",
            "city.id:cityId",
            "area.id:areaId",
            "province.name:province",
            "city.name:city",
            "city.name:locationCity",
            "area.name:area",
            "company.id:companyId",
            "company.logo:companyLogo",
            "company.name:companyName",
            "department.id:departmentId",
            "department.name:departmentName",
            "position.id:positionId",
            "position.name:positionName",
            "entrepreneurLevel",
            "hasPassed:identityHasPass",
            "accountFreezeBalance"
    };

    private static final String[] MEMBER_MESSAGE_JSON = {
            "headImage.path:headerImageUrl",
            "realName",
            "storeName",
            "username:nickName",
            "mobile:phoneNumber",
            "hpAccount",
            "gender",
            "entrepreneurLevel",
            "partnerLevel:isPartner",
            "isNoDisturb.bool:isPhoneNumberNoDisturb",
    };
    private static final String[] SimpleInfo_Json = {"member.realName:name", "member.mobile:phoneNumber", "idCard.path:IDPic", "half.path:halfBodyPic"};
    @Autowired
    private MemberService memberService;
    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private ThirdPartySocialAccountService socialAccountService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ResumeService resumeService;
    @Autowired
    private MomentsBlockedService momentsBlockedService;
    @Autowired
    private BaseFriendsService baseFriendsService;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private ResumeBlockedService resumeBlockedService;
    @Autowired
    private CardCouponReceiveService cardCouponReceiveService;
    @Autowired
    private RedPacketService redPacketService;


    /**
     * 向用户发起手机验证码
     */
    @RequestMapping(value = "/sms", method = POST)
    public ResponseEntity sendSms(String phoneNumber, SmsType smsType) {
        memberService.sendValidateCode(phoneNumber, smsType);
        if (smsType == BIND_MOBILE) {
            MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberMobile(phoneNumber);
            return ok(of("hasRegisterParent", isNotNull(memberExtInfo) && isNotNull(memberExtInfo.getRecommendUser())));
        }
        return OK;
    }

    /**
     * 手机登陆
     */
    @RequestMapping(value = "/mobile", method = POST)
    public ResponseEntity info(String phoneNumber, String password) {
        Member member = memberService.loginByMobileOrHpAccount(phoneNumber, password);
        return ok(wrapAppMemberModel(member));
    }

    /**
     * 注册
     */
    @RequestMapping(method = POST)
    public ResponseEntity reg(String phoneNumber, String inviteCode, String code, String password, PhoneType platformType, String version) {
        Member member = memberService.register(phoneNumber, password, code, inviteCode, platformType, version);
        return ok(wrapAppMemberModel(member));
    }

    /**
     * 第三方登陆
     */
    @RequestMapping(value = "/tpAccount", method = POST)
    public ResponseEntity tpAccountLogin(ThirdPartySocialAccount socialAccount) {
        Member member = socialAccountService.findByTpAccount(socialAccount);
        return ok(wrapAppMemberModel(member));
    }

    /**
     *
     */
    @RequestMapping(value = "/simpleInfo/{token}", method = GET)
    public ResponseEntity simpleInfo(@PathVariable String token) {
        Member member = baseMemberService.findByToken(token);
        MemberExtInfo extInfo = memberExtInfoService.findByMemberToken(member.getToken());

        JSONObject simpleInfo = propsFilter(extInfo, SimpleInfo_Json);
        simpleInfo.put("IDNumber", extInfo.getIdCardNo());

        ifNotNullThen(extInfo.getIdCard(), i -> simpleInfo.put("IDPic", i.getPath()));
        ifNotNullThen(extInfo.getHalf(), i -> simpleInfo.put("halfBodyPic", i.getPath()));

        simpleInfo.put("identityHasPass", member.getHasPassed());
        return ok(simpleInfo);
    }

    /**
     * 获取用户信息
     */
    @RequestMapping(value = "/token/{token}", method = GET)
    public ResponseEntity info(@PathVariable String token) {
        Member member = baseMemberService.findByToken(token);
        return ok(wrapAppMemberModel(member));
    }

    /**
     * 12.3查询用户信息
     */
    @RequestMapping(value = "/memberInfo", method = GET)
    public ResponseEntity findMemberInfo(String userToken, String checkedUserToken) {
        Member member = memberService.checkUserMessage(checkedUserToken);
        Shop shop = shopRepository.findByOwnerToken(checkedUserToken);
        JSONObject jo = propsFilter(member, MEMBER_MESSAGE_JSON);
        SubLevelType subLevelType = null;
        SubLevelType subLevelType1 = null;
        if (isNotBlank(userToken)) {
            boolean isABlackB = momentsBlockedService.findByMemberTokenAndMomentsMemberToken(userToken, checkedUserToken);
            boolean isBBlackA = momentsBlockedService.findByMemberTokenAndMomentsMemberToken(checkedUserToken, userToken);
            boolean isCollectResume = resumeService.checkIsCollectionResume(userToken, checkedUserToken);
            boolean isHpFriend = baseFriendsService.isFriends(userToken, checkedUserToken);
            Boolean isBBlockedA = resumeBlockedService.isABlockedB(checkedUserToken, userToken);
            subLevelType = memberRecRelService.recRelLevel(userToken, checkedUserToken);
            subLevelType1 = memberRegRelService.regRelLevel(userToken, checkedUserToken);
            jo.put("isABlackB", isABlackB);
            jo.put("isBBlackA", isBBlackA);
            jo.put("isCollectResume", isCollectResume);
            jo.put("isHpFriend", isHpFriend);
            jo.put("isBBlockA", isBBlockedA);
        } else {
            valueIsNull(jo, false, "isABlackB", "isBBlackA", "isCollectResume", "isHpFriend", "isBBlockA");
        }
        Integer recommendSubLevel = subLevelType == null ? 0 : subLevelType.getLevel();
        Integer registerSubLevel = subLevelType1 == null ? 0 : subLevelType1.getLevel();
        ifNotNullThen(member.getUsername(), x -> jo.replace("nickName", decodePathVariable(x)));
        jo.put("recommendSubLevel", recommendSubLevel);
        jo.put("registerSubLevel", registerSubLevel);
        ifNotNullThen(shop, s -> jo.put("storeName", s.getName()));
        jo.put("token", checkedUserToken);
        ifNotNullThen(member.getBirthday(), x -> jo.put("birthday", ISO_DATE_FORMAT.format(x)));
        ifNotNullThen(member.getPartnerLevel(), x -> jo.replace("isPartner", x == YES));
        return ok(jo);
    }

    /**
     * 第三方登陆绑定手机
     */
    @RequestMapping(value = "/tpAccount/mobile", method = PUT)
    public ResponseEntity tpAccountBindMobile(ThirdPartySocialAccount socialAccount, String phoneNumber, String code, String inviteCode, PhoneType platformType, String version) {
        Member member = memberService.thirdPartyBindMobile(socialAccount, phoneNumber, code, inviteCode, platformType, version);
        return ok(wrapAppMemberModel(member));
    }

    private JSONObject wrapAppMemberModel(Member member) {
        JSONObject jsonObject = propsFilter(member, MEMBER_MODEL);
        socialAccountService.wrapThirdPartyInfo(member, jsonObject);

        ifNotNullThen(member.getUsername(), x -> jsonObject.replace("nickName", decodePathVariable(x)));
        Shop shop = shopService.findByOwnerId(member.getId());
        jsonObject.put("storeInfo", propsFilter(shop, "id", "name"));
        Account account = accountService.findByMemberToken(member.getToken());
        jsonObject.put("accountBalance", isNull(account) ? null : account.getBalance());
        jsonObject.put("cardAndCouponCount", cardCouponReceiveService.countMyCardcoupon(member));
        jsonObject.put("redPacketCount", redPacketService.countMyRedPacket(member));
        jsonObject.put("isPartner", memberService.isPartner(member.getToken()));
        jsonObject.put("accountValid", !isNull(account) && account.getValid() == YES);
        ifNotNullThen(account, a -> jsonObject.put("accountFreezeBalance", a.getFreezingAmount()));
        return jsonObject;
    }

    @RequestMapping(value = "/tpAccount/check", method = GET)
    public ResponseEntity tpAccountCheck(ThirdPartySocialAccount thirdPartySocialAccount) {
        ThirdPartySocialAccount socialAccount = socialAccountService.getThirdPartySocialAccount(thirdPartySocialAccount);
        ifNullThrow(socialAccount, TIP_ACCOUNT_NOT_ACTIVE);
        JSONObject body = new JSONObject();
        body.put("userToken", socialAccount.getMember().getToken());
        return ok(body);
    }


    /**
     * 修改个人信息
     */
    @RequestMapping(value = "/member/{userToken}", method = PUT)
    public ResponseEntity updateInfo(@PathVariable String userToken, Member member) {
        memberService.update(userToken, member);

        Resume resume = resumeService.findResume(userToken);
        ifNullThrow(resume, TIP_NO_RESUME);
        ifNullThrow(resume.getIntention(), TIP_NO_INTENTION);
        return OK;
    }

    /**
     * 设置/重置交易密码
     */
    @RequestMapping(value = "/account/{userToken}", method = PUT)
    public ResponseEntity setPwd(@PathVariable String userToken, String code, String password) {
        memberService.setPwd(userToken, code, password);
        return OK;
    }

    /**
     * 修改交易密码
     */
    @RequestMapping(value = "/resetPwd/{userToken}", method = PUT)
    public ResponseEntity resetPwd(@PathVariable String userToken, String oldPassword, String password) {
        memberService.resetPwd(userToken, oldPassword, password);
        return OK;
    }

    /**
     * 记录访问行为
     */
    @RequestMapping(value = "/noteLogin", method = PUT)
    public ResponseEntity noteLogin(String userToken, Double lng, Double lat, Long provinceId, Long cityId, Long areaId,
                                    VisitState type, String deviceVersion, PhoneType platformType) {
        memberService.refreshLastVisit(userToken, lng, lat, provinceId, cityId, areaId, type, deviceVersion, platformType);
        return OK;
    }


    /**
     * 设置电话免打扰
     */
    @RequestMapping(value = "/setDisturb/{userToken}", method = PUT)
    public ResponseEntity setDisturb(@PathVariable String userToken, OperatorType operatorType) {
        memberService.setDisturb(userToken, operatorType);
        return OK;
    }

    /**
     * 更新电话号码
     *
     * @Author mu.jie
     * @Date 2017/3/17
     */
    @RequestMapping(value = "/updateMobile", method = PUT)
    public ResponseEntity updateMobile(String userToken, Integer oldCode, String newPhoneNumber, Integer newCode) {
        memberService.updateMobile(userToken, oldCode, newPhoneNumber, newCode);
        return OK;
    }
}
