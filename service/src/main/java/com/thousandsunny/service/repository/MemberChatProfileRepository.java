package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.MemberChatProfile;

/**
 * Created by admin on 2016/10/12.
 */

public interface MemberChatProfileRepository extends BaseRepository<MemberChatProfile> {

    MemberChatProfile findByOwnerTokenAndChatUserToken(String ownerToken, String chatUserToken);
}
