package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.model.Group;
import com.thousandsunny.core.model.MemberGroup;
import com.thousandsunny.service.service.GroupService;
import com.thousandsunny.thirdparty.domain.service.BaseGroupService;
import com.thousandsunny.thirdparty.vo.OperationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.pageToJson;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by admin on 2016/10/12.
 */

@RestController
@RequestMapping(value = "/api/portal/group", produces = APPLICATION_JSON_UTF8_VALUE)
public class GroupController {
    @Autowired
    private GroupService groupService;
    @Autowired
    private BaseGroupService baseGroupService;


    private static final String[] MEMBER_INFO_LIST = {
            "token",
            "realName",
            "headImage.path:headerImageUrl"
    };

    private static final String[] GROUP_INFO = {
            "id:groupId",
            "hxGroupId",
            "name:groupName"
    };


    /**
     * 建立群聊
     *
     * @Date 2016/9/26 0026  /已检查完
     */
    @RequestMapping(value = "setUp", method = POST)
    public ResponseEntity setUp(OperationPrincipal sysTaskApproval) {
        Group group = groupService.setUp(sysTaskApproval);
        JSONObject jsonObject = propsFilter(group, GROUP_INFO);
        MemberGroup memberGroup = group.getChairMan();
        jsonObject.put("isOwner", memberGroup.getIsOwnerBoolean());
        jsonObject.put("isTop", memberGroup.getIsTopBoolean());
        jsonObject.put("isNoDisturb", memberGroup.getIsNoDisturbBoolean());
        jsonObject.put("hxGroupId", memberGroup.getGroup().getHxGroupId());
        return ok(jsonObject);
    }


    /**
     * 获取群聊信息  /已检查过
     */

    @RequestMapping(value = "/findOneGroup", method = GET)
    public ResponseEntity findOneGroup(String userToken, String hxGroupId,Long groupId) {
        MemberGroup memberGroup = baseGroupService.findOneGroup(userToken, hxGroupId,groupId);
        Group group = new Group();
        JSONObject jsonObject = new JSONObject();
        if (memberGroup != null) {
            group = memberGroup.getGroup();
            jsonObject = propsFilter(group, GROUP_INFO);
            jsonObject.put("isOwner", memberGroup.getIsOwnerBoolean());
            jsonObject.put("isTop", memberGroup.getIsTopBoolean());
            jsonObject.put("isNoDisturb", memberGroup.getIsNoDisturbBoolean());
        }
        return ok(jsonObject);
    }


    /**
     * 群消息免打扰  已检查完
     */
    @RequestMapping(value = "/noDisturb", method = PUT)
    public ResponseEntity noDisturb(String userToken, Long groupId, OperatorType operatorType) {
        baseGroupService.noDisturb(userToken, groupId, operatorType);
        return OK;
    }


    /**
     * 群消息置顶   已检查完
     */
    @RequestMapping(value = "toTop", method = PUT)
    public ResponseEntity toTop(String userToken, Long groupId, OperatorType operatorType) {
        baseGroupService.toTop(userToken, groupId, operatorType);
        return OK;
    }


    /**
     * 群成员添加  已检查
     */

    @RequestMapping(value = "addGroupMate", method = POST)
    public ResponseEntity addGroupMate(OperationPrincipal sysTaskApproval, Long groupId) {
        groupService.addGroupMate(sysTaskApproval, groupId);
        return OK;
    }


    /**
     * 退出群聊 已检查完
     */
    @RequestMapping(value = "outGroup", method = POST)
    public ResponseEntity outGroup(String userToken, Long groupId) {
        baseGroupService.outGroup(userToken, groupId);
        return OK;
    }


    /**
     * 群成员列表  已检查
     */
    @RequestMapping(value = "groupMateList", method = GET)
    public ResponseEntity groupMateList(String type,Long groupId, String hxGroupId,Integer pageNo, Integer pageSize) {
        JSONObject jsonObject = new JSONObject();
        List<MemberGroup> memberGroupsList =  baseGroupService.groupMateList(groupId,hxGroupId);
        int totalCount = memberGroupsList.size();
        if("ALL".equals(type)){
            List<JSONObject> json = simpleMap(memberGroupsList, e -> propsFilter(e.getMember(), MEMBER_INFO_LIST));
            jsonObject.put("list",json);
            jsonObject.put("totalCount",totalCount);
        }
        if("PAGE".equals(type)){
            Page<MemberGroup>  memberGroups = baseGroupService.groupMateList(groupId, pageNo, pageSize);
            jsonObject = pageToJson(memberGroups, e -> propsFilter(e.getMember(), MEMBER_INFO_LIST));
            jsonObject.put("totalCount",totalCount);
        }
        return ok(jsonObject);
    }


    /**
     * 群主转让   已检查完
     */

    @RequestMapping(value = "changeOwner", method = PUT)
    public ResponseEntity changeOwner(String userToken, String ownerUserToken, Long groupId) {
        baseGroupService.changeOwner(userToken, ownerUserToken, groupId);
        return OK;
    }


    /**
     * 群成员搜索   检查
     */

    @RequestMapping(value = "/search", method = GET)
    public ResponseEntity search(Long groupId, String keyword, Integer pageNo, Integer pageSize) {
        Page<MemberGroup> memberGroups = baseGroupService.search(groupId, decodePathVariable(keyword), pageNo, pageSize);
        JSONObject jsonObject = pageToJson(memberGroups, e -> propsFilter(e.getMember(), MEMBER_INFO_LIST));
        return ok(jsonObject);
    }


    /**
     * 群成员删除
     */
    @RequestMapping(value = "delGroupMate", method = DELETE)
    public ResponseEntity delGroupMate(OperationPrincipal sysTaskApproval, Long groupId) {
        baseGroupService.delGroupMate(sysTaskApproval, groupId);
        return OK;
    }


    /**
     * 加入群聊  已检查完
     */
    @RequestMapping(value = "inGroup", method = POST)
    public ResponseEntity inGroup(String userToken, Long groupId) {
        MemberGroup memberGroup = baseGroupService.inGroup(userToken, groupId);
        JSONObject jsonObject = propsFilter(memberGroup.getGroup(), GROUP_INFO);
        jsonObject.put("isOwner", memberGroup.getIsOwnerBoolean());
        jsonObject.put("isTop", memberGroup.getIsTopBoolean());
        jsonObject.put("isNoDisturb", memberGroup.getIsNoDisturbBoolean());
        return ok(jsonObject);

    }

    /**
     * 2.15修改群名称
     */
    @RequestMapping(value = "/updateGroupName", method = POST)
    public ResponseEntity updateGroupName(String userToken,String name,Long groupId,String hxGroupId){
        MemberGroup memberGroup = baseGroupService.updateGroupName(userToken,name,groupId,hxGroupId);
        Group group = new Group();
        JSONObject jsonObject = new JSONObject();
        if (memberGroup != null) {
            group = memberGroup.getGroup();
            jsonObject = propsFilter(group, GROUP_INFO);
            jsonObject.put("isOwner", memberGroup.getIsOwnerBoolean());
            jsonObject.put("isTop", memberGroup.getIsTopBoolean());
            jsonObject.put("isNoDisturb", memberGroup.getIsNoDisturbBoolean());
        }
        return ok(jsonObject);
    }


}
