package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.JobConstant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.thousandsunny.service.ModuleKey.JobConstantEnum;

public interface JobConstantRepository extends BaseRepository<JobConstant> {
    List<JobConstant> findByAccountAndIsDelete(JobConstantEnum jobConstant,BooleanEnum flag);

    List<JobConstant> findByAccountAndIsDeleteOrderByMinVal(JobConstantEnum jobConstant,BooleanEnum flag);

    List<JobConstant> findByIdIn(List<Long> ids);

    @Query("from JobConstant j where j.minVal<= :salary and j.maxVal >= :salary")
    JobConstant findByMaxValGreaterThanEquaAndMinValLessThanEqual(@Param("salary") Integer exactSalary);


}
