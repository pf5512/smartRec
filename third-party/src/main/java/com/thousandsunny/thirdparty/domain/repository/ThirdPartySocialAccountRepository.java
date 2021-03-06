package com.thousandsunny.thirdparty.domain.repository;


import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.thirdparty.model.ThirdPartySocialAccount;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.ThirdPartySocialAccountType;

/**
 * Created by guitarist on 6/22/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public interface ThirdPartySocialAccountRepository extends BaseRepository<ThirdPartySocialAccount> {

    List<ThirdPartySocialAccount> findByMember(Member member);

    ThirdPartySocialAccount findByMemberAndAccountType(Member member, ThirdPartySocialAccountType accountType);
}
