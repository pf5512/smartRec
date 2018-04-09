package com.thousandsunny.cms.domain.service;


import com.thousandsunny.cms.ModuleKey;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.cms.model.Operation;
import com.thousandsunny.cms.model.Site;
import com.thousandsunny.cms.domain.repository.ChannelRepository;
import com.thousandsunny.cms.domain.repository.OperationsRepository;
import com.thousandsunny.cms.domain.repository.SiteRepository;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.BaseUserDerails;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.cms.ModuleKey.ImgDirection.SIDEWAYS;
import static com.thousandsunny.cms.ModuleKey.SiteTypeEnum.FUNCTION;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by guitarist on 4/6/16.
 */
@Service
public class BaseChannelService extends BaseService<Channel> {

    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private BaseArticleService baseArticleService;
    @Autowired
    private UserService userService;
    @Autowired
    private AclService aclService;
    @Autowired
    private OperationsRepository operationsRepository;

    /**
     * 站点下的栏目
     */
    public List<Channel> siteRootChannel(Long siteId) {
        return channelRepository.findBySiteIdAndParentChannelIsNull(siteId);
    }

    public Result<Channel> saveChannel(Channel channel, String channelClassy,String operationIds) {
        if(operationIds != null){
            List<Operation> operations = new ArrayList<>();
            String [] oper = operationIds.split(",");
            newArrayList(oper).forEach(e->{
                operations.add(operationsRepository.findOne(Long.parseLong(e)));
            });
            channel.setOperations(operations);
        }
        parseChannel(channel, channelClassy);
        if (channel.getHasImg() == NO) {
            channel.setImgDirection(SIDEWAYS);
        }
        if (channel.getWeight() == null) {
            channel.setWeight((channelRepository.findMaxWeight(channel.getSite().getId()) / 10 + 1) * 10);
        }
        channel.setCreateTime(new Date());
        return OK(saveAndFlush(channel));
    }

    private void parseChannel(Channel channel, String channelClassy) {
        if (isNotBlank(channelClassy)) {
            String[] array = channelClassy.split(",");
            Long pId = Long.parseLong(array[array.length - 1]);
            channel.setParentChannel(channelRepository.findOne(pId));
            channel.setChannelPath(channelClassy);
        } else {
            channel.setParentChannel(null);
            channel.setChannelPath(null);
        }
    }

    public Result<Channel> updateChannel(Channel channel, String channelClassy,String operationIds) {
        Channel oldChannel = channelRepository.findOne(channel.getId());
        parseChannel(oldChannel, channelClassy);
        if(operationIds!=null){
            List<Operation> operations = new ArrayList<>();
            String [] oper = operationIds.split(",");
            newArrayList(oper).forEach(e->{
                operations.add(operationsRepository.findOne(Long.parseLong(e)));
            });
            oldChannel.setOperations(operations);
        }else {
            oldChannel.setOperations(null);
        }
        if (channel.getHasImg() == NO) {
            oldChannel.setImgDirection(SIDEWAYS);
        } else {
            oldChannel.setImgDirection(channel.getImgDirection());
        }
        oldChannel.setUrl(channel.getUrl());
        oldChannel.setName(channel.getName());
        oldChannel.setWeight(channel.getWeight());
        oldChannel.setContent(channel.getContent());
        oldChannel.setAllowTg(channel.getAllowTg());
        oldChannel.setHasImg(channel.getHasImg());
        oldChannel.setChannelType(channel.getChannelType());
        oldChannel.setContentType(channel.getContentType());
        oldChannel.setVisitType(channel.getVisitType());
        oldChannel.setMenuType(channel.getMenuType());
        oldChannel.setCreateTime(new Date());
        oldChannel.setPreview(channel.getPreview());
        channelRepository.save(oldChannel);
        return OK((oldChannel));
    }

    public List<Channel> findBySiteId(Long id) {
        if (id != null) {
            return channelRepository.findBySiteId(id);
        } else {
            return channelRepository.findAll();
        }
    }

    public void deleteById(Long id) {
        Channel oldChannel = channelRepository.findOne(id);
        oldChannel.setIsDelete(YES);
    }

    public void moveToOther(String idstr, Long deleteId) {
        String[] ids = idstr.split(",");
        Long id = Long.parseLong(ids[ids.length - 1]);
        Channel channel = channelRepository.findOne(id);
        Channel deletedChannel = channelRepository.findOne(deleteId);
        if (id != deleteId) {
            if (deletedChannel.getChildChannels().size() != 0) {
                List<Channel> channels = deletedChannel.getChildChannels();
                channels.forEach(c -> {
                    c.setParentChannel(channel);
                });
                deletedChannel.setChildChannels(null);
            } else {
                List<Article> articles = baseArticleService.findByChannelId(deleteId);
                articles.forEach(article -> {
                    article.setChannel(channel);
                    baseArticleService.save(article);
                });
            }
        }
        deletedChannel.setIsDelete(YES);
        channelRepository.save(deletedChannel);
    }

    /**
     * 子栏目
     */
    public List<Channel> childChannels(Long id) {
        return channelRepository.findByParentChannelIdAndIsDelete(id, NO);
    }

    //    @Cacheable(value = "channel", key = "#channelId")
    public List<Channel> notDeletedChild(Long channelId) {
        return channelRepository.findByParentChannelIdAndIsDelete(channelId, NO);
    }

    public Channel parentChannel(Long id) {
        return findOne(id).getParentChannel();
    }

    /**
     * 功能管理
     */
    public Result<Channel> classControl() {
        List<Site> sites = siteRepository.findByIsDeleteAndTypeIn(NO,newArrayList(FUNCTION));
        List<Channel> channels = channelRepository.findBySiteIdAndParentChannelIsNull(sites.get(0).getId());
        return OK(channels);
    }

    public Result<Channel> specialChannel(ModuleKey.MenuType menuType) {
        return OK(channelRepository.findByMenuTypeAndParentChannelIsNull(menuType));
    }

    /**
     * 所有的一级栏目
     */
    public List<Channel> allRootChannels() {
        return channelRepository.findByParentChannelIsNull();
    }

    public List<Channel> allRootChannelsSiteIdIn(List<Long> ids) {
        return channelRepository.findByParentChannelIsNullAndSiteIdIn(ids);
    }

    /**
     * 所有可投稿的栏目
     */
    public List<Channel> allAllowTgSiteIdIn(List<Long> ids) {
        return channelRepository.findByIsDeleteAndAllowTgAndSiteIdIn(NO, YES, ids);
    }

    public Long findMaxWeight(Long id) {
        return channelRepository.findMaxWeight(id);
//        return null;
    }

    public Result<Operation> findOperation(Long channelId) {
        Channel channel = channelRepository.findOne(channelId);
        return OK(channel.getOperations());
    }

    /**
     * 所有可见的-栏目
     */
    public List<Channel> visibleNotDeletedChannels() {
        BaseUserDerails userDetails = userService.userPrincipal();
        if (userDetails.getUsername().equals("admin")) {
            return channelRepository.findByIsDelete(NO);
        } else {
            return aclService.visibleChannels();
        }
    }

    /**
     * 所有没有删除的栏目
     */
    public List<Channel> notDeletedChannels() {
        return channelRepository.findByIsDelete(NO);
    }

    /**
     * 所有的非父级栏目
     */
    public List<Channel> notRootNotDeletedChannels() {
        return channelRepository.findByParentChannelIsNotNullAndIsDelete(NO);
    }


}

