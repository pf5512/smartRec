package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.ModuleKey.RecState;
import com.thousandsunny.service.ModuleKey.RecruitmentState;
import com.thousandsunny.service.ModuleKey.RecruitmentType;
import com.thousandsunny.service.ModuleKey.RefundEnum;
import com.thousandsunny.service.ModuleTips;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.thirdparty.ModuleKey.ChargeType;
import com.thousandsunny.thirdparty.ModuleKey.FlowState;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.domain.service.ThirdPartyPayAccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleTips.TIP_PARAM_ILLEGAL;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.service.ModuleKey.ApplyType.JOB_ALL;
import static com.thousandsunny.service.ModuleKey.MomentsType.LINK;
import static com.thousandsunny.service.ModuleKey.RecState.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.*;
import static com.thousandsunny.service.ModuleKey.RefundEnum.HAS_REFUNDED;
import static com.thousandsunny.service.ModuleKey.RenewType.FAILED;
import static com.thousandsunny.service.ModuleKey.RenewType.SUCCESS;
import static com.thousandsunny.service.ModuleKey.RenewType.WAIT;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType.*;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.*;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.*;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_MEMBER_ACCOUNT_NOT_EXIST;
import static java.util.Objects.isNull;
import static org.apache.jackrabbit.util.Text.md5;

@Service
public class JobService extends BaseService<Job> {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobTypeRepository jobTypeRepository;
    @Autowired
    private JobConstantRepository jobConstantRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JobCollectService jobCollectService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountFlowRepository accountFlowRepository;
    @Autowired
    private HpApplyRepository hpApplyRepository;
    @Autowired
    private JobApplyRecordRepository jobApplyRecordRepository;
    @Autowired
    private ComplainRepository complainRepository;
    @Autowired
    private AutomaticRenewalsRepository automaticRenewalsRepository;
    @Autowired
    private RenewalsRecordRepository renewalsRecordRepository;
    @Autowired
    private MomentsService momentsService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private ThirdPartyPayAccountService payAccountService;
    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private ResumeBlockedRepository resumeBlockedRepository;
    @Autowired
    private JobBlockedRepository jobBlockedRepository;
    @Autowired
    private SrAccountApplyRecordService srAccountApplyRecordService;
    @Autowired
    private AccountFlowService accountFlowService;

    public Page<Job> getJobList(RecruitmentState state, String nameKeyword, Long jobType, Long salary, Long period, Long provinceId, Long cityId, Long areaId, Pageable pageable) {
        Specification<Job> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.equal(rt.get("isEnable"), YES));
            predicates.add(rb.equal(rt.get("state"), state));
            ifNotBlankThen(nameKeyword, t -> predicates.add(rb.like(rt.get("name"), "%" + t + "%")));
            ifNotNullThen(jobType, t -> predicates.add(rb.equal(rt.get("jobType").get("id"), t)));
            ifNotNullThen(salary, t -> predicates.add(rb.equal(rt.get("salary").get("id"), t)));
            ifNotNullThen(period, t -> predicates.add(rb.equal(rt.get("period").get("id"), t)));
            ifNotNullThen(provinceId, t -> predicates.add(rb.equal(rt.get("shop").get("province").get("id"), t)));
            ifNotNullThen(cityId, t -> predicates.add(rb.equal(rt.get("shop").get("city").get("id"), t)));
            ifNotNullThen(areaId, t -> predicates.add(rb.equal(rt.get("shop").get("area").get("id"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("recType"), false),
                    new OrderImpl(rt.get("fresh"), false), new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return jobRepository.findAll(specification, pageable);
    }

    public Job getNormalJobByType(String type, Long id) {
        Job j = jobRepository.findByIdAndStateAndIsDelete(id, NORMAL, NO);
        ifNullThrow(j, TIP_NO_JOB);
        //查询本岗位
        if (type.equals("CURRENT")) {
            return j;
        } else {//查询当前店铺的下一个岗位
            String userToken = j.getShop().getOwner().getToken();
            List<Job> jobs = jobRepository.findByShopOwnerTokenAndStateAndIsDeleteOrderByDateDescFreshDesc(userToken, NORMAL, NO);
            ifTrueThrow(jobs.indexOf(j) == -1, TIP_NO_JOB);
            int index = jobs.indexOf(j) - 1;
            //当前岗位已经是店铺的最后一个岗位
            ifTrueThrow(0 > index, TIP_NO_NEXT_JOB);
            return getJob(userToken, jobs.get(index).getId());

        }
    }

    public Job getNormalJob(Long id) {
        Job job = jobRepository.findByIdAndState(id, NORMAL);
        ifNullThrow(job, TIP_NO_JOB);
        return job;
    }

    public Job getJob(String userToken, Long id) {
        Job job = jobRepository.findByShopOwnerTokenAndIdAndStateAndIsDelete(userToken, id, NORMAL, NO);
        ifNullThrow(job, TIP_NOT_PUBLISH_JOB);
        return job;
    }

    //判断当前岗位是否为店铺的最后一个岗位
    public Boolean isLast(Long id, Long shopId) {
        Job job = jobRepository.findTop1ByShopIdAndIsDeleteAndStateOrderByDateDescFreshDesc(shopId, NO, NORMAL);
        if (job != null && id.equals(job.getId()))
            return true;
        else
            return false;
    }

    /**
     * 发布岗位
     */
    public Charge publishJob(String userToken, Job job, Long classId, String payPassword, String openId) {
        ifTrueThrow(isNull(job.getSalary()) || isNull(job.getPeriod()) || isNull(job.getEpmCount()), TIP_INVALID_DATA);
        Shop shop = shopRepository.findByOwnerToken(userToken);
        ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
        job.setShop(shop);

        JobType jobType = jobTypeRepository.findOne(classId);
        ifNullThrow(jobType, TIP_NO_JOB_TYPE);
        job.setJobType(jobType);

        ifNotNullThen(job.getJobType().getName(), job::setName);

        //免费岗位
        if (job.getRecType() == FREE) {
            job.setState(NORMAL);
            jobRepository.save(job);
            publishJobMoments(userToken, job, shop);//发布说说
            return null;
        } else {
            job.setState(WAIT_FOR_PAY);
            jobRepository.save(job);

            BigDecimal money;
            if (job.getRecType() == ONCE)
                money = job.getReward().multiply(new BigDecimal(job.getEpmCount()));
            else {
                money = job.getReward().multiply(new BigDecimal(job.getEpmCount() * 4));
            }

            //选择支付方式
            Charge charge = chooseByPayType(userToken, job.getPayType(), payPassword, money, JOB_NEW, job, null, openId);
            ifTrueThen(job.getPayType() == PAY_BY_BALANCE, () -> {//余额支付
                job.setState(NORMAL);
                publishJobMoments(userToken, job, shop);//发布说说
            });
            ifTrueThen(job.getPayType() == PAY_BY_ALIPAY || job.getPayType() == PAY_BY_WX || job.getPayType() == PAY_BY_WX_PUB, () -> job.setState(WAIT_FOR_PAY));
            ifTrueThen(job.getPayType() == PAY_OFFLINE, () -> job.setState(RecruitmentState.PAY_OFFLINE));//线下支付
            updateJobWaitForPayAndUpdateSrApplyRecord(job, charge, PUBLISH_JOB, money);
            jobRepository.save(job);

            dealWithJobBlocked(userToken, job);//工作拉黑
            return charge;
        }
    }


    /**
     * 工作拉黑
     */
    private void dealWithJobBlocked(String userToken, Job job) {
        List<ResumeBlocked> resumeBlockeds = resumeBlockedRepository.findByResumeMemberToken(userToken);
        for (ResumeBlocked resumeBlocked : resumeBlockeds) {
            Member resumeMember = resumeBlocked.getMember();
            JobBlocked jobBlocked = new JobBlocked();
            jobBlocked.setJob(job);
            jobBlocked.setMember(resumeMember);
            jobBlocked.setDate(new Date());
            jobBlockedRepository.save(jobBlocked);
        }
    }

    /**
     * 发布岗位的时候,发布说说
     */
    public void publishJobMoments(String userToken, Job job, Shop shop) {
        Moments moments = new Moments();
        String str = shop.getName() + "正在招聘" + job.getName() + ",期待您的加入！";
        moments.setContent(str.getBytes(Charset.forName("UTF-8")));
        CloudFile logo = new CloudFile();
        logo.setPath(shop.getLogo().getPath());
        moments.setPics(newArrayList(logo));

        Member publisher = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(publisher, TIP_NO_MEMBER);

        //同时发布一条说说
        moments.setAuthor(publisher);
        moments.setLatitude(shop.getLatitude());
        moments.setLongitude(shop.getLongitude());
        moments.setJob(job);
        momentsService.publishMoments(userToken, moments, new String[]{}, LINK, str,
                shop.getProvince().getId(), shop.getCity().getId(), shop.getArea().getId());
    }

    //非免费岗位选择支付方式，余额付款或者线下付款，则产生流水，返回null；若支付宝或者微信，则返回charge对象
    private Charge chooseByPayType(String userToken,
                                   PayType payType,
                                   String payPassword,
                                   BigDecimal money,
                                   SourceType sourceType,
                                   Job job,
                                   JobApplyRecord jobApplyRecord,
                                   String openId) {
        AccountFlow accountFlow = new AccountFlow();
        if (payType == PAY_BY_BALANCE) {//余额付款
            Account account = accountRepository.findByMemberToken(userToken);
            ifNullThrow(account, TIP_NO_ACCOUNT);
            ifFalseThrow(account.getPayPassword().equals(md5(md5(payPassword))), TIP_ERROR_PAY_PASSWORD);
            accountFlow.setAccount(account);
            accountFlow.setAmount(money);
            accountFlow.setOrderNo(genSerialNo());
            accountFlow.setRecordType(BILL_PAY_ONLINE);
            accountFlow.setJob(job);
            accountFlow.setPayType(PAY_BY_BALANCE);
            accountFlow.setState(FlowState.SUCCESS);
            accountFlow.setType(ChargeType.PAY_OUT);
            accountFlow.setRemark(sourceType.getRemark());
            accountFlow.setSource(sourceType);
            accountFlow.setJobApplyRecord(jobApplyRecord);

            BigDecimal subBalance = account.getBalance().subtract(account.getFreezingAmount()).subtract(money);
            BigDecimal subTotal = account.getTotal().subtract(money).subtract(account.getFreezingAmount());
            ifTrueThrow(subBalance.intValue() < 0 || subTotal.intValue() < 0, TIP_NO_ENOUGH_BALANCE);
            account.setBalance(account.getBalance().subtract(money));
            account.setTotal(account.getTotal().subtract(money));
            accountRepository.save(account);

            accountFlowRepository.save(accountFlow);
            //产生平台流水
            accountFlowService.processPlatform(null, money, sourceType, PLATFORM_IN, null, null, job, jobApplyRecord, payType);

            return null;
        } else {
            return payAccountService.choosePayType(userToken, payType, money, sourceType, job, jobApplyRecord, null, null, null, "岗位付款", openId);
        }
    }

    //返回岗位列表，根据岗位的状态排序，然后按照先发布时间后刷新时间排序
    public List<Job> getPublishedJobList(String userToken) {
        List<Job> jobs = jobRepository.findByShopOwnerTokenAndIsDeleteOrderByDateDescFreshDesc(userToken, NO);
        return jobs;
//        List<Job> jobList = newArrayList();
//        for (Job job : jobs) {
//            List<AccountFlow> accountFlows = accountFlowRepository.findByStateAndJobId(FlowState.SUCCESS, job.getId());
//            for (AccountFlow accountFlow : accountFlows) {
//                if (accountFlow != null && !jobList.contains(accountFlow.getJob())) {
//                    jobList.add(accountFlow.getJob());
//                }
//            }
//
//        }
//        return jobList;

//        List<Job> orderedJobs = simpleFilter(jobList, e -> e.getState() != PAUSE && e.getState() != FROZEN && e.getState() != STOP);
//        orderedJobs.addAll(simpleFilter(jobList,e -> e.getState() == PAUSE || e.getState() == FROZEN || e.getState() == STOP));
//        return orderedJobs;
    }

    //免费岗一天刷新一次，其他岗位一天刷新三次
    public void refresh(String userToken, Long id) {
        Job job = getJob(userToken, id);
        //判断是否为首次刷新
        if (isNull(job.getFresh())) {
            job.setFresh(new Date());
            job.setFreshCount(1);
            return;
        }
        int refreshedDay = LocalDateTime.ofInstant(job.getFresh().toInstant(), ZoneId.systemDefault()).getDayOfMonth();
        int nowDay = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).getDayOfMonth();
        //免费岗位每天只能刷新一次
        if (job.getRecType() == FREE) {
            ifTrueThrow(nowDay == refreshedDay, TIP_CAN_NOT_REFRESH);
            job.setFresh(new Date());
        }
        //悬赏岗位每天刷新三次
        else {
            //两次刷新为同一天，判断刷新次数
            if (nowDay == refreshedDay) {
                ifTrueThrow(job.getFreshCount() == 3, TIP_CAN_NOT_REFRESH);
                job.setFresh(new Date());
                job.setFreshCount(job.getFreshCount() + 1);
            }
            //两次刷新不是同一天，重置次数
            else {
                job.setFresh(new Date());
                job.setFreshCount(1);
            }
        }
        jobRepository.save(job);
    }

    //如果没有人上班的时候，可以去相应岗位的编辑页面（悬赏招聘中的人数和金额均不能编辑，免费招聘不受限制）；如果有人上班的情况下，不可编辑任何数据；
    public void edit(String userToken, Job j) {
        Job job = jobRepository.findByShopOwnerTokenAndIdAndIsDelete(userToken, j.getId(), NO);
        ifNullThrow(job, TIP_NOT_PUBLISH_JOB);
        ifTrueThrow(job.getWorkerCount() + job.getQuitterCount() > 0, TIP_CAN_NOT_EDIT_JOB);
        if (job.getRecType() == FREE)
            ifNotNullThen(j.getEpmCount(), job::setEpmCount);
        ifNotNullThen(jobTypeRepository.findOne(j.getJobType().getId()).getName(), job::setName);
        ifNotNullThen(jobConstantRepository.findOne(j.getSalary().getId()), job::setSalary);
        ifNotNullThen(jobConstantRepository.findOne(j.getPeriod().getId()), job::setPeriod);
        ifNotNullThen(j.getDescription(), job::setDescription);
        if (job.getRecType() == MONTHLY)
            ifNotNullThen(j.getIsAuto(), job::setIsAuto);
    }

    //免费岗直接增加人数，其他岗先选择支付方式，要支付增加人数*4个月的金额
    public Charge addPeople(String userToken, Long id, Integer additionPeopleCount, PayType payType, String payPassword, String openId) {
        Job job = getJob(userToken, id);
        BigDecimal money;
        if (job.getRecType() == FREE) {
            job.setEpmCount(job.getEpmCount() + additionPeopleCount);
            return null;
        } else if (job.getRecType() == ONCE) {
            money = job.getReward().multiply(new BigDecimal(additionPeopleCount));
        } else {
            money = job.getReward().multiply(new BigDecimal(additionPeopleCount * 4));
        }
        Charge charge = chooseByPayType(userToken, payType, payPassword, money, JOB_ADD, job, null, openId);
        if (payType == PAY_BY_BALANCE) {
            job.setEpmCount(job.getEpmCount() + additionPeopleCount);
        } else {
            job.setChangeCount(additionPeopleCount);
        }
        ifTrueThen(job.getPayType() == PAY_BY_ALIPAY || job.getPayType() == PAY_BY_WX || job.getPayType() == PAY_BY_WX_PUB, () -> job.setState(ADD_PEOPLE_FOR_PAY));
        updateJobWaitForPayAndUpdateSrApplyRecord(job, charge, JOB_ADD_EMPLOYEE_COUNT, money);

        jobRepository.save(job);
        return charge;
    }

    /**
     * 设置工作_待付款
     */
    private void updateJobWaitForPayAndUpdateSrApplyRecord(Job job, Charge charge, SrAccountApplyRecordType source, BigDecimal money) {
        ifTrueThen(job.getPayType() == PAY_BY_WX || job.getPayType() == PAY_BY_ALIPAY || job.getPayType() == PAY_BY_WX_PUB, () -> {//第三方支付
            SrAccountApplyRecord accountApplyRecord = srAccountApplyRecordService.findByOrderNo(charge.getOrderNo());
            accountApplyRecord.setSource(source);
            accountApplyRecord.setAmount(money);
            accountApplyRecord.setJob(job);
            srAccountApplyRecordService.save(accountApplyRecord);
        });
    }

    //岗位在NORMAL和PAUSE状态之间切换
    public void changeState(String userToken, Long id, OperatorType operatorType) {
        Job job = jobRepository.findByShopOwnerTokenAndIdAndIsDelete(userToken, id, NO);
        ifNullThrow(job, TIP_NOT_PUBLISH_JOB);
        ifFalseThrow(job.getState() == NORMAL || job.getState() == PAUSE, TIP_CANNOT_CHANGE_STATE);
        if (operatorType == SURE) {
            ifTrueThrow(job.getState() == PAUSE, TIP_PAUSE);
            job.setState(PAUSE);
        } else {
            ifTrueThrow(job.getState() == NORMAL, TIP_RESUME);
            job.setState(NORMAL);
        }
        jobRepository.save(job);
    }

    //岗位发布后没有立即付款，具体付款流程同发布时付款
    public Charge payForJob(String userToken, Long id, PayType payType, String payPassword, String openId) {
        List<RecruitmentState> list = newArrayList(WAIT_FOR_PAY, ADD_PEOPLE_FOR_PAY);
        Job job = jobRepository.findByShopOwnerTokenAndIdAndStateInAndIsDelete(userToken, id, list, NO);
        ifNullThrow(job, TIP_NO_PAY_JOB);
        ifTrueThrow(job.getRecType() == FREE, TIP_NOT_NEED_PAY);

        BigDecimal money;
        if (job.getRecType() == ONCE)
            money = job.getReward().multiply(new BigDecimal(job.getEpmCount()));
        else
            money = job.getReward().multiply(new BigDecimal(job.getEpmCount() * 4));
        Charge charge = chooseByPayType(userToken, payType, payPassword, money, JOB_NEW, job, null, openId);
        if (job.getPayType() != payType)
            job.setPayType(payType);
        if (job.getPayType() == PAY_BY_BALANCE)
            job.setState(NORMAL);
        if (job.getPayType() == PAY_OFFLINE) {
            job.setState(RecruitmentState.PAY_OFFLINE);
        }
        updateJobWaitForPayAndUpdateSrApplyRecord(job, charge, JOB_PAY, money);
        jobRepository.save(job);

//        AccountFlow accountFlow = accountFlowRepository.findTop1ByAccountMemberTokenAndSourceOrderByCreateDate(userToken, JOB_NEW);
//        ifNotNullThen(accountFlow, a -> accountFlowRepository.save(a));
        return charge;

    }

    //岗位收藏
    public void collectJob(String userToken, Long id, OperatorType operatorType) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        JobCollect jobCollect = jobCollectService.findByMemberTokenAndJobId(userToken, id);

        if (operatorType == SURE) {//收藏
            Job job = getNormalJob(id);
            ifTrueThrow(jobCollect != null && jobCollect.getCollectEver() == NO, TIP_COLLECT);
            if (jobCollect == null) {
                jobCollect = new JobCollect();
                jobCollect.setMember(member);
                jobCollect.setJob(job);
                jobCollect.setDate(new Date());
                jobCollectService.save(jobCollect);
            } else {
                jobCollect.setCollectEver(NO);
                jobCollect.setDate(new Date());
            }
        } else {//取消收藏
            ifNullThrow(jobCollect, TIP_NOT_COLLECT);
            ifTrueThrow(jobCollect.getCollectEver() == BooleanEnum.YES, TIP_CANCEL_COLLECT);
            jobCollect.setCollectEver(BooleanEnum.YES);
        }
    }

    //判断该岗位已经有人上班或离职时，不允许删除；悬赏类型的岗位存在没有退费的人数时，不允许删除；可删除的岗位情况：1、免费招聘；2、待付款招聘；3、未招到人且已退款招聘；
    public void deleteJob(String userToken, Long id) {
        Job job = jobRepository.findByShopOwnerTokenAndIdAndIsDelete(userToken, id, NO);
        if (job.getRecType() == FREE || job.getState() == WAIT_FOR_PAY) {
            job.setIsDelete(YES);
            job.setState(DELETE);
            jobRepository.save(job);
            return;
        }
        ifFalseThrow(job.getState() == NORMAL, TIP_JOB_CANNOT_DELETE);
        List<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByJobId(id);
        ifTrueThrow(!jobApplyRecords.isEmpty() || job.getEpmCount() != 0, TIP_HAS_WORKER_JOB);
        job.setIsDelete(YES);
        job.setState(DELETE);
        jobRepository.save(job);
    }

    //岗位减少人数后，要判断人数的合法性，申请后岗位会冻结，直到结算完成
    public void cutCount(String userToken, Long id, Integer count, RecruitmentType recruitmentType) {
        Job job = getJob(userToken, id);
        ifFalseThrow(count > 0 && count <= job.getEpmCount(), TIP_COUNT_OUT_OF_RANGE);
        job.setState(FROZEN);
        BigDecimal refund;
        ifFalseThrow(job.getRecType() == recruitmentType, TIP_JOB_ERROR_TYPE);
        if (recruitmentType == ONCE) {
            refund = job.getReward().multiply(new BigDecimal(count));
        } else
            refund = job.getReward().multiply(new BigDecimal(count * 4));
        HpApply hpApply = new HpApply();
        Member member = memberRepository.findByToken(userToken);
        hpApply.setApplicant(member);
        hpApply.setJob(job);
        hpApply.setMoney(refund);
        hpApply.setState(IN_REVIEW);
        if (count == job.getEpmCount())
            hpApply.setType(ModuleKey.ApplyType.JOB_ALL);
        else hpApply.setType(ModuleKey.ApplyType.JOB_CUT);
        hpApply.setRefundCount(count);
        hpApplyRepository.save(hpApply);
        Account memberAccount = accountRepository.findByMemberToken(userToken);
        ifNullThrow(memberAccount, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        savePayInAccountFlow(hpApply, memberAccount);
    }

    public Page<JobApplyRecord> listWorker(String userToken, RecruitmentType type, Pageable pageable) {
        List<RecState> recStates = newArrayList(WORKING, WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN);
        return jobApplyRecordRepository.findByShopOwnerTokenAndJobRecTypeAndRecStateInOrderByDateDesc(userToken, type, recStates, pageable);
    }


    public Complain complain(String userToken, Long id, String reason) {
        JobApplyRecord jobApplyrecord = getJobApplyRecord(userToken, id);
        ifFalseThrow(jobApplyrecord.getRecState() == WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN, TIP_ABNORMAL_STATUS);
        Complain complain = new Complain();
        complain.setComplainant(jobApplyrecord.getShop().getOwner());
        complain.setDefendant(jobApplyrecord.getReceiver());
        complain.setReason(reason);
        return complainRepository.save(complain);
    }

    /**
     * 岗位推荐已离职员工列表
     */
    public Page<JobApplyRecord> listResign(String userToken, RecruitmentType type, Pageable pageable) {
        Page<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByShopOwnerTokenAndJobRecTypeAndRecStateOrderByResignDateDesc(userToken, type, ALREADY_RESIGN, pageable);
        ifNullThrow(jobApplyRecords, TIP_NO_MEMBER_JOB_RECORD);
        return jobApplyRecords;
    }

    /**
     * 岗位推荐上班失败员工列表
     */
    public Page<JobApplyRecord> listFailed(String userToken, RecruitmentType type, Pageable pageable) {
        Page<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByShopOwnerTokenAndJobRecTypeAndRecStateOrderByResignDateDesc(userToken, type, WORK_FAIL, pageable);
        ifNullThrow(jobApplyRecords, TIP_NO_MEMBER_JOB_RECORD);
        return jobApplyRecords;
    }

    public Boolean isRefunded(RecState recState, RefundEnum refundEnum) {
        return recState == ALREADY_RESIGN || recState == WORK_FAIL && refundEnum == HAS_REFUNDED;
    }

    public AutomaticRenewals getAutomaticRenewals(JobApplyRecord jobApplyRecord) {
        return automaticRenewalsRepository.findByJobIdAndWorkerTokenAndIsDelete(jobApplyRecord.getJob().getId(), jobApplyRecord.getReceiver().getToken(), NO);
    }

    /**
     * 岗位推荐在职员工按月悬赏续费
     */
    public Charge renew(String userToken, Long id, PayType payType, String payPassword, String openId) {
        JobApplyRecord jobApplyRecord = jobApplyRecordService.getJobApplyRecord(userToken, id);
        ifFalseThrow(jobApplyRecord.getJob().getRecType() == MONTHLY, ModuleTips.TIP_NO_NEED_RENEW);
        AutomaticRenewals automaticRenewals = getAutomaticRenewals(jobApplyRecord);
        BigDecimal money = automaticRenewals.getFee().add(automaticRenewals.getBreach());
        RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimesAndRenewType(automaticRenewals.getId(), automaticRenewals.getTimes(), FAILED);
        if (!isNotNull(renewalsRecord)) {
            renewalsRecord = new RenewalsRecord();
            renewalsRecord.setRenewals(automaticRenewals);
            renewalsRecord.setStartDate(automaticRenewals.getStartDate());
            renewalsRecord.setFinalDate(automaticRenewals.getFinalDate());
        }
        renewalsRecord.setJob(jobApplyRecord.getJob());
        Charge charge = chooseByPayType(userToken, payType, payPassword, money, JOB_RENEW, jobApplyRecord.getJob(), jobApplyRecord, openId);
        if (payType == PAY_BY_BALANCE) {
            AccountFlow accountFlow = accountFlowRepository.findTop1ByAccountMemberTokenOrderByCreateDateDesc(userToken);
            renewalsRecord.setAccountFlow(accountFlow);
            renewalsRecord.setFee(accountFlow.getAmount());
            renewalsRecord.setRenewType(SUCCESS);
            jobApplyRecordService.refreshAutoRenewalRecord(automaticRenewals);
        }
        if (payType == PAY_OFFLINE) {
            renewalsRecord.setRenewType(WAIT);
        }
        ifTrueThen(payType == PAY_BY_WX || payType == PAY_BY_ALIPAY || payType == PAY_BY_WX_PUB, () -> {//第三方支付
            SrAccountApplyRecord accountApplyRecord = srAccountApplyRecordService.findByOrderNo(charge.getOrderNo());
            accountApplyRecord.setJobApplyRecord(jobApplyRecord);
            accountApplyRecord.setJob(jobApplyRecord.getJob());
            accountApplyRecord.setAmount(money);
            accountApplyRecord.setSource(RENEW_JOB);
            srAccountApplyRecordService.save(accountApplyRecord);
        });
        renewalsRecord.setDate(new Date());
        renewalsRecordRepository.save(renewalsRecord);
        return charge;

    }

    public Integer getResignAndNotRefundedPeopleCount(Job job) {
        return job.getQuitterCount() - job.getRefundCount();
    }

    /**
     * 接受岗位推荐
     */
    public void acceptJobRecRecord(String receiverToken, String referralToken, Long jobId) {
//        不要删掉
//        List<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByReceiverToken(receiverToken);
//        for (JobApplyRecord j : jobApplyRecords) {
//            ifTrueThrow(j.getRecState() == WORKING || j.getRecState() == WAIT_FOR_EMPLOYEE_CONFIRM_RESIGN, TIP_NO_JOBRECORD);
//        }
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReferralTokenAndReceiverTokenAndJobId(referralToken, receiverToken, jobId);
        if (isNotNull(jobApplyRecord)) {
            jobApplyRecord.setRecState(NOT_WORK);
            jobApplyRecord.setResignDate(null);
            jobApplyRecord.setStartDate(null);
            jobApplyRecord.setResignRemark("");
            jobApplyRecord.setResignType(null);
            jobApplyRecordRepository.save(jobApplyRecord);
            return;
        }
        Job job = jobRepository.findOne(jobId);
        ifNullThrow(job, TIP_PARAM_ILLEGAL);

        Member receiver = memberRepository.findByTokenAndIsDelete(receiverToken, NO);
        Member referral = memberRepository.findByTokenAndIsDelete(referralToken, NO);

        jobApplyRecord = new JobApplyRecord();
        jobApplyRecord.setDate(new Date());
        jobApplyRecord.setJob(job);
        jobApplyRecord.setShop(job.getShop());
        jobApplyRecord.setReceiver(receiver);
        jobApplyRecord.setReferral(referral);
        jobApplyRecord.setRecState(NOT_WORK);
        jobApplyRecordService.save(jobApplyRecord);

        //解除上一个关系，并产生新的关系
//        memberRecRelService.updateMemberRecRel(receiver, referral);
    }


    public JobApplyRecord getJobApplyRecord(String userToken, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findByShopOwnerTokenAndId(userToken, id);
        ifNullThrow(jobApplyRecord, TIP_NO_JOB_RECORD);
        return jobApplyRecord;
    }

    public void changeJobState(JobApplyRecord jobApplyRecord) {
        Job job = jobApplyRecord.getJob();
        job.setEpmCount(job.getEpmCount() - 1);
        job.setWorkerCount(job.getWorkerCount() + 1);
        if (job.getEpmCount() == 0) {
            job.setState(STOP);
        }
        jobRepository.save(job);
    }


    public Integer getQuitWorkerNumber(RecruitmentType recruitmentType, RecState recState, Long id) {
        return jobApplyRecordRepository.countByJobRecTypeAndRecStateAndShopAreaId(recruitmentType, recState, id);
    }

    public Integer getInJobWorkerNumber(RecruitmentType recruitmentType, RecState recState, Long id, Boolean bool) {

//        StringBuffer sql = new StringBuffer();
//        sql.append("SELECT COUNT(s.m) FROM (SELECT sum(datediff(resign_date,start_date)) m FROM sr_job_apply_record a JOIN sr_job b ON (a.job_id = b.id) WHERE rec_state = '"+recState+"' AND rec_type = '"+recruitmentType+"' GROUP BY a.id) s WHERE s.m>=30");
//        Query query = entityManager.createNativeQuery(sql.toString());
        List<JobApplyRecord> jobApplyRecords = jobApplyRecordRepository.findByRecStateAndJobRecTypeAndShopAreaIdAndResignDateIsNull(recState, recruitmentType, id);
        int lessCount = 0;
        int moreCount = 0;
        for (JobApplyRecord jobApplyRecord : jobApplyRecords) {
            long time = jobApplyRecord.getStartDate().getTime() + (3600 * 1000 * 24 * 30L);
            long now = new Date().getTime();
            if (now > time) {
                moreCount++;
            } else {
                lessCount++;
            }

        }
        if (bool) {
            return moreCount;
        }
        return lessCount;
//        return query.getFirstResult();

//        if (bool)
//            return jobApplyRecordRepository.countMoreThanAMonthWorker(recruitmentType, recState, id);
//        else
//            return jobApplyRecordRepository.countLessThanAMonthWorker(recruitmentType, recState, id);

    }

    public Integer getRecruitingNumber(RecruitmentType recruitmentType, Long id) {
        Integer sum = 0;
        List<Job> jobs = jobRepository.findByShopAreaIdAndRecTypeAndIsEnableAndStateOrderByDateDesc(id, recruitmentType, YES, NORMAL);
        for (Job job : jobs) {
            sum += job.getEpmCount();
        }
        return sum;
    }

    public Long countByShop(Shop shop) {
        return jobRepository.countByShopAndIsDelete(shop, NO);
    }

    public List<Job> findNearbyJob(Long jobType, Long salary, Long period, Double longitude, Double latitude, Pageable pageable) {
        String jobTypeStr = jobType == null ? "%" : jobType + "";
        String salaryStr = salary == null ? "%" : salary + "";
        String periodStr = period == null ? "%" : period + "";
        if (isNull(longitude) || isNull(latitude))
            return jobRepository.findByState(NORMAL, pageable).getContent();
        else
            return jobRepository.findByDistance(NORMAL.name(), longitude, latitude, jobTypeStr, salaryStr, periodStr, pageable.getOffset(), pageable.getPageSize());
    }


    public List<Job> findByShop(Shop shop) {
        return jobRepository.findByShopAndStateAndIsDelete(shop, NORMAL, NO);
    }

    public HpApply handle(Long id, OperatorType type) {
        HpApply hpApply = hpApplyRepository.findByIdAndState(id, IN_REVIEW);
        ifNullThrow(hpApply, TIP_NO_APPLY);
        Account account = accountRepository.findByMemberToken(hpApply.getApplicant().getToken());
        ifNullThrow(account, TIP_NO_ACCOUNT);
        //之前是在这创建流水,改成在创建提现申请时创建流水，在这审核时将流水状态改变
        AccountFlow accountFlow = accountFlowRepository.findByAccountAndHpApplyAndStateAndType(account, hpApply, FlowState.APPROVAL, ChargeType.PAY_IN);
        Job job = hpApply.getJob();
        if (type == SURE) {
            ifNotNullThen(accountFlow, t -> t.setState(FlowState.SUCCESS));
            // 保存系统账号出账流水
            savePayOutAccountFlow(hpApply);
            if (hpApply.getType() == ModuleKey.ApplyType.JOB_CUT) {
                ifTrueThrow(job.getEpmCount() < hpApply.getRefundCount(), TIP_COUNT_OUT_OF_RANGE);
                job.setEpmCount(job.getEpmCount() - hpApply.getRefundCount());
                if (job.getEpmCount() == 0) job.setState(STOP);
                else job.setState(NORMAL);
                jobRepository.save(job);
            } else {
                job.setRefundCount(job.getRefundCount() + hpApply.getRefundCount());
                if (job.getEpmCount() == 0) job.setState(STOP);
                else job.setState(NORMAL);
                jobRepository.save(job);
            }
            hpApply.setState(REVIEW_SUCCESS);
        } else {
            ifNotNullThen(accountFlow, t -> t.setState(FlowState.FAILED));
            if (job.getEpmCount() == 0) job.setState(STOP);
            else job.setState(NORMAL);
            jobRepository.save(job);
            hpApply.setState(REVIEW_FAILED);
            //将自动续费重置回来
            List<AutomaticRenewals> automaticRenewals = automaticRenewalsRepository.findByJobIdAndIsDelete(job.getId(), YES);
            automaticRenewals.forEach(a -> a.setIsDelete(NO));
            automaticRenewalsRepository.save(automaticRenewals);
        }
        ifNotNullThen(accountFlow, t -> accountFlowRepository.save(accountFlow));
        return hpApplyRepository.save(hpApply);
    }

    // 保存系统账号账号出账流水
    public void savePayOutAccountFlow(HpApply hpApply) {
        Account account = accountRepository.findByZues(YES);
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(account);
        accountFlow.setAmount(hpApply.getMoney());
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRecordType(BILL_REFUND);
        accountFlow.setPayType(PAY_BY_BALANCE);
        accountFlow.setType(ChargeType.PAY_OUT);
        accountFlow.setHpApply(hpApply);
        if (hpApply.getType() == JOB_ALL || hpApply.getType() == ModuleKey.ApplyType.JOB_CUT) {
            accountFlow.setSource(JOB_REFUND);
            accountFlow.setRemark(JOB_REFUND.getRemark());
        } else {
            accountFlow.setSource(JOB_RESIGN);
            accountFlow.setRemark(JOB_RESIGN.getRemark());
        }
        accountFlow.setState(FlowState.SUCCESS);
        account.setBalance(account.getBalance().subtract(hpApply.getMoney()));
        account.setTotal(account.getTotal().subtract(hpApply.getMoney()));
        accountRepository.save(account);
        accountFlowRepository.save(accountFlow);
    }

    // 保存退款账号进账流水
    public void savePayInAccountFlow(HpApply hpApply, Account account) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(account);
        accountFlow.setAmount(hpApply.getMoney());
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRecordType(BILL_REFUND);
        accountFlow.setPayType(PAY_BY_BALANCE);
        accountFlow.setType(ChargeType.PAY_IN);
        accountFlow.setHpApply(hpApply);
        accountFlow.setJob(hpApply.getJob());
        if (hpApply.getType() == JOB_ALL || hpApply.getType() == ModuleKey.ApplyType.JOB_CUT) {
            accountFlow.setSource(JOB_REFUND);
            accountFlow.setRemark(JOB_REFUND.getRemark());
        } else {
            accountFlow.setSource(JOB_RESIGN);
            accountFlow.setRemark(JOB_RESIGN.getRemark());
        }
        accountFlow.setCreateDate(new Date());
        accountFlow.setState(FlowState.APPROVAL);
        account.setBalance(account.getBalance().add(hpApply.getMoney()));
        account.setTotal(account.getTotal().add(hpApply.getMoney()));
        accountRepository.save(account);
        accountFlowRepository.save(accountFlow);
    }


    /**
     * 合伙区域岗位列表
     */
    public Page<Job> jobList(String userToken, RecruitmentType type, Pageable pageable, Long areaId) {
        List<Partner> partners = partnerRepository.findByMemberTokenOrderByDate(userToken);
        ifTrueThrow(partners.isEmpty(), TIP_NO_PARTNER_APPLY);
//        Partner partner = partners.get(0);
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
//        Page<Job> jobPage = jobRepository.findByShopProvinceInAndShopCityInAndShopAreaIn(province, city, area, pageable);
//        return jobPage;
        Page<Job> jobs = jobRepository.findByShopAreaIdAndRecTypeAndIsEnableAndStateOrderByDateDesc(areaId, type, YES, NORMAL, pageable);
        return jobs;
    }


    //以下方法为管理端
    public Page<Job> listJobs(Pageable pageable, String text, String recruitmentType, String positionStatus, Long shopId) {
        ifNullThrow(shopId, TIP_PARAM_FALSE);
        Specification<Job> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("shop").get("id"), shopId));
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("name"), "%" + t + "%"), rb.like(rt.get("jobType").get("name"), "%" + t + "%"))));
            ifNotBlankThen(recruitmentType, t -> predicates.add(rb.equal(rt.get("recType"), RecruitmentType.valueOf(t))));
            ifNotBlankThen(positionStatus, t -> predicates.add(rb.equal(rt.get("state"), RecruitmentState.valueOf(t))));

            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return jobRepository.findAll(specification, pageable);
    }

    /**
     * 店铺岗位启用
     */
    public void enabled(Long id) {
        Job job = jobRepository.findOne(id);
        job.setIsEnable(job.getIsEnable().equals(YES) ? NO : YES);
        jobRepository.save(job);
    }

    public Page<AccountFlow> listJobOptation(Pageable pageable, Long id) {
        List<SourceType> sourceTypes = newArrayList(JOB_NEW, JOB_ADD, JOB_CUT, JOB_RENEW, JOB_RESIGN, JOB_REFUND);
        Account account = accountRepository.findByZues(YES);
        return accountFlowRepository.findByJobIdAndAccountNotAndSourceIn(id, account, sourceTypes, pageable);
    }

    public Page<Job> findJobList(BackPageVo pageVo, String text, RecruitmentType recruitmentType, RecruitmentState positionStatus) {
        Specification<Job> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(text, t -> {
                String textStr = "%" + t + "%";
                predicates.add(rb.or(rb.like(rt.get("shop").get("name"), textStr), rb.like(rt.get("id"), textStr), rb.like(rt.get("name"), textStr)));
            });
            ifNotNullThen(recruitmentType, t -> predicates.add(rb.equal(rt.get("recType"), t)));
            ifNotNullThen(positionStatus, t -> predicates.add(rb.equal(rt.get("state"), t)));

            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return jobRepository.findAll(specification, pageVo.pageRequest());
    }

    public List<JSONObject> countJobInfo(Date startTime, Date endTime, RecruitmentType rewardType, Long shopId) {
        List<JSONObject> recruitArr = new ArrayList<>();
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, WAIT_FOR_PAY));
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, RecruitmentState.PAY_OFFLINE));
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, NORMAL));
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, PAUSE));
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, STOP));
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, FROZEN));
        recruitArr.add(countEveryJobInfo(startTime, endTime, rewardType, shopId, DELETE));
        return recruitArr;
    }

    public JSONObject countEveryJobInfo(Date startTime, Date endTime, RecruitmentType rewardType, Long shopId, RecruitmentState state) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", state.getTitle());
        Specification<Job> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("shop").get("id"), shopId));
            predicates.add(rb.greaterThanOrEqualTo(rt.get("date"), startTime));
            predicates.add(rb.lessThanOrEqualTo(rt.get("date"), endTime));
            predicates.add(rb.equal(rt.get("state"), state));
            ifNotNullThen(rewardType, e -> predicates.add(rb.equal(rt.get("recType"), rewardType)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        long everyJobNum = jobRepository.count(specification);
        jsonObject.put("value", everyJobNum);
        return jsonObject;
    }

    public void cancelOffline(Member member, Long id) {
        Job job = jobRepository.findByShopOwnerTokenAndIdAndIsDelete(member.getToken(), id, NO);
        ifFalseThrow(RecruitmentState.PAY_OFFLINE == job.getState(), TIP_COURSE_CAN_NOT_CANCEL_UNDER_LINE);
        job.setState(ADD_PEOPLE_FOR_PAY);
        job.setPayType(null);
        //删除线下付款待确认的流水
        AccountFlow accountFlow = accountFlowRepository.findByJobIdAndSourceAndPayType(job.getId(), JOB_ADD, PAY_OFFLINE);
        ifNotNullThen(accountFlow, e -> accountFlowRepository.delete(e));
        jobRepository.save(job);
    }


    public void cancelAddPeople(Member member, Long id) {
        Job job = jobRepository.findByShopOwnerTokenAndIdAndStateAndIsDelete(member.getToken(), id, RecruitmentState.ADD_PEOPLE_FOR_PAY, NO);
        ifNullThrow(job, TIP_NO_JOB);
        job.setChangeCount(0);
        job.setState(NORMAL);
        jobRepository.save(job);
    }

    public Job findCanRefundJob(String userToken, Long jobId) {
        List<RecruitmentState> list = newArrayList(NORMAL, STOP, PAUSE, ADD_PEOPLE_FOR_PAY);
        Job job = jobRepository.findByShopOwnerTokenAndIdAndStateInAndIsDelete(userToken, jobId, list, NO);
        ifNullThrow(job, TIP_NOT_PUBLISH_JOB);
        return job;
    }
}
