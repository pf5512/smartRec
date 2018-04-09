package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.repository.PositionRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Company;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * Created by ekoo on 2016/11/21.
 */
@Service
public class PositionService extends BaseService<Position> {

    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private MemberRepository memberRepository;

    public void persistPosition(Long id, String parentsChannel, String channelName,String userToken){
        Member member = memberRepository.findByToken(userToken);
        Company company = member.getCompany();
        Position position = null;
        Position parent = null;
        if(id==null){
            position = new Position();
        }else {
            position = positionRepository.findOne(id);
        }

        if(parentsChannel!=null){
            String[] arr = parentsChannel.split(",");
            String parentStr = arr[arr.length-1];
            Long parentId = Long.parseLong(parentStr);
            parent = positionRepository.findOne(parentId);
        }
        position.setParent(parent);
        position.setName(channelName);
        position.setDate(new Date());
        position.setCompany(company);
        positionRepository.save(position);
    }

    public void delPosition(Long id) {
        Position position = positionRepository.findOne(id);
        position.setIsDelete(YES);
    }

    public Position findPosition(Long id) {
      return  positionRepository.findOne(id);
    }

}
