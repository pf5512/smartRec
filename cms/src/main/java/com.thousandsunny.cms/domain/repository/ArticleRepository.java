package com.thousandsunny.cms.domain.repository;


import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by guitarist on 2016/4/7.
 */
public interface ArticleRepository extends BaseRepository<Article> {

    List<Article> findByChannelIdAndIsDeleteOrderByWeightDescPublishTimeDesc(Long channelId, BooleanEnum no);

    List<Article> findByIsDeleteOrderByDeleteDateDesc(BooleanEnum isDelete);

    List<Article> findByTitle(String title);

    Page<Article> findByChannelIdAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(Long channel, BooleanEnum isDeleted, BooleanEnum audited, Pageable pageable);

    List<Article> findByIsDeleteAndTitleContaining(BooleanEnum isDelete, String keyWord);

    List<Article> findTop20ByChannelAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(Channel channel, BooleanEnum isDelete, BooleanEnum audited);

    List<Article> findTop20ByChannelIdInAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(List<Long> ids, BooleanEnum isDeleted, BooleanEnum audited);

    List<Article> findByChannelIdInAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(List<Long> ids, BooleanEnum isDeleted, BooleanEnum audited);

    Page<Article> findByChannelIdInAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(List<Long> ids, BooleanEnum isDeleted, BooleanEnum audited, Pageable pageable);


    List<Article> findByTitleLike(String s, Pageable pageRequest);

    List<Article> findByKeywordsAndChannelIdAndAuditedAndIdNotInOrderByTopDescRecommendDescWeightDescPublishTimeDesc(String keywords, Long channelId, BooleanEnum audited, ArrayList<Long> longs);

    @Query("select max(a.weight) from Article a where a.channel.id = ?1")
    Long findMaxWeight(Long id);

    Page<Article> findByTitleLikeOrContentLikeAndChannelIdIn(String title, String content, List<Long> idContainer, Pageable pageRequest);

    List<Article> findByChannelIdAndIsDeleteAndTitleContainingOrderByWeightDescPublishTimeDesc(Long id, BooleanEnum no, String keyWord);

    Page<Article> findByChannelIdAndRecommendAndIsDeleteAndAuditedOrderByWeightDescPublishTimeDesc(Long id, BooleanEnum yes, BooleanEnum no, BooleanEnum yes1, Pageable pageable);

    List<Article> findByChannelIdAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(Long channelId, BooleanEnum no, BooleanEnum no1);

    List<Article> findByRecommendAndIsDeleteAndAuditedAndChannelIdInOrderByWeightDescPublishTimeDesc(BooleanEnum rec, BooleanEnum isDelete, BooleanEnum audit, List<Long> childChannelsIds, PageRequest pageRequest);

    Long countByPublishTimeAfter(Date date);

    List<Article> findByChannelIdInAndIsDeleteAndIdNotIn(Set<Long> platformActivityChannelIds, BooleanEnum no, Set<Long> readedArticleIds);
}
