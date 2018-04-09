package com.thousandsunny.cms.domain.service;

import com.thousandsunny.core.model.AppStatistics;
import com.thousandsunny.core.model.AppVisitedHistory;
import com.thousandsunny.core.domain.repository.AppVisitedHistoryRepository;
import com.thousandsunny.core.domain.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.thousandsunny.common.CacheableUtil._5MinutesContainer;
import static com.thousandsunny.common.CacheableUtil._1DayContainer;

/**
 * 如果这些代码有用，那它们是guitarist在9/9/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class AppVisitedHistoryService extends BaseService<AppVisitedHistory> {

    /**
     * 过期时间,5分钟不刷新认为已经关闭浏览器退出
     */
    @Autowired
    private AppVisitedHistoryRepository visitedHistoryRepository;
    @Autowired
    private BaseArticleService baseArticleService;

    public AppStatistics appStatistics() {
        Long historyPeople = visitedHistoryRepository.count();
        AppStatistics appStatistics = new AppStatistics();
        appStatistics.setHistoryPeople(historyPeople);
        appStatistics.setCurrentPeople(_5MinutesContainer.size());//当前人数
        appStatistics.setTodayArticle(baseArticleService.todayCount());//今日文章数量
        appStatistics.setTodayPeople(_1DayContainer.size());//今日人数
        return appStatistics;
    }
}
