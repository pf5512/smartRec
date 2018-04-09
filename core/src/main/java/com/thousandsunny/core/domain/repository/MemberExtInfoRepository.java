package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.MemberExtInfo;

public interface MemberExtInfoRepository extends BaseRepository<MemberExtInfo> {
    MemberExtInfo findByMemberToken(String userToken);

    MemberExtInfo findByMemberMobile(String phoneNumber);
}
