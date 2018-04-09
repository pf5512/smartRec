package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.JobBlocked;
import com.thousandsunny.service.repository.JobBlockedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 如果这些代码有用，那它们是guitarist在29/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class JobBlockedService extends BaseService<JobBlocked> {
    @Autowired
    private JobBlockedRepository jobBlockedRepository;

    public Boolean isJobBlocked(Long id, String userToken) {
        return jobBlockedRepository.findByMemberTokenAndJobId(userToken, id) != null;
    }
}
