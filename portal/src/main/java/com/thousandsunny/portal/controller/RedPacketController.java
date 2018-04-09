package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.RedPacketReceive;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.service.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.listToJson;
import static com.thousandsunny.common.lambda.LambdaUtil.pageToJson;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by mu.jie on 2017/2/23.
 */
@RestController
@RequestMapping(value = "/api/portal/redPacket", produces = APPLICATION_JSON_UTF8_VALUE)
public class RedPacketController {

    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private MemberService memberService;

    private static final String[] RED_PACKET_JSON = {
            "id", "redPacket.category.title:name", "redPacket.amount:amount", "redPacket.validDate:validDate", "state"
    };

    /**
     * 21.1红包列表
     *
     * @Author mu.jie
     * @Date 2017/2/23
     */
    @RequestMapping(method = GET)
    public ResponseEntity findRedPacket(String userToken, BooleanEnum isGetNormalState) {
        JSONObject jsonObject = new JSONObject();
        Member member = memberService.findByToken(userToken);
        List<RedPacketReceive> list = redPacketService.findRedPacket(member, isGetNormalState);
        List<JSONObject> jsons = simpleMap(list, redPacketReceive -> {
            JSONObject jo = propsFilter(redPacketReceive, RED_PACKET_JSON);
            return jo;
        });
        jsonObject.put("list", jsons);
        return ok(jsonObject);
    }

    /**
     * 21.2我的红包提醒
     *
     * @Author mu.jie
     * @Date 2017/2/23
     */
    @RequestMapping(value = "/unRead", method = GET)
    public ResponseEntity findUnReadReadPacket(String userToken) {
        Member member = memberService.findByToken(userToken);
        List<RedPacketReceive> list = redPacketService.findUnRedPacket(member);
        List<JSONObject> body = simpleMap(list, x -> {
            JSONObject jo = propsFilter(x, RED_PACKET_JSON);
            return jo;
        });
        return ok(listToJson(body));
    }

}
