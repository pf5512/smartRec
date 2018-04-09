package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.portal.controller.dto.ComparatorWork;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.JobApplyRecordService;
import com.thousandsunny.service.service.JobService;
import com.thousandsunny.service.service.ResumeLookService;
import com.thousandsunny.service.service.ResumeService;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.thousandsunny.common.DistanceUtil.compareToVersion;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.JsonUtil.valueIsNull;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.FindJobState.WORKING_NOT_CONSIDER;
import static com.thousandsunny.service.ModuleKey.RecState;
import static com.thousandsunny.service.ModuleKey.RecState.NOT_WORK;
import static com.thousandsunny.service.ModuleTips.TIP_PLACE_FALSE;
import static java.util.Objects.deepEquals;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.ListUtils.union;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/portal/resumes", produces = APPLICATION_JSON_UTF8_VALUE)
public class ResumeController {
    public static final String[] INTENSION_MODEL = {
            "salary:expectSalary",
            "findJobState",
            "workYear",
            "province.id:workPlaceProvinceId",
            "province.name:workPlaceProvinceName",
            "city.id:workPlaceCityId",
            "city.name:workPlaceCityName",
            "area.id:workPlaceAreaId",
            "area.name:workPlaceAreaName"
    };
    private static final String[] RESUMELOOK_INFO = {
            "id",
            "shop.logo.path:storeLogo",
            "shop.name:storeName",
            "shop.id:storeId",
            "date",
            "isRead.bool:isRead"
    };
    private static final String[] JOB_MODEL = {
            "id", "name"
    };

    public static final String[] PICTURE_INFO = {
            "path",
            "isPlatformAddBoolean:isPlatformAdd"
    };


    public static final String[] CERTIFICATE_INFO = {
            "id",
            "path:imageUrl",
            "isPlatformAdd"
    };

    public static final String[] JOB_WORK_INFO = {
            "id",
            "job.name:jobName",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "shop.name:storeName",
            "recState:state",
            "startDate:startWorkDate",
            "resignDate:endWorkDate",
            "shop.id:storeId",
            "job.state:jobState"
    };

    public static final String[] WORKEXP_INFO = {
            "id",
            "readableStartDate:startDate",
            "readableEndDate:endDate",
            "shopName:storeName",
            "positionName:position",
            "description"

    };

    public static final String[] TRAIN_INFO = {
            "id",
            "readableStartDate:startDate",
            "readableEndDate:endDate",
            "institutionName",
            "courseName:course"
    };
    private static final String[] WORK_EXP_JSON = {"id", "shopName:storeName", "positionName:position", "readableStartDate:startDate", "readableEndDate:endDate", "description"};
    private static final String[] TRAIN_EXP_JSON = {"id", "readableStartDate:startDate", "readableEndDate:endDate", "institutionName", "courseName:course"};


    private static String[] RESUME_INFO = {
            "id",
            "realName",
            "token:userToken",
            "position",
            "salary",
            "workYear",
            "area.name:areaName",
            "headImage.path:headhearderImageUrl",
            "distance"
    };

    private String[] RESUME_LIST_JSON = {
            "member.realName:realName",
            "member.token:userToken",
            "member.headImage.path:hearderImageUrl",
            "intention.workYear:workYear",
            "intention.area.name:areaName",

    };

    @Autowired
    private ResumeLookService resumeLookService;
    @Autowired
    private ResumeService resumeService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private BaseMemberService memberService;


    /**
     * 简历列表
     */
    @RequestMapping(method = GET)
    public ResponseEntity resumeList(String nameKeyword, Long jobType, Long salary, Long period, Long provinceId, Long cityId, Long areaId, PageVO pageVO) {
        checkPlaceFormat(provinceId, cityId, areaId);
        Page<Resume> page = resumeService.allResume(nameKeyword, jobType, salary, period, provinceId, cityId, areaId, pageVO.pageRequest());
        sortResume(page.getContent());
        JSONObject jsonObject = pageToJson(page, x -> {
            JSONObject jo = propsFilter(x, RESUME_LIST_JSON);
            if (isNotNull(x.getIntention()))
                jo.put("date", getBigDate(x.getIntention().getDate(), x.getFresh()));
            else jo.put("date", x.getFresh());
            StringBuffer position = getPosition(x);
            jo.put("position", position);
            parseResumeAreaName(x, jo);
            ifNotNullThen(x.getIntention(), y -> jo.put("salary", y.getSalary() + "/月"));
            return jo;
        });
        return ok(jsonObject);
    }

    private void parseResumeAreaName(Resume x, JSONObject jo) {
        String areaName = "全国";
        if (isNotNull(x.getIntention()) && isNotNull(x.getIntention().getArea())) {
            areaName = x.getIntention().getArea().getName();
        } else if (isNotNull(x.getIntention()) && isNotNull(x.getIntention().getCity())) {
            areaName = x.getIntention().getCity().getName();
        } else if (isNotNull(x.getIntention()) && isNotNull(x.getIntention().getProvince())) {
            areaName = x.getIntention().getProvince().getName();
        }
        jo.replace("areaName", areaName);
    }

    /**
     * 获取最新时间
     */
    public Date getBigDate(Date date1, Date date2) {
        if (isNotNull(date1) && isNotNull(date2)) {
            if (date1.getTime() > date2.getTime())
                return date1;
            else return date2;
        } else if ((!isNotNull(date1)) && isNotNull(date2)) {
            return date2;
        } else if ((!isNotNull(date2)) && isNotNull(date1)) {
            return date1;
        } else return null;
    }

    public void checkPlaceFormat(Long provinceId, Long cityId, Long areaId) {
        if (!isNotNull(provinceId)) {
            ifTrueThrow(isNotNull(cityId) || isNotNull(areaId), TIP_PLACE_FALSE);
        }
        ifTrueThrow(isNotNull(provinceId) && (!isNotNull(cityId)) && isNotNull(areaId), TIP_PLACE_FALSE);
    }

    /**
     * 获取岗位信息
     */
    public StringBuffer getPosition(Resume resume) {
        StringBuffer position = new StringBuffer();
        ifNotNullThen(resume.getIntention(), x -> ifNotNullThen(x.getJobTypes(), f -> {
            f.forEach(g -> position.append(g.getName()).append(","));
            if (position.length() > 0)
                position.deleteCharAt(position.length() - 1);
        }));
        return position;
    }

    /**
     * 简历列表-附近
     */
    @RequestMapping(value = "/nearby", method = GET)
    public ResponseEntity nearResume(Long jobType, Long salary, Long period, Double lng, Double lat, PageVO pageVO) {
        List<Resume> resumes = resumeService.nearResume(jobType, salary, period, lng, lat, pageVO.pageRequest());
        sortResume(resumes);
        List<JSONObject> list = simpleMap(resumes, e -> {
            JSONObject jo = propsFilter(e, RESUME_LIST_JSON);
            if (isNotNull(e.getIntention()))
                jo.put("date", getBigDate(e.getIntention().getDate(), e.getFresh()));
            else jo.put("date", e.getFresh());
            StringBuffer position = getPosition(e);
            jo.put("position", position);
            jo.put("salary", e.getIntention().getSalary() + "/月");
            parseResumeAreaName(e, jo);
            if (isNotNull(e.getLatitude()) && isNotNull(e.getLongitude())) {
                jo.put("distance", resumeService.GetDistance(lat, lng, e.getLatitude(), e.getLongitude()));
            } else {
                jo.put("distance", null);
            }
            jo.put("type", e.getIntention().getFindJobState());
            return jo;
        });
        JSONObject body = listToJson(list);
        body.put("last", !deepEquals(list.size(), pageVO.getPageSize()));
        return ok(body);
    }

    /**
     * 14.1  14.2  简历排序
     *
     * @Author xiao xue wei
     * @Date 2017/1/3
     */
    public void sortResume(List<Resume> resumes) {
        simpleSort(resumes, (x, y) -> {
            if (x.getIntention().getFindJobState() == WORKING_NOT_CONSIDER) return -1;
            else return 1;
        });
    }

    /**
     * 用户简历信息
     */
    @RequestMapping(value = "/info", method = GET)
    public ResponseEntity resumeInfo(String userToken, String checkedUserToken) {
        Resume resume = resumeService.reviewResume(userToken, checkedUserToken);
        JSONObject jsonObject = new JSONObject();
        if (isNotNull(resume)) {
            jsonObject.put("highlights", resume.getHighlights());
            jsonObject.put("jobIntension", null);
            ifNotNullThen(resume.getIntention(), e -> {
                JSONObject jobIntension = propsFilter(e, INTENSION_MODEL);
                List<Object> objects = simpleMap(e.getJobTypes(), jobType -> propsFilter(jobType, JOB_MODEL));
                if (objects.size() > 0) {
                    jobIntension.put("positionList", objects);
                } else {
                    jobIntension.put("positionList", null);
                }
                if (jobIntension.size() > 0) {
                    jsonObject.put("jobIntension", jobIntension);
                } else {
                    jsonObject.put("jobIntension", null);
                }

            });
            List<ResumeWorkExp> workExps = resume.getWorkExps();
            ComparatorWork comparatorWork = new ComparatorWork();
            Collections.sort(workExps, comparatorWork);
            List<JSONObject> workExperienceList = simpleMap(workExps, e -> propsFilter(e, WORKEXP_INFO));
            jsonObject.put("workExperienceList", workExperienceList);
            List<JSONObject> trainExperienceList = simpleSortMap(resume.getTrainExps(), (x, y) -> {
                        if (x.getStartDate() == null) return -1;
                        if (y.getStartDate() == null) return 1;
                        return y.getStartDate().compareTo(x.getStartDate());
                    }, e -> {
                        JSONObject jo = propsFilter(e, TRAIN_INFO);
                        List<JSONObject> certificateList = simpleMap(e.getCertification(), k -> propsFilter(k, PICTURE_INFO));
                        jo.put("certificateList", certificateList);
                        jo.put("isPlatformAdd",e.getIsPlatformAddBoolean());
                        return jo;
                    }
            );
            jsonObject.put("trainExperienceList", trainExperienceList);

        } else {
            valueIsNull(jsonObject, null, "highlights", "jobIntension", "workExperienceList", "trainExperienceList");
        }

        return ok(jsonObject);

    }


    /**
     * 编辑个人亮点
     */
    @RequestMapping(value = "/editResume", method = POST)
    public ResponseEntity editResume(String userToken, String highlights) {
        resumeService.editResume(userToken, highlights);
        return OK;
    }


    /**
     * 编辑求职意向
     */
    @RequestMapping(value = "/resumeIntention", method = PUT)
    public ResponseEntity editResumeIntention(String userToken, ResumeIntention intention) {
        resumeService.editResumeIntention(userToken, intention);
        return OK;
    }


    /**
     * 新增工作经验
     */

    @RequestMapping(value = "/resumeWorkExp", method = POST)
    public ResponseEntity editResumeWorkExp(String userToken, ResumeWorkExp work) {
        ResumeWorkExp exp = resumeService.editResumeWorkExp(userToken, work);
        return ok(propsFilter(exp, WORK_EXP_JSON));
    }


    /**
     * 删除工作经验
     */

    @RequestMapping(value = "/delResumeWorkExp", method = DELETE)
    public ResponseEntity delResumeWorkExp(String userToken, Long id) {
        resumeService.delResumeWorkExp(userToken, id);
        return OK;
    }


    /**
     * 新增培训经历
     */

    @RequestMapping(value = "/resumeTrainExp", method = POST)
    public ResponseEntity addResumeTrainExp(String userToken, ResumeTrainExp trainExp) {
        trainExp = resumeService.addResumeTrainExp(userToken, trainExp);
        JSONObject object = propsFilter(trainExp, TRAIN_EXP_JSON);

        List<JSONObject> certificateList = simpleMap(trainExp.getCertification(), k -> propsFilter(k, PICTURE_INFO));
        object.put("certificateList", certificateList);
        return ok(object);
    }


    /**
     * 删除培训经历
     */

    @RequestMapping(value = "/delResumeTrainExp", method = DELETE)
    public ResponseEntity delResumeTrainExp(String userToken, Long id) {
        resumeService.delResumeTrainExp(userToken, id);
        return OK;
    }


    /**
     * 刷新简历
     */

    @RequestMapping(value = "/fresh", method = PUT)
    public ResponseEntity freshResume(String userToken) {
        resumeService.freshResume(userToken);
        return OK;
    }


    /**
     * 简历收藏
     */

    @RequestMapping(value = "/collectResume", method = POST)
    public ResponseEntity collectResume(String userToken, String collectedUserToken, OperatorType operatorType) {
        resumeService.collectResume(userToken, collectedUserToken, operatorType);
        return OK;
    }


    /**
     * 我推荐别人的岗位列表
     */
    @RequestMapping(value = "/recTrace", method = GET)
    public ResponseEntity recTrace(String userToken, PageVO pageVO) {
        Page<JobApplyRecord> jobRecRecords = jobApplyRecordService.recTrace(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(jobRecRecords, e -> {
            JSONObject jo = propsFilter(e, JOB_WORK_INFO);
            String realName = e.getReceiver().getRealName();
            if (isNotNull(realName)) jo.put("recommendedRealName", realName);
            else jo.put("recommendedRealName", e.getReceiver().getMobile());
            jo.put("recommendedUserToken", e.getReceiver().getToken());
            return jo;
        });
        return ok(jsonObject);
    }


    /**
     * 别人推荐我的岗位列表
     */
    @RequestMapping(value = "/recedTrace", method = GET)
    public ResponseEntity recedTrace(String userToken, PageVO pageVO) {
        Page<JobApplyRecord> jobRecRecords = jobApplyRecordService.recedTrace(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(jobRecRecords, this::parseRecedTrace);
        return ok(resort(jsonObject));


    }

    private JSONObject parseRecedTrace(JobApplyRecord e) {
        JSONObject jo = propsFilter(e, JOB_WORK_INFO);
        String realName = e.getReferral().getRealName();
        if (isNotNull(realName)) jo.put("recommendRealName", realName);
        else jo.put("recommendRealName", e.getReferral().getMobile());
        jo.put("state", e.getRecState());
        return jo;
    }

    /**
     * 14.24别人推荐我的岗位列表-详情
     *
     * @Author mu.jie
     * @Date 2016/12/22
     */
    @RequestMapping(value = "/recedTraceInfo", method = GET)
    public ResponseEntity receTraceInfo(String userToken, Long recommendId) {
        JobApplyRecord jobApplyRecord = jobApplyRecordService.jobApplyRecord(recommendId);
        return ok(parseRecedTrace(jobApplyRecord));
    }

    /**
     * 重新排序
     */
    private JSONObject resort(JSONObject jsonObject) {
        List<JSONObject> objects = (List<JSONObject>) jsonObject.get("list");
        List<JSONObject> exceptNotWork = simpleFilter(objects, j -> j.getObject("state", RecState.class) != NOT_WORK);
        List<JSONObject> notWork = simpleFilter(objects, j -> j.getObject("state", RecState.class) == NOT_WORK);
        jsonObject.put("list", union(exceptNotWork, notWork));
        return jsonObject;
    }


    /**
     * 接受岗位推荐
     */
    @RequestMapping(value = "/jobRecRecord", method = POST)
    public ResponseEntity addJobRecRecord(String userToken, String recommendUserToken, Long jobId) {
        jobService.acceptJobRecRecord(userToken, recommendUserToken, jobId);
        return OK;
    }

    /**
     * 查询是否可以绑定岗位推荐关系
     */
    @RequestMapping(value = "/jobRecRecord/state", method = GET)
    public ResponseEntity jobRecRecordState(String userToken, String recommendUserToken, Long jobId, String version) {
        if (isBlank(version) || (isNotBlank(version) && compareToVersion("1.0.3", version) > 0)) {//app版本1.0.3以下返回
            Member member = jobApplyRecordService.jobRecRecordState(userToken, recommendUserToken, jobId);
            Boolean isCanBind = isNull(member);
            if (isCanBind) {
                member = memberService.findByToken(recommendUserToken);
            }
            return ok(of("isCanBind", isCanBind, "recommendUserRealName", isBlank(member.getRealName()) ? member.getMobile() : member.getRealName()));
        } else {//app版本1.0.3以上返回
            Member receiver = memberService.findByToken(userToken);
            Member referral = jobApplyRecordService.jobRecRecordState(receiver, jobId);
            JSONObject body = new JSONObject();
            boolean flag = isNull(referral);
            if (flag) {
                referral = memberService.findByToken(recommendUserToken);
            }
            body.put("isHasRecord", !flag);
            body.put("recordRecommendUserToken", flag ? null : referral.getToken());
            body.put("recommendUserRealName", isBlank(referral.getRealName()) ? referral.getMobile() : referral.getRealName());
            return ok(body);
        }
    }

    /**
     * 解除岗位推荐
     */
    @RequestMapping(value = "/jobRecRecord", method = DELETE)
    public ResponseEntity delJobRecRecord(String userToken, Long id) {
        jobApplyRecordService.delJobRecRecord(userToken, id);
        return OK;
    }

    /**
     * 岗位推荐员工发起上班(同时生成续费记录)
     */
    @RequestMapping(value = "/requestWorking", method = PUT)
    public ResponseEntity requestWorking(String userToken, Long id, Date date) {
        jobApplyRecordService.requestWorking(userToken, id, date);
        return OK;
    }


    /**
     * 岗位推荐员工确认离职
     */
    @RequestMapping(value = "/leaveWork", method = PUT)
    public ResponseEntity leaveWork(String userToken, Long id) {
        jobApplyRecordService.leaveWork(userToken, id);
        return OK;
    }

    /**
     * 岗位推荐员工收回上班
     */
    @RequestMapping(value = "/callback", method = PUT)
    public ResponseEntity callback(String userToken, Long id) {
        jobApplyRecordService.callback(userToken, id);
        return OK;
    }


    /**
     * 用户培训证书列表
     */
    @RequestMapping(value = "/certifications", method = GET)
    public ResponseEntity certificationList(String userToken) {
        List<ResumeTrainExp> trainExps = resumeService.findResume(userToken).getTrainExps();
        List<CloudFile> certification = new ArrayList<>();
        for (ResumeTrainExp r : trainExps) {
            certification.addAll(r.getCertification());
        }
        List<JSONObject> jsonObjects = simpleMap(certification, e -> propsFilter(e, CERTIFICATE_INFO));
        return ok(listToJson(jsonObjects));
    }

    /**
     * 谁看过我的简历消息总数量
     */

    @RequestMapping(value = "/visitorCount", method = GET)
    public ResponseEntity lookersCount(String userToken) {
        ResumeLook resumeLook = resumeLookService.firstStoreName(userToken);
        JSONObject jsonObject = new JSONObject();
        Long value = resumeLookService.countResumeVisitor(userToken);
        jsonObject.put("count", value);
        jsonObject.put("firstStoreName", null);
        jsonObject.put("date", null);
        ifNotNullThen(resumeLook, s -> {
            jsonObject.put("firstStoreName", s.getShop().getName());
            jsonObject.put("date", s.getDate().getTime());
        });
        return ok(jsonObject);
    }

    /**
     * 谁看过我的简历消息总数量归零
     */
    @RequestMapping(value = "/readResumeLookers", method = PUT)
    public ResponseEntity readResumeLookers(String userToken) {
        resumeLookService.readResumeLookers(userToken);
        return OK;
    }

    /**
     * 谁看过我的简历列表
     */
    @RequestMapping(value = "/resumeLookers", method = GET)
    public ResponseEntity resumeLookers(String userToken, PageVO pageVO) {
        Page<ResumeLook> resumeLookPage = resumeLookService.resumeLookers(userToken, pageVO.pageRequest());
        return ok(pageToJson(resumeLookPage, e -> propsFilter(e, RESUMELOOK_INFO)));
    }


}
