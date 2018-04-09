package com.thousandsunny.core.domain.repository;


import com.thousandsunny.core.model.MemberScoreConstant;

/**
 * Created by guitarist on 4/21/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public interface UserScoreContantRepository extends BaseRepository<MemberScoreConstant> {

    MemberScoreConstant findByScoreType(String signIn);
}
