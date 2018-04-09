package com.thousandsunny.cms.domain.controller;

import com.thousandsunny.cms.dto.SysUser;
import com.thousandsunny.cms.model.UserRoleRef;
import com.thousandsunny.cms.domain.repository.RoleRepository;
import com.thousandsunny.cms.domain.repository.UserRoleRefRepository;
import com.thousandsunny.cms.domain.service.AclService;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.User;
import com.thousandsunny.core.domain.repository.OrgRepository;
import com.thousandsunny.core.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.entity.Result.conflict;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by mu.jie on 2016/8/11 0011.
 */
@RestController
@RequestMapping(value = "/setting")
public class SettingController {
    @Autowired
    private UserService userService;
    @Autowired
    private AclService aclService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRefRepository userRoleRefRepository;
    @Autowired
    private OrgRepository orgRepository;

    /**
     * 保存用户信息
     *
     * @Author mu.jie
     * @Date 2016/8/11 0011
     */
    @RequestMapping(value = "/saveUserSet", method = POST)
    public Result<String> saveUserSet(User user, String upImg, String ids) {
        user = userService.saveUserSet(user, upImg);
        aclService.saveUserRoleRef(ids, user);
        return OK();
    }

    @RequestMapping(value = "/checkIsExist", method = GET)
    public Result<String> checkIsExist(String name) {
        User user = userService.findByUsername(name);
        if (user != null) {
            return conflict("用户名已存在");
        } else {
            return OK();
        }
    }

    /**
     * 显示用户设置图片编辑页面
     *
     * @Author mu.jie
     * @Date 2016/8/11 0011
     */
    @RequestMapping(value = "/upicon", method = GET, produces = TEXT_HTML_VALUE)
    public ModelAndView upicon(ModelAndView view) {
        User user = userService.getUserFromContext();
        view.addObject("image", user.getHeadImagePath());
        view.setViewName("manager/upicon");
        return view;
    }

    /**
     * 查询用户列表
     *
     * @Author mu.jie
     * @Date 2016/8/11 0011
     */
    @RequestMapping(value = "/user", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<SysUser> allUser(Long id) {
        return OK(aclService.findByOrgId(id));
    }

    /**
     * 管理员管理新增页面
     *
     * @Author mu.jie
     * @Date 2016/8/11 0011
     */
    @RequestMapping(value = "/adminMgtAdd", method = GET, produces = TEXT_HTML_VALUE)
    public ModelAndView adminMgtAdd(ModelAndView view, Long channelId) {
        view.addObject("user", new User());
        view.addObject("channelId", channelId);
        view.addObject("roles", roleRepository.findAll());
        view.setViewName("manager/setting/adminMgt-add");
        return view;
    }

    /**
     * 管理员管理编辑页面
     *
     * @Author mu.jie
     * @Date 2016/8/11 0011
     */
    @RequestMapping(value = "/adminMgtEdit", method = GET, produces = TEXT_HTML_VALUE)
    public ModelAndView adminMgtEdit(ModelAndView view, Long id) {
        User user = userService.findOne(id);
        view.addObject("user", user);
        view.addObject("roles", roleRepository.findAll());
        List<UserRoleRef> userRoleRef = userRoleRefRepository.findByUser(user);
        StringBuffer stringBuffer = new StringBuffer();
        for (UserRoleRef u : userRoleRef) {
            stringBuffer.append(u.getRole().getId()).append(",");
        }
        if (stringBuffer.length() > 0) {
            view.addObject("choseRoles", stringBuffer.substring(0, stringBuffer.length() - 1));
        }
        view.setViewName("manager/setting/adminMgt-add");
        return view;
    }

    /**
     * 用户管理,删除一个用户
     *
     * @Author mu.jie
     * @Date 2016/8/12 0012
     */
    @RequestMapping(value = "/delUser", method = DELETE, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<String> delUser(Long id) {
        User user = userService.findOne(id);
        List<UserRoleRef> roleRefList = userRoleRefRepository.findByUser(user);
        userRoleRefRepository.delete(roleRefList);
        return OK(userService.delete(id));
    }

    /**
     * 用户管理,删除多个用户
     *
     * @Author mu.jie
     * @Date 2016/8/12 0012
     */
    @RequestMapping(value = "/delManyUser", method = DELETE, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<String> delManyUser(String idList) {
        return aclService.delManyUser(idList);
    }

    /**
     * 更改用户是否启用
     *
     * @Author mu.jie
     * @Date 2016/8/12 0012
     */
    @RequestMapping(value = "/updateValid", method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<String> updateValid(Long id) {
        User user = userService.findOne(id);
        if (user.getValid() == YES) {
            user.setValid(NO);
        } else {
            user.setValid(YES);
        }
        userService.save(user);
        return OK("success");
    }

    /**
     * 用户管理,搜索用户
     *
     * @Author mu.jie
     * @Date 2016/8/12 0012
     */
    @RequestMapping(value = "/searchUser", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<User> searchUser(Long orgId, String search) {
        return OK(aclService.searchUser(orgId, decodePathVariable(search)));
    }

    @RequestMapping(value = "/turnAdd", method = GET, produces = TEXT_HTML_VALUE)
    public ModelAndView turnAdd() {
        ModelAndView view = new ModelAndView();
        view.addObject("weight", orgRepository.findMaxWeight() + 1);
        view.setViewName("view/setting/deptMgt-add");
        return view;
    }

}
