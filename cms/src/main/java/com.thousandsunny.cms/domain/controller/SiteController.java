package com.thousandsunny.cms.domain.controller;


import com.thousandsunny.cms.dto.SimpleObj;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.cms.model.Site;
import com.thousandsunny.cms.model.SiteRootChannels;
import com.thousandsunny.cms.domain.service.BaseChannelService;
import com.thousandsunny.cms.domain.service.SiteService;
import com.thousandsunny.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;
import static com.thousandsunny.cms.ModuleKey.SiteTemplateEnum.TEMP1;
import static com.thousandsunny.cms.ModuleKey.SiteTypeEnum.INFORMATION;
import static com.thousandsunny.cms.ModuleKey.SiteTypeEnum.TOPICS;
import static com.thousandsunny.cms.domain.controller.ChannelController.recursiveNotDeletedChannels;
import static com.thousandsunny.common.entity.Result.OK;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@RestController
@RequestMapping(value = "/baseSites")
public class SiteController {

    @Autowired
    private SiteService siteService;
    @Autowired
    private BaseChannelService baseChannelService;

    @RequestMapping(method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<SimpleObj> list() {
        return OK(siteService.findByTypeIn(newArrayList(INFORMATION, TOPICS)).stream().map(Site::getSimpleSite).collect(toList()));
    }

    /**
     * 查看站点
     */
    @RequestMapping(value = "/{id}", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Site> findOne(@PathVariable Long id) {
        return OK(siteService.findOne(id));
    }


    @RequestMapping(method = POST, produces = APPLICATION_JSON_UTF8_VALUE, params = "first")
    public Result<Site> saveSite(Site site) {
        site.setTemplate(TEMP1);
        site.setType(TOPICS);//默认是专题
        return OK(siteService.save(site));
    }

    /**
     * 完整的站点的栏目树(角色配置。。)
     */
    @RequestMapping(value = "/channels/lv1", method = GET, produces = APPLICATION_JSON_VALUE)
    public List<SiteRootChannels> sitesRootChannels() {
        return siteService.siteRootChannels();
    }

    /**
     * 站点列表
     */
    @RequestMapping(method = GET, produces = TEXT_HTML_VALUE, params = "v")
    public ModelAndView siteView(ModelAndView view) {
        List<Site> sites = siteService.findAll();
        view.setViewName("manager/information/site");
        view.addObject("site", sites.get(0));
        return view;
    }

    /**
     * 编辑页面
     */
    @RequestMapping(value = "/{id}", method = GET, produces = TEXT_HTML_VALUE, params = "v")
    public ModelAndView editView(@PathVariable Long id, ModelAndView view) {
        view.setViewName("manager/information/site");
        Site site = siteService.findOne(id);
        view.addObject("site", site);
        return view;
    }

    /**
     * 保存后跳转到站点管理页
     */
    @RequestMapping(method = POST, produces = TEXT_HTML_VALUE)
    public ModelAndView createSite(Site site, ModelAndView view, MultipartFile file) {
        siteService.saveSite(site, file);
        view.setViewName("manager/information/site");
        return view;
    }

    @RequestMapping(method = PUT, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Site> updateSite(Site site) {
        return OK(siteService.save(site));
    }

    @RequestMapping(value = "/{siteId}", method = DELETE, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<String> deleteSite(@PathVariable("siteId") Long toDeleteId) {
//        return siteService.delete(toDeleteId);
        Site site = siteService.findOne(toDeleteId);
        site.setIsDelete(YES);
        siteService.save(site);
        return OK("delete success");
    }


    /**
     * 站点下的栏目树
     */
    @RequestMapping(value = "/{siteId}/channels", method = GET, produces = APPLICATION_JSON_VALUE)
    public Result<Channel> readChannels(@PathVariable Long siteId) {
        List<Channel> rootSiteChannels = baseChannelService.siteRootChannel(siteId);//所有的根目录(未过滤权限)
        List<Channel> notDeletedChannels = baseChannelService.notDeletedChannels();//所有的权限

        List<Channel> parentChannels = rootSiteChannels.stream().filter(notDeletedChannels::contains).collect(toList());//对根栏目过滤
        List<Channel> notRootChannels = notDeletedChannels.stream().filter(c -> c.getParentChannel() != null).collect(toList());//过滤根栏目
        return OK(recursiveNotDeletedChannels(parentChannels, notRootChannels));
    }

}
