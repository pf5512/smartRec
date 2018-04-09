package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.core.domain.repository.MemberExtInfoRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.CloudFileService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.EarningRepository;
import com.thousandsunny.service.repository.EntrepreneursApplyRepository;
import com.thousandsunny.service.repository.EntrepreneursRepository;
import com.thousandsunny.service.repository.JobApplyRecordRepository;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartyPayAccountService;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.subtractMonth;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.IdentityType.JUNIOR;
import static com.thousandsunny.core.ModuleKey.IdentityType.SENIOR;
import static com.thousandsunny.core.ModuleKey.SubLevelType.*;
import static com.thousandsunny.service.ModuleKey.*;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.service.ModuleKey.EarningType.ENTREPRENEUR_AWARD;
import static com.thousandsunny.service.ModuleKey.EntrepreneursType.*;
import static com.thousandsunny.service.ModuleKey.RecState.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType.ENTREPRENEUR_PAY;
import static com.thousandsunny.service.ModuleKey.WorkerQueryType.*;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.CANCEL;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_IN;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_INCOME;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.ENTREPRENEUR_APPLY;
import static java.util.Objects.isNull;

@Service
public class EntrepreneursService extends BaseService<Entrepreneurs> {
    @Autowired
    private EntrepreneursRepository entrepreneursRepository;
    @Autowired
    private EntrepreneursApplyRepository entrepreneursApplyRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JobApplyRecordRepository jobApplyRecordRepository;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private EarningRepository earningRepository;
    @Autowired
    private CloudFileService fileService;
    @Autowired
    private ThirdPartyPayAccountService payAccountService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MemberExtInfoRepository memberExtInfoRepository;
    @Autowired
    private AccountFlowRepository accountFlowRepository;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private SrAccountApplyRecordService accountApplyService;
    @Autowired
    private MemberService memberService;

    private List<ApplyEnum> applyList_ = newArrayList(REVIEW_SUCCESS, REVIEW_FAILED, OFFLINE_PAY_CONFIRM, SUCCESS);
    private List<ApplyEnum> applyList = newArrayList(REVIEW_SUCCESS, OFFLINE_PAY_CONFIRM, SUCCESS);

    public List<EntrepreneursApply> findApply(String userToken) {
        List<EntrepreneursApply> entrepreneursApplys = entrepreneursApplyRepository.findByMemberTokenOrderByApplyDate(userToken);
        return entrepreneursApplys;
    }


    public Entrepreneurs findEntrepreneurs(String userToken) { //为空抛异常
        Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(userToken);
        ifNullThrow(entrepreneurs, TIP_NO_ENTREPRENEURS);
        return entrepreneurs;
    }


    public Entrepreneurs getEntrepreneurs(String userToken) { //为空不抛异常
        Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(userToken);
        return entrepreneurs;
    }

    /**
     * 提交创业者申请
     */
    public EntrepreneursApply saveApply(String userToken, EntrepreneursApply entrepreneursApply) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        List<EntrepreneursApply> entrepreneursApplyList = entrepreneursApplyRepository.findByMemberTokenAndStateOrderByApplyDate(member.getToken(), IN_REVIEW);
        ifFalseThrow(entrepreneursApplyList.isEmpty(), TIP_HAS_APPLYED);
        List<EntrepreneursApply> entrepreneursApplies = entrepreneursApplyRepository.findByMemberTokenAndStateInOrderByApplyDate(member.getToken(), applyList);
        if (entrepreneursApplies.size() != 0) {
            EntrepreneursApply apply = entrepreneursApplies.get(entrepreneursApplies.size() - 1);
            if (apply.getType() == APPLY_JUNIOR) {
                ifFalseThrow(entrepreneursApply.getType() == APPLY_JUNIOR_TO_SENIOR, TIP_NOT_APPLY);
                AccountFlow accountFlow = accountFlowRepository.findByEntrepreneursApplyIdAndTypeAndSourceAndState(apply.getId(), ModuleKey.ChargeType.PAY_OUT, ENTREPRENEUR_APPLY, ModuleKey.FlowState.SUCCESS);
                if (isNotNull(accountFlow)) {
                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);//设置起时间
                    cal.add(Calendar.MONTH, -1);//减一个月
                    ifTrueThrow(accountFlow.getCreateDate().compareTo(cal.getTime()) == -1, TIP_TIME_OUT);//超过一个月不能申请
                }
            }
            ifTrueThrow(apply.getType() == APPLY_JUNIOR_TO_SENIOR || apply.getType() == APPLY_SENIOR, TIP_HAS_APPLY);
        }
        ifNotNullThen(entrepreneursApply.getIdCardFront(),
                f -> entrepreneursApply.setIdCardFront(fileService.save(f)));

        ifNotNullThen(entrepreneursApply.getHalf(),
                f -> entrepreneursApply.setHalf(fileService.save(f)));

        entrepreneursApply.setMember(member);
        ifFalseThrow(identityCheck(entrepreneursApply), TIP_ERROR_IDENTITY);
        entrepreneursApplyRepository.save(entrepreneursApply);
        return entrepreneursApply;
    }

    private Boolean identityCheck(EntrepreneursApply entrepreneursApply) {
        MemberExtInfo memberExtInfo = memberExtInfoRepository.findByMemberToken(entrepreneursApply.getMember().getToken());
        ifNullThrow(memberExtInfo, TIP_NO_MEMBEREXTINFO);
        if (entrepreneursApply.getMember().getIdentityHasPass() == NO) {
            ifNotNullThen(entrepreneursApply.getName(), memberExtInfo.getMember()::setRealName);
            ifNotNullThen(entrepreneursApply.getMobile(), memberExtInfo.getMember()::setMobile);
            ifNotNullThen(entrepreneursApply.getIdCardNo(), memberExtInfo::setIdCardNo);
            ifNotNullThen(entrepreneursApply.getIdCardFront(), memberExtInfo::setIdCard);
            ifNotNullThen(entrepreneursApply.getHalf(), memberExtInfo::setHalf);
        } else {
            if (memberExtInfo.getMember().getRealName() != null) {
                if (!entrepreneursApply.getName().equals(memberExtInfo.getMember().getRealName()))
                    return false;
            } else {
                memberExtInfo.getMember().setRealName(entrepreneursApply.getName());
            }
            if (memberExtInfo.getMember().getMobile() != null) {
                if (!entrepreneursApply.getMobile().equals(memberExtInfo.getMember().getMobile()))
                    return false;
            } else {
                memberExtInfo.getMember().setMobile(entrepreneursApply.getMobile());
            }
            if (memberExtInfo.getIdCardNo() != null) {
                if (!entrepreneursApply.getIdCardNo().equals(memberExtInfo.getIdCardNo()))
                    return false;
            } else {
                memberExtInfo.setIdCardNo(entrepreneursApply.getIdCardNo());
            }
            if (memberExtInfo.getIdCard() != null) {
                if (!entrepreneursApply.getIdCardFront().getPath().equals(memberExtInfo.getIdCard().getPath()))
                    return false;
            } else {
                memberExtInfo.setIdCard(entrepreneursApply.getIdCardFront());
            }
            if (memberExtInfo.getHalf() != null) {
                if (!entrepreneursApply.getHalf().getPath().equals(memberExtInfo.getHalf().getPath()))
                    return false;
            } else {
                memberExtInfo.setHalf(entrepreneursApply.getHalf());
            }
        }
        return true;
    }


    /**
     * 创业者付款
     */
    public Charge payForApply(String userToken, PayType payType, String openId) {
        List<EntrepreneursApply> entrepreneursApplys = entrepreneursApplyRepository.findByMemberTokenOrderByApplyDate(userToken);
        EntrepreneursApply entrepreneursApply = entrepreneursApplys.get(entrepreneursApplys.size() - 1);
        ifNullThrow(entrepreneursApply, TIP_NO_APPLY);
        ifTrueThrow(entrepreneursApply.getState() != REVIEW_SUCCESS, TIP_ERROR_PARTNER_APPLY_STATUS);

        BigDecimal money;
        if (entrepreneursApply.getType() == APPLY_JUNIOR)
            money = new BigDecimal(200);
        else if (entrepreneursApply.getType() == APPLY_SENIOR)
            money = new BigDecimal(10200);
        else
            money = new BigDecimal(10000);
        entrepreneursApply.setJoinMoney(money);

        if (payType == PAY_OFFLINE)
            entrepreneursApply.setState(OFFLINE_PAY_CONFIRM);
        else if (payType == PAY_BY_BALANCE) {
            //线上付款
//            entrepreneursApply.setState(SUCCESS);
            //修改member的创业者身份,如果是余额付款直接改变，支付宝和微信付款在回调方法中修改
            Member member = memberRepository.findByToken(userToken);
            if (entrepreneursApply.getType() == APPLY_JUNIOR)
                member.setEntrepreneurLevel(JUNIOR);
            else
                member.setEntrepreneurLevel(SENIOR);
            memberRepository.save(member);
            createEntrepreneurs(userToken, entrepreneursApply);
            //往系统账户中加钱
            refreshZuesAccount(money);
            //产生一条平台收入流水
            accountFlowService.processPlatform(null, entrepreneursApply.getJoinMoney(), ENTREPRENEUR_APPLY, PLATFORM_IN, null, entrepreneursApply, null, null, PAY_OFFLINE);
            divideMoney(entrepreneursApply, money, false, payType);//分钱
        }
        Charge charge = payAccountService.choosePayType(userToken, payType, money, ENTREPRENEUR_APPLY, null, null, entrepreneursApply, null, null, "创业者付款", openId);
        entrepreneursApplyRepository.save(entrepreneursApply);
//        AccountFlow accountFlow = accountFlowRepository.findTop1ByAccountMemberTokenAndSourceOrderByCreateDate(userToken, ENTREPRENEUR_APPLY);
//        ifNotNullThen(accountFlow, a -> a.setEntrepreneursApply(entrepreneursApply));

        ifTrueThen(payType == PAY_BY_WX || payType == PAY_BY_ALIPAY || payType == PAY_BY_WX_PUB, () -> {//第三方支付
            SrAccountApplyRecord accountApplyRecord = accountApplyService.findByOrderNo(charge.getOrderNo());
            accountApplyRecord.setEntrepreneursApply(entrepreneursApply);
            accountApplyRecord.setAmount(money);
            accountApplyRecord.setSource(ENTREPRENEUR_PAY);
            accountApplyService.save(accountApplyRecord);
        });
        return charge;
    }

    /**
     * 系统账户加钱
     */
    private Account refreshZuesAccount(BigDecimal money) {
        Account account = accountService.findZuesAccount();
        account.setBalance(account.getBalance().add(money));
        account.setTotal(account.getTotal().add(money));
        accountService.save(account);
        return account;
    }

    public void createEntrepreneurs(String userToken, EntrepreneursApply entrepreneursApply) {
        //更新或添加到合伙人
        Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(userToken);
        //ifNotNullThrow(entrepreneurs, TIP_IS_ENTREPRENEURS);
        if (isNotNull(entrepreneurs)) {
            entrepreneurs.setDate(new Date());
        } else {
            entrepreneurs = new Entrepreneurs();
            ifNotNullThen(entrepreneursApply.getMember(), entrepreneurs::setMember);
            ifNotNullThen(entrepreneursApply.getName(), entrepreneurs::setName);
            ifNotNullThen(entrepreneursApply.getMobile(), entrepreneurs::setMobile);
            ifNotNullThen(entrepreneursApply.getIdCardNo(), entrepreneurs::setIdCardNo);

            Account account = accountService.findByMemberToken(userToken);
            ifNullThrow(account, TIP_NO_ACCOUNT);
            entrepreneurs.setAccount(account);
        }
        entrepreneursRepository.save(entrepreneurs);
    }

    /**
     * 创业者收益规则：
     * 初级创业者，200元，收益100w；
     * 高级创业者，10200元，收益200w；
     * 初级可在一个月内升级为高级，过期无效；
     * <p>
     * 初级创业者：其下的好友，如成为初级/高级创业者，付款后，可获得50元/人的提成；
     * <p>
     * 高级创业者：其下的好友，成为初级/高级创业者，前50次，可获得200元/人的提成，收回1w本金后，其下好友无论选择哪种类型，均只能获得50元/人的提成；
     * 上级创业者,平台分钱
     *
     * @Author mu.jie
     * @Date 2016/12/16
     */
    public void divideMoney(EntrepreneursApply entrepreneursApply, BigDecimal money, Boolean flag, PayType payType) {
        MemberExtInfo nowMemberExt = memberExtInfoRepository.findByMemberToken(entrepreneursApply.getMember().getToken());
        final BigDecimal[] entrepreneursMoney = {BigDecimal.ZERO};
        final BigDecimal[] platformMoney = {BigDecimal.ZERO};

        ifNotNullThen(nowMemberExt, x -> ifNotNullThen(x.getRecommendUser(), recommendUser -> {
                    MemberExtInfo superMemberExt = memberExtInfoRepository.findByMemberToken(recommendUser.getToken());
                    Member superMember = superMemberExt.getMember();
                    Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(superMember.getToken());
                    if (entrepreneursApply.getType() != APPLY_JUNIOR_TO_SENIOR) {
                        if (superMember.getEntrepreneurLevel() == JUNIOR && entrepreneurs.getIncome().compareTo(JUNIOR.getIncome()) < 0) {
                            entrepreneursMoney[0] = new BigDecimal(50);
                            x.setJuniorEntrepreneursCount(x.getJuniorEntrepreneursCount() + 1);
                        } else if (superMember.getEntrepreneurLevel() == SENIOR && superMemberExt.getSeniorEntrepreneursCount() <= 50 && entrepreneurs.getIncome().compareTo(SENIOR.getIncome()) < 0) {
                            entrepreneursMoney[0] = new BigDecimal(200);
                            x.setSeniorEntrepreneursCount(x.getSeniorEntrepreneursCount() + 1);
                        } else if (superMember.getEntrepreneurLevel() == SENIOR && superMemberExt.getSeniorEntrepreneursCount() > 50 && entrepreneurs.getIncome().compareTo(SENIOR.getIncome()) < 0) {
                            entrepreneursMoney[0] = new BigDecimal(50);
                            x.setSeniorEntrepreneursCount(x.getSeniorEntrepreneursCount() + 1);
                        }
                    }
                    platformMoney[0] = money.subtract(entrepreneursMoney[0]);
                    x.setEntrepreneursCount(x.getEntrepreneursCount() + 1);
                    Account superAccount = accountService.findByMemberToken(superMember.getToken());
                    ifNotNullThen(entrepreneurs, e -> {
                        //上级创业者分钱
                        accountService.zuesAccountRefundMoney(superAccount, entrepreneursMoney[0]);
                        //上级创业者进账流水
                        accountFlowService.createEntrepreneursAccountFlow(entrepreneursApply, superAccount, entrepreneursMoney[0], payType);
                        e.setIncome(e.getIncome().add(entrepreneursMoney[0]));
                        e.setEntrepreneurRewardIncome(e.getEntrepreneurRewardIncome().add(entrepreneursMoney[0]));
                        entrepreneursRepository.save(e);
                    });
                    //平台收益流水，创业收益
                    accountFlowService.processPlatform(null, platformMoney[0], ModuleKey.SourceType.ENTREPRENEUR_AWARD, PLATFORM_INCOME, null, entrepreneursApply, null, null, payType);
                    memberExtInfoRepository.save(x);
                })
        );

    }


    /**
     * 创业者状态
     */
    public BigDecimal findIncome(String userToken) {
        Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(userToken);
        if (isNull(entrepreneurs)) return new BigDecimal(0);
        else return entrepreneurs.getIncome();
    }


    public Map<String, Integer> entrepreneurCount(String userToken, JSONObject jsonObject) {
        Entrepreneurs superEntrepreneurs = entrepreneursRepository.findByMemberToken(userToken);
        Map<String, Integer> numbers = new HashMap<>();
        int junior = 0;
        int senior = 0;
        Set<Long> ids = memberRegRelService.getMemberIds(userToken, SUB_LEVEL_ONE);
        Set<Member> members = memberRepository.findByIdIn(ids);
        for (Member m : members) {
            Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(m.getToken());
            boolean falg = isNotNull(superEntrepreneurs) && isNotNull(entrepreneurs) && superEntrepreneurs.getDate().getTime() < entrepreneurs.getDate().getTime();
            if (m.getEntrepreneurLevel() == JUNIOR && falg)
                junior++;
            else if (m.getEntrepreneurLevel() == SENIOR && falg)
                senior++;
            else
                continue;
        }
        jsonObject.put("registerSubLevel1EntrepreneurCount", junior + senior);//// 注册1级好友是创业者的数量
        jsonObject.put("registerSubLevel1EntrepreneurJuniorCount", junior);// 注册1级好友是初级创业者的数量
        jsonObject.put("registerSubLevel1EntrepreneurSeniorCount", senior);// 注册1级好友是高级创业者的数量

        return numbers;
    }

    public String enAcount(String userToken, JSONObject jsonObject) {
        Date startDate = subtractMonth(new Date(), 1);
        Set<Long> memberSet = getRecAndRegMemberId(userToken);

        //一次性悬赏上班未满一个月
        long onceRewardLessThanOneMonthPeopleCount = findJobApplyList(ONCE, startDate, "lessOneMonth", memberSet);
        jsonObject.put("onceRewardLessThanOneMonthPeopleCount", onceRewardLessThanOneMonthPeopleCount); // 一次性悬赏上班未满一个月员工数量

        //一次性悬赏上班满一个月
        long onceRewardMoreThanOneMonthPeopleCount = findJobApplyList(ONCE, startDate, "moreOneMonth", memberSet);
        jsonObject.put("onceRewardMoreThanOneMonthPeopleCount", onceRewardMoreThanOneMonthPeopleCount);

        //一次性悬赏离职
        long onceRewardResignedPeopleCount = jobApplyRecordRepository.countByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(memberSet, ONCE, ALREADY_RESIGN);
        jsonObject.put("onceRewardResignedPeopleCount", onceRewardResignedPeopleCount);

        //一次性悬赏上班失败
        long onceRewardFailedPeopleCount = jobApplyRecordRepository.countByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(memberSet, ONCE, WORK_FAIL);
        jsonObject.put("onceRewardFailedPeopleCount", onceRewardFailedPeopleCount);

        //按月悬赏上班未满一个月
        long monthlyRewardLessThanOneMonthPeopleCount = findJobApplyList(MONTHLY, startDate, "lessOneMonth", memberSet);
        jsonObject.put("monthlyRewardLessThanOneMonthPeopleCount", monthlyRewardLessThanOneMonthPeopleCount);

        //按月悬赏上班满一个月
        long monthlyRewardMoreThanOneMonthPeopleCount = findJobApplyList(MONTHLY, startDate, "moreOneMonth", memberSet);
        jsonObject.put("monthlyRewardMoreThanOneMonthPeopleCount", monthlyRewardMoreThanOneMonthPeopleCount);

        //按月悬赏离职
        long monthlyRewardResignedPeopleCount = jobApplyRecordRepository.countByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(memberSet, MONTHLY, ALREADY_RESIGN);
        jsonObject.put("monthlyRewardResignedPeopleCount", monthlyRewardResignedPeopleCount);

        //按月悬赏上班失败
        long monthlyRewardFailedPeopleCount = jobApplyRecordRepository.countByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(memberSet, MONTHLY, WORK_FAIL);
        jsonObject.put("monthlyRewardFailedPeopleCount", monthlyRewardFailedPeopleCount);

        return "success";
    }

    private Set<Long> getRecAndRegMemberId(String userToken) {
        Set<Long> memberSet = new HashSet<>();
        Set<Long> memberRegsOne = memberRegRelService.getMemberIds(userToken, SUB_LEVEL_ONE);//注册一级关系
        if (memberRegsOne.size() > 0) memberSet.addAll(memberRegsOne);
        Set<Long> memberRegsTwo = memberRegRelService.getMemberIds(userToken, SUB_LEVEL_TWO);//注册两级关系
        if (memberRegsTwo.size() > 0) memberSet.addAll(memberRegsTwo);
        Set<Long> memberRecsOne = memberRecRelService.getMemberIds(userToken, SUB_LEVEL_ONE);//推荐一级关系
        if (memberRecsOne.size() > 0) memberSet.addAll(memberRecsOne);
        Set<Long> memberRecsTwo = memberRecRelService.getMemberIds(userToken, SUB_LEVEL_TWO);//推荐二级关系
        if (memberRecsTwo.size() > 0) memberSet.addAll(memberRecsTwo);
        Set<Long> memberRecsThree = memberRecRelService.getMemberIds(userToken, SUB_LEVEL_THREE);//推荐三级关系
        if (memberRecsThree.size() > 0) memberSet.addAll(memberRecsThree);
        for (Long memberReg : memberRegsTwo) {
            Member one = memberService.findOne(memberReg);
            Set<Long> memberRecs = memberRecRelService.getMemberIds(one.getToken(), SUB_LEVEL_TWO);
            if (memberRecs.size() > 0) memberSet.addAll(memberRecs);
        }
        Member member = memberService.findByToken(userToken);
        memberSet.add(member.getId());//自己推荐上班的
        Set<Long> memberRegsThree = memberRegRelService.getMemberIds(userToken, SUB_LEVEL_THREE);
        memberSet.addAll(memberRegsThree);
        memberSet = memberSet.stream().filter(x -> x != null).collect(Collectors.toSet());
        return memberSet;
    }

    private long findJobApplyList(RecruitmentType recType, Date date, String message, Set<Long> memberIds) {
        Specification<JobApplyRecord> specification = (rt, rq, rb) -> {
            List<RecState> states = newArrayList(WORKING, WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN);
            List<Predicate> predicates = newArrayList();
            predicates.add(rt.get("referral").get("id").in(memberIds));
            predicates.add(rb.equal(rt.get("job").get("recType"), recType));
            predicates.add(rt.get("recState").in(states));
            if (message.equals("moreOneMonth"))
                predicates.add(rb.lessThan(rt.get("startDate"), date));
            else if (message.equals("lessOneMonth"))
                predicates.add(rb.greaterThan(rt.get("startDate"), date));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return jobApplyRecordRepository.count(specification);
    }

    //获取List<JobApplyRecord>中被推荐人的ids
    public Set<Long> getSetLong(List<JobApplyRecord> listName) {
        Set<Long> longSet = new HashSet<>();
        for (JobApplyRecord j : listName) {
            longSet.add(j.getReceiver().getId());
        }
        return longSet;

    }

    public Set<Long> intersection(Set<Long> setA, Set<Long> setB) {
        Set<Long> setIntersection = new HashSet<>();

        Iterator<Long> iterA = setA.iterator();
        while (iterA.hasNext()) {
            Long next = iterA.next();
            if (setB.contains(next)) {
                setIntersection.add(next);
            }
        }
        return setIntersection;
    }


    public Page<Earning> reward(String userToken, Pageable pageable) {
        Page<Earning> earningPage = earningRepository.findByEntrepreneursMemberTokenAndEarningType(userToken, ENTREPRENEUR_AWARD, pageable);
        ifNullThrow(earningPage, TIP_NO_CHANSHUERROR);
        return earningPage;
    }


    public Page<Earning> earningsList(String userToken, Pageable pageable) {
        Page<Earning> earningPage = earningRepository.findByEntrepreneursMemberToken(userToken, pageable);
        ifNullThrow(earningPage, TIP_NO_CHANSHUERROR);
        return earningPage;
    }

    public Earning findEarning(String userToken, Long id) {
        Earning earning = earningRepository.findOne(id);
        ifNullThrow(earning, TIP_NO_CHANSHUERROR);
        return earning;
    }

/*
    public Page<JobApplyRecord> jobApplyRecords(String userToken, String type, Pageable pageable) {
        Set<Long> ids = memberRecRelService.allFriendsList(userToken);
        Page<JobApplyRecord> jobApplyRecordPage = jobApplyRecordRepository.findByReceiverIdIn(ids, pageable);
        return jobApplyRecordPage;
    }*/


    public Page<JobApplyRecord> jobApplyRecords(String userToken, WorkerQueryType type, Pageable pageable) {
        Set<Long> ids = getRecAndRegMemberId(userToken);

        List<RecState> recStates = newArrayList(WORKING, WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN);
        Date startDate = subtractMonth(new Date(), 1);
        if (type == ONCE_AWARD_LESS_THAN_ONE_MONTH) {
            //一次性悬赏上班未满一个月
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateInAndStartDateGreaterThanOrderByDateDesc(ids, ONCE, recStates, startDate, pageable);

        } else if (type == ONCE_AWARD_MORE_THAN_ONE_MONTH) {
            //一次性悬赏上班满一个月
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateInAndStartDateLessThanOrderByDateDesc(ids, ONCE, recStates, startDate, pageable);

        } else if (type == ONCE_AWARD_RESIGN) {
            //一次性悬赏离职
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(ids, ONCE, ALREADY_RESIGN, pageable);

        } else if (type == ONCE_AWARD_WORK_FAILED) {
            //一次性悬赏上班失败
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(ids, ONCE, WORK_FAIL, pageable);
        } else if (type == MONTHLY_AWARD_LESS_THAN_ONE_MONTH) {
            //按月悬赏上班未满一个月
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateInAndStartDateGreaterThanOrderByDateDesc(ids, MONTHLY, recStates, startDate, pageable);
        } else if (type == MONTHLY_AWARD_MORE_THAN_ONE_MONTH) {
            //按月悬赏上班满一个月
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateInAndStartDateLessThanOrderByDateDesc(ids, MONTHLY, recStates, startDate, pageable);
        } else if (type == MONTHLY_AWARD_RESIGN) {
            //按月悬赏离职
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(ids, MONTHLY, ALREADY_RESIGN, pageable);
        } else if (type == MONTHLY_AWARD_WORK_FAILED) {
            //按月悬赏上班失败
            return jobApplyRecordRepository.findByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(ids, MONTHLY, WORK_FAIL, pageable);
        }
        return null;

    }


    /**
     * 15.10创业者/合伙人 管理推荐上班员工详情
     */
    public JobApplyRecord getDetail(String userToken, Long id) {
        return jobApplyRecordRepository.findOne(id);

    }

    public void reviewEntrepreneursApply(String userToken, Long id, OperatorType type) {
        EntrepreneursApply entrepreneursApply = entrepreneursApplyRepository.findOne(id);
        ifNullThrow(entrepreneursApply, TIP_NO_APPLY);
        ifTrueThrow(entrepreneursApply.getState() != IN_REVIEW, TIP_CANT_REVIEW);
        ifTrueThen(type == SURE, () -> agreeEp(entrepreneursApply));//同意 付款后才能成为合伙人
        ifTrueThen(type == CANCEL, () -> entrepreneursApply.setState(REVIEW_FAILED));//拒绝
        entrepreneursApplyRepository.save(entrepreneursApply);
    }

    //同意 付款后才能成为合伙人
    private void agreeEp(EntrepreneursApply entrepreneursApply) {
        Member entrepreneur = entrepreneursApply.getMember();
        entrepreneur.setIdentityHasPass(YES);
        memberService.save(entrepreneur);

        entrepreneursApply.setState(REVIEW_SUCCESS);
        Entrepreneurs partner = new Entrepreneurs();
        copyProperties(partner, entrepreneursApply);
        partner.setId(null);

        Account account = accountService.findByMemberMobile(entrepreneursApply.getMobile());
        partner.setAccount(account);
        save(partner);
    }

    public String confirm(String userToken, OperatorType type) {
        EntrepreneursApply entrepreneursApply = entrepreneursApplyRepository.findByMemberTokenAndState(userToken, OFFLINE_PAY_CONFIRM);
        ifNullThrow(entrepreneursApply, TIP_NO_APPLY_RECORD);

        // ifNotNullThrow(entrepreneurs, TIP_IS_ENTREPRENEURS);

        if (type == SURE) {
            confirmEntrepreneursApply(entrepreneursApply);
        } else
            entrepreneursApply.setState(REVIEW_SUCCESS);
        entrepreneursApplyRepository.save(entrepreneursApply);
        return "success";
    }

    public void confirmEntrepreneursApply(EntrepreneursApply entrepreneursApply) {
        String userToken = entrepreneursApply.getMember().getToken();
        Entrepreneurs entrepreneurs = entrepreneursRepository.findByMemberToken(userToken);
        entrepreneursApply.setReviewDate(new Date());
        entrepreneursApply.setState(SUCCESS);
        if (isNull(entrepreneurs)) {
            entrepreneurs = new Entrepreneurs();
            ifNotNullThen(entrepreneursApply.getMember(), entrepreneurs::setMember);
            ifNotNullThen(entrepreneursApply.getName(), entrepreneurs::setName);
            ifNotNullThen(entrepreneursApply.getMobile(), entrepreneurs::setMobile);
            ifNotNullThen(entrepreneursApply.getIdCardNo(), entrepreneurs::setIdCardNo);
        } else {
            entrepreneurs.setDate(new Date());
        }

        Account account = accountService.findByMemberToken(userToken);
        ifNullThrow(account, TIP_NO_ACCOUNT);
        entrepreneurs.setAccount(account);
        entrepreneurs.setDate(new Date());
        entrepreneursRepository.save(entrepreneurs);
        Member member = memberRepository.findByToken(userToken);
        if (entrepreneursApply.getType() == APPLY_JUNIOR)
            member.setEntrepreneurLevel(JUNIOR);
        else
            member.setEntrepreneurLevel(SENIOR);
        memberRepository.save(member);
        //往系统账户中加钱
        refreshZuesAccount(entrepreneursApply.getJoinMoney());
        //产生一条平台收入流水
        accountFlowService.processPlatform(null, entrepreneursApply.getJoinMoney(), ENTREPRENEUR_APPLY, PLATFORM_IN, null, entrepreneursApply, null, null, PAY_OFFLINE);
        //平台收益，创业收益
        divideMoney(entrepreneursApply, entrepreneursApply.getJoinMoney(), true, PAY_OFFLINE);
    }


    //以下方法为管理者端

    public Page<EntrepreneursApply> listEntrepreneursApply(Pageable pageable, String text, String tableType, Long province,
                                                           Long city, Long area, EntrepreneursType ESTPType, String auditStatus, String payStatus) {
        Specification<EntrepreneursApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("name"), "%" + t + "%"), rb.like(rt.get("member").get("realName"), "%" + t + "%"), rb.like(rt.get("member").get("username"), "%" + t + "%"))));
            ifNotNullThen(province, t -> predicates.add(rb.equal(rt.get("memberExtInfo").get("province").get("id"), t)));
            ifNotNullThen(city, t -> predicates.add(rb.equal(rt.get("memberExtInfo").get("city").get("id"), t)));
            ifNotNullThen(area, t -> predicates.add(rb.equal(rt.get("memberExtInfo").get("area").get("id"), t)));
            ifNotNullThen(ESTPType, t -> predicates.add(rb.equal(rt.get("type"), t)));
            if (tableType.equals("no_review"))
                predicates.add(rb.equal(rt.get("state"), IN_REVIEW));
            else if (tableType.equals("has_review")) {
                predicates.add(rt.get("state").in(applyList_));
                ifNotBlankThen(auditStatus, x -> {
                    if (x.equals("failed")) {
                        predicates.add(rb.equal(rt.get("state"), REVIEW_FAILED));
                    } else if (x.equals("success")) {
                        predicates.add(rt.get("state").in(applyList));
                        ifNotBlankThen(payStatus, y -> {
                            if (y.equals("no_pay")) {
                                predicates.add(rb.equal(rt.get("state"), REVIEW_SUCCESS));
                            } else if (y.equals("has_pay")) {
                                predicates.add(rb.equal(rt.get("state"), SUCCESS));
                            } else if (y.equals("line_paying")) {
                                predicates.add(rb.equal(rt.get("state"), OFFLINE_PAY_CONFIRM));
                            }
                        });
                    }
                });
            }
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("applyDate"), false)).getRestriction();
        };
        return entrepreneursApplyRepository.findAll(specification, pageable);
    }

    public EntrepreneursApply audit(Long id, String auditStatus, String reason) {
        EntrepreneursApply entrepreneursApply = entrepreneursApplyRepository.findOne(id);
        entrepreneursApply.setState(auditStatus.equals("YES") ? REVIEW_SUCCESS : REVIEW_FAILED);
        if ("YES".equals(auditStatus)) {
            entrepreneursApply.getMember().setIdentityHasPass(YES);
        }
        entrepreneursApply.setNotes(reason);
        return entrepreneursApplyRepository.save(entrepreneursApply);
    }

    public EntrepreneursApply info(Long id) {
        EntrepreneursApply entrepreneursApply = entrepreneursApplyRepository.findOne(id);
        ifNullThrow(entrepreneursApply, TIP_NO_CHANSHUERROR);
        return entrepreneursApply;
    }

    public Boolean isEp(Long id) {
        return entrepreneursRepository.countByMemberId(id) > 0;
    }

    public List<EntrepreneursApply> findApplyList(String userToken) {
        return entrepreneursApplyRepository.findByMemberTokenAndStateInOrderByApplyDate(userToken, applyList);
    }

    public JSONObject countEntrepreneursInfo(Date startTime, Date endTime) {
        JSONObject jo = new JSONObject();
        Specification<EntrepreneursApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("applyDate"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("applyDate"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<EntrepreneursApply> list = entrepreneursApplyRepository.findAll(specification);
        Set<Long> reviewedMemberIds = new HashSet<>();
        Set<Long> reviewSuccessMemberIds = new HashSet<>();
        if (!list.isEmpty()) {
            list.forEach(e -> {
                reviewedMemberIds.add(e.getMember().getId());
                if (e.getState() == REVIEW_SUCCESS || e.getState() == OFFLINE_PAY_CONFIRM || e.getState() == SUCCESS)
                    reviewSuccessMemberIds.add(e.getMember().getId());
            });
            jo.put("applyUserNum", reviewedMemberIds.size());
            jo.put("audited", reviewSuccessMemberIds.size());
        } else {
            jo.put("applyUserNum", 0);
            jo.put("audited", 0);
        }
        Specification<Entrepreneurs> specification1 = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        Long entrepreneursNum = entrepreneursRepository.count(specification1);
        jo.put("isCyz", entrepreneursNum);
        return jo;
    }

    public Long countApplyingEntre(Date startTime, Date endTime) {
        Specification<EntrepreneursApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("state"), IN_REVIEW));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return entrepreneursApplyRepository.count(specification);
    }
}
