package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.AutomaticRenewals;
import com.thousandsunny.service.model.JobApplyRecord;
import com.thousandsunny.service.model.MemberMsg;
import com.thousandsunny.service.model.RenewalsRecord;
import com.thousandsunny.service.repository.AutomaticRenewalsRepository;
import com.thousandsunny.service.repository.MemberMsgRepository;
import com.thousandsunny.service.repository.RenewalsRecordRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.JOB_DEFAULT_REMIND;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.JOB_PREPAY_REMIND;
import static com.thousandsunny.service.ModuleKey.RenewType.FAILED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.sql.Date.valueOf;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;

/**
 * 如果这些代码有用，那它们是guitarist在30/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class AutomaticRenewalsService extends BaseService<AutomaticRenewals> {
    @Autowired
    private AutomaticRenewalsRepository automaticRenewalsRepository;
    @Autowired
    private MemberMsgRepository memberMsgRepository;
    @Autowired
    private RenewalsRecordRepository renewalsRecordRepository;

    public List<AutomaticRenewals> findAutoRenewalRecord(Date now) {
        Specification<AutomaticRenewals> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.lessThanOrEqualTo(rt.get("nextTime"), now));
            predicates.add(rb.equal(rt.get("auto"), TRUE));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return automaticRenewalsRepository.findAll(specification);
//        return automaticRenewalsRepository.findByNextTimeAndAuto(now, TRUE);
    }

    public AutomaticRenewals findAutoRenewals(Long jobId, Long workerId) {
        return automaticRenewalsRepository.findByJobIdAndWorkerIdAndIsDelete(jobId, workerId, NO);
    }

    /**
     * 自动续费提醒---计时器
     *
     * @Author xiao xue wei
     * @Date 2017/1/4
     */
    public void sendRepayMessage() {
        List<AutomaticRenewals> allList = automaticRenewalsRepository.findByIsDelete(NO);
        java.sql.Date now = valueOf(LocalDate.now());
        ifNotEmptyThen(allList, e -> e.forEach(automaticRenewal -> {
            if (automaticRenewal.getAuto()) {//判断是否开启自动续费（开启：到支付时间前一天提醒）
                Long subTime = automaticRenewal.getStartDate().getTime() - now.getTime();
                // 确保自动续费的开始时间在第二天内
                if (subTime > 0 && subTime <= (24 * 3600 * 1000L)) {
                    sendMsg(automaticRenewal, "您的付费招聘需要预付费用啦，到预付时间将从您的账户余额中自动扣款，请保障您的余额费用充足！");
                }
            } else {//判断是否开启自动续费（未开启：到支付时间之后七天内每天提醒一次）
                Long subTime = now.getTime() - automaticRenewal.getStartDate().getTime();
                if (subTime >= 0 && subTime < (7 * 24 * 3600 * 1000L)) {
                    RenewalsRecord renewalsRecord = renewalsRecordRepository.
                            findByRenewalsIdAndTimesAndRenewType(automaticRenewal.getId(), automaticRenewal.getTimes(), ModuleKey.RenewType.SUCCESS);
                    if (isNotNull(renewalsRecord)) return;
                    saveFalseRenewalRecord(automaticRenewal);
                    sendMsg(automaticRenewal, "您的悬赏招聘需要预付费用啦！");
                }
            }
        }));
    }

    private void saveFalseRenewalRecord(AutomaticRenewals r) {
        RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimesAndRenewType(r.getId(), r.getTimes(), FAILED);
        if (!isNotNull(renewalsRecord)) {
            renewalsRecord = new RenewalsRecord();
            renewalsRecord.setRenewType(FAILED);
            renewalsRecord.setAssigned(FALSE);
            renewalsRecord.setRenewals(r);
            renewalsRecord.setJob(r.getJob());
            renewalsRecord.setTimes(r.getTimes());
            renewalsRecord.setStartDate(r.getStartDate());
            renewalsRecord.setFinalDate(r.getFinalDate());
        }
        renewalsRecord.setDate(new java.util.Date());
        renewalsRecordRepository.save(renewalsRecord);
    }

    private void sendMsg(AutomaticRenewals automaticRenewal, String content) {
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setJob(automaticRenewal.getJob());
        memberMsg.setType(JOB_PREPAY_REMIND);
        memberMsg.setReceiver(automaticRenewal.getAccount().getMember());
        memberMsg.setContent(content);
        List<MemberMsg> msgList = memberMsgRepository.findByReceiverAndIsDeleteAndTypeOrderByDateDesc(
                automaticRenewal.getAccount().getMember(), NO, JOB_PREPAY_REMIND);
        if (!msgList.isEmpty()) {
            msgList.forEach(msg -> msg.setIsNew(NO));
            memberMsgRepository.save(msgList);
        }
        memberMsgRepository.save(memberMsg);
    }


    public AutomaticRenewals findByJobApplyRecord(JobApplyRecord one) {
        return automaticRenewalsRepository.findByJobIdAndWorkerIdAndIsDelete(one.getJob().getId(), one.getReceiver().getId(), NO);
    }
}
