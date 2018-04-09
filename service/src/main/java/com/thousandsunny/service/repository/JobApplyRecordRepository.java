package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.JobApplyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.thousandsunny.service.ModuleKey.RecState;
import static com.thousandsunny.service.ModuleKey.RecruitmentType;

/**
 * Created by admin on 2016/10/26.
 */
public interface JobApplyRecordRepository extends BaseRepository<JobApplyRecord> {
    Page<JobApplyRecord> findByShopOwnerTokenAndRecState(String userToken, RecState recState, Pageable pageable);

    Page<JobApplyRecord> findByShopOwnerTokenAndJobRecTypeAndRecStateOrderByResignDateDesc(String userToken, RecruitmentType recruitmentType, RecState recState, Pageable pageable);

    List<JobApplyRecord> findByJobId(Long id);

    Page<JobApplyRecord> findByReferralTokenOrderByDateDesc(String userToken, Pageable pageable);

    Page<JobApplyRecord> findByReceiverTokenOrderByDateDesc(String userToken, Pageable pageable);

    JobApplyRecord findByReferralTokenAndReceiverTokenAndJobId(String referralToken, String receiverToken, Long id);

    Integer countByJobRecTypeAndRecStateAndShopAreaId(RecruitmentType recruitmentType, RecState recState, Long id);

    Page<JobApplyRecord> findByJobRecTypeAndRecStateAndShopAreaIdOrderByDateDesc(RecruitmentType recruitmentType, RecState recState, Long id, Pageable pageable);

    @Query("select  count (j) from JobApplyRecord j " +
            "where j.job.recType=:r1 and j.recState=:r2 and j.shop.area.id=:rid " +
            "and(((MONTH(j.resignDate)>MONTH(j.startDate) )and (DAY(j.resignDate)<DAY(startDate)))" +
            "or MONTH(j.resignDate)=MONTH(j.startDate))")
    Integer countLessThanAMonthWorker(@Param("r1") RecruitmentType recruitmentType, @Param("r2") RecState recState, @Param("rid") Long id);

    @Query("select  count (j) from JobApplyRecord j " +
            "where j.job.recType=:r1 and j.recState=:r2 and j.shop.area.id=:rid " +
            "and (((MONTH(j.resignDate)>MONTH(j.startDate) )and (DAY(j.resignDate)>DAY(startDate)))" +
            "or MONTH(j.resignDate)-MONTH(j.startDate)>1)")
    Integer countMoreThanAMonthWorker(@Param("r1") RecruitmentType recruitmentType, @Param("r2") RecState recState, @Param("rid") Long id);

//    @Query("select  j from JobApplyRecord j " +
//            "where j.job.recType=:r1 and j.recState=:r2 and j.shop.area.id=:rid " +
//            "and(((MONTH(j.resignDate)>MONTH(j.startDate) )and (DAY(j.resignDate)<DAY(startDate)))" +
//            "or MONTH(j.resignDate)=MONTH(j.startDate))" +
//            "order by j.date desc")
//    Page<JobApplyRecord> findLessThanAMonthWorker(@Param("r1") RecruitmentType recruitmentType, @Param("r2") RecState recState, @Param("rid") Long id, Pageable pageable);

    List<JobApplyRecord> findByJobRecTypeAndShopAreaIdAndRecStateOrderByDate(RecruitmentType recruitmentType, Long id, RecState recState);

    @Query("select  j from JobApplyRecord j " +
            "where j.job.recType=:r1 and j.recState=:r2 and j.shop.area.id=:rid " +
            "and(((MONTH(j.resignDate)>MONTH(j.startDate) )and (DAY(j.resignDate)>DAY(startDate)))" +
            "or MONTH(j.resignDate)-MONTH(j.startDate)>1)" +
            "order by j.date desc")
    Page<JobApplyRecord> findMoreThanAMonthWorker(@Param("r1") RecruitmentType recruitmentType, @Param("r2") RecState recState, @Param("rid") Long id, Pageable pageable);

    List<JobApplyRecord> findByReferralToken(String userToken);

    Page<JobApplyRecord> findByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(Set<Long> memberIds,RecruitmentType type, RecState recState,Pageable pageable);

    long countByReferralIdInAndJobRecTypeAndRecStateOrderByDateDesc(Set<Long> memberIds, RecruitmentType type, RecState recState);

    Page<JobApplyRecord> findByReferralIdInAndJobRecTypeAndRecStateInAndStartDateLessThanOrderByDateDesc(Set<Long> ids, RecruitmentType type, List<RecState> recState, Date startDate, Pageable pageable);

    Page<JobApplyRecord> findByReferralIdInAndJobRecTypeAndRecStateInAndStartDateGreaterThanOrderByDateDesc(Set<Long> ids, RecruitmentType type, List<RecState> recState, Date startDate, Pageable pageable);

    Page<JobApplyRecord> findByReceiverIdIn(Set<Long> ids, Pageable pageable);

    JobApplyRecord findByJobIdAndReceiverToken(Long id, String receiver);

    List<JobApplyRecord> findByJobIdAndRefundAndRecStateIn(Long id, ModuleKey.RefundEnum refund, List<RecState> list);

    JobApplyRecord findByIdAndReceiverToken(Long id, String receiver);

    JobApplyRecord findByIdAndShopOwnerTokenAndRecState(Long id, String userToken, RecState recState);


    JobApplyRecord findByJobIdAndReferralTokenAndReceiverToken(Long id, String referral, String receiver);

    JobApplyRecord findByShopOwnerTokenAndId(String userToken, Long id);

    Page<JobApplyRecord> findByIdIn(Set<Long> ids, Pageable pageable);

    List<JobApplyRecord> findByReceiverToken(String token);

    JobApplyRecord findByReceiverTokenAndJobIdAndRecState(String token, Long id, RecState working);

    /**
     * 正在工作中的申请记录
     */
    JobApplyRecord findByReferralTokenAndRecState(Member inviter, RecState working);

    /**
     * 正在工作中的申请记录
     */
    Integer countByReferralTokenAndRecState(String inviterToken, RecState working);

    JobApplyRecord findByJobIdAndRecState(Long id, RecState working);

    List<JobApplyRecord> findByRecStateAndJobRecTypeAndResignDateIsNull(RecState recState, RecruitmentType recruitmentType);

    JobApplyRecord findByReceiverIdAndJobId(Long userId, Long jobId);

    List<JobApplyRecord> findByRecStateAndJobRecTypeAndShopAreaIdAndResignDateIsNull(RecState recState, RecruitmentType recruitmentType, Long area);

    Page<JobApplyRecord> findByShopOwnerTokenAndJobRecTypeAndRecStateInOrderByDateDesc(String userToken, RecruitmentType recruitmentType, List<RecState> recStates, Pageable pageable);
}
