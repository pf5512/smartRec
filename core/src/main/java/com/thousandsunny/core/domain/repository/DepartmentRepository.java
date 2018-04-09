package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Department;

import java.util.List;

/**
 * Created by mu.jie on 2016/9/20.
 */
public interface DepartmentRepository extends BaseRepository<Department> {

    Department findByManager(Long manager);

    List<Department> findByDepartmentTypeIn(List<ModuleKey.DepartmentType> departmentType);

    Department findById(Long id);

    List<Department> findByIsDeleteAndParentIsNull(ModuleKey.BooleanEnum no);
}
