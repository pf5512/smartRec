package com.thousandsunny.core.domain.service;

import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.repository.SysActionLogRepository;
import com.thousandsunny.core.model.SysActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static java.util.Calendar.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 如果这些代码有用，那它们是guitarist在8/9/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class SysActionLogService extends BaseService<SysActionLog> {

    @Autowired
    private SysActionLogRepository sysActionLogRepository;

    public void deleteBatch(List<Long> ids) {
        delete(simpleMap(ids, SysActionLog::new));
    }

    public Result<SysActionLog> search(String level, String method, String period, String search, PageRequest pageVo) {
        Stream<SysActionLog> stream = findAll(pageVo).getContent().stream();
        if (isNotBlank(level) && !"错误级别".equals(level)) {
            stream = stream.filter(x -> x.getLogLevel() != null && x.getLogLevel().name().equals(level));
        }
        if (isNotBlank(period) && !"周期".equals(period)) {
            stream = stream.filter(x -> {
                if (x.getCreateDate() == null) {
                    return false;
                }
                Calendar nowDate = Calendar.getInstance();
                nowDate.setTime(new Date());
                Calendar createDate = Calendar.getInstance();
                createDate.setTime(x.getCreateDate());
                boolean isSameYear = nowDate.get(YEAR) == createDate.get(YEAR);
                boolean isSameMonth = isSameYear && nowDate.get(MONTH) == createDate.get(MONTH);
                boolean isSameWeek = isSameMonth && nowDate.get(WEEK_OF_MONTH) == createDate.get(WEEK_OF_MONTH);
                boolean isSameDay = isSameMonth && nowDate.get(DAY_OF_MONTH) == createDate.get(DAY_OF_MONTH);
                if ("当天".equals(period)) {
                    return isSameDay;
                } else if ("最近一周".equals(period)) {
                    return isSameWeek;
                } else if ("最近一月".equals(period)) {
                    return isSameMonth;
                } else if ("最近一年".equals(period)) {
                    return isSameYear;
                }
                return false;
            });
        }
        if (isNotBlank(method) && !"请求方式".equals(method)) {
            stream = stream.filter(x -> x.getMethod() != null && x.getMethod().name().equals(method));
        }
        if (isNotBlank(search)) {
            stream = stream.filter(x -> (x.getContent() != null && x.getContent().contains(search)) ||
                    (x.getSource() != null && x.getSource().contains(search))
            );
        }
        return OK(stream.collect(toList()));
    }

    public Page<SysActionLog> findByKeyWord(String keyWord, PageRequest pageRequest) {
        return sysActionLogRepository.findByContentContainingOrderByCreateDateDesc(keyWord, pageRequest);
    }
}
