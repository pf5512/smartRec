package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.model.Menu;
import com.thousandsunny.core.domain.repository.BaseRepository;

import java.util.List;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public interface MenuRepository extends BaseRepository<Menu> {

    List<Menu> findByParentIsNull();
    List<Menu> findByParentId(Long id);
}
