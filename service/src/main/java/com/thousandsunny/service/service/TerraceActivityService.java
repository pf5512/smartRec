package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.TerraceActivity;
import com.thousandsunny.service.repository.TerraceActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 */
@Service
public class TerraceActivityService extends BaseService<TerraceActivity> {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TerraceActivityRepository terraceActivityRepository;

    public Page<TerraceActivity> findTerraceActivityList(String userToken, Long classId, Pageable pageable){
        Member member = memberRepository.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        return terraceActivityRepository.findByTaClassId(classId, pageable);
    }
}
