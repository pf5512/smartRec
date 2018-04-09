package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPage;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.service.FeedBackService;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.FeedBack;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.ResumeRepository;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.collect.ImmutableMap.of;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.DateUtil.dayOfMonthNum;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.*;
import static com.thousandsunny.common.KaptchaUtil.cacheableKaptcha;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.MANAGER;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.IdentityType;
import static com.thousandsunny.service.ModuleKey.*;
import static com.thousandsunny.service.ModuleKey.BenefitType.SALARY_PROTECTION;
import static com.thousandsunny.service.ModuleKey.ComplainType.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.service.ModuleKey.WithdrawType.*;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.FAILED;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.*;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_MEMBER_NOT_EXISTED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * 如果这些代码有用，那它们是guitarist在21/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/manager/members", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerMemberController {
    private static final String[] app_member_model = new String[]{
            "id",
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
    private static final String[] MEMBER_JSON = {
            "id",
            "hpAccount",
            "realName:name",
            "position",
            "email",
            "education",
            "gender.title:gender",
            "mobile:tel",
            "position.name:position",
            "accountType"
    };

    private static final String[] MEMBERS_JSON = {
            "id",
            "vipId",
            "hpAccount",
            "mobile",
            "realName",
            "createTime",
            "valid",
            "entrepreneurLevel",
            "partnerLevel",
            "source"
    };

    private static final String[] INTENTION_JSON = {
            "workYear:jobIntension",
            "findJobState.title:jobStatus",
            "salary:salaryExpectation",
            "city.name:workplace"
    };

    private static final String[] EXP_JSON = {
            "shopName",
            "positionName:position",
            "startDate",
            "endDate",
            "description:jobDescription"
    };
    private static final String[] TRAIN_JSON = {
            "institutionName:trainOrg",
            "courseName:trainCourse",
            "startDate",
            "endDate",
            "trainCertificate"
    };

    private static final String[] PIC_JSON = {
            "path:imgUrl",
            "isPlatformAdd:platforMauthentication"
    };

    private static final String[] MEMBER_DETAIL_JSON = {
            "headImage.path:headImg",
            "id:vipId",
            "mobile",
            "realName:username",
            "username:nickName",
            "gender.title:gender",
            "hpAccount:HPaccount",
            "entrepreneurLevel.title:isEntrepreneurs",
            "partnerLevel.title:isPartner",
            "source"
    };

    private static final String[] SHOP_BASE_INFO = {
            "name:shopName",
            "id:shopId",
            "owner.realName:contacter",
            "owner.mobile:mobile",
            "owner.hpAccount:hpAccount",
            "role.title:position",
            "address",
            "createTime",
    };

    private static final String[] WHO_LOOK_HIM_LIST = {
            "id",
            "shop.name",
            "shop.owner.realName",
            "shop.owner.mobile",
            "shop.owner.hpAccount",
            "date"
    };

    private static final String[] WHO_LOOK_HIM_DETAIL = {
            "shop.id:shopId",
            "shop.name:shopName",
            "shop.owner.realName:contacter",
            "shop.owner.mobile:mobile",
            "shop.owner.hpAccount:hpAccount",
            "shop.ownerPosition:positon",
            "shop.address:address",
            "shop.date:createTime",
    };

    private static final String[] REC_INFO_LIST = {
            "id",
            "job.name",
            "shop.name",
            "recState.title",
    };

    private static final String[] REC_INFO_DETAIL = {
            "job.name:position",
            "shop.name:shopName",
            "recState.title:workStatus",
            "reward.title:rewardStatus"
    };

    private static final String[] CAR_FEE_LIST = {
            "id",
            "job.name",
            "job.shop.name",
    };

    private static final String[] FREE_TRAING_LIST = {
            "id",
            "type.title",
            "benefitRel.job.name",
            "benefitRel.job.reward",
            "benefitRel.job.shop.name",
            "createDate",
            "effectiveDate",
            "state"

    };

    private static final String[] SALARY_PROTECTION_LIST = {
            "id",
            "job.shop.name",
    };

    private static final String[] QUICK_LOAN_LIST = {
            "id",
            "amount",
    };

    private static final String[] MOMENT_LIST = {
            "id",
            "content",
    };

    private static final String[] RESUME_BLOCKED_LIST = {
            "id",
            "resumeMember.mobile",
            "resumeMember.realName",
            "resumeMember.hpAccount",
    };

    private static final String[] MOMENT_BLOCKED_LIST = {
            "id",
            "momentsMember.mobile",
            "momentsMember.realName",
            "momentsMember.hpAccount",
    };

    private static final String[] FEED_BACK_LIST = {
            "id",
            "content",
    };

    private static final String[] COMPLAIN_LIST = {
            "id",
            "type.title",
    };

    private static final String[] FRIENDS_LIST = {
            "id",
            "realName",
            "mobile",
            "hpAccount",
    };

    private static final String[] WITHDRAWACCOUNT_LIST = {
            "id",
            "type.title",
    };

    private static final String[] ACCOUNT_FLOW_LIST = {
            "id",
            "recordType.title",
            "remark",
    };
    private static final String[] MANAGER_MEMBER_MODEL = {
            "token",
            "headImage.path:headerImageUrl",
            "username:userName",
            "birthday",
            "province.name:province",
            "city.name:city",
            "area.name:area",
            "company.id:companyId",
            "department.name:departmentName",
            "position.name:positionName",
            "role",
            "schoolName",
            "schoolId",
    };

    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private ShopService shopService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private WithdrawAccountService withdrawAccountService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private ResumeLookService resumeLookService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private BenefitRelService benefitRelService;
    @Autowired
    private BenefitService benefitService;
    @Autowired
    private MomentsService momentsService;
    @Autowired
    private ResumeBlockedService resumeBlockedService;
    @Autowired
    private MomentsBlockedService momentsBlockedService;
    @Autowired
    private FeedBackService feedBackService;
    @Autowired
    private ComplainService complainService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberVisitService memberVisitService;
    @Autowired
    private FriendsService friendsService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private CourseApplyService courseApplyService;
    @Autowired
    private CardCouponReceiveService cardCouponReceiveService;


    /**
     * 获取用户信息 //
     */
    @RequestMapping(value = "/token/{token}", method = GET)
    public ResponseEntity info(@PathVariable String token) {
        String[] school_info = {"id", "name"};
        Member member = baseMemberService.findByToken(token);
        JSONObject jsonObject = propsFilter(member, MEMBER_JSON);
        jsonObject.put("regTime", ISO_DATETIME_FORMAT.format(member.getCreateTime()));
        String role = member.getRole() == null ? "" : member.getRole().getTitle();
        jsonObject.put("role", role);
        if (member.getRole() == ModuleKey.AccountEnum.SCHOOL) {
            School school = schoolService.findMemberSchool(member);
            if (isNotNull(school)) {
                JSONObject schoolInfo = propsFilter(school, school_info);
                jsonObject.put("school", schoolInfo);

                StringBuffer address = new StringBuffer();
                ifNotNullThen(school.getProvince(), t -> address.append(t.getName()));
                ifNotNullThen(school.getCity(), t -> address.append("-").append(t.getName()));
                ifNotNullThen(school.getArea(), t -> address.append("-").append(t.getName()));
                jsonObject.put("address", address);
//                jsonObject.put("")
            }
        }
        enumToJson(member.getRole(), jsonObject, "accountType");
        return ok(jsonObject);
    }

    /**
     * 登录
     *
     * @Author mu.jie
     * @Date 2017/1/11
     */
    @RequestMapping(value = "/login", method = POST)
    public ResponseEntity info(String account, String password, String code, String token) {
        Member member = memberService.loginByKaptcha(account, password, code, token);
        if (member.getRole() == SCHOOL || member.getRole() == MANAGER) {
            JSONObject body = propsFilter(member, MANAGER_MEMBER_MODEL);
            if (member.getRole() == SCHOOL) {
                School school = schoolService.findMemberSchool(member);
                ifNotNullThen(school, x -> {
                    body.replace("schoolName", x.getName());
                    body.replace("schoolId", x.getId());
                });
            }
            return ok(body);
        } else {
            ifTrueThrow(true, TIP_MEMBER_NOT_EXISTED);
            return ok(of("msg", "账号不正确"));
        }

    }

    /**
     * 前台短信验证码
     */
    @RequestMapping(value = "/kaptcha", method = GET)
    public void kaptcha(String sessionKey, HttpServletResponse response) throws Exception {
        cacheableKaptcha(sessionKey, response);
    }


    /**
     * 会员列表
     */
    @RequestMapping(value = "/member", method = GET)
    public Result list(BackPageVo pageVO, String text, Long province, Long city, Long area, IdentityType ESTPStatus, BooleanEnum partnerStatus) {
        Page<Member> memberPage = memberService.listMembers(pageVO.pageRequest(), decodePathVariable(text), province, city, area, ESTPStatus, partnerStatus);
        Page<JSONObject> jsonObject = memberPage.map(e -> {

            JSONObject jo = propsFilter(e, MEMBERS_JSON);
            enumToJson(e.getPartnerLevel(), jo, "partnerLevel");
            enumToJson(e.getEntrepreneurLevel(), jo, "entrepreneurLevel");
            ifNotNullThen(e.getValid(), x -> jo.replace("valid", x == YES));
            ifNotNullThen(e.getCreateTime(), x -> jo.replace("createTime", ISO_DATETIME_FORMAT.format(x)));
            MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(e.getToken());

            if (isNotNull(memberExtInfo)) {
                Member recommendUser = memberExtInfo.getRecommendUser();
                ifNotNullThen(memberExtInfo.getPhoneType(), x -> jo.replace("source", x.getTitle()));
                jo.put("version", memberExtInfo.getVersion());
                if (isNotNull(recommendUser))
                    jo.put("tjr", recommendUser.getRealName());
                else
                    jo.put("tjr", null);
            } else {
                jo.put("tjr", null);
                jo.put("version", null);
            }

            return jo;
        });
        return OK(jsonObject);

    }


    /**
     * 删除
     */
    @RequestMapping(value = "/member", method = DELETE)
    public Result memberDelete(String id) {
        String[] strings = id.split(",");
        Arrays.asList(strings).forEach(e -> baseMemberService.memberDelete(Long.parseLong(e)));
        return Result.OK();
    }


    /**
     * 详情
     */
    @RequestMapping(value = "/member/{id}", method = GET)
    public ResponseEntity memberInfo(@PathVariable Long id) {
        Member member = baseMemberService.findOne(id);
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(member.getToken());
        Resume resume = resumeRepository.findByMemberToken(member.getToken());
        JSONObject jsonObject = new JSONObject();

        JSONObject personalInfo1 = new JSONObject();
        JSONObject personalInfo = propsFilter(member, MEMBER_DETAIL_JSON);
        ifNotNullThen(member.getUsername(), x -> personalInfo.replace("nickName", decodePathVariable(x)));
        if (isNotNull(member.getBirthday()))
            personalInfo.put("birthday", ISO_DATE_FORMAT.format(member.getBirthday()));
        else personalInfo.put("birthday", null);
        if (isNotNull(member.getCreateTime()))
            personalInfo.put("regDate", ISO_DATE_FORMAT.format(member.getCreateTime()));
        else personalInfo.put("regDate", null);
        if (isNotNull(memberExtInfo)) {
            if (isNotNull(memberExtInfo.getRecommendUser())) {
                personalInfo.put("referrer", memberExtInfo.getRecommendUser().getRealName());
            }
            ifNotNullThen(memberExtInfo.getPhoneType(), x -> personalInfo.replace("source", x.getTitle()));
            personalInfo.put("version", memberExtInfo.getVersion());
        } else {
            personalInfo.put("referrer", null);
            personalInfo.replace("source", null);
            personalInfo.put("version", null);
        }

        JSONObject credentialsInfo = new JSONObject();
        ifNotNullThen(memberExtInfo, x -> {
            credentialsInfo.put("IDCardNo", x.getIdCardNo());
            if (isNotNull(memberExtInfo.getHalf()))
                credentialsInfo.put("IDCardFrontImg", memberExtInfo.getIdCard().getPath());
            else credentialsInfo.put("IDCardFrontImg", null);
            if (isNotNull(memberExtInfo.getHalf()))
                credentialsInfo.put("IDCardHalfImg", memberExtInfo.getHalf().getPath());
            else credentialsInfo.put("IDCardHalfImg", null);
        });
        JSONObject ActivateStatus = new JSONObject();
        if (isNotNull(memberExtInfo)) {
            ActivateStatus.put("app", memberExtInfo.getIsActivationAPP());
            ActivateStatus.put("wx", memberExtInfo.getIsActivationWX());
        } else {
            ActivateStatus.put("app", null);
            ActivateStatus.put("wx", null);
        }

        personalInfo1.put("personalInfo", personalInfo);
        personalInfo1.put("credentialsInfo", credentialsInfo);
        personalInfo1.put("ActivateStatus", ActivateStatus);
        JSONObject resumeJson = new JSONObject();
        if (resume != null) {
            if (isNotNull(resume.getHighlights())) {
                resumeJson.put("personalHighlight", resume.getHighlights().split(","));
            } else resumeJson.put("personalHighlight", null);
            final JSONObject[] jobIntension = {null};
            ifNotNullThen(resume.getIntention(), e -> {
                jobIntension[0] = propsFilter(e, INTENTION_JSON);
                StringBuffer jobPosition = new StringBuffer("");
                if (!e.getJobTypes().isEmpty()) {
                    e.getJobTypes().forEach(x -> {
                        jobPosition.append(x.getName() + ",");
                    });
                    jobPosition.deleteCharAt(jobPosition.length() - 1);
                }
                jobIntension[0].put("jobPosition", jobPosition.toString());

                StringBuffer area = new StringBuffer("");
                ifNotNullThen(e.getProvince(), x -> area.append(x.getName()).append("-"));
                ifNotNullThen(e.getCity(), x -> area.append(x.getName()).append("-"));
                ifNotNullThen(e.getArea(), x -> area.append(x.getName()).append("-"));
                area.deleteCharAt(area.length() - 1);
                jobIntension[0].put("workplace", area.toString());
            });
            resumeJson.put("jobIntension", jobIntension[0]);
            List<JSONObject> jobExperiences;
            if (!resume.getWorkExps().isEmpty()) {
                jobExperiences = simpleMap(resume.getWorkExps(), e -> {
                    JSONObject exp = propsFilter(e, EXP_JSON);
                    if (isNotNull(e.getStartDate())) exp.put("startDate", ISO_DATE_FORMAT.format(e.getStartDate()));
                    else exp.put("startDate", null);
                    if (isNotNull(e.getEndDate())) exp.put("endDate", ISO_DATE_FORMAT.format(e.getEndDate()));
                    else exp.put("endDate", null);
                    return exp;
                });
            } else jobExperiences = null;
            resumeJson.put("jobExperiences", jobExperiences);

            List<JSONObject> trainingExperiences;
            if (!resume.getTrainExps().isEmpty()) {
                trainingExperiences = simpleMap(resume.getTrainExps(), e -> {
                    JSONObject exp = propsFilter(e, TRAIN_JSON);
                    if (isNotNull(e.getStartDate())) exp.put("startDate", ISO_DATETIME_FORMAT.format(e.getStartDate()));
                    else exp.put("startDate", null);
                    if (isNotNull(e.getEndDate())) exp.put("endDate", ISO_DATETIME_FORMAT.format(e.getEndDate()));
                    else exp.put("endDate", null);
                    List<JSONObject> pics = simpleMap(e.getCertification(), k -> {
                        JSONObject picture = propsFilter(e, PIC_JSON);
                        return picture;
                    });
                    exp.put("trainCertificate", pics);
                    return exp;
                });
            } else trainingExperiences = null;
            resumeJson.put("trainingExperiences", trainingExperiences);
        } else {
            resumeJson.put("personalHighlight", null);
            resumeJson.put("jobIntension", null);
            resumeJson.put("jobExperiences", null);
            resumeJson.put("trainingExperiences", null);
        }

        JSONObject shopBaseInfo = null;
        Shop shop = shopService.findByOwnerId(member.getId());
        if (isNotNull(shop)) {
            shopBaseInfo = propsFilter(shop, SHOP_BASE_INFO);
            StringBuffer area = new StringBuffer("");
            ifNotNullThen(shop.getProvince(), e -> area.append(e.getName()).append("-"));
            ifNotNullThen(shop.getCity(), e -> area.append(e.getName()).append("-"));
            ifNotNullThen(shop.getArea(), e -> area.append(e.getName()).append("-"));
            area.deleteCharAt(area.length() - 1);
            shopBaseInfo.put("area", area.toString());
            shopBaseInfo.put("XYZ", shop.getLongitude() + "," + shop.getLatitude());
            if (isNotNull(shop.getDate()))
                shopBaseInfo.put("createTime", ISO_DATETIME_FORMAT.format(shop.getDate()));
        }

        JSONObject accountBalance = new JSONObject();
        Account account = accountService.findByMemberToken(member.getToken());
        if (isNotNull(account))
            accountBalance.put("accountBalance", account.getBalance());
        else
            accountBalance.put("accountBalance", 0);
        BigDecimal withdrawAmountTotal = withdrawAccountService.countAllWithdraw(member.getToken());
        accountBalance.put("withdrawAmountTotal", withdrawAmountTotal);
        accountBalance.put("estpEarnings", accountFlowService.countEarnings(member.getToken(), "estpEarnings"));
        accountBalance.put("partnerEarnings", accountFlowService.countEarnings(member.getToken(), "partnerEarnings"));

        JSONObject businessType = new JSONObject();
        businessType.put("businessType", member.getEntrepreneurLevel().getTitle());
        List<EntrepreneursApply> entrepreneursApplies = entrepreneursService.findApplyList(member.getToken());
        if (!entrepreneursApplies.isEmpty()) {
            EntrepreneursApply entrepreneursApply = entrepreneursApplies.get(entrepreneursApplies.size() - 1);
            if (isNotNull(entrepreneursApply.getApplyDate()))
                businessType.put("businessTime", ISO_DATE_FORMAT.format(entrepreneursApply.getApplyDate()));
            else businessType.put("businessTime", null);
        }
        jsonObject.put("shopBaseInfo", shopBaseInfo);
        jsonObject.put("personalInfo", personalInfo1);
        jsonObject.put("resume", resumeJson);
        jsonObject.put("accountBalance", accountBalance);
        jsonObject.put("businessType", businessType);
        return ok(jsonObject);
    }


    /**
     * 启用
     */
    @RequestMapping(value = "/member", method = PUT)
    public Result enabled(Long id) {
        memberService.enabled(id);
        return OK();
    }

    /**
     * 11.1.5 谁看过他
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    @RequestMapping(value = "/wholookHim", method = GET)
    public Result whoLookHim(BackPageVo pageVO, String text, String visitTime, Long userId) {
        Page<ResumeLook> pages = resumeLookService.whoLookHim(pageVO, text, visitTime, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, WHO_LOOK_HIM_LIST);
            if (isNotNull(e.getDate())) jo.replace("date", ISO_DATETIME_FORMAT.format(e.getDate()));
            return jo;
        }));
    }

    /**
     * 11.1.6 谁看过他查看
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    @RequestMapping(value = "/whoLookHimDetail", method = GET)
    public Result whoLookHimDetail(Long id) {
        ResumeLook resumeLook = resumeLookService.findOne(id);
        JSONObject jsonObject = propsFilter(resumeLook, WHO_LOOK_HIM_DETAIL);
        if (isNotNull(resumeLook.getShop().getDate()))
            jsonObject.replace("createTime", ISO_DATETIME_FORMAT.format(resumeLook.getShop().getDate()));
        StringBuffer area = new StringBuffer("");
        ifNotNullThen(resumeLook.getShop().getProvince(), x -> area.append(x.getName()).append("-"));
        ifNotNullThen(resumeLook.getShop().getCity(), x -> area.append(x.getName()).append("-"));
        ifNotNullThen(resumeLook.getShop().getArea(), x -> area.append(x.getName()).append("-"));
        ifNotBlankThen(area.toString(), e -> area.deleteCharAt(area.length() - 1));
        jsonObject.put("shopArea", area);
        jsonObject.put("xyz", resumeLook.getShop().getLongitude() + "," + resumeLook.getShop().getLatitude());
        return OK(jsonObject);
    }

    /**
     * 11.1.7 推荐管理
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    @RequestMapping(value = "/recommendControList", method = GET)
    public Result recommendContro(BackPageVo pageVO, String text, RecState workStatus, String tableType, Long userId) {
        Page<JobApplyRecord> pages = jobApplyRecordService.findRecommendContro(pageVO, text, workStatus, tableType, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, REC_INFO_LIST);
            putInfo(e, jo, tableType);
            ifNotNullThen(e.getDate(), x -> jo.put("bindTime", ISO_DATETIME_FORMAT.format(x)));
            return jo;
        }));
    }

    public void putInfo(JobApplyRecord jobApplyRecord, JSONObject jsonObject, String tableType) {
        if (tableType.equals("recOthers")) {
            if (!jobApplyRecord.getReceiver().getRealName().equals(""))
                jsonObject.put("userName", jobApplyRecord.getReceiver().getRealName());
            else jsonObject.put("userName", null);
            if (!jobApplyRecord.getReceiver().getHpAccount().equals(""))
                jsonObject.put("hpAccount", jobApplyRecord.getReceiver().getHpAccount());
            else jsonObject.put("hpAccount", null);
        } else if (tableType.equals("OthersRecMe")) {
            if (!jobApplyRecord.getReferral().getRealName().equals(""))
                jsonObject.put("userName", jobApplyRecord.getReferral().getRealName());
            else jsonObject.put("userName", null);
            if (!jobApplyRecord.getReferral().getHpAccount().equals(""))
                jsonObject.put("hpAccount", jobApplyRecord.getReferral().getHpAccount());
            else jsonObject.put("hpAccount", null);
        }
        if (jobApplyRecord.getJob().getRecType() == ONCE)
            jsonObject.put("rewardType", "悬赏" + jobApplyRecord.getJob().getReward() + "元");
        else if (jobApplyRecord.getJob().getRecType() == MONTHLY)
            jsonObject.put("rewardType", "悬赏" + jobApplyRecord.getJob().getReward() + "元/月");
        else jsonObject.put("rewardType", null);
    }

    /**
     * 11.1.8 推荐管理查看
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    @RequestMapping(value = "/recommendControDetail", method = GET)
    public Result recommendControDetail(Long id, String tableType) {
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findOne(id);
        ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
        JSONObject jsonObject = propsFilter(jobApplyRecord, REC_INFO_DETAIL);
        putInfo(jobApplyRecord, jsonObject, tableType);
        ifNotNullThen(jobApplyRecord.getDate(), e -> jsonObject.put("bindTime", ISO_DATETIME_FORMAT.format(e)));
        if (isNotNull(jobApplyRecord.getStartDate()))
            jsonObject.put("joinTime", ISO_DATETIME_FORMAT.format(jobApplyRecord.getStartDate()));
        else jsonObject.put("joinTIme", null);
        if (isNotNull(jobApplyRecord.getResignDate()))
            jsonObject.put("leaveTime", ISO_DATETIME_FORMAT.format(jobApplyRecord.getResignDate()));
        else jsonObject.put("leaveTime", null);
        return OK(jsonObject);
    }

    /**
     * 11.1.9 100元车旅费
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    @RequestMapping(value = "/carFeeList", method = GET)
    public Result carFeeList(BackPageVo pageVo, String rewardTime, Long userId) {
        Page<BenefitRel> pages = benefitRelService.findCarFeePage(pageVo, rewardTime, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, CAR_FEE_LIST);
            JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(e.getMember().getId(), e.getJob().getId());
            ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
            jo.put("rewardTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            ifNotNullThen(jobApplyRecord.getReferral(), x -> {
                jo.put("referer", x.getRealName());
                jo.put("refererHpAccount", x.getHpAccount());
            });
            if (e.getJob().getRecType() == ONCE)
                jo.put("rewardType", "悬赏" + e.getJob().getReward() + "元");
            else if (e.getJob().getRecType() == MONTHLY)
                jo.put("rewardType", "悬赏" + e.getJob().getReward() + "元/月");
            else jo.put("rewardType", null);
            return jo;
        }));

    }

    /**
     * 11.1.10 免费培训
     *
     * @Author mu.jie
     * @Date 2017/3/6
     */
    @RequestMapping(value = "/freeTrainList", method = GET)
    public Result freeTraingList(BackPageVo pageVo, BenefitItemState useStatus, Long userId) {
        Page<BenefitItem> pages = benefitRelService.findFreeTrainPage(pageVo, useStatus, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, FREE_TRAING_LIST);

            ifNotNullThen(e.getEffectiveDate(), x -> jo.replace("effectiveDate", ISO_DATETIME_FORMAT.format(x)));
            ifNotNullThen(e.getCreateDate(), x -> jo.replace("createDate", ISO_DATETIME_FORMAT.format(x)));
            enumToJson(e.getState(), jo, "state");
            Job job = e.getBenefitRel().getJob();
            ifNotNullThen(job, x -> {
                if (x.getRecType() == ONCE)
                    jo.replace("benefitRel.job.reward", "悬赏" + x.getReward() + "元");
                else if (x.getRecType() == MONTHLY)
                    jo.replace("benefitRel.job.reward", "悬赏" + x.getReward() + "元/月");
            });

            return jo;
        }));
    }

    /**
     * 11.1.11 免费培训查看
     *
     * @Author mu.jie
     * @Date 2017/3/6
     */
    @RequestMapping(value = "/freeTrainDetail", method = GET)
    public Result freeTraingDetail(Long id) {
        BenefitItem one = benefitRelService.freeTrainDetail(id);
        String[] source_json = {"type.title:trainName", "benefitRel.job.name:position", "rewardType",
                "benefitRel.job.shop.name:shopName", "referer", "refererHpAccount",
                "workStatus", "joinTime", "leaveTime"};
        String[] useDetails_json = {
                "type.title:name", "createDate:getTime", "effectiveDate:validTime", "state.title:useStatus", "remark:useTo"
        };
        JSONObject body = new JSONObject();
        JSONObject sourceJson = propsFilter(one, source_json);
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(one.getBenefitRel().getMember().getId(), one.getBenefitRel().getJob().getId());
        ifNotNullThen(jobApplyRecord, x -> {
            sourceJson.replace("referer", x.getReferral().getRealName());
            sourceJson.replace("refererHpAccount", x.getReferral().getHpAccount());
            sourceJson.replace("workStatus", x.getRecState().getTitle());
            ifNotNullThen(x.getStartDate(), t -> sourceJson.replace("joinTime", ISO_DATETIME_FORMAT.format(t)));
            ifNotNullThen(x.getResignDate(), t -> sourceJson.replace("leaveTime", ISO_DATETIME_FORMAT.format(t)));
        });
        body.put("source", sourceJson);

        JSONObject useDetailsJson = propsFilter(one, useDetails_json);
        ifNotNullThen(one.getCreateDate(), x -> useDetailsJson.replace("getTime", ISO_DATETIME_FORMAT.format(x)));
        ifNotNullThen(one.getEffectiveDate(), x -> useDetailsJson.replace("validTime", ISO_DATETIME_FORMAT.format(x)));
        ifNotNullThen(one.getState(), x -> enumToJson(x, useDetailsJson, "useStatus"));
        body.put("useDetails", useDetailsJson);
        return OK(body);
    }

    /**
     * 11.1.12 免费培训修改
     *
     * @Author mu.jie
     * @Date 2017/3/6
     */
    @RequestMapping(value = "/freeTrain", method = POST)
    public Result updateFreeTrain(Long id, BenefitItemState useStatus, String useTo) {
        BenefitItem one = benefitRelService.updateFreeTrain(id, useStatus, useTo);
        return OK("success");
    }

    /**
     * 11.1.13 工资保障
     *
     * @Author xiao xue wei
     * @Date 2016/12/21
     */
    @RequestMapping(value = "/salaryProtectionPage", method = GET)
    public Result salaryProtectionPage(BackPageVo pageVo, Long userId) {
        Page<BenefitRel> pages = benefitRelService.findSalaryProtectionPage(pageVo, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, SALARY_PROTECTION_LIST);
            if (isNotNull(e.getDate()))
                jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            else jo.put("createTime", null);
            if (e.getValid() == YES)
                jo.put("status", "未处理");
            else jo.put("status", "已处理");
            BenefitApply benefitApply = benefitService.findBenefitApply(e.getMember(), SALARY_PROTECTION);
            if (isNotNull(benefitApply)) {
                if (!benefitApply.getRemindDates().isEmpty()) {
                    Date remind = benefitApply.getRemindDates().get(benefitApply.getRemindDates().size() - 1).getDate();
                    jo.put("remind", ISO_DATETIME_FORMAT.format(remind));
                } else jo.put("remind", null);
            } else jo.put("remind", null);
            return jo;
        }));
    }

    /**
     * 11.1.14 快速贷款
     *
     * @Author xiao xue wei
     * @Date 2016/12/21
     */
    @RequestMapping(value = "/quickLoanPage", method = GET)
    public Result quickLoanPage(BackPageVo pageVo, Long userId) {
        Page<BenefitApply> pages = benefitService.findQuickLoanPage(pageVo, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, QUICK_LOAN_LIST);
            if (isNotNull(e.getDate()))
                jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            else jo.put("createTime", null);
            if (e.getValid() == YES)
                jo.put("status", "未解决");
            else jo.put("status", "已解决");
            if (!e.getRemindDates().isEmpty()) {
                Date remind = e.getRemindDates().get(e.getRemindDates().size() - 1).getDate();
                jo.put("remind", ISO_DATETIME_FORMAT.format(remind));
            } else jo.put("remind", null);
            return jo;
        }));
    }

    /**
     * 11.1.15 惠美圈
     *
     * @Author xiao xue wei
     * @Date 2016/12/21
     */
    @RequestMapping(value = "/momentList", method = GET)
    public Result momentPage(BackPageVo pageVo, String text, Long userId) {
        Member member = memberService.findOne(userId);
        ifNullThrow(member, TIP_NO_MEMBER);
        Page<Moments> pages = momentsService.findMomentPage(pageVo, text, member.getId());
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, MOMENT_LIST);
            jo.replace("content", new String(e.getContent(), Charset.forName("UTF-8")));
            if (!e.getPics().isEmpty())
                jo.put("img", e.getPics().get(e.getPics().size() - 1).getPath());
            else jo.put("img", null);
            if (!e.getMemberFavorites().isEmpty())
                jo.put("like", e.getMemberFavorites().size());
            else jo.put("like", 0);
            if (!e.getCommentaries().isEmpty())
                jo.put("comment", e.getCommentaries().size());
            else jo.put("comment", 0);
            jo.put("publishTime", ISO_DATETIME_FORMAT.format(e.getPublishTime()));
            if (e.getIsDelete() == StateEnum.YES || e.getIsDelete() == StateEnum.HIDE)
                jo.put("state", false);
            else jo.put("state", true);
            return jo;
        }));
    }

    /**
     * 11.1.16收藏
     *
     * @Author xiao xue wei
     * @Date 2016/12/21
     */
    @RequestMapping(value = "/collections", method = GET)
    public Result collectionPage(BackPageVo pageVo, String text, String likeType, Long userId) {
        Member member = memberService.findOne(userId);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<JSONObject> jsonObjects = findCollectionsOfMember(text, likeType, member);
        List<JSONObject> rtList;
        if (jsonObjects.size() >= (pageVo.getPage() * pageVo.getRows()))
            rtList = jsonObjects.subList(((pageVo.getPage() * pageVo.getRows()) - pageVo.getRows()), ((pageVo.getPage() * pageVo.getRows())));
        else
            rtList = jsonObjects.subList(((pageVo.getPage() * pageVo.getRows()) - pageVo.getRows()), jsonObjects.size());
        return OK(new BackPage<>(rtList, pageVo.pageRequest(), jsonObjects.size()));
    }

    private List<JSONObject> findCollectionsOfMember(String text, String likeType, Member member) {
        //岗位收藏
        List<JSONObject> jobCollections = memberService.findJobCollections(member, text);
        //店铺收藏
        List<JSONObject> shopCollections = memberService.findShopCollections(member, text);
        //简历收藏
        List<JSONObject> resumeCollections = memberService.findResumeCollections(member, text);
        //视屏收藏
        List<JSONObject> videoCollections = memberService.findVideoCollections(member, text);
        //学校收藏
        List<JSONObject> schoolCollections = memberService.findSchoolCollections(member, text);
        //课程收藏
        List<JSONObject> CourseCollections = memberService.findCourseCollections(member, text);
        //图片收藏
        List<JSONObject> photoCollections = memberService.findPhotoCollections(member, text);

        List<JSONObject> allCollections = new ArrayList<>();
        allCollections.addAll(jobCollections);
        allCollections.addAll(shopCollections);
        allCollections.addAll(resumeCollections);
        allCollections.addAll(videoCollections);
        allCollections.addAll(schoolCollections);
        allCollections.addAll(CourseCollections);
        allCollections.addAll(photoCollections);

        if ("job".equals(likeType)) return jobCollections;
        else if ("shop".equals(likeType)) return shopCollections;
        else if ("resume".equals(likeType)) return resumeCollections;
        else if ("video".equals(likeType)) return videoCollections;
        else if ("school".equals(likeType)) return schoolCollections;
        else if ("course".equals(likeType)) return CourseCollections;
        else if ("picture".equals(likeType)) return photoCollections;
        else return allCollections;
    }

    /**
     * 11.1.17 屏蔽
     *
     * @Author xiao xue wei
     * @Date 2016/12/21
     */
    @RequestMapping(value = "/screenList", method = GET)
    public Result screenList(BackPageVo pageVo, String text, String tableType, Long userId) {
        Member member = memberService.findOne(userId);
        ifNullThrow(member, TIP_NO_MEMBER);
        if (tableType.equals("resumeScreen")) {
            Page<ResumeBlocked> pages = resumeBlockedService.findRBPage(pageVo, text, member.getToken());
            return OK(pages.map(e -> {
                JSONObject jo = propsFilter(e, RESUME_BLOCKED_LIST);
                jo.put("blockTime", ISO_DATETIME_FORMAT.format(e.getDate()));
                Shop shop = shopService.findByOwnerId(e.getResumeMember().getId());
                if (isNotNull(shop))
                    jo.put("shopName", shop.getName());
                else jo.put("shopName", null);
                return jo;
            }));
        } else if (tableType.equals("momentScreen")) {
            Page<MomentsBlocked> pages = momentsBlockedService.findMBPage(pageVo, text, member.getToken());
            return OK(pages.map(e -> {
                JSONObject jo = propsFilter(e, MOMENT_BLOCKED_LIST);
                jo.put("blockTime", ISO_DATETIME_FORMAT.format(e.getDate()));
                return jo;
            }));
        }
        return OK();
    }

    /**
     * 11.1.18 红包
     *
     * @Author xiao xue wei
     * @Date 2017/3/2
     */
    @RequestMapping(value = "/redPackets", method = GET)
    public Result redPackets(BackPageVo pageVo, RedPacketCategory type, RedPacketState useStatus, Long userId) {
        String[] json = {"id", "redPacket.category.title:type", "redPacket.amount:amount", "status"};
        Member member = memberService.findOne(userId);
        ifNullThrow(member, TIP_NO_MEMBER);
        Page<RedPacketReceive> page = redPacketService.findRedPacketReceives(pageVo.pageRequest(), type, useStatus, member);
        return OK(page.map(redPacketReceive -> {
            JSONObject jo = propsFilter(redPacketReceive, json);
            enumToJson(redPacketReceive.getState(), jo, "status");
            if (redPacketReceive.getRedPacket().getCategory() == RedPacketCategory.RED_PACKET_COURSE_TRAIN)
                jo.put("useLimit", "培训课程");
            else jo.put("useLimit", "一元购");
            if (isNotNull(redPacketReceive.getValidDate()))
                jo.put("validDate", ISO_DATETIME_FORMAT.format(redPacketReceive.getValidDate()));
            else jo.put("validDate", null);
            return jo;
        }));
    }

    /**
     * 11.1.19 红包查看
     *
     * @Author xiao xue wei
     * @Date 2017/3/2
     */
    @RequestMapping(value = "/redPacketDetal", method = GET)
    public Result redPacketDetail(Long id) {
        String[] json = {"redPacket.category:type", "redPacket.amount:amount", "status", "useTo", "useTime"};
        RedPacketReceive redPacketReceive = redPacketService.findMyRedPacket(id);
        JSONObject jsonObject = propsFilter(redPacketReceive, json);
        enumToJson(redPacketReceive.getState(), jsonObject, "status");
        if (redPacketReceive.getRedPacket().getCategory() == RedPacketCategory.RED_PACKET_COURSE_TRAIN)
            jsonObject.put("useLimit", "培训课程");
        else jsonObject.put("useLimit", "一元购");
        if (isNotNull(redPacketReceive.getValidDate()))
            jsonObject.put("validDate", ISO_DATETIME_FORMAT.format(redPacketReceive.getValidDate()));
        else jsonObject.put("validDate", null);
        if (isNotNull(redPacketReceive.getCreateTime()))
            jsonObject.put("getTime", ISO_DATETIME_FORMAT.format(redPacketReceive.getCreateTime()));
        else jsonObject.put("getTime", null);
        //TODO: useTo, useTime!!!
        if (redPacketReceive.getState() == RedPacketState.USED) {
            CourseApply courseApply = courseApplyService.findRedPacketCourseApply(redPacketReceive);
            if (isNotNull(courseApply)) {
                jsonObject.put("useTo", courseApply.getCourse().getName());
                jsonObject.put("useTime", ISO_DATETIME_FORMAT.format(courseApply.getDate()));
            }
        } else valueIsNull(jsonObject, null, "useTo", "useTime");
        return OK(jsonObject);
    }

    /**
     * 11.1.20 卡券
     *
     * @Author xiao xue wei
     * @Date 2017/3/3
     */
    @RequestMapping(value = "/cardCoupons", method = GET)
    public Result cardCoupons(BackPageVo pageVo, String text, CardCouponType type, String status, Long userId) {
        String[] json = {"id", "cardCoupon.name", "cardCoupon.type.title",};
        Member member = memberService.findOne(userId);
        ifNullThrow(member, TIP_NO_MEMBER);
        Page<CardCouponReceive> page = cardCouponReceiveService.findMembersCardCouponReceive(pageVo.pageRequest(), text, type, status, member);
        return OK(page.map(cardCouponReceive -> {
            JSONObject jsonObject = propsFilter(cardCouponReceive, json);
            if (isNotNull(cardCouponReceive.getCardCoupon().getValidDate()))
                jsonObject.put("validDate", ISO_DATETIME_FORMAT.format(cardCouponReceive.getCardCoupon().getValidDate()));
            else jsonObject.put("validDate", null);
            if (isNotNull(cardCouponReceive.getReceiveTime()))
                jsonObject.put("getTime", ISO_DATETIME_FORMAT.format(cardCouponReceive.getReceiveTime()));
            else jsonObject.put("getTime", null);
            if (cardCouponReceive.getCardCoupon().getType() == CardCouponType.COUPON && cardCouponReceive.getIsUse() == YES)
                jsonObject.put("status", "已使用");
            else if (cardCouponReceive.getIsOverdue() == YES)
                jsonObject.put("status", "已过期");
            else jsonObject.put("status", "正常");
            return jsonObject;
        }));
    }

    /**
     * 11.1.21 客户服务
     *
     * @Author xiao xue wei
     * @Date 2016/12/21
     */
    @RequestMapping(value = "/feedBackList", method = GET)
    public Result feedBackList(BackPageVo pageVo, String text, Long userId) {
        Page<FeedBack> pages = feedBackService.findFeedBackPage(pageVo, text, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, FEED_BACK_LIST);
            jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            if (e.getIsDeal() == YES)
                jo.put("status", "已解决");
            else jo.put("status", "未解决");
            return jo;
        }));
    }

    /**
     * 11.1.22 投诉记录
     *
     * @Author xiao xue wei
     * @Date 2016/12/22
     */
    @RequestMapping(value = "/complainList", method = GET)
    public Result ComplainPage(BackPageVo pageVo, ComplainType complainType, Long userId) {
        Page<Complain> pages = complainService.findComplainList(pageVo, complainType, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, COMPLAIN_LIST);
            if (e.getType() == COMPLAINT_TO_STORE || e.getType() == USER_COMPLAINT_TO_STORE_UNCONFIRM_WORK)
                if (isNotNull(e.getShop())) {
                    jo.put("complain", e.getShop().getName());
                } else jo.put("complain", null);
            else if (e.getType() == COMPLAINT_TO_SCHOOL)
                if (isNotNull(e.getSchool()))
                    jo.put("complain", e.getSchool().getName());
                else jo.put("complain", null);
            else {
                if (isNotNull(e.getDefendant()))
                    jo.put("complain", e.getDefendant().getRealName());
            }
            jo.put("complainTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            if (e.getIsDeal() == NO)
                jo.put("status", "未解决");
            else jo.put("status", "已解决");
            return jo;
        }));
    }

    /**
     * 11.1.23 好友
     *
     * @Author xiao xue wei
     * @Date 2016/12/22
     */
    @RequestMapping(value = "/friendsList", method = GET)
    public Result friendsList(BackPageVo pageVo, String text, String friendType, Long userId) {
        List<Member> list = memberService.findFriendsList(text, friendType, userId);
        List<JSONObject> jsList = new ArrayList<>();
        list.forEach(e -> {
            JSONObject jo = propsFilter(e, FRIENDS_LIST);
            JSONObject jo1 = propsFilter(e, FRIENDS_LIST);
            JSONObject jo2 = propsFilter(e, FRIENDS_LIST);
            Map<String, Object> map;
            Map<String, Object> map1;
            Map<String, Object> map2;
            if ("REG_ONE".equals(friendType) || "REG_TWO".equals(friendType) || "REG_THREE".equals(friendType)) {
                map = memberRegRelService.oneToOneReg(userId, e.getId());
                map1 = new HashMap<>();
                map2 = new HashMap<>();
            } else if ("REC_ONE".equals(friendType) || "REC_TWO".equals(friendType) || "REC_THREE".equals(friendType)) {
                map = new HashMap<>();
                map1 = memberRecRelService.oneToOneRec(userId, e.getId());
                map2 = new HashMap<>();
            } else if ("HUIYOU".equals(friendType)) {
                map = new HashMap<>();
                map1 = new HashMap<>();
                map2 = friendsService.oneToOneHuiyou(userId, e.getId());
            } else {
                map = memberRegRelService.oneToOneReg(userId, e.getId());
                map1 = memberRecRelService.oneToOneRec(userId, e.getId());
                map2 = friendsService.oneToOneHuiyou(userId, e.getId());
            }
            if (!map.isEmpty()) {
                jo.put("friendType", map.get("relation"));
                jo.put("createTime", ISO_DATETIME_FORMAT.format(map.get("time")));
                jsList.add(jo);
            }
            if (!map1.isEmpty()) {
                jo1.put("friendType", map1.get("relation"));
                jo1.put("createTime", ISO_DATETIME_FORMAT.format(map1.get("time")));
                jsList.add(jo1);
            }
            if (!map2.isEmpty()) {
                jo2.put("friendType", map2.get("relation"));
                jo2.put("createTime", ISO_DATETIME_FORMAT.format(map2.get("time")));
                jsList.add(jo2);
            }
        });
        List<JSONObject> rtList;
        if (jsList.size() >= (pageVo.getPage() * pageVo.getRows()))
            rtList = jsList.subList(((pageVo.getPage() * pageVo.getRows()) - pageVo.getRows()), ((pageVo.getPage() * pageVo.getRows())));
        else
            rtList = jsList.subList(((pageVo.getPage() * pageVo.getRows()) - pageVo.getRows()), jsList.size());
        return OK(new BackPage<>(rtList, pageVo.pageRequest(), jsList.size()));
    }

    /**
     * 11.1.24 提现账户
     *
     * @Author xiao xue wei
     * @Date 2016/12/22
     */
    @RequestMapping(value = "/withdrawAccountList", method = GET)
    public Result withdrawAccountList(BackPageVo pageVo, Long userId) {
        Page<WithdrawAccount> pages = withdrawAccountService.findHisWithdraw(userId, pageVo);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, WITHDRAWACCOUNT_LIST);
            jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            return jo;
        }));
    }

    /**
     * 11.1.25 提现账户详情
     *
     * @Author xiao xue wei
     * @Date 2016/12/22
     */
    @RequestMapping(value = "/withdrawAccountDetail", method = GET)
    public Result withdrawAccountDetail(Long id) {
        JSONObject jsonObject = new JSONObject();
        WithdrawAccount withdrawAccount = withdrawAccountService.findOne(id);
        if (withdrawAccount.getType() == WITHDRAW_ACCOUNT_ALIPAY) {
            jsonObject.put("widthDrawWay", "支付宝");
            jsonObject.put("username", withdrawAccount.getName());
            jsonObject.put("alipayAccount", withdrawAccount.getAccount());
        } else if (withdrawAccount.getType() == WITHDRAW_ACCOUNT_WX) {
            jsonObject.put("widthDrawWay", "微信");
            jsonObject.put("username", withdrawAccount.getName());
            jsonObject.put("wxAccount", withdrawAccount.getAccount());
        } else if (withdrawAccount.getType() == WITHDRAW_ACCOUNT_BANK_CARD) {
            JSONObject account = new JSONObject();
            account.put("widthDrawWay", "银行卡");
            account.put("username", withdrawAccount.getName());
            account.put("bankNo", withdrawAccount.getAccount());
            account.put("bankName", withdrawAccount.getBank());
            account.put("brandBankInfo", withdrawAccount.getBranchBank());
            JSONObject creditInfo = new JSONObject();
            MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(withdrawAccount.getMember().getToken());
            creditInfo.put("IDcard", memberExtInfo.getIdCardNo());
            creditInfo.put("IDCardFrontImg", withdrawAccount.getIdCard().getPath());
            creditInfo.put("IDCardHalfImg", withdrawAccount.getHalf().getPath());
            jsonObject.put("account", account);
            jsonObject.put("creditInfo", creditInfo);
        }
        return OK(jsonObject);
    }

    /**
     * 11.1.26 账单
     *
     * @Author xiao xue wei
     * @Date 2016/12/23
     */
    @RequestMapping(value = "/accountFlowList", method = GET)
    public Result accountFlowList(BackPageVo pageVo, String text, Date startTime, Date endTime, RecordType billType, Long userId) {
        Page<AccountFlow> pages = accountFlowService.findAccountFlowPage(pageVo, text, startTime, endTime, billType, userId);
        return OK(pages.map(e -> {
            JSONObject jo = propsFilter(e, ACCOUNT_FLOW_LIST);
            if (e.getRecordType() == BILL_RECHARGE || e.getRecordType() == BILL_INCOME || e.getRecordType() == BILL_REFUND) {
                jo.put("amount", "+" + e.getAmount());
            } else jo.put("amount", "-" + e.getAmount());
            jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getCreateDate()));
            return jo;
        }));
    }

    /**
     * 11.1.27 账单详情
     *
     * @Author xiao xue wei
     * @Date 2016/12/28
     */
    @RequestMapping(value = "/accountFlowDetail", method = GET)
    public Result accountFlowDetail(Long id) {
        String[] WITHDRAW_DETAIL = {
                "recordType.title:type", "state.title:status", "recordType.title:remark", "withdrawAccount.type.title:account",
        };
        String[] ENTREPRENEURS_APPLY_FEE_DETAIL = {
                "source.title:type", "payType.title:payWay", "remark",
        };
        String[] PARTNER_APPLY_FEE_DETAIL = {
                "source.title:type", "payType.title:payWay", "remark",
        };
        AccountFlow accountFlow = accountFlowService.findOne(id);
        ifNullThrow(accountFlow, TIP_NO_ACCOUNT_FLOW);
        JSONObject jsonObject = new JSONObject();
        if (accountFlow.getRecordType() == BILL_WITHDRAW) {//提现 审核中，审核失败，审核成功
            jsonObject = propsFilter(accountFlow, WITHDRAW_DETAIL);
            jsonObject.put("expend", "-" + accountFlow.getAmount());
            jsonObject.put("applyTime", ISO_DATETIME_FORMAT.format(accountFlow.getApplyDate()));
            if (accountFlow.getState() == FAILED) {
                jsonObject.put("failureReason", accountFlow.getReason());
            } else if (accountFlow.getState() == SUCCESS) {
                jsonObject.put("payTime", ISO_DATETIME_FORMAT.format(accountFlow.getReceivedDate()));
            }
        } else if (accountFlow.getSource() == ENTREPRENEUR_APPLY && accountFlow.getType() == PAY_OUT) {//创业者付费
            jsonObject = propsFilter(accountFlow, ENTREPRENEURS_APPLY_FEE_DETAIL);
            jsonObject.put("expend", "-" + accountFlow.getAmount());
            jsonObject.put("payTime", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate()));
        } else if (accountFlow.getSource() == PARTNER_APPLY && accountFlow.getType() == PAY_OUT) {//合伙人付费
            jsonObject = propsFilter(accountFlow, PARTNER_APPLY_FEE_DETAIL);
            jsonObject.put("expend", "-" + accountFlow.getAmount());
            jsonObject.put("partnerArea", partnerArea(accountFlow.getPartnerApply()));
            jsonObject.put("payTime", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate()));
        } else if (accountFlow.getSource() == COURSE_APPLY && accountFlow.getType() == PAY_OUT) {//培训费付费
            jsonObject = propsFilter(accountFlow, PARTNER_APPLY_FEE_DETAIL);
            jsonObject.put("expend", "-" + accountFlow.getAmount());
            jsonObject.put("payTime", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate()));
        } else if (accountFlow.getSource() == COURSE_REFUND && accountFlow.getType() == PAY_IN) {//培训费退费 成功(审核中和失败不会产生流水)
            jsonObject = propsFilter(accountFlow, PARTNER_APPLY_FEE_DETAIL);
            jsonObject.put("expend", "+" + accountFlow.getAmount());
            CourseRefundApply courseRefundApply = accountFlow.getCourseRefundApply();
            jsonObject.put("refundType", courseRefundApply.getWay().getTitle());
            jsonObject.put("applyTime", ISO_DATETIME_FORMAT.format(courseRefundApply.getCreateTime()));
            jsonObject.put("toAccount", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate()));
        } else if (accountFlow.getSource() == EMPLOYEE_CAR_FEE && accountFlow.getType() == PAY_IN) {//100元车旅费
            jsonObject.put("earnings", "+" + accountFlow.getAmount());
            jsonObject.put("time", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate()));
            if (isNotNull(accountFlow.getJobApplyRecord())) {
                jsonObject.put("recruitPosition", accountFlow.getJobApplyRecord().getJob().getName());
                jsonObject.put("rewardType", accountFlow.getJobApplyRecord().getJob().getRecType().getTitle());
                JSONObject shop = new JSONObject();
                shop.put("coverImg", accountFlow.getJobApplyRecord().getJob().getShop().getLogo().getPath());
                shop.put("shopName", accountFlow.getJobApplyRecord().getJob().getShop().getName());
                jsonObject.put("shop", shop);
                jsonObject.put("referer", accountFlow.getJobApplyRecord().getReferral().getRealName());
            }
        } else if (accountFlow.getSource() == PARTNER_ONCE_AWARD || accountFlow.getSource() == PARTNER_MONTHLY_AWARD) {
            jsonObject.put("earnings", "+" + accountFlow.getAmount());
            jsonObject.put("time", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate()));
            if (isNotNull(accountFlow.getJobApplyRecord())) {
                jsonObject.put("recruitPosition", accountFlow.getJobApplyRecord().getJob().getName());
                jsonObject.put("rewardType", accountFlow.getJobApplyRecord().getJob().getRecType().getTitle());
                JSONObject shop = new JSONObject();
                shop.put("coverImg", accountFlow.getJobApplyRecord().getJob().getShop().getLogo().getPath());
                shop.put("shopName", accountFlow.getJobApplyRecord().getJob().getShop().getName());
                jsonObject.put("shop", shop);
                jsonObject.put("area", shopArea(accountFlow.getJobApplyRecord().getShop()));
                jsonObject.put("referer", accountFlow.getJobApplyRecord().getReferral().getRealName());
            }
        } else if (accountFlow.getSource() == JOB_NEW) {//岗位招聘付费
            jsonObject.put("expend", "-" + accountFlow.getAmount());
            jsonObject.put("type", accountFlow.getRecordType().getTitle());
            jsonObject.put("payWay", accountFlow.getPayType().getTitle());
            final JSONObject finalJsonObject = jsonObject;
            ifNotNullThen(accountFlow.getCreateDate(), t -> finalJsonObject.put("payTime", ISO_DATETIME_FORMAT.format(t)));
            jsonObject = finalJsonObject;
            jsonObject.put("remark", accountFlow.getRemark());
        } else if (accountFlow.getSource() == JOB_ADD) {
            jsonObject.put("expend", "-" + accountFlow.getAmount());
        }
        return OK(jsonObject);
    }

    /**
     * 9.12.1 用户行为数据分析列表
     *
     * @Author mu.jie
     * @Date 2016/12/23
     */
    @RequestMapping(value = "/statistics", method = GET)
    public Result memberVisitStatistics(Long provinceId, Long cityId, Long areaId, Integer year, Integer month) {
        List<JSONObject> body = new ArrayList<>();
        JSONObject date = new JSONObject();
        JSONObject countNotLoginMember = new JSONObject();
        Integer dayOfMonthNum = dayOfMonthNum(year, month);
        for (int day = 0; day < dayOfMonthNum; day++) {
            Long num = memberVisitService.memberVisitStatistics(provinceId, cityId, areaId, year, month, day);
            date.put(day + "", num);
        }
        countNotLoginMember.put("type", "未登录APP使用人数");
        countNotLoginMember.put("date", date);
        body.add(countNotLoginMember);

        JSONObject countMemberRegister = new JSONObject();
        date.clear();
        for (int day = 0; day < dayOfMonthNum; day++) {
            Long num = memberVisitService.memberRegisterStatistics(provinceId, cityId, areaId, year, month, day);
            date.put(day + "", num);
        }
        countMemberRegister.put("date", date);
        countMemberRegister.put("type", "注册用户人数");
        body.add(countMemberRegister);

        return OK(body);
    }

    public String shopArea(Shop shop) {
        StringBuffer stringBuffer1 = new StringBuffer("");
        ifNotNullThen(shop.getProvince(), t -> stringBuffer1.append(t.getName()));
        ifNotNullThen(shop.getCity(), t -> stringBuffer1.append("-").append(t.getName()));
        ifNotNullThen(shop.getArea(), t -> stringBuffer1.append("-").append(t.getName()));
        return stringBuffer1.toString();
    }

    public String partnerArea(PartnerApply partnerApply) {
        StringBuffer stringBuffer = new StringBuffer("");
        StringBuffer stringBuffer1 = new StringBuffer("");
        ifNotNullThen(partnerApply.getProvince(), t -> stringBuffer1.append(t.getName()));
        ifNotNullThen(partnerApply.getCity(), t -> stringBuffer1.append("-").append(t.getName()));
        ifNotNullThen(partnerApply.getArea(), t -> stringBuffer1.append("-").append(t.getName()));
        stringBuffer.append(stringBuffer1);
        StringBuffer stringBuffer2 = new StringBuffer("");
        ifNotNullThen(partnerApply.getProvince2(), t -> stringBuffer2.append(t.getName()));
        ifNotNullThen(partnerApply.getCity2(), t -> stringBuffer2.append("-").append(t.getName()));
        ifNotNullThen(partnerApply.getArea2(), t -> stringBuffer2.append("-").append(t.getName()));
        ifNotBlankThen(stringBuffer2.toString(), t -> stringBuffer.append(",").append(t));
        return stringBuffer.toString();
    }

    /**
     * 11.1.28 创业费用管理
     *
     * @Author xiao xue wei
     * @Date 2017/2/10
     */
    @RequestMapping(value = "/entreFeeList", method = GET)
    public Result entreFeeList(BackPageVo pageVo, String text, EntrepreneursType estpType, Long userId) {
        String[] ENTREFEE_LIST_INFO = {"id", "entrepreneursApply.member.realName", "entrepreneursApply.member.mobile",
                "entrepreneursApply.member.hpAccount", "entrepreneursApply.type.title", "amount",};
        Page<AccountFlow> page = accountFlowService.findEntreFeePage(pageVo.pageRequest(), text, estpType, userId);
        return OK(page.map(e -> {
            JSONObject jo = propsFilter(e, ENTREFEE_LIST_INFO);
            if (isNotNull(e.getCreateDate()))
                jo.put("countTime", ISO_DATETIME_FORMAT.format(e.getCreateDate()));
            else jo.put("countTime", null);
            return jo;
        }));
    }

    /**
     * 1.7 账户信息
     *
     * @Author xiao xue wei
     * @Date 2017/2/26
     */
    @RequestMapping(value = "/accountInfo", method = GET)
    public Result accountInfo(String userToken) {
        Account account = accountService.findByMemberToken(userToken);
        ifNullThrow(account, TIP_NO_ACCOUNT);
        JSONObject jsonObject = propsFilter(account, "balance");
        jsonObject.put("carryAbleBalance", account.getBalance().subtract(account.getFreezingAmount()));
        if (isNotBlank(account.getPayPassword())) jsonObject.put("hasPassword", true);
        else jsonObject.put("hasPassword", false);
        return OK(jsonObject);
    }

    /**
     * 11.1.29 培训订单
     *
     * @Author xiao xue wei
     * @Date 2017/3/6
     */
    @RequestMapping(value = "/courseOrder", method = GET)
    public Result courseOrder(BackPageVo pageVO, CourseApplyState status, Long userId) {
        String[] COURSE_APPLY_LIST_INFO = {"id", "serialNo", "course.name", "state", "course.price",
                "discount", "price"};
        Page<CourseApply> page = courseApplyService.findMyCourseApplyPage(pageVO.pageRequest(), status, userId);
        return OK(page.map(courseApply -> {
            JSONObject jo = propsFilter(courseApply, COURSE_APPLY_LIST_INFO);
            jo.put("payableAmount", courseApply.getPrice().subtract(courseApply.getDiscount()));
            if (isNotNull(courseApply.getDate())) jo.put("signTime", ISO_DATE_FORMAT.format(courseApply.getDate()));
            else jo.put("signTime", null);
            if (isNotNull(courseApply.getTrainDate()))
                jo.put("trainTime", ISO_DATE_FORMAT.format(courseApply.getTrainDate()));
            else jo.put("trainTime", null);
            enumToJson(courseApply.getState(), jo, "state");
            return jo;
        }));
    }
}
