package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.thousandsunny.common.HTMLUtil.cleanHtmlTags;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleTips.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by 13336 on 2017/2/14.
 */
@RestController
@RequestMapping(value = "/api/portal/school", produces = APPLICATION_JSON_UTF8_VALUE)
public class SchoolController {
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private SchoolApplyService schoolApplyService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseEvaluationService courseEvaluationService;
    @Autowired
    private CourseSignUpService courseSignUpService;
    @Autowired
    private SchoolPhotoService schoolPhotoService;
    @Autowired
    private CourseApplyService courseApplyService;

    /**
     * 6.1 学校列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/schoolList", method = GET)
    public ResponseEntity schoolList(String keyword, Long provinceId, Long cityId, Long areaId, PageVO pageVO) {
        String[] SCHOOL_LIST_INFO = {"id", "name", "address", "ispartSchoolBoolean:isCooperate"};
        Page<School> page = schoolService.findSchoolPage(pageVO.pageRequest(), keyword, provinceId, cityId, areaId);
        JSONObject jsonObject = pageToJson(page, e -> {
            JSONObject jo = propsFilter(e, SCHOOL_LIST_INFO);
            SchoolPhoto schoolPhoto = schoolPhotoService.findSchoolFirstPhoto(e);
            if (isNotNull(schoolPhoto)) jo.put("firstImageUrl", schoolPhoto.getPhoto().getPath());
            else jo.put("firstImageUrl", null);
            return jo;
        });
        return ok(jsonObject);
    }

    /**
     * 6.2 学校详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/schoolDetail", method = GET)
    public ResponseEntity schoolDetail(String userToken, Long id) {
        String[] SCHOOL_DETAIL_INFO = {"id", "name", "address", "longitude:lng", "latitude:lat", "member.realName:contactName",
                "telephone:contactPhoneNumber", "link:webSiteUrl","province.name:provinceName","city.name:cityName","area.name:areaName"};
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        School school = schoolService.findOne(id);
        ifNullThrow(school, TIP_NO_SCHOOL);
        JSONObject jsonObject = propsFilter(school, SCHOOL_DETAIL_INFO);
        if (!school.getCourses().isEmpty()) {
            Set<String> courseTagSet = new HashSet<>();
            school.getCourses().forEach(course -> ifNotNullThen(course.getTag(), cmsTag -> courseTagSet.add(cmsTag.getName())));
            List<JSONObject> coursesList = new ArrayList<>();
            if (!courseTagSet.isEmpty()) {
                courseTagSet.forEach(courseTag -> {
                    JSONObject courseTagJo = new JSONObject();
                    courseTagJo.put("name", courseTag);
                    coursesList.add(courseTagJo);
                });
                jsonObject.put("coursesList", coursesList);
            } else jsonObject.put("coursesList", null);
        } else jsonObject.put("coursesList", null);
        jsonObject.put("isCollect", memberService.judgeIsCollectSchool(member, school));
        return ok(jsonObject);
    }

    /**
     * 6.3 学校入驻
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/schoolEnter", method = POST)
    public ResponseEntity schoolEnter(SchoolApply schoolApply, String userToken) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        schoolApplyService.saveSchoolApply(member, schoolApply);
        return OK;
    }

    /**
     * 6.4 学校收藏
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/schoolCollect", method = POST)
    public ResponseEntity schoolCollect(String userToken, Long id, OperatorType operatorType) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        School school = schoolService.findOne(id);
        ifNullThrow(school, TIP_NO_SCHOOL);
        memberService.collectSchool(school, operatorType, member);
        return OK;
    }

    /**
     * 6.5 学校环境相册列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/environmentList", method = GET)
    public ResponseEntity environmentList(Long id) {
        String[] ENVERONMENT_PHOTO_INFO = {"photo.path:path", "text:title"};
        School school = schoolService.findOne(id);
        ifNullThrow(school, TIP_NO_SCHOOL);
        JSONObject jsonObject = new JSONObject();
        List<SchoolPhoto> photos = schoolPhotoService.findSchoolEnvironmentPhotos(school);
        if (!photos.isEmpty()) {
            List<JSONObject> list = simpleMap(photos, e -> propsFilter(e, ENVERONMENT_PHOTO_INFO));
            jsonObject.put("list", list);
        } else jsonObject.put("list", new ArrayList<>());
        return ok(jsonObject);
    }

    /**
     * 6.6 学校作品相册列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/productionList", method = GET)
    public ResponseEntity productionList(Long id) {
        String[] PICTURE_LIST_INFO = {"photo.path:path", "text:title"};
        School school = schoolService.findOne(id);
        ifNullThrow(school, TIP_NO_SCHOOL);
        List<SchoolPhoto> photos = schoolPhotoService.findSchoolProductionPhotos(school);
        List<JSONObject> pictureList;
        if (!photos.isEmpty()) {
            pictureList = simpleMap(photos, e -> {
                JSONObject jo = propsFilter(e, PICTURE_LIST_INFO);
                jo.put("type", "PICTURE");
                return jo;
            });
        } else pictureList = new ArrayList<>();
        List<Course> courseList = courseService.findCourseList(school);
        List<JSONObject> albumList = new ArrayList<>();
        for (Course course : courseList) {
            if (!course.getPhotos().isEmpty()) {
                JSONObject jo = propsFilter(course.getPhotos().get(0), "path");
                jo.put("title", course.getName());
                jo.put("courseId", course.getId());
                jo.put("type", "ALBUM");
                albumList.add(jo);
            }
        }
        pictureList.addAll(albumList);
        return ok(listToJson(pictureList));
    }

    /**
     * 6.7 课程相册列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/coursePhotos", method = GET)
    public ResponseEntity coursePhotos(Long courseId) {
        String[] COURSE_PHOTO_INFO = {"path", "title"};
        Course course = courseService.findOne(courseId);
        ifNullThrow(course, TIP_NO_COURSE);
        JSONObject jsonObject = new JSONObject();
        if (!course.getPhotos().isEmpty()) {
            List<JSONObject> list = simpleMap(course.getPhotos(), e -> propsFilter(e, COURSE_PHOTO_INFO));
            jsonObject.put("list", list);
        } else jsonObject.put("list", null);
        return ok(jsonObject);
    }

    /**
     * 6.8 学校评价列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/evaluateList", method = GET)
    public ResponseEntity evaluateList(PageVO pageVO, Long id) {
        String[] SCHOOL_EVALUATE_PAGE_INFO = {"id", "content", "createTime:date", "course.id:courseId", "course.name:courseName",
                "member.headImage.path:userHeaderImageUrl", "member.realName:userRealName",};
        School school = schoolService.findOne(id);
        ifNullThrow(school, TIP_NO_SCHOOL);
        Page<CourseEvaluation> schoolEvaluations = courseEvaluationService.findSchoolEvaluations(school, pageVO.pageRequest());
        JSONObject body = pageToJson(schoolEvaluations, e -> {
            JSONObject jo = propsFilter(e, SCHOOL_EVALUATE_PAGE_INFO);
            List<JSONObject> pics;
            if (!e.getPhotos().isEmpty()) {
                pics = simpleMap(e.getPhotos(), photo -> propsFilter(photo, "path"));
            } else pics = null;
            jo.put("imageList", pics);
            return jo;
        });
        //平均分和总分
        body = courseEvaluationService.countAverage(school, body);
        return ok(body);
    }

    /**
     * 6.9 发现-课程列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/courseList", method = GET)
    public ResponseEntity courseList(BooleanEnum isOnlyCooperate, String keyword, Long provinceId, Long cityId, Long areaId, PageVO pageVO) {
        String[] COURSE_PAGE_INFO = {"id", "name", "day", "price", "isPlatformCourseBoolean:isCooperate", "isEmploymentPlanningBoolean:isEmploymentPlanning", "school.id:schoolId", "school.name:schoolName"};
        Page<Course> page = courseService.findCoursePage(pageVO.pageRequest(), isOnlyCooperate, decodePathVariable(keyword), provinceId, cityId, areaId);
        JSONObject body = pageToJson(page, course -> {
            JSONObject jo = propsFilter(course, COURSE_PAGE_INFO);
            if (!course.getPhotos().isEmpty()) jo.put("firstImageUrl", course.getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            return jo;
        });
        return ok(body);
    }

    /**
     * 6.10 学校-课程列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/schoolCourses", method = GET)
    public ResponseEntity schoolCourses(Long id) {
        String[] SCHOOL_COURSES_INFO = {"id", "name", "day", "price", "isEmploymentPlanningBoolean:isEmploymentPlanning", "isPlatformCourseBoolean:isCooperate", "school.id:schoolId", "school.name:schoolName"
        };
        JSONObject body = new JSONObject();
        List<Course> courseList = courseService.findSchoolCourses(id);
        List<JSONObject> list = simpleMap(courseList, course -> {
            JSONObject jo = propsFilter(course, SCHOOL_COURSES_INFO);
            if (!course.getPhotos().isEmpty()) jo.put("firstImageUrl", course.getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            jo.put("isSignFull", courseSignUpService.judgeIsSignFull(course));
            return jo;
        });
        body.put("list", list);
        return ok(body);
    }

    /**
     * 6.11 课程详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/courseDetail", method = GET)
    public ResponseEntity courseDetail(Long id, String userToken) {
        String[] COURSES_DETAIL_INFO = {"id", "name", "day", "price", "introduce", "isEmploymentPlanningBoolean:isEmploymentPlanning",
                "isPlatformCourseBoolean:isCooperate", "school.id:schoolId", "school.name:schoolName"};
        Course course = courseService.findOne(id);
        ifNullThrow(course, TIP_NO_COURSE);
        JSONObject body = propsFilter(course, COURSES_DETAIL_INFO);
        ifNotBlankThen(course.getIntroduce(), e -> body.replace("introduce", cleanHtmlTags(e)));
        body.put("isSignFull", courseSignUpService.judgeIsSignFull(course));
        ifNotBlankThen(userToken, token -> {
            Member member = memberService.findByToken(token);
            ifNullThrow(member, TIP_NO_MEMBER);
            body.put("isCollect", memberService.judgeIsCollectCourse(member, course));
        });
        double price = course.getPrice().doubleValue();
        double amount = price * course.getRedPacketPercent() / 100;
        body.put("redPacketPreferentialPrice", amount);
        return ok(body);
    }

    /**
     * 6.12 课程收藏
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/courseCollect", method = POST)
    public ResponseEntity courseCollect(String userToken, Long id, OperatorType operatorType) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        Course course = courseService.findOne(id);
        ifNullThrow(course, TIP_NO_COURSE);
        memberService.collectCourse(course, operatorType, member);
        return OK;
    }

    /**
     * 6.13 课程培训时间列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/trainTimeList", method = GET)
    public ResponseEntity trainTimeList(Long id) {
        JSONObject body = new JSONObject();
        Course course = courseService.findOne(id);
        ifNullThrow(course, TIP_NO_COURSE);
        List<CourseSignUp> signUpList = courseSignUpService.findSignUpList(course);
        ifTrueThrow(signUpList.isEmpty(), TIP_COURSE_HASNT_SIGN_DATE);
        body.put("startDate", signUpList.get(0).getDate().getTime());
        body.put("endDate", signUpList.get(signUpList.size() - 1).getDate().getTime());
        List<JSONObject> list = simpleMap(signUpList, signUp -> {
            JSONObject jo = propsFilter(signUp, "date.time:date");
            jo.put("surplusCount", signUp.getCount() - signUp.getSignedCount());
            return jo;
        });
        body.put("list", list);
        return ok(body);
    }

    /**
     * 6.14 课程报名
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/courseApply", method = POST)
    public ResponseEntity courseApply(CourseApply courseApply, String userToken, Long courseId, Date date) {
        Member member = memberService.findByToken(userToken);
        CourseApply one = courseApplyService.saveCourseApply(courseApply, member, courseId, date);
        JSONObject body = new JSONObject();
        body.put("orderNo", one.getSerialNo());
        return ok(body);
    }

    /**
     * 6.15课程报名付款
     *
     * @Author mu.jie
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/pay", method = POST)
    public ResponseEntity pay(String userToken, String orderNo, ModuleKey.PayType payType, String payPassword, String openId) {
        Member member = memberService.findByToken(userToken);
        Charge charge = courseApplyService.pay(member, orderNo, payType, payPassword, openId);
        if (charge == null) {
            return OK;
        }
        return ok(charge);
    }
}
