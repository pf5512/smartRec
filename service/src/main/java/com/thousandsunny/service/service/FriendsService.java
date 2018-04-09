package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.domain.repository.FriendsRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Friends;
import com.thousandsunny.service.model.MemberRecRel;
import com.thousandsunny.service.model.MemberRegRel;
import com.thousandsunny.service.repository.MemberRecRelRepository;
import com.thousandsunny.service.repository.MemberRegRelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.thousandsunny.common.lambda.LambdaUtil.ifFalseThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleTips.TIP_JOIN_NO_GROUP;

/**
 * Created by admin on 2016/10/21.
 */
@Service
public class FriendsService extends BaseService<Friends> {
    @Autowired
    private FriendsRepository friendsRepository;

    @Autowired
    private MemberRecRelRepository memberRecRelRepository;


    @Autowired
    private MemberRegRelRepository memberRegRelRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ResumeService resumeService;

    /**
     * 好友总数
     */
    public Long friendsCount(String chatUserTokne) {
        return friendsRepository.countByOwnerToken(chatUserTokne);
    }


    public List<Integer> acount(String userToken) {
        long L1 = System.currentTimeMillis();
        Long mId = memberRepository.findByTokenAndIsDelete(userToken, NO).getId();
        List<MemberRecRel> memberRecRelList = memberRecRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
        List<MemberRegRel> memberRegRelList = memberRegRelRepository.findByMemberTokenOrP1OrP2(userToken, mId, mId);
        long L2 = System.currentTimeMillis();
        System.out.println(L2 - L1);
        List<Integer> acountList = new ArrayList<>();

        Set<Long> RecRelIds1 = new HashSet<>();
        Set<Long> RecRelIds2 = new HashSet<>();
        Set<Long> RecRelIds3 = new HashSet<>();
        Set<Long> RegRelIds1 = new HashSet<>();
        Set<Long> RegRelIds2 = new HashSet<>();
        Set<Long> RegRelIds3 = new HashSet<>();
        Set<Long> memberIds = new HashSet<>();
        Set<Long> regRelIdsMmemberIds = new HashSet<>();
        Set<Long> recRelIdsMmemberIds = new HashSet<>();
        for (MemberRecRel c : memberRecRelList) {
            if (c.getMember().getId() == mId) {
                if (c.getP1() != null) RecRelIds1.add(c.getP1());
                if (c.getP2() != null) RecRelIds2.add(c.getP2());
                if (c.getP3() != null) RecRelIds3.add(c.getP3());

            }

            if (c.getP1() == mId) {
                if (c.getP2() != null) RecRelIds1.add(c.getP2());
                if (c.getP3() != null) RecRelIds2.add(c.getP3());
            }

            if (c.getP2() == mId) {
                if (c.getP3() != null) RecRelIds1.add(c.getP3());
            }

        }


        for (MemberRegRel g : memberRegRelList) {

            if (g.getMember().getId() == mId) {
                if (g.getP1() != null) RegRelIds1.add(g.getP1());
                if (g.getP2() != null) RegRelIds2.add(g.getP2());
                if (g.getP3() != null) RegRelIds3.add(g.getP3());

            }

            if (g.getP1() == mId) {
                if (g.getP2() != null) RegRelIds1.add(g.getP2());
                if (g.getP3() != null) RegRelIds2.add(g.getP3());
            }

            if (g.getP2() == mId) {
                if (g.getP3() != null) RegRelIds1.add(g.getP3());
            }
        }


        regRelIdsMmemberIds.addAll(RegRelIds1);
        regRelIdsMmemberIds.addAll(RegRelIds2);
        regRelIdsMmemberIds.addAll(RegRelIds3);

        recRelIdsMmemberIds.addAll(RecRelIds1);
        recRelIdsMmemberIds.addAll(RecRelIds2);
        recRelIdsMmemberIds.addAll(RecRelIds3);

        memberIds.addAll(regRelIdsMmemberIds);
        memberIds.addAll(recRelIdsMmemberIds);
        acountList.add(memberIds.size()); // 好友数量
        acountList.add(acountMeiyeren(memberIds));  // 好友中是美业人的数量

        acountList.add(RegRelIds1.size());   // 注册一级好友数量
        acountList.add(acountMeiyeren(RegRelIds1));

        acountList.add(RegRelIds2.size());     // 注册二级好友数量
        acountList.add(acountMeiyeren(RegRelIds2));
        acountList.add(RegRelIds3.size());   // 注册三级好友数量
        acountList.add(acountMeiyeren(RegRelIds3));

        acountList.add(RecRelIds1.size()); //上班推荐一级好友数量
        acountList.add(acountMeiyeren(RecRelIds1));

        acountList.add(RecRelIds2.size());   // 上班推荐二级好友数量
        acountList.add(acountMeiyeren(RecRelIds2));

        acountList.add(RecRelIds3.size());  // 上班推荐三级好友数量
        acountList.add(acountMeiyeren(RecRelIds3));

        acountList.add(regRelIdsMmemberIds.size());//注册好友数量
        acountList.add(acountMeiyeren(regRelIdsMmemberIds));  // 注册好友中是美业人的数量

        acountList.add(recRelIdsMmemberIds.size());//上班推荐好友数量
        acountList.add(acountMeiyeren(recRelIdsMmemberIds));  // 上班推荐好友中是美业人的数量

        return acountList;
    }


    public int acountMeiyeren(Set<Long> ids) {
        int account = resumeService.countIsMeiyeren(ids);
        return account;
    }


    /**
     * 慧友列表
     */
    public Page<Friends> huiYouList(String userToken, Pageable pageable) {
        return friendsRepository.findByOwnerToken(userToken, pageable);
    }

    public List<Friends> huiYouList(String userToken) {
        return friendsRepository.findByOwnerToken(userToken);
    }


    /**
     * 慧友搜索
     */
    public Page<Friends> searchHuiYou(String userToken, String keyword, Integer pageNo, Integer pageSize) {

        PageVO pageVO = new PageVO();
        pageVO.setPageNo(pageNo);
        pageVO.setPageSize(pageSize);
        Page<Friends> friendsPage = null;
        if (keyword != null && keyword.trim().length() > 0) {
            friendsPage = friendsRepository.findByOwnerTokenAndFriendRealNameContaining(userToken, keyword, pageVO.pageRequest());
        } else {
            friendsPage = friendsRepository.findByOwnerToken(userToken, pageVO.pageRequest());
        }
        ifFalseThrow(friendsPage != null, TIP_JOIN_NO_GROUP);
        return friendsPage;

    }


    /**
     * 惠友中是美业人的数量
     */
    public int huiYouacount(String userToken) {
        List<Friends> friendses = friendsRepository.findByOwnerToken(userToken);
        Set<Long> ids = new HashSet<>();
        for (Friends f : friendses) {
            ids.add(f.getFriend().getId());
        }

        return acountMeiyeren(ids);
    }

    public Map<String, Object> oneToOneHuiyou(Long userId, Long id) {
        Map<String, Object> map = new HashMap<>();
        Friends friends = friendsRepository.findByOwnerIdAndFriendId(userId, id);
        if (isNotNull(friends)) {
            map.put("relation", "慧友");
            map.put("time", friends.getKnowDate());
        }
        return map;
    }
}
