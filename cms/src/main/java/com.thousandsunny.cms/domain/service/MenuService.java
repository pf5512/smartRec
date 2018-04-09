package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.model.Menu;
import com.thousandsunny.cms.domain.repository.MenuRepository;
import com.thousandsunny.core.domain.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在8/23/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class MenuService extends BaseService<Menu> {
    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> findByParentIsNull() {
        return menuRepository.findByParentIsNull();
    }

    public List<Menu> findByParentId(Long id) {
        return menuRepository.findByParentId(id);
    }
}
