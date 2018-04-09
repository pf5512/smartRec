package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.service.ModuleKey.ApplyEnum;
import com.thousandsunny.service.model.HpApply;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.HpApplyService;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.JsonUtil.valueIsNull;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.service.ModuleKey.ApplyType.*;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * Created by mu.jie on 2016/12/5.
 */
@RestController
@RequestMapping(value = "/api/manager/hpApply", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerHpApplyController {

    @Autowired
    private HpApplyService hpApplyService;
    @Autowired
    private AccountFlowService accountFlowService;

    /**
     * 9.10.1 岗位招聘退款管理
     *
     * @Author mu.jie
     * @Date 2016/12/5
     */
    @RequestMapping(method = GET)
    public Result findHpApplyList(BackPageVo backPageVo, String text, String tableType, Date startTime, Date endTime, ApplyEnum refundStatus) {
        String[] JSON = {"id", "job.shop.name", "job.name", "job.recType.title", "job.reward", "type.title", "job.epmCount", "money", "date", "state"};
        Page<HpApply> page = hpApplyService.findHpApplyList(backPageVo, decodePathVariable(text), tableType, startTime, endTime, refundStatus);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, JSON);
            ifNotNullThen(x.getDate(), t -> jo.replace("date", ISO_DATETIME_FORMAT.format(t)));
            enumToJson(x.getState(), jo, "state");
            return jo;
        }));
    }

    /**
     * 9.10.2 退款修改
     *
     * @Author mu.jie
     * @Date 2016/12/6
     */
    @RequestMapping(method = POST)
    public Result updateHpApplyList(Long id, ApplyEnum auditState, String reson) {
        hpApplyService.updateHpApplyList(id, auditState, reson);
        return OK("success");
    }

    /**
     * 9.10.3 已审核统计
     *
     * @Author mu.jie
     * @Date 2016/12/6
     */
    @RequestMapping(value = "/count", method = GET)
    public Result countHpApply(String text, Date startTime, Date endTime, ApplyEnum refundStatus) {
//        JOB_REFUND("离职退款"), JOB_CUT("岗位减少人数退款"),JOB_RESIGN("退押金");
        BigDecimal tkB = hpApplyService.countHpApply(decodePathVariable(text), startTime, endTime, refundStatus, JOB_REFUND);
        BigDecimal tk = tkB != null ? tkB : BigDecimal.ZERO;
        BigDecimal jsB = hpApplyService.countHpApply(decodePathVariable(text), startTime, endTime, refundStatus, JOB_CUT);
        BigDecimal js = jsB != null ? jsB : BigDecimal.ZERO;
        BigDecimal tyjB = hpApplyService.countHpApply(decodePathVariable(text), startTime, endTime, refundStatus, JOB_RESIGN);
        BigDecimal tyj = tyjB != null ? tyjB : BigDecimal.ZERO;
        BigDecimal sum = tk.add(js).add(tyj);
        JSONObject body = new JSONObject();
        body.put("hiringRefund", sum);
        body.put("restFullRefundFee", tk);
        body.put("reduceHiringCost", js);
        body.put("refundDepositFee", tyj);
        return OK(body);
    }

    /**
     * 9.10.4 详情
     *
     * @Author mu.jie
     * @Date 2016/12/6
     */
    @RequestMapping(value = "/info", method = GET)
    public Result findHpApplyInfo(Long id) {
        String[] JSON = {"job.shop.name:shopName", "job.name:positionName", "job.recType.title:recruitType", "job.reward:award",
                "positionOpType", "state:operation", "remark"};
        HpApply one = hpApplyService.findOne(id);
        JSONObject body = propsFilter(one, JSON);
        enumToJson(one.getType(), body, "positionOpType");
        if (isNotNull(one.getDate())) body.put("applyRefundTime", ISO_DATETIME_FORMAT.format(one.getDate()));
        else valueIsNull(body, null, "applyRefundTime");
        body.put("refundWay", "账户余额");
        enumToJson(one.getState(), body, "operation");
        body.put("allRefund", one.getMoney());
        body.put("recruitNum", one.getRefundCount());
        AccountFlow accountFlow = null;
        if (one.getType() == JOB_CUT || one.getType() == JOB_ALL) {
            if (one.getState() == ApplyEnum.REVIEW_SUCCESS) {
                accountFlow = accountFlowService.findHpApplyFlow(one);
            }
        } else {
            body.put("dimissionNum", one.getRefundCount());
            body.put("depositAndAwardNum", one.getRefundFourNum());
            body.put("onlyDepositNum", one.getRefundThreeNum());
            if (one.getState() == ApplyEnum.REVIEW_SUCCESS) {
                accountFlow = accountFlowService.findReturnDepositHpApplyFlow(one);
            }
            if (isNotNull(one.getBreach())) {
                body.put("breakContractRcruit", one.getBreachJobApplyNum());
                body.put("breakContractDays", one.getBreachDays());
                body.put("breakContractAmount", one.getBreach());
            }
        }
        ifNotNullThen(accountFlow, e -> {
            if (isNotNull(e.getCreateDate()))
                body.put("refundTime", ISO_DATETIME_FORMAT.format(e.getCreateDate()));
            else body.put("refundTime", null);
            body.put("refundFlow", e.getOrderNo());
        });

        return OK(body);
    }
}
