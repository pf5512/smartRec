package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Complain;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.repository.ComplainRepository;
import com.thousandsunny.service.repository.SchoolRepository;
import com.thousandsunny.service.repository.ShopRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleKey.ComplainType;
import static com.thousandsunny.service.ModuleKey.ComplainType.*;
import static com.thousandsunny.service.ModuleTips.TIP_PARAM_FALSE;
import static com.thousandsunny.service.ModuleTips.TIP_NO_SCHOOL;
import static com.thousandsunny.service.ModuleTips.TIP_SHOP_NOT_EXIST;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_MEMBER_NOT_EXISTED;
import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by admin on 2016/11/7.
 */
@Service
public class ComplainService extends BaseService<Complain> {
    @Autowired
    private ComplainRepository complainRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private ShopRepository shopRepository;

    public Complain save(String userToken, String complaintedUserToken, Long complaintedSchoolId, Long complaintedStoreId, ComplainType type, String reasons) {
        ifFalseThrow(checkParam(complaintedUserToken, complaintedSchoolId, complaintedStoreId, type), TIP_PARAM_FALSE);
        Member member = memberRepository.findByToken(userToken);
        Complain complain = new Complain();
        if (isNotBlank(complaintedUserToken)) {
            Member compMember = memberRepository.findByToken(complaintedUserToken);
            ifNullThrow(compMember, TIP_MEMBER_NOT_EXISTED);
            complain.setDefendant(compMember);
        }
        if (isNotNull(complaintedSchoolId)) {
            School school = schoolRepository.findOne(complaintedSchoolId);
            ifNullThrow(school, TIP_NO_SCHOOL);
            complain.setSchool(school);
        }
        if (isNotNull(complaintedStoreId)) {
            Shop shop = shopRepository.findOne(complaintedStoreId);
            ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
            complain.setShop(shop);
        }
        complain.setComplainant(member);
        complain.setDate(new Date());
        complain.setReason(reasons);
        complain.setType(type);
        return complainRepository.save(complain);
    }

    private Boolean checkParam(String complaintedUserToken, Long complaintedSchoolId, Long complaintedStoreId, ComplainType type) {
        if (type == COMPLAINT_TO_USER || type == STORE_COMPLAINT_TO_USER_UNCONFIRM_RESIGN) {
            if (isBlank(complaintedUserToken)) return false;
        }
        if (type == COMPLAINT_TO_SCHOOL) {
            if (complaintedSchoolId == null) return false;
        }
        if (type == COMPLAINT_TO_STORE || type == USER_COMPLAINT_TO_STORE_UNCONFIRM_WORK || type == ENTREPRENEUR_COMPLAINT_WORK || type == PARTNER_COMPLAINT_WORK) {
            if (complaintedStoreId == null) return false;
        }
        return true;
    }

    public Page<Complain> findComplainList(BackPageVo backPageVo, String text, BooleanEnum tableType, ComplainType complainType) {
        Specification<Complain> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDeal"), tableType));
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("complainant").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("complainant").get("hpAccount"), "%" + t + "%"))));
            ifNotNullThen(complainType, t -> predicates.add(rb.equal(rt.get("type"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return complainRepository.findAll(spec, backPageVo.pageRequest());
    }

    public void delComplainList(String id) {
        ifNotBlankThen(id, x -> newArrayList(id.split(",")).forEach(idStr -> {
            Complain one = complainRepository.findOne(parseLong(idStr));
            complainRepository.delete(one);
        }));
    }

    public void updateComplain(Long id, BooleanEnum complainStatus, String opinion) {
        Complain one = complainRepository.findOne(id);
        one.setIsDeal(complainStatus);
        one.setOpinion(opinion);
        complainRepository.save(one);
    }

    public Page<Complain> findComplainList(BackPageVo backPageVo, ComplainType complainType, Long userId) {
        Specification<Complain> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("complainant").get("id"), userId));
            ifNotNullThen(complainType, t -> predicates.add(rb.equal(rt.get("type"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return complainRepository.findAll(spec, backPageVo.pageRequest());
    }

    public Long countNontDealComplain(Date startTime, Date endTime) {
        Specification<Complain> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDeal"), NO));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return complainRepository.count(specification);
    }
}
