package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.service.MemberRecRelService;
import com.thousandsunny.service.service.MemberRegRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by ekoo on 2016/12/15.
 */
@RestController
@RequestMapping(value = "/api/portal/regrec", produces = APPLICATION_JSON_UTF8_VALUE)
public class MemberRegRelRecRelController {

    private static final String[] MEMBER_JSON = {
            "token:registerParentLevel1Token",
            "realName:registerParentLevel1RealName",
            "mobile:registerParentLevel1PhoneNumber"
    };

    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberRepository memberRepository;


    /**
     * 20.临时-查询用户父级关系
     */
    @RequestMapping(value = "/memberFatherSonRelationship", method = GET)
    public ResponseEntity memberFatherSonRelationship(String userToken) {

        Set<Long> regs = memberRegRelService.getMemberRegRel(userToken);
        Set<Long> recs = memberRecRelService.getMemberRecRel(userToken);
        Set<Long> ids = new HashSet<Long>();
        ids.addAll(recs);
        ids.addAll(regs);
        Set<Member> members = memberRepository.findByIdIn(ids);
        List<Member> memberList = new ArrayList<Member>();
        memberList.addAll(members);
        List<JSONObject> json = simpleMap(memberList, x -> propsFilter(x,MEMBER_JSON));
        return ok(json);

    }

}
