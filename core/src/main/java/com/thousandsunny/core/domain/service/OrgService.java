package com.thousandsunny.core.domain.service;

import com.thousandsunny.core.domain.repository.OrgRepository;
import com.thousandsunny.core.domain.repository.UserRepository;
import com.thousandsunny.core.model.Org;
import com.thousandsunny.core.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 如果这些代码有用，那它们是guitarist在7/26/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class OrgService extends BaseService<Org> {
    @Autowired
    private OrgRepository orgRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Org> findRootOrgs() {
        return orgRepository.findByParentIsNullAndIsDelete(NO);
    }

    public Page<User> usersUnderOrg(Long id, PageRequest pageRequest) {
        return userRepository.findByOrgId(id, pageRequest);
    }

    public List<Org> childOrgs(Long id) {
        return orgRepository.findByParentIdAndIsDelete(id, NO);
    }

    public Org save(Org org, String channelClassy) {
        if (isNotBlank(channelClassy)) {
            List<Long> idStr = simpleMap(newArrayList(channelClassy.split(",")), Long::new);
            Long id = idStr.get(idStr.size() - 1);
            org.setParent(new Org(id));
        }
        return orgRepository.save(org);
    }

    /**
     * 假删除
     */
    public Org deleteFake(Long id) {
        Org org = orgRepository.findOne(id);
        org.setIsDelete(YES);
        return orgRepository.save(org);
    }
}
