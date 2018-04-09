package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.ModuleKey.RedPacketState;
import com.thousandsunny.service.model.RedPacketReceive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * Created by mu.jie on 2017/2/23.
 */
public interface RedPacketReceiveRepository extends BaseRepository<RedPacketReceive> {

    List<RedPacketReceive> findByMemberAndStateIn(Member member, List<RedPacketState> redPacketStates);

    List<RedPacketReceive> findByMemberAndStateAndIsRead(Member member, RedPacketState normal, BooleanEnum no);

    List<RedPacketReceive> findByMemberAndState(Member member, RedPacketState normal);

    List<RedPacketReceive> findByState(RedPacketState normal);

    List<RedPacketReceive> findByStateAndValidDateBetween(RedPacketState normal, Date now, Date date);

    List<RedPacketReceive> findByRedPacketIdAndState(Long id, RedPacketState normal);

    Long countByMemberIdAndStateAndValidDateGreaterThan(Long id, RedPacketState normal, Date date);

    RedPacketReceive findByIdAndState(Long id, RedPacketState normal);
}
