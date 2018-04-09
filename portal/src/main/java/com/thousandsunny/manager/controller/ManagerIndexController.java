package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.FeedBackService;
import com.thousandsunny.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.thousandsunny.common.entity.Result.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by 13336 on 2017/1/5.
 */
@RestController
@RequestMapping(value = "/api/manager/index", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerIndexController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MomentsService momentsService;
    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private PartnerApplyService partnerApplyService;
    @Autowired
    private FeedBackService feedBackService;
    @Autowired
    private ComplainService complainService;
    @Autowired
    private BenefitService benefitService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private MemberVisitService memberVisitService;
    @Autowired
    private SchoolApplyService schoolApplyService;
    @Autowired
    private SchoolService schoolService;

//    /**
//     * 后台管理系统首页数据
//     *
//     * @Author xiao xue wei
//     * @Date 2017/1/5
//     */
//    @RequestMapping(method = GET)
//    public Result index(Date startTime, Date endTime) {
//        JSONObject jsonObject = new JSONObject();
//        //注册人数
//        jsonObject.put("registerNum", memberService.countMembers(startTime, endTime));
//        //惠美圈相关数据
//        jsonObject.put("huimeiquan", momentsService.countMomentsInfo(startTime, endTime));
//        //创业者相关数据
//        jsonObject.put("chuangyezhe", entrepreneursService.countEntrepreneursInfo(startTime, endTime));
//        //合伙人相关数据
//        jsonObject.put("hehuoren", partnerApplyService.countPartnerInfo(startTime, endTime));
//        //学校相关数据
//        JSONObject xuexiaojigou = new JSONObject();
//        xuexiaojigou.put("applySchool", 0);
//        xuexiaojigou.put("isSchool", 0);
//        xuexiaojigou.put("comSchool", 0);
//        jsonObject.put("xuexiaojigou", xuexiaojigou);
//        //TODO:useCase数据
//
//        //useCase数据统计
//        JSONObject useCase = new JSONObject();
//        useCase.put("noLogin", memberVisitService.countLoginInfo(startTime, endTime, "no"));
//        useCase.put("logined", memberVisitService.countLoginInfo(startTime, endTime, "yes"));
//        useCase.put("meiyeren", memberVisitService.countMeiyerenInfo(startTime, endTime, "yes"));
//        useCase.put("noMeiyeren", memberVisitService.countMeiyerenInfo(startTime, endTime, "no"));
//        jsonObject.put("useCase", useCase);
//
//        //待处理相关数据
//        JSONObject suspend = new JSONObject();
//        suspend.put("cyzApplyNum", entrepreneursService.countApplyingEntre(startTime, endTime));
//        suspend.put("hhrNum", partnerApplyService.countApplyingPartner(startTime, endTime));
//        suspend.put("schoolApplyNum", 0);
//        suspend.put("cusService", feedBackService.countNotDealFeedBack(startTime, endTime));
//        suspend.put("tousMgt", complainService.countNontDealComplain(startTime, endTime));
//        suspend.put("gzbz", benefitService.countNotDealSalaryProtection(startTime, endTime));
//        suspend.put("dksq", benefitService.countNotDealQuickLoan(startTime, endTime));
//        suspend.put("gwtk", accountFlowService.countReviewingJobRefund(startTime, endTime));
//        suspend.put("txsq", accountFlowService.countReviewingWithdraw(startTime, endTime));
//
//        jsonObject.put("suspend", suspend);
//        return OK(jsonObject);
//    }

    /**
     * 13.1 首页数据1
     *
     * @Author xiao xue wei
     * @Date 2017/3/16
     */
    @RequestMapping(value = "/one", method = GET)
    public Result indexOne(Date startTime, Date endTime) {
        JSONObject jsonObject = new JSONObject();
        //注册人数
        jsonObject.put("registerNum", memberService.countMembers(startTime, endTime));
        //惠美圈相关数据
        jsonObject.put("huimeiquan", momentsService.countMomentsInfo(startTime, endTime));
        //创业者相关数据
        jsonObject.put("chuangyezhe", entrepreneursService.countEntrepreneursInfo(startTime, endTime));
        //合伙人相关数据
        jsonObject.put("hehuoren", partnerApplyService.countPartnerInfo(startTime, endTime));
        //学校相关数据
        JSONObject xuexiaojigou = new JSONObject();
        xuexiaojigou.put("applySchool", schoolApplyService.countSchoolApplyInfo(startTime, endTime));
        xuexiaojigou.put("isSchool", schoolService.countSchoolInfo(startTime, endTime, "school"));
        xuexiaojigou.put("comSchool", schoolService.countSchoolInfo(startTime, endTime, "partSchool"));
        jsonObject.put("xuexiaojigou", xuexiaojigou);
        return OK(jsonObject);
    }

    /**
     * 13.2 首页数据2
     *
     * @Author xiao xue wei
     * @Date 2017/3/16
     */
    @RequestMapping(value = "/two", method = GET)
    public Result indexTwo(Date startTime, Date endTime) {
        //useCase数据统计
        JSONObject jsonObject = new JSONObject();
        JSONObject useCase = new JSONObject();
        useCase.put("noLogin", memberVisitService.countLoginInfo(startTime, endTime, "no"));
        useCase.put("logined", memberVisitService.countLoginInfo(startTime, endTime, "yes"));
        useCase.put("meiyeren", memberVisitService.countMeiyerenInfo(startTime, endTime, "yes"));
        useCase.put("noMeiyeren", memberVisitService.countMeiyerenInfo(startTime, endTime, "no"));
        jsonObject.put("userCase", useCase);
        return OK(jsonObject);
    }

    /**
     * 13.3 首页数据3
     *
     * @Author xiao xue wei
     * @Date 2017/3/16
     */
    @RequestMapping(value = "/three", method = GET)
    public Result indexThree(Date startTime, Date endTime) {
        //useCase数据统计
        JSONObject jsonObject = new JSONObject();
        //待处理相关数据
        JSONObject suspend = new JSONObject();
        suspend.put("cyzApplyNum", entrepreneursService.countApplyingEntre(startTime, endTime));
        suspend.put("hhrNum", partnerApplyService.countApplyingPartner(startTime, endTime));
        suspend.put("schoolApplyNum", schoolApplyService.countApplyingSchool(startTime, endTime));
        suspend.put("cusService", feedBackService.countNotDealFeedBack(startTime, endTime));
        suspend.put("tousMgt", complainService.countNontDealComplain(startTime, endTime));
        suspend.put("gzbz", benefitService.countNotDealSalaryProtection(startTime, endTime));
        suspend.put("dksq", benefitService.countNotDealQuickLoan(startTime, endTime));
        suspend.put("gwtk", accountFlowService.countReviewingJobRefund(startTime, endTime));
        suspend.put("txsq", accountFlowService.countReviewingWithdraw(startTime, endTime));
        jsonObject.put("suspend", suspend);
        return OK(jsonObject);
    }
}
