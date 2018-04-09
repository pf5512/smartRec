package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.repository.MemberExtInfoRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.EarningRepository;
import com.thousandsunny.service.repository.PartnerApplyRepository;
import com.thousandsunny.service.repository.PartnerRepository;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartyPayAccountService;
import com.thousandsunny.thirdparty.domain.service.UtilsService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.service.ModuleKey.EarningType.PARTNER_MONTHLY_AWARD;
import static com.thousandsunny.service.ModuleKey.EarningType.PARTNER_ONCE_AWARD;
import static com.thousandsunny.service.ModuleKey.KeyPercentage.CONSTANT_PARTNER_FEE;
import static com.thousandsunny.service.ModuleKey.RecState.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType.PARTNER_PAY;
import static com.thousandsunny.service.ModuleKey.WorkerQueryType.*;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.CANCEL;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static com.thousandsunny.thirdparty.ModuleKey.PayOfflineType.PAY_OFFLINE_BANK;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_IN;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.PARTNER_APPLY;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.ListUtils.union;

@Service
public class PartnerService extends BaseService<Partner> {
    @Autowired
    private PartnerApplyService partnerApplyService;
    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;
    @Autowired
    private EarningRepository earningRepository;
    @Autowired
    private ThirdPartyPayAccountService payAccountService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MemberExtInfoRepository memberExtInfoRepository;
    @Autowired
    private UtilsService utilsService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private PartnerApplyRepository partnerApplyRepository;
    @Autowired
    private SrAccountApplyRecordService accountApplyService;
    @Autowired
    private MemberService memberService;

    private List<ApplyEnum> applyList_ = newArrayList(REVIEW_SUCCESS, REVIEW_FAILED, OFFLINE_PAY_CONFIRM, ApplyEnum.SUCCESS);
    private List<ApplyEnum> applyList = newArrayList(REVIEW_SUCCESS, OFFLINE_PAY_CONFIRM, ApplyEnum.SUCCESS);
    private List<ApplyEnum> applyAreaList = newArrayList(REVIEW_SUCCESS, OFFLINE_PAY_CONFIRM, ApplyEnum.SUCCESS, IN_REVIEW);

    public String apply(String userToken, PartnerApply partnerApply) {
        checkApplyArea(partnerApply);
        Member member = memberService.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<PartnerApply> list = partnerApplyRepository.findByMemberTokenAndStateIn(member.getToken(), applyAreaList);
        if (!list.isEmpty()) {
            final int[] count = {0};
            list.forEach(e -> {
                if (isNotNull(e.getProvince())) count[0]++;
                if (isNotNull(e.getProvince2())) count[0]++;
            });
            if (isNotNull(partnerApply.getProvince())) count[0]++;
            if (isNotNull(partnerApply.getProvince2())) count[0]++;
            ifTrueThrow(count[0] > 2, TIP_HAS_PARTNER_APPLY);
        }

        partnerApply.setMember(member);
        partnerApply.setState(IN_REVIEW);
        if (isNotNull(partnerApply.getHalf())) {
            partnerApply.getHalf().setType(IMAGE);
            cloudFileRepository.save(partnerApply.getHalf());
        }
        if (isNotNull(partnerApply.getIdCard())) {
            partnerApply.getIdCard().setType(IMAGE);
            cloudFileRepository.save(partnerApply.getIdCard());
        }
        ifFalseThrow(identityCheck(partnerApply), TIP_ERROR_IDENTITY);
        partnerApplyService.save(partnerApply);
        return "success";
    }

    public void checkApplyArea(PartnerApply partnerApply) {
        //已经被申请的地区不能被重复申请（即一个地区只能存在一个合伙人）
        if (isNotNull(partnerApply.getArea())) {
            Long id1 = partnerApply.getProvince().getId();
            Long id2 = partnerApply.getCity().getId();
            Long id3 = partnerApply.getArea().getId();
            PartnerApply par = partnerApplyService.findByProvinceIdAndCityIdAndAreaIdAndStateIn(id1, id2, id3, applyAreaList);
            ifNotNullThrow(par, TIP_HAS_PARTNER);
        }
        if (isNotNull(partnerApply.getArea2())) {
            Long id21 = partnerApply.getProvince2().getId();
            Long id22 = partnerApply.getCity2().getId();
            Long id23 = partnerApply.getArea2().getId();
            PartnerApply par1 = partnerApplyService.findByProvinceIdAndCityIdAndAreaIdAndStateIn(id21, id22, id23, applyAreaList);
            ifNotNullThrow(par1, TIP_HAS_PARTNER);
        }
    }

    private Boolean identityCheck(PartnerApply partnerApply) {
        MemberExtInfo memberExtInfo = memberExtInfoRepository.findByMemberToken(partnerApply.getMember().getToken());
        ifNullThrow(memberExtInfo, TIP_NO_MEMBEREXTINFO);
        if (partnerApply.getMember().getIdentityHasPass() == NO) {
            ifNotNullThen(partnerApply.getName(), memberExtInfo.getMember()::setRealName);
            ifNotNullThen(partnerApply.getMobile(), memberExtInfo.getMember()::setMobile);
            ifNotNullThen(partnerApply.getIdCardNo(), memberExtInfo::setIdCardNo);
            ifNotNullThen(partnerApply.getIdCard(), memberExtInfo::setIdCard);
            ifNotNullThen(partnerApply.getHalf(), memberExtInfo::setHalf);
        } else {
            if (memberExtInfo.getMember().getRealName() != null) {
                if (!partnerApply.getName().equals(memberExtInfo.getMember().getRealName()))
                    return false;
            } else {
                memberExtInfo.getMember().setRealName(partnerApply.getName());
            }
            if (memberExtInfo.getMember().getMobile() != null) {
                if (!partnerApply.getMobile().equals(memberExtInfo.getMember().getMobile()))
                    return false;
            } else {
                memberExtInfo.getMember().setMobile(partnerApply.getMobile());
            }
            if (memberExtInfo.getIdCardNo() != null) {
                if (!partnerApply.getIdCardNo().equals(memberExtInfo.getIdCardNo()))
                    return false;
            } else {
                memberExtInfo.setIdCardNo(partnerApply.getIdCardNo());
            }
            if (memberExtInfo.getIdCard() != null) {
                if (!partnerApply.getIdCard().getPath().equals(memberExtInfo.getIdCard().getPath()))
                    return false;
            } else {
                memberExtInfo.setIdCard(partnerApply.getIdCard());
            }
            if (memberExtInfo.getHalf() != null) {
                if (!partnerApply.getHalf().getPath().equals(memberExtInfo.getHalf().getPath()))
                    return false;
            } else {
                memberExtInfo.setHalf(partnerApply.getHalf());
            }
        }
        return true;
    }

    public List<PartnerApply> getState(String userToken) {
        return partnerApplyService.findByMemberTokenOrderByDate(userToken);
    }

    public List<Partner> getPartner(String userToken) {
        List<Partner> partners = partnerRepository.findByMemberTokenOrderByDate(userToken);
        ifTrueThrow(partners.isEmpty(), TIP_NO_PARTNER_APPLY);
        return partners;
    }

    public BigDecimal getIncome(List<Partner> partners) {
        final BigDecimal[] income = {new BigDecimal(0)};
        partners.forEach(e -> income[0] = income[0].add(e.getIncome()));
        return income[0];
    }

    /**
     * 合伙人申请付款
     */
    public Charge payForApply(String userToken, PayType payType, String openId) {
        List<PartnerApply> partnerApplys = partnerApplyService.findByMemberTokenOrderByDate(userToken);
        ifTrueThrow(partnerApplys.size() == 0, TIP_NO_PARTNER_APPLY);
        PartnerApply partnerApply = partnerApplys.get(partnerApplys.size() - 1);

        ifTrueThrow(partnerApply.getState() == SUCCESS, TIP_NO_PAY);
        ifTrueThrow(partnerApply.getState() != REVIEW_SUCCESS, TIP_ERROR_PARTNER_APPLY_STATUS);
        BigDecimal money;
        if (isNull(partnerApply.getProvince2()) && isNull(partnerApply.getCity2()) && isNull(partnerApply.getArea2()))
            money = CONSTANT_PARTNER_FEE.val();
        else
            money = CONSTANT_PARTNER_FEE.val().add(CONSTANT_PARTNER_FEE.val());

        partnerApply.setJoinMoney(money);

        Charge charge = payAccountService.choosePayType(userToken, payType, money, PARTNER_APPLY, null, null, null, partnerApply, null, "合伙人付款", openId);
        partnerApply.setUpdateDate(new Date());
        ifTrueThen(payType == PAY_OFFLINE, () -> partnerApply.setState(OFFLINE_PAY_CONFIRM));

        ifTrueThen(payType == PAY_BY_BALANCE, () -> {//余额付款
            accountFlowService.processPlatform(null, money, PARTNER_APPLY, PLATFORM_IN, partnerApply, null, null, null, payType);
            //添加到合伙人
            createPartner(partnerApply);
            //修改member的合伙人身份
            Member member = memberService.findByToken(userToken);
            member.setPartnerLevel(YES);
            memberService.save(member);
        });

        ifTrueThen(payType == PAY_BY_WX || payType == PAY_BY_ALIPAY || payType == PAY_BY_WX_PUB, () -> {//第三方支付
            SrAccountApplyRecord accountApplyRecord = accountApplyService.findByOrderNo(charge.getOrderNo());
            accountApplyRecord.setSource(PARTNER_PAY);
            accountApplyRecord.setAmount(money);
            accountApplyRecord.setPartnerApply(partnerApply);
            accountApplyService.save(accountApplyRecord);
        });
        partnerApplyService.save(partnerApply);
        return charge;

    }

    public void createPartner(PartnerApply partnerApply) {
        Partner partner = findByMemberIdAndAreaId(partnerApply.getMember().getId(), partnerApply.getArea().getId());
        ifNotNullThrow(partner, TIP_HAS_PARTNER);
        partner = new Partner();
        copyProperties(partner, partnerApply);
        partner.setId(null);
        Account account = accountService.findByMemberMobile(partnerApply.getMobile());
        partner.setAccount(account);

        final Partner finalPartner = partner;
        ifNotNullThen(partnerApply.getProvince2(), r -> {//如果有两个地区
            Partner partner2 = findByMemberIdAndAreaId(partnerApply.getMember().getId(), partnerApply.getArea2().getId());
            ifNotNullThrow(partner2, TIP_HAS_PARTNER);
            partner2 = new Partner();
            copyProperties(partner2, finalPartner);
            partner2.setProvince(partnerApply.getProvince2());
            partner2.setCity(partnerApply.getCity2());
            partner2.setArea(partnerApply.getArea2());
            save(partner2);
        });
        partnerApply.setState(SUCCESS);
        partnerApplyRepository.save(partnerApply);
        save(partner);
    }


    public Page<Earning> getPartnerEarning(String userToken, Pageable pageable) {
        return earningRepository.findByPartnerMemberTokenAndEarningTypeOrEarningType(userToken, PARTNER_ONCE_AWARD, PARTNER_MONTHLY_AWARD, pageable);
    }


    public void reviewPartnerApply(String userToken, Long id, OperatorType type) {
        PartnerApply partnerApply = partnerApplyService.findOne(id);
        ifNullThrow(partnerApply, TIP_NO_APPLY);
        ifTrueThrow(partnerApply.getState() != IN_REVIEW, TIP_CANT_REVIEW);
        ifTrueThen(type == SURE, () -> passPartnerApply(partnerApply));//同意
        ifTrueThen(type == CANCEL, () -> partnerApply.setState(REVIEW_FAILED));//拒绝
        partnerApplyService.save(partnerApply);
    }

    /**
     * 通过合伙人申请
     */
    private void passPartnerApply(PartnerApply partnerApply) {
        partnerApply.setState(REVIEW_SUCCESS);
        Member partner = partnerApply.getMember();
        partner.setIdentityHasPass(YES);
        memberService.save(partner);
    }

    /**
     * 合伙人线下付款确认成功
     */
    public PartnerApply confirm(String userToken, OperatorType type) {
        PartnerApply partnerApply = partnerApplyService.findByMemberTokenAndState(userToken, OFFLINE_PAY_CONFIRM);
        ifNullThrow(partnerApply, TIP_NO_APPLY_RECORD);
        if (type == SURE) {
            confirmPartnerApply(partnerApply);
        } else {
            partnerApply.setState(REVIEW_SUCCESS);
        }
        saveAccountFlow(partnerApply);
        return partnerApplyService.save(partnerApply);

    }

    void confirmPartnerApply(PartnerApply partnerApply) {
        partnerApply.setState(ApplyEnum.SUCCESS);

        String userToken = partnerApply.getMember().getToken();
        Partner partner = new Partner();
        copyProperties(partner, partnerApply);
        partner.setId(null);

        Account account = accountService.findByMemberToken(userToken);
        ifNullThrow(account, TIP_NO_ACCOUNT);
        partner.setAccount(account);
        partnerRepository.save(partner);
        if (!isNull(partnerApply.getProvince2())) {
            Partner _partner = new Partner();
            copyProperties(_partner, partnerApply);
            _partner.setId(null);
            ifNotNullThen(partnerApply.getProvince2(), _partner::setProvince);
            ifNotNullThen(partnerApply.getCity2(), _partner::setCity);
            ifNotNullThen(partnerApply.getArea2(), _partner::setArea);
            partner.setAccount(account);
            partnerRepository.save(_partner);
        }
        Member member = memberService.findByToken(userToken);
        member.setPartnerLevel(YES);
        memberService.save(member);
        //往系统中加钱，自己账户不减钱
        Account zuesAccount = accountService.findZuesAccount();
        zuesAccount.setBalance(zuesAccount.getBalance().add(partnerApply.getJoinMoney()));
        zuesAccount.setTotal(zuesAccount.getTotal().add(partnerApply.getJoinMoney()));
        accountService.save(zuesAccount);

        accountFlowService.processPlatform(null, partnerApply.getJoinMoney(), PARTNER_APPLY, PLATFORM_IN, partnerApply, null, null, null, PAY_OFFLINE);//产生平台流水
    }

    Integer countByMemberToken(String token) {
        return partnerRepository.countByMemberToken(token);
    }

    /**
     * 保存业务流水
     */
    private AccountFlow saveAccountFlow(PartnerApply partnerApply) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAmount(partnerApply.getJoinMoney());
        accountFlow.setType(PAY_OUT);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRecordType(BILL_PAY_OFFLINE);
        accountFlow.setSource(PARTNER_APPLY);
        accountFlow.setState(FlowState.SUCCESS);
        accountFlow.setRemark(PARTNER_APPLY.getRemark());
//        accountFlow.setJobApplyRecord(accountFlow.getJobApplyRecord());
        accountFlow.setPayType(PAY_OFFLINE);
        accountFlow.setPartnerApply(partnerApply);
        accountFlow.setPayOfflineType(PAY_OFFLINE_BANK);//fixme 这里有问题

        accountFlow = accountFlowService.save(accountFlow);
        return accountFlow;
    }


    /**
     * 剩余的可选择列表
     */
    public List<JSONObject> avaCascadeAllRegions() {
        List<Partner> partners = partnerRepository.findAll();
        List<PartnerApply> partnerApplies = partnerApplyService.findByStateIn(newArrayList(IN_REVIEW, REVIEW_SUCCESS, OFFLINE_PAY_CONFIRM, SUCCESS));
        List<Region> chosingProvinces1 = simpleFilterMap(partnerApplies, this::notNullNullNull, PartnerApply::getProvince);//选择中的省
        List<Region> chosingCities1 = simpleFilterMap(partnerApplies, this::notNullNotNullNull, PartnerApply::getCity);//选择中的市
        List<Region> chosingAreas1 = simpleFilterMap(partnerApplies, this::allNotNull, PartnerApply::getArea);//选择中的区
        List<Region> chosingProvinces2 = simpleFilterMap(partnerApplies, this::notNullNullNull, PartnerApply::getProvince2);//选择中的省
        List<Region> chosingCities2 = simpleFilterMap(partnerApplies, this::notNullNotNullNull, PartnerApply::getCity2);//选择中的市
        List<Region> chosingAreas2 = simpleFilterMap(partnerApplies, this::allNotNull, PartnerApply::getArea2);//选择中的区
        List<Region> chosingRegions1 = union(union(chosingProvinces1, chosingCities1), chosingAreas1);
        List<Region> chosingRegions2 = union(union(chosingProvinces2, chosingCities2), chosingAreas2);

        List<Region> chosedProvinces = simpleFilterMap(partners, this::notNullNullNull, Partner::getProvince);//已被选择的省
        List<Region> chosedCities = simpleFilterMap(partners, this::notNullNotNullNull, Partner::getCity);//已被选择的市
        List<Region> chosedAreas = simpleFilterMap(partners, this::allNotNull, Partner::getArea);//已被选择的区
        List<Region> chosedRegions = union(union(chosedProvinces, chosedCities), chosedAreas);

        return utilsService.filterCascadeAllRegions(union(chosingRegions2, union(chosingRegions1, chosedRegions)));
    }

    /**
     * 省不为空,市和区为空
     */
    private Boolean notNullNullNull(PartnerApply partner) {
        return isNotNull(partner.getProvince()) && isNull(partner.getCity()) && isNull(partner.getArea());
    }

    /**
     * 省和市不为空,区为空
     */
    private Boolean notNullNotNullNull(PartnerApply partner) {
        return isNotNull(partner.getProvince()) && isNotNull(partner.getCity()) && isNull(partner.getArea());
    }

    /**
     * 省市区不为空
     */
    private Boolean allNotNull(PartnerApply partner) {
        return isNotNull(partner.getProvince()) && isNotNull(partner.getCity()) && isNotNull(partner.getArea());
    }

    /**
     * 省不为空,市和区为空
     */
    private Boolean notNullNullNull(Partner partner) {
        return isNotNull(partner.getProvince()) && isNull(partner.getCity()) && isNull(partner.getArea());
    }

    /**
     * 省和市不为空,区为空
     */
    private Boolean notNullNotNullNull(Partner partner) {
        return isNotNull(partner.getProvince()) && isNotNull(partner.getCity()) && isNull(partner.getArea());
    }

    /**
     * 省市区不为空
     */
    private Boolean allNotNull(Partner partner) {
        return isNotNull(partner.getProvince()) && isNotNull(partner.getCity()) && isNotNull(partner.getArea());
    }


    public Partner findPartner(Region area) {
        return partnerRepository.findByArea(area);
    }

    //以下方法为管理者端

    public Page<PartnerApply> partnerApply(Pageable pageable, String text, String tableType,
                                           Long province, Long city, Long area, String auditStatus, String payStatus) {
        Specification<PartnerApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("name"), "%" + t + "%"), rb.like(rt.get("member").get("realName"), "%" + t + "%"), rb.like(rt.get("member").get("username"), "%" + t + "%"))));
            ifNotNullThen(province, t -> predicates.add(rb.equal(rt.get("province").get("id"), t)));
            ifNotNullThen(city, t -> predicates.add(rb.equal(rt.get("city").get("id"), t)));
            ifNotNullThen(area, t -> predicates.add(rb.equal(rt.get("area").get("id"), t)));
            if (tableType.equals("no_review"))
                predicates.add(rb.equal(rt.get("state"), IN_REVIEW));
            else if (tableType.equals("has_review")) {
                predicates.add(rt.get("state").in(applyList_));
                ifNotBlankThen(auditStatus, _auditStatus -> {
                    if (_auditStatus.equals("failed")) {
                        predicates.add(rb.equal(rt.get("state"), REVIEW_FAILED));
                    } else if (_auditStatus.equals("success")) {
                        predicates.add(rt.get("state").in(applyList));
                    }
                });
                ifNotBlankThen(payStatus, _payStatus -> {
                    if (_payStatus.equals("no_pay")) {
                        predicates.add(rb.equal(rt.get("state"), REVIEW_SUCCESS));
                    } else if (_payStatus.equals("has_pay")) {
                        predicates.add(rb.equal(rt.get("state"), ApplyEnum.SUCCESS));
                    } else if (_payStatus.equals("line_paying")) {
                        predicates.add(rb.equal(rt.get("state"), OFFLINE_PAY_CONFIRM));
                    }
                });
            }
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return partnerApplyService.findAll(specification, pageable);

    }

    /**
     * 合伙人管理推荐上班员工列表
     */
    public List<JobApplyRecord> queryWorkers(String userToken, Long areaId, ModuleKey.WorkerQueryType type, Pageable pageable) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(findByMemberIdAndAreaId(member.getId(), areaId), TIP_PARTNER_WRONG_AREA);
        ModuleKey.RecruitmentType recruitmentType = null;
        ModuleKey.RecState recState = null;
        boolean flag = true;
        if (type == ONCE_AWARD_LESS_THAN_ONE_MONTH) {
            recruitmentType = ONCE;
            recState = WORKING;
            flag = false;
        } else if (type == ONCE_AWARD_MORE_THAN_ONE_MONTH) {
            recruitmentType = ONCE;
            recState = WORKING;
        } else if (type == MONTHLY_AWARD_LESS_THAN_ONE_MONTH) {
            recruitmentType = MONTHLY;
            recState = WORKING;
            flag = false;
        } else if (type == MONTHLY_AWARD_MORE_THAN_ONE_MONTH) {
            recruitmentType = MONTHLY;
            recState = WORKING;
        } else if (type == ONCE_AWARD_RESIGN) {
            recruitmentType = ONCE;
            recState = ALREADY_RESIGN;
        } else if (type == ONCE_AWARD_WORK_FAILED) {
            recruitmentType = ONCE;
            recState = WORK_FAIL;
            flag = false;
        } else if (type == MONTHLY_AWARD_RESIGN) {
            recruitmentType = MONTHLY;
            recState = ALREADY_RESIGN;
        } else {
            recruitmentType = MONTHLY;
            recState = WORK_FAIL;
            flag = false;
        }
        List<JobApplyRecord> list = jobApplyRecordService.getInJobWorkerPage(recruitmentType, recState, areaId, pageable);
        return isMoreThanMonth(list, flag);
//            return jobApplyRecordService.getInJobWorkerPage(ONCE, WORKING, areaId, false, pageable);
//
//        else if (type == ONCE_AWARD_MORE_THAN_ONE_MONTH)
//            return jobApplyRecordService.getInJobWorkerPage(ONCE, WORKING, areaId, true, pageable);
//        else if (type == ONCE_AWARD_RESIGN)
//            return jobApplyRecordService.getQuitWorkerPage(ONCE, ALREADY_RESIGN, areaId, pageable);
//        else if (type == ONCE_AWARD_WORK_FAILED)
//            return jobApplyRecordService.getQuitWorkerPage(ONCE, WORK_FAIL, areaId, pageable);

//        else if (type == MONTHLY_AWARD_LESS_THAN_ONE_MONTH)
//            return jobApplyRecordService.getInJobWorkerPage(MONTHLY, WORKING, areaId, false, pageable);
//        else if (type == MONTHLY_AWARD_MORE_THAN_ONE_MONTH)
//            return jobApplyRecordService.getInJobWorkerPage(MONTHLY, WORKING, areaId, true, pageable);
//        else if (type == MONTHLY_AWARD_RESIGN)
//            return jobApplyRecordService.getQuitWorkerPage(MONTHLY, ALREADY_RESIGN, areaId, pageable);
//        else
//            return jobApplyRecordService.getQuitWorkerPage(MONTHLY, WORK_FAIL, areaId, pageable);

    }


    private List<JobApplyRecord> isMoreThanMonth(List<JobApplyRecord> jobApplyRecords, boolean flag) {
        List<JobApplyRecord> moreThanMonth = newArrayList();
        List<JobApplyRecord> lessThanMonth = newArrayList();
        for (JobApplyRecord jobApplyRecord : jobApplyRecords) {
            long time = jobApplyRecord.getStartDate().getTime() + (3600 * 1000 * 24 * 30L);
            long now = new Date().getTime();
            if (now > time) {
                moreThanMonth.add(jobApplyRecord);
            } else {
                lessThanMonth.add(jobApplyRecord);
            }
        }
        if (flag) {
            return moreThanMonth;
        }
        return lessThanMonth;
    }

    public PartnerApply info(Long id) {
        PartnerApply partnerApply = partnerApplyService.findOne(id);
        ifNullThrow(partnerApply, TIP_NO_CHANSHUERROR);
        return partnerApply;
    }

    public PartnerApply audit(Long id, String auditStatus, String reason) {
        PartnerApply partnerApply = partnerApplyService.findOne(id);
        partnerApply.setState(auditStatus.equals("YES") ? REVIEW_SUCCESS : REVIEW_FAILED);
        partnerApply.setNotes(reason);
        return partnerApplyService.save(partnerApply);
    }


    /**
     * 合伙区域店铺
     */
    public Page<Shop> shopList(String userToken, Pageable pageable, Long areaId) {
        List<Partner> partners = partnerRepository.findByMemberTokenOrderByDate(userToken);
        ifTrueThrow(partners.isEmpty(), TIP_NO_PARTNER_APPLY);
//        List<Region> province = new ArrayList<>();
//        List<Region> city = new ArrayList<>();
//        List<Region> area = new ArrayList<>();
//        Partner partner = partners.get(0);
//        Partner partner1 = partners.get(partners.size() - 1);//如果两个区域
//        province.add(partner.getProvince());
//        province.add(partner1.getProvince());
//        city.add(partner.getCity());
//        city.add(partner1.getCity());
//        area.add(partner.getArea());
//        area.add(partner1.getArea());
        Page<Shop> shops = shopService.findByAreaIdAndStateOrderByDateDesc(areaId, YES, pageable);
//        Page<Shop> shops = shopRepository.findByProvinceInAndCityInAndAreaIn(province, city, area, pageable);
        return shops;
    }

    public JobApplyRecord getDetail(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        return jobApplyRecordService.findOne(id);
    }

    public Boolean isPartner(Long memberId) {
        return partnerRepository.countByMemberId(memberId) > 0;
    }

    public Partner findByMemberIdAndAreaId(Long userId, Long areaId) {
        return partnerRepository.findByMemberIdAndAreaId(userId, areaId);
    }
}
