package com.thousandsunny.core.domain.repository;


import com.sun.org.apache.xpath.internal.operations.Bool;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Company;
import com.thousandsunny.core.model.Department;
import com.thousandsunny.core.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.thousandsunny.core.ModuleKey.*;

/**
 * Created by Administrator on 2016/6/7 0007.
 */
public interface MemberRepository extends BaseRepository<Member> {
    Member findByMobile(String mobile);

    Member findByToken(String uid);

    List<Member> findByMobileContaining(String mobile);

    Page<Member> findByUsernameLike(String s, Pageable pageRequest);

    List<Member> findAllByIsDeleteOrderByCreateTimeDesc(BooleanEnum no);

    Member findByUsernameAndIsDeleteAndValid(String username, BooleanEnum no, BooleanEnum yes);

    Member findByUsernameAndIsDelete(String username, BooleanEnum no);

    Member findByTokenAndIsDelete(String uid, BooleanEnum no);

    List<Member> findByTokenInAndIsDelete(List<String> uids, BooleanEnum no);

    Member findByMobileAndIsDelete(String mobile, BooleanEnum no);

    Member findByUsername(String username);

    List<Member> findByDepartmentId(Long departmentId);

    Page<Member> findByDepartmentIdIn(List<Long> ids, Pageable pageable);

    Page<Member> findByUsernameContaining(String keyWord, Pageable pageable);

    List<Member> findByCompanyIdAndPositionIdAndIsDelete(Long cId, Long pId, BooleanEnum no);

    List<Member> findByDepartmentInAndIsDelete(List<Department> departments, BooleanEnum no);

    Member findByCompanyIdAndDepartmentIdAndPositionType(Long company, Long department, PositionType departmentManager);

    List<Member> findByUsernameContaining(String keyWord);

    Member findByCompanyIdAndDepartmentIdAndPositionIsManager(Long company, Long department, BooleanEnum departmentManager);


    Page<Member> findByIdIn(List<Long> ids, Pageable pageable);

    Page<Member> findByIdIn(Set<Long> ids, Pageable pageable);

    Page<Member> findByIdInAndRealNameContaining(List<Long> ids, String username, Pageable pageable);

    Page<Member> findByIdInAndRealNameContaining(Set<Long> ids, String username, Pageable pageable);

    Member findByWxOpenId(String openid);

    Set<Member> findByIdIn(Set<Long> ids);

    Member findByHpAccount(String inviteCode);

    Long countByIsDeleteAndValid(BooleanEnum no, BooleanEnum yes);

    List<Member> findByIsDeleteAndValid(BooleanEnum no, BooleanEnum yes);

    Member findByMobileOrHpAccountAndIsDelete(String mobile, String hpAccount, BooleanEnum no);

    Member findByHpAccountAndIsDelete(String usernameOrMobile, BooleanEnum no);
}
