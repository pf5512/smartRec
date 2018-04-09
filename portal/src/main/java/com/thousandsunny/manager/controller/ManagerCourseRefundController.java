package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.CourseApply;
import com.thousandsunny.service.model.CourseRefundApply;
import com.thousandsunny.service.model.RedPacket;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.CourseApplyService;
import com.thousandsunny.service.service.CourseRefundApplyService;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import com.thousandsunny.thirdparty.model.AccountFlow;
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
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.*;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2017/2/21.
 */
@RestController
@RequestMapping(value = "/api/manager/courseRefund", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerCourseRefundController {
    @Autowired
    private CourseRefundApplyService courseRefundApplyService;
    @Autowired
    private CourseApplyService courseApplyService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AccountFlowService accountFlowService;

    private static final String[] REFUND_LIST_JSON = {
            "id", "courseApply.serialNo", "member.mobile", "member.realName", "courseApply.course.name",
            "reason.title", "amount", "school", "platform", "orderStatus", "createTime"
    };
    private static final String[] REFUND_COURSE_INFO_JSON = {
            "courseApply.course.school.name:schoolName", "courseApply.course.name:courseName",
            "courseApply.course.isPlatformCourse:isPartnerCourse", "courseApply.course.platformPercent:platFormShareRate",
            "courseApply.course.redPacketPercent:redPacketUseRate", "courseApply.course.price:amount", "courseApply.course.day:days"
    };
    private static final String[] REFUND_COURSE_APPLY_INFO_INFO_JSON = {
            "courseApply.member.mobile:mobile", "courseApply.member.realName:username",
            "courseApply.member.hpAccount:hpAccount", "courseApply.serialNo:orderNo", "courseApply.trainDate:trainTime",
            "courseApply.price:totalAmount", "couponWay", "redPacketUseNum", "redPacketAmount", "courseApply.discount:discount",
            "courseApply.price:amountPayable", "schoolIncome", "platFormIncome", "orderStatus",
            "courseApply.date:signUpTime", "courseApply.payType:payWay", "courseApply.payDate:payTime"
    };
    private static final String[] REFUND_COURSE_REBACK_STATUS_JSON = {
            "reason.title:rebackReason", "amount:rebackTotal", "school:schoolRebackAmount", "platform:platFormRebackAmount",
            "createTime:rebackApplyTime", "rebackWay", "auditTime:rebackAuditTime", "cancelTime:cancelRebackTime",
            "opration", "refundRemark:remark", "receivedDate:rebackTime", "way:rebackPathway", "rebackFlow"
    };

    /**
     * 9.13.1 课程报名退款列表
     *
     * @Author mu.jie
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/list", method = GET)
    public Result findRefundList(String userToken, BackPageVo backPageVo, String tableType, String text, Date startTime, Date endTime) {
        Member member = memberService.findByToken(userToken);
        Page<CourseRefundApply> page = courseRefundApplyService.findRefundList(member, backPageVo, tableType, decodePathVariable(text), startTime, endTime);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, REFUND_LIST_JSON);
            jo.put("userType", member.getRole());
            jo.replace("createTime", ISO_DATETIME_FORMAT.format(x.getCreateTime()));
            enumToJson(x.getState(), jo, "orderStatus");
            return jo;
        }));
    }

    /**
     * 9.13.2 已审核统计
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    @RequestMapping(value = "/count", method = GET)
    public Result countRefund(String userToken, String text, Date startTime, Date endTime) {
        Member member = memberService.findByToken(userToken);
        BigDecimal money = courseRefundApplyService.countRefund(member, decodePathVariable(text), startTime, endTime, "sum");
        BigDecimal platform = courseRefundApplyService.countRefund(member, decodePathVariable(text), startTime, endTime, "platform");
        BigDecimal school = courseRefundApplyService.countRefund(member, decodePathVariable(text), startTime, endTime, "school");
        JSONObject body = new JSONObject();
        body.put("totalRebackAmount", money == null ? BigDecimal.ZERO : money);
        body.put("schoolRebackAmount", school == null ? BigDecimal.ZERO : school);
        body.put("platRebackAmount", platform == null ? BigDecimal.ZERO : platform);
        return OK(body);
    }

    /**
     * 9.13.3 详情
     *
     * @Author mu.jie
     * @Date 2017/2/21
     */
    @RequestMapping(value = "/info", method = GET)
    public Result findRefundInfo(Long id) {
        CourseRefundApply one = courseRefundApplyService.findOne(id);
        JSONObject body = new JSONObject();
        JSONObject courseInfo = propsFilter(one, REFUND_COURSE_INFO_JSON);
        CourseApply courseApply = one.getCourseApply();
        enumToJson(courseApply.getCourse().getIsPlatformCourse(), courseInfo, "isPartnerCourse");
        body.put("courseInfo", courseInfo);

        JSONObject signUpInfo = propsFilter(one, REFUND_COURSE_APPLY_INFO_INFO_JSON);
        ifNotNullThen(one.getCourseApply().getTrainDate(), d -> signUpInfo.replace("trainTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getCourseApply().getDate(), d -> signUpInfo.replace("signUpTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getCourseApply().getPayDate(), d -> signUpInfo.replace("payTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getCourseApply().getPayType(), d -> enumToJson(d, signUpInfo, "payWay"));
        ifNotNullThen(one.getCourseApply().getState(), d -> enumToJson(d, signUpInfo, "orderStatus"));
        List<RedPacket> redPacketList = new ArrayList<>();
        courseApply.getRedPacketReceives().forEach(redPacketReceive -> redPacketList.add(redPacketReceive.getRedPacket()));
        if (redPacketList != null && redPacketList.size() > 0) {
            JSONObject couponWay = new JSONObject();
            couponWay.put("key", "REDPACKET");
            couponWay.put("text", "红包");
            signUpInfo.replace("couponWay", couponWay);
            signUpInfo.replace("redPacketUseNum", redPacketList.size());
            List<BigDecimal> redPackets = newArrayList();
            redPacketList.forEach(x -> redPackets.add(x.getAmount()));
            signUpInfo.replace("redPacketAmount", redPackets.toArray());
        }
        BigDecimal money = courseApply.getPrice().subtract(courseApply.getDiscount());//用户付的钱
        BigDecimal schoolMoney = money.multiply(new BigDecimal((1d - (courseApply.getCourse().getPlatformPercent() / 100)) + ""));//学校应该分的钱
        BigDecimal platformMoney = money.subtract(schoolMoney);//平台应该分的钱
        signUpInfo.replace("schoolIncome", schoolMoney);
        signUpInfo.replace("platFormIncome", platformMoney);
        if (courseApply.getState() == COURSE_ORDER_TRAINED_COMMENTED || courseApply.getState() == COURSE_ORDER_TRAINED_UNCOMMENT) {
            JSONObject orderStatus = new JSONObject();
            orderStatus.put("key", "COURSE_ORDER_TRAINED");
            orderStatus.put("text", "已培训");
            signUpInfo.replace("orderStatus", orderStatus);
        } else if (courseApply.getState() == COURSE_ORDER_PAID) {
            enumToJson(courseApply.getState(), signUpInfo, "orderStatus");
        }
        body.put("signUpInfo", signUpInfo);
        JSONObject refundJson = propsFilter(one, REFUND_COURSE_REBACK_STATUS_JSON);
        refundJson.replace("rebackWay", "账户余额");
        enumToJson(one.getState(), refundJson, "opration");
        ifNotNullThen(one.getCreateTime(), d -> refundJson.replace("rebackApplyTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getAuditTime(), d -> refundJson.replace("rebackAuditTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getCancelTime(), d -> refundJson.replace("cancelRebackTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getReceivedDate(), d -> refundJson.replace("rebackTime", ISO_DATETIME_FORMAT.format(d)));
        AccountFlow accountFlow = accountFlowService.findCourseApplyFlow(courseApply, APPROVAL);
        ifNotNullThen(accountFlow, f -> refundJson.replace("rebackFlow", f.getOrderNo()));
        body.put("rebackStatus", refundJson);
        return OK(body);
    }

    /**
     * 9.13.4 编辑
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    @RequestMapping(method = POST)
    public Result updateCourseRefundState(Long id, OperatorType opration, String remark) {
        CourseRefundApply courseRefundApply = courseRefundApplyService.findOne(id);
        CourseApply courseApply = courseRefundApply.getCourseApply();
        if (opration == SURE) {
            courseRefundApply.setState(COURSE_ORDER_REFUNDED);
            courseRefundApply.setRefundRemark(remark);
            courseApply.setState(COURSE_ORDER_REFUNDED);
            //退钱逻辑
            courseRefundApplyService.refundPay(courseRefundApply);
        } else {
            courseRefundApply.setState(COURSE_ORDER_REFUND_FAIL);
            courseRefundApply.setRefundRemark(remark);
            courseApply.setState(COURSE_ORDER_REFUND_FAIL);
            AccountFlow accountFlow = accountFlowService.findCourseRefundApplyFlow(courseApply, courseRefundApply, APPROVAL);
            ifNotNullThen(accountFlow, x -> {
                x.setState(FAILED);
                accountFlowService.save(x);
            });
        }
        courseRefundApply.setAuditTime(new Date());
        courseRefundApplyService.save(courseRefundApply);
        courseApplyService.save(courseApply);
        return OK("success");
    }

    /**
     * 9.13.5 获取charge
     *
     * @Author mu.jie
     * @Date 2017/3/3
     */
    @RequestMapping(value = "/pay", method = POST)
    public Result pay(Long id) {
        CourseRefundApply courseRefundApply = courseRefundApplyService.findOne(id);
        Charge charge = courseRefundApplyService.pay(courseRefundApply);
        return OK(charge);
    }
}
