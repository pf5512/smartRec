package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.PartnerApply;
import com.thousandsunny.service.repository.PartnerApplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;

/**
 * 如果这些代码有用，那它们是guitarist在12/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class PartnerApplyService extends BaseService<PartnerApply> {
    @Autowired
    private PartnerApplyRepository partnerApplyRepository;
    @Autowired
    private MemberRepository memberRespository;

    public PartnerApply findByProvinceIdAndCityIdAndAreaId(Long id1, Long id2, Long id3) {
        return partnerApplyRepository.findByProvinceIdAndCityIdAndAreaId(id1, id2, id3);
    }

    public PartnerApply findByProvinceIdAndCityIdAndAreaIdAndStateIn(Long id1, Long id2, Long id3, List<ApplyEnum> list) {
        return partnerApplyRepository.findByProvinceIdAndCityIdAndAreaIdAndStateIn(id1, id2, id3, list);
    }

    public List<PartnerApply> findByMemberTokenOrderByDate(String userToken) {
        return partnerApplyRepository.findByMemberTokenOrderByDate(userToken);
    }

    public PartnerApply findByMemberTokenAndState(String userToken, ApplyEnum state) {
        return partnerApplyRepository.findByMemberTokenAndState(userToken, state);
    }

    public List<PartnerApply> findByStateIn(List<ApplyEnum> enums) {
        return partnerApplyRepository.findByStateIn(enums);
    }

    public JSONObject countPartnerInfo(Date startTime, Date endTime) {
        JSONObject jo = new JSONObject();
        Specification<PartnerApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<PartnerApply> list = partnerApplyRepository.findAll(specification);
        Set<Long> applyMemberIds = new HashSet<>();
        Set<Long> applySuccessMemberIds = new HashSet<>();
        Set<Long> partnerMemberIds = new HashSet<>();
        if (!list.isEmpty()) {
            list.forEach(e -> {
                applyMemberIds.add(e.getMember().getId());
                if (e.getState() == REVIEW_SUCCESS || e.getState() == OFFLINE_PAY_CONFIRM || e.getState() == SUCCESS)
                    applySuccessMemberIds.add(e.getMember().getId());
                if (e.getState() == SUCCESS)
                    partnerMemberIds.add(e.getMember().getId());
            });
            jo.put("applyUserNum", applyMemberIds.size());
            jo.put("audited", applySuccessMemberIds.size());
            jo.put("isCyz", partnerMemberIds.size());
        } else {
            jo.put("applyUserNum", 0);
            jo.put("audited", 0);
            jo.put("isCyz", 0);
        }
        return jo;
    }

    public Long countApplyingPartner(Date startTime, Date endTime) {
        Specification<PartnerApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            predicates.add(rb.equal(rt.get("state"), IN_REVIEW));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return partnerApplyRepository.count(specification);
    }
}
