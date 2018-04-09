package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.domain.repository.MemberRoleRepository;
import com.thousandsunny.cms.domain.repository.RoleRepository;
import com.thousandsunny.cms.model.MemberRole;
import com.thousandsunny.cms.model.Role;
import com.thousandsunny.core.domain.repository.FriendsRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.repository.MemberVisitRepository;
import com.thousandsunny.core.domain.repository.RegionRepository;
import com.thousandsunny.core.domain.service.*;
import com.thousandsunny.core.model.*;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartySocialAccountService;
import com.thousandsunny.thirdparty.easemob.service.EasemobService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.ThirdPartySocialAccount;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.google.common.collect.ImmutableBiMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.CacheableUtil._5MinutesContainer;
import static com.thousandsunny.common.CacheableUtil.cleanUp;
import static com.thousandsunny.common.DateUtil.*;
import static com.thousandsunny.common.HTMLUtil.encodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.RandomNumberUtil.genInitPwd;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.PhoneType.*;
import static com.thousandsunny.core.ModuleKey.SmsType.*;
import static com.thousandsunny.core.ModuleTips.*;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.CANCEL;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static com.thousandsunny.thirdparty.ModuleTips.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.deepEquals;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.jackrabbit.util.Text.md5;

@Service
public class MemberService extends BaseService<Member> {
    @Autowired
    private EasemobService easemobService;
    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private HpAccountGenService genService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private CloudFileService cloudFileService;
    @Autowired
    private BenefitService benefitService;
    @Autowired
    private BenefitRelService benefitRelService;
    @Autowired
    private ALiSmsService aLiSmsSender;
    @Autowired
    private ThirdPartySocialAccountService thirdPartySocialAccountService;
    @Autowired
    private MemberRegRelRepository memberRegRelRepository;
    @Autowired
    private MemberRecRelRepository memberRecRelRepository;
    @Autowired
    private MemberVisitRepository memberVisitRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private FriendsRepository friendsRepository;
    @Autowired
    private SchoolCollectRepository schoolCollectRepository;
    @Autowired
    private CourseCollectRepository courseCollectRepository;
    @Autowired
    private JobCollectRepository jobCollectRepository;
    @Autowired
    private ShopCollectRepository shopCollectRepository;
    @Autowired
    private ResumeCollectRepository resumeCollectRepository;
    @Autowired
    private VideoCollectRepository videoCollectionRepository;
    @Autowired
    private PictureCollectRepository pictureCollectRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private MemberRoleRepository memberRoleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SchoolPhotoRepository schoolPhotoRepository;


    /**
     * 注册
     */
    public Member register(String phoneNumber, String password, String code, String inviteCode, PhoneType phoneType, String version) {
        Pair<Member, MemberExtInfo> infoPair = baseMemberService.register(phoneNumber, password, code, phoneType, version);
        return wrapMember(password, inviteCode, infoPair);
    }

    /**
     * 创建用户
     * 保存邀请关系
     * 保存到环信
     * <p>
     * 保存好处记录
     */
    public Member wrapMember(String password, String inviteCode, Pair<Member, MemberExtInfo> infoPair) {
        HpAccountGen lastHpAccount = genService.lastHpAccount();
        Member member = infoPair.getLeft();
        MemberExtInfo memberExtInfo = infoPair.getRight();

        String hpAccount = prefix() + lastHpAccount.getSeq() + "";
        hpAccount = hpAccount.replace("4", "H");//将慧聘账号的4改成H
        member.setHpAccount(hpAccount);
        baseMemberService.save(member);

        easemobService.registerEasemobUser(member.getToken());
        ifNotBlankThen(inviteCode, i -> {
            Member inviter = memberRegRelService.buildOneRecord(i, member);
            memberExtInfo.setRecommendUser(inviter);
            memberExtInfoService.save(memberExtInfo);
        });//建立邀请关系

        saveAccount(password, member);

        lastHpAccount.setMember(member);
        genService.save(lastHpAccount);

        JobApplyRecord _j = new JobApplyRecord();
        _j.setReceiver(member);
        benefitService.findAll().forEach(b -> benefitRelService.createBenefitRel(null, b, _j));//新增好处
        return member;
    }


    private void saveAccount(String password, Member member) {
        Account account = new Account();
        account.setMember(member);
        account.setPayPassword(md5(md5(password)));
        accountService.save(account);
    }

    public Boolean isPartner(String token) {

//        accountFlowRepository.findByPartnerApplyIdAndState()需要判断是否付过款
        return partnerService.countByMemberToken(token) > 0;
    }

    private static String prefix() {
        return "H" + format(new Date(), "yyMMdd");
    }


    /**
     * 设置/重置交易密码
     */
    public Account setPwd(String token, String code, String password) {
        Member member = baseMemberService.findByToken(token);
        ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        smsService.validateReceiverAndCode(member.getMobile(), code, RESET_PWD);
        Account account = accountService.findByMemberToken(token);
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        account.setPayPassword(md5(md5(password)));
        account.setValid(YES);
        accountService.save(account);
//        aLiSmsSender.sendContent(member.getMobile(), SMS_RESET_SUC, SYS_EVENT);
        return account;
    }

    /**
     * 手机号查找
     */
    public Member loginByMobileOrHpAccount(String usernameOrMobile, String password) {
        Member member = memberRepository.findByMobileAndIsDelete(usernameOrMobile, NO);
        if (isNull(member))
            member = memberRepository.findByHpAccountAndIsDelete(usernameOrMobile, NO);
        ifNullThrow(member, TIP_MOBILE_NOT_BIND);
        ifFalseThrow(md5(md5(password)).equals(member.getPassword()), TIP_PWD_WRONG);
        refreshLastLogin(member.getToken());
        return member;
    }


    /**
     * 修改交易密码
     */
    public Account resetPwd(String token, String oldPassword, String password) {
        Account account = accountService.findByMemberToken(token);
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        ifFalseThrow(Objects.equals(account.getPayPassword(), md5(md5(oldPassword))), TIP_PWD_WRONG);
        account.setPayPassword(md5(md5(password)));
        account.setValid(YES);
        accountService.save(account);
        return account;
    }

    /**
     * 设置电话免打扰
     */
    public Member setDisturb(String userToken, OperatorType operatorType) {
        Member member = baseMemberService.findByToken(userToken);
        member.setIsNoDisturb(operatorType == SURE ? YES : NO);
        return baseMemberService.save(member);
    }

    public Page<Member> listMembers(Pageable pageable, String text, Long province, Long city, Long area, IdentityType ESTPStatus, BooleanEnum partnerStatus) {
        Specification<Member> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(text, t -> predicates.add(rb.or(
                    rb.like(rt.get("username"), "%" + t + "%"),
                    rb.like(rt.get("realName"), "%" + t + "%"),
                    rb.like(rt.get("mobile"), "%" + t + "%"),
                    rb.like(rt.get("hpAccount"), "%" + t + "%")
            )));
            predicates.add(rb.or(rb.equal(rt.get("role"), AccountEnum.MANAGER), rb.isNull(rt.get("role"))));
            ifNotNullThen(province, t -> predicates.add(rb.equal(rt.get("province").get("id"), t)));
            ifNotNullThen(city, t -> predicates.add(rb.equal(rt.get("city").get("id"), t)));
            ifNotNullThen(area, t -> predicates.add(rb.equal(rt.get("area").get("id"), t)));
            ifNotNullThen(ESTPStatus, t -> predicates.add(rb.equal(rt.get("entrepreneurLevel"), t)));
            ifNotNullThen(partnerStatus, t -> predicates.add(rb.equal(rt.get("partnerLevel"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return memberRepository.findAll(specification, pageable);
    }


    /**
     * 修改个人信息
     */
    public void update(String userToken, Member member) {
        Member oldMember = baseMemberService.findByToken(userToken);
        ifNotNullThen(member.getHeadImage(), img -> oldMember.setHeadImage(cloudFileService.save(img)));
//        ifNotNullThen(member.getUsername(), oldMember::setUsername);
        ifNotBlankThen(member.getUsername(), x -> oldMember.setUsername(encodePathVariable(x)));
        ifNotBlankThen(member.getRealName(), oldMember::setRealName);
        ifNotNullThen(member.getGender(), oldMember::setGender);
        ifNotNullThen(member.getBirthday(), oldMember::setBirthday);
        memberRepository.save(oldMember);
    }

    public Member checkUserMessage(String checkedUserToken) {
        return memberRepository.findByTokenAndIsDelete(checkedUserToken, NO);
    }


    public void enabled(Long id) {
        Member member = memberRepository.findOne(id);
        ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        member.setValid(member.getValid() == YES ? NO : YES);
        memberRepository.save(member);
    }

    public Member findByPhone(String tel) {
        return memberRepository.findByMobileAndIsDelete(tel, NO);
    }

    /**
     * 第三方登陆绑定手机
     */
    public Member thirdPartyBindMobile(ThirdPartySocialAccount socialAccount, String phoneNumber, String code, String inviteCode, PhoneType phoneType, String version) {
        String password = genInitPwd();
        final Member[] member = {findByPhone(phoneNumber)};
        ifNullThen(member[0], () -> {
            Pair<Member, MemberExtInfo> infoPair = baseMemberService.tpRegister(phoneNumber, md5(password), code, phoneType, version);
            member[0] = wrapMember(md5(password), inviteCode, infoPair);
        });

        socialAccount.setMember(member[0]);
        ThirdPartySocialAccount tpSocialAccount = thirdPartySocialAccountService.getThirdPartySocialAccount(socialAccount);
        ifNotNullThrow(tpSocialAccount, TIP_TP_HAS_BIND);
        socialAccount.setAccountName(encodePathVariable(socialAccount.getAccountName()));
        thirdPartySocialAccountService.save(socialAccount);
        aLiSmsSender.sendContent(phoneNumber, INIT_MEMBER, of("code", password));
        return member[0];
    }


    /**
     * 发送手机验证码
     */
    public void sendValidateCode(String phoneNumber, SmsType smsType) {
        if (smsType == FORGET_PWD) {
            Member member = findByPhone(phoneNumber);
            ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        }
        if (smsType == CREATE_USER) {
            Member member = findByPhone(phoneNumber);
            ifNotNullThrow(member, TIP_MOBILE_EXISTED);
        }
        baseMemberService.sendValidateCode(phoneNumber, smsType);
    }

    /**
     * 他的好友列表（获取memberId）
     */
    public Set<Long> getMemberIds(String friendType, Long mId) {
        String userToken = memberRepository.findOne(mId).getToken();
        Set<Long> regOneIds = new HashSet<>();
        Set<Long> regTwoIds = new HashSet<>();
        Set<Long> regThreeIds = new HashSet<>();
        Set<Long> recOneIds = new HashSet<>();
        Set<Long> recTwoIds = new HashSet<>();
        Set<Long> recThreeIds = new HashSet<>();
        Set<Long> huiyouIds = new HashSet<>();
        Set<Long> allMemberIds = new HashSet<>();
        List<MemberRegRel> friendsList;
        friendsList = memberRegRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
        for (MemberRegRel memberRegRel : friendsList) {
            if (mId.equals(memberRegRel.getP2()) && memberRegRel.getP3() != null) {
                regOneIds.add(memberRegRel.getP3());
                allMemberIds.add(memberRegRel.getP3());
            } else if (mId.equals(memberRegRel.getP1()) && memberRegRel.getP2() != null) {
                regOneIds.add(memberRegRel.getP2());
                allMemberIds.add(memberRegRel.getP2());
            } else if (mId.equals(memberRegRel.getMember().getId()) && memberRegRel.getP1() != null) {
                regOneIds.add(memberRegRel.getP1());
                allMemberIds.add(memberRegRel.getP1());
            }
        }

        friendsList = memberRegRelRepository.findByMemberTokenOrP1(userToken, mId);
        for (MemberRegRel memberRegRel : friendsList) {
            if (mId.equals(memberRegRel.getP1()) && memberRegRel.getP3() != null) {
                regTwoIds.add(memberRegRel.getP3());
                allMemberIds.add(memberRegRel.getP3());
            } else if (mId.equals(memberRegRel.getMember().getId()) && memberRegRel.getP2() != null) {
                regTwoIds.add(memberRegRel.getP2());
                allMemberIds.add(memberRegRel.getP2());
            }
        }

        friendsList = memberRegRelRepository.findByMemberToken(userToken);
        for (MemberRegRel memberRegRel : friendsList) {
            if (memberRegRel.getP3() != null) {
                regThreeIds.add(memberRegRel.getP3());
                allMemberIds.add(memberRegRel.getP3());
            }
        }

        List<MemberRecRel> friendsList1;
        friendsList1 = memberRecRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
        for (MemberRecRel memberRecRel : friendsList1) {
            if (mId.equals(memberRecRel.getP2()) && memberRecRel.getP3() != null) {
                recOneIds.add(memberRecRel.getP3());
                allMemberIds.add(memberRecRel.getP3());
            } else if (mId.equals(memberRecRel.getP1()) && memberRecRel.getP2() != null) {
                recOneIds.add(memberRecRel.getP2());
                allMemberIds.add(memberRecRel.getP2());
            } else if (mId.equals(memberRecRel.getMember().getId()) && memberRecRel.getP1() != null) {
                recOneIds.add(memberRecRel.getP1());
                allMemberIds.add(memberRecRel.getP1());
            }
        }

        friendsList1 = memberRecRelRepository.findByMemberTokenOrP1(userToken, mId);
        for (MemberRecRel memberRecRel : friendsList1) {
            if (mId.equals(memberRecRel.getP1()) && memberRecRel.getP3() != null) {
                recTwoIds.add(memberRecRel.getP3());
                allMemberIds.add(memberRecRel.getP3());
            } else if (mId.equals(memberRecRel.getMember().getId()) && memberRecRel.getP2() != null) {
                recTwoIds.add(memberRecRel.getP2());
                allMemberIds.add(memberRecRel.getP2());
            }
        }

        friendsList1 = memberRecRelRepository.findByMemberToken(userToken);
        for (MemberRecRel memberRecRel : friendsList1) {
            if (memberRecRel.getP3() != null) {
                recThreeIds.add(memberRecRel.getP3());
                allMemberIds.add(memberRecRel.getP3());
            }
        }

        List<Friends> friendsList2 = friendsRepository.findByOwnerId(mId);
        for (Friends friends : friendsList2) {
            if (friends.getFriend() != null) {
                huiyouIds.add(friends.getFriend().getId());
                allMemberIds.add(friends.getFriend().getId());
            }
        }

        if ("REG_ONE".equals(friendType))
            return regOneIds;
        else if ("REG_TWO".equals(friendType))
            return regTwoIds;
        else if ("REG_THREE".equals(friendType))
            return regThreeIds;
        else if ("REC_ONE".equals(friendType))
            return recOneIds;
        else if ("REC_TWO".equals(friendType))
            return recTwoIds;
        else if ("REC_THREE".equals(friendType))
            return recThreeIds;
        else if ("HUIYOU".equals(friendType))
            return huiyouIds;
        return allMemberIds;
    }

    public List<Member> findFriendsList(String text, String friendType, Long userId) {
        Set<Long> memberIds = getMemberIds(friendType, userId);
        if (isEmpty(memberIds)) {
            return new ArrayList<>();
        }
        Specification<Member> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotBlankThen(text, e -> {
                String textStr = "%" + e + "%";
                predicates.add(rb.or(rb.like(rt.get("realName"), textStr), rb.like(rt.get("mobile"), textStr)));
            });
            predicates.add(rt.get("id").in(memberIds));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return memberRepository.findAll(specification);
    }

    public Member findByTokenAndIsDelete(String userToken, BooleanEnum no) {
        return memberRepository.findByTokenAndIsDelete(userToken, no);
    }

    public Member findByToken(String userToken) {
        Member member = memberRepository.findByToken(userToken);
        ifNullThrow(member, TIP_MEMBER_NOT_EXISTED);
        return member;
    }

    public Long countMembers(Date startTime, Date endTime) {
        Specification<Member> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("valid"), YES));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createTime"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createTime"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return memberRepository.count(specification);
    }

    /**
     * 记录登录行为
     */
    public void refreshLastVisit(String token, Double lng, Double lat, Long provinceId, Long cityId, Long areaId,
                                 VisitState type, String deviceVersion, PhoneType platformType) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteAddr();
        Region province = null;
        Region city = null;
        Region area = null;
        MemberExtInfo member = null;
        if (isNotBlank(token)) {
            member = memberExtInfoService.findByMemberToken(token);
            member.setLastVisitTime(new Date());
            member.setLastIp(ip);
            member.setLng(lng);
            member.setLat(lat);
            if (isNotNull(provinceId)) {
                province = regionRepository.findOne(provinceId);
                member.setProvince(province);
            }
            if (isNotNull(cityId)) {
                city = regionRepository.findOne(cityId);
                member.setCity(city);
            }
            if (isNotNull(areaId)) {
                area = regionRepository.findOne(areaId);
                member.setArea(area);
            }
            member.setVisitCount(member.getVisitCount() + 1);
            memberExtInfoService.save(member);
            Resume resume = resumeRepository.findByMemberToken(member.getMember().getToken());
            ifNotNullThen(resume, x -> {
                resume.setLatitude(lat);
                resume.setLongitude(lng);
                resumeRepository.save(resume);
            });
        }
        saveMemberVisit(member, type, deviceVersion, platformType, ip, province, city, area, lng, lat);
    }

    private void saveMemberVisit(MemberExtInfo memberExtInfo, VisitState type, String deviceVersion, PhoneType platformType,
                                 String ip, Region province, Region city, Region area, Double lng, Double lat) {
        MemberVisit memberVisit = null;
        if (isNotNull(deviceVersion))
            memberVisit = memberVisitRepository.findTop1ByDeviceVersionOrderByCreateTimeDesc(deviceVersion);
        else if (isNotNull(memberExtInfo)) {
            memberVisit = memberVisitRepository.findTop1ByMemberIdOrderByCreateTimeDesc(memberExtInfo.getMember().getId());
        }
        if (isNotNull(memberExtInfo)) {
            ifTrueThen(platformType == WEB, () -> {
                memberExtInfo.setIsActivationWX(TRUE);
                memberExtInfo.setIsActivationAPP(FALSE);
            });
            ifTrueThen(platformType == IOS || platformType == ANDROID, () -> {
                memberExtInfo.setIsActivationWX(FALSE);
                memberExtInfo.setIsActivationAPP(TRUE);
            });

            memberExtInfoService.save(memberExtInfo);
        }
        Date now = new Date();
        if (isNull(memberVisit)) {
            memberVisit = new MemberVisit();
        } else {
            Date createTime = memberVisit.getCreateTime();
            if (isNotNull(memberVisit) && !isSameYear(createTime, now) && !isSameMonth(createTime, now) && isSameDayOfMonth(createTime, now)) {
                memberVisit = new MemberVisit();
            } else {
                memberVisit.setVisitCount(memberVisit.getVisitCount() + 1);
            }
        }

        final MemberVisit finalMemberVisit = memberVisit;
        finalMemberVisit.setCreateTime(now);
        finalMemberVisit.setIp(ip);
        finalMemberVisit.setPlatformType(platformType);
        ifNotNullThen(memberExtInfo, x -> {
            finalMemberVisit.setMember(x.getMember());
            Long resumes = resumeRepository.countByMemberId(x.getMember().getId());
            ifNotNullThen(resumes, y -> finalMemberVisit.setIsMeiyeren(YES));
        });
        ifNotNullThen(province, x -> finalMemberVisit.setProvince(x));
        ifNotNullThen(city, x -> finalMemberVisit.setCity(x));
        ifNotNullThen(area, x -> finalMemberVisit.setArea(x));
        ifNotNullThen(province, x -> finalMemberVisit.setProvince(x));
        ifNotNullThen(lng, x -> finalMemberVisit.setLng(x));
        ifNotNullThen(lat, x -> finalMemberVisit.setLat(x));
        ifNotNullThen(type, x -> finalMemberVisit.setType(x));
        memberVisitRepository.save(finalMemberVisit);
    }

    /**
     * 记录登录行为
     */
    public MemberExtInfo refreshLastLogin(String token) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        MemberExtInfo member = memberExtInfoService.findByMemberToken(token);
        member.setLastIp(request.getRemoteAddr());
        member.setLastLoginTime(new Date());
        return memberExtInfoService.save(member);
    }

    /**
     * 验证码登录
     */
    public Member loginByKaptcha(String username, String password, String code, String token) {
        cleanUp();
        ifTrueThrow(isBlank(token) || isBlank(code), TIP_TOKEN_WRONG);
        String cachedCode = _5MinutesContainer.getIfPresent(token);
        ifFalseThrow(deepEquals(cachedCode, code), TIP_KAPTCHA_WRONG);
        return loginByMobileOrHpAccount(username, password);
    }

    public Boolean judgeIsCollectSchool(Member member, School school) {
        SchoolCollect schoolCollect = schoolCollectRepository.findByMemberTokenAndSchoolIdAndCollectEver(member.getToken(), school.getId(), NO);
        if (isNotNull(schoolCollect)) return true;
        else return false;
    }

    public void collectSchool(School school, OperatorType operatorType, Member member) {
        SchoolCollect schoolCollect = schoolCollectRepository.findByMemberTokenAndSchoolId(member.getToken(), school.getId());
        ifTrueThrow(isNotNull(schoolCollect) &&
                ((schoolCollect.getCollectEver() == NO && operatorType == SURE) ||
                        (schoolCollect.getCollectEver() == YES && operatorType == CANCEL)), TIP_HAS_COLLECT_OR_CANCEL);
        if (isNull(schoolCollect)) {
            schoolCollect = new SchoolCollect();
            schoolCollect.setSchool(school);
            schoolCollect.setMember(member);
        }
        schoolCollect.setDate(new Date());
        if (operatorType == SURE) schoolCollect.setCollectEver(NO);
        else schoolCollect.setCollectEver(YES);
        schoolCollectRepository.save(schoolCollect);
    }

    public Boolean judgeIsCollectCourse(Member member, Course course) {
        CourseCollect courseCollect = courseCollectRepository.findByMemberTokenAndCourseIdAndCollectEver(member.getToken(), course.getId(), NO);
        if (isNotNull(courseCollect)) return true;
        else return false;
    }

    public void collectCourse(Course course, OperatorType operatorType, Member member) {
        CourseCollect courseCollect = courseCollectRepository.findByMemberTokenAndCourseId(member.getToken(), course.getId());
        ifTrueThrow(isNotNull(courseCollect) &&
                ((courseCollect.getCollectEver() == NO && operatorType == SURE) ||
                        (courseCollect.getCollectEver() == YES && operatorType == CANCEL)), TIP_HAS_COLLECT_OR_CANCEL);
        if (isNull(courseCollect)) {
            courseCollect = new CourseCollect();
            courseCollect.setCourse(course);
            courseCollect.setMember(member);
        }
        courseCollect.setDate(new Date());
        if (operatorType == SURE) courseCollect.setCollectEver(NO);
        else courseCollect.setCollectEver(YES);
        courseCollectRepository.save(courseCollect);
    }

    public List<JSONObject> findJobCollections(Member member, String text) {
        String[] json = {"id", "job.name:title",};
        Specification<JobCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("job").get("name"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<JobCollect> list = jobCollectRepository.findAll(spec);
        return simpleMap(list, jobCollect -> {
            JSONObject jo = propsFilter(jobCollect, json);
            jo.put("likeType", "岗位");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(jobCollect.getDate()));
            return jo;
        });
    }

    public List<JSONObject> findShopCollections(Member member, String text) {
        String[] json = {"id", "shop.name:title"};
        Specification<ShopCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("shop").get("name"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<ShopCollect> list = shopCollectRepository.findAll(spec);
        return simpleMap(list, shopCollect -> {
            JSONObject jo = propsFilter(shopCollect, json);
            jo.put("likeType", "店铺");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(shopCollect.getDate()));
            return jo;
        });
    }

    public List<JSONObject> findResumeCollections(Member member, String text) {
        String[] json = {"id", "member.realName:title"};
        Specification<ResumeCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("member").get("realName"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<ResumeCollect> list = resumeCollectRepository.findAll(spec);
        return simpleMap(list, resumeCollect -> {
            JSONObject jo = propsFilter(resumeCollect, json);
            jo.put("likeType", "简历");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(resumeCollect.getDate()));
            return jo;
        });
    }

    public List<JSONObject> findVideoCollections(Member member, String text) {
        String[] json = {"id", "video.title:title"};
        Specification<VideoCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("video").get("title"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<VideoCollect> list = videoCollectionRepository.findAll(spec);
        return simpleMap(list, videoCollect -> {
            JSONObject jo = propsFilter(videoCollect, json);
            jo.put("likeType", "视频");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(videoCollect.getDate()));
            return jo;
        });
    }

    public List<JSONObject> findSchoolCollections(Member member, String text) {
        String[] json = {"id", "school.name:title"};
        Specification<SchoolCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("school").get("name"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<SchoolCollect> list = schoolCollectRepository.findAll(spec);
        return simpleMap(list, schoolCollect -> {
            JSONObject jo = propsFilter(schoolCollect, json);
            jo.put("likeType", "培训学校");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(schoolCollect.getDate()));
            return jo;
        });
    }

    public List<JSONObject> findCourseCollections(Member member, String text) {
        String[] json = {"id", "course.name:title"};
        Specification<CourseCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("course").get("name"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<CourseCollect> list = courseCollectRepository.findAll(spec);
        return simpleMap(list, courseCollect -> {
            JSONObject jo = propsFilter(courseCollect, json);
            jo.put("likeType", "培训课程");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(courseCollect.getDate()));
            return jo;
        });
    }

    public List<JSONObject> findPhotoCollections(Member member, String text) {
        String[] json = {"id", "picture.path:title"};
        Specification<PictureCollect> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("collectEver"), NO));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("picture").get("title"), "%" + e + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<PictureCollect> list = pictureCollectRepository.findAll(spec);
        return simpleMap(list, pictureCollect -> {
            JSONObject jo = propsFilter(pictureCollect, json);
            jo.put("likeType", "图片");
            jo.put("likeTime", ISO_DATETIME_FORMAT.format(pictureCollect.getDate()));
            return jo;
        });
    }

    public Page<Member> findManagerPage(Pageable pageable, String text) {
        List<AccountEnum> list = newArrayList(AccountEnum.SCHOOL, AccountEnum.MANAGER);
        Specification<Member> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rt.get("role").in(list));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("realName"), "%" + t + "%"), rb.like(rt.get("mobile"), "%" + t + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return memberRepository.findAll(specification, pageable);
    }

    public void deleteManager(List<Long> ids) {
        if (!ids.isEmpty()) ids.forEach(id -> {
            Member member = findOne(id);
            ifTrueThrow(member == null || (member.getRole() != AccountEnum.MANAGER && member.getRole() != AccountEnum.SCHOOL), TIP_PARAM_FALSE);
            member.setIsDelete(YES);
            memberRepository.save(member);
            if (member.getRole() == AccountEnum.SCHOOL) {
                School school = schoolRepository.findByMemberId(member.getId());
                school.setIsDelete(YES);
                List<SchoolPhoto> schoolPhotos = schoolPhotoRepository.findBySchoolIdAndIsDelete(school.getId(), NO);
                schoolPhotos.forEach(x -> x.setIsDelete(YES));
                schoolPhotoRepository.save(schoolPhotos);
            }
        });
    }

    public void editManager(Member member, String schoolName, Long provinceId,
                            Long cityId, Long areaId, String schoolAddress, String schoolXYZ, String schoolWeb, String roleIds, BooleanEnum valid) {
        if (member.getRole() == AccountEnum.MANAGER) {
            Member memberByMobile = memberRepository.findByMobile(member.getMobile());
            ifNotNullThrow(memberByMobile, TIP_MOBILE_EXISTED);
        }
        if (isNotNull(member.getId())) {
            // 修改
            Member oldMember = memberRepository.findOne(member.getId());
            ifNullThrow(oldMember, TIP_NO_MEMBER);
//            ifNotBlankThen(member.getHpAccount(), oldMember::setHpAccount);
            ifNotBlankThen(member.getRealName(), oldMember::setRealName);
            ifNotBlankThen(member.getUsername(), oldMember::setUsername);
            ifNotNullThen(member.getGender(), oldMember::setGender);
            ifNotNullThen(member.getRole(), oldMember::setRole);
            if (isNotNull(valid)) oldMember.setValid(valid);
            else oldMember.setValid(NO);
            ifNotBlankThen(member.getPassword(), e -> oldMember.setPassword(md5(md5(e))));
            memberRepository.save(oldMember);
            if (isNotBlank(roleIds)) saveMemberRoles(roleIds, oldMember);
            if (member.getRole() == AccountEnum.SCHOOL) {
                School school = schoolRepository.findByMemberId(oldMember.getId());
                ifNullThrow(school, TIP_NO_SCHOOL);
                ifNotBlankThen(schoolName, school::setName);
                ifNotNullThen(provinceId, e -> school.setProvince(regionRepository.findByIdAndRegionLevel(e, 1)));
                ifNotNullThen(cityId, e -> school.setCity(regionRepository.findByIdAndRegionLevel(e, 2)));
                ifNotNullThen(areaId, e -> school.setArea(regionRepository.findByIdAndRegionLevel(e, 3)));
                school.setAddress(schoolAddress);
                school.setLink(schoolWeb);
                school.setTelephone(member.getMobile());
                ifNotBlankThen(schoolXYZ, e -> {
                    String[] xyz = e.split(",");
                    school.setLongitude(Double.parseDouble(xyz[0]));
                    school.setLatitude(Double.parseDouble(xyz[1]));
                });
                schoolRepository.save(school);
            }

        } else {
            //新增
            ifFalseThrow(isNotNull(member.getHpAccount()) && isNotNull(member.getPassword()), TIP_PARAM_FALSE);
            Member memberByHpAccount = memberRepository.findByHpAccount(member.getHpAccount());
            ifNotNullThrow(memberByHpAccount, TIP_HP_ACCOUNT_EXISTED);
            Member newMember = new Member();
            ifNotBlankThen(member.getHpAccount(), newMember::setHpAccount);
            ifNotBlankThen(member.getRealName(), newMember::setRealName);
            ifNotNullThen(member.getGender(), newMember::setGender);
            ifNotBlankThen(member.getUsername(), newMember::setUsername);
            if (isNotBlank(member.getMobile()) && member.getRole() == AccountEnum.MANAGER) {
                newMember.setMobile(member.getMobile());
            }
            ifNotNullThen(member.getRole(), newMember::setRole);
            if (isNotNull(valid)) newMember.setValid(valid);
            else newMember.setValid(NO);
            if (isNotBlank(member.getPassword())) {
                newMember.setPassword(md5(md5(member.getPassword())));
            } else newMember.setPassword(md5(md5(md5("123456"))));
            newMember = memberRepository.save(newMember);
            if (isNotBlank(roleIds)) saveMemberRoles(roleIds, newMember);
            MemberExtInfo memberExtInfo = new MemberExtInfo();
            memberExtInfo.setMember(newMember);
            String lastIp = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRemoteAddr();
            memberExtInfo.setLastIp(lastIp);
            memberExtInfoService.save(memberExtInfo);
            // 保存账户
            saveAccount(newMember.getPassword(), newMember);
            if (member.getRole() == AccountEnum.SCHOOL) {
                School school = new School();
                school.setMember(newMember);
                school.setIsPartSchool(NO);
                school.setTelephone(member.getMobile());
                ifNotBlankThen(schoolName, school::setName);
                ifNotNullThen(provinceId, e -> school.setProvince(regionRepository.findByIdAndRegionLevel(e, 1)));
                ifNotNullThen(cityId, e -> school.setCity(regionRepository.findByIdAndRegionLevel(e, 2)));
                ifNotNullThen(areaId, e -> school.setArea(regionRepository.findByIdAndRegionLevel(e, 3)));
                ifNotBlankThen(schoolAddress, school::setAddress);
                ifNotBlankThen(schoolWeb, school::setLink);
                ifNotBlankThen(schoolXYZ, e -> {
                    String[] xyz = e.split(",");
                    school.setLongitude(Double.parseDouble(xyz[0]));
                    school.setLatitude(Double.parseDouble(xyz[1]));
                });
                schoolRepository.save(school);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(md5(""));
        System.out.println(md5(md5(md5(""))));
    }

    public void saveMemberRoles(String roleIds, Member oldMember) {
        //将之前对应的member的角色删除
        List<MemberRole> oldMemberRoles = memberRoleRepository.findByMemberAndIsDelete(oldMember, NO);
        if (!oldMemberRoles.isEmpty()) {
            oldMemberRoles.forEach(memberRole -> memberRole.setIsDelete(YES));
            memberRoleRepository.save(oldMemberRoles);
        }
        //保存新的member的角色
        newArrayList(roleIds.split(",")).forEach(x -> {
            Role role = roleRepository.findOne(Long.parseLong(x));
            ifNullThrow(role, TIP_NO_ROLE);
            MemberRole memberRole = new MemberRole();
            memberRole.setMember(oldMember);
            memberRole.setRole(role);
            memberRoleRepository.save(memberRole);
        });
    }

    public void updateMobile(String userToken, Integer oldCode, String newPhoneNumber, Integer newCode) {
        Member member = findByToken(userToken);
        ifNullThrow(member.getMobile(), TIP_MOBILE_NOT_BIND);
        smsService.validateReceiverAndTypeAndCode(member.getMobile(), CHANGE_PWD, oldCode);
        smsService.validateReceiverAndTypeAndCode(newPhoneNumber, CHANGE_PWD, newCode);
        Member newPhoneMember = findByPhone(newPhoneNumber);
        ifNotNullThrow(newPhoneMember, TIP_MOBILE_BINDED);
        member.setMobile(newPhoneNumber);
        memberRepository.save(member);
    }
}
