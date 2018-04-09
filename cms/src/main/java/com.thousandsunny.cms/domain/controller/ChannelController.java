package com.thousandsunny.cms.domain.controller;

import com.thousandsunny.cms.domain.repository.SiteRepository;
import com.thousandsunny.cms.domain.service.BaseArticleService;
import com.thousandsunny.cms.domain.service.BaseChannelService;
import com.thousandsunny.cms.domain.service.SiteService;
import com.thousandsunny.cms.dto.IdNameList;
import com.thousandsunny.cms.dto.IdParentChannel;
import com.thousandsunny.cms.dto.SimpleChannel;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.DocumentFileService;
import com.thousandsunny.core.model.DocumentFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static com.thousandsunny.cms.ModuleKey.CONTENT_SIZE;
import static com.thousandsunny.cms.ModuleKey.MenuType;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by guitarist on 2016/4/6.
 */
@RestController
@RequestMapping(value = "/baseChannels")
public class ChannelController {

    @Autowired
    private BaseChannelService baseChannelService;
    @Autowired
    private BaseArticleService baseArticleService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private DocumentFileService documentFileService;

    @RequestMapping(value = "/{id}", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Channel> readChannel(@PathVariable Long id) {
        return OK(baseChannelService.findOne(id));
    }

    /**
     * 栏目的子栏目
     */
    @RequestMapping(value = "/{id}", method = GET, produces = APPLICATION_JSON_UTF8_VALUE, params = "child")
    public Result<Channel> childChannelsPure(@PathVariable Long id) {
        return OK(baseChannelService.childChannels(id).stream().map(c -> c.setChildChannels(null)).collect(toList()));
    }

    /**
     * 栏目的子栏目_每个栏目前20篇
     */
    @RequestMapping(value = "/{id}/articles", method = GET, produces = APPLICATION_JSON_UTF8_VALUE, params = "top20")
    public Result<Channel> childChannelsTop20(@PathVariable Long id) {
        List<Channel> list = baseChannelService.childChannels(id).stream().map(c -> c.setChildChannels(null)).collect(toList()).stream()
                .map(channel -> channel.setTruncatedArticles(baseArticleService.findTop20UnderChannel(channel))).collect(toList());
        return OK(list);
    }

    /**
     * 栏目的子栏目所有新闻的前20
     */
    @RequestMapping(value = "/{id}/articles", method = GET, produces = APPLICATION_JSON_UTF8_VALUE, params = "childTop20")
    public Result<Article> childChannelsAllTop20(@PathVariable Long id) {
        List<Long> ids = baseChannelService.childChannels(id).stream().map(c -> c.setChildChannels(null)).collect(toList()).stream().map(Channel::getId).collect(toList());
        List<Article> top20UnderChannels = baseArticleService.findTop20UnderChannels(ids);
        return OK(top20UnderChannels);
    }

    /**
     * 栏目的父栏目
     */
    @RequestMapping(value = "/parentChannel/{id}", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<IdParentChannel> parentChannels(@PathVariable Long id) {
        Channel parentChannel = baseChannelService.parentChannel(id);
        if (isNull(parentChannel)) return OK();
        Channel channel = parentChannel.setChildChannels(null);
        return OK(recursiveParentChannel(new IdParentChannel(channel.getId()), baseChannelService));
    }

    /**
     * 查看站点下的栏目
     */
    @RequestMapping(value = "/allChannel", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Channel> allChannel(Long id) {
        List<Channel> siteChannels = baseChannelService.siteRootChannel(id);
        List<Channel> visibleNotDeletedChannels = baseChannelService.visibleNotDeletedChannels();

        List<Channel> parentChannels = siteChannels.stream().filter(visibleNotDeletedChannels::contains).collect(toList());//对根栏目过滤
        List<Channel> notRootChannels = visibleNotDeletedChannels.stream().filter(c -> c.getParentChannel() != null).collect(toList());//过滤根栏目
        return OK(recursiveNotDeletedChannels(parentChannels, notRootChannels));
    }

    /**
     * 跳转到新增页面
     */
    @RequestMapping(method = GET, value = "turnAdd", produces = APPLICATION_JSON_VALUE)
    public ModelAndView turnAdd(Long id) {
        ModelAndView view = new ModelAndView();
        view.addObject("siteId", id);
        Long weight = baseChannelService.findMaxWeight(id);
        if (weight == null) {
            weight = 0l;
        }
        view.addObject("weight", (weight / 10 + 1) * 10);
        view.setViewName("manager/information/channel-add");
        return view;
    }

    /**
     * 调到编辑页面
     */
    @RequestMapping(method = GET, value = "/turnEdit", produces = APPLICATION_JSON_VALUE)
    public ModelAndView turnEdit(Long channelId, Long siteId) {
        ModelAndView view = new ModelAndView();
        List<Integer> ids = new ArrayList<>();
        Channel channel = baseChannelService.findOne(channelId);
        channel.getOperations().forEach(e -> ids.add(e.getId().intValue()));
        view.addObject("site", siteService.findOne(siteId));
        view.addObject("channel", channel);
        view.addObject("operations", ids);
        view.setViewName("manager/information/channel-edit");
        return view;
    }

    /**
     * 保存新增
     */
    @RequestMapping(method = POST, produces = APPLICATION_JSON_VALUE)
    public ModelAndView createChannel(Channel channel, String channelClassy, MultipartFile file, String operationIds, ModelAndView view) {
        if (file != null) {
            channel.setImg(documentFileService.saveDocumentFile(file, IMAGE));
        }
        baseChannelService.saveChannel(channel, channelClassy, operationIds);
        view.setViewName("redirect:/view/information/channel");
        return view;
    }

    @RequestMapping(method = POST, produces = APPLICATION_JSON_UTF8_VALUE, value = "editChannel")
    public ModelAndView editChannel(Channel channel, String channelClassy, MultipartFile file, String operationIds) {
        Channel oldChannel = baseChannelService.findOne(channel.getId());
        ifNotNullThen(oldChannel.getImg(), channel::setImg);
        if (!isNull(file) && !file.isEmpty()) {
            DocumentFile img = documentFileService.saveDocumentFile(file, IMAGE);
            channel.setImg(img);
        }
        baseChannelService.updateChannel(channel, channelClassy, operationIds);
        ModelAndView view = new ModelAndView();
        view.setViewName("redirect:/view/information/channel");
        return view;
    }

    /**
     * 删除类别及咨询
     */
    @RequestMapping(value = "/{id}", method = DELETE, produces = APPLICATION_JSON_UTF8_VALUE)
    public void deleteChannel(@PathVariable Long id) {
        baseChannelService.deleteById(id);
    }

    /**
     * 仅删除类别把类别下的咨询移到
     */
    @RequestMapping(value = "moveTo/{idstr}", method = DELETE, produces = APPLICATION_JSON_UTF8_VALUE)
    public void deleteChannel(@PathVariable String idstr, Long deleteId) {
        baseChannelService.moveToOther(idstr, deleteId);
    }

    /**
     * 栏目下的新闻
     * 内容截取指定内容返回
     */
    @RequestMapping(value = "{id}/articles", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Article> getArticlesByChannelId(@PathVariable Long id, PageVO pageVO) {
        Page<Article> articles = baseArticleService.findByChannelId(id, pageVO.pageRequest());
        Page<Article> articlePage = articles.map(a -> {
            String content = a.getContent();
            if (isBlank(content) || content.length() < CONTENT_SIZE)
                return a;
            a.setContent(content.substring(0, CONTENT_SIZE));
            return a;
        });
        return OK(articlePage);
    }

    /**
     * 栏目下推荐的新闻
     */
    @RequestMapping(value = "{id}/articles/rec", method = GET)
    public Result rec(@PathVariable Long id, PageVO pageVO) {
        return OK(baseArticleService.findRecByChannelId(id, pageVO.pageRequest()));
    }

    /**
     * 栏目下推荐的新闻
     */
    @RequestMapping(value = "/{id}/articles/recs", method = GET)
    public Result recArticles(@PathVariable Long id) {
        return OK(baseArticleService.findRecByParentChannelId(id));
    }

    /**
     * 功能管理
     */
    @RequestMapping(value = "/classControl", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Channel> classControl() {
        return baseChannelService.classControl();
    }


    /**
     * 跳转到类别新增
     */
    @RequestMapping(value = "/turnAddClass", method = GET, produces = APPLICATION_JSON_VALUE)
    public ModelAndView turnAddClass() {
        ModelAndView view = new ModelAndView();
        Long weight = baseChannelService.findMaxWeight(12l);
        view.addObject("weight", (weight / 10 + 1) * 10);
        view.addObject("siteId", siteRepository.findByEntityStatusAndIsDelete(NO, NO).get(0).getId());
        view.setViewName("manager/function/classMgt-add");
        return view;
    }

    /**
     * 跳转到类别编辑
     */
    @RequestMapping(value = "/turnEditClass", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView turnEditClass(Long channelId) {
        ModelAndView view = new ModelAndView();
        view.addObject("siteId", siteRepository.findByEntityStatusAndIsDelete(NO, NO).get(0).getId());
        List<Integer> ids = new ArrayList<>();
        Channel channel = baseChannelService.findOne(channelId);
        channel.getOperations().forEach(e -> ids.add(e.getId().intValue()));
        view.addObject("siteId", siteRepository.findByEntityStatusAndIsDelete(NO, NO).get(0).getId());
        view.addObject("channel", baseChannelService.findOne(channelId));
        view.addObject("operations", ids);
        view.setViewName("manager/function/classMgt-edit");
        return view;
    }

    /**
     * 保存类别新增
     */
    @RequestMapping(method = POST, produces = APPLICATION_JSON_UTF8_VALUE, value = "/AddClass")
    public ModelAndView AddClass(Channel channel, String channelClassy, String operationIds) {
        ModelAndView view = new ModelAndView();
        baseChannelService.saveChannel(channel, channelClassy, operationIds);
        view.setViewName("redirect:classMgt");
        return view;
    }

    /**
     * 跳转页面
     *
     * @Author mu.jie
     * @Date 2016/8/10 0010
     */
    @RequestMapping(value = "/classMgt", method = GET, produces = TEXT_HTML_VALUE)
    public ModelAndView classMgt(ModelAndView view) {
        view.setViewName("manager/function/classMgt");
        return view;
    }

    /**
     * 功能管理中的，查询专有类别
     *
     * @Author mu.jie
     * @Date 2016/7/31
     */
    @RequestMapping(value = "/specialChannel", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<Channel> specialChannel(MenuType menuType) {
        return baseChannelService.specialChannel(menuType);
    }

    /**
     * 递归父级栏目
     */
    private static IdParentChannel recursiveParentChannel(IdParentChannel channel, BaseChannelService baseChannelService) {
        Channel childChannel = baseChannelService.findOne(channel.getId());
        Channel parentChannel = childChannel.getParentChannel();
        if (isNull(parentChannel))
            return channel;
        IdParentChannel idParentChannel = new IdParentChannel(parentChannel.getId());
        channel.setParentChannel(idParentChannel);
        recursiveParentChannel(idParentChannel, baseChannelService);
        return channel;
    }

    /**
     * 递归未删除的-未删除的栏目
     */
    public static List<Channel> recursiveNotDeletedChannels(List<Channel> parentChannels, List<Channel> channels) {
        parentChannels.forEach(parentChannel -> {
            List<Channel> childChannels = channels.stream()
                    .filter(c -> c.getParentChannel().getId().equals(parentChannel.getId())).collect(toList());
            parentChannel.setChildChannels(childChannels);
            recursiveNotDeletedChannels(childChannels, channels);
        });
        return parentChannels;
    }

    /**
     * 递归未删除的-未删除的栏目idNameList
     */
    public static List<IdNameList> recursiveNotDeletedIdNameList(List<IdNameList> parentChannels, List<Channel> channels) {
        parentChannels.forEach(parentChannel -> {
            List<IdNameList> childChannels = channels.stream()
                    .filter(c -> c.getParentChannel().getId().equals(parentChannel.getId()))
                    .map(IdNameList::new)
                    .collect(toList());
            parentChannel.setList(childChannels);
            recursiveNotDeletedIdNameList(childChannels, channels);
        });
        return parentChannels;
    }

    /**
     * 递归simple可见栏目
     */
    public static List<SimpleChannel> recursiveVisibleSimpleChannels(List<SimpleChannel> parentChannels, BaseChannelService baseChannelService) {
        parentChannels.forEach(channel -> {
            List<SimpleChannel> childChannels = baseChannelService.notDeletedChild(channel.getId()).stream()
                    .map(SimpleChannel::new).collect(toList());
            channel.setChildren(childChannels);
            recursiveVisibleSimpleChannels(childChannels, baseChannelService);
        });
        return parentChannels;
    }

}