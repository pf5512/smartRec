package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.domain.repository.ChannelRepository;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;

/**
 * Created by ekoo on 2016/12/14.
 */
@Service
public class ChannelService extends BaseService<Channel> {

    private static final String[] TREE_JSON = {"id", "name:text", "weight","content:description"};

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ChannelRepository channelRepository;

    public List<JSONObject> channelTree(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<Channel> channels = channelRepository.findByIsDeleteAndParentChannelIsNull(NO);
        return parseChannel(channels);
    }

    private List<JSONObject> parseChannel(List<Channel> parent) {
        List<JSONObject> jsonObjects = simpleMap(parent, (channel -> {
            JSONObject jo = propsFilter(channel, TREE_JSON);
            List<Channel> childs = channel.getChildChannels().stream().filter(x -> x.getIsDelete() == NO).collect(Collectors.toList());
            jo.put("children", parseChannel(childs));
            return jo;
        }));
        return jsonObjects;
    }


    public void delChannel(Long id) {
        Channel channel = channelRepository.findOne(id);
        channel.setIsDelete(YES);
    }


    public void persistChannel(Long id, String parentsChannel, String channelName,Long sort,String description,Date publishTime) throws ParseException {
        Channel channel = null;
        Channel parent = null;
        if (id == null) {
            channel = new Channel();
        } else {
            channel = channelRepository.findOne(id);
        }

        if (parentsChannel != null&&!"".equals(parentsChannel)) {
            String[] arr = parentsChannel.split(",");
            String parentStr = arr[arr.length - 1];
            Long parentId = Long.parseLong(parentStr);
            parent = channelRepository.findOne(parentId);
        }
        channel.setName(channelName);
        channel.setContent(description);
        channel.setParentChannel(parent);
        channel.setWeight(sort);
        channel.setCreateTime(publishTime);
        channelRepository.save(channel);
    }


    public Channel channelInfo(Long id){
        return channelRepository.findOne(id);
    }

    public List<Channel> findActivityClassList() {
        return channelRepository.findByParentChannelIdAndIsDelete(20L, NO);
    }
}
