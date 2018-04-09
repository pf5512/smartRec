package com.thousandsunny.service.service;

import com.thousandsunny.core.ModuleKey.ApplyState;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.SchoolApply;
import com.thousandsunny.service.repository.SchoolApplyRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.ApplyState.*;
import static com.thousandsunny.service.ModuleTips.TIP_CAN_NOT_REVIEW;
import static com.thousandsunny.service.ModuleTips.TIP_NO_SCHOOLAPPLY;
import static com.thousandsunny.service.ModuleTips.TIP_SCHOOL_APPLY_EXIST;

/**
 * Created by Xiao Xuewei on 2017/2/14.
 */
@Service
public class SchoolApplyService extends BaseService<SchoolApply> {
    @Autowired
    private CloudFileRepository cloudFileRepository;
    @Autowired
    private SchoolApplyRepository schoolApplyRepository;

    public void saveSchoolApply(Member member, SchoolApply schoolApply) {
        SchoolApply dbSchoolApply = schoolApplyRepository.findByMemberId(member.getId());
        ifNotNullThrow(dbSchoolApply, TIP_SCHOOL_APPLY_EXIST);
        schoolApply.setMember(member);
        schoolApply.setState(APPROVAL);
        ifNotNullThen(schoolApply.getPhotos(), photos -> schoolApply.setPhotos(cloudFileRepository.save(photos)));
        save(schoolApply);
    }

    /**
     * 后台 3.1.1
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    public Page<SchoolApply> findSchoolApplyPage(Pageable pageable, String tableType, String text, Long provinceId, Long cityId, Long areaId) {
        Specification<SchoolApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            List<ApplyState> reviewedList = newArrayList(AGREE, REJECT);
            if ("in_review".equals(tableType)) predicates.add(rb.equal(rt.get("state"), APPROVAL));
            else if ("has_review".equals(tableType)) predicates.add(rt.get("state").in(reviewedList));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("area").get("id"), e)));
            ifNotBlankThen(text, e -> predicates.add(rb.or(rb.like(rt.get("name"), "%" + e + "%"),
                    rb.like(rt.get("member").get("realName"), "%" + e + "%"),
                    rb.like(rt.get("member").get("hpAccount"), "%" + e + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return schoolApplyRepository.findAll(spec, pageable);
    }

    /**
     * 后台3.1.3
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public void auditSchoolApply(Long id, ApplyState auditStatus, String reason) {
        SchoolApply schoolApply = schoolApplyRepository.findOne(id);
        ifNullThrow(schoolApply, TIP_NO_SCHOOLAPPLY);
        ifFalseThrow(schoolApply.getState() == APPROVAL, TIP_CAN_NOT_REVIEW);
        schoolApply.setState(auditStatus);
        schoolApply.setRemark(reason);
        schoolApplyRepository.save(schoolApply);
    }

    public Long countApplyingSchool(Date startTime, Date endTime) {
        Specification<SchoolApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createTime"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createTime"), e)));
            predicates.add(rb.equal(rt.get("state"), APPROVAL));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return schoolApplyRepository.count(specification);
    }

    public Long countSchoolApplyInfo(Date startTime, Date endTime) {
        Specification<SchoolApply> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createTime"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createTime"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return schoolApplyRepository.count(specification);
    }
}
