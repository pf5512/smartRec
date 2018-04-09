package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.ResumeIntention;
import com.thousandsunny.service.repository.ResumeIntentionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2016/10/26.
 */
@Service
public class ResumeIntentionService extends BaseService<ResumeIntention> {
    @Autowired
    private ResumeIntentionRepository resumeIntentionRepository;
}
