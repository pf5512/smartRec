package com.thousandsunny.service.service;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.MemberRecRel;
import com.thousandsunny.service.model.MemberRegRel;
import com.thousandsunny.service.repository.MemberRegRelRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.SubLevelType;
import static com.thousandsunny.core.ModuleKey.SubLevelType.*;
import static com.thousandsunny.service.ModuleTips.*;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by admin on 2016/10/20.
 */
@Service
public class MemberRegRelService extends BaseService<MemberRegRel> {

    @Autowired
    private MemberRegRelRepository memberRegRelRepository;
    @Autowired
    private MemberRepository memberRepository;


    /**
     * 好友列表
     */
    public Page<Member> friendsList(String userToken, SubLevelType subLevel, Pageable pageable) {
        Page<Member> memberPage = null;
        Set<Long> ids = getMemberIds(userToken, subLevel);
        if (ids != null) {
            memberPage = memberRepository.findByIdIn(ids, pageable);
        }
        ifFalseThrow(memberPage != null, TIP_NO_CHANSHUERROR);
        return memberPage;
    }

    /**
     * ,好友搜索
     */
    public Page<Member> search(String userToken, String keyword, SubLevelType subLevel, Pageable pageable) {
        Page<Member> memberPage = null;
        if (keyword != null && keyword.trim().length() > 0) {
            memberPage = memberRepository.findByIdInAndRealNameContaining(getMemberIds(userToken, subLevel), keyword, pageable);
        } else {
            memberPage = memberRepository.findByIdIn(getMemberIds(userToken, subLevel), pageable);
        }
        ifFalseThrow(memberPage != null, TIP_NO_CHANSHUERROR);
        return memberPage;
    }


    /**
     * 对应级别的好友关系,注册
     */
    public Set<Long> getMemberIds(String userToken, SubLevelType subLevel) {
        Set<Long> meberIds = new HashSet<>();
        Long mId = memberRepository.findByToken(userToken).getId();

        List<MemberRegRel> friendsList;
        if (subLevel == SUB_LEVEL_ONE) {
            friendsList = memberRegRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
            for (MemberRegRel memberRegRel : friendsList) {
                if (mId.equals(memberRegRel.getP2()) && memberRegRel.getP3() != null)
                    meberIds.add(memberRegRel.getP3());
                else if (mId.equals(memberRegRel.getP1()) && memberRegRel.getP2() != null)
                    meberIds.add(memberRegRel.getP2());
                else if (mId.equals(memberRegRel.getMember().getId()) && memberRegRel.getP1() != null)
                    meberIds.add(memberRegRel.getP1());
                else
                    continue;
            }
        }

        if (subLevel == SUB_LEVEL_TWO) {
            friendsList = memberRegRelRepository.findByMemberTokenOrP1(userToken, mId);
            for (MemberRegRel memberRegRel : friendsList) {
                if (mId.equals(memberRegRel.getP1()) && memberRegRel.getP3() != null)
                    meberIds.add(memberRegRel.getP3());
                else if (mId.equals(memberRegRel.getMember().getId()) && memberRegRel.getP2() != null)
                    meberIds.add(memberRegRel.getP2());
            }
        }

        if (subLevel == SUB_LEVEL_THREE) {
            friendsList = memberRegRelRepository.findByMemberToken(userToken);
            for (MemberRegRel memberRegRel : friendsList) {
                if (memberRegRel.getP3() != null) meberIds.add(memberRegRel.getP3());
            }
        }
        return meberIds;
    }


    //注册关系级别
    public SubLevelType regRelLevel(String userToken, String regUserToken) {
        Long cId = memberRepository.findByTokenAndIsDelete(regUserToken, NO).getId();
        Page<MemberRegRel> page = memberRegRelRepository.findTop1ByMemberTokenOrP1OrP2OrP3(userToken, cId, cId, cId, new PageRequest(0, 1));
//        ifEmptyThrow(page.getContent(), TIP_NO_REG_REL);
        if (isNotEmpty(page.getContent())) {
            MemberRegRel regRel = page.getContent().get(0);
            if (regRel.getP1().equals(cId))
                return SUB_LEVEL_ONE;
            else if (regRel.getP2().equals(cId))
                return SUB_LEVEL_TWO;
            else
                return SUB_LEVEL_THREE;
        } else {
            return null;
        }
    }


    /**
     * 所有推荐好友列表id
     */
    public Set<Long> friendsList(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
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
        List<MemberRegRel> friendsList = memberRegRelRepository.findByMemberTokenOrP1OrP2(userToken, member.getId(), member.getId());
        for (MemberRegRel m : friendsList) {
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


    public Set<Long> allIds(Member member, Set<Long> ids) {
        if (member != null) {
            Long mId = member.getId();
            String userToken = member.getToken();
            List<MemberRegRel> friendsList = memberRegRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
            for (MemberRegRel m : friendsList) {
                if (m.getMember().getId() == mId) {
                    if (m.getP1() != null) ids.add(m.getP1());
                    if (m.getP2() != null) ids.add(m.getP2());
                    if (m.getP3() != null) ids.add(m.getP3());
                } else if (m.getP1() == mId) {
                    if (m.getP2() != null) ids.add(m.getP2());
                    if (m.getP3() != null) ids.add(m.getP3());
                } else if (m.getMember().getId() == mId) {
                    if (m.getP3() != null) ids.add(m.getP3());
                }
            }
        }

        return ids;
    }


    public Set<Long> getMemberRegRel(String userToken) {
        Set<Long> meberIds = new HashSet<>();
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<MemberRegRel> memberRegRel = memberRegRelRepository.findByP3(member.getId());

        if (memberRegRel != null) {
            memberRegRel.forEach(x -> {
                Member m = x.getMember();
                if (m != null) {
                    meberIds.add(m.getId());
                }
                Long p1 = x.getP1();
                Long p2 = x.getP2();
                if (p1 != null) {
                    meberIds.add(p1);
                }
                if (p2 != null) {
                    meberIds.add(p2);
                }
            });
            return meberIds;
        }

        memberRegRel = memberRegRelRepository.findByP2(member.getId());
        if (memberRegRel != null) {
            memberRegRel.forEach(x -> {
                Member m = x.getMember();
                if (m != null) {
                    meberIds.add(m.getId());
                }
                Long p1 = x.getP1();
                if (p1 != null) {
                    meberIds.add(p1);
                }
            });
            return meberIds;
        }
        memberRegRel = memberRegRelRepository.findByP1(member.getId());
        if (memberRegRel != null) {
            memberRegRel.forEach(x -> {
                Member m = x.getMember();
                if (m != null) {
                    meberIds.add(m.getId());
                }
            });

        }
        return meberIds;
    }


    public Member buildOneRecord(String inviteCode, Member invitee) {
        Member inviter = memberRepository.findByMobile(inviteCode);
        if (isNull(inviter))
            inviter = memberRepository.findByHpAccount(inviteCode);
        ifNullThrow(inviter, TIP_INVITER_NOT_EXIST);
        ifTrueThrow(inviter.getId().equals(invitee.getId()), TIP_NOT_REC_MYSELF);
        MemberRegRel newRecord = new MemberRegRel();
        newRecord.setMember(inviter);
        newRecord.setP1(invitee.getId());
        memberRegRelRepository.save(newRecord);

        notFirstInvite(inviter, invitee);
        firstInvite(inviter, invitee);
        return inviter;
    }

    /**
     * 第一次邀请别人的逻辑
     */
    private void firstInvite(Member inviter, Member invitee) {
        Specification<MemberRegRel> specification = (root, cq, cb) -> cb.or(
                cb.and(cb.equal(root.get("p1"), inviter.getId()), cb.isNull(root.get("p2"))),
                cb.and(cb.equal(root.get("p2"), inviter.getId()), cb.isNull(root.get("p3")))
        );
        List<MemberRegRel> regRels = memberRegRelRepository.findAll(specification);
        regRels.forEach(e -> {
            if (isNull(e.getP2()))
                e.setP2(invitee.getId());
            else
                e.setP3(invitee.getId());
        });
    }

    /**
     * 不是一次邀请别人
     */
    private void notFirstInvite(Member inviter, Member invitee) {
        Specification<MemberRegRel> specification = (root, cq, cb) -> cb.or(
                cb.and(cb.equal(root.get("p1"), inviter.getId()), cb.isNotNull(root.get("p2"))),
                cb.and(cb.equal(root.get("p2"), inviter.getId()), cb.isNotNull(root.get("p3")))
        );
        List<MemberRegRel> regRels = memberRegRelRepository.findAll(specification);
        List<MemberRegRel> p1s = regRels.stream()
                .filter(e -> e.getP1().equals(inviter.getId()))
                .limit(1).collect(toList());
        List<MemberRegRel> p2s = regRels.stream()
                .filter(e -> e.getP2().equals(inviter.getId()))
                .limit(1).collect(toList());
        if (p1s.size() > 0) {
            MemberRegRel memberRegRel = new MemberRegRel();
            p1s.forEach(e -> {
                memberRegRel.setMember(e.getMember());
                memberRegRel.setP1(e.getP1());
                memberRegRel.setP2(invitee.getId());
                memberRegRelRepository.save(e);
            });
            memberRegRelRepository.save(memberRegRel);
        }
        if (p2s.size() > 0) {
            MemberRegRel memberRegRel = new MemberRegRel();
            p2s.forEach(e -> {
                memberRegRel.setMember(e.getMember());
                memberRegRel.setP1(e.getP1());
                memberRegRel.setP2(e.getP2());
                memberRegRel.setP3(invitee.getId());
                memberRegRelRepository.save(e);
            });
            memberRegRelRepository.save(memberRegRel);
        }
    }

    @Test
    public void testCollection() {

        List<Long> demos = range(1, 101).boxed().collect(toList());
        demos.forEach(e -> e = e * 10);
        demos.forEach(System.out::println);
        System.out.println(demos.stream().map(Object::toString).collect(Collectors.joining(",")));
//
        demos.stream().map(e -> e = e * 10).forEach(System.out::println);
        demos.forEach(System.out::println);

        // 字符串连接，concat = "ABCD"
        String concat = Stream.of("A", "B", "C", "D").reduce(",", String::concat);
        System.out.println(concat);

    }


    //以下为管理端接口

    public Map<String, Long> regCascade(Long id) {
        Map<String, Long> ids = new HashMap<>();

        MemberRegRel memberRegRels = memberRegRelRepository.findTop1ByP3OrderByDateDesc(id);
        if (isNotNull(memberRegRels)) {
            ids.put("id1", memberRegRels.getP2());
            ids.put("id2", memberRegRels.getP1());
            ids.put("id3", memberRegRels.getMember().getId());
            return ids;
        }

        memberRegRels = memberRegRelRepository.findTop1ByP2OrderByDateDesc(id);
        if (isNotNull(memberRegRels)) {
            ids.put("id1", memberRegRels.getP1());
            ids.put("id2", memberRegRels.getMember().getId());
            return ids;
        }

        memberRegRels = memberRegRelRepository.findTop1ByP1OrderByDateDesc(id);
        ifNotNullThen(memberRegRels, m -> ids.put("id1", m.getMember().getId()));
        return ids;
    }

    /**
     * 两者之间的注册关系
     *
     * @Author xiao xue wei
     * @Date 2016/12/22
     */

    public Map<String, Object> oneToOneReg(Long memberId, Long pId) {
        String userToken = memberRepository.findOne(memberId).getToken();
        Map<String, Object> map = new HashMap<>();
        List<MemberRegRel> friendsList;
        friendsList = memberRegRelRepository.findByMemberTokenOrP1OrP2(userToken, memberId, memberId);
        for (MemberRegRel memberRegRel : friendsList) {
            if (memberId.equals(memberRegRel.getP2()) && memberRegRel.getP3() != null && pId.equals(memberRegRel.getP3())) {
                map.put("relation", "注册-朋友");
                map.put("time", memberRegRel.getDate());
                return map;
            } else if (memberId.equals(memberRegRel.getP1()) && memberRegRel.getP2() != null && pId.equals(memberRegRel.getP2())) {
                map.put("relation", "注册-朋友");
                map.put("time", memberRegRel.getDate());
                return map;
            } else if (memberId.equals(memberRegRel.getMember().getId()) && memberRegRel.getP1() != null && pId.equals(memberRegRel.getP1())) {
                map.put("relation", "注册-朋友");
                map.put("time", memberRegRel.getDate());
                return map;
            }
        }

        friendsList = memberRegRelRepository.findByMemberTokenOrP1(userToken, memberId);
        for (MemberRegRel memberRegRel : friendsList) {
            if (memberId.equals(memberRegRel.getP1()) && memberRegRel.getP3() != null && pId.equals(memberRegRel.getP3())) {
                map.put("relation", "注册-熟人");
                map.put("time", memberRegRel.getDate());
                return map;
            } else if (memberId.equals(memberRegRel.getMember().getId()) && memberRegRel.getP2() != null && pId.equals(memberRegRel.getP2())) {
                map.put("relation", "注册-熟人");
                map.put("time", memberRegRel.getDate());
                return map;
            }
        }
        friendsList = memberRegRelRepository.findByMemberToken(userToken);
        for (MemberRegRel memberRegRel : friendsList) {
            if (memberRegRel.getP3() != null && pId.equals(memberRegRel.getP3())) {
                map.put("relation", "注册-人脉");
                map.put("time", memberRegRel.getDate());
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
            List<MemberRegRel> memberRegRel = memberRegRelRepository.findByP3(userId);
            ifNotNullThen(memberRegRel, e -> e.forEach(m -> ifNotNullThen(m.getMember(), f -> chiefFriendsIds.add(f.getId()))));

        } else if (subLevel == SUB_LEVEL_TWO) {// 所有二级好友id
            List<MemberRegRel> memberRegRel = memberRegRelRepository.findByP2(userId);
            ifNotNullThen(memberRegRel, e -> e.forEach(m -> ifNotNullThen(m.getMember(), f -> chiefFriendsIds.add(f.getId()))));

        } else if (subLevel == SUB_LEVEL_ONE) {// 所有一级好友id
            List<MemberRegRel> memberRegRel = memberRegRelRepository.findByP1(userId);
            ifNotNullThen(memberRegRel, e -> e.forEach(m -> ifNotNullThen(m.getMember(), f -> chiefFriendsIds.add(f.getId()))));
        }
        return chiefFriendsIds;
    }
}
