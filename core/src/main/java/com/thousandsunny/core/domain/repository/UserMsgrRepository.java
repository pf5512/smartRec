package com.thousandsunny.core.domain.repository;


import com.thousandsunny.core.model.UserMsg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserMsgrRepository extends BaseRepository<UserMsg> {

    UserMsg findByIdAndReceiverId(Long msgId, Long userId);

    Page<UserMsg> findByReceiverId(Long userId, Pageable pageRequest);
}
