package com.thousandsunny.service.service;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.ModuleKey.SubLevelType;
import com.thousandsunny.core.domain.repository.MemberExtInfoRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.JobApplyRecord;
import com.thousandsunny.service.model.MemberRecRel;
import com.thousandsunny.service.model.MemberRegRel;
import com.thousandsunny.service.repository.JobApplyRecordRepository;
import com.thousandsunny.service.repository.MemberRecRelRepository;
import com.thousandsunny.service.repository.MemberRegRelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.SubLevelType.*;
import static com.thousandsunny.service.ModuleKey.RecState.WORKING;
import static com.thousandsunny.service.ModuleTips.TIP_NO_CHANSHUERROR;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Created by admin on 2016/10/20.
 */
@Service
public class MemberRecRelService extends BaseService<MemberRecRel> {
    @Autowired
    private MemberRecRelRepository memberRecRelRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JobApplyRecordRepository jobApplyRecordRepository;
    @Autowired
    private MemberRegRelRepository memberRegRelRepository;

    /**
     * 好友列表
     */
    public Page<Member> friendsList(String userToken, SubLevelType subLevel, Pageable pageable) {

        Page<Member> memberPage = memberRepository.findByIdIn(getMemberIds(userToken, subLevel), pageable);
        ifFalseThrow(memberPage != null, TIP_NO_CHANSHUERROR);
        return memberPage;
    }


    /**
     * ,好友搜索
     */
    public Page<Member> search(String userToken, String keyword, SubLevelType subLevel, Pageable pageable) {
        Page<Member> memberPage;
        if (keyword != null && keyword.trim().length() > 0) {
            memberPage = memberRepository.findByIdInAndRealNameContaining(getMemberIds(userToken, subLevel), keyword, pageable);
        } else {
            memberPage = memberRepository.findByIdIn(getMemberIds(userToken, subLevel), pageable);
        }
        ifFalseThrow(memberPage != null, TIP_NO_CHANSHUERROR);
        return memberPage;

    }

    /**
     * 对应级别的好友关系,推荐
     */
    public Set<Long> getMemberIds(String userToken, ModuleKey.SubLevelType subLevel) {
        Set<Long> meberIds = new HashSet<>();
        Long mId = memberRepository.findByToken(userToken).getId();

        List<MemberRecRel> friendsList;
        if (subLevel == SUB_LEVEL_ONE) {
            friendsList = memberRecRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
            for (MemberRecRel memberRecRel : friendsList) {
                if (mId.equals(memberRecRel.getP2()) && memberRecRel.getP3() != null)
                    meberIds.add(memberRecRel.getP3());
                else if (mId.equals(memberRecRel.getP1()) && memberRecRel.getP2() != null)
                    meberIds.add(memberRecRel.getP2());
                else if (mId.equals(memberRecRel.getMember().getId()))
                    meberIds.add(memberRecRel.getP1());
            }
        }

        if (subLevel == SUB_LEVEL_TWO) {
            friendsList = memberRecRelRepository.findByMemberTokenOrP1(userToken, mId);
            for (MemberRecRel memberRecRel : friendsList) {
                if (mId.equals(memberRecRel.getP1()) && memberRecRel.getP3() != null)
                    meberIds.add(memberRecRel.getP3());
                else if (mId.equals(memberRecRel.getMember().getId()) && memberRecRel.getP2() != null)
                    meberIds.add(memberRecRel.getP2());
            }
        }

        if (subLevel == SUB_LEVEL_THREE) {
            friendsList = memberRecRelRepository.findByMemberToken(userToken);
            for (MemberRecRel memberRecRel : friendsList) {
                if (memberRecRel.getP3() != null) meberIds.add(memberRecRel.getP3());
            }
        }
        return meberIds;
    }

    /**
     * 推荐关系级别
     */

    public SubLevelType recRelLevel(String userToken, String regUserToken) {
        Long cId = memberRepository.findByTokenAndIsDelete(regUserToken, NO).getId();
        PageRequest pageRequest = new PageRequest(0, 1, new Sort(DESC, "date"));
        Page<MemberRecRel> page = memberRecRelRepository.findTop1ByMemberTokenOrP1OrP2OrP3OrderByDate(userToken, cId, cId, cId, pageRequest);
//        ifEmptyThrow(page.getContent(), TIP_NO_REC_REL);
        if (isEmpty(page.getContent())) {
            return null;
        }
        MemberRecRel recRel = page.getContent().get(0);
        if (cId.equals(recRel.getP1()))
            return SUB_LEVEL_ONE;
        else if (cId.equals(recRel.getP2()))
            return SUB_LEVEL_TWO;
        else
            return SUB_LEVEL_THREE;
    }


    /**
     * 所有推荐好友列表id
     */
    Set<Long> friendsList(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        Set<Long> meberIds = new HashSet<>();
        meberIds = allIds(member, meberIds);
        return meberIds;
    }


    /**
     * 总好友列表id（推荐1-3级+推荐1-3级列表里的用户的注册1-3级和推荐1-3级别）
     */
    public Set<Long> allFriendsList(String userToken) {
        Set<Long> meberIds = new HashSet<>();
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<MemberRecRel> friendsList = memberRecRelRepository.findByMemberTokenOrP1OrP2(userToken, member.getId(), member.getId());
        for (MemberRecRel m : friendsList) {
            if (m.getP1() != null) {
                meberIds.add(m.getP1());
                allIds(memberRepository.findOne(m.getP1()), meberIds);
            }
            if (m.getP2() != null) {
                meberIds.add(m.getP2());
                allIds(memberRepository.findOne(m.getP2()), meberIds);
            }
            if (m.getP3() != null) {
                meberIds.add(m.getP3());
                allIds(memberRepository.findOne(m.getP3()), meberIds);
            }
        }

        return meberIds;
    }


    private Set<Long> allIds(Member member, Set<Long> ids) {
        if (member != null) {
            Long mId = member.getId();
            String userToken = member.getToken();
            List<MemberRecRel> friendsList = memberRecRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
            for (MemberRecRel c : friendsList) {
                if (mId.equals(c.getMember().getId())) {
                    if (c.getP1() != null) ids.add(c.getP1());
                    if (c.getP2() != null) ids.add(c.getP2());
                    if (c.getP3() != null) ids.add(c.getP3());

                } else if (mId.equals(c.getP1())) {
                    if (c.getP2() != null) ids.add(c.getP2());
                    if (c.getP3() != null) ids.add(c.getP3());
                } else if (mId.equals(c.getP2())) {
                    if (c.getP3() != null) ids.add(c.getP3());
                } else {

                }
            }
        }

        return ids;
    }


    public Set<Long> getMemberRecRel(String userToken) {
        Set<Long> meberIds = new HashSet<>();
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        MemberRecRel memberRecRel = memberRecRelRepository.findTop1ByP3OrderByDateDesc(member.getId());
        if (memberRecRel != null) {
            Member m = memberRecRel.getMember();
            if (m != null) {
                meberIds.add(m.getId());
            }
            Long p1 = memberRecRel.getP1();
            Long p2 = memberRecRel.getP2();
            if (p1 != null) {
                meberIds.add(p1);
            }
            if (p2 != null) {
                meberIds.add(p2);
            }
            return meberIds;
        }

        memberRecRel = memberRecRelRepository.findTop1ByP2OrderByDateDesc(member.getId());
        if (memberRecRel != null) {
            Member m = memberRecRel.getMember();
            if (m != null) {
                meberIds.add(m.getId());
            }
            Long p1 = memberRecRel.getP1();
            if (p1 != null) {
                meberIds.add(p1);
            }
            return meberIds;
        }
        memberRecRel = memberRecRelRepository.findTop1ByP1OrderByDateDesc(member.getId());
        if (memberRecRel != null) {
            Member m = memberRecRel.getMember();
            if (m != null) {
                meberIds.add(m.getId());
            }

        }
        return meberIds;
    }


    /**
     * 保存推荐关系
     */
    Member buildOneRecord(Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordRepository.findOne(id);
        Member inviter = jobApplyRecord.getReferral();
        Member invitee = jobApplyRecord.getReceiver();

        updateMemberRecRel(invitee, inviter);

        MemberRecRel newRecord = new MemberRecRel();
        newRecord.setMember(inviter);
        newRecord.setP1(invitee.getId());
        memberRecRelRepository.save(newRecord);


        Integer workingRecord = jobApplyRecordRepository.countByReferralTokenAndRecState(inviter.getToken(), WORKING);
        Boolean inviterIsWokring = workingRecord > 0;//邀请人是否在工作
        if (inviterIsWokring) {
            notFirstInvite(inviter, invitee, newRecord);
            firstInvite(inviter, invitee);
        }
        return inviter;
    }

    public void updateMemberRecRel(Member receiver, Member referral) {
        MemberRecRel memberRecRel1 = memberRecRelRepository.findTop1ByMemberTokenOrderByDateDesc(receiver.getToken());
        MemberRecRel memberRecRel = memberRecRelRepository.findTop1ByP1OrderByDateDesc(receiver.getId());
        if (isNotNull(memberRecRel) || isNotNull(memberRecRel1)) {
            List<MemberRecRel> memberRecRelList = memberRecRelRepository.findByMemberTokenOrP1OrP2OrP3(receiver.getToken(), receiver.getId(), receiver.getId(), receiver.getId());
            //解除上一个关系
            memberRecRelList.forEach(m -> {
                if (receiver.getId().equals(m.getP1())) m.setP1(null);
                if (receiver.getId().equals(m.getP2())) m.setP2(null);
                if (receiver.getId().equals(m.getP3())) m.setP3(null);
                memberRecRelRepository.save(m);
                if (isNull(m.getP1()) && isNull(m.getP2()) && isNull(m.getP3())) {
                    memberRecRelRepository.delete(m);
                }
            });
        }
    }

    private void saveNewRecRel(Member inviter, Long inviteeId, Consumer<MemberRecRel> consumer, MemberRecRel newRecRel) {
        ifNotNullThen(consumer, c -> c.accept(newRecRel));
        memberRecRelRepository.save(newRecRel);
    }

    /**
     * 第一次邀请别人的逻辑
     */
    private void firstInvite(Member inviter, Member invitee) {
        Specification<MemberRecRel> specification = (root, cq, cb) -> cb.or(
                cb.and(cb.equal(root.get("p1"), inviter.getId()), cb.isNull(root.get("p2"))),
                cb.and(cb.equal(root.get("p2"), inviter.getId()), cb.isNull(root.get("p3")))
        );
        PageRequest pageRequest = new PageRequest(0, 2, new Sort(DESC, "date"));
        Page<MemberRecRel> recRels = memberRecRelRepository.findAll(specification, pageRequest);
        recRels.forEach(e -> {
            if (isNull(e.getP2()))
                e.setP2(invitee.getId());
            else
                e.setP3(invitee.getId());
        });
    }

    /**
     * 不是一次邀请别人
     */
    private void notFirstInvite(Member inviter, Member invitee, MemberRecRel newRecord) {
        Specification<MemberRecRel> specification = (root, cq, cb) -> cb.or(
                cb.and(cb.equal(root.get("p1"), inviter.getId()), cb.isNotNull(root.get("p2"))),
                cb.and(cb.equal(root.get("p2"), inviter.getId()), cb.isNotNull(root.get("p3")))
        );
        PageRequest pageRequest = new PageRequest(0, 2, new Sort(DESC, "date"));
        Page<MemberRecRel> recRels = memberRecRelRepository.findAll(specification, pageRequest);
        MemberRecRel p1_p2 = new MemberRecRel();
        MemberRecRel p1_p2_p3 = new MemberRecRel();
        recRels.forEach(e -> {
            if (e.getP1().equals(inviter.getId())) {
                p1_p2.setMember(e.getMember());
                p1_p2.setP1(e.getP1());
                p1_p2.setP2(invitee.getId());
                memberRecRelRepository.save(p1_p2);
            } else {
                p1_p2_p3.setMember(e.getMember());
                p1_p2_p3.setP2(e.getP2());
                p1_p2_p3.setP1(e.getP1());
                p1_p2_p3.setP3(invitee.getId());
                memberRecRelRepository.save(p1_p2_p3);
            }
        });
    }


    //以下为管理端接口

    public Map<String, Long> recCascade(Long id) {
        Map<String, Long> ids = new HashMap<>();

        MemberRecRel memberRecRels = memberRecRelRepository.findTop1ByP3OrderByDateDesc(id);
        if (isNotNull(memberRecRels)) {
            ids.put("id1", memberRecRels.getP2());
            ids.put("id2", memberRecRels.getP1());
            ids.put("id3", memberRecRels.getMember().getId());
            return ids;
        }

        memberRecRels = memberRecRelRepository.findTop1ByP2OrderByDateDesc(id);
        if (isNotNull(memberRecRels)) {
            ids.put("id1", memberRecRels.getP1());
            ids.put("id2", memberRecRels.getMember().getId());
            return ids;
        }

        memberRecRels = memberRecRelRepository.findTop1ByP1OrderByDateDesc(id);
        ifNotNullThen(memberRecRels, m -> ids.put("id1", m.getMember().getId()));
        return ids;
    }

    /**
     * 两者之间的推荐关系
     *
     * @Author xiao xue wei
     * @Date 2016/12/22
     */

    public Map<String, Object> oneToOneRec(Long memberId, Long pId) {
        String userToken = memberRepository.findOne(memberId).getToken();
        Map<String, Object> map = new HashMap<>();
        List<MemberRecRel> friendsList;
        friendsList = memberRecRelRepository.findByMemberTokenOrP1OrP2(userToken, memberId, memberId);
        for (MemberRecRel memberRecRel : friendsList) {
            if (memberId.equals(memberRecRel.getP2()) && memberRecRel.getP3() != null && pId.equals(memberRecRel.getP3())) {
                map.put("relation", "推荐-朋友");
                map.put("time", memberRecRel.getDate());
                return map;
            } else if (memberId.equals(memberRecRel.getP1()) && memberRecRel.getP2() != null && pId.equals(memberRecRel.getP2())) {
                map.put("relation", "推荐-朋友");
                map.put("time", memberRecRel.getDate());
                return map;
            } else if (memberId.equals(memberRecRel.getMember().getId()) && memberRecRel.getP1() != null && pId.equals(memberRecRel.getP1())) {
                map.put("relation", "推荐-朋友");
                map.put("time", memberRecRel.getDate());
                return map;
            }
        }

        friendsList = memberRecRelRepository.findByMemberTokenOrP1(userToken, memberId);
        for (MemberRecRel memberRecRel : friendsList) {
            if (memberId.equals(memberRecRel.getP1()) && memberRecRel.getP3() != null && pId.equals(memberRecRel.getP3())) {
                map.put("relation", "推荐-熟人");
                map.put("time", memberRecRel.getDate());
                return map;
            } else if (memberId.equals(memberRecRel.getMember().getId()) && memberRecRel.getP2() != null && pId.equals(memberRecRel.getP2())) {
                map.put("relation", "推荐-熟人");
                map.put("time", memberRecRel.getDate());
                return map;
            }
        }
        friendsList = memberRecRelRepository.findByMemberToken(userToken);
        for (MemberRecRel memberRecRel : friendsList) {
            if (memberRecRel.getP3() != null && pId.equals(memberRecRel.getP3())) {
                map.put("relation", "推荐-人脉");
                map.put("time", memberRecRel.getDate());
                return map;
            }
        }
        return map;
    }


    /**
     * 获取上级列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/3
     */
    public Set<Long> findChiefFriends(Long userId, ModuleKey.SubLevelType subLevel) {
        Set<Long> chiefFriendsIds = new HashSet<>();

        if (subLevel == SUB_LEVEL_THREE) {// 所有三级好友id
            List<MemberRecRel> recList = memberRecRelRepository.findByP3(userId);
            ifNotEmptyThen(recList, e -> e.forEach(f -> {
                if (isNotNull(f.getMember()))
                    chiefFriendsIds.add(f.getMember().getId());
            }));
            List<MemberRegRel> memberRegRels = memberRegRelRepository.findByP3(userId);
            ifNotNullThen(memberRegRels, e -> e.forEach(m -> ifNotNullThen(m.getMember(), f -> chiefFriendsIds.add(f.getId()))));

        } else if (subLevel == SUB_LEVEL_TWO) {// 所有二级好友id
            List<MemberRecRel> recList = memberRecRelRepository.findByP3OrP2(userId, userId);
            ifNotEmptyThen(recList, e -> e.forEach(f -> {
                if (userId.equals(f.getP3()) && (f.getP1() != null)) chiefFriendsIds.add(f.getP1());
                if (userId.equals(f.getP2()) && (f.getMember() != null)) chiefFriendsIds.add(f.getMember().getId());
            }));
            List<MemberRegRel> memberRegRels = memberRegRelRepository.findByP2(userId);
            ifNotNullThen(memberRegRels, e -> e.forEach(m -> ifNotNullThen(m.getMember(), f -> chiefFriendsIds.add(f.getId()))));

        } else if (subLevel == SUB_LEVEL_ONE) {// 所有一级好友id
            List<MemberRecRel> recList = memberRecRelRepository.findByP3OrP2OrP1(userId, userId, userId);
            ifNotEmptyThen(recList, e -> e.forEach(f -> {
                if (userId.equals(f.getP3()) && (f.getP2() != null)) chiefFriendsIds.add(f.getP2());
                if (userId.equals(f.getP2()) && (f.getP1() != null)) chiefFriendsIds.add(f.getP1());
                if (userId.equals(f.getP1()) && (f.getMember() != null)) chiefFriendsIds.add(f.getMember().getId());
            }));
            List<MemberRegRel> memberRegRels = memberRegRelRepository.findByP1(userId);
            ifNotNullThen(memberRegRels, e -> e.forEach(m -> ifNotNullThen(m.getMember(), f -> chiefFriendsIds.add(f.getId()))));
        }
        return chiefFriendsIds;
    }

}
