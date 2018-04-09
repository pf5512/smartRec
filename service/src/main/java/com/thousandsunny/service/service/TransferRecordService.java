package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.ModuleKey.ApplyState;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Job;
import com.thousandsunny.service.model.ResumeBlocked;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.model.TransferRecord;
import com.thousandsunny.service.repository.JobRepository;
import com.thousandsunny.service.repository.ShopRepository;
import com.thousandsunny.service.repository.TransferRecordRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.core.ModuleKey.ApplyState.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.TIP_APPROVING;
import static com.thousandsunny.service.ModuleTips.TIP_NO_CHANSHUERROR;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 如果这些代码有用，那它们是guitarist在18/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class TransferRecordService extends BaseService<TransferRecord> {
    @Autowired
    private TransferRecordRepository transferRecordRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ResumeBlockedService resumeBlockedService;

    /**
     * 管理权转让
     */
    public TransferRecord createNewRecord(TransferRecord transferRecord) {
        TransferRecord oldTransferRecord = transferRecordRepository.findByAssignorAndValidAndIsDelete(transferRecord.getAssignor(), TRUE, NO);
        ifNotNullThrow(oldTransferRecord, TIP_APPROVING);
        return save(transferRecord);
    }


    public Page<TransferRecord> findTransferRecordList(BackPageVo pageVO, String text, ApplyState auditStatus, ApplyState tableType) {

        Specification<TransferRecord> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (auditStatus != null) {
                predicates.add(rb.equal(rt.get("state"), auditStatus));
            } else {
                if (tableType == APPROVAL) {
                    predicates.add(rb.or(rb.equal(rt.get("state"), AGREE), rb.equal(rt.get("state"), REJECT)));
                }
                if (tableType == APPLY) {
                    predicates.add(rb.equal(rt.get("state"), tableType));
                }
            }
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("assignor").get("realName"), "%" + t + "%"),
                    rb.or(rb.like(rt.get("receiverRealName"), "%" + t + "%"), rb.like(rt.get("shopName"), "%" + t + "%")))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return transferRecordRepository.findAll(specification, pageVO.pageRequest());
    }

    public void delTransferRecord(String ids) {
        if (isNotBlank(ids)) {
            newArrayList(ids.split(",")).forEach(idStr -> {
                TransferRecord one = transferRecordRepository.findOne(Long.parseLong(idStr));
                one.setIsDelete(YES);
                transferRecordRepository.save(one);
            });
        }

    }

    public TransferRecord findTransferRecord(Long id) {
        return transferRecordRepository.findByIdAndIsDelete(id, NO);
    }

    public void updateTransferRecord(String reson, ApplyState auditStatus, Long id) {
        TransferRecord transferRecord = transferRecordRepository.findByIdAndIsDelete(id, NO);
        ifNullThrow(transferRecord, TIP_NO_CHANSHUERROR);
        transferRecord.setRemark(reson);
        transferRecord.setReviewTime(new Date());
        transferRecord.setState(auditStatus);
        transferRecord = transferRecordRepository.save(transferRecord);
        if (auditStatus == AGREE) {
            transfer(transferRecord);
        }
    }

    /**
     * 店铺所有权转让，assignor -> receiver
     * 1.转让：店铺基础信息，岗位信息，上班的员工，店铺合作信息，转让信息，卡券信息，付出来的钱（岗位招聘的未分配的部分及押金），屏蔽（有哪些人屏蔽了不让他看简历）
     * 2.转让后，关于屏蔽的店铺管理员，所有老的店铺管理员的被屏蔽关系保留，且新的店铺管理员也要对应被屏蔽。（注意新的管理员可能已经被屏蔽了）
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    private void transfer(TransferRecord transferRecord) {
        Member assignor = transferRecord.getAssignor();
        Member receiver = memberRepository.findByMobile(transferRecord.getReceiverPhoneNumber());
        Shop shop = shopRepository.findByOwnerId(assignor.getId());
        shop.setOwner(receiver);
        shopRepository.save(shop);
        List<ResumeBlocked> resumeBlockeds = resumeBlockedService.findByResumeMemberToken(assignor.getToken());
        resumeBlockeds.forEach(resumeBlocked -> resumeBlockedService.blocked(resumeBlocked.getMember().getToken(), receiver.getMobile(), receiver.getToken()));
    }

    public Page<TransferRecord> findTransferRecordPage(Long shopId, Pageable pageable) {
        return transferRecordRepository.findByShopIdAndIsDelete(shopId, NO, pageable);
    }
}
