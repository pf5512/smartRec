package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.domain.service.RoleService;
import com.thousandsunny.cms.model.MemberRole;
import com.thousandsunny.cms.model.Role;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.service.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.service.ModuleTips.TIP_NO_AUTHORITY;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_MEMBER_NOT_EXISTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by 13336 on 2017/3/7.
 */
@RestController
@RequestMapping(value = "/api/manager/role", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerRoleController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private RoleService roleService;

    /**
     * 10.1.1 管理员列表
     *
     * @Author xiao xue wei
     * @Date 2017/3/8
     */
    @RequestMapping(value = "/managerList", method = GET)
    public Result managerList(BackPageVo pageVo, String text) {
        String[] MANAGER_INFO = {"id", "hpAccount", "realName", "role.title", "validBoolean", "regTime"};
        Page<Member> page = memberService.findManagerPage(pageVo.pageRequest(), decodePathVariable(text));
        return OK(page.map(member -> {
            JSONObject jo = propsFilter(member, MANAGER_INFO);
            if (member.getRole() == ModuleKey.AccountEnum.MANAGER) jo.put("companyName", "慧聘");
            else if (member.getRole() == ModuleKey.AccountEnum.SCHOOL) {
                School school = schoolService.findMemberSchool(member);
                jo.put("companyName", school.getName());
            }
            List<MemberRole> memberRoles = roleService.findMemberRoles(member);
            if (!memberRoles.isEmpty()) {
                StringBuffer role = new StringBuffer();
                memberRoles.forEach(memberRole -> role.append(memberRole.getRole().getName()).append(","));
                role.deleteCharAt(role.length() - 1);
                jo.put("role", role.toString());
            } else jo.put("role", null);
            ifNotNullThen(member.getCreateTime(), e -> jo.replace("regTime", ISO_DATETIME_FORMAT.format(member.getCreateTime())));
            return jo;
        }));
    }

    /**
     * 10.1.2 删除
     *
     * @Author xiao xue wei
     * @Date 2017/3/9
     */
    @RequestMapping(value = "/managerDelete", method = DELETE)
    public Result deleteManager(String ids, String userToken) {
        Member member = memberService.findByToken(userToken);
        ifFalseThrow(member.getRole() == ModuleKey.AccountEnum.MANAGER, TIP_NO_AUTHORITY);
        String[] ids1 = ids.split(",");
        List<String> strings = newArrayList(ids1);
        List<Long> idList = new ArrayList<>();
        if (!strings.isEmpty()) strings.forEach(e -> idList.add(Long.parseLong(e)));
        memberService.deleteManager(idList);
        return OK();
    }

    /**
     * 10.1.3 启用
     *
     * @Author xiao xue wei
     * @Date 2017/3/9
     */
    @RequestMapping(value = "/managerEnable", method = POST)
    public Result enableManager(Long id, String userToken) {
        Member member = memberService.findByToken(userToken);
        ifFalseThrow(member.getRole() == ModuleKey.AccountEnum.MANAGER, TIP_NO_AUTHORITY);
        memberService.enabled(id);
        return OK();
    }

    /**
     * 10.1.4 新增/修改
     *
     * @Author xiao xue wei
     * @Date 2017/3/9
     */
    @RequestMapping(value = "/managerEdit", method = POST)
    public Result editManager(Member member, String schoolName, Long provinceId, Long cityId, Long areaId,
                              String schoolAddress, String schoolXYZ, String schoolWeb, String roleIds, ModuleKey.BooleanEnum valid) {
        memberService.editManager(member, schoolName, provinceId, cityId, areaId, schoolAddress, schoolXYZ, schoolWeb, roleIds, valid);
        return OK();
    }

    /**
     * 10.1.5 详情
     *
     * @Author xiao xue wei
     * @Date 2017/3/9
     */
    @RequestMapping(value = "/managerDetail", method = GET)
    public Result managerDetail(Long id) {
        String[] MANAGER_DETAIL_INFO = {"hpAccount:account", "realName:userName", "gender", "mobile",
                "regTime", "role:type", "validBoolean:state", "username:nickName"};
        String[] SCHOOL_INFO = {"name:schoolName", "province.id:provinceId", "city.id:cityId", "area.id:areaId",
                "link:schoolWeb", "address:schoolAddress"};
        Member member = memberService.findOne(id);
        ifFalseThrow(isNotNull(member) && member.getIsDelete() == ModuleKey.BooleanEnum.NO, TIP_MEMBER_NOT_EXISTED);
        JSONObject detail = propsFilter(member, MANAGER_DETAIL_INFO);
        ifNotNullThen(member.getCreateTime(), e -> detail.replace("regTime", ISO_DATETIME_FORMAT.format(e)));
        if (member.getRole() == ModuleKey.AccountEnum.SCHOOL) {
            School school = schoolService.findMemberSchool(member);
            detail.replace("mobile", school.getTelephone());
            JSONObject schoolInfo = propsFilter(school, SCHOOL_INFO);
            StringBuffer schoolXYZ = new StringBuffer();
            ifNotNullThen(school.getLongitude(), schoolXYZ::append);
            ifNotNullThen(school.getLatitude(), e -> schoolXYZ.append(",").append(e));
            schoolInfo.put("schoolXYZ", schoolXYZ.toString());
            detail.put("schoolInfo", schoolInfo);
        }
        StringBuffer roleIds = new StringBuffer();
        List<MemberRole> memberRoles = roleService.findMemberRoles(member);
        if (!memberRoles.isEmpty()) {
            memberRoles.forEach(memberRole -> roleIds.append("" + memberRole.getRole().getId()).append(","));
            roleIds.deleteCharAt(roleIds.length() - 1);
        }
        detail.put("role", roleIds.toString());
        return OK(detail);
    }

    /**
     * 10.2.1 角色列表
     *
     * @Author xiao xue wei
     * @Date 2017/3/9
     */
    @RequestMapping(value = "/roleList", method = GET)
    public Result roleList() {
        String[] ROLE_LIST_INFO = {"id", "name"};
        List<Role> list = roleService.findRoleList();
        return OK(simpleMap(list, role -> propsFilter(role, ROLE_LIST_INFO)));
    }
}
