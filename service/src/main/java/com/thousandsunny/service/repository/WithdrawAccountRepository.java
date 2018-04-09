package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.*;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.WithdrawAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by admin on 2016/11/7.
 */
public interface WithdrawAccountRepository extends BaseRepository<WithdrawAccount>{
    List<WithdrawAccount> findByMemberTokenAndIsDelete(String userToken, BooleanEnum isDelete);

    WithdrawAccount findByMemberTokenAndIdAndIsDelete(String userToken,Long id,BooleanEnum isDelete);

    WithdrawAccount findByIdAndMemberTokenAndIsDelete(Long id, String userToken,BooleanEnum no);

    Page<WithdrawAccount> findByMemberIdAndIsDelete(Long userId, BooleanEnum isDelete, Pageable pageable);
}
