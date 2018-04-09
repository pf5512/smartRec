package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.ResumeWorkExp;
import com.thousandsunny.service.repository.ResumeWorkExpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by admin on 2016/10/26.
 */
@Service
public class ResumeWorkExpService extends BaseService<ResumeWorkExp> {
    @Autowired
    private ResumeWorkExpRepository resumeWorkExpRepository;
}
