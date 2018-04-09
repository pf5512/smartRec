package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Position;

/**
 * Created by mu.jie on 2016/9/27.
 */
public interface PositionRepository extends BaseRepository<Position> {

    Position findByIdAndIsDelete (Long id,BooleanEnum no);
}
