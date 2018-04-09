package com.thousandsunny.thirdparty.domain.repository;


import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.thirdparty.model.Account;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

public interface AccountRepository extends BaseRepository<Account> {
    Account findByMemberId(Long memberId);
    Account findByMemberToken(String userToken);

    Account findByMemberMobile(String mobile);

    Account findByZues(BooleanEnum yes);
}
