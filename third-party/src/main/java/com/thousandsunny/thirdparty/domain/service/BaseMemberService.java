package com.thousandsunny.thirdparty.domain.service;


import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.domain.repository.*;
import com.thousandsunny.core.domain.service.*;
import com.thousandsunny.core.model.*;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.easemob.service.EasemobService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.wechat.entity.WXUserInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static cn.jpush.api.utils.StringUtils.isMobileNumber;
import static com.thousandsunny.common.RandomNumberUtil.genInitPwd;
import static com.thousandsunny.common.RandomNumberUtil.genValCode;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.SmsType.*;
import static com.thousandsunny.core.ModuleTips.*;
import static com.thousandsunny.service.ModuleKey.SMS_INIT;
import static com.thousandsunny.service.ModuleKey.SMS_RESET_SUC;
import static com.thousandsunny.service.ModuleTips.TIP_NO_CHANSHUERROR;
import static com.thousandsunny.thirdparty.ModuleTips.*;
import static java.sql.Timestamp.from;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.apache.jackrabbit.util.Text.md5;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
public class BaseMemberService extends BaseService<Member> {

    @Autowired
    private SmsService smsService;
    @Autowired
    private MemberVisitRepository memberVisitRepository;
    @Autowired
    private MemberSignInRecordRepository memberSignInRecordRepository;
    @Autowired
    private MemberScoreRepository memberScoreRepository;
    @Autowired
    private UserScoreContantRepository userScoreContantRepository;
    @Autowired
    private EasemobService easemobService;
    @Autowired
    private CloudFileService cloudPicService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private ALiSmsService aLiSmsSender;
    //    @Autowired
//    private ResumeRepository resumeRepository;
    @Autowired
    private AccountRepository accountRepository;


    /**
     * 密码忘记
     */
    public Member forgetPwd(String mobile, String code, String password) {
        smsService.validateReceiverAndCode(mobile, code, FORGET_PWD);
        Member member = findByMobile(mobile);
        member.setPassword(password);
        baseRepository.save(md5DoublePwd(member));
        aLiSmsSender.sendContent(mobile, SMS_RESET_SUC, SYS_EVENT);
        return member;
    }

    /**
     * 手机号查找
     */
    public Member findByMobile(String mobile) {
        Member member = memberRepository.findByMobileAndIsDelete(mobile, NO);
        ifNullThrow(member, TIP_MOBILE_NOT_BIND);
        return member;
    }


    /**
     * 手机号查找
     */
    public Member loginByMobile(String mobile, String password) {
        Member member = memberRepository.findByMobile(mobile);
        ifNullThrow(member, TIP_MOBILE_NOT_BIND);
        ifFalseThrow(md5(md5(password)).equals(member.getPassword()), TIP_PWD_WRONG);
        return member;
    }

    /**
     * 修改密码_这里的密码算法
     */
    public Member resetPwdByCode(String mobile, String code, String password) {
        smsService.validateReceiverAndCode(mobile, code, RESET_PWD);
        Member member = memberRepository.findByMobile(mobile);
        ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        member.setPassword(password);
        md5DoublePwd(member);
        baseRepository.save(member);
        aLiSmsSender.sendContent(mobile, SMS_RESET_SUC, SYS_EVENT);
        return member;
    }

    /**
     * 验证原密码后更改用户名
     */
    public Member resetPwdByOldPwd(String token, String password, String oldPassword) {
        Member member = memberRepository.findByToken(token);
        ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        ifFalseThrow(md5(md5(oldPassword)).equals(member.getPassword()), TIP_OLD_PWD_WRONG);
        String mobile = member.getMobile();
        member.setPassword(password);
        md5DoublePwd(member);
        baseRepository.save(member);
        aLiSmsSender.sendContent(mobile, SMS_RESET_SUC, SYS_EVENT);
        return member;
    }

    /**
     * 用户注册
     */
    public Pair<Member, MemberExtInfo> register(String phoneNumber, String password, String code, PhoneType phoneType, String version) {
        smsService.validateReceiverAndCode(phoneNumber, code, CREATE_USER);
        return register(phoneNumber, password, phoneType, version);
    }

    /**
     * 第三方_用户注册
     */
    public Pair<Member, MemberExtInfo> tpRegister(String phoneNumber, String password, String code, PhoneType phoneType, String version) {
        smsService.validateReceiverAndCode(phoneNumber, code, BIND_MOBILE);
        return register(phoneNumber, password, phoneType, version);
    }

    /**
     * 用户注册
     */
    private Pair<Member, MemberExtInfo> register(String phoneNumber, String password, PhoneType phoneType, String version) {
        ifTrueThrow(mobileIsExisted(phoneNumber), TIP_MOBILE_EXISTED);
        ifFalseThrow(isMobileNumber(phoneNumber), TIP_MOBILE_ILLEAGL);
        Member rawMember = new Member();
        rawMember.setMobile(phoneNumber);
        rawMember.setPassword(password);
        rawMember = baseRepository.save(md5DoublePwd(rawMember));
        easemobService.registerEasemobUser(rawMember.getToken());//同步用户到环信

        MemberExtInfo memberExtInfo = new MemberExtInfo();
        memberExtInfo.setMember(rawMember);
        memberExtInfo.setPhoneType(phoneType);
        memberExtInfo.setVersion(version);

        String lastIp = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRemoteAddr();
        memberExtInfo.setLastIp(lastIp);
        memberExtInfoService.save(memberExtInfo);
        return of(rawMember, memberExtInfo);
    }


    /**
     * md5加密两次密码
     */
    private Member md5DoublePwd(Member member) {
        member.setPassword(md5(md5(member.getPassword())));
        return member;
    }

    /**
     * 签到
     */
    public Integer signIn(Long memberId) {

        MemberCheckRecord memberCheckRecord = memberSignInRecordRepository.findByMemberId(memberId);
        Integer result;
        if (isNull(memberCheckRecord))
            result = saveLastSignInRecord(memberId);
        else
            result = refreshLastSignInRecord(memberCheckRecord);
        memberAddScore(memberId, result);
        return result;

    }

    public static Boolean sameCompany(Member c1, Member c2) {
        return c1.getCompany().getId().equals(c2.getCompany().getId());
    }

    public static Boolean sameDepartment(Member c1, Member c2) {
        return sameCompany(c1, c2) && c1.getDepartment().getId().equals(c2.getDepartment().getId());
    }

    public MemberScore memberAddScore(Long memberId, Integer socore) {
        checkUserScoreIfNotExistCreateOne(memberId);
        MemberScore memberScore = memberScoreRepository.findByMemberId(memberId);
        memberScore.setScore(memberScore.getScore() + socore);
        memberScore = memberScoreRepository.save(memberScore);
        return memberScore;
    }

    /**
     * 保存最后一次签到
     */
    private Integer saveLastSignInRecord(Long userId) {
        MemberCheckRecord userSignInRecord = new MemberCheckRecord();
        userSignInRecord.setDate(new Date());
        userSignInRecord.setMember(memberRepository.findOne(userId));
        memberSignInRecordRepository.save(userSignInRecord);
        return userSignInScore();
    }

    /**
     * 刷新最后一次签到
     */
    private Integer refreshLastSignInRecord(MemberCheckRecord userSignInRecord) {
        Date time = userSignInRecord.getDate();
        LocalDateTime lastSignIn = from(time.toInstant()).toLocalDateTime();
        LocalDateTime today = now();
        ifTrueThrow(lastSignIn.getDayOfYear() == today.getDayOfYear(), TIP_HAS_SIGNED_IN);
        userSignInRecord.setDate(new Date());
        memberSignInRecordRepository.save(userSignInRecord);
        return userSignInScore();
    }

    /**
     * 签到的积分
     */
    private Integer userSignInScore() {
        return userScoreContantRepository.findByScoreType(SCORE_TYPE_SIGN_IN).getScore();
    }

    /**
     * 发送手机验证码
     */
    public void sendValidateCode(String phoneNumber, SmsType smsType) {
        aLiSmsSender.sendContent(phoneNumber, smsType);
//        Integer validateCode = createValidateCode();
//        smsService.sendContent(phoneNumber, "验证码为:" + validateCode, validateCode, smsType);
    }

    /**
     * 发送手机验证码
     */
    public void sendValidateCodeIfFirstTimeCreateIt(String phoneNumber, SmsType smsType) {
        if (!mobileIsExisted(phoneNumber)) {
            Member member = new Member();
            member.setUsername(randomUUID().toString().substring(0, 8));
            member.setMobile(phoneNumber);
            member.setPassword(genInitPwd());
            md5DoublePwd(member);
            baseRepository.save(member);
            aLiSmsSender.sendContent(member.getMobile(), SMS_INIT, CREATE_USER);
        }
        Integer validateCode = genValCode();
        smsService.sendContent(phoneNumber, "验证码为:" + validateCode, validateCode, smsType);
    }


    /**
     * 随机密码初始化
     */
    public Member initialWithRandomInfo(String mobile) {
        Member member = null;
        if (!mobileIsExisted(mobile)) {
            member = new Member();
            member.setUsername(randomUUID().toString().substring(0, 8));
            member.setMobile(mobile);
            String password = genInitPwd();
            member.setPassword(password);
            md5DoublePwd(member);
            member = baseRepository.save(member);
//            initAccount(member);
//            smsService.sendContent(mobile, "初始密码:" + password, null, INIT_MEMBER);
        }
        return member;
    }

    /**
     * 微信初始化
     */
    public Member initialWithWxUser(WXUserInfo wxUserInfo) {
        Member member = null;
        if (!mobileIsExisted(wxUserInfo.getOpenid())) {
            member = new Member();
            member.setUsername(wxUserInfo.getNickname());
            member.setWxOpenId(wxUserInfo.getOpenid());
            String password = genInitPwd();
            member.setPassword(password);
            md5DoublePwd(member);
            member = baseRepository.save(member);
        }
        return member;
    }

    /**
     * 用户手机号是否存在
     */
    public Boolean mobileIsExisted(String mobile) {
        return memberRepository.findByMobile(mobile) != null;
    }

    public void checkUserScoreIfNotExistCreateOne(Long memberId) {
        MemberScore memberScore = memberScoreRepository.findByMemberId(memberId);
        if (isNull(memberScore)) {
            memberScore = new MemberScore();
            Member u = new Member();
            u.setId(memberId);
            memberScore.setScore(0);
            memberScore.setMember(u);
            memberScoreRepository.save(memberScore);
        }
    }

    public Member getMemberFromContext() {
        Authentication authentication = getContext().getAuthentication();
        BaseUserDerails userDerails = null;
        try {
            userDerails = (BaseUserDerails) authentication.getPrincipal();
        } catch (Exception e) {
            ifNullThrow(null, TIP_NOT_LOGIN);
        }
        return findByUsername(userDerails.getUsername());
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsernameAndIsDelete(username, NO);
    }

    public Page<Member> usernameLike(String username, PageRequest pageRequest) {
        return memberRepository.findByUsernameLike("%" + username + "%", pageRequest);
    }

    public List<Member> search(String search) {
        Stream<Member> stream = memberRepository.findAllByIsDeleteOrderByCreateTimeDesc(NO).stream();
        if (isNotBlank(search)) {
            stream = stream.filter(x -> contains(x.getUsername(), search));
        }
        return stream.collect(toList());
    }

    public List<Member> findAllByOrderByCreateTimeDesc() {
        return memberRepository.findAllByIsDeleteOrderByCreateTimeDesc(NO);
    }

    public Member findByToken(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        return member;
    }

    public Member checkUserMessage(String userToken, String checkedUserToken) {
        Member member = memberRepository.findByTokenAndIsDelete(checkedUserToken, NO);
        return member;
    }

    /**
     * 修改个人信息
     */
    public Member updateBasicInfo(String token, Member member) {
        Member oldMember = findByToken(token);
        ifNotNullThen(member.getHeadImage(), cloudPic -> oldMember.setHeadImage(cloudPicService.save(cloudPic)));
        ifNotNullThen(member.getUsername(), oldMember::setUsername);
        ifNotNullThen(member.getGender(), oldMember::setGender);
        ifNotNullThen(member.getBirthday(), oldMember::setBirthday);
        ifNotNullThen(member.getProvince(), oldMember::setProvince);
        ifNotNullThen(member.getCity(), oldMember::setCity);
        ifNotNullThen(member.getArea(), oldMember::setArea);
        oldMember.setModifyTime(new Date());
        return memberRepository.save(oldMember);
    }

    /**
     * 得到部门经理
     */
    public Member findDepartManager(Member member) {
        ifNullThrow(member.getDepartment(), TIP_NOT_JOIN_DEPARTMENT);
        return memberRepository.findByCompanyIdAndDepartmentIdAndPositionIsManager(member.getCompany().getId(), member.getDepartment().getId(), YES);
    }

    public Member findByWxOpenId(String openid) {
        return memberRepository.findByWxOpenId(openid);
    }

    public Member findByTokenAndIsDelete(String userToken, BooleanEnum isDelete) {
        return memberRepository.findByTokenAndIsDelete(userToken, isDelete);
    }

    public Page<Member> findByIds(List<Long> meberIds, Pageable pageable) {
        Page<Member> memberPage = memberRepository.findByIdIn(meberIds, pageable);
        ifFalseThrow(memberPage != null, TIP_NO_CHANSHUERROR);
        return memberPage;

    }


    public Page<Member> findByIdsAndUserName(List<Long> meberIds, String keyword, Integer pageNo, Integer pageSize) {

        PageVO pageVO = new PageVO();
        pageVO.setPageNo(pageNo);
        pageVO.setPageSize(pageSize);
        Page<Member> memberPage = null;
        if (keyword != null && keyword.trim().length() > 0) {
            memberPage = memberRepository.findByIdInAndRealNameContaining(meberIds, keyword, pageVO.pageRequest());
        } else {
            memberPage = memberRepository.findByIdIn(meberIds, pageVO.pageRequest());
        }
        ifTrueThrow(isNull(memberPage), TIP_NO_CHANSHUERROR);
        return memberPage;
    }


    public void memberDelete(Long id) {
        Member member = memberRepository.findOne(Long.valueOf(id));
        member.setIsDelete(YES);
        memberRepository.save(member);
    }

    /**
     * 设置支付密码
     */
    public void setPayPwd(String token, String password, String telCode) {
        Account account = accountRepository.findByMemberToken(token);
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        String mobile = account.getMember().getMobile();
        smsService.validateReceiverAndCode(mobile, telCode, SET_PAY_PWD);
        account.setPayPassword(md5(md5(md5(password))));
        aLiSmsSender.sendContent(mobile, SMS_RESET_SUC, SYS_EVENT);
    }

    /**
     * 修改支付密码
     */
    public Member restPayPwd(String token, String password, String oldPassword) {
        Account account = accountRepository.findByMemberToken(token);
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        Member member = account.getMember();
        ifFalseThrow(md5(md5(md5(oldPassword))).equals(account.getPayPassword()), TIP_OLD_PWD_WRONG);
        String mobile = member.getMobile();
        account.setPayPassword(md5(md5(md5(password))));
        accountRepository.save(account);
        aLiSmsSender.sendContent(mobile, SMS_RESET_SUC, SYS_EVENT);
        return member;
    }

}
