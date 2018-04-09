package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.AppVisitedHistory;

/**
 * 如果这些代码有用，那它们是guitarist在9/9/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface AppVisitedHistoryRepository extends BaseRepository<AppVisitedHistory> {
    Long countByIdentifier(String sessionId);
}
