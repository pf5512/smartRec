package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.model.Operation;
import com.thousandsunny.core.domain.repository.BaseRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在8/8/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface OperationsRepository extends BaseRepository<Operation> {
    List<Operation> findByIdIn(ArrayList<Long> longs);

    Operation findByCode(String code);
}
