package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.ModuleKey.*;
import com.thousandsunny.core.model.FriendApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by Administrator on 2016/9/22 0022.
 */
public interface FriendApplyRepository extends BaseRepository<FriendApply>{

   Page<FriendApply> findByApproverTokenAndApplyStateInOrderByApplyDateDesc(String userToken, List<ApplyState>applyStates ,Pageable pageable);
   FriendApply findByApplicantTokenAndApproverToken(String uToken, String aToken);
}
