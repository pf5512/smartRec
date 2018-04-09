package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.MemberRegRel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRegRelRepository extends BaseRepository<MemberRegRel> {
    List<MemberRegRel> findByMemberToken(String token);

    List<MemberRegRel> findByMemberTokenOrP1OrP2(String token, Long P1, Long P2);

    List<MemberRegRel> findByP3(Long P3);

    List<MemberRegRel> findByP2(Long P2);

    List<MemberRegRel> findByP1(Long P1);

    @Query("FROM MemberRegRel m where m.member.token=:token  And (m.p1=:p1  or m.p2=:p2 or m.p3=:p3)")
    Page<MemberRegRel> findTop1ByMemberTokenOrP1OrP2OrP3(@Param("token") String token, @Param("p1") Long P1, @Param("p2") Long P2, @Param("p3") Long p3, Pageable pageable);

    List<MemberRegRel> findByMemberTokenOrP1(String token, Long P1);

    MemberRegRel findTop1ByP3OrderByDateDesc(Long id);

    MemberRegRel findTop1ByP2OrderByDateDesc(Long id);

    MemberRegRel findTop1ByP1OrderByDateDesc(Long id);
}
