package com.thousandsunny.core.config;

import com.thousandsunny.core.model.AppVisitedHistory;
import com.thousandsunny.core.model.SysActionLog;
import com.thousandsunny.core.domain.repository.AppVisitedHistoryRepository;
import com.thousandsunny.core.domain.repository.SysActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.alibaba.fastjson.JSON.toJSONString;
import static com.thousandsunny.common.CacheableUtil.*;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThen;
import static java.time.LocalDate.now;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.boot.logging.LogLevel.INFO;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.valueOf;

/**
 * 如果这些代码有用，那它们是guitarist在7/27/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Configuration
public class CustomMVCAdapter extends WebMvcConfigurerAdapter {

    @Autowired
    private SysActionLogRepository logRepository;
    @Autowired
    private AppVisitedHistoryRepository visitedHistoryRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new _MyInterceptor()).addPathPatterns("/api/manager/**");
        super.addInterceptors(registry);
    }

    private class _MyInterceptor extends HandlerInterceptorAdapter {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            cleanUp();
            RequestMethod method = valueOf(request.getMethod());
            String sessionId = request.getHeader("sessionId");
            if (method != GET) {
                SysActionLog sysActionLog = new SysActionLog();
//            sysActionLog.setUser();
                sysActionLog.setContent(toJSONString(request.getParameterMap()));
                sysActionLog.setCreateDate(new Date());
                sysActionLog.setLogLevel(INFO);
                sysActionLog.setMethod(method);
                sysActionLog.setSource(request.getRemoteHost());
                sysActionLog.setUrl(request.getRequestURL().toString());
                logRepository.save(sysActionLog);
            }

            ifTrueThen(localDate.isBefore(now()), () -> _1DayContainer.invalidateAll());
            if (isNotBlank(sessionId)) {
                _1DayContainer.put(sessionId, sessionId);
                _5MinutesContainer.put(sessionId, sessionId);

                Long count = visitedHistoryRepository.countByIdentifier(sessionId);
                ifTrueThen(count == 0, () -> visitedHistoryRepository.save(new AppVisitedHistory(null, sessionId)));
            }

            return super.preHandle(request, response, handler);
        }
    }
}
