package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CourseRefundReason;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.CourseApplyService;
import com.thousandsunny.service.service.CourseEvaluationService;
import com.thousandsunny.service.service.CourseRefundApplyService;
import com.thousandsunny.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.JsonUtil.valueIsNull;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.FREE;
import static com.thousandsunny.service.ModuleTips.TIP_NO_COURSEAPPLY;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by 13336 on 2017/2/15.
 */
@RestController
@RequestMapping(value = "/api/portal/course", produces = APPLICATION_JSON_UTF8_VALUE)
public class CourseController {
    @Autowired
    private CourseApplyService courseApplyService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CourseEvaluationService courseEvaluationService;
    @Autowired
    private CourseRefundApplyService courseRefundApplyService;

    /**
     * 18.1 我的培训订单列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/myCourseOrderList", method = GET)
    public ResponseEntity myCourseOrderList(String userToken, PageVO pageVO) {
        String[] COURSEAPPLY_LIST_INFO = {"id", "state", "school.name:schoolName", "course.name:courseName", "course.day:courseDay",
                "price:coursePrice", "trainDate", "serialNo:orderNo", "discount:preferentialPrice"};
        Member member = memberService.findByToken(userToken);
        Page<CourseApply> courseApplies = courseApplyService.findCourseApplyPage(member, pageVO.pageRequest());
        JSONObject body = pageToJson(courseApplies, courseApply -> {
            JSONObject jo = propsFilter(courseApply, COURSEAPPLY_LIST_INFO);
            if (!courseApply.getCourse().getPhotos().isEmpty())
                jo.put("firstImageUrl", courseApply.getCourse().getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            jo.put("trainCompleteDate", new Date(courseApply.getTrainDate().getTime() + courseApply.getCourse().getDay() * (24 * 3600 * 1000)));
            return jo;
        });
        return ok(body);
    }

    /**
     * 18.2 培训订单详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/courseOrderDetail", method = GET)
    public ResponseEntity courseOrderDetail(String userToken, String orderNo) {
        String[] COURSEAPPLY_DETAIL_INFO = {"id", "state", "school.name:schoolName", "course.name:courseName", "course.day:courseDay", "trainCompleteDate",
                "price:coursePrice", "trainDate", "serialNo:orderNO", "date:signDate", "payType", "closeDate", "payDate", "courseFirstImageUrl", "refundMaxAmount"};
        Member member = memberService.findByToken(userToken);
        CourseApply apply = courseApplyService.findCourseApply(member, orderNo);
        ifNullThrow(apply, TIP_NO_COURSEAPPLY);
        JSONObject body = propsFilter(apply, COURSEAPPLY_DETAIL_INFO);
        if (!apply.getCourse().getPhotos().isEmpty())
            body.replace("courseFirstImageUrl", apply.getCourse().getPhotos().get(0).getPath());
        body.replace("refundMaxAmount", apply.getPrice().subtract(apply.getDiscount()));
        CourseRefundApply courseRefundApply = courseRefundApplyService.findByMemberAndCourseApply(member, apply);
        if (isNotNull(courseRefundApply)) {
            body.put("refundApplyDate", courseRefundApply.getCreateTime());    // 退款申请时
            body.put("refundReviewDate", courseRefundApply.getAuditTime());    // 退款审核时间
            body.put("refundCancelDate", courseRefundApply.getCancelTime());    // 退款取消时间
            body.put("refundReceivedDate", courseRefundApply.getReceivedDate());   // 退款到账时间
            body.put("refundReason", courseRefundApply.getReason().getTitle());    // 退款原因
            body.put("refundRemark", courseRefundApply.getRemark());    // 退款备注
            body.put("refundFailReason", courseRefundApply.getRefundRemark());    // 退款失败原因
        } else {
            valueIsNull(body, null, "refundApplyDate", "refundReviewDate", "refundCancelDate",
                    "refundReceivedDate", "refundReason", "refundRemark", "refundFailReason");
        }
        ifTrueThen(apply.getTrainDate() != null && apply.getCourse().getDay() != null,
                () -> body.replace("trainCompleteDate", addDays(apply.getTrainDate(), apply.getCourse().getDay())));
        CourseEvaluation courseEvaluation = courseEvaluationService.findCourseEvaluation(member, apply);
        if (isNotNull(courseEvaluation)) {
            body.put("commentScore", courseEvaluation.getScore());
            body.put("commentDate", courseEvaluation.getCreateTime());
            body.put("commentContent", courseEvaluation.getContent());
            if (!courseEvaluation.getPhotos().isEmpty()) {
                List<JSONObject> list = new ArrayList<>();
                courseEvaluation.getPhotos().forEach(cloudFile -> list.add(propsFilter(cloudFile, "path")));
                body.put("commentImageList", list);
            } else body.put("commentImageList", null);
        } else {
            valueIsNull(body, null, "commentScore", "commentDate", "commentContent", "commentImageList");
        }

        body.put("preferentialPrice", apply.getDiscount());// 优惠金额
        List<RedPacket> redPacketList = new ArrayList<>();
        apply.getRedPacketReceives().forEach(redPacketReceive -> redPacketList.add(redPacketReceive.getRedPacket()));
        if (apply.getIsUseFee() == YES) {
            body.put("preferentialType", FREE);
        } else {
            if (redPacketList.isEmpty()) {
                body.put("preferentialType", null);
            } else {
                body.put("preferentialType", "RED_PACKET");
            }
        }
        if (!redPacketList.isEmpty()) {
            List<JSONObject> redPackets = new ArrayList<>();
            for (RedPacket r : redPacketList) {
                JSONObject j = new JSONObject();
                j.put("price", r.getAmount().doubleValue());
                redPackets.add(j);
            }
            body.put("redPacketList", redPackets);
        } else body.put("redPacketList", null);

        return ok(body);
    }

    /**
     * 18.3 取消线下付款(type=COURSE_ORDER_OFFLINE_PAY_CONFIRM(线下付款待确认)时，才允许发起取消线下付款)
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/cancelUnderLine", method = POST)
    public ResponseEntity cancelUnderLine(String userToken, String orderNo) {
        Member member = memberService.findByToken(userToken);
        courseApplyService.cancelUnderLine(member, orderNo);
        return OK;
    }

    /**
     * 18.4 取消培训(type=COURSE_ORDER_WAIT_FOR_PAY(待付款)时，才允许发起取消培训操作)
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/cancelCourse", method = POST)
    public ResponseEntity cancelCourse(String userToken, String orderNo) {
        Member member = memberService.findByToken(userToken);
        courseApplyService.cancelCourse(member, orderNo);
        return OK;
    }

    /**
     * 18.5 培训完成(type= COURSE_ORDER_PAID(已付款) 且当前时间》培训时间+培训天数，才允许发起培训完成操作)
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/finishCourse", method = POST)
    public ResponseEntity finishCourse(String userToken, String orderNo) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        courseApplyService.finishCourse(member, orderNo);
        return OK;
    }

    /**
     * 18.6 培训评价(已培训后30天内都可进行评价，超过时间，则无法“评价”；)
     *
     * @Author xiao xue wei
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/evaluateCourse", method = POST)
    public ResponseEntity evaluateCourse(String userToken, String orderNo, CourseEvaluation courseEvaluation) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        courseEvaluationService.evaluateCourse(member, orderNo, courseEvaluation);
        return OK;
    }

    /**
     * 18.7 培训发起退款
     *
     * @Author mu.jie
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/refund", method = POST)
    public ResponseEntity courseRefund(String userToken, String orderNo, CourseRefundReason reason, String remark, BigDecimal amount) {
        Member member = memberService.findByToken(userToken);
        courseRefundApplyService.refund(member, orderNo, reason, remark, amount);
        return OK;
    }

    /**
     * 18.8 培训取消退款(type=COURSE_ORDER_REFUNDING(退款中)，才允许发起取消退款操作)
     *
     * @Author mu.jie
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/cancelRefund", method = POST)
    public ResponseEntity cancelCourseRefund(String userToken, String orderNo) {
        Member member = memberService.findByToken(userToken);
        courseRefundApplyService.cancelCourseRefund(member, orderNo);
        return OK;
    }

}
