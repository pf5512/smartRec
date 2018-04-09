package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.EntrepreneursApply;

import java.util.List;

import static com.thousandsunny.service.ModuleKey.ApplyEnum;

/**
 * Created by admin on 2016/11/1.
 */
public interface EntrepreneursApplyRepository extends BaseRepository<EntrepreneursApply> {
    List<EntrepreneursApply> findByMemberTokenOrderByApplyDate  (String token);
    EntrepreneursApply findByMemberTokenAndState(String userToken, ApplyEnum state);

    List<EntrepreneursApply> findByMemberTokenAndStateOrderByApplyDate(String userToken, ApplyEnum state);

    List<EntrepreneursApply> findByMemberTokenAndStateInOrderByApplyDate(String userToken, List<ApplyEnum> list);
}
