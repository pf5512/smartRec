package com.thousandsunny.core.domain.service;

import com.thousandsunny.core.domain.repository.MemberExtInfoRepository;
import com.thousandsunny.core.model.MemberExtInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberExtInfoService extends BaseService<MemberExtInfo> {
    @Autowired
    private MemberExtInfoRepository memberInfoRepository;
    @Autowired
    private MemberExtInfoRepository memberExtInfoRepository;

    public MemberExtInfo findByMemberToken(String userToken) {
        return memberInfoRepository.findByMemberToken(userToken);
    }

    public MemberExtInfo findByMemberMobile(String phoneNumber) {
        return memberExtInfoRepository.findByMemberMobile(phoneNumber);
    }
}
