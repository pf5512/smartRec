package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.domain.service.CmsTagService;
import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey.CourseApplyState;
import com.thousandsunny.service.ModuleKey.PhotoType;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.*;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.COURSE_ORDER_REFUNDING;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.COURSE_ORDER_REFUND_FAIL;
import static com.thousandsunny.service.ModuleTips.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by 13336 on 2017/2/15.
 */
@RestController
@RequestMapping(value = "/api/manager/school", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerSchoolController {
    @Autowired
    private SchoolApplyService schoolApplyService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private CmsTagService cmsTagService;
    @Autowired
    private SchoolPhotoService schoolPhotoService;
    @Autowired
    private CourseApplyService courseApplyService;
    @Autowired
    private CourseEvaluationService courseEvaluationService;
    @Autowired
    private WithdrawAccountService withdrawAccountService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private CourseRefundApplyService courseRefundApplyService;

    /**
     * 3.1.1 学校申请列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/schoolApplyList", method = GET)
    public Result schoolApplyList(BackPageVo pageVO, String tableType, String text, Long province, Long city, Long area) {
        String[] SCHOOLAPPLY_LIST_INFO = {"id", "member.realName", "member.hpAccount", "name",};
        Page<SchoolApply> page = schoolApplyService.findSchoolApplyPage(pageVO.pageRequest(), tableType, decodePathVariable(text), province, city, area);
        return OK(page.map(schoolApply -> {
            JSONObject jsonObject = propsFilter(schoolApply, SCHOOLAPPLY_LIST_INFO);
            if (isNotNull(schoolApply.getCreateTime()))
                jsonObject.put("applyTime", ISO_DATETIME_FORMAT.format(schoolApply.getCreateTime()));
            else jsonObject.put("applyTime", null);
            if (schoolApply.getState() == ModuleKey.ApplyState.APPROVAL) {
                jsonObject.put("auditStatus", new JSONObject(new HashedMap()) {{
                    put("text", "待审核");
                    put("key", "APPROVAL");
                }});
            } else if (schoolApply.getState() == ModuleKey.ApplyState.REJECT) {
                jsonObject.put("auditStatus", new JSONObject(new HashedMap()) {{
                    put("text", "已拒绝");
                    put("key", "REJECT");
                }});
            } else if (schoolApply.getState() == ModuleKey.ApplyState.AGREE) {
                jsonObject.put("auditStatus", new JSONObject(new HashedMap()) {{
                    put("text", "已通过");
                    put("key", "AGREE");
                }});
            }
            return jsonObject;
        }));
    }

    /**
     * 3.1.2 申请详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/schoolApplyDetail", method = GET)
    public Result schoolApplyDetail(Long id) {
        String[] SCHOOLAPPLY_DETAIL_INFO = {"userName", "phoneNumber:mobile", "name:schoolName", "address", "webSiteUrl:webSite",};

        String[] APPLY_MEMBER_INFO = {"id:vipId", "headImage.path:headImg", "mobile", "realName:username", "username:nickName",
                "gender.title:gender", "hpAccount:HPaccount", "entrepreneurLevel.title:isEntrepreneurs", "partnerLevel.title:isPartner"};

        JSONObject body = new JSONObject();
        SchoolApply schoolApply = schoolApplyService.findOne(id);
        ifNullThrow(schoolApply, TIP_NO_SCHOOLAPPLY);
        JSONObject applyDetails = propsFilter(schoolApply, SCHOOLAPPLY_DETAIL_INFO);
        StringBuffer area = new StringBuffer("");
        ifNotNullThen(schoolApply.getProvince(), x -> area.append(x.getName()).append("-"));
        ifNotNullThen(schoolApply.getCity(), x -> area.append(x.getName()).append("-"));
        ifNotNullThen(schoolApply.getArea(), x -> area.append(x.getName()).append("-"));
        if (isNotBlank(area)) area.deleteCharAt(area.length() - 1);
        applyDetails.put("schoolArea", area);
        if (isNotNull(schoolApply.getCreateTime()))
            applyDetails.put("applyTime", ISO_DATETIME_FORMAT.format(schoolApply.getCreateTime()));
        else applyDetails.put("applyTime", null);
        if (!schoolApply.getPhotos().isEmpty()) {
            String[] photos = new String[schoolApply.getPhotos().size()];
            for (int i = 0; i < schoolApply.getPhotos().size(); i++) {
                photos[i] = schoolApply.getPhotos().get(i).getPath();
            }
            applyDetails.put("photos", photos);
        } else applyDetails.put("photos", null);
        body.put("applyDetails", applyDetails);

        Member member = schoolApply.getMember();
        JSONObject userInfo = propsFilter(member, APPLY_MEMBER_INFO);
        ifNotNullThen(member.getUsername(), x -> userInfo.replace("nickName", decodePathVariable(x)));
        if (isNotNull(member.getBirthday())) userInfo.put("birthday", ISO_DATE_FORMAT.format(member.getBirthday()));
        else userInfo.put("birthDay", null);
        if (isNotNull(member.getCreateTime()))
            userInfo.put("regDate", ISO_DATETIME_FORMAT.format(member.getCreateTime()));
        else userInfo.put("regDate", null);
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(member.getToken());
        if (isNotNull(memberExtInfo)) {
            if (isNotNull(memberExtInfo.getRecommendUser())) {
                if (isNotNull(memberExtInfo.getRecommendUser().getRealName())) {
                    userInfo.put("referrer", memberExtInfo.getRecommendUser().getRealName());
                } else if (isNotNull(memberExtInfo.getRecommendUser().getMobile())) {
                    userInfo.put("referrer", memberExtInfo.getRecommendUser().getMobile());
                } else userInfo.put("referrer", null);
            } else userInfo.put("referrer", null);
        } else userInfo.put("referrer", null);
        body.put("userInfo", userInfo);

        JSONObject auditStatus = new JSONObject();
        auditStatus.put("auditStatus", null);
        enumToJson(schoolApply.getState(), auditStatus, "auditStatus");
        auditStatus.put("reason", schoolApply.getRemark());
        body.put("auditStatus", auditStatus);
        return OK(body);
    }

    /**
     * 3.1.3 审核
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/reviewApply", method = POST)
    public Result reviewApply(Long id, ModuleKey.ApplyState auditStatus, String reason) {
        schoolApplyService.auditSchoolApply(id, auditStatus, reason);
        return OK();
    }

    /**
     * 3.2.1 课程属性列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/courseTagList", method = GET)
    public Result courseTagList() {
        String[] TAG_LIST_INFO = {"id", "name:text"};
        List<CmsTag> tagList = cmsTagService.findCourseTagList();
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> list = simpleMap(tagList, cmsTag -> propsFilter(cmsTag, TAG_LIST_INFO));
        jsonObject.put("list", list);
        return OK(jsonObject);
    }

    /**
     * 3.2.2 新增/编辑
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/editTag", method = POST)
    public Result editTag(Long id, String text) {
        cmsTagService.editTag(id, text);
        return OK();
    }

    /**
     * 3.2.3 删除
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/deleteTags", method = DELETE)
    public Result deleteTag(String id) {
        Long[] ids = stringToLong(id);
        cmsTagService.deleteTags(ids);
        return OK();
    }

    /**
     * 3.3.1 学校相册列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/schoolPhotos", method = GET)
    public Result schoolPhotos(BackPageVo pageVO, String text, PhotoType imgType, String userToken) {
        String[] SCHOOL_PHOTO_LIST_INFO = {"id", "school.name", "type.title", "photo.path", "number", "isEnableBoolean"};
        Member member = memberService.findByToken(userToken);
        Page<SchoolPhoto> page = schoolPhotoService.findSchoolPhotoPage(pageVO.pageRequest(), decodePathVariable(text), imgType, member);
        return OK(page.map(schoolPhoto -> {
            JSONObject jo = propsFilter(schoolPhoto, SCHOOL_PHOTO_LIST_INFO);
            jo.put("userType", member.getRole());
            if (isNotNull(schoolPhoto.getCreateTime()))
                jo.put("publishTime", ISO_DATETIME_FORMAT.format(schoolPhoto.getCreateTime()));
            else jo.put("publishTime", null);
            return jo;
        }));
    }

    /**
     * 3.3.2 删除学校相册照片
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/photosDelete", method = DELETE)
    public Result photosDelete(String id) {
        Long[] ids = stringToLong(id);
        schoolPhotoService.deleteSchoolPhotos(ids);
        return OK();
    }

    /**
     * 3.3.3 启用
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/photoEnable", method = POST)
    public Result photoEnable(Long id, String userToken) {
        Member member = memberService.findByToken(userToken);
        schoolPhotoService.saveSchoolPhoto(id, member);
        return OK();
    }

    /**
     * 3.3.4 新增/编辑
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/editSchoolPhoto", method = POST)
    public Result editSchoolPhoto(String userToken,Long id, Long schoolId, PhotoType imgType,
                                  String imgUrl, String imgDescription, Integer sortNo, Date publishTime) {
        schoolPhotoService.editSchoolPhoto(userToken,id, schoolId, imgType, imgUrl, imgDescription, sortNo, publishTime);
        return OK();
    }

    /**
     * 3.3.5 详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/photoDetail", method = GET)
    public Result photoDetail(Long id, String userToken) {
        String[] SCHOOL_PHOTO_DETAIL_INFO = {"school.id:schoolId", "type:imgType", "photo.path:imgUrl", "text:imgDescription",
                "number:sortNo",};
        Member member = memberService.findByToken(userToken);
        SchoolPhoto schoolPhoto = schoolPhotoService.findOne(id);
        ifNullThrow(schoolPhoto, TIP_NO_SCHOOL_PHOTO);
        JSONObject jsonObject = propsFilter(schoolPhoto, SCHOOL_PHOTO_DETAIL_INFO);
        jsonObject.put("userType", member.getRole());
        if (isNotNull(schoolPhoto.getCreateTime()))
            jsonObject.put("publishTime", ISO_DATETIME_FORMAT.format(schoolPhoto.getCreateTime()));
        else jsonObject.put("publishTime", null);
        return OK(jsonObject);
    }

    /**
     * 3.3.6 学校列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/22
     */
    @RequestMapping(value = "/schoolList", method = GET)
    public Result schoolList() {
        String[] SCHOOL_LIST_INFO = {"id", "name"};
        JSONObject jsonObject = new JSONObject();
        List<School> schools = schoolService.findSchoolList();
        List<JSONObject> list = simpleMap(schools, school -> propsFilter(school, SCHOOL_LIST_INFO));
        jsonObject.put("list", list);
        return OK(jsonObject);
    }

    /**
     * 3.4.1 课程报名列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/courseApplyList", method = GET)
    public Result courseApplyList(BackPageVo pageVO, String text, CourseApplyState orderStatus, String userToken) {
        String[] COURSE_APPLY_LIST_INFO = {"id", "serialNo", "member.mobile", "member.realName", "course.name", "state", "course.price",
                "discount",};
        Member member = memberService.findByToken(userToken);
        Page<CourseApply> page = courseApplyService.findAllCourseApplyPage(pageVO.pageRequest(), decodePathVariable(text), orderStatus, member);
        return OK(page.map(courseApply -> {
            JSONObject jo = propsFilter(courseApply, COURSE_APPLY_LIST_INFO);
            jo.put("userType", member.getRole());
            jo.put("amountPayable", courseApply.getPrice().subtract(courseApply.getDiscount()));
            if (isNotNull(courseApply.getDate())) jo.put("signUpTime", ISO_DATE_FORMAT.format(courseApply.getDate()));
            else jo.put("signUpTime", null);
            if (isNotNull(courseApply.getTrainDate()))
                jo.put("trainTime", ISO_DATE_FORMAT.format(courseApply.getTrainDate()));
            else jo.put("trainTime", null);
            enumToJson(courseApply.getState(), jo, "state");
            return jo;
        }));
    }

//    /**
//     * 3.4.2 删除
//     *
//     * @Author xiao xue wei
//     * @Date 2017/2/16
//     */
//    @RequestMapping(value = "/courseApplyDelete", method = DELETE)
//    public Result courseApplyDelete(String id) {
//        Long[] ids = stringToLong(id);
//        courseApplyService.deleteCourseApply(ids);
//        return OK();
//    }

    /**
     * 3.4.3 上传证书(上传证书后保存一个培训经历在个人简历中)
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/uploadCertificate", method = POST)
    public Result uploadCertificate(Long id, String imgs) {
        courseApplyService.uploadCertificate(id, imgs);
        return OK();
    }

    /**
     * 3.4.4 证书列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/certificateList", method = GET)
    public Result certificateList(Long id) {
        List<CloudFile> list = courseApplyService.findCertificateList(id);
        List<String> imgPaths = new ArrayList<>();
        if (!list.isEmpty()) {
            list.forEach(cloudFile -> imgPaths.add(cloudFile.getPath()));
        }
        JSONObject body = new JSONObject();
        body.put("imgs", list.toArray());
        return OK(body);
    }

    /**
     * 3.4.5 报名详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/courseApplyDetail", method = GET)
    public Result courseApplyDetail(Long id) {
        String[] COURSE_DETAIL_INFO = {"school.name:schoolName", "name:courseName", "isPlatformCourse:isPartnerCourse",
                "platformPercent:platFormShareRate", "redPacketPercent:redPacketUseRate", "price:amount", "day:days"};
        String[] COURSEAPPLY_DETAIL_INFO = {"member.mobile:mobile", "member.realName:username", "member.hpAccount:hpAccount",
                "serialNo:orderNo", "course.price:totalAmount", "discont", "orderStatus",};
        String[] COURSE_REFUND_APPLY_REASON = {"reason.title:reason", "remark"};
        JSONObject body = new JSONObject();
        CourseApply courseApply = courseApplyService.findOne(id);
        ifNullThrow(courseApply, TIP_NO_COURSEAPPLY);
        JSONObject courseDetails = propsFilter(courseApply.getCourse(), COURSE_DETAIL_INFO);
        enumToJson(courseApply.getCourse().getIsPlatformCourse(), courseDetails, "isPartnerCourse");
        body.put("courseDetails", courseDetails);

        JSONObject signUpInfo = propsFilter(courseApply, COURSEAPPLY_DETAIL_INFO);
        if (isNotNull(courseApply.getDate()))
            signUpInfo.put("signUpTime", ISO_DATE_FORMAT.format(courseApply.getDate()));
        else signUpInfo.put("signUpTime", null);
        if (isNotNull(courseApply.getTrainDate()))
            signUpInfo.put("trainTime", ISO_DATE_FORMAT.format(courseApply.getTrainDate()));
        else signUpInfo.put("trainTime", null);
        if (!courseApply.getRedPacketReceives().isEmpty()) {
            signUpInfo.put("redPacketUseNum", courseApply.getRedPacketReceives().size());
            List<BigDecimal> list = new ArrayList<>();
            courseApply.getRedPacketReceives().forEach(redPacketReceive -> list.add(redPacketReceive.getRedPacket().getAmount()));
            signUpInfo.put("redPacketAmount", list.toArray());
        } else {
            signUpInfo.put("redPacketUseNum", 0);
            signUpInfo.put("redPacketAmount", null);
        }
        signUpInfo.put("amountPayable", courseApply.getPrice().subtract(courseApply.getDiscount()));
        enumToJson(courseApply.getState(), signUpInfo, "orderStatus");
        if (courseApply.getIsUseFee() == YES) {
            signUpInfo.put("couponWay", new JSONObject(new HashedMap()) {{
                put("text", "免费培训");
                put("key", "USEFEE");
            }});
        } else if (courseApply.getDiscount() != null) {
            signUpInfo.put("couponWay", new JSONObject(new HashedMap()) {{
                put("text", "红包");
                put("key", "REDPACKET");
            }});
        } else signUpInfo.put("couponWay", null);
        signUpInfo.put("schoolIncome", courseApply.getCourse().getPrice()
                .multiply(new BigDecimal((1 - (courseApply.getCourse().getPlatformPercent() / 100D)) + "")));
        signUpInfo.put("platFormIncome", courseApply.getCourse().getPrice()
                .multiply(new BigDecimal(courseApply.getCourse().getPlatformPercent() / 100 + "")).subtract(courseApply.getDiscount()));
        body.put("signUpInfo", signUpInfo);

        if (courseApply.getState() == COURSE_ORDER_REFUNDING || courseApply.getState() == COURSE_ORDER_REFUND_FAIL) {
            CourseRefundApply courseRefundApply = courseRefundApplyService.findCourseRefundApply(courseApply);
            JSONObject rebackReason = propsFilter(courseRefundApply, COURSE_REFUND_APPLY_REASON);
            body.put("rebackReason", rebackReason);
            if (courseApply.getState() == COURSE_ORDER_REFUND_FAIL) {
                JSONObject rebackFailureReason = propsFilter(courseRefundApply, "refundRemark:reason");
                body.put("rebackFailureReason", rebackFailureReason);
            }
        }

        JSONObject orderRemark = new JSONObject();
        orderRemark.put("remark", courseApply.getRemark());
        body.put("orderRemark", orderRemark);
        return OK(body);
    }

    /**
     * 3.4.6 订单备注
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/courseOrderRemark", method = POST)
    public Result courseOrderRemark(Long id, String remark) {
        courseApplyService.courseOrderRemark(id, remark);
        return OK();
    }

    /**
     * 3.5.1 课程评价列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/17
     */
    @RequestMapping(value = "/courseEvaluateList", method = GET)
    public Result courseEvaluateList(BackPageVo pageVo, String text, String userToken) {
        String[] COURSE_EVALUATE_LIST_INFO = {"id", "member.mobile", "member.realName", "member.hpAccount", "course.name", "course.school.name",
                "isEnableBoolean"};
        Member member = memberService.findByToken(userToken);
        Page<CourseEvaluation> page = courseEvaluationService.findCourseEvaluationPage(pageVo.pageRequest(), text, member);
        return OK(page.map(courseEvaluation -> {
            JSONObject jo = propsFilter(courseEvaluation, COURSE_EVALUATE_LIST_INFO);
            jo.put("userType", member.getRole());
            jo.put("publishTime", ISO_DATETIME_FORMAT.format(courseEvaluation.getCreateTime()));
            return jo;
        }));
    }

    /**
     * 3.5.2 删除
     *
     * @Author xiao xue wei
     * @Date 2017/2/17
     */
    @RequestMapping(value = "/evaluateDelete", method = DELETE)
    public Result evaluateDelete(String id) {
        Long[] ids = stringToLong(id);
        courseEvaluationService.deleteEvaluations(ids);
        return OK();
    }

    /**
     * 3.5.3 启用
     *
     * @Author xiao xue wei
     * @Date 2017/2/17
     */
    @RequestMapping(value = "/evaluateEnable", method = POST)
    public Result evaluateEnable(Long id, String userToken) {
        Member member = memberService.findByToken(userToken);
        courseEvaluationService.enableEvaluation(id, member);
        return OK();
    }

    /**
     * 3.5.4 详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/17
     */
    @RequestMapping(value = "/evaluateDetail", method = GET)
    public Result evaluateDetail(Long id) {
        String[] COURSE_EVALUATION_DETAIL_INFO = {"member.mobile:mobile", "member.realName:username", "member.hpAccount:hpAccount",
                "course.school.name:schoolName", "course.name:courseName", "score", "content",};
        String[] COURSE_EVALUATION_MEMBER_INFO = {"headImage.path:headImg", "id:vipId", "mobile", "realName:username", "username:nickName",
                "gender.title:gender", "hpAccount:HPaccount", "entrepreneurLevel.title:isEntrepreneurs", "partnerLevel.title:isPartner"};
        CourseEvaluation courseEvaluation = courseEvaluationService.findOne(id);
        ifNullThrow(courseEvaluation, TIP_NO_COURSE_EVALUATION);
        JSONObject body = new JSONObject();
        JSONObject commentDetails = propsFilter(courseEvaluation, COURSE_EVALUATION_DETAIL_INFO);
        if (!courseEvaluation.getPhotos().isEmpty()) {
            List<String> list = new ArrayList<>();
            courseEvaluation.getPhotos().forEach(cloudFile -> list.add(cloudFile.getPath()));
            commentDetails.put("imgs", list.toArray());
        } else commentDetails.put("imgs", null);
        if (isNotNull(courseEvaluation.getCreateTime()))
            commentDetails.put("publishTime", ISO_DATETIME_FORMAT.format(courseEvaluation.getCreateTime()));
        else commentDetails.put("publishTime", null);
        body.put("commentDetails", commentDetails);

        Member member = courseEvaluation.getMember();
        JSONObject userInfo = propsFilter(member, COURSE_EVALUATION_MEMBER_INFO);
        ifNotBlankThen(member.getUsername(), name -> userInfo.replace("nickName", decodePathVariable(name)));
        if (isNotNull(member.getBirthday())) userInfo.put("birthday", ISO_DATE_FORMAT.format(member.getBirthday()));
        else userInfo.put("birthDay", null);
        if (isNotNull(member.getCreateTime()))
            userInfo.put("regDate", ISO_DATETIME_FORMAT.format(member.getCreateTime()));
        else userInfo.put("regDate", null);
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(member.getToken());
        if (isNotNull(memberExtInfo)) {
            if (isNotNull(memberExtInfo.getRecommendUser())) {
                if (isNotNull(memberExtInfo.getRecommendUser().getRealName())) {
                    userInfo.put("referrer", memberExtInfo.getRecommendUser().getRealName());
                } else if (isNotNull(memberExtInfo.getRecommendUser().getMobile())) {
                    userInfo.put("referrer", memberExtInfo.getRecommendUser().getMobile());
                } else userInfo.put("referrer", null);
            } else userInfo.put("referrer", null);
        } else userInfo.put("referrer", null);
        body.put("userInfo", userInfo);
        return OK(body);
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
     * 3.6.1 学校提现账户列表
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/withdrawalList", method = GET)
    public Result findSchoolWithdrawalList(String userToken, BackPageVo backPageVo, String text) {
        String[] json = {"id", "school.name", "type.title", "member.realName", "date"};
        Member member = memberService.findByToken(userToken);
        Page<WithdrawAccount> page = withdrawAccountService.findSchoolWithDrawalList(member, backPageVo, decodePathVariable(text));
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, json);
            jo.put("userType", member.getRole());
            jo.replace("date", ISO_DATETIME_FORMAT.format(x.getDate()));
            return jo;
        }));
    }

    /**
     * 3.6.2 新增/编辑提现账户
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/withdrawal", method = POST)
    public Result addOrEditWithdrawl(String userToken, WithdrawAccount withdrawAccount, String idCardImg, String halfImg) {
        Member member = memberService.findByToken(userToken);
        withdrawAccountService.addOrEditWithdrawal(member, withdrawAccount, idCardImg, halfImg);
        return OK("success");
    }

    /**
     * 3.6.3 账户详情
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/withdrawal", method = GET)
    public Result withdrawalInfo(Long id) {
        String[] json = {"type:withDrawWay", "account", "idCardNo:IDCardNo", "bank", "branchBank:branchOfBank",
                "IDCardFrontImg", "IDCardHalfImg"};
        WithdrawAccount withdrawAccount = withdrawAccountService.findOne(id);
        JSONObject body = propsFilter(withdrawAccount, json);
        ifNotNullThen(withdrawAccount.getType(), x -> enumToJson(x, body, "withDrawWay"));
        ifNotNullThen(withdrawAccount.getIdCard(), x -> body.replace("IDCardFrontImg", x.getPath()));
        ifNotNullThen(withdrawAccount.getHalf(), x -> body.replace("IDCardHalfImg", x.getPath()));
        return OK(body);
    }

    /**
     * 3.6.4 删除
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/withdrawal", method = DELETE)
    public Result deleteWithdrawal(Long id, String userToken) {
        Member member = memberService.findByToken(userToken);
        ifFalseThrow(member.getRole() == SCHOOL, TIP_NO_AUTHORITY);
        WithdrawAccount one = withdrawAccountService.findOne(id);
        one.setIsDelete(YES);
        withdrawAccountService.save(one);
        return OK("success");
    }

    /**
     * 3.6.5 可提现账户列表
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/withdrawalAccount", method = GET)
    public Result withdrawalAccount(String userToken) {
        String[] json = {"id", "accountType", "account", "member.realName:username", "bank:bankName"};
        List<WithdrawAccount> withdrawAccountList = withdrawAccountService.findByMemberToken(userToken);
        List<JSONObject> body = simpleMap(withdrawAccountList, x -> {
            JSONObject jo = propsFilter(x, json);
            ifNotNullThen(x.getType(), e -> enumToJson(e, jo, "accountType"));
            return jo;
        });
        return OK(body);
    }

    /**
     * 3.6.6 提现
     *
     * @Author mu.jie
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/withdrawalPay", method = POST)
    public Result withdrawalPay(String userToken, Long withdrawId, String authCode, BigDecimal amount, String password) {
        Member member = memberService.findByToken(userToken);
        schoolService.withdrawlPay(member, withdrawId, authCode, amount, password);
        return OK("success");
    }

}
