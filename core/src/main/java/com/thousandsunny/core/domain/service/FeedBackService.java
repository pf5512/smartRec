package com.thousandsunny.core.domain.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.FeedBackRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.model.FeedBack;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * 如果这些代码有用，那它们是guitarist在9/22/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class FeedBackService extends BaseService<FeedBack> {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FeedBackRepository feedBackRepository;

    public FeedBack save(FeedBack feedBack, String token) {
        feedBack.setMember(memberRepository.findByToken(token));
        return feedBackRepository.save(feedBack);
    }

    public Page<FeedBack> findFeedBackList(BackPageVo backPageVo, String text, BooleanEnum tableType) {
        Specification<FeedBack> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDeal"), tableType));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("member").get("hpAccount"), "%" + t + "%"),
                    rb.like(rt.get("content"), "%" + t + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };

        return feedBackRepository.findAll(spec, backPageVo.pageRequest());
    }

    public void delFeedBack(String ids) {
        ifNotBlankThen(ids, x -> newArrayList(ids.split(",")).forEach(idStr -> {
            FeedBack feedBack = feedBackRepository.findOne(Long.parseLong(idStr));
            feedBack.setIsDelete(YES);
            feedBackRepository.save(feedBack);
        }));
    }

    public void updateFeedBack(Long id, BooleanEnum isDeal, String reson) {
        FeedBack feeBack = feedBackRepository.findOne(id);
        feeBack.setOpinion(reson);
        feeBack.setIsDeal(isDeal);
        feedBackRepository.save(feeBack);
    }

    public Page<FeedBack> findFeedBackPage(BackPageVo pageVo, String text, Long userId) {
        Specification<FeedBack> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(text, t -> {
                String textStr = "%" + t + "%";
                predicates.add(rb.like(rt.get("content"), textStr));
            });
            predicates.add(rb.equal(rt.get("member").get("id"), userId));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };

        return feedBackRepository.findAll(spec, pageVo.pageRequest());
    }

    public Long countNotDealFeedBack(Date startTime, Date endTime) {
        Specification<FeedBack> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.equal(rt.get("isDeal"), NO));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return feedBackRepository.count(specification);
    }
}
