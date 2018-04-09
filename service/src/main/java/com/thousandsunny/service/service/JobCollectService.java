package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.JobCollect;
import com.thousandsunny.service.repository.JobCollectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;

/**
 * 如果这些代码有用，那它们是guitarist在12/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class JobCollectService extends BaseService<JobCollect> {
    @Autowired
    private JobCollectRepository jobCollectRepository;

    public JobCollect findByMemberTokenAndJobId(String userToken, Long id) {
        return jobCollectRepository.findByMemberTokenAndJobIdAndCollectEver(userToken, id,NO);
    }
}
