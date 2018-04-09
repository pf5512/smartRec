package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Course;
import com.thousandsunny.service.model.CourseApply;
import com.thousandsunny.service.model.CourseEvaluation;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.repository.CourseApplyRepository;
import com.thousandsunny.service.repository.CourseEvaluationRepository;
import com.thousandsunny.service.repository.CourseRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.JsonUtil.valueIsNull;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.MANAGER;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.COURSE_ORDER_TRAINED_COMMENTED;
import static com.thousandsunny.service.ModuleTips.*;

/**
 * Created by 13336 on 2017/2/14.
 */
@Service
public class CourseEvaluationService extends BaseService<CourseEvaluation> {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseEvaluationRepository courseEvaluationRepository;
    @Autowired
    private CourseApplyRepository courseApplyRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;

    public Page<CourseEvaluation> findSchoolEvaluations(School school, Pageable pageable) {
        List<Course> courseList = courseRepository.findBySchoolIdAndIsDeleteAndIsEnable(school.getId(), NO, YES);
        List<Long> courseIdList = new ArrayList<>();
        courseList.forEach(course -> courseIdList.add(course.getId()));
        return courseEvaluationRepository.findByCourseIdInAndIsDeleteAndIsEnable(courseIdList, NO, YES, pageable);
    }

    /**
     * 计算课程评价的平均分
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    public JSONObject countAverage(School school, JSONObject jsonObject) {
        List<Course> courseList = courseRepository.findBySchoolIdAndIsDeleteAndIsEnable(school.getId(), NO, YES);
        if (courseList.size() <= 0) {
            return valueIsNull(jsonObject, 0, "totalCount", "totalCount");
        }
        List<Long> courseIdList = new ArrayList<>();
        courseList.forEach(course -> courseIdList.add(course.getId()));
        //查找所有当前学校的课程
        Specification<CourseEvaluation> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rt.get("course").get("id").in(courseIdList));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<CourseEvaluation> list = courseEvaluationRepository.findAll(spec);
        Double average = 0D;//平均分
        if (!list.isEmpty()) {
            for (CourseEvaluation c : list) {
                average += c.getScore();
            }
            jsonObject.put("totalCount", list.size());
            jsonObject.put("averageScore", average / (list.size()));
        } else {
            jsonObject.put("totalCount", 0);
            jsonObject.put("averageScore", 0);
        }
        return jsonObject;
    }

    /**
     * 课程评价
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    public void evaluateCourse(Member member, String orderNo, CourseEvaluation courseEvaluation) {
        ifTrueThrow(courseEvaluation.getScore() > 10, TIP_SCAN_IN_TEN_NUM);
        CourseApply courseApply = courseApplyRepository.findBySerialNoAndMemberId(orderNo, member.getId());
        ifNullThrow(courseApply, TIP_NO_COURSEAPPLY);
        ifNotNullThen(courseEvaluation.getPhotos(), photos -> courseEvaluation.setPhotos(cloudFileRepository.save(photos)));
        courseEvaluation.setMember(member);
        courseEvaluation.setCourse(courseApply.getCourse());
        courseEvaluation.setCreateTime(new Date());
        courseEvaluationRepository.save(courseEvaluation);
        courseApply.setState(COURSE_ORDER_TRAINED_COMMENTED);
    }

    public Page<CourseEvaluation> findCourseEvaluationPage(Pageable pageable, String text, Member member) {
        Specification<CourseEvaluation> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if (member.getRole() == ModuleKey.AccountEnum.SCHOOL)
                predicates.add(rb.equal(rt.get("course").get("school").get("member").get("id"), member.getId()));
            ifNotBlankThen(text, e -> predicates.add(rb.or(rb.like(rt.get("member").get("mobile"), "%" + e + "%"),
                    rb.like(rt.get("member").get("realName"), "%" + e + "%"), rb.like(rt.get("member").get("hpAccount"), "%" + e + "%"))));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return courseEvaluationRepository.findAll(spec, pageable);
    }

    public void deleteEvaluations(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            CourseEvaluation courseEvaluation = courseEvaluationRepository.findOne(ids[i]);
            ifNullThrow(courseEvaluation, TIP_NO_COURSE_EVALUATION);
            courseEvaluation.setIsDelete(YES);
            courseEvaluationRepository.save(courseEvaluation);
        }
    }

    public void enableEvaluation(Long id, Member member) {
        ifFalseThrow(member.getRole() == MANAGER, TIP_NO_AUTHORITY);
        CourseEvaluation courseEvaluation = courseEvaluationRepository.findOne(id);
        ifNullThrow(courseEvaluation, TIP_NO_COURSE_EVALUATION);
        if (courseEvaluation.getIsEnable() == YES) courseEvaluation.setIsEnable(NO);
        else courseEvaluation.setIsEnable(YES);
        courseEvaluationRepository.save(courseEvaluation);
    }

    public CourseEvaluation findCourseEvaluation(Member member, CourseApply apply) {
        return courseEvaluationRepository.findByMemberIdAndCourseIdAndIsDeleteAndIsEnable(member.getId(), apply.getCourse().getId(), NO, YES);
    }
}
