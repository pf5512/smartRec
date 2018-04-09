package com.thousandsunny.thirdparty.domain.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Group;
import com.thousandsunny.core.model.MemberGroup;
/**
 * Created by admin on 2016/10/12.
 */
public interface GroupRepository extends BaseRepository<Group> {
    Group findBychairManAndHxGroupId(MemberGroup memberGroup, String hxGroupId);

  //  List<Group> findByHxGroupIdAndIsDelete(String hxGroupId,ModuleKey.BooleanEnum no);
    Group findByHxGroupIdAndIsDelete(String hxGroupId,ModuleKey.BooleanEnum no);

}
