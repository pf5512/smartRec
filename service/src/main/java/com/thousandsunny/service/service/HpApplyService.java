package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.ModuleKey.ApplyType;
import com.thousandsunny.service.ModuleKey.ApplyEnum;
import com.thousandsunny.service.model.HpApply;
import com.thousandsunny.service.repository.HpApplyRepository;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.*;

/**
 * Created by mu.jie on 2016/12/5.
 */
@Service
public class HpApplyService extends BaseService<HpApply> {
    @Autowired
    private HpApplyRepository hpApplyRepository;
    @Autowired
    private JobService jobService;

    private List<ApplyEnum> applyEnumList = newArrayList(REVIEW_SUCCESS, REVIEW_FAILED);

    public Page<HpApply> findHpApplyList(BackPageVo backPageVo, String text, String tableType, Date startTime, Date endTime, ApplyEnum refundStatus) {
        Specification<HpApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if ("APPLOVAL".equals(tableType)) {
                predicates.add(rb.equal(rt.get("state"), IN_REVIEW));
            } else if ("SUCCESS".equals(tableType)) {
                predicates.add(rt.get("state").in(applyEnumList));
            }
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("job").get("name"), "%" + t + "%"),
                    rb.like(rt.get("job").get("shop").get("name"), "%" + t + "%"))));
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("date"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("date"), t)));
            ifNotNullThen(refundStatus, t -> predicates.add(rb.equal(rt.get("state"), refundStatus)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return hpApplyRepository.findAll(spec, backPageVo.pageRequest());
    }

    public void updateHpApplyList(Long id, ApplyEnum auditState, String reson) {
        OperatorType type = auditState == REVIEW_SUCCESS ? SURE : CANCEL;
        HpApply hpApply = jobService.handle(id, type);
        hpApply.setRemark(reson);
        hpApplyRepository.save(hpApply);
    }

    public BigDecimal countHpApply(String text, Date startTime, Date endTime, ApplyEnum refundStatus, ApplyType type) {
        StringBuffer sql = new StringBuffer();
        sql.append("select  SUM(hpapply0_.money) as col_0_0_  from   sr_hp_apply hpapply0_ cross join  sr_job job1_ cross " +
                " join  sr_shop shop3_  where   hpapply0_.job_id=job1_.id  and job1_.shop_id=shop3_.id ");
        sql.append(" and (hpapply0_.state in ('REVIEW_SUCCESS', 'REVIEW_FAILED')) ");
        sql.append(" and hpapply0_.type = '" + type + "'");
        ifNotBlankThen(text, t -> sql.append(" and (job1_.name like '%" + t + "%'  or shop3_.name like '%" + t + "%'  ) "));
        ifNotNullThen(startTime, t -> sql.append(" and hpapply0_.date>" + t));
        ifNotNullThen(endTime, t -> sql.append(" and hpapply0_.date<" + t));
        ifNotNullThen(refundStatus, t -> sql.append(" and hpapply0_.state='" + t + "'"));
        Query query = entityManager.createNativeQuery(sql.toString());
        BigDecimal singleResult = (BigDecimal) query.getSingleResult();
        return singleResult;
    }
}
