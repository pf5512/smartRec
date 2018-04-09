package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.domain.repository.CmsTagRepository;
import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Course;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.repository.CourseApplyRepository;
import com.thousandsunny.service.repository.CourseRepository;
import com.thousandsunny.service.repository.SchoolRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.dayGap;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.cms.ModuleTips.TIP_NO_TAG;
import static com.thousandsunny.service.ModuleTips.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by 13336 on 2017/2/14.
 */
@Service
public class CourseService extends BaseService<Course> {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseApplyRepository courseApplyRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private CmsTagRepository cmsTagRepository;

    /**
     * 查找当前学校下所有课程
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    public List<Course> findCourseList(School school) {
        return courseRepository.findBySchoolIdAndIsDeleteAndIsEnable(school.getId(), NO, YES);
    }

    public Page<Course> findCoursePage(Pageable pageable, BooleanEnum isOnlyCooperate, String keyword, Long provinceId, Long cityId, Long areaId) {
        Specification<Course> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (isOnlyCooperate == YES)
                predicates.add(rb.equal(rt.get("isPlatformCourse"), YES));
            ifNotBlankThen(keyword, e -> predicates.add(rb.like(rt.get("school").get("name"), "%" + e + "%")));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("school").get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("school").get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("school").get("area").get("id"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return courseRepository.findAll(spec, pageable);
    }

    public List<Course> findSchoolCourses(Long id) {
        return courseRepository.findBySchoolIdAndIsDeleteAndIsEnable(id, NO, YES);
    }

    public Page<Course> backgroundCoursePage(Pageable pageable, String text, Member member) {
        Specification<Course> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (member.getRole() == ModuleKey.AccountEnum.SCHOOL)
                predicates.add(rb.equal(rt.get("school").get("member").get("id"), member.getId()));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("name"), "%" + t + "%"), rb.like(rt.get("school").get("name"), "%" + t + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return courseRepository.findAll(spec, pageable);
    }

    public void enableCourse(Long id, Member member) {
        ifFalseThrow(member.getRole() == ModuleKey.AccountEnum.MANAGER, TIP_NO_AUTHORITY);
        Course course = courseRepository.findOne(id);
        ifNullThrow(course, TIP_NO_COURSE);
        if (course.getIsEnable() == YES) course.setIsEnable(NO);
        else course.setIsEnable(YES);
        courseRepository.save(course);
    }

    public void deleteCourse(Long[] ids) {
        for (Long id : ids) {
            Course course = courseRepository.findOne(id);
            ifNullThrow(course, TIP_NO_COURSE);
            long countCourseApply = courseApplyRepository.countByCourseId(course.getId());
            ifTrueThrow(countCourseApply > 0, TIP_CAN_NOT_DELETE_COURSE);
            course.setIsDelete(YES);
            courseRepository.save(course);
        }

    }

    public void editCourse(Course course, Long schoolId, Long courseClassId, String albumJson) {
        School school = schoolRepository.findOne(schoolId);
        ifNullThrow(school, TIP_NO_SCHOOL);
        CmsTag cmsTag = cmsTagRepository.findByIdAndIsDelete(courseClassId, NO);
        ifNullThrow(cmsTag, TIP_NO_TAG);
        List<CloudFile> photos = saveCloudFiles(albumJson);
        ifTrueThrow(photos.isEmpty(), TIP_NO_COURSE_PHOTOS);
        ifFalseThrow(isNotBlank(course.getName()) && isNotNull(course.getIsPlatformCourse()) && isNotNull(course.getPlatformPercent()) &&
                isNotNull(course.getRedPacketPercent()) && isNotNull(course.getPrice()) && isNotNull(course.getDay()), TIP_INFORMATION_IS_WRONG);
        if (isNotNull(course.getId())) {
            Course oldCourse = courseRepository.findOne(course.getId());
            ifNotBlankThen(course.getIntroduce(), e -> oldCourse.setIntroduce(e));
            ifNotNullThen(course.getDay(), e -> oldCourse.setDay(e));
            ifNotNullThen(course.getCreateTime(), e -> oldCourse.setCreateTime(e));
            ifNotNullThen(course.getIsEmploymentPlanning(), e -> oldCourse.setIsEmploymentPlanning(e));
            oldCourse.setName(course.getName());
            oldCourse.setIsPlatformCourse(course.getIsPlatformCourse());
            oldCourse.setPlatformPercent(course.getPlatformPercent());
            oldCourse.setRedPacketPercent(course.getRedPacketPercent());
            oldCourse.setPrice(course.getPrice());
            oldCourse.setDay(course.getDay());
            oldCourse.setSchool(school);
            oldCourse.setTag(cmsTag);
            oldCourse.setPhotos(photos);
            courseRepository.save(oldCourse);
        } else {
            course.setSchool(school);
            course.setTag(cmsTag);
            course.setPhotos(photos);
            if (!isNotNull(course.getCreateTime())) course.setCreateTime(new Date());
            courseRepository.save(course);
        }
    }

    public List<CloudFile> saveCloudFiles(String albumJson) {
        JSONArray array = JSONObject.parseArray(albumJson);
        Iterator<Object> iterator = array.iterator();
        List<CloudFile> list = new ArrayList<>();
        while (iterator.hasNext()) {
            JSONObject jo = (JSONObject) iterator.next();
            CloudFile cloudFile = null;
            if (jo.getString("imgUrl") != null) {
                cloudFile = new CloudFile();
                cloudFile.setPath(jo.getString("imgUrl"));
            }
            if (jo.getString("description") != null) {
                String decription = jo.getString("description");
                cloudFile.setTitle(decription);
            }

            if (isNotNull(cloudFile)) list.add(cloudFileRepository.save(cloudFile));
        }
        return list;
    }

    public void publishCourse(Long id) {
        Course course = courseRepository.findOne(id);
        ifNullThrow(course, TIP_NO_COURSE);
        Date now = new Date();
        if (dayGap(course.getModifyTime(), now) >= 1) {
            course.setModifyTime(now);
            courseRepository.save(course);
        } else {
            ifFalseThrow(false, TIP_ONE_DAY_ONE_TIMES);
        }
    }
}
