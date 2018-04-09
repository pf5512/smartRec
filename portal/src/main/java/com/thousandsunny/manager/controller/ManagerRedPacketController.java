package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.RedPacketCategory;
import com.thousandsunny.service.model.RedPacket;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.service.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2017/2/23.
 */
@RestController
@RequestMapping(value = "/api/manager/redPacket", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerRedPacketController {
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private MemberService memberService;

    private static final String[] MEMBER_INFO_JSON = {"id", "realName:username", "hpAccount"};

    private static final String[] RED_PACKET_LIST_JSON = {
            "id", "category.title", "amount", "validDate", "sendType.title", "createDate"
    };
    private static final String[] RED_PACEKET_INFO_JSON = {
            "category.title:type", "amount", "validDate", "receiveUser", "sendType.title:remark",
            "specialTypes:userType", "startDate:userJoinStartTime", "endDate:userJoinEndTime"
    };

    /**
     * 8.2.1 红包列表
     *
     * @Author mu.jie
     * @Date 2017/2/23
     */
    @RequestMapping(method = GET)
    public Result findRedPacket(BackPageVo backPageVo, RedPacketCategory type) {
        Page<RedPacket> page = redPacketService.findAllRedPacket(backPageVo, type);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, RED_PACKET_LIST_JSON);
            ifNotNullThen(x.getValidDate(), t -> jo.replace("validDate", ISO_DATETIME_FORMAT.format(t)));
            ifNotNullThen(x.getCreateDate(), t -> jo.replace("createDate", ISO_DATETIME_FORMAT.format(t)));
            return jo;
        }));
    }

    /**
     * 8.2.2 新增/修改
     *
     * @Author mu.jie
     * @Date 2017/2/24
     */
    @RequestMapping(method = POST)
    public Result updateRedPacket(RedPacket redPacket, String receiveUser, String userType) {
        redPacketService.updateRedPacket(redPacket, receiveUser, userType);
        return OK("success");
    }

    /**
     * 8.2.3 详情
     *
     * @Author mu.jie
     * @Date 2017/2/24
     */
    @RequestMapping(value = "/info", method = GET)
    public Result findRedPacketInfo(Long id) {
        RedPacket redPacket = redPacketService.findOne(id);
        JSONObject jo = propsFilter(redPacket, RED_PACEKET_INFO_JSON);
        ifNotNullThen(redPacket.getCategory(), d -> enumToJson(d, jo, "type"));
        ifNotNullThen(redPacket.getValidDate(), d -> jo.replace("validDate", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(redPacket.getStartDate(), d -> jo.replace("userJoinStartTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(redPacket.getEndDate(), d -> jo.replace("userJoinEndTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(redPacket.getSendType(), d -> enumToJson(d, jo, "remark"));
        jo.replace("receiveUser", redPacketService.findReceivesString(redPacket));
        return OK(jo);
    }

    /**
     * 8.2.4 查询用户信息
     *
     * @Author mu.jie
     * @Date 2017/2/24
     */
    @RequestMapping(value = "/members", method = GET)
    public Result findMemberInfo(String id) {
        List<Member> memberList = newArrayList();
        if (isNotBlank(id)) {
            newArrayList(id.split(",")).forEach(i -> {
                Member member = memberService.findOne(Long.parseLong(i));
                ifNotNullThen(member, m -> memberList.add(m));
            });
        }
        List<JSONObject> body = simpleMap(memberList, member -> propsFilter(member, MEMBER_INFO_JSON));
        return OK(listToJson(body));
    }

    /**
     * 8.2.5 搜索用户
     *
     * @Author mu.jie
     * @Date 2017/2/26
     */
    @RequestMapping(value = "/search", method = GET)
    public Result search(String username, String userType, Date startTime, Date endTime) {
        List<Member> members = redPacketService.search(username, userType, startTime, endTime);
        List<JSONObject> body = simpleMap(members, member -> propsFilter(member, MEMBER_INFO_JSON));
        return OK(listToJson(body));
    }

    /**
     * 8.2.6 所有用户数
     *
     * @Author mu.jie
     * @Date 2017/3/10
     */
    @RequestMapping(value = "/count", method = GET)
    public Result countAllMember() {
        Long num = redPacketService.countAllMember();
        JSONObject body = new JSONObject();
        body.put("num", num);
        return OK(body);
    }


}
