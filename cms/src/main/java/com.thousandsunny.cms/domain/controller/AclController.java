package com.thousandsunny.cms.domain.controller;

import com.thousandsunny.cms.dto.IdNameMemo;
import com.thousandsunny.cms.dto.SimpleChannel;
import com.thousandsunny.cms.dto.SysMenu;
import com.thousandsunny.cms.model.*;
import com.thousandsunny.cms.domain.repository.UserRoleRefRepository;
import com.thousandsunny.cms.domain.service.*;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.User;
import com.thousandsunny.core.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.entity.Result.OK;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by guitarist on 7/22/16.
 */
@RestController
@RequestMapping(value = "/sys/acl", produces = APPLICATION_JSON_UTF8_VALUE)
public class AclController {

    @Autowired
    private BaseChannelService baseChannelService;
    @Autowired
    private AclService aclService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private UserRoleRefRepository userRoleRefRepository;
    @Autowired
    private UserService userService;

    /**
     * 所有的角色
     */
    @RequestMapping(value = "/roles", method = GET)
    public Result findRoles() {
        return OK(aclService.findRoles().stream().map(IdNameMemo::new).collect(toList()));
    }

    /**
     * 添加角色
     */
    @RequestMapping(value = "/roles", method = POST)
    public Result saveRole(Role role, String authority) {
        return OK(aclService.saveRole(role, authority));
    }

    /**
     * 更新角色
     */
    @RequestMapping(value = "/roles", method = PUT)
    public Result updateRole(Role role, String authority) {
        return OK(aclService.updateRole(role, authority));
    }

    /**
     * 角色详情
     */
    @RequestMapping(value = "/roles/{id}", method = GET)
    public Result find(@PathVariable Long id) {
        return OK(aclService.findRole(id));
    }

    /**
     * 用户在某个栏目下的操作权限
     */
    @RequestMapping(value = "/roles/{channelId}/operations", method = GET)
    public Result operationsUnderChannel(@PathVariable Long channelId) {
        return OK(aclService.operationsUnderChannel(channelId));
    }

    /**
     * 用户绑定角色
     */
    @RequestMapping(value = "/userRoleRef", method = PUT, params = "urf")
    public Result updateUserRoleRef(Long uid, Long[] rids) {
        return OK(aclService.updateUserRoleRef(uid, rids));
    }

    /**
     * 删除角色
     *
     * @Author mu.jie
     * @Date 2016/8/12 0012
     */
    @RequestMapping(value = "/delRole", method = DELETE)
    public Result<String> delRole(Long id) {
        return aclService.delRole(id);
    }

    /**
     * 递归角色配置树
     */
    @RequestMapping(value = "/acl/recursiveChannels/{id}", method = GET)
    public Result<SimpleChannel> recursiveChannels(@PathVariable Long id) {
        List<SimpleChannel> rootChannels = baseChannelService.allRootChannelsSiteIdIn(newArrayList(1l, 2l, 3l, 4l, 5l, 6l, 7l))
                .stream().map(SimpleChannel::new).collect(toList());
        List<Channel> allNotRootChannels = baseChannelService.notRootNotDeletedChannels();
        List<Authority> authorities = authorityService.findAll();//权限配置
        List<SimpleChannel> simpleChannels = recursiveSelectedOperations(rootChannels, id, authorities, allNotRootChannels);
        return OK(simpleChannels);
    }

    /**
     * 递归栏目树--附带栏目已选择的行为
     */
    private List<SimpleChannel> recursiveSelectedOperations(List<SimpleChannel> rootChannels, Long roleId, List<Authority> authorities, List<Channel> allNotRootChannels) {
        rootChannels.forEach(rootChannel -> {
            List<Authority> authority = authorities.stream()
//                    .filter(a -> a.getChannel().getId().equals(rootChannel.getId()) && a.getRole().getId().equals(roleId))
                    .collect(toList());
            rootChannel.setOperations(parseArray(toJSONString(rootChannel.getOperations()), Operation.class));
            if (!isEmpty(authority)) {
                authority.get(0).getOperations().forEach(checked ->//已经选择的权限
                        rootChannel.getOperations().forEach(raw -> {//拥有的权限
                            if (raw.getId() == checked.getId()) {
                                raw.setChecked(TRUE);
                            }
                        }));
            }
            List<SimpleChannel> children = allNotRootChannels.stream()
                    .filter(c -> c.getParentChannel().getId().equals(rootChannel.getId()))
                    .map(SimpleChannel::new)
                    .collect(toList());//所有的子栏目
            rootChannel.setChildren(children);
            recursiveSelectedOperations(children, roleId, authorities, allNotRootChannels);
        });
        return rootChannels;
    }

    /**
     * 递归菜单
     */
//    @RequestMapping(value = "/acl/recursiveMenu/{id}", method = GET)
//    public Result<Menu> recursiveMenu(@PathVariable Long id) {
//        List<SysMenu> parentMenus = menuService.findByParentIsNull().stream().map(SysMenu::new).collect(toList());
//        List<SysMenu> selectedMenus = roleService.findOne(id).getMenus().stream().map(SysMenu::new).collect(toList());
//        List<SysMenu> sysMenus = recursiveMenu(parentMenus, selectedMenus);
//        return OK(sysMenus);
//    }

    private List<SysMenu> recursiveMenu(List<SysMenu> parentMenus, List<SysMenu> selectedMenus) {
        if (isNull(parentMenus)) return null;
        parentMenus.forEach(menu -> {
            selectedMenus.forEach(e -> {
                if (e.getId().equals(menu.getId())) {
                    menu.setChecked(true);
                }
            });
            recursiveMenu(menu.getChild(), selectedMenus);
        });
        return parentMenus;
    }

//    @RequestMapping(value = "/acl/saveMenu", method = PUT)
//    public Result<Role> saveMenu(Long roleId, String idString) {
//        Role role = roleService.findOne(roleId);
//        role.setMenus(null);
//        roleService.save(role);
//        List<Menu> menus = new ArrayList<>();
//        String[] idstr = idString.split(",");
//        List<String> ids = Arrays.asList(idstr);
//        ids.forEach(e -> {
//            Menu menu = menuService.findOne(Long.parseLong(e));
//            menus.add(menu);
//        });
//        role.setMenus(menus);
//        return OK(roleService.save(role));
//    }

    /**
     * 根据角色显示菜单
     */
//    @RequestMapping(value = "/acl/menuCatalogue", method = GET)
//    public ModelAndView menuCatalogue() {
//        List<SysMenu> parentMenus = menuService.findByParentIsNull().stream().map(SysMenu::new).collect(toList());
//        User user = userService.getUserFromContext();
//        UserRoleRef userRoleRefs = userRoleRefRepository.findByUser(user).get(0);
//        List<SysMenu> menus = userRoleRefs.getRole().getMenus().stream().map(SysMenu::new).collect(toList());
//        List<SysMenu> sysMenus = recursiveMenu(parentMenus, menus);
//        ModelAndView view = new ModelAndView();
//        view.setViewName("manager/index");
//        view.addObject("parentMenus", sysMenus);
//        return view;
//    }
}
