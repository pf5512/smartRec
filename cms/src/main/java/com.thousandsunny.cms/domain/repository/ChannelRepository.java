package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.ModuleKey;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.core.domain.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * Created by guitarist on 2016/4/6.
 */
public interface ChannelRepository extends BaseRepository<Channel> {
    List<Channel> findBySiteIdAndParentChannelIsNull(Long site);


    Channel findByAlias(String alias);

    List<Channel> findByParentChannelIsNull();

    List<Channel> findByParentChannelId(Long channelId);

    List<Channel> findBySiteId(Long siteId);

    List<Channel> findByParentChannelIdAndIsDelete(Long channelId, BooleanEnum no);

    List<Channel> findByMenuTypeAndParentChannelIsNull(ModuleKey.MenuType menuType);

    List<Channel> findByMenuTypeAndParentChannelIsNullOrderByWeightDescCreateTimeDesc(ModuleKey.MenuType menuType);

   List<Channel> findByParentChannelIsNullAndSiteIdIn(List<Long> ids);

    @Query("select max(c.weight) from Channel c where c.site.id =?1 ")
    Long findMaxWeight(Long id);

    List<Channel> findByParentChannelIsNotNullAndIsDelete(BooleanEnum no);

    List<Channel> findByIsDelete(BooleanEnum no);

    List<Channel> findByIsDeleteAndAllowTgAndSiteIdIn(BooleanEnum isDelete, BooleanEnum allowTg, List<Long> ids);

    List<Channel> findByIsDeleteAndParentChannelIsNull(BooleanEnum flag);
}
