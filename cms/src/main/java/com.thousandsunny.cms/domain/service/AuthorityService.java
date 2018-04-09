package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.model.Authority;
import com.thousandsunny.cms.domain.repository.AuthorityRepository;
import com.thousandsunny.core.domain.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 如果这些代码有用，那它们是guitarist在8/23/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class AuthorityService extends BaseService<Authority> {
    @Autowired
    private AuthorityRepository authorityRepository;

}
