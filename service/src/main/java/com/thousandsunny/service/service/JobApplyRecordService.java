package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.DateUtil;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.ModuleTip;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
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
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BooleanSupplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.FRIEND_WORK_REMIND;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.WORK_STATE_CONFIRM_REMIND;
import static com.thousandsunny.core.ModuleKey.RemindMsgType.WORK_STATE_CONFIRM_REMIND_TO_EMPLOYEE;
import static com.thousandsunny.core.ModuleKey.RemindMsgType.WORK_STATE_CONFIRM_REMIND_TO_STORE;
import static com.thousandsunny.core.ModuleKey.SubLevelType.*;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.IN_REVIEW;
import static com.thousandsunny.service.ModuleKey.ApplyType.JOB_RESIGN;
import static com.thousandsunny.service.ModuleKey.BenefitType.CAR_FEE;
import static com.thousandsunny.service.ModuleKey.BenefitType.WORK_INSURANCE;
import static com.thousandsunny.service.ModuleKey.KeyPercentage.CONSTANT_CAR_FEE;
import static com.thousandsunny.service.ModuleKey.*;
import static com.thousandsunny.service.ModuleKey.RecState.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.FROZEN;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RefundEnum.HAS_NOT_REFUNDED;
import static com.thousandsunny.service.ModuleKey.ResignEnum.QUIT;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_BY_BALANCE;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_PAY_ONLINE;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_MEMBER_ACCOUNT_NOT_EXIST;
import static java.lang.Boolean.FALSE;
import static java.util.Calendar.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addMonths;

/**
 * 如果这些代码有用，那它们是guitarist在15/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class JobApplyRecordService extends BaseService<JobApplyRecord> {
    @Autowired
    private JobApplyRecordRepository jobApplyRecordRepository;
    @Autowired
    private BenefitRelService benefitRelService;
    @Autowired
    private AutomaticRenewalsRepository automaticRenewalsRepository;
    @Autowired
    private JobService jobService;
    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private HpApplyRepository hpApplyRepository;
    @Autowired
    private MemberMsgService memberMsgService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private RenewalsRecordService renewalsRecordService;
    @Autowired
    private AutomaticRenewalsService automaticRenewalsService;
    @Autowired
    private BenefitRelRepository benefitRelRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RenewalsRecordRepository renewalsRecordRepository;

    private List<RecState> states = newArrayList(ALREADY_RESIGN, WORK_FAIL);

    public JobApplyRecord findByReferralTokenAndReceiverTokenAndJobId(String userToken, String recommendUserToken, Long jobId) {
        return jobApplyRecordRepository.findByReferralTokenAndReceiverTokenAndJobId(userToken, recommendUserToken, jobId);
    }

    /**
     * 我推荐别人的岗位列表
     */
    public Page<JobApplyRecord> recTrace(String userToken, Pageable pageable) {
        return jobApplyRecordRepository.findByReferralTokenOrderByDateDesc(userToken, pageable);
    }

    /**
     * 解除岗位推荐
     */
    public void delJobRecRecord(String receiver, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecord(id);
        ifTrueThrow(jobApplyRecord.getRecState() != NOT_WORK && jobApplyRecord.getRecState() != ALREADY_RESIGN
                && jobApplyRecord.getRecState() != WORK_FAIL, TIP_HAS_WORKING);
        jobApplyRecordRepository.delete(jobApplyRecord);
    }

    /**
     * 岗位推荐员工发起上班,同时生成自动续费记录
     */
    public void requestWorking(String receiver, Long id, Date date) {
        List<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByReceiverToken(receiver);
        for (JobApplyRecord j : jobApplyRecords) {
            ifTrueThrow(j.getRecState() == WORKING || j.getRecState() == WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN, TIP_NO_JOBRECORD);
        }
        JobApplyRecord jobApplyRecord = jobApplyRecord(id);
        RecState recState = jobApplyRecord.getRecState();
        ifTrueThrow(recState != NOT_WORK
                && recState != STORE_RETURN
                && recState != ALREADY_RESIGN
                && recState != WORK_FAIL, TIP_CAN_NOT_RAISE_REQUEST);
        ifTrueThrow(jobApplyRecord.getJob().getEpmCount() == 0, TIP_CAN_NOT_RAISE_REQUEST);

        jobApplyRecord.setRecState(WAIT_FOR_STORE_CONFIRM_WORK);
        jobApplyRecord.setStartDate(date);
        jobApplyRecordRepository.save(jobApplyRecord);
        sendWorkMessage(jobApplyRecord);
    }

    /**
     * 查询是否可以绑定岗位推荐关系
     */
    public Member jobRecRecordState(String receiver, String Referral, Long id) {
        JobApplyRecord record = jobApplyRecordRepository.findByJobIdAndReferralTokenAndReceiverToken(id, Referral, receiver);
        return isNull(record) ? null : record.getReferral();
    }

    public Member jobRecRecordState(Member receiver, Long id) {
        JobApplyRecord record = jobApplyRecordRepository.findByReceiverIdAndJobId(receiver.getId(), id);
        return isNull(record) ? null : record.getReferral();
    }

    public String undo(String userToken, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findByIdAndShopOwnerTokenAndRecState(id, userToken, WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN);
        ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
        jobApplyRecord.setRecState(WORKING);
        jobApplyRecordRepository.save(jobApplyRecord);
        return "success";
    }

    /**
     * 岗位推荐店铺确认上班
     * 确认上班
     * >>变动账户信息
     * >>>>店家账户扣钱
     * >>>>系统账户加钱
     * <p>
     * >>产生流水
     * <p>
     * >>产生自动续费设置
     * <p>
     * >>产生续费信息
     * <p>
     * >>保存好处记录
     */
    public void confirm(String userToken, Long recordId, OperatorType operatorType) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findByShopOwnerTokenAndId(userToken, recordId);

        ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
        ifFalseThrow(jobApplyRecord.getRecState() == WAIT_FOR_STORE_CONFIRM_WORK, TIP_ABNORMAL_STATUS);

        if (operatorType == SURE) {
            jobApplyRecord.setRecState(WORKING);

            jobApplyRecordRepository.save(jobApplyRecord);

            sendFriendMessage(jobApplyRecord, "work"); // 给上班者的上级发送好友上班提醒

            memberRecRelService.buildOneRecord(recordId);

            Account memberAccount = accountService.findByMemberToken(jobApplyRecord.getReceiver().getToken());

            AutomaticRenewals automaticRenewals = createAutomaticRenewals(jobApplyRecord);//保存自动续费设置

            BigDecimal reward = jobApplyRecord.getJob().getReward();//需要交的费用

//            accountService.memberAccountPayMoney(memberAccount, reward);//变动账户信息

            AccountFlow accountFlow = saveAccountFlow(jobApplyRecord, memberAccount, reward, null);//产生流水,这里本不应该产生流水，所以将source设为null

            ifNotNullThen(automaticRenewals, this::refreshAutoRenewalRecord);//刷新自动续费记录

            saveRenewalRecord(automaticRenewals, accountFlow);//保存续费记录

            benefitRelService.findByMemberToken(jobApplyRecord.getReceiver().getToken())
                    .forEach(r -> benefitRelService.refreshBenefitRel(r, accountFlow, jobApplyRecord));//刷新好处记录
            //将job中的epmCount-1，workerCount+1；如果epmCount==0，将state变成STOP状态
            jobService.changeJobState(jobApplyRecord);

        } else {
            jobApplyRecord.setRecState(STORE_RETURN);
        }
        jobApplyRecordRepository.save(jobApplyRecord);
    }


    /**
     * 刷新自动续费记录
     */
    public void refreshAutoRenewalRecord(AutomaticRenewals automaticRenewals) {
        automaticRenewals.setNextTime(addMonths(automaticRenewals.getStartDate(), 1));
        automaticRenewals.setStartDate(addMonths(automaticRenewals.getStartDate(), 1));
        automaticRenewals.setFinalDate(addMonths(automaticRenewals.getFinalDate(), 1));
        automaticRenewals.setTimes(automaticRenewals.getTimes() + 1);
        automaticRenewals.setBreach(new BigDecimal(0));
        automaticRenewalsService.save(automaticRenewals);
    }


    /**
     * 保存续费记录
     */
    private void saveRenewalRecord(AutomaticRenewals automaticRenewals, AccountFlow accountFlow) {
        Boolean carFeeIsValid = isNotNull(benefitRelService.carFeeIsValid(accountFlow.getJobApplyRecord().getReceiver().getToken()));//这一次是否需要支付车旅费用

        RenewalsRecord renewalsRecord = new RenewalsRecord();
        renewalsRecord.setDate(accountFlow.getJobApplyRecord().getStartDate());
        renewalsRecord.setAccountFlow(accountFlow);
        renewalsRecord.setRenewType(RenewType.SUCCESS);
        renewalsRecord.setAssigned(FALSE);

        BigDecimal amount = accountFlow.getAmount();
        renewalsRecord.setFee(carFeeIsValid ? amount.subtract(CONSTANT_CAR_FEE.val()) : amount);//可以被分配的资金(需要支付则减100)
        renewalsRecord.setRenewals(automaticRenewals);
        renewalsRecord.setJob(accountFlow.getJobApplyRecord().getJob());
        renewalsRecordService.save(renewalsRecord);
    }

    /**
     * 保存业务流水
     */
    AccountFlow saveAccountFlow(JobApplyRecord jobApplyRecord, Account memberAccount, BigDecimal payAmount, ModuleKey.SourceType sourceType) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(memberAccount);
        accountFlow.setAmount(payAmount);
        accountFlow.setJob(jobApplyRecord.getJob());
        accountFlow.setType(PAY_OUT);
        accountFlow.setPayType(PAY_BY_BALANCE);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRecordType(BILL_PAY_ONLINE);
        if (isNotNull(sourceType)) {
            accountFlow.setSource(sourceType);
            accountFlow.setRemark(sourceType.getRemark());
        }
        accountFlow.setState(SUCCESS);
        accountFlow.setJobApplyRecord(jobApplyRecord);
        accountFlow = accountFlowService.save(accountFlow);
        return accountFlow;
    }

    /**
     * 保存自动续费记录
     */
    private AutomaticRenewals createAutomaticRenewals(JobApplyRecord jobApplyRecord) {
        AutomaticRenewals automaticRenewals = null;
        if (jobApplyRecord.getJob().getRecType() == MONTHLY) {
            Account ownerAccount = accountService.findByMemberToken(jobApplyRecord.getShop().getOwner().getToken());

            automaticRenewals = automaticRenewalsRepository.findByJobIdAndWorkerIdAndIsDelete(jobApplyRecord.getJob().getId(), jobApplyRecord.getReceiver().getId(), NO);
            if (isNull(automaticRenewals)) automaticRenewals = new AutomaticRenewals();
            automaticRenewals.setAccount(ownerAccount);//店家账号
            automaticRenewals.setWorker(jobApplyRecord.getReceiver());
            automaticRenewals.setJob(jobApplyRecord.getJob());
            automaticRenewals.setStartDate(jobApplyRecord.getStartDate());
            automaticRenewals.setAuto(jobApplyRecord.getJob().getIsAuto());
            automaticRenewals.setAvaDate(jobApplyRecord.getStartDate());
            automaticRenewals.setFinalDate(addDays(automaticRenewals.getStartDate(), 6));
            automaticRenewals.setNextTime(addMonths(jobApplyRecord.getStartDate(), 1));
            automaticRenewals.setFee(jobApplyRecord.getJob().getReward());
            automaticRenewals = automaticRenewalsRepository.save(automaticRenewals);
        }
        return automaticRenewals;
    }

    /**
     * 岗位推荐待确认上班列表
     */
    public Page<JobApplyRecord> listRecord(String userToken, Pageable pageable) {
        return jobApplyRecordRepository.findByShopOwnerTokenAndRecState(userToken, WAIT_FOR_STORE_CONFIRM_WORK, pageable);
    }

    /**
     * 岗位推荐在职员工发起离职
     */
    public String startResign(String userToken, Long id, String date, ResignEnum type, String remark) {
        JobApplyRecord jobApplyRecord = getJobApplyRecord(userToken, id);
        ifFalseThrow(jobApplyRecord.getRecState() == WORKING, TIP_ABNORMAL_STATUS);
        jobApplyRecord.setRecState(WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN);
        try {
            jobApplyRecord.setResignDate(ISO_DATE_FORMAT.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        jobApplyRecord.setResignType(type);
        jobApplyRecord.setResignRemark(remark);
        jobApplyRecordRepository.save(jobApplyRecord);
        sendResignMessage(jobApplyRecord);
        return "success";
    }

    private void sendResignMessage(JobApplyRecord jobApplyRecord) {
        memberMsgService.updateMemberMsgIsNew(jobApplyRecord.getReceiver(), WORK_STATE_CONFIRM_REMIND);
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setJob(jobApplyRecord.getJob());
        memberMsg.setReceiver(jobApplyRecord.getReceiver());
        memberMsg.setType(WORK_STATE_CONFIRM_REMIND);
        memberMsg.setRemindType(WORK_STATE_CONFIRM_REMIND_TO_EMPLOYEE);
        memberMsg.setJobApplyRecord(jobApplyRecord);
        memberMsg.setContent(jobApplyRecord.getShop().getName() + " 请你来确认工作状态。");
        memberMsgService.save(memberMsg);
    }

    private void sendWorkMessage(JobApplyRecord jobApplyRecord) {
        memberMsgService.updateMemberMsgIsNew(jobApplyRecord.getShop().getOwner(), WORK_STATE_CONFIRM_REMIND);
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setJob(jobApplyRecord.getJob());
        memberMsg.setReceiver(jobApplyRecord.getShop().getOwner());
        memberMsg.setType(WORK_STATE_CONFIRM_REMIND);
        memberMsg.setRemindType(WORK_STATE_CONFIRM_REMIND_TO_STORE);
        memberMsg.setJobApplyRecord(jobApplyRecord);
        memberMsg.setContent(jobApplyRecord.getReceiver().getRealName() + "发起“确认上班”啦，请你来确认！");
        memberMsgService.save(memberMsg);
    }

    /**
     * 店家确认上班时向上班者的所有上级发送上班提醒消息
     *
     * @Author xiao xue wei
     * @Date 2017/1/3
     */
    public void sendFriendMessage(JobApplyRecord jobApplyRecord, String type) {
        Set<Member> recThreeMembers = memberRepository.findByIdIn(memberRecRelService.findChiefFriends(jobApplyRecord.getReceiver().getId(), SUB_LEVEL_THREE));
        Set<Member> recTwoMembers = memberRepository.findByIdIn(memberRecRelService.findChiefFriends(jobApplyRecord.getReceiver().getId(), SUB_LEVEL_TWO));
        Set<Member> recOneMembers = memberRepository.findByIdIn(memberRecRelService.findChiefFriends(jobApplyRecord.getReceiver().getId(), SUB_LEVEL_ONE));
        Set<Member> regThreeMembers = memberRepository.findByIdIn(memberRegRelService.findChiefFriends(jobApplyRecord.getReceiver().getId(), SUB_LEVEL_THREE));
        Set<Member> regTwoMembers = memberRepository.findByIdIn(memberRegRelService.findChiefFriends(jobApplyRecord.getReceiver().getId(), SUB_LEVEL_TWO));
        Set<Member> regOneMembers = memberRepository.findByIdIn(memberRegRelService.findChiefFriends(jobApplyRecord.getReceiver().getId(), SUB_LEVEL_ONE));
        sendFriendMessageTo(recOneMembers, jobApplyRecord, "推荐朋友", type);
        sendFriendMessageTo(recTwoMembers, jobApplyRecord, "推荐熟人", type);
        sendFriendMessageTo(recThreeMembers, jobApplyRecord, "推荐人脉", type);
        sendFriendMessageTo(regOneMembers, jobApplyRecord, "注册朋友", type);
        sendFriendMessageTo(regTwoMembers, jobApplyRecord, "注册熟人", type);
        sendFriendMessageTo(regThreeMembers, jobApplyRecord, "注册人脉", type);
    }

    public void sendFriendMessageTo(Set<Member> members, JobApplyRecord jobApplyRecord, String msg, String type) {
        if (!members.isEmpty()) {
            members.forEach(e -> {
                memberMsgService.updateMemberMsgIsNew(e, FRIEND_WORK_REMIND);
                MemberMsg memberMsg = new MemberMsg();
                memberMsg.setJob(jobApplyRecord.getJob());
                memberMsg.setReceiver(e);
                memberMsg.setType(FRIEND_WORK_REMIND);
                memberMsg.setRemindType(null);
                memberMsg.setJobApplyRecord(jobApplyRecord);
                if ("work".equals(type)) {
                    if (isNotNull(jobApplyRecord.getReceiver().getRealName()))
                        memberMsg.setContent("你的" + msg + " " + jobApplyRecord.getReceiver().getRealName() + " 去上班了！");
                    else memberMsg.setContent("你的" + msg + " " + jobApplyRecord.getReceiver().getMobile() + " 去上班了！");
                } else if ("leave".equals(type)) {
                    if (isNotNull(jobApplyRecord.getReceiver().getRealName()))
                        memberMsg.setContent("你的" + msg + " " + jobApplyRecord.getReceiver().getRealName() + " 去离职了！");
                    else memberMsg.setContent("你的" + msg + " " + jobApplyRecord.getReceiver().getMobile() + " 去离职了！");
                }
                memberMsgService.save(memberMsg);
            });
        }
    }

    /**
     * 别人推荐我的岗位列表
     */
    public Page<JobApplyRecord> recedTrace(String userToken, Pageable pageable) {
        return jobApplyRecordRepository.findByReceiverTokenOrderByDateDesc(userToken, pageable);
    }

    //以下方法为管理端
    public Page<JobApplyRecord> list(Pageable pageable, String text, RecState workStatus, Long positionId) {
        Specification<JobApplyRecord> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("job").get("id"), positionId));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("referral").get("realName"), "%" + t + "%"), rb.like(rt.get("receiver").get("realName"), "%" + t + "%"))));
            if (isNotNull(workStatus) && (!workStatus.getTitle().isEmpty())) {
                predicates.add(rb.equal(rt.get("recState"), workStatus));
            }
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return jobApplyRecordRepository.findAll(specification, pageable);
    }

    public BenefitRel findBenefitRel(String token, Long id) {
        return benefitRelService.findByMemberTokenAndJobIdAndType(token, id, CAR_FEE);
    }

    /**
     * 岗位推荐员工收回上班
     */
    public void callback(String receiver, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecord(id);
        refreshRecord(jobApplyRecord,
                () -> jobApplyRecord.getRecState() != WAIT_FOR_STORE_CONFIRM_WORK,
                TIP_CAN_NOT_CALLBACK,
                NOT_WORK);
    }

    private void refreshRecord(JobApplyRecord jobApplyRecord, BooleanSupplier supplier, ModuleTip msg, RecState recState) {
        ifTrueThrow(supplier, msg);
        jobApplyRecord.setRecState(recState);
        save(jobApplyRecord);
    }

    /**
     * 岗位推荐员工确认离职
     */
    public void leaveWork(String receiver, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecord(id);
        refreshRecord(jobApplyRecord,
                () -> jobApplyRecord.getRecState() != WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN,
                TIP_NOT_LEAVEWORKING,
                ALREADY_RESIGN);
        sendFriendMessage(jobApplyRecord, "leave");
        Job job = jobApplyRecord.getJob();
        job.setQuitterCount(job.getQuitterCount() + 1);
        if (getMonths(jobApplyRecord.getStartDate(), jobApplyRecord.getResignDate()) >= 1) {
            jobApplyRecord.setRecState(ALREADY_RESIGN);
            job.setWorkerCount(job.getWorkerCount() - 1);
        } else {
            jobApplyRecord.setRecState(WORK_FAIL);
            job.setEpmCount(job.getEpmCount() + 1);
            job.setWorkerCount(job.getWorkerCount() - 1);
            if (job.getEpmCount() > 0) {
                job.setState(RecruitmentState.NORMAL);
            }
        }
        jobService.save(job);
        // 将上班好处置为失效
        List<BenefitRel> benefitRels = benefitRelRepository.findByMemberTokenAndValid(receiver, YES);
        ifNotEmptyThen(benefitRels, b -> {
            b.forEach(x -> {
                if (x.getType() == WORK_INSURANCE) {
                    x.setValid(NO);
                    x.setInvalidDate(null);
                }
                Boolean flag = accountFlowService.findCarFeeAccountFlow(receiver);
                if (x.getType() == CAR_FEE && jobApplyRecord.getRecState() == WORK_FAIL && flag) {
                    //上班失败后没有得到车旅费，但是第二次上班后满一个月得到车旅费
                    x.setInvalidDate(null);
                    x.setValid(NO);
                }
            });
            benefitRelRepository.save(benefitRels);
        });
    }

    //获取两个date相差的月数
    private int getMonths(Date date1, Date date2) {
        int iMonth = 0;
        int flag = 0;
        try {
            Calendar objCalendarDate1 = Calendar.getInstance();
            objCalendarDate1.setTime(date1);

            Calendar objCalendarDate2 = Calendar.getInstance();
            objCalendarDate2.setTime(date2);

            if (objCalendarDate2.equals(objCalendarDate1))
                return 0;
            if (objCalendarDate1.after(objCalendarDate2)) {
                Calendar temp = objCalendarDate1;
                objCalendarDate1 = objCalendarDate2;
                objCalendarDate2 = temp;
            }
            if (objCalendarDate2.get(DAY_OF_MONTH) < objCalendarDate1.get(DAY_OF_MONTH))
                flag = 1;

            if (objCalendarDate2.get(YEAR) > objCalendarDate1.get(YEAR))
                iMonth = ((objCalendarDate2.get(YEAR) - objCalendarDate1.get(YEAR))
                        * 12 + objCalendarDate2.get(MONTH) - flag)
                        - objCalendarDate1.get(MONTH);
            else
                iMonth = objCalendarDate2.get(MONTH)
                        - objCalendarDate1.get(MONTH) - flag;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.abs(iMonth);
    }


    //计算离职员工中正常离职和上班失败的人数
    public int countRefundType(Long id, int type) {
        List<JobApplyRecord> applyRecords = jobApplyRecordRepository.findByJobIdAndRefundAndRecStateIn(id, HAS_NOT_REFUNDED, states);
        return calRefund(applyRecords, type);
    }

    //计算岗位中所有违约的记录的总的违约时间
    public int countResignDays(Long id) {
        List<JobApplyRecord> applyRecords = jobApplyRecordRepository.findByJobIdAndRefundAndRecStateIn(id, HAS_NOT_REFUNDED, states);
        return calResignDays(applyRecords);
    }

    //计算岗位总的违约金
    public BigDecimal countResignBreach(Long id) {
        List<JobApplyRecord> applyRecords = jobApplyRecordRepository.findByJobIdAndRefundAndRecStateIn(id, HAS_NOT_REFUNDED, states);
        return calResignBreach(applyRecords);
    }

    //离职退款，分为三种情况：1、退4个月；2、退3个月；退3个月-违约金；具体判断规则见countRefundMoney方法
    public String refundResign(String userToken, Long id) {
        Job job = jobService.findCanRefundJob(userToken, id);
        ifFalseThrow(job.getRecType() == MONTHLY, TIP_JOB_ERROR_TYPE);
        job.setState(FROZEN);
        //获取当前岗位已经离职未退款的记录列表
        List<RecState> states = newArrayList(ALREADY_RESIGN, WORK_FAIL);
        List<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByJobIdAndRefundAndRecStateIn(id, HAS_NOT_REFUNDED, states);
        ifFalseThrow(jobApplyRecords.size() <= job.getQuitterCount(), TIP_COUNT_OUT_OF_RANGE);
        HpApply hpApply = new HpApply();
        BigDecimal refund = new BigDecimal(0);
        Integer refundFourNum = 0;//退四个月的人
        Integer refundThreeNum = 0;//退三个月的人
        Integer refundBreachNum = 0;//退三个月减违约金的人
        BigDecimal allBreach = new BigDecimal(0);//所有违约金
        Integer breachDays = 0;//总违约天数
        Integer breachJobApplyNum = 0;
        for (JobApplyRecord jobApplyRecord : jobApplyRecords) {
            Integer check = checkRefundState(jobApplyRecord, jobApplyRecord.getResignDate());
            if (check == 1) refundFourNum++;
            else if (check == 0) refundThreeNum++;
            else {
                refundBreachNum++;
                breachJobApplyNum++;
                AutomaticRenewals automaticRenewals = automaticRenewalsRepository.
                        findByJobIdAndWorkerIdAndIsDelete(jobApplyRecord.getJob().getId(), jobApplyRecord.getReceiver().getId(), NO);
                allBreach = allBreach.add(automaticRenewals.getBreach());
                if (automaticRenewals.getFinalDate().getTime() < jobApplyRecord.getResignDate().getTime()) {
                    breachDays += (DateUtil.dayGap(new Date(automaticRenewals.getFinalDate().getTime()), jobApplyRecord.getResignDate()));
                }
            }

            refund = refund.add(countRefundMoney(jobApplyRecord, jobApplyRecord.getResignDate()));
            AutomaticRenewals automaticRenewals = automaticRenewalsRepository.
                    findByJobIdAndWorkerIdAndIsDelete(job.getId(), jobApplyRecord.getReceiver().getId(), NO);
            RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimes(automaticRenewals.getId(), automaticRenewals.getTimes());
            renewalsRecord.setDealType(RenewalsDealType.DEAL_BY_RETURN);
            //修改续费记录
            renewalsRecordService.save(renewalsRecord);
            //删除自动续费设置
            renewalsRecordService.deleteAutoRenewals(automaticRenewals);
            //将退款申请保存到jobApplyRecord
            jobApplyRecord.setHpApply(hpApplyRepository.save(hpApply));
        }
        //发起退款申请
        Member member = baseMemberService.findByToken(userToken);
        Account memberAccount = accountService.findByMemberToken(userToken);
        ifNullThrow(memberAccount, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        hpApply.setApplicant(member);
        hpApply.setJob(job);
        hpApply.setMoney(refund);
        hpApply.setState(IN_REVIEW);
        hpApply.setType(JOB_RESIGN);
        hpApply.setRefundCount(jobApplyRecords.size());
        hpApply.setRefundFourNum(refundFourNum);
        hpApply.setRefundThreeNum(refundThreeNum);
        hpApply.setRefundBreachNum(refundBreachNum);
        if (allBreach.doubleValue() > 0) hpApply.setBreach(allBreach);
        hpApply.setBreachDays(breachDays);
        hpApply.setBreachJobApplyNum(breachJobApplyNum);
        hpApplyRepository.save(hpApply);
        jobApplyRecordRepository.save(jobApplyRecords);
        jobService.savePayInAccountFlow(hpApply, memberAccount);
        return "success";
    }


    //计算剩余的钱
    private BigDecimal calResignMoney(List<JobApplyRecord> jobApplyRecords) {
        BigDecimal money = new BigDecimal(0);
        for (JobApplyRecord jar : jobApplyRecords) {
            money = money.add(countRefundMoney(jar, jar.getResignDate()));
        }
        return money;
    }

    //计算违约金
    private BigDecimal calResignBreach(List<JobApplyRecord> jobApplyRecords) {
        BigDecimal breach = new BigDecimal(0);
        for (JobApplyRecord jar : jobApplyRecords) {
            if (checkRefundState(jar, jar.getResignDate()) == -1) {
                Long jobId = jar.getJob().getId();
                Long workerId = jar.getReceiver().getId();
                AutomaticRenewals automaticRenewals = automaticRenewalsRepository.findByJobIdAndWorkerIdAndIsDelete(jobId, workerId, NO);
                breach = breach.add(automaticRenewals.getBreach());
            }
        }
        return breach;
    }


    //计算违约时间
    private int calResignDays(List<JobApplyRecord> jobApplyRecords) {
//        int num = 0;
//        for (JobApplyRecord jar : jobApplyRecords) {
//            if (checkRefundState(jar, jar.getResignDate()) == -1) {
//                //离职时间
//                LocalDate resignDate = LocalDateTime.ofInstant(jar.getResignDate().toInstant(), ZoneId.systemDefault()).toLocalDate();
//                //上班时间
//                LocalDate startDate = LocalDateTime.ofInstant(jar.getStartDate().toInstant(), ZoneId.systemDefault()).toLocalDate();
//                num += Period.between(startDate, resignDate).getDays();
//            }
//        }
//        return num;
        final int[] day = {0};
        for (JobApplyRecord jobApplyRecord : jobApplyRecords) {
            AutomaticRenewals automaticRenewals = automaticRenewalsRepository.
                    findByJobIdAndWorkerIdAndIsDelete(jobApplyRecord.getJob().getId(), jobApplyRecord.getReceiver().getId(), NO);
            boolean isBreach = (automaticRenewals.getFinalDate().getTime() < new Date().getTime());
            ifTrueThen(isBreach, () -> {
                RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimes(automaticRenewals.getId(), automaticRenewals.getTimes());
                if ((isNotNull(renewalsRecord) && renewalsRecord.getRenewType() == RenewType.FAILED) || (!isNotNull(renewalsRecord))) {
                    //当前时间
                    LocalDate nowDate = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).toLocalDate();
                    //续费最后时间
                    LocalDate finalDate = LocalDateTime.ofInstant(new Date(automaticRenewals.getFinalDate().getTime()).toInstant(), ZoneId.systemDefault()).toLocalDate();
                    day[0] += Period.between(finalDate, nowDate).getDays();
                }
            });
        }
        return day[0];
    }

    //统计某种离职类型（离职，上班失败）的人数
    private int calRefund(List<JobApplyRecord> jobApplyRecords, int type) {
        int num = 0;
        for (JobApplyRecord jar : jobApplyRecords) {
            if (checkRefundState(jar, jar.getResignDate()) == type)
                num++;
        }
        return num;
    }

    //判断离职时退款属于哪种情况（1、退4个月；2、退3个月；退3个月-违约金）
    private Integer checkRefundState(JobApplyRecord jobApplyRecord, Date date) {
        Long jobId = jobApplyRecord.getJob().getId();
        Long workerId = jobApplyRecord.getReceiver().getId();
        RenewalsRecord renewalsRecord = renewalsRecordService.findByRenewalsJobIdAndRenewalsWorkerId(jobId, workerId);
        AutomaticRenewals automaticRenewals = automaticRenewalsRepository.findByJobIdAndWorkerIdAndIsDelete(jobId, workerId, NO);
        Date d1, d2, d3;
        d2 = date;//离职时间
        //无续费记录
        if (isNull(renewalsRecord)) {
            d1 = automaticRenewals.getAvaDate();//上班时间
            d3 = automaticRenewals.getNextTime();//下次续费的时间
        } else {
            d1 = renewalsRecord.getDate();//最近一次续费时间
            d3 = automaticRenewals.getNextTime();//下次续费时间
        }

        if (d1.before(d2) && d2.before(d3))
            return 1;//情况1
        else if (d3.before(d2) && d2.before(addDays(d3, 6)))
            return 0;//情况2
        else
            return -1;//情况3
    }

    //根据离职属于哪种情况计算具体要退款的数额
    public BigDecimal countRefundMoney(JobApplyRecord jobApplyRecord, Date time) {
        Integer check = checkRefundState(jobApplyRecord, time);
        if (check == 1) {
            return jobApplyRecord.getJob().getReward().multiply(new BigDecimal(4));
        } else if (check == 0) {
            return jobApplyRecord.getJob().getReward().multiply(new BigDecimal(3));
        } else {
            Long jobId = jobApplyRecord.getJob().getId();
            Long workerId = jobApplyRecord.getReceiver().getId();
            AutomaticRenewals automaticRenewals = automaticRenewalsRepository.findByJobIdAndWorkerIdAndIsDelete(jobId, workerId, NO);
            return jobApplyRecord.getJob().getReward().multiply(new BigDecimal(3)).subtract(automaticRenewals.getBreach());
        }

    }

    //计算退款获得的钱
    public BigDecimal countResignMoney(Long id) {
        return calResignMoney(jobApplyRecordRepository.findByJobIdAndRefundAndRecStateIn(id, HAS_NOT_REFUNDED, states));
    }

    public JobApplyRecord jobApplyRecord(Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findOne(id);
        ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
        return jobApplyRecord;
    }

    public JobApplyRecord getJobApplyRecord(String userToken, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findByShopOwnerTokenAndId(userToken, id);
        ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
        return jobApplyRecord;
    }

    public Page<JobApplyRecord> getQuitWorkerPage(RecruitmentType recruitmentType, RecState recState, Long id, Pageable pageable) {
        return jobApplyRecordRepository.findByJobRecTypeAndRecStateAndShopAreaIdOrderByDateDesc(recruitmentType, recState, id, pageable);
    }

    public List<JobApplyRecord> getInJobWorkerPage(RecruitmentType recruitmentType, RecState recState, Long id, Pageable pageable) {
        return jobApplyRecordRepository.findByJobRecTypeAndShopAreaIdAndRecStateOrderByDate(recruitmentType, id, recState);

//        if (bool)
//            return jobApplyRecordRepository.findMoreThanAMonthWorker(recruitmentType, recState, id, pageable);
//        else
//            return jobApplyRecordRepository.findLessThanAMonthWorker(recruitmentType, recState, id, pageable);
    }

    public JobApplyRecord findJobApplyRecord(Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findOne(id);
        ifNullThrow(jobApplyRecord, TIP_NO_CHANSHUERROR);
        return jobApplyRecord;
    }

    public JobApplyRecord findByReceiverTokenAndJobIdAndRecState(String token, Long id, RecState working) {
        return jobApplyRecordRepository.findByReceiverTokenAndJobIdAndRecState(token, id, working);
    }

    public JobApplyRecord findByJobIdAndRecState(Long id, RecState working) {
        return jobApplyRecordRepository.findByJobIdAndRecState(id, working);
    }


    /**
     * 查找我推荐的人或者推荐我的人
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    public Page<JobApplyRecord> findRecommendContro(BackPageVo pageVO, String text, RecState workStatus, String tableType, Long userId) {
        Specification<JobApplyRecord> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotBlankThen(text, e -> {
                String textStr = "%" + e + "%";
                if (tableType.equals("recOthers")) {
                    predicates.add(rb.or(rb.like(rt.get("shop").get("name"), textStr),
                            rb.like(rt.get("job").get("name"), textStr),
                            rb.like(rt.get("receiver").get("realName"), textStr)));
                } else if (tableType.equals("OthersRecMe")) {
                    predicates.add(rb.or(rb.like(rt.get("shop").get("name"), textStr),
                            rb.like(rt.get("job").get("name"), textStr),
                            rb.like(rt.get("receiver").get("realName"), textStr)));
                }
            });
            ifNotNullThen(workStatus, e -> predicates.add(rb.equal(rt.get("recState"), workStatus)));
            if (tableType.equals("recOthers")) predicates.add(rb.equal(rt.get("referral").get("id"), userId));
            else if (tableType.equals("OthersRecMe")) predicates.add(rb.equal(rt.get("receiver").get("id"), userId));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return jobApplyRecordRepository.findAll(specification, pageVO.pageRequest());
    }

    public JobApplyRecord findByReceiverTokenAndJobId(Long id, Long jobId) {
        return jobApplyRecordRepository.findByReceiverIdAndJobId(id, jobId);
    }

    /**
     * 平台让员工离职
     *
     * @Author xiao xue wei
     * @Date 2017/1/12
     */
    public String leaveWorkByPlatform(RenewalsRecord r, Date leaveTime) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findByReceiverIdAndJobId(r.getRenewals().getWorker().getId(), r.getJob().getId());
        ifNullThrow(jobApplyRecord, TIP_NO_MEMBER_JOB_RECORD);
        jobApplyRecord.setRecState(ALREADY_RESIGN);
        jobApplyRecord.setResignType(QUIT);
        jobApplyRecord.setResignDate(leaveTime);
        save(jobApplyRecord);
        return "success";
    }

    public List<JSONObject> countShopJobApplyInfo(Date startTime, Date endTime, RecruitmentType rewardType, Long shopId) {
        List<JSONObject> workStuffArr = new ArrayList<>();
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, NOT_WORK));
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, STORE_RETURN));
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, WAIT_FOR_STORE_CONFIRM_WORK));
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, WORKING));
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN));
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, ALREADY_RESIGN));
        workStuffArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, WORK_FAIL));
        return workStuffArr;
    }

    public JSONObject countEveryJobInfo(Date startTime, Date endTime, RecruitmentType rewardType, Long shopId, RecState state) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", state.getTitle());
        Specification<JobApplyRecord> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("shop").get("id"), shopId));
            predicates.add(rb.greaterThanOrEqualTo(rt.get("date"), startTime));
            predicates.add(rb.lessThanOrEqualTo(rt.get("date"), endTime));
            predicates.add(rb.equal(rt.get("recState"), state));
            ifNotNullThen(rewardType, e -> predicates.add(rb.equal(rt.get("job").get("recType"), rewardType)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        long everyJobApplyNum = jobApplyRecordRepository.count(specification);
        jsonObject.put("value", everyJobApplyNum);
        return jsonObject;
    }
}
