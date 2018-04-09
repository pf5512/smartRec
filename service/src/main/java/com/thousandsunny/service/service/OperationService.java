package com.thousandsunny.service.service;

import com.thousandsunny.cms.domain.repository.OperationsRepository;
import com.thousandsunny.cms.model.Operation;
import com.thousandsunny.core.domain.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Xiaoxuewei on 2016/12/27.
 */

@Service
public class OperationService extends BaseService<Operation> {
    @Autowired
    private OperationsRepository operationsRepository;

    public Operation findOperation(String code){
        return operationsRepository.findByCode(code);
    }
}
