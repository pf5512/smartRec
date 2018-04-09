package com.thousandsunny.core.domain.Controller;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.SysActionLogService;
import com.thousandsunny.core.model.SysActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.entity.Result.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * 如果这些代码有用，那它们是guitarist在8/9/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/manager/logs", produces = APPLICATION_JSON_UTF8_VALUE)
public class SysActionLogController {

    @Autowired
    private SysActionLogService sysActionLogService;

    /**
     * 查询所有日志
     */
    @RequestMapping(method = GET)
    public Result<SysActionLog> all(BackPageVo pageVo, @RequestParam(defaultValue = "%") String text) {
        return OK(sysActionLogService.findByKeyWord(decodePathVariable(text), pageVo.descPageRequest("createDate")));
    }

    /**
     * 删除多条
     */
    @RequestMapping(method = DELETE)
    public Result<String> delMany(Long[] id) {
        sysActionLogService.deleteBatch(newArrayList(id));
        return OK();
    }

    /**
     * 详情页
     */
    @RequestMapping(value = "/{id}", method = GET)
    public Result sysLogsLook(@PathVariable Long id) {
        return OK(sysActionLogService.findOne(id));
    }
}
