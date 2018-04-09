package com.thousandsunny.manager.controller;

/**
 * Created by 13336 on 2017/2/19.
 */

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.service.ModuleKey.CardCouponState;
import com.thousandsunny.service.ModuleKey.CardCouponType;
import com.thousandsunny.service.model.CardCoupon;
import com.thousandsunny.service.model.CardCouponReceive;
import com.thousandsunny.service.model.TransferRecord;
import com.thousandsunny.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import com.thousandsunny.service.ModuleKey.RecruitmentType;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.service.ModuleTips.TIP_CARD_NOT_EXIST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api/manager/cardCoupon", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerShopCardCouponController {
    @Autowired
    private CardCouponService cardCouponService;
    @Autowired
    private CardCouponReceiveService cardCouponReceiveService;
    @Autowired
    private TransferRecordService transferRecordService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;

    /**
     * 11.2.11 店铺卡券管理
     *
     * @Author xiao xue wei
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/cardCouponList", method = GET)
    public Result cardCouponList(BackPageVo pageVo, CardCouponType cardVoucherType, String text, CardCouponState cardVoucherStatus, Long shopId) {
        String[] CARDCOUPON_LIST_INFO = {"id", "name", "type.title", "state", "isEnableBoolean"};
        Page<CardCoupon> page = cardCouponService.findcardCouponPage(pageVo.pageRequest(), cardVoucherType, cardVoucherStatus, shopId, text);
        return OK(page.map(cardCoupon -> {
            JSONObject jsonObject = propsFilter(cardCoupon, CARDCOUPON_LIST_INFO);
            enumToJson(cardCoupon.getState(), jsonObject, "state");
            if (isNotNull(cardCoupon.getCreateTime()))
                jsonObject.put("publishTime", ISO_DATETIME_FORMAT.format(cardCoupon.getCreateTime()));
            else jsonObject.put("publishTime", null);
            if (isNotNull(cardCoupon.getValidDate()))
                jsonObject.put("validTime", ISO_DATETIME_FORMAT.format(cardCoupon.getValidDate()));
            else jsonObject.put("validTime", null);
            return jsonObject;
        }));
    }

    /**
     * 11.2.12 卡券启用
     *
     * @Author xiao xue wei
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/cardCouponEnable", method = POST)
    public Result cardCouponEnable(Long id) {
        cardCouponService.enableCardCoupon(id);
        return OK();
    }

    /**
     * 11.2.13 卡券详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/cardCouponDetail", method = GET)
    public Result cardCouponDetail(Long id) {
        String[] CARDCOUPON_DETAIL_INFO = {"id", "name", "type", "details:goodsDetails", "useNotice:noticeForUse", "state.title:status",};
        CardCoupon cardCoupon = cardCouponService.findOne(id);
        ifNullThrow(cardCoupon, TIP_CARD_NOT_EXIST);
        JSONObject jsonObject = propsFilter(cardCoupon, CARDCOUPON_DETAIL_INFO);
        enumToJson(cardCoupon.getType(), jsonObject, "type");
        if (cardCoupon.getType() == CardCouponType.COUPON) {
            jsonObject.put("discountAmount", cardCoupon.getSalePrice());
            jsonObject.put("originalPrice", cardCoupon.getCostPrice());
            jsonObject.put("allowedGetMany", cardCoupon.getCanReceiveManyTimes().getTitle());
        } else jsonObject.put("discount", cardCoupon.getDiscount());
        if (isNotNull(cardCoupon.getCreateTime()))
            jsonObject.put("publishTime", ISO_DATETIME_FORMAT.format(cardCoupon.getCreateTime()));
        else jsonObject.put("publishTime", null);
        if (isNotNull(cardCoupon.getValidDate()))
            jsonObject.put("validTime", ISO_DATETIME_FORMAT.format(cardCoupon.getValidDate()));
        else jsonObject.put("validTime", null);
        if (isNotNull(cardCoupon.getStopTime()))
            jsonObject.put("endTime", ISO_DATETIME_FORMAT.format(cardCoupon.getStopTime()));
        else jsonObject.put("endTime", null);
        return OK(jsonObject);
    }

    /**
     * 11.2.14 卡券领取使用记录
     *
     * @Author xiao xue wei
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/cardCouponUseRecord", method = GET)
    public Result cardCouponUseRecord(BackPageVo pageVo, String text, String useStatus, Long cardVoucherId) {
        String[] CARDCOUPONRECEIVE_LIST_INFO = {"id", "member.realName", "member.mobile", "member.hpAccount",};
        Page<CardCouponReceive> page = cardCouponReceiveService.findShopCardCouponReceivePage(pageVo.pageRequest(), text, useStatus, cardVoucherId);
        return OK(page.map(cardCouponReceive -> {
            JSONObject jsonObject = propsFilter(cardCouponReceive, CARDCOUPONRECEIVE_LIST_INFO);
            if (isNotNull(cardCouponReceive.getReceiveTime()))
                jsonObject.put("getTime", ISO_DATETIME_FORMAT.format(cardCouponReceive.getReceiveTime()));
            else jsonObject.put("getTime", null);
            if (cardCouponReceive.getIsOverdue() == ModuleKey.BooleanEnum.YES) {
                jsonObject.put("useStatus", "已过期");
            } else {
                if (cardCouponReceive.getCardCoupon().getType() == CardCouponType.COUPON) {
                    if (cardCouponReceive.getIsUse() == ModuleKey.BooleanEnum.YES)
                        jsonObject.put("useStatus", "已使用");
                    else jsonObject.put("useStatus", "正常");
                } else jsonObject.put("useStatus", "正常");
            }
            return jsonObject;
        }));
    }

    /**
     * 11.2.15 店铺统计
     *
     * @Author xiao xue wei
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/countShop", method = GET)
    public Result countShop(Date startTime, Date endTime, RecruitmentType rewardType, Long shopId) {
        JSONObject body = new JSONObject();
        List<JSONObject> recruitArr = jobService.countJobInfo(startTime, endTime, rewardType, shopId);
        body.put("recruitArr", recruitArr);
        List<JSONObject> workStuffArr = jobApplyRecordService.countShopJobApplyInfo(startTime, endTime, rewardType, shopId);
        body.put("workStuffArr", workStuffArr);
        return OK(body);
    }

    /**
     * 11.2.16 转让记录
     *
     * @Author xiao xue wei
     * @Date 2017/2/19
     */
    @RequestMapping(value = "/transferRecord", method = GET)
    public Result transferRecord(BackPageVo pageVo, Long shopId) {
        String[] TRANSFER_RECORD_LIST_INFO = {"id", "assignor.realName", "assignor.hpAccount", "receiverRealName", "receiverHpAccount", "excuse"};
        Page<TransferRecord> page = transferRecordService.findTransferRecordPage(shopId, pageVo.pageRequest());
        return OK(page.map(transferRecord -> {
            JSONObject jsonObject = propsFilter(transferRecord, TRANSFER_RECORD_LIST_INFO);
            if (isNotNull(transferRecord.getDate()))
                jsonObject.put("applyTime", ISO_DATETIME_FORMAT.format(transferRecord.getDate()));
            else jsonObject.put("applyTime", null);
            if (transferRecord.getState() == ModuleKey.ApplyState.AGREE) {
                if (isNotNull(transferRecord.getReviewTime()))
                    jsonObject.put("passTime", ISO_DATETIME_FORMAT.format(transferRecord.getReviewTime()));
                else jsonObject.put("passTime", null);
            } else jsonObject.put("passTime", null);
            return jsonObject;
        }));
    }
}
