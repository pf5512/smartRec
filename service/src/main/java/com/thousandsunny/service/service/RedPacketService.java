package com.thousandsunny.service.service;

import com.google.common.collect.Lists;
import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.RedPacketCategory;
import com.thousandsunny.service.event.RedPacketExecutor;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.MemberRecRelRepository;
import com.thousandsunny.service.repository.MemberRegRelRepository;
import com.thousandsunny.service.repository.RedPacketReceiveRepository;
import com.thousandsunny.service.repository.RedPacketRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotEmptyThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.RedPacketState.*;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.core.ModuleKey.IdentityType.SENIOR;
import static com.thousandsunny.core.ModuleKey.IdentityType.JUNIOR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.thousandsunny.service.ModuleKey.RedPacketState;

/**
 * Created by mu.jie on 2017/2/23.
 */
@Service
public class RedPacketService extends BaseService<RedPacket> {
    @Autowired
    private RedPacketReceiveRepository redPacketReceiveRepository;
    @Autowired
    private RedPacketRepository redPacketRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RedPacketExecutor redPacketExecutor;
    @Autowired
    private MemberRegRelRepository memberRegRelRepository;
    @Autowired
    private MemberRecRelRepository memberRecRelRepository;

    public List<RedPacketReceive> findRedPacket(Member member, BooleanEnum isGetNormalState) {
        List<RedPacketReceive> list;
        if (isGetNormalState == YES) {
            list = redPacketReceiveRepository.findByMemberAndState(member, NORMAL);
        } else {
            list = redPacketReceiveRepository.findByMemberAndStateIn(member, newArrayList(EXPIRE, USED));
        }
        if (list.isEmpty()) {
            list.forEach(x -> x.setIsRead(YES));
            redPacketReceiveRepository.save(list);
        }
        return list;
    }

    public List<RedPacketReceive> findUnRedPacket(Member member) {
        List<RedPacketReceive> list = redPacketReceiveRepository.findByMemberAndStateAndIsRead(member, NORMAL, NO);
        ifNotEmptyThen(list, x -> {
            x.forEach(r -> r.setIsRead(YES));
            redPacketReceiveRepository.save(x);
        });
        return list;
    }

    public Page<RedPacket> findAllRedPacket(BackPageVo backPageVo, RedPacketCategory type) {
        if (type == null) {
            return redPacketRepository.findAll(backPageVo.pageRequest());
        } else {
            return redPacketRepository.findByCategory(type, backPageVo.pageRequest());
        }
    }

    public void updateRedPacket(RedPacket redPacket, String receiveUser, String userType) {
        List<Member> memberList = newArrayList();

        if (isNotBlank(receiveUser)) {
            final List<Member> finalMemberList1 = memberList;
            newArrayList(receiveUser.split(",")).forEach(x -> {
                Member member = memberRepository.findOne(Long.parseLong(x));
                final List<Member> finalMemberList = finalMemberList1;
                ifNotNullThen(member, finalMemberList::add);
            });
            redPacket.setMembers(memberList);
        } else {
            memberList = memberRepository.findByIsDeleteAndValid(NO, YES);
            redPacket.setMembers(memberList);
        }
        if (isNotBlank(userType)) redPacket.setSpecialTypes(userType);
        if (redPacket.getId() == null) {
            redPacket = redPacketRepository.save(redPacket);
        } else {
            RedPacket old = redPacketRepository.findOne(redPacket.getId());
            ifNotNullThen(redPacket.getAmount(), old::setAmount);
            ifNotNullThen(redPacket.getValidDate(), old::setValidDate);
            ifNotNullThen(redPacket.getMembers(), old::setMembers);
            ifNotNullThen(redPacket.getCategory(), old::setCategory);
            ifNotNullThen(redPacket.getSendType(), old::setSendType);
            ifNotNullThen(redPacket.getSpecialTypes(), old::setSpecialTypes);
            ifNotNullThen(redPacket.getStartDate(), old::setStartDate);
            ifNotNullThen(redPacket.getEndDate(), old::setEndDate);
            redPacket = redPacketRepository.save(old);
        }
        redPacketExecutor.executeRedPacket(memberList, redPacket);
    }

    public List<Member> search(String username, String userType, Date startTime, Date endTime) {
        if (isNotBlank(username)) {
            Specification<Member> spec = (rt, rq, rb) -> {
                List<Predicate> predicates = newArrayList();
                predicates.add(rb.or(
                        rb.like(rt.get("realName"), "%" + username + "%"),
                        rb.like(rt.get("hpAccount"), "%" + username + "%"),
                        rb.like(rt.get("mobile"), "%" + username + "%")
                ));
                predicates.add(rb.equal(rt.get("isDelete"), NO));
                predicates.add(rb.equal(rt.get("valid"), YES));
                return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
            };
            return memberRepository.findAll(spec);
        } else {
            if (isNotBlank(userType)) {
                Set<Member> members = new HashSet<>();
                Set<Long> memberIds = new HashSet<>();
                List<MemberRecRel> recRels = memberRecRelRepository.findAll();
                List<MemberRegRel> regRels = memberRegRelRepository.findAll();
                for (String type : userType.split(",")) {
                    if ("REGIST_USER".equals(type)) {
                        //注册用户
                        Specification<Member> spec = getMemberSpecification(startTime, endTime, "", null);
                        return memberRepository.findAll(spec);
                    } else if ("REG".equals(type)) {
                        //注册用户的推荐人
                        searchRegMember(memberIds, regRels);
                    } else if ("REC".equals(type)) {
                        //推荐用户去上班
                        searchRecMember(memberIds, recRels);
                    } else if ("RECED".equals(type)) {
                        //被推荐去上班的用户
                        searchRecedMember(memberIds, recRels);
                    } else if ("PARTNER".equals(type)) {
                        //合伙人
                        searchPartnerMember(members, startTime, endTime);
                    } else if ("JUNIOR".equals(type)) {
                        //初级创业者
                        searchJuniorMember(members, startTime, endTime);
                    } else if ("SENIOR".equals(type)) {
                        //高级创业者
                        searchSeniorMember(members, startTime, endTime);
                    }

                }
                Specification<Member> spec = getMemberSpecification(startTime, endTime, "other", memberIds);
                List<Member> members1 = memberRepository.findAll(spec);
                members.addAll(members1);
                return members.stream().collect(Collectors.toList());
            }
            return null;
        }
    }

    private Specification<Member> getMemberSpecification(Date startTime, Date endTime, String str, Set<Long> ids) {
        return (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.equal(rt.get("valid"), YES));
            if (str.equals("partner")) predicates.add(rb.equal(rt.get("partnerLevel"), YES));
            if (str.equals("junior")) predicates.add(rb.equal(rt.get("entrepreneurLevel"), JUNIOR));
            if (str.equals("senior")) predicates.add(rb.equal(rt.get("entrepreneurLevel"), SENIOR));
            if (ids != null && !ids.isEmpty() && str.equals("other")) predicates.add(rt.get("id").in(ids));
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("createTime"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("createTime"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
    }

    //注册用户的推荐人
    private Set<Long> searchRegMember(Set<Long> memberIds, List<MemberRegRel> regRels) {
        regRels.forEach(reg -> {
            if (reg.getP1() != null && reg.getP2() == null && reg.getP3() == null)
                memberIds.add(reg.getMember().getId());
            if (reg.getP2() != null && reg.getP3() == null) {
                ifNotNullThen(reg.getP1(), x -> memberIds.add(x));
                ifNotNullThen(reg.getMember(), x -> memberIds.add(x.getId()));
            }
            if (reg.getP3() != null) {
                ifNotNullThen(reg.getP2(), x -> memberIds.add(reg.getP2()));
                ifNotNullThen(reg.getP1(), x -> memberIds.add(x));
                ifNotNullThen(reg.getMember(), x -> memberIds.add(x.getId()));
            }
        });
        return memberIds;
    }

    //推荐用户去上班
    private Set<Long> searchRecMember(Set<Long> memberIds, List<MemberRecRel> recRels) {
        recRels.forEach(rec -> {
            if (rec.getP1() != null && rec.getP2() == null && rec.getP3() == null)
                memberIds.add(rec.getMember().getId());
            if (rec.getP2() != null && rec.getP3() == null) {
                ifNotNullThen(rec.getP1(), x -> memberIds.add(x));
                ifNotNullThen(rec.getMember(), x -> memberIds.add(x.getId()));
            }
            if (rec.getP3() != null) {
                ifNotNullThen(rec.getP1(), x -> memberIds.add(x));
                ifNotNullThen(rec.getP2(), x -> memberIds.add(x));
                ifNotNullThen(rec.getMember(), x -> memberIds.add(x.getId()));
            }
        });
        return memberIds;
    }

    //被推荐去上班的用户
    private Set<Long> searchRecedMember(Set<Long> memberIds, List<MemberRecRel> recRels) {
        recRels.forEach(rec -> {
            if (rec.getP1() != null && rec.getP2() == null && rec.getP3() == null) {
                memberIds.add(rec.getP1());
            }
            if (rec.getP2() != null && rec.getP3() == null) {
                memberIds.add(rec.getP2());
            }
            if (rec.getP3() != null) {
                memberIds.add(rec.getP3());
            }
        });
        return memberIds;
    }

    //合伙人
    private Set<Member> searchPartnerMember(Set<Member> members, Date startTime, Date endTime) {
        Specification<Member> spec = getMemberSpecification(startTime, endTime, "partner", null);
        List<Member> list = memberRepository.findAll(spec);
        members.addAll(list);
        return members;
    }

    //初级创业者
    private Set<Member> searchJuniorMember(Set<Member> members, Date startTime, Date endTime) {
        Specification<Member> spec = getMemberSpecification(startTime, endTime, "junior", null);
        List<Member> list = memberRepository.findAll(spec);
        members.addAll(list);
        return members;
    }

    //高级创业者
    private Set<Member> searchSeniorMember(Set<Member> members, Date startTime, Date endTime) {
        Specification<Member> spec = getMemberSpecification(startTime, endTime, "senior", null);
        List<Member> list = memberRepository.findAll(spec);
        members.addAll(list);
        return members;
    }

    public Page<RedPacketReceive> findRedPacketReceives(Pageable pageable, RedPacketCategory type, RedPacketState state, Member member) {
        Specification<RedPacketReceive> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            ifNotNullThen(type, e -> predicates.add(rb.equal(rt.get("redPacket").get("category"), e)));
            ifNotNullThen(state, e -> predicates.add(rb.equal(rt.get("state"), e)));
            predicates.add(rb.equal(rt.get("member"), member));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return redPacketReceiveRepository.findAll(spec, pageable);
    }

    public RedPacketReceive findMyRedPacket(Long id) {
        return redPacketReceiveRepository.findOne(id);
    }

    public Long countAllMember() {
        return memberRepository.countByIsDeleteAndValid(NO, YES);
    }

    public String findReceivesString(RedPacket redPacket) {
        StringBuffer sb = new StringBuffer();
        List<RedPacketReceive> list = redPacketReceiveRepository.findByRedPacketIdAndState(redPacket.getId(), NORMAL);
        if (!list.isEmpty()) {
            list.forEach(x -> sb.append(x.getMember().getId()).append(","));
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : null;
    }

    public Long countMyRedPacket(Member member) {
        return redPacketReceiveRepository.countByMemberIdAndStateAndValidDateGreaterThan(member.getId(), NORMAL, new Date());
    }
}
