package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.TerraceActivityClass;
import com.thousandsunny.service.repository.TerraceActivityClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;

/**
 * Created by Xiaoxuewei on 2016/11/30.
 */
@Service
public class TerraceActivityClassService extends BaseService<TerraceActivityClass> {
    @Autowired
    private TerraceActivityClassRepository terraceActivityClassRepository;

    public List<TerraceActivityClass> findClassList() {
        return terraceActivityClassRepository.findByIsDelete(NO);
    }
}
