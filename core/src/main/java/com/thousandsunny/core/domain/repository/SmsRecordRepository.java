package com.thousandsunny.core.domain.repository;


import com.thousandsunny.core.model.SmsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.thousandsunny.core.ModuleKey.SmsType;
public interface SmsRecordRepository extends BaseRepository<SmsRecord> {

    Page<SmsRecord> findByReceiverAndSmsTypeOrderBySendDateDesc(String receiver, SmsType smsType, Pageable PageRequest);

    SmsRecord findByReceiverAndSmsTypeAndCodeOrderBySendDateDesc(String receiver, SmsType smsType, Integer code);
}
