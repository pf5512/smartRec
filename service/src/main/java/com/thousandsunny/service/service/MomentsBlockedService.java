package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.MomentsBlocked;
import com.thousandsunny.service.model.ResumeBlocked;
import com.thousandsunny.service.repository.MomentsBlockedRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifFalseThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleTips.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by admin on 2016/10/25.
 */
@Service
public class MomentsBlockedService extends BaseService<MomentsBlocked> {


    @Autowired
    private MomentsBlockedRepository momentsBlockedRepository;

    @Autowired
    private MemberRepository memberRepository;

    public Page<MomentsBlocked> findByUserToken(String userToken, PageRequest pageRequest) {
        return momentsBlockedRepository.findByMemberToken(userToken, pageRequest);
    }

    public void blocked(String userToken, String invitedUserToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifTrueThrow(member == null, TIP_NO_MEMBER);
        MomentsBlocked momentsBlocked = new MomentsBlocked();
        Member momentsMember = null;
        if (isNotBlank(invitedUserToken)) {
            ifTrueThrow(momentsBlockedRepository.findByMemberTokenAndMomentsMemberToken(userToken, invitedUserToken) != null, TIP_YES_BLOCKED);
            momentsMember = memberRepository.findByTokenAndIsDelete(invitedUserToken, NO);
            ifTrueThrow(momentsMember == null, TIP_NO_MEMBER);
        }
        momentsBlocked.setMember(member);
        momentsBlocked.setMomentsMember(momentsMember);
        momentsBlockedRepository.save(momentsBlocked);
    }


    public void cancelBlocked(String userToken,String invitedUserToken) {

        MomentsBlocked momentsBlocked = momentsBlockedRepository.findByMemberTokenAndMomentsMemberToken(userToken,invitedUserToken);
        ifTrueThrow(momentsBlocked == null, TIP_NO_BLOCKED);
        momentsBlockedRepository.delete(momentsBlocked);
    }

    public boolean findByMemberTokenAndMomentsMemberToken(String userToken, String invitedUserToken) {
        MomentsBlocked momentsBlocked = momentsBlockedRepository.findByMemberTokenAndMomentsMemberToken(userToken, invitedUserToken);
        if(momentsBlocked==null){
            return false;
        }
        return true;
    }

    public Page<MomentsBlocked> findMBPage(BackPageVo pageVo, String text, String userToken){
        Specification<MomentsBlocked> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotBlankThen(text, e -> {
                String textStr = "%" + text + "%";
                predicates.add(rb.or(rb.like(rt.get("momentsMember").get("realName"), textStr),
                        rb.like(rt.get("momentsMember").get("mobile"), textStr),
                        rb.like(rt.get("momentsMember").get("hpAccount"), textStr)));
            });
            predicates.add(rb.equal(rt.get("member").get("token"), userToken));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return momentsBlockedRepository.findAll(spec, pageVo.pageRequest());
    }

}
