package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.dto.SysUser;
import com.thousandsunny.cms.model.*;
import com.thousandsunny.cms.domain.repository.AuthorityRepository;
import com.thousandsunny.cms.domain.repository.RoleRepository;
import com.thousandsunny.cms.domain.repository.UserRoleRefRepository;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.User;
import com.thousandsunny.core.domain.repository.UserRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * 如果这些代码有用，那它们是guitarist在7/22/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
@Transactional
public class AclService extends BaseService<Role> {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRefRepository userRoleRefRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;


    /**
     * 保存角色
     */
    public Role saveRole(Role role, String channelPermission) {
        return wrapRoles(roleRepository.save(role), channelPermission);
    }

    /**
     * 查找角色
     */
    public Role findRole(Long id) {
        return roleRepository.findOne(id);
    }

    /**
     * 所有角色
     */
    public List<Role> findRoles() {
        return roleRepository.findAll();
    }

    /**
     * 更新角色
     */
    public Role updateRole(Role role, String channelPermission) {
        role = wrapRoles(roleRepository.findOne(role.getId()), channelPermission);
        return roleRepository.save(role);
    }

    private Role wrapRoles(Role finalRole, String channelPermission) {
//        if (!isEmpty(finalRole.getAuthorityRefs()))
//            authorityRepository.delete(finalRole.getAuthorityRefs());
        List<Authority> authorities = newArrayList();
        if (!isNull(channelPermission)) {
            String[] channelPermissionArr = channelPermission.split(",");
            newArrayList(channelPermissionArr).forEach(s -> {
                Authority authority = new Authority();
                String[] channelIdOperations = s.split(":");

                Long channelId = new Long(channelIdOperations[0]);
//                authority.setChannel(new Channel(channelId));//这个配置的记录的栏目id

                List<Long> operationIds = newArrayList(channelIdOperations[1].split("_"))
                        .stream().map(Long::new).collect(toList());//被选择了的操作的id
                authority.setOperations(operationIds.stream().map(Operation::new).collect(toList()));
                authority.setRole(finalRole);
                authorities.add(authorityRepository.save(authority));
//                finalRole.setAuthorityRefs(authorities);
            });
        }
        return finalRole;
    }


    /**
     * 更新角色
     */
    public Boolean updateUserRoleRef(Long id, Long[] rids) {
        userRoleRefRepository.deleteByUserId(id);
        newArrayList(rids).forEach(roleId -> userRoleRefRepository.save(new UserRoleRef(new User(id), new Role(roleId))));
        return true;
    }

    public List<Channel> visibleChannels() {
        User user = userService.getUserFromContext();
        List<Channel> container = newArrayList();
//        userRoleRefRepository.findByUser(user).forEach(urf -> urf.getRole().getAuthorityRefs().forEach(authority -> container.add(authority.getChannel())));
        return container.stream().filter(c -> c.getIsDelete() != YES).collect(toList());//过滤已经删除的,和一级栏目
    }

    public List<SysUser> findByOrgId(Long id) {
        List<User> list = userRepository.findByOrgId(id);
        return list.stream().map(user -> {
            StringBuffer role = new StringBuffer();
            List<UserRoleRef> userRoleRef = userRoleRefRepository.findByUser(user);
            for (UserRoleRef u : userRoleRef) {
                role.append(u.getRole().getName()).append(",");
            }
            SysUser sysUser = new SysUser(user);
            if (role.length() > 0) {
                sysUser.setRole(role.substring(0, role.length() - 1));
            }
            return sysUser;
        }).collect(Collectors.toList());
    }

    public Boolean saveUserRoleRef(String ids, User user) {
        if (isNotBlank(ids)) {
            String[] roleIdArr = ids.split(",");
            List<UserRoleRef> oldUserRoleRef = userRoleRefRepository.findByUser(user);
            userRoleRefRepository.delete(oldUserRoleRef);
            for (String s : roleIdArr) {
                Long roleId = Long.parseLong(s);
                Role role = roleRepository.findOne(roleId);
                UserRoleRef newUserRoleRef = new UserRoleRef();
                newUserRoleRef.setRole(role);
                newUserRoleRef.setUser(user);
                userRoleRefRepository.save(newUserRoleRef);
            }
            return true;
        }else {
            List<UserRoleRef> oldUserRoleRef = userRoleRefRepository.findByUser(user);
            userRoleRefRepository.delete(oldUserRoleRef);
        }
        return false;
    }

    public Result<String> delManyUser(String idList) {
        if (isNotBlank(idList)) {
            String[] idArr = idList.split(",");
            for (String s : idArr) {
                Long id = Long.parseLong(s);
                User user = userRepository.findOne(id);
                List<UserRoleRef> roleRef = userRoleRefRepository.findByUser(user);
                userRoleRefRepository.delete(roleRef);
                userRepository.delete(user);
            }
            return OK("delete success");
        }
        return OK("unDelete");
    }

    public List<SysUser> searchUser(Long orgId, String search) {
        Stream<SysUser> stream = findByOrgId(orgId).stream();
        if (isNotBlank(search)) {
            stream = stream.filter(user -> (user.getRealName() != null && user.getRealName().contains(search)) ||
                    (user.getUsername() != null && user.getUsername().contains(search)) ||
                    (user.getRole() != null && user.getRole().contains(search)));
        }
        return stream.collect(Collectors.toList());
    }

    public Result<String> delRole(Long id) {
        Role role = roleRepository.findOne(id);
//        List<Authority> authorities = authorityRepository.findByRoleId(id);
//        ifNotNullThen(authorities, x -> authorityRepository.delete(x));
        List<UserRoleRef> userRoleRefs = userRoleRefRepository.findByRoleId(id);
        ifNotNullThen(userRoleRefs, x -> userRoleRefRepository.delete(x));
        roleRepository.delete(role);
        return OK("delete success");
    }

    /**
     * 用户在某个栏目下的操作权限
     */
    public List<Operation> operationsUnderChannel(Long channelId) {
        User user = userService.getUserFromContext();
        List<UserRoleRef> userRoleRefs = userRoleRefRepository.findByUser(user);
        List<Operation> operations = newArrayList();
        userRoleRefs.forEach(userRoleRef -> {
//            Authority authority = authorityRepository.findByChannelIdAndRoleId(channelId, userRoleRef.getRole().getId());
//            ifNotNullThen(authority, a -> operations.addAll(a.getOperations()));
        });
        return operations;
    }
}
