package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.HpAccountGen;
import com.thousandsunny.service.repository.HpAccountGenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.time.LocalDate.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * 如果这些代码有用，那它们是guitarist在10/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class HpAccountGenService extends BaseService<HpAccountGen> {
    @Autowired
    private HpAccountGenRepository accountGenRepository;

    public HpAccountGen lastHpAccount() {
        Page<HpAccountGen> gen = accountGenRepository.findAll(new PageRequest(0, 1, DESC, "date"));
        List<HpAccountGen> content = gen.getContent();
        if (!content.isEmpty() && now().equals(ofInstant(content.get(0).getDate().toInstant(), systemDefault()).toLocalDate()))
            return accountGenRepository.save(new HpAccountGen(content.get(0).getSeq() + 1));
        else
            return accountGenRepository.save(new HpAccountGen());

    }

}
