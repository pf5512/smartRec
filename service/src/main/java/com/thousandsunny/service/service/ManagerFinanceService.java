package com.thousandsunny.service.service;

import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.service.ModuleKey.EntrepreneursType;
import static com.thousandsunny.thirdparty.ModuleKey.*;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;

/**
 * Created by admin on 2016/11/24.
 */
@Service
public class ManagerFinanceService {
    @Autowired
    private AccountFlowRepository accountFlowRepository;

    /**
     * 付款流水列表
     */
    public Page<AccountFlow> applyFlows(Pageable pageRequest, String text, FlowState tableType, Date startTime, Date endTime, PayType payWay, SourceType sourceType, EntrepreneursType ESTPType) {
        Specification<AccountFlow> specification = getAccountFlowSpecification(text, tableType, startTime, endTime, payWay, sourceType, ESTPType);
        return accountFlowRepository.findAll(specification, pageRequest);
    }

    /**
     * 9.2.2 已付款统计(创业者)
     */
    public BigDecimal countPays(String text, Date startTime, Date endTime, PayType payWay, SourceType sourceType, EntrepreneursType ESTPType) {
        Specification<AccountFlow> specification = getAccountFlowSpecification(text, SUCCESS, startTime, endTime, payWay, sourceType, ESTPType);
        List<AccountFlow> accountFlows = accountFlowRepository.findAll(specification);
        BigDecimal income = new BigDecimal(0);
        accountFlows.forEach(e -> income.add(e.getAmount()));
        return income;
    }

    private Specification<AccountFlow> getAccountFlowSpecification(String text, FlowState tableType,
                                                                   Date startTime, Date endTime, PayType payWay,
                                                                   SourceType sourceType, EntrepreneursType ESTPType) {
        return (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("state"), tableType));
            predicates.add(rb.equal(rt.get("source"), sourceType));
            ifNotNullThen(ESTPType, e -> predicates.add(rb.equal(rt.get("entrepreneursApply").get("type"), e)));
            ifNotNullThen(payWay, e -> predicates.add(rb.equal(rt.get("payType"), e)));
            ifNotNullThen(startTime, e -> predicates.add(rb.lessThan(rt.get("updateDate"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.greaterThan(rt.get("updateDate"), e)));
            ifNotNullThen(text, e -> predicates.add(rb.or(rb.like(rt.get("amount").get("member").get("realName"), "%" + e + "%"),
                    rb.like(rt.get("orderNo"), "%" + e + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
    }
}
