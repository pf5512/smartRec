package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.CourseApplyGen;
import com.thousandsunny.service.model.HpAccountGen;
import com.thousandsunny.service.repository.CourseApplyGenRepository;
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
 * Created by mu.jie on 2017/2/21.
 */
@Service
public class CourseApplyGenService extends BaseService<CourseApplyGen> {
    @Autowired
    private CourseApplyGenRepository courseApplyGenRepository;

    public CourseApplyGen getMaxNo() {
        Page<CourseApplyGen> gen = courseApplyGenRepository.findAll(new PageRequest(0, 1, DESC, "date"));
        List<CourseApplyGen> content = gen.getContent();
        if (!content.isEmpty() && now().equals(ofInstant(content.get(0).getDate().toInstant(), systemDefault()).toLocalDate()))
            return courseApplyGenRepository.save(new CourseApplyGen(content.get(0).getSeq() + 1));
        else
            return courseApplyGenRepository.save(new CourseApplyGen());
    }
}
