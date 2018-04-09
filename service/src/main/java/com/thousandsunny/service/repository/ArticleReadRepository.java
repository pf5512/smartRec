package com.thousandsunny.service.repository;

import com.thousandsunny.cms.model.ArticleRead;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;

import java.util.List;
import java.util.Set;

/**
 * Created by 13336 on 2017/3/22.
 */
public interface ArticleReadRepository extends BaseRepository<ArticleRead> {
    ArticleRead findByMemberTokenAndArticleId(String token, Long id);

    List<ArticleRead> findByMemberIdAndArticleChannelIdInAndIsDeleteAndIsRead(Long id, Set<Long> platFormActivityChannelIds, ModuleKey.BooleanEnum no, ModuleKey.BooleanEnum yes);
}
