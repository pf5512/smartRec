package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.common.DateUtil;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.DistanceUtil.getDistance;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.NORMAL;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleTips.TIP_NO_AUTHORITY;
import static com.thousandsunny.service.ModuleTips.TIP_PLACE_FALSE;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static java.sql.Date.valueOf;
import static java.util.Objects.isNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by admin on 2016/10/21.
 */
@RestController
@RequestMapping(value = "/api/portal/job", produces = APPLICATION_JSON_UTF8_VALUE)
public class JobController {
    private static final String[] job_list_json = {
            "id",
            "name",
            "shop.name:storeName",
            "salary.name:salary",
            "period.name:workYear",
            "reward:rewardAmount",
            "recType:jobType",
            "epmCount:unFindPeopleCount",
            "shop.owner.token:storeManagerUserToken",
            "shop.logo.path:storeLogoImageUrl"

    };
    private static final String[] job_type_json = {
            "id",
            "name"
    };
    private static final String[] job_salary_json = {
            "id",
            "name"
    };
    private static final String[] job_detail_json = {
            "id",
            "name",
            "description",
            "date",
            "shop.id:storeId",
            "shop.name:storeName",
            "shop.logo.path:storeLogoImageUrl",
            "shop.address:storeAddress",
            "shop.longitude:storeLng",
            "shop.latitude:storeLat",
            "salary.name:salary",
            "period.name:workYear",
            "epmCount:unFindPeopleCount",
            "reward:rewardAmount",
            "recType:jobType",
            "provinceName",
            "cityName",
            "areaName",
            "shop.owner.realName:managerRealName",
            "shop.owner.token:managerUserToken",
            "shop.owner.headImage.path:managerHeaderImageUrl",
            "shop.ownerPosition:managerPosition",
            "shop.owner.mobile:managerPhoneNumber",
            "shop.brightSpots:storeHighlights"
    };
    private static final String[] job_IPublish_json = {
            "id",
            "name",
            "jobType.id:classId",
            "date",
            "shop.address:areaName",
            "salary.name:salary",
            "period.name:workYear",
            "epmCount:unFindPeopleCount",
            "reward:rewardAmount",
            "workerCount:workingPeopleCount",
            "quitterCount:resignedPeopleCount",
            "recType:jobType",
            "state",
            "isAuto",
            "description",
            "salary.id:salaryId",
            "period.id:periodId",
    };
    private static final String[] job_record_wait_confirm_json = {
            "id",
            "job.name:jobName",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "receiver.realName:employeeRealName",
            "receiver.mobile:employeePhoneNumber",
            "startDate:startWorkDate",
            "referral.realName:recommendRealName"
    };
    private static final String[] job_worker_json = {
            "id",
            "job.name:jobName",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "receiver.realName:employeeRealName",
            "receiver.mobile:employeePhoneNumber",
            "startDate:startWorkDate",
            "referral.realName:recommendRealName",
            "recState:state",
            "resignDate",
            "resignRemark",
            "isStartRenew",
            "renewStartDate",
            "renewEndDate",
            "breachAmount",
            "receiver.token:userToken"
    };
    private static final String[] job_refund_info_json = {
            "epmCount:unFindPeopleCount",
            "workerCount:workingPeopleCount",
            "quitterCount:resignedPeopleCount",
            "reward:rewardAmount",
            "recType:jobType",
    };
    @Autowired
    private JobService jobService;
    @Autowired
    private JobBlockedService jobBlockedService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private JobConstantService jobConstantService;
    @Autowired
    private JobTypeService jobTypeService;
    @Autowired
    private JobCollectService jobCollectService;
    @Autowired
    private AutomaticRenewalsService automaticRenewalsService;
    @Autowired
    private MemberService memberService;


    /**
     * 岗位列表
     */
    @RequestMapping(value = "/list", method = GET)
    public ResponseEntity findJobList(String userToken, String nameKeyword, Long jobType, Long salary, Long period, Long provinceId, Long cityId, Long areaId, PageVO pageVO) {
        checkPlaceFormat(provinceId, cityId, areaId);
        Page<Job> jobs = jobService.getJobList(NORMAL, nameKeyword, jobType, salary, period, provinceId, cityId, areaId, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(jobs, e -> {
            JSONObject jo = propsFilter(e, job_list_json);
            if (!isNull(e.getFresh()))
                jo.put("date", e.getFresh());
            else jo.put("date", e.getDate());
            String areaName = parseShopAreaName(e);
            jo.put("areaName", areaName);
            jo.put("isBlockStoreManager", jobBlockedService.isJobBlocked(e.getId(), userToken));
            jo.put("distance", 0);
            return jo;
        });
        return ok(jsonObject);
    }

    public void checkPlaceFormat(Long provinceId, Long cityId, Long areaId) {
        if (provinceId == null) {
            ifTrueThrow(cityId != null || areaId != null, TIP_PLACE_FALSE);
        }
        ifTrueThrow(provinceId != null && cityId == null && areaId != null, TIP_PLACE_FALSE);
    }

    private String parseShopAreaName(Job e) {
        String areaName = "";
        if (e.getShop().getArea() != null) {
            areaName = e.getShop().getArea().getName();
        } else if (e.getShop().getCity() != null) {
            areaName = e.getShop().getCity().getName();
        } else if (e.getShop().getProvince() != null) {
            areaName = e.getShop().getProvince().getName();
        }
        return areaName;
    }

    /**
     * 岗位列表-附近
     */
    @RequestMapping(value = "/nearby", method = GET)
    public ResponseEntity showNearby(String userToken, Long jobType, Long salary, Long period, Double lng, Double lat, PageVO pageVO) {
        List<Job> jobs = jobService.findNearbyJob(jobType, salary, period, lng, lat, pageVO.pageRequest());
        List<JSONObject> jsonObjects = simpleMap(jobs, e -> {
                    JSONObject jsonObject = propsFilter(e, job_list_json);
                    if (!isNull(e.getFresh()))
                        jsonObject.put("date", e.getFresh());
                    else jsonObject.put("date", e.getDate());
                    String areaName = parseShopAreaName(e);
                    jsonObject.put("areaName", areaName);
                    jsonObject.put("isBlockStoreManager", jobBlockedService.isJobBlocked(e.getId(), userToken));
                    jsonObject.put("distance", getDistance(lng, lat, e.getShop().getLongitude(), e.getShop().getLatitude()));
                    return jsonObject;
                }
        );
        JSONObject jo = new JSONObject();
        jo.put("list", jsonObjects);
        jo.put("last", jobs.size() != pageVO.getPageSize());
        jo.put("pageNo", pageVO.getPageNo());
        jo.put("first", pageVO.getPageNo() == 0);
        return

                ok(jo);

    }


    /**
     * 岗位类别列表
     */
    @RequestMapping(value = "/jobClassList", method = GET)
    public ResponseEntity showJobClassList() {
        List<JobType> jobTypes = jobTypeService.getParentJobClassList();
        List<JSONObject> jsonObjects = simpleMap(jobTypes, e -> {
            JSONObject jo = propsFilter(e, job_type_json);
            List<JobType> childJobClasses = jobTypeService.getChildJobClassList(e.getId());
            List<JSONObject> jos = simpleMap(childJobClasses, x -> propsFilter(x, job_type_json));
            jo.put("list", jos);
            return jo;
        });
        return ok(listToJson(jsonObjects));
    }


    /**
     * 岗位薪资待遇列表
     */
    @RequestMapping(value = "/salaryList", method = GET)
    public ResponseEntity showJobSalaryList() {
        List<JobConstant> jobConstants = jobConstantService.getJobSalaryList();
        return ok(listToJson(simpleMap(jobConstants, e -> propsFilter(e, job_salary_json))));
    }

    /**
     * 岗位工作经验列表
     */
    @RequestMapping(value = "/experienceList", method = GET)
    public ResponseEntity showJobExperienceList() {
        List<JobConstant> jobConstants = jobConstantService.getJobExperienceList();
        List<JSONObject> jsonObjects = simpleMap(jobConstants, e -> propsFilter(e, job_salary_json));
        return ok(listToJson(jsonObjects));
    }

    /**
     * 岗位详情
     */
    @RequestMapping(value = "/detail", method = GET)
    public ResponseEntity showJobDetail(Long id, String userToken, String type) {
        Job job = jobService.getNormalJobByType(type, id);
        JobCollect jobCollect = jobCollectService.findByMemberTokenAndJobId(userToken, id);

        JSONObject jsonObject = propsFilter(job, job_detail_json);
//        String areaName = parseShopAreaName(job);
//        jsonObject.replace("areaName", areaName);
        ifNotNullThen(job.getShop().getProvince(), x -> jsonObject.replace("provinceName", x.getName()));
        ifNotNullThen(job.getShop().getCity(), x -> jsonObject.replace("cityName", x.getName()));
        ifNotNullThen(job.getShop().getArea(), x -> jsonObject.replace("areaName", x.getName()));
        if (job.getShop().getIsNoDisturb() == YES) {
            jsonObject.put("isManagerPhoneNumberNoDisturb", true);
        } else {
            jsonObject.put("isManagerPhoneNumberNoDisturb", false);
        }

        if (!isNull(job.getFresh()))
            jsonObject.put("date", job.getFresh());
        else
            jsonObject.put("date", job.getDate());

        jsonObject.put("isBlockStoreManager", jobBlockedService.isJobBlocked(id, userToken));
        jsonObject.put("isLast", jobService.isLast(job.getId(), job.getStoreId()));
        jsonObject.put("distance", 0);
        jsonObject.put("isCollect", isNotNull(jobCollect));
        return ok(jsonObject);
    }

    /**
     * 发布岗位
     */
    @RequestMapping(value = "/publish", method = POST)
    public ResponseEntity publishJob(String userToken, Job job, Long classId, String payPassword, String openId) {
        Charge charge = jobService.publishJob(userToken, job, classId, payPassword, openId);
        return isNull(charge) ? OK : ok(charge);
    }

    /**
     * 我发布的岗位列表
     */
    @RequestMapping(value = "/IPublishes", method = GET)
    public ResponseEntity showPublishedJobList(String userToken) {
        List<Job> jobs = jobService.getPublishedJobList(userToken);
        List<JSONObject> jsonObjects = simpleMap(jobs, e -> {
            JSONObject jo = propsFilter(e, job_IPublish_json);
            if (isNotNull(e.getChangeCount()) && e.getChangeCount() > 0) jo.put("unPayPeopleCount", e.getChangeCount());
            else jo.put("unPayPeopleCount", 0);
            jo.put("resignedAndRefundedPeopleCount", e.getRefundCount());
            jo.put("resignedAndNotRefundPeopleCount", jobService.getResignAndNotRefundedPeopleCount(e));
            return jo;
        });

        return ok(listToJson(jsonObjects));
    }

    /**
     * 岗位刷新
     */
    @RequestMapping(value = "/refresh", method = PUT)
    public ResponseEntity refresh(String userToken, Long id) {
        jobService.refresh(userToken, id);
        return OK;
    }

    /**
     * 岗位编辑
     */
    @RequestMapping(value = "/edit", method = PUT)
    public ResponseEntity edit(String userToken, Job job) {
        jobService.edit(userToken, job);
        return OK;
    }

    /**
     * 岗位增加招聘人数
     */
    @RequestMapping(value = "/addPeople", method = PUT)
    public ResponseEntity addPeople(String userToken, Long id, Integer additionPeopleCount, PayType payType, String payPassword, String openId) {
        Charge charge = jobService.addPeople(userToken, id, additionPeopleCount, payType, payPassword, openId);
        return isNull(charge) ? OK : ok(charge);
    }

    /**
     * 岗位暂停/恢复
     */
    @RequestMapping(value = "/changeState", method = PUT)
    public ResponseEntity changeState(String userToken, Long id, OperatorType operatorType) {
        jobService.changeState(userToken, id, operatorType);
        return OK;
    }

    /**
     * 岗位删除
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteJob(String userToken, Long id) {
        jobService.deleteJob(userToken, id);
        return OK;
    }

    /**
     * 待付款岗位付款
     */
    @RequestMapping(value = "/pay", method = POST)
    public ResponseEntity payForJob(String userToken, Long id, PayType payType, String payPassword, String openId) {
        Charge charge = jobService.payForJob(userToken, id, payType, payPassword, openId);
        return isNull(charge) ? OK : ok(charge);
    }

    /**
     * 岗位收藏
     */
    @RequestMapping(value = "/collect", method = POST)
    public ResponseEntity collectJob(String userToken, Long id, OperatorType operatorType) {
        jobService.collectJob(userToken, id, operatorType);
        return OK;
    }

    /**
     * 岗位一次性悬赏减少人数退款申请
     */
    @RequestMapping(value = "/cutOnce", method = RequestMethod.POST)
    public ResponseEntity cutOnce(String userToken, Long id, Integer count) {
        jobService.cutCount(userToken, id, count, ModuleKey.RecruitmentType.ONCE);
        return OK;
    }


    /**
     * 岗位按月悬赏减少人数退款申请
     */
    @RequestMapping(value = "/cutMonthly", method = RequestMethod.POST)
    public ResponseEntity cut_monthly(String userToken, Long id, Integer count) {
        jobService.cutCount(userToken, id, count, ModuleKey.RecruitmentType.MONTHLY);
        return OK;
    }

    /**
     * 岗位按月悬赏离职员工退押金申请
     */
    @RequestMapping(value = "/refundResign", method = RequestMethod.POST)
    public ResponseEntity refundResign(String userToken, Long id) {
        jobApplyRecordService.refundResign(userToken, id);
        return OK;

    }

    /**
     * 岗位推荐待确认上班列表
     */
    @RequestMapping(value = "/recordList", method = GET)
    public ResponseEntity listRecord(String userToken, PageVO pageVO) {
        Page<JobApplyRecord> jobApplyRecords = jobApplyRecordService.listRecord(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(jobApplyRecords, e -> propsFilter(e, job_record_wait_confirm_json));
        return ok(jsonObject);
    }

    /**
     * 8.22.1 岗位推荐待确认上班详情(WX)
     *
     * @Author mu.jie
     * @Date 2016/12/9
     */
    @RequestMapping(value = "/recordInfo", method = GET)
    public ResponseEntity recordInfo(String userToken, Long id) {
        JobApplyRecord one = jobApplyRecordService.jobApplyRecord(id);
        JSONObject body = propsFilter(one, job_record_wait_confirm_json);
        return ok(body);
    }

    /**
     * 岗位推荐店铺确认上班
     */
    @RequestMapping(value = "/confirm", method = PUT)
    public ResponseEntity confirm(String userToken, Long id, OperatorType operatorType) {
        jobApplyRecordService.confirm(userToken, id, operatorType);
        return OK;
    }

    /**
     * 岗位推荐在职员工列表
     */
    @RequestMapping(value = "/workerList", method = GET)
    public ResponseEntity listWorker(String userToken, ModuleKey.RecruitmentType type, PageVO pageVO) {
        Page<JobApplyRecord> jobApplyRecords = jobService.listWorker(userToken, type, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(jobApplyRecords, this::jobApplyRecordToJson);
        return ok(jsonObject);
    }

    /**
     * 8.24-1 岗位推荐在职员工详情（WX）
     *
     * @Author mu.jie
     * @Date 2016/12/9
     */
    @RequestMapping(value = "/workerInfo", method = GET)
    public ResponseEntity WorkerInfo(Long id) {
        JobApplyRecord one = jobApplyRecordService.findOne(id);
        JSONObject body = jobApplyRecordToJson(one);
        return ok(body);
    }

    private JSONObject jobApplyRecordToJson(JobApplyRecord one) {
        JSONObject jo = propsFilter(one, job_worker_json);
        Date now = new Date();
        if (one.getJob().getRecType() == MONTHLY) {
            AutomaticRenewals automaticRenewals = automaticRenewalsService.findByJobApplyRecord(one);
            jo.replace("renewStartDate", ISO_DATETIME_FORMAT.format(automaticRenewals.getStartDate()));
            jo.replace("renewEndDate", ISO_DATETIME_FORMAT.format(automaticRenewals.getFinalDate()));
            if (now.getTime() >= automaticRenewals.getStartDate().getTime())
                jo.replace("isStartRenew", true);
            else jo.replace("isStartRenew", false);
        }
        jo.put("resignType", one.getResignType());
        jo.put("isRefund", jobService.isRefunded(one.getRecState(), one.getRefund()));
        if (one.getJob().getRecType() == MONTHLY) {
            parseAutomatic(one, jo);
        }
        return jo;
    }

    /**
     * 岗位推荐在职员工发起离职
     */
    @RequestMapping(value = "/startResign", method = POST)
    public ResponseEntity startResign(String userToken, Long id, String date, ModuleKey.ResignEnum type, String remark) {
        jobApplyRecordService.startResign(userToken, id, date, type, decodePathVariable(remark));
        return OK;
    }

    /**
     * 岗位推荐待确认离职员工离职申诉
     */
    @RequestMapping(value = "/complain", method = POST)
    public ResponseEntity complain(String userToken, Long id, String reason) {
        jobService.complain(userToken, id, decodePathVariable(reason));
        return OK;
    }

    /**
     * 岗位推荐已离职员工列表
     */
    @RequestMapping(value = "/resignList", method = GET)
    public ResponseEntity listResign(String userToken, ModuleKey.RecruitmentType type, PageVO pageVO) {
        Page<JobApplyRecord> jobApplyRecords = jobService.listResign(userToken, type, pageVO.pageRequest());
        return ok(wrapJobRecord(jobApplyRecords));
    }

    private JSONObject wrapJobRecord(Page<JobApplyRecord> jobApplyRecords) {
        return pageToJson(jobApplyRecords, this::parseJobApplyRecordToJobWorkerJson);
    }

    /**
     * 岗位推荐已离职员工列表详情（WX）
     *
     * @Author mu.jie
     * @Date 2016/12/9
     */
    @RequestMapping(value = "/resignInfo", method = GET)
    public ResponseEntity resignInfo(Long id) {
        JobApplyRecord one = jobApplyRecordService.findOne(id);
        JSONObject body = parseJobApplyRecordToJobWorkerJson(one);
        return ok(body);
    }

    private JSONObject parseJobApplyRecordToJobWorkerJson(JobApplyRecord one) {
        JSONObject jo = propsFilter(one, job_worker_json);
        jo.put("resignType", one.getResignType());
        jo.put("isRefund", jobService.isRefunded(one.getRecState(), one.getRefund()));
        parseAutomatic(one, jo);
        return jo;
    }

    private void parseAutomatic(JobApplyRecord one, JSONObject jo) {
        AutomaticRenewals automaticRenewals = jobService.getAutomaticRenewals(one);
        ifNotNullThen(automaticRenewals, x -> {
//            jo.put("isStartRenew", automaticRenewals.getAuto());
            Date now = new Date();
            if (now.getTime() >= automaticRenewals.getStartDate().getTime())
                jo.replace("isStartRenew", true);
            else jo.replace("isStartRenew", false);
            java.sql.Date sqlNow = valueOf(LocalDate.now());
            Date nowTime = new Date(sqlNow.getTime());
            jo.put("renewStartDate", automaticRenewals.getStartDate().getTime());
            jo.put("renewEndDate", automaticRenewals.getFinalDate().getTime());
            jo.put("breachAmount", automaticRenewals.getBreach());
            if (automaticRenewals.getBreach().doubleValue() > 0)
                jo.put("breachDay", DateUtil.dayGap(new Date(automaticRenewals.getFinalDate().getTime()), nowTime));//违约天数
            else jo.put("breachDay", null);
        });
        ifNullThen(automaticRenewals, () -> {
            jo.put("isStartRenew", null);
            jo.put("renewStartDate", null);
            jo.put("renewEndDate", null);
            jo.put("breachAmount", null);
        });
    }

    /**
     * 岗位推荐上班失败员工列表
     */
    @RequestMapping(value = "/failedList", method = GET)
    public ResponseEntity listFailed(String userToken, ModuleKey.RecruitmentType type, PageVO pageVO) {
        Page<JobApplyRecord> jobApplyRecords = jobService.listFailed(userToken, type, pageVO.pageRequest());
        return ok(wrapJobRecord(jobApplyRecords));
    }

    /**
     * 岗位推荐在职员工按月悬赏续费详情
     */
    @RequestMapping(value = "/autoRenewDetail", method = GET)
    public ResponseEntity showAutoRenewDetail(String userToken, Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordService.getJobApplyRecord(userToken, id);
        JSONObject jsonObject = propsFilter(jobApplyRecord, job_worker_json);
        jsonObject.put("resignType", null);
        jsonObject.put("isRefund", jobService.isRefunded(jobApplyRecord.getRecState(), jobApplyRecord.getRefund()));
        parseAutomatic(jobApplyRecord, jsonObject);
        return ok(jsonObject);
    }


    /**
     * 岗位推荐在职员工按月悬赏续费
     */
    @RequestMapping(value = "/renew", method = POST)
    public ResponseEntity renew(String userToken, Long id, PayType payType, String payPassword, String openId) {
        Charge charge = jobService.renew(userToken, id, payType, payPassword, openId);
        if (isNull(charge)) return OK;
        return ok(charge);
    }

    /**
     * 处理退款（临时,缺流水记录）
     */
    @RequestMapping(value = "/handle", method = POST)
    public ResponseEntity handle(Long id, OperatorType type) {
        jobService.handle(id, type);
        return OK;
    }

    /**
     * 8.33岗位按月悬赏离职员工退押金申请
     */
    @RequestMapping(value = "/refundInfo", method = GET)
    public ResponseEntity showRefundInfo(String userToken, Long jobId) {
        Job job = jobService.findCanRefundJob(userToken, jobId);
        JSONObject jo = propsFilter(job, job_refund_info_json);
        jo.put("resignedAndRefundedPeopleCount", job.getRefundCount());
        jo.put("resignedAndNotRefundPeopleCount", jobService.getResignAndNotRefundedPeopleCount(job));
        jo.put("resignedAndNotRefundWithThreeMonthAmountPeopleCount", jobApplyRecordService.countRefundType(jobId, 0));
        jo.put("resignedAndNotRefundWithFourMonthAmountPeopleCount", jobApplyRecordService.countRefundType(jobId, 1));
        jo.put("breachPeople", jobApplyRecordService.countRefundType(jobId, -1));//暂无问题
        jo.put("breachDay", jobApplyRecordService.countResignDays(jobId));//已修改过
        jo.put("breachAmount", jobApplyRecordService.countResignBreach(jobId));//可以
        jo.put("refundAmount", jobApplyRecordService.countResignMoney(jobId));//可以
        return ok(jo);
    }

    /**
     * 8.34岗位推荐在职员工撤回离职
     */
    @RequestMapping(value = "/undo", method = PUT)
    public ResponseEntity undo(String userToken, Long id) {
        jobApplyRecordService.undo(userToken, id);
        return OK;
    }

    /**
     * 8.9我的岗位详情（弃用）
     *
     * @Author mu.jie
     * @Date 2016/12/7
     */
    @RequestMapping(value = "/jobInfo", method = GET)
    public ResponseEntity myJobInfo(String userToken, Long id) {
        String[] JSON = {"id", "name", "date", "areaName", "salary", "workYear", "epmCount:unFindPeopleCount", "workerCount:workingPeopleCount",
                "quitterCount:resignedPeopleCount", "resignedAndRefundedPeopleCount", "resignedAndNotRefundPeopleCount", "reward:rewardAmount",
                "recType:jobType", "state", "jobState", "description", "isAuto"};
        Job job = jobService.findOne(id);
        ifTrueThrow(!userToken.equals(job.getShop().getOwner().getToken()), TIP_NO_AUTHORITY);
        JSONObject body = propsFilter(job, JSON);
        body.replace("salary", propsFilter(job.getSalary(), job_type_json));
        body.replace("workYear", propsFilter(job.getPeriod(), job_type_json));
        body.replace("jobState", propsFilter(job.getJobType(), job_type_json));
        body.replace("resignedAndRefundedPeopleCount", job.getRefundCount());
        body.replace("resignedAndNotRefundPeopleCount", jobService.getResignAndNotRefundedPeopleCount(job));
        return ok(body);
    }

    /**
     * 8.35 岗位取消线下付款
     *
     * @Author xiao xue wei
     * @Date 2017/3/15
     */
    @RequestMapping(value = "/cancelOffline", method = POST)
    public ResponseEntity cancelOffline(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        jobService.cancelOffline(member, id);
        return OK;
    }

    /**
     * 8.36 岗位取消增加人数
     *
     * @Author xiao xue wei
     * @Date 2017/3/17
     */
    @RequestMapping(value = "/cancelAddPeople", method = POST)
    public ResponseEntity cancelAddPeople(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        jobService.cancelAddPeople(member, id);
        return OK;
    }
}
