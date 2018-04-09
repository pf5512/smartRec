package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.RedPacketCategory;
import com.thousandsunny.service.model.RedPacket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by mu.jie on 2017/2/23.
 */
public interface RedPacketRepository extends BaseRepository<RedPacket> {
    Page<RedPacket> findByCategory(RedPacketCategory type, Pageable pageable);
}
