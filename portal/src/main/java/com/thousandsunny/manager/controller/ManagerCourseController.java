package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Course;
import com.thousandsunny.service.model.CourseSignUp;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.CourseService;
import com.thousandsunny.service.service.CourseSignUpService;
import com.thousandsunny.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.*;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.service.ModuleTips.TIP_NO_COURSE;
import static com.thousandsunny.service.ModuleTips.TIP_PARAM_FALSE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by mu.jie on 2017/2/21.
 */
@RestController
@RequestMapping(value = "/api/manager/course", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerCourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseSignUpService courseSignUpService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private MemberService memberService;

    /**
     * 11.3.1 学校课程列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/courseList", method = GET)
    public Result courseList(BackPageVo pageVo, String text, String userToken) {
        String[] COURSE_LIST_INFO = {"id", "name", "school.name", "isPartnerCourse", "platformPercent", "redPacketPercent", "price", "sortNO",
                "isEnableBoolean", "isEmploymentPlanning:isCareerPlanning",};
        Member member = memberService.findByToken(userToken);
        Page<Course> page = courseService.backgroundCoursePage(pageVo.pageRequest(), decodePathVariable(text), member);
        return OK(page.map(course -> {
            JSONObject jsonObject = propsFilter(course, COURSE_LIST_INFO);
            jsonObject.put("userType", member.getRole());
            enumToJson(course.getIsPlatformCourse(), jsonObject, "isPartnerCourse");
            enumToJson(course.getIsEmploymentPlanning(), jsonObject, "isCareerPlanning");
            if (isNotNull(course.getCreateTime()))
                jsonObject.put("publishTime", ISO_DATETIME_FORMAT.format(course.getCreateTime()));
            else jsonObject.put("publishTime", null);
            return jsonObject;
        }));
    }

    /**
     * 11.3.2 启用
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/courseEnable", method = POST)
    public Result courseEnable(Long id, String userToken) {
        Member member = memberService.findByToken(userToken);
        courseService.enableCourse(id, member);
        return OK();
    }

    /**
     * 11.3.3 删除
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/courseDelete", method = DELETE)
    public Result courseDelete(String id) {
        if (isNotBlank(id)) {
            Long[] ids = stringToLong(id);
            courseService.deleteCourse(ids);
        } else ifFalseThrow(false, TIP_PARAM_FALSE);
        return OK();
    }

    public Long[] stringToLong(String strs) {
        String[] str1 = strs.split(",");
        Long[] str2 = new Long[str1.length];
        for (int i = 0; i < str1.length; i++) {
            str2[i] = Long.valueOf(str1[i]);
        }
        return str2;
    }

    /**
     * 11.3.4 详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/courseDetail", method = GET)
    public Result courseDetail(Long id, String userToken) {
        String[] COURSE_DETAIL_INFO = {"school.id:schoolId", "name:courseName", "isPlatformCourse:isPartnerCourse", "platformPercent:platFormRate",
                "redPacketPercent:redPacketRate", "tag.id:courseClassId", "price:courseAmount", "day:days", "introduce:description",
                "sortNO:sort", "isEmploymentPlanning:isCareerPlanning"};
        String[] CLOUDFILE_INFO = {"path:imgUrl", "title:description"};
        Member member = memberService.findByToken(userToken);
        Course course = courseService.findOne(id);
        ifNullThrow(course, TIP_NO_COURSE);
        JSONObject jsonObject = propsFilter(course, COURSE_DETAIL_INFO);
        jsonObject.put("userType", member.getRole());
        if (isNotNull(course.getCreateTime()))
            jsonObject.put("publishTime", ISO_DATETIME_FORMAT.format(course.getCreateTime()));
        else valueIsNull(jsonObject, null, "publishTime");
        List<JSONObject> list = new ArrayList<>();
        if (!course.getPhotos().isEmpty()) {
            course.getPhotos().forEach(cloudFile -> list.add(propsFilter(cloudFile, CLOUDFILE_INFO)));
        }
        jsonObject.put("album", list);
        return OK(jsonObject);
    }

    /**
     * 11.3.5 新增/编辑
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/courseEdit", method = POST)
    public Result courseEdit(Course course, Long schoolId, Long courseClassId, String albumJson) {
        courseService.editCourse(course, schoolId, courseClassId, albumJson);
        return OK();
    }

    /**
     * 11.3.6 课程报名时间列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/dateList", method = GET)
    public Result dateList(BackPageVo pageVo, Long courseId) {
        String[] COURSE_SIGN_UP_LIST_INFO = {"id", "count", "signedCount", "isEnableBoolean"};
        Page<CourseSignUp> page = courseSignUpService.findCourseSignUpPage(pageVo.pageRequest(), courseId);
        return OK(page.map(courseSignUp -> {
            JSONObject jsonObject = propsFilter(courseSignUp, COURSE_SIGN_UP_LIST_INFO);
            if (isNotNull(courseSignUp.getDate()))
                jsonObject.put("date", ISO_DATE_FORMAT.format(courseSignUp.getDate()));
            else valueIsNull(jsonObject, null, "date");
            jsonObject.put("amount", accountFlowService.countCourseApplyFlowsMoneys(courseSignUp));
            return jsonObject;
        }));
    }

    /**
     * 11.3.7 报名时间删除
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/dateDelete", method = DELETE)
    public Result dateDelete(Long courseId, Long id) {
        courseSignUpService.deleteCourseSignUp(courseId, id);
        return OK();
    }

    /**
     * 11.3.8 报名时间编辑
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/dateEdit", method = POST)
    public Result dateEdit(Long courseId, Long id, Integer allowUserNum) {
        courseSignUpService.editCourseSignUp(courseId, id, allowUserNum);
        return OK();
    }

    /**
     * 11.3.9 报名时间新增
     *
     * @Author xiao xue wei
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/dateAdd", method = POST)
    public Result dateAdd(Long courseId, Integer allowUserNum, String dates) {
        courseSignUpService.addDates(courseId, allowUserNum, dates);
        return OK();
    }

    /**
     * 11.3.10 刷新课程
     *
     * @Author xiao xue wei
     * @Date 2017/2/26
     */
    @RequestMapping(value = "/freshCourse", method = POST)
    public Result publishCourse(Long id) {
        courseService.publishCourse(id);
        return OK();
    }

    /**
     * 11.3.11 报名时间启用
     *
     * @Author xiao xue wei
     * @Date 2017/2/26
     */
    @RequestMapping(value = "/enableSignUpTime", method = POST)
    public Result enableSignUpTime(Long id, Long courseId) {
        courseSignUpService.enableSignUpTime(id, courseId);
        return OK();
    }
}
