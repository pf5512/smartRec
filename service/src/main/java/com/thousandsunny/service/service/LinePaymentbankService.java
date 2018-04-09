package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.LinePaymentBank;
import com.thousandsunny.service.repository.LinePaymentBankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;

/**
 * Created by 13336 on 2016/11/30.
 */
@Service
public class LinePaymentbankService extends BaseService<LinePaymentBank> {
    @Autowired
    private LinePaymentBankRepository linePaymentBankRepository;

    public List<LinePaymentBank> findBankList() {
        List<LinePaymentBank> list = linePaymentBankRepository.findByIsDelete(NO);
        return list;
    }
}
