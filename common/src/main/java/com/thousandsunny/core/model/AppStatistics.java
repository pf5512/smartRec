package com.thousandsunny.core.model;

import lombok.Data;

/**
 * 如果这些代码有用，那它们是guitarist在9/9/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
public class AppStatistics {
    private Long todayPeople;
    private Long todayArticle;
    private Long currentPeople;
    private Long historyPeople;
}
