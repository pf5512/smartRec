package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.VideoCommentary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/13.
 */
public interface VideoCommentaryRepository extends BaseRepository<VideoCommentary> {
    Page<VideoCommentary> findByVideoIdOrderByDateDesc(Long videoId, Pageable pageable);

    VideoCommentary findByVideoIdAndCommentaryId(Long vId,Long cId);

    VideoCommentary findByIdAndMember(Long id, Member member);
}
