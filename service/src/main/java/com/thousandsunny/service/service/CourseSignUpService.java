package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.Course;
import com.thousandsunny.service.model.CourseApply;
import com.thousandsunny.service.model.CourseSignUp;
import com.thousandsunny.service.repository.CourseApplyRepository;
import com.thousandsunny.service.repository.CourseRepository;
import com.thousandsunny.service.repository.CourseSignUpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by 13336 on 2017/2/15.
 */
@Service
public class CourseSignUpService extends BaseService<CourseSignUp> {
    @Autowired
    private CourseSignUpRepository courseSignUpRepository;
    @Autowired
    private CourseApplyRepository courseApplyRepository;
    @Autowired
    private CourseRepository courseRepository;

    /**
     * 判断课程报名是否已爆满
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    public Boolean judgeIsSignFull(Course course) {
        List<CourseSignUp> signUpList = courseSignUpRepository.findByCourseIdAndIsDelete(course.getId(), NO);
        if (!signUpList.isEmpty()) {
            Integer count = 0;
            for (CourseSignUp courseSignUp : signUpList) {
                count += (courseSignUp.getCount() - courseSignUp.getSignedCount());
            }
            if (count > 0) return false;
            else return true;
        } else return true;
    }

    public List<CourseSignUp> findSignUpList(Course course) {
        return courseSignUpRepository.findByCourseIdAndIsDeleteOrderByDate(course.getId(), NO);
    }

    public Page<CourseSignUp> findCourseSignUpPage(Pageable pageable, Long courseId) {
        return courseSignUpRepository.findByCourseIdAndIsDelete(courseId, NO, pageable);
    }

    public void deleteCourseSignUp(Long courseId, Long id) {
        CourseSignUp courseSignUp = courseSignUpRepository.findByIdAndCourseId(id, courseId);
        ifNullThrow(courseSignUp, TIP_NO_COURSE_SIGN_UP);
        Specification<CourseApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("course").get("id"), courseId));
            Date date = new Date(courseSignUp.getDate().getTime());
            Date date1 = new Date(courseSignUp.getDate().getTime() + (24 * 3600 * 1000));
            predicates.add(rb.greaterThanOrEqualTo(rt.get("trainDate"), date));
            predicates.add(rb.lessThan(rt.get("trainDate"), date1));
            return rq.where(toArray(predicates, Predicate.class)).getRestriction();
        };
        long courseApplyNum = courseApplyRepository.count(spec);
        ifTrueThrow(courseApplyNum > 0, TIP_CAN_NOT_DELETE_SIGN_UP_DATE);
        courseSignUp.setIsDelete(YES);
        courseSignUpRepository.save(courseSignUp);
    }

    public void editCourseSignUp(Long courseId, Long id, Integer allowUserNum) {
        CourseSignUp courseSignUp = courseSignUpRepository.findByIdAndCourseIdAndIsDelete(id, courseId, NO);
        ifNullThrow(courseSignUp, TIP_NO_COURSE_SIGN_UP);
        ifTrueThrow(allowUserNum < courseSignUp.getSignedCount(), TIP_CAN_NOT_DOWN_SIGNED_NUM);
        allowUserNum = allowUserNum == null ? 0 : allowUserNum;
        courseSignUp.setCount(allowUserNum);
        courseSignUpRepository.save(courseSignUp);
    }

    public void addDates(Long courseId, Integer allowUserNum, String dates) {
        List<Date> dateList = getDateList(dates);
        ifTrueThrow(dateList.isEmpty(), TIP_HAS_NOT_WRITE_DATE);
        Course course = courseRepository.findOne(courseId);
        ifNullThrow(course, TIP_NO_COURSE);
        dateList.forEach(date -> {
            CourseSignUp courseSignUp = new CourseSignUp();
            Integer a = allowUserNum == null ? 0 : allowUserNum;
            courseSignUp.setCount(a);
            courseSignUp.setSignedCount(0);
            courseSignUp.setDate(date);
            courseSignUp.setCourse(course);
            courseSignUpRepository.save(courseSignUp);
        });
    }

    public List<Date> getDateList(String dates) {
        List<Date> dateList = new ArrayList<>();
        if (isNotBlank(dates)) {
            String[] dateArray = dates.split(",");
            List<String> list = newArrayList(dateArray);
            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            list.forEach(s -> {
                try {
                    dateList.add(fmt.parse(s));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        }
        return dateList;
    }

    public void enableSignUpTime(Long id, Long courseId) {
        CourseSignUp courseSignUp = courseSignUpRepository.findByIdAndCourseIdAndIsDelete(id, courseId, NO);
        ifNullThrow(courseSignUp, TIP_NO_COURSE_SIGN_UP);
        if (courseSignUp.getIsEnable() == YES) courseSignUp.setIsEnable(NO);
        else courseSignUp.setIsEnable(YES);
        courseSignUpRepository.save(courseSignUp);
    }
}
