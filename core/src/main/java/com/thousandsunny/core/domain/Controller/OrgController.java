package com.thousandsunny.core.domain.Controller;

import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.Controller.dto.IdNameChildChannel;
import com.thousandsunny.core.model.Org;
import com.thousandsunny.core.domain.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateUtils.parseDate;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * 如果这些代码有用，那它们是guitarist在7/26/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/baseOrgs", produces = APPLICATION_JSON_UTF8_VALUE)
public class OrgController {
    @Autowired
    private OrgService orgService;

    /**
     * 查看组织树
     */
    @RequestMapping(method = GET)
    public Result orgs() {
        List<IdNameChildChannel> list = simpleMap(orgService.findRootOrgs(), IdNameChildChannel::new);
        return OK(recursiveOrgs(list));
    }

    /**
     * 添加/更新组织
     */
    @RequestMapping(method = POST)
    public Result saveOrg(Org org, String createTime_, String channelClassy) throws ParseException {
        org.setCreateTime(parseDate(createTime_));
        return OK(orgService.save(org, channelClassy));
    }

    /**
     * 假删除
     */
    @RequestMapping(value = "/{id}", method = DELETE)
    public Result deleteOrg(@PathVariable Long id) throws ParseException {
        return OK(orgService.deleteFake(id));
    }

    /**
     * 组织下的用户
     */
    @RequestMapping(value = "/{id}", method = GET, produces = APPLICATION_JSON_UTF8_VALUE, params = "users")
    public Result usersUnderOrg(@PathVariable Long id, PageVO pageVO) {
        return OK(orgService.usersUnderOrg(id, pageVO.pageRequest()));
    }

    private List<IdNameChildChannel> recursiveOrgs(List<IdNameChildChannel> list) {
        list.forEach(i -> {
            List<IdNameChildChannel> orgs = simpleMap(orgService.childOrgs(i.getId()), IdNameChildChannel::new);
            i.setChildChannels(orgs);
            recursiveOrgs(orgs);
        });
        return list;
    }
}
