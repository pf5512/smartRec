package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.TerraceActivityMember;
import com.thousandsunny.service.repository.TerraceActivityMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Service
public class TerraceActivityMemberService extends BaseService<TerraceActivityMember> {

    @Autowired
    private TerraceActivityMemberRepository terraceActivityMemberRepository;

    public boolean checkIsRead(String userToken, Long terraceActivityId) {
        List<TerraceActivityMember> list = terraceActivityMemberRepository.findByMemberTokenAndTerraceActivityId(userToken, terraceActivityId);
        return isNotEmpty(list);
    }
}
