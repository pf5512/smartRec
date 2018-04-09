package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.LinePaymentBank;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * Created by Xiaoxuewei on 2016/11/30.
 */
public interface LinePaymentBankRepository extends BaseRepository<LinePaymentBank> {
    List<LinePaymentBank> findByIsDelete(BooleanEnum isDelete);

    LinePaymentBank findByIdAndIsDelete(Long x, BooleanEnum no);
}
