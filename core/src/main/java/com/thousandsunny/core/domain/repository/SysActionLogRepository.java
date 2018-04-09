package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.SysActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 如果这些代码有用，那它们是guitarist在7/27/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface SysActionLogRepository extends BaseRepository<SysActionLog> {
    Page<SysActionLog> findByContentContainingOrderByCreateDateDesc(String keyWord, Pageable pageRequest);
}
