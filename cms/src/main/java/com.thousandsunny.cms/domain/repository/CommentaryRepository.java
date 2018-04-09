package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * 如果这些代码有用，那它们是guitarist在8/4/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface CommentaryRepository extends BaseRepository<Commentary> {
    List<Commentary> findByArticleIdAndState(Long id, ModuleKey.BooleanEnum yes);

    List<Commentary> findByArticleTitleContainingOrderByCreateTimeDesc(String keyword);

    Page<Commentary> findByArticleIdAndState(Long articleId, BooleanEnum yes, Pageable pageRequest);

    List<Commentary> findByOrderByCreateTimeDesc();
    List<Commentary> findByMomentsId(Long id);
    Integer countByMomentsId(Long id);
}
