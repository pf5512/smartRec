package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.ModuleKey.CheckType;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberCheckRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * Created by guitarist on 4/21/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public interface MemberSignInRecordRepository extends BaseRepository<MemberCheckRecord> {
    MemberCheckRecord findByMemberId(Long memberId);

    MemberCheckRecord findByMemberIdAndTypeAndDateBetween(Long memberId, CheckType type, Date startDate, Date endDate);

    Page<MemberCheckRecord> findByMemberIdAndDateBetween(Long memberId, Date startDate, Date endDate, Pageable pageable);

    Page<MemberCheckRecord> findByMemberInAndDateBetween(List<Member> list, Date startDate,Date endDate, Pageable pageable);
}
