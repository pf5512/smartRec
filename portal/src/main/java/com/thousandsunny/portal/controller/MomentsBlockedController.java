package com.thousandsunny.portal.controller;

import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.service.model.MomentsBlocked;
import com.thousandsunny.service.service.MomentsBlockedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.pageToJson;
import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/portal/momentsBlocked", produces = APPLICATION_JSON_UTF8_VALUE)
public class MomentsBlockedController {
    @Autowired
    private MomentsBlockedService momentsBlockedService;

    private static final String[] MEMBER_INFO = {
            "id",
            "momentsMember.headImage.path:headerImageUrl",
            "momentsMember.realName:realName",
            "momentsMember.token:token"
    };

    /**
     * 屏蔽查看动态的用户列表
     */

    @RequestMapping(value = "blockedList", method = GET)
    public ResponseEntity blockedList(String userToken, PageVO pageVo) {
        Page<MomentsBlocked> members = momentsBlockedService.findByUserToken(userToken, pageVo.pageRequest());
        return ok(pageToJson(members, e -> propsFilter(e, MEMBER_INFO)));
    }


    /**
     * 新增屏蔽动态的用户
     */
    @RequestMapping(value = "/blocked", method = POST)
    public ResponseEntity blocked(String userToken, String invitedUserToken) {
        momentsBlockedService.blocked(userToken, invitedUserToken);
        return OK;
    }


    /**
     * 取消屏蔽动态用户
     */


    @RequestMapping(value = "/cancelBlocked", method = DELETE)
    public ResponseEntity cancelBlocked(String userToken, String invitedUserToken) {
        momentsBlockedService.cancelBlocked(userToken,invitedUserToken);
        return OK;
    }

}
