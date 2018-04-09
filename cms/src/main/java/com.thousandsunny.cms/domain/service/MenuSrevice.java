package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.model.Menu;
import com.thousandsunny.cms.domain.repository.MenuRepository;
import com.thousandsunny.core.domain.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
@Service
public class MenuSrevice extends BaseService<Menu>{
    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> allRootMenusId() {
        return menuRepository.findByParentIsNull();
    }

}
