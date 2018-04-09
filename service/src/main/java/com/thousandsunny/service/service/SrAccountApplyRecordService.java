package com.thousandsunny.service.service;

import com.pingplusplus.model.Charge;
import com.pingplusplus.model.Event;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.CourseSignUpRepository;
import com.thousandsunny.service.repository.EntrepreneursApplyRepository;
import com.thousandsunny.service.repository.RenewalsRecordRepository;
import com.thousandsunny.service.repository.SrAccountApplyRecordRepository;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.IdentityType.JUNIOR;
import static com.thousandsunny.core.ModuleKey.IdentityType.SENIOR;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.COURSE_ORDER_PAID;
import static com.thousandsunny.service.ModuleKey.EntrepreneursType.APPLY_JUNIOR;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.*;
import static com.thousandsunny.service.ModuleKey.RenewType.FAILED;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType.*;
import static com.thousandsunny.service.ModuleTips.TIP_CAN_NOT_SIGN_UP;
import static com.thousandsunny.service.ModuleTips.TIP_NO_COURSE_SIGN_UP;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_PAY_ONLINE;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_IN;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;
import static com.thousandsunny.thirdparty.pingpp.PingppUtil.CHARGE_SUCCEEDED;
import static com.thousandsunny.thirdparty.pingpp.PingppUtil.verifyRequest;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.OK;

/**
 * 如果这些代码有用，那它们是guitarist在21/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class SrAccountApplyRecordService extends BaseService<SrAccountApplyRecord> {

    @Autowired
    private AccountService accountService;
    @Autowired
    private SrAccountApplyRecordRepository accountApplyRecordRepository;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private JobService jobService;
    @Autowired
    private EntrepreneursApplyRepository entrepreneursService;
    @Autowired
    private PartnerApplyService partnerApplyService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private EntrepreneursService entrepreneurService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RenewalsRecordRepository renewalsRecordRepository;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private CourseApplyService courseApplyService;
    @Autowired
    private CourseRefundApplyService courseRefundApplyService;
    @Autowired
    private CourseSignUpRepository courseSignUpRepository;


    public SrAccountApplyRecord findByOrderNo(String orderNo) {
        return accountApplyRecordRepository.findByOrderNo(orderNo);
    }

    /**
     * 完成回调
     */
    public void completeCallBack(HttpServletRequest request, HttpServletResponse response) {
        Pair<String, Event> result = verifyRequest(request);
        if (CHARGE_SUCCEEDED.equals(result.getKey())) {
            Charge charge = (Charge) result.getValue().getData().getObject();
            SrAccountApplyRecord record = findByOrderNo(charge.getOrderNo());

            if (isNull(record))
                return;
            if (record.getState() == APPROVAL) {
                Account account = refreshZuesAccount(record);//系统账户加钱

                refreshSrAccountApplyRecord(record);//更新充值记录状态

                saveAccountFlow(record, account);//刷新平台流水状态

                saveAccountFlow(record);//产生用户出账流水

                response.setStatus(OK.value());
            }
        }
    }

    /**
     * 个人账户出账流水
     *
     * @Author mu.jie
     * @Date 2016/12/28
     */
    private void saveAccountFlow(SrAccountApplyRecord record) {
        AccountFlow accountFlow = new AccountFlow();
        Account account = null;
        if (record.getSource() == ENTREPRENEUR_PAY) {
            EntrepreneursApply apply = record.getEntrepreneursApply();
            //修改创业者状态
            Member member = memberService.findByToken(apply.getMember().getToken());
            if (apply.getType() == APPLY_JUNIOR)
                member.setEntrepreneurLevel(JUNIOR);
            else
                member.setEntrepreneurLevel(SENIOR);
            memberService.save(member);
            entrepreneurService.divideMoney(apply, record.getAmount(), false, record.getPayType());//付款成功后分钱

            account = accountService.findByMemberToken(apply.getMember().getToken());
            accountFlow.setEntrepreneursApply(apply);
            accountFlow.setSource(ENTREPRENEUR_APPLY);
            accountFlow.setRemark(ENTREPRENEUR_APPLY.getRemark().replace("?", apply.getType().getTitle()));
            entrepreneurService.createEntrepreneurs(apply.getMember().getToken(), apply);
        }
        if (record.getSource() == JOB_ADD_EMPLOYEE_COUNT) {
            Job job = record.getJob();
            account = accountService.findByMemberToken(job.getShop().getOwner().getToken());
            accountFlow.setJob(record.getJob());
            accountFlow.setJobApplyRecord(record.getJobApplyRecord());
            accountFlow.setSource(JOB_ADD);
            accountFlow.setRemark(JOB_ADD.getRemark());
        }
        if (record.getSource() == JOB_PAY || record.getSource() == PUBLISH_JOB) {
            Job job = record.getJob();
            accountFlow.setJob(record.getJob());
            JobApplyRecord jobApplyRecord = record.getJobApplyRecord();
            accountFlow.setJobApplyRecord(jobApplyRecord);
            account = accountService.findByMemberToken(job.getShop().getOwner().getToken());
            accountFlow.setSource(JOB_NEW);
            accountFlow.setRemark(JOB_NEW.getRemark());
        }
        if (record.getSource() == PARTNER_PAY) {
            PartnerApply partnerApply = record.getPartnerApply();
            account = accountService.findByMemberToken(partnerApply.getMember().getToken());
            accountFlow.setPartnerApply(partnerApply);
            accountFlow.setSource(PARTNER_APPLY);
            accountFlow.setRemark(PARTNER_APPLY.getRemark());
            partnerService.createPartner(partnerApply);

            //修改member的合伙人身份
            Member member = memberService.findByToken(partnerApply.getMember().getToken());
            member.setPartnerLevel(YES);
            memberService.save(member);
        }
        if (record.getSource() == RENEW_JOB) {
            accountFlow.setSource(JOB_RENEW);
            accountFlow.setRemark(JOB_RENEW.getRemark());
            accountFlow.setJob(record.getJob());
            accountFlow.setJobApplyRecord(record.getJobApplyRecord());
            account = accountService.findByMemberToken(record.getJob().getShop().getOwner().getToken());
            AutomaticRenewals automaticRenewals = jobService.getAutomaticRenewals(record.getJobApplyRecord());
            RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimesAndRenewType(automaticRenewals.getId(), automaticRenewals.getTimes(), FAILED);
            renewalsRecord.setAccountFlow(accountFlow);
            renewalsRecord.setFee(accountFlow.getAmount());
            renewalsRecord.setRenewType(ModuleKey.RenewType.SUCCESS);
            jobApplyRecordService.refreshAutoRenewalRecord(automaticRenewals);
        }
        if (record.getSource() == COURSE_PAY) {
            CourseApply courseApply = record.getCourseApply();
            account = accountService.findByMemberToken(courseApply.getMember().getToken());
            accountFlow.setCourseApply(courseApply);
            accountFlow.setSource(COURSE_APPLY);
            accountFlow.setRemark(COURSE_APPLY.getRemark());
            //学校，平台分钱
            courseApplyService.divideMoney(courseApply, record.getAmount(), record.getPayType());
            courseApply.setState(COURSE_ORDER_PAID);
            courseApplyService.save(courseApply);
            CourseSignUp courseSignUp = courseSignUpRepository.
                    findByCourseIdAndDateAndIsDeleteAndIsEnable(courseApply.getCourse().getId(),
                            new java.sql.Date(courseApply.getTrainDate().getTime()), NO, YES);
            ifNullThrow(courseSignUp, TIP_NO_COURSE_SIGN_UP);
            ifTrueThrow(courseSignUp.getCount() - courseSignUp.getSignedCount() <= 0, TIP_CAN_NOT_SIGN_UP);
            courseSignUp.setSignedCount(courseSignUp.getSignedCount() + 1);
            courseSignUpRepository.save(courseSignUp);
        }
        if (record.getSource() == SCHOOL_PAY) {
            CourseRefundApply courseRefundApply = record.getCourseRefundApply();
            account = accountService.findByMemberToken(courseRefundApply.getCourseApply().getSchool().getMember().getToken());
            accountFlow.setCourseRefundApply(courseRefundApply);
            accountFlow.setSource(SCHOOL_OF_PAY);
            accountFlow.setRemark(SCHOOL_OF_PAY.getRemark());
            courseRefundApplyService.refundPay(courseRefundApply);
        }
        accountFlow.setAccount(account);
        accountFlow.setType(PAY_OUT);
        accountFlow.setAmount(record.getAmount());
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setPayType(record.getPayType());
        accountFlow.setApplyDate(record.getCreateDate());
        accountFlow.setState(SUCCESS);
        accountFlow.setRecordType(BILL_PAY_ONLINE);
        accountFlowService.save(accountFlow);
    }

    /**
     * 平台收益流水
     */
    private void saveAccountFlow(SrAccountApplyRecord record, Account account) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setRecordType(PLATFORM_IN);
        accountFlow.setState(SUCCESS);
        accountFlow.setType(PAY_IN);
        accountFlow.setAccount(account);
        accountFlow.setAmount(record.getAmount());
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setPayType(record.getPayType());
        accountFlow.setApplyDate(record.getCreateDate());

        ifTrueThen(record.getSource() == ENTREPRENEUR_PAY, () -> {
            EntrepreneursApply apply = record.getEntrepreneursApply();
            apply.setState(ApplyEnum.SUCCESS);
            accountFlow.setSource(ENTREPRENEUR_APPLY);
            entrepreneursService.save(apply);
            accountFlow.setEntrepreneursApply(record.getEntrepreneursApply());
            accountFlow.setRemark(ENTREPRENEUR_APPLY.getRemark().replace("?", apply.getType().getTitle()));
        });
        ifTrueThen(record.getSource() == JOB_ADD_EMPLOYEE_COUNT, () -> {
            Job job = record.getJob();
            job.setEpmCount(job.getEpmCount() + job.getChangeCount());
            job.setState(NORMAL);
            job.setChangeCount(0);
            jobService.save(job);

            accountFlow.setJob(job);
            accountFlow.setSource(JOB_ADD);
            accountFlow.setRemark(JOB_ADD.getRemark());
        });
        ifTrueThen(record.getSource() == JOB_PAY, () -> {
            Job job = record.getJob();
            if (job.getState() == WAIT_FOR_PAY) {
                accountFlow.setSource(JOB_NEW);
                accountFlow.setRemark(JOB_NEW.getRemark());
            } else if (job.getState() == ADD_PEOPLE_FOR_PAY) {
                accountFlow.setSource(JOB_ADD);
                accountFlow.setRemark(JOB_ADD.getRemark());
                job.setEpmCount(job.getEpmCount() + job.getChangeCount());
                job.setChangeCount(0);
            }
            accountFlow.setJob(job);
            job.setState(NORMAL);
            jobService.save(job);
        });
        ifTrueThen(record.getSource() == PARTNER_PAY, () -> {
            PartnerApply apply = record.getPartnerApply();
            apply.setState(ApplyEnum.SUCCESS);

            accountFlow.setSource(PARTNER_APPLY);
            accountFlow.setPartnerApply(record.getPartnerApply());
            accountFlow.setRemark(PARTNER_APPLY.getRemark());
            partnerApplyService.save(apply);
        });
        ifTrueThen(record.getSource() == PUBLISH_JOB, () -> {
            accountFlow.setJob(record.getJob());
            accountFlow.setSource(JOB_NEW);
            accountFlow.setRemark(JOB_NEW.getRemark());
            record.getJob().setState(NORMAL);
            Shop shop = record.getJob().getShop();
            jobService.publishJobMoments(shop.getOwner().getToken(), record.getJob(), shop);//发布说说
            jobService.save(record.getJob());
        });
        ifTrueThen(record.getSource() == RENEW_JOB, () -> {
            accountFlow.setJob(record.getJob());
            accountFlow.setJobApplyRecord(record.getJobApplyRecord());
            accountFlow.setSource(JOB_RENEW);
            accountFlow.setRemark(JOB_RENEW.getRemark());
        });
        ifTrueThen(record.getSource() == COURSE_PAY, () -> {
            accountFlow.setCourseApply(record.getCourseApply());
            accountFlow.setSource(COURSE_APPLY);
            accountFlow.setRemark(COURSE_APPLY.getRemark());
        });
        ifTrueThen(record.getSource() == SCHOOL_PAY, () -> {
            accountFlow.setCourseRefundApply(record.getCourseRefundApply());
            accountFlow.setSource(SCHOOL_OF_PAY);
            accountFlow.setRemark(SCHOOL_OF_PAY.getRemark());
        });
        accountFlowService.save(accountFlow);
    }

    /**
     * 刷新充值记录状态
     */
    private void refreshSrAccountApplyRecord(SrAccountApplyRecord record) {
        record.setState(SUCCESS);
        record.setUpdateDate(new Date());
        accountApplyRecordRepository.save(record);
    }

    /**
     * 系统账户加钱
     */
    private Account refreshZuesAccount(SrAccountApplyRecord record) {
        Account account = accountService.findZuesAccount();
        account.setBalance(account.getBalance().add(record.getAmount()));
        account.setTotal(account.getTotal().add(record.getAmount()));
        accountService.save(account);
        return account;
    }

}
