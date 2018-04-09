package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.MemberRecRel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by admin on 2016/10/20.
 */
public interface MemberRecRelRepository extends BaseRepository<MemberRecRel> {
    List<MemberRecRel> findByMemberToken(String userToken);

    List<MemberRecRel> findByMemberTokenOrP1OrP2(String token, Long P1, Long P2);

    List<MemberRecRel> findByMemberTokenOrP1OrP2OrP3(String token, Long P1, Long P2,Long p3);

    List<MemberRecRel> findByMemberTokenOrP1(String token, Long P1);

    MemberRecRel findTop1ByMemberTokenOrderByDateDesc(String userToken);

    MemberRecRel findTop1ByP1OrderByDateDesc(Long id);

    MemberRecRel findTop1ByP2OrderByDateDesc(Long id);

    MemberRecRel findTop1ByP3OrderByDateDesc(Long id);

    @Query("FROM MemberRecRel m where m.member.token=:token  And (m.p1=:p1  or m.p2=:p2 or m.p3=:p3)")
    Page<MemberRecRel> findTop1ByMemberTokenOrP1OrP2OrP3OrderByDate(@Param("token") String token, @Param("p1") Long P1, @Param("p2") Long P2, @Param("p3") Long p3, Pageable pageable);

    List<MemberRecRel> findByP3(Long p3);

    List<MemberRecRel> findByP3OrP2(Long userId, Long userId1);

    List<MemberRecRel> findByP3OrP2OrP1(Long userId, Long userId1, Long userId2);
}
