package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPage;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Resume;
import com.thousandsunny.service.model.ResumeLook;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.repository.ResumeLookRepository;
import com.thousandsunny.service.repository.ResumeRepository;
import com.thousandsunny.service.repository.ShopRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static java.util.Objects.isNull;
import static com.thousandsunny.service.ModuleTips.*;

/**
 * Created by admin on 2016/10/27.
 */
@Service
public class ResumeLookService extends BaseService<ResumeLook> {
    @Autowired
    private ResumeLookRepository resumeLookRepository;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private MemberRepository memberRepository;

    public ResumeLook firstStoreName(String userToken) {
        return resumeLookRepository.findTop1ByResumeMemberTokenOrderByDateDesc(userToken);

    }


    public Long countResumeVisitor(String userToken) {
        return resumeLookRepository.countByResumeMemberTokenAndIsRead(userToken, NO);

    }

    public void readResumeLookers(String userToken) {
        List<ResumeLook> resumeLooks = resumeLookRepository.findByResumeMemberTokenAndIsReadOrderByDate(userToken, NO);
        for (ResumeLook resumeLook : resumeLooks) {
            resumeLook.setIsRead(YES);
            resumeLookRepository.save(resumeLook);
        }

    }


    public Page<ResumeLook> resumeLookers(String userToken, Pageable pageable) {
        return resumeLookRepository.findByResumeMemberTokenOrderByDateDesc(userToken, pageable);
    }


    /**
     * 刷新查看记录
     */
    public void refreshTrace(String userToken, String checkedUserToken) {
        if (!checkedUserToken.equals(userToken)) {
            ResumeLook resumeLook = resumeLookRepository.findByShopOwnerTokenAndResumeMemberToken(userToken, checkedUserToken);
            if (isNull(resumeLook)) {
                resumeLook = new ResumeLook();
                Shop shop = shopRepository.findByOwnerToken(userToken);
                if (!isNull(shop)) {
                    Resume resume = resumeRepository.findByMemberToken(checkedUserToken);
                    resumeLook.setDate(new Date());
                    resumeLook.setIsRead(NO);
                    resumeLook.setShop(shop);
                    resumeLook.setResume(resume);
                    resumeLookRepository.save(resumeLook);
                }
            } else {
                resumeLook.setDate(new Date());
                resumeLook.setIsRead(NO);
                resumeLookRepository.save(resumeLook);
            }
        }
    }

    /**
     * 谁看过我的简历
     *
     * @Author xiao xue wei
     * @Date 2016/12/20
     */
    public Page<ResumeLook> whoLookHim(BackPageVo page, String text, String visitTime, Long userId) {
        Member member = memberRepository.findOne(userId);
        ifNullThrow(member, TIP_NO_MEMBER);
        Specification<ResumeLook> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("resume").get("member").get("id"), userId));
            ifNotBlankThen(text, e -> {
                String textStr = "%" + e + "%";
                rb.or(rb.like(rt.get("shop").get("name"), textStr),
                        rb.like(rt.get("shop").get("owner").get("realName"), textStr),
                        rb.like(rt.get("shop").get("owner").get("mobile"), textStr));
            });
            ifNotBlankThen(visitTime, e -> {
                String visitTimeStart = e + " 00:00:00";
                String visitTimeEnd = e + " 23:59:59";
                Date startTime = null;
                Date endTime = null;
                try {
                    startTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(visitTimeStart);
                    endTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(visitTimeEnd);
                    predicates.add(rb.greaterThan(rt.get("date"), startTime));
                    predicates.add(rb.lessThan(rt.get("date"), endTime));
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            });
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return resumeLookRepository.findAll(specification, page.pageRequest());
    }
}
