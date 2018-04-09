package com.thousandsunny.thirdparty.domain.repository;


import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.CourseApply;
import com.thousandsunny.service.model.CourseRefundApply;
import com.thousandsunny.service.model.HpApply;
import com.thousandsunny.service.model.Job;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.ModuleKey.FlowState;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import com.thousandsunny.service.ModuleKey.EntrepreneursType;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.thirdparty.ModuleKey.SourceType;

/**
 * Created by guitarist on 2016/4/7.
 */
public interface AccountFlowRepository extends BaseRepository<AccountFlow> {

    AccountFlow findTop1ByAccountMemberTokenOrderByCreateDateDesc(String userToken);

    Page<AccountFlow> findByJobIdAndSourceIn(Long id,List<SourceType> sourceType,Pageable pageable);

    List<AccountFlow> findByJobIdAndSourceIn(Long id,List<SourceType> sourceType);

    AccountFlow findByAccountMemberTokenAndId(String userToken, Long id);

    AccountFlow findTop1ByAccountMemberTokenAndSourceOrderByCreateDate(String userToken, SourceType sourceType);

    @Query(value = "select SUM(entreprene1_.join_money) as col_0_0_  from tp_account_flow accountflo0_ join sr_entrepreneurs_apply entreprene1_ " +
            "join  core_member member2_  where accountflo0_.entrepreneurs_apply_id=entreprene1_.id and entreprene1_.member_id=member2_.id " +
            "and accountflo0_.state=:state and ( member2_.real_name like :text  or member2_.hp_account like :text  ) " +
            "and accountflo0_.create_date>:startTime   and accountflo0_.create_date<:endTime  and entreprene1_.type=:estpType and accountflo0_.pay_type=:payType " +
            "order by   accountflo0_.create_date desc", nativeQuery = true)
    BigDecimal sumJoinMoney(@Param("state") FlowState state, @Param("text") String text, @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("estpType") EntrepreneursType estpType, @Param("payType") PayType payType);

    AccountFlow findByEntrepreneursApplyIdAndTypeAndSourceAndState(Long entrepreneursId, ModuleKey.ChargeType payType, SourceType source, FlowState state);

    List<AccountFlow> findByWithdrawAccountMemberTokenAndRecordTypeAndState(String userToken, ModuleKey.RecordType billWithdraw, FlowState success);

    List<AccountFlow> findByAccountMemberTokenAndRecordTypeAndStateAndSourceIn(String userToken, ModuleKey.RecordType billIncome, FlowState success, List<SourceType> sourceTypeList);

    AccountFlow findByPartnerApplyIdAndSourceAndStateAndType(Long partnerApplyId, SourceType source, FlowState state, ModuleKey.ChargeType type);

    List<AccountFlow> findByEntrepreneursApplyIdAndSource(Long id, SourceType entrepreneurApply);

    AccountFlow findByCourseApplyAndSourceAndStateAndType(CourseApply courseApply, SourceType courseApply1, FlowState state, ModuleKey.ChargeType payOut);

    AccountFlow findByTypeAndRecordTypeAndSourceAndHpApplyId(ModuleKey.ChargeType payIn, ModuleKey.RecordType billRefund, SourceType jobRefund, Long id);

    AccountFlow findByAccountAndHpApplyAndStateAndType(Account account, HpApply hpApply, FlowState state, ModuleKey.ChargeType type);

    AccountFlow findByCourseApplyAndCourseRefundApplyAndSourceAndStateAndType(CourseApply courseApply, CourseRefundApply courseRefundApply, SourceType courseRefund, FlowState state, ModuleKey.ChargeType payIn);

    AccountFlow findByJobIdAndSourceAndPayType(Long id, SourceType jobNew, PayType payOffline);

    Page<AccountFlow> findByJobIdAndAccountNotAndSourceIn(Long id, Account account, List<SourceType> sourceTypes, Pageable pageable);
}
