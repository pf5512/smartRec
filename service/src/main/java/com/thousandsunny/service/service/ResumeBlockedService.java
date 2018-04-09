package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Job;
import com.thousandsunny.service.model.JobBlocked;
import com.thousandsunny.service.model.ResumeBlocked;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.repository.JobBlockedRepository;
import com.thousandsunny.service.repository.JobRepository;
import com.thousandsunny.service.repository.ResumeBlockedRepository;
import com.thousandsunny.service.repository.ShopRepository;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.TIP_NO_BLOCKED;
import static com.thousandsunny.service.ModuleTips.TIP_YES_BLOCKED;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;


@Service
public class ResumeBlockedService extends BaseService<ResumeBlocked> {
    @Autowired
    private ResumeBlockedRepository resumeBlockedRepository;
    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobBlockedRepository jobBlockedRepository;


    /**
     * 屏蔽查看简历的用户列表
     */
    public List<ResumeBlocked> blockedMember(String userToken) {
        return resumeBlockedRepository.findByMemberToken(userToken);
    }

    /**
     * 新增屏蔽店铺
     */
    public Shop blocked(String userToken, String phoneNumber, String invitedUserToken) {
        ResumeBlocked resumeBlocked = new ResumeBlocked();
        Member resumeMember;
        if (invitedUserToken != null && invitedUserToken.trim().length() > 0) {
            resumeMember = memberService.findByToken(invitedUserToken);
            ifNotNullThrow(resumeBlockedRepository.findByMemberTokenAndResumeMemberToken(userToken, invitedUserToken), TIP_YES_BLOCKED);

        } else {
            resumeMember = memberService.findByMobile(phoneNumber);
            ifNotNullThrow(resumeBlockedRepository.findByMemberTokenAndResumeMemberMobile(userToken, phoneNumber), TIP_YES_BLOCKED);
        }
        Member member = memberService.findByToken(userToken);
        resumeBlocked.setMember(member);
        resumeBlocked.setResumeMember(resumeMember);
        resumeBlockedRepository.save(resumeBlocked);
        Shop shop = shopRepository.findByOwnerToken(resumeMember.getToken());
        List<Job> jobs = jobRepository.findByShopAndIsDeleteAndIsEnable(shop, NO, YES);
        for (Job job : jobs) {
            JobBlocked jobBlocked = jobBlockedRepository.findByMemberTokenAndJobId(userToken, job.getId());
            if (jobBlocked == null) {
                JobBlocked jo = new JobBlocked();
                jo.setMember(member);
                jo.setJob(job);
                jo.setDate(new Date());
                jobBlockedRepository.save(jo);
            }
        }

        return shop;
    }


    /**
     * 取消屏蔽店铺
     */
    public void cancelBlocked(String userToken, Long id, String invitedUserToken) {
        ResumeBlocked resumeBlocked;
        if (id != null)
            resumeBlocked = resumeBlockedRepository.findOne(id);
        else
            resumeBlocked = resumeBlockedRepository.findByMemberTokenAndResumeMemberToken(userToken, invitedUserToken);
        ifTrueThrow(resumeBlocked == null, TIP_NO_BLOCKED);
        Shop shop = shopRepository.findByOwnerToken(resumeBlocked.getResumeMember().getToken());
        List<Job> jobs = jobRepository.findByShopAndIsDeleteAndIsEnable(shop, NO, YES);
        ifNotNullThen(jobs, j -> j.forEach(t -> {
            JobBlocked jobBlocked = jobBlockedRepository.findByMemberTokenAndJobId(userToken, t.getId());
            jobBlockedRepository.delete(jobBlocked);
        }));
        resumeBlockedRepository.delete(resumeBlocked);
    }

    public Boolean isABlockedB(String memberA, String memberB) {
        return resumeBlockedRepository.findByMemberTokenAndResumeMemberToken(memberA, memberB) != null;
    }

    public Page<ResumeBlocked> findRBPage(BackPageVo pageVo, String text, String userToken) {
        Specification<ResumeBlocked> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotBlankThen(text, e -> {
                String textStr = "%" + text + "%";
                predicates.add(rb.or(rb.like(rt.get("resumeMember").get("realName"), textStr),
                        rb.like(rt.get("resumeMember").get("mobile"), textStr),
                        rb.like(rt.get("resumeMember").get("hpAccount"), textStr)));
            });
            predicates.add(rb.equal(rt.get("member").get("token"), userToken));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return resumeBlockedRepository.findAll(spec, pageVo.pageRequest());
    }

    public List<ResumeBlocked> findByResumeMemberToken(String userToken){
        return resumeBlockedRepository.findByResumeMemberToken(userToken);
    }

}
