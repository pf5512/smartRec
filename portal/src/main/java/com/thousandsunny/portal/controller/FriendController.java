package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey.SubLevelType;
import com.thousandsunny.core.domain.service.BaseFriendsService;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Friends;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.portal.controller.dto.ComparatorMember;
import com.thousandsunny.portal.controller.dto.LetterFriend;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.thousandsunny.common.ChineseUtil.firstLetter;
import static com.thousandsunny.common.ChineseUtil.groupBy;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.FriendType;
import static com.thousandsunny.core.ModuleKey.FriendType.REGISTER;
import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/portal/friend", produces = APPLICATION_JSON_UTF8_VALUE)
public class FriendController {
    private static final String[] FRIEND_MODEL = {
            "friend.token:token",
            "friend.realName:realName",
            "friend.headImage.path:headerImageUrl",
            "friend.hpAccount:hpAccount"
    };

    private static final String[] FRIENDS_INFO = {
            "token",
            "realName",
            "headImage.path:headerImageUrl",
            "hpAccount"
    };

    private static final String[] LETTER_INFO = {
            "letter",
            "list"
    };

    private static final String[] MEMBER_INFO = {
            "headerImageUrl",
            "isActivationAPP",    // app是否激活
            "isActivationWX",    // 微信是否激活
            "recommendUser.realName:recommendRealName",    // 推荐人姓名
            "registerTime",
            "lastVisitTime:lastVisitTime", // 最后访问时间
            "visitCount",   // 总访问次数
            "hpFriendsCount",    // 惠友数量
            "isRecommendUserWithResume",
            "hpFriendsWithResumeCount"    // 惠友中是美业人的数量

    };

    @Autowired
    private BaseFriendsService baseFriendsService;
    @Autowired
    private FriendsService friendsService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberExtInfoService memberInfoService;
    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberMsgService memberMsgService;
    @Autowired
    private ResumeService resumeService;


    /**
     * 好友列表
     */

    @RequestMapping(value = "/friendsList", method = GET)
    public ResponseEntity friendsList(String userToken, FriendType friendType, SubLevelType subLevel, PageVO pageVO) {
        Set<String> letterSet = new HashSet<>();
        Page<Member> friendsPage;
        List<LetterFriend> letterFriendList = new ArrayList<>();
        if (friendType == REGISTER)
            friendsPage = memberRegRelService.friendsList(userToken, subLevel, pageVO.pageRequest());
        else
            friendsPage = memberRecRelService.friendsList(userToken, subLevel, pageVO.pageRequest());

        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        for (Member s : friendsPage) {
            boolean b = pattern.matcher(firstLetter(s.getRealName())).matches();
            if (!b)
                letterSet.add(firstLetter(s.getRealName()).toUpperCase());
        }
        wrapFriendList(letterSet, friendsPage, letterFriendList);
        return wrapFriendsResponseEntity(letterFriendList, friendsPage.isLast(), pageVO);

    }

    private void wrapFriendList(Set<String> letterSet, Page<Member> friendsPage, List<LetterFriend> letterFriendList) {
        List<String> letterList = letterSet.stream().sorted((x, y) ->{
            if ("#".equals(x)||"#".equals(y)) return y.charAt(0) - x.charAt(0);
            return x.charAt(0) - y.charAt(0);
        }).collect(Collectors.toList());
        for (String c : letterList) {
            LetterFriend letterFriend = new LetterFriend();
            List<Member> memberList = new ArrayList<>();
            for (Member m : friendsPage) {
                String s;
                Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
                boolean b = pattern.matcher(firstLetter(m.getRealName())).matches();
                if (b) s = "#";
                else
                    s = firstLetter(m.getRealName()).toUpperCase();
                if (c.equals(s))
                    memberList.add(m);
            }
            letterFriend.setLetter(c);
            letterFriend.setMember(memberList);
            letterFriendList.add(letterFriend);
        }
    }


    /**
     * 好友搜索
     */
    @RequestMapping(value = "search", method = GET)
    public ResponseEntity search(String userToken, String keyword, FriendType friendType, SubLevelType subLevel, PageVO pageVO) {
        Page<Member> friendsPage;
        if (friendType == REGISTER)
            friendsPage = memberRegRelService.search(userToken, keyword, subLevel, pageVO.pageRequest());
        else
            friendsPage = memberRecRelService.search(userToken, keyword, subLevel, pageVO.pageRequest());

        JSONObject jsonObject = pageToJson(friendsPage, e -> propsFilter(e, FRIENDS_INFO));
        //  jsonObject.put("count", memberGroups.getTotalElements());
        return ok(jsonObject);


    }


    //获取好友信息
    @RequestMapping(value = "/myFriendInfo", method = GET)
    public ResponseEntity myFriendInfo(String userToken, String checkedUserToken) {
        MemberExtInfo memberInfo = memberInfoService.findByMemberToken(checkedUserToken);
        Member checkUser = memberInfo.getMember();
        SubLevelType registerSubLevel = memberRegRelService.regRelLevel(userToken, checkedUserToken);
        SubLevelType recommendSubLevel = memberRecRelService.recRelLevel(userToken, checkedUserToken);
        Long hpFriendsCount = friendsService.friendsCount(checkedUserToken);
        JSONObject jsonObject = new JSONObject();
        if (isNotNull(memberInfo)) {
            Member recommend = memberInfo.getRecommendUser();
            boolean isRecommendUserWithResume = false;
            if (recommend != null) {
                isRecommendUserWithResume = resumeService.checkIsMeiyeren(recommend.getId());
            }

            jsonObject = propsFilter(memberInfo, MEMBER_INFO);
            jsonObject.put("token", checkUser.getToken());

            CloudFile headImage = checkUser.getHeadImage();
            if (isNotNull(headImage))
                jsonObject.put("headerImageUrl", headImage.getPath());

            jsonObject.put("realName", checkUser.getRealName());
            jsonObject.put("hpAccount", checkUser.getHpAccount());
            jsonObject.put("entrepreneurLevel", checkUser.getEntrepreneurLevel());
            jsonObject.put("isPartner", memberService.isPartner(checkedUserToken));
            jsonObject.put("registerSubLevel", isNotNull(registerSubLevel) ? registerSubLevel.getLevel() : 0);
            jsonObject.put("recommendSubLevel", isNotNull(recommendSubLevel) ? recommendSubLevel.getLevel() : 0);
            jsonObject.put("hpFriendsCount", hpFriendsCount);
            jsonObject.put("hpFriendsWithResumeCount", friendsService.huiYouacount(checkedUserToken));//惠友中是美业人的数量
            if (recommend != null) {
                jsonObject.put("isRecommendUserWithResume", isRecommendUserWithResume); //用户是否是美业人
            } else {
                jsonObject.put("isRecommendUserWithResume", null);
            }

        }
        return ok(jsonObject);

    }


    /**
     * 好友数量
     */
    @RequestMapping(value = "acount", method = GET)
    public ResponseEntity acount(String userToken) {

        List<Integer> acountList = friendsService.acount(userToken);
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("friendsCount", acountList.get(0));
        jsonObject.put("friendsWithResumeCount", acountList.get(1));
        jsonObject.put("registerSubLevel1FriendsCount", acountList.get(2));
        jsonObject.put("registerSubLevel1FriendsWithResumeCount", acountList.get(3));
        jsonObject.put("registerSubLevel2FriendsCount", acountList.get(4));
        jsonObject.put("registerSubLevel2FriendsWithResumeCount", acountList.get(5));
        jsonObject.put("registerSubLevel3FriendsCount", acountList.get(6));
        jsonObject.put("registerSubLevel3FriendsWithResumeCount", acountList.get(7));
        jsonObject.put("recommendSubLevel1FriendsCount", acountList.get(8));
        jsonObject.put("recommendSubLevel1FriendsWithResumeCount", acountList.get(9));
        jsonObject.put("recommendSubLevel2FriendsCount", acountList.get(10));
        jsonObject.put("recommendSubLevel2FriendsWithResumeCount", acountList.get(11));
        jsonObject.put("recommendSubLevel3FriendsCount", acountList.get(12));
        jsonObject.put("recommendSubLevel3FriendsWithResumeCount", acountList.get(13));
        jsonObject.put("registerFriendsCount", acountList.get(14));
        jsonObject.put("registerFriendsWithResumeCount", acountList.get(15));
        jsonObject.put("recommendFriendsCount", acountList.get(16));
        jsonObject.put("recommendFriendsWithResumeCount", acountList.get(17));
        return ok(jsonObject);

    }


    /**
     * 添加惠友请求
     */
    @RequestMapping(value = "addFriend", method = POST)
    public ResponseEntity addFriend(String userToken, String invitedUserToken) {
        memberMsgService.addFriend(userToken, invitedUserToken);
        return OK;
    }


    /**
     * 接受惠友请求
     */
    @RequestMapping(value = "acceptApply", method = PUT)
    public ResponseEntity acceptApply(String userToken, Long applyId) {
        memberMsgService.acceptApply(userToken, applyId);
        return OK;
    }


    /**
     * 惠友列表
     */
    @RequestMapping(value = "huiYouList", method = GET)
    public ResponseEntity huiYouList(String userToken, PageVO pageVO) {
        Page<Friends> huiYouPage = friendsService.huiYouList(userToken, pageVO.pageRequest());
        Page<JSONObject> page = huiYouPage.map(k -> propsFilter(k, FRIEND_MODEL));
        return ok(groupBy(page, jsonObject -> jsonObject.getString("realName")));
    }

    private ResponseEntity wrapFriendsResponseEntity(List<LetterFriend> letterFriendList, Boolean last, PageVO pageVO) {
        List<JSONObject> jos = simpleMap(letterFriendList, e -> {
            JSONObject jo = propsFilter(e, LETTER_INFO);
            jo.replace("letter", e.getLetter());
            List<JSONObject> jos1 = simpleMap(e.getMember(), k -> propsFilter(k, FRIENDS_INFO));
            jo.put("list", jos1);
            return jo;
        });
        JSONObject body = listToJson(jos);
        body.put("last", last);
        body.put("pageNo", pageVO.getPageNo());
        return ok(body);
    }


    /**
     * 慧友搜索
     */
    @RequestMapping(value = "searchHuiYou", method = GET)
    public ResponseEntity searchHuiYou(String userToken, String keyword, Integer pageNo, Integer pageSize) {
        Page<Friends> huiYouPage = friendsService.searchHuiYou(userToken, decodePathVariable(keyword), pageNo, pageSize);
        JSONObject jsonObject = pageToJson(huiYouPage, e -> propsFilter(e.getFriend(), FRIENDS_INFO));
        return ok(jsonObject);
    }


    /**
     * 总好友列表
     */

    @RequestMapping(value = "/allFriendsList", method = GET)
    public ResponseEntity allFriendsList(String userToken, PageVO pageVO) {

        Set<String> letterSet = new HashSet<>();
        Set<Long> idsSet1 = memberRegRelService.allFriendsList(userToken);
        Set<Long> idsSet2 = memberRecRelService.allFriendsList(userToken);
        List<Friends> friendses = friendsService.huiYouList(userToken);
        Set<Long> idsSet3 = new HashSet<Long>();
        for (Friends friends : friendses) {
            idsSet3.add(friends.getFriend().getId());
        }
        List<LetterFriend> letterFriendList = new ArrayList<>();

        idsSet1.addAll(idsSet2);
        idsSet1.addAll(idsSet3);//我的惠友列表
        List<Long> idsList = new ArrayList<>(idsSet1);
        Page<Member> friendsPage = baseMemberService.findByIds(idsList, pageVO.pageRequest());
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$"); //realName是数字的归为#
        for (Member s : friendsPage) {
            boolean b = pattern.matcher(firstLetter(s.getRealName())).matches();
            if (!b) letterSet.add(firstLetter(s.getRealName()).toUpperCase());
        }

        wrapFriendList(letterSet, friendsPage, letterFriendList);
        ComparatorMember comparator = new ComparatorMember();
        Collections.sort(letterFriendList, comparator);
        return wrapFriendsResponseEntity(letterFriendList, friendsPage.isLast(), pageVO);

    }


    /**
     * 我的总好友列表搜索
     */
    @RequestMapping(value = "allSearch", method = GET)
    public ResponseEntity allSearch(String userToken, String keyword, Integer pageNo, Integer pageSize) {
        Page<Member> friendsPage;
        Set<Long> idsSet1 = memberRegRelService.allFriendsList(userToken);
        Set<Long> idsSet2 = memberRecRelService.allFriendsList(userToken);
        idsSet1.addAll(idsSet2);
        List<Long> idsList = new ArrayList<>(idsSet1);
        friendsPage = baseMemberService.findByIdsAndUserName(idsList, decodePathVariable(keyword), pageNo, pageSize);
        JSONObject jsonObject = pageToJson(friendsPage, e -> propsFilter(e, FRIENDS_INFO));
        return ok(jsonObject);

    }


}
