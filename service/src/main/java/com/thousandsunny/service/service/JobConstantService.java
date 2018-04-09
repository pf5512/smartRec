package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.ModuleKey.JobConstantEnum;
import com.thousandsunny.service.model.JobConstant;
import com.thousandsunny.service.repository.JobConstantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.JobConstantEnum.EXPERIENCE;
import static com.thousandsunny.service.ModuleKey.JobConstantEnum.SALARY;
import static com.thousandsunny.service.ModuleTips.TIP_OUT_RANGE;

/**
 * 如果这些代码有用，那它们是guitarist在18/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class JobConstantService extends BaseService<JobConstant> {
    @Autowired
    private JobConstantRepository jobConstantRepository;

    public JobConstant findRangeByExactSalary(Integer exactSalary) {
        JobConstant jobConstant = jobConstantRepository.findByMaxValGreaterThanEquaAndMinValLessThanEqual(exactSalary);
        ifNullThrow(jobConstant, TIP_OUT_RANGE);
        return jobConstant;
    }

    public List<JobConstant> findSalaryOrWorkExperienceList(JobConstantEnum type) {
        return jobConstantRepository.findByAccountAndIsDeleteOrderByMinVal(type, NO);
    }

    public void delSalaryOrWorkExperience(long[] ids) {
        List<Long> jobConstantIds = new ArrayList<>();
        for (long id : ids) {
            jobConstantIds.add(id);
        }
        List<JobConstant> jobConstants = jobConstantRepository.findByIdIn(jobConstantIds);
        for (JobConstant jobConstant : jobConstants) {
            jobConstant.setIsDelete(YES);
        }
    }

    public List<JobConstant> getJobSalaryList() {
        return jobConstantRepository.findByAccountAndIsDelete(SALARY,NO);
    }

    public List<JobConstant> getJobExperienceList() {
        return jobConstantRepository.findByAccountAndIsDelete(EXPERIENCE,NO);
    }

    public void persistSalaryOrWorkExperience(String range, JobConstantEnum type, Long id) {
        JobConstant jobConstant = null;
        if (id == null) {
            jobConstant = new JobConstant();
        } else {
            jobConstant = jobConstantRepository.findOne(id);
        }
        jobConstant.setName(range);
        jobConstant.setAccount(type);
        String maxValStr = range.substring(range.indexOf("-") + 1, range.length() - 1).trim();
        String minValStr = range.substring(0, range.indexOf("-")).trim();
        jobConstant.setMaxVal(Integer.parseInt(maxValStr));
        jobConstant.setMinVal(Integer.parseInt(minValStr));
        jobConstantRepository.save(jobConstant);
    }
}
