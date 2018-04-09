package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.ShopCollect;
import com.thousandsunny.service.repository.ShopCollectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * 如果这些代码有用，那它们是guitarist在02/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class ShopCollectService extends BaseService<ShopCollect> {
    @Autowired
    private ShopCollectRepository shopCollectRepository;

    public ShopCollect findByMemberTokenAndShopId(String userToken, Long id) {
        return shopCollectRepository.findByMemberTokenAndShopId(userToken, id);
    }

    public Object findByMemberTokenAndShopIdAndCollectEver(String userToken, Long id, BooleanEnum isCollectEver) {
        return shopCollectRepository.findByMemberTokenAndShopIdAndCollectEver(userToken, id,isCollectEver);
    }
}
