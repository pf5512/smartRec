package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CardCouponReceiveState;
import com.thousandsunny.service.model.CardCoupon;
import com.thousandsunny.service.model.CardCouponReceive;
import com.thousandsunny.service.service.CardCouponLookService;
import com.thousandsunny.service.service.CardCouponReceiveService;
import com.thousandsunny.service.service.CardCouponService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.JsonUtil.valueIsNull;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.CardCouponReceiveState.NOT_RECEIVING;
import static com.thousandsunny.service.ModuleKey.CardCouponType;
import static com.thousandsunny.service.ModuleKey.CardCouponType.CARD;
import static com.thousandsunny.service.ModuleKey.CardCouponType.COUPON;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by xiao.xue.wei on 2017/1/17.
 */
@RestController
@RequestMapping(value = "/api/portal/cardCoupon", produces = APPLICATION_JSON_UTF8_VALUE)
public class CouponAndCardController {

    private final static String[] card_list_info = {
            "id", "name", "discount:cardDiscount", "state", "shop.name:storeName", "shop.area.name:storeAreaName"
    };

    private final static String[] coupon_list_info = {
            "id", "name", "costPrice:couponCostPrice", "salePrice:couponSalePrice",
            "canReceiveManyTimes:couponCanReceiveManyTimes", "shop.name:storeName", "shop.area.name:storeAreaName"
    };

    private final static String[] my_card_list_info = {
            "id:id", "cardCoupon.name:name", "cardCoupon.discount:cardDiscount", "cardCoupon.state:state", "cardCoupon.shop.name:storeName",
            "cardCoupon.shop.area.name:storeAreaName", "state:receiveState",
    };

    private final static String[] my_coupon_list_info = {
            "id:id", "cardCoupon.name:name", "cardCoupon.discount:cardDiscount", "cardCoupon.salePrice:couponSalePrice", "state:receiveState",
            "cardCoupon.canReceiveManyTimes:couponCanReceiveManyTimes", "cardCoupon.shop.name:storeName", "cardCoupon.shop.area.name:storeAreaName"
    };

    private final static String[] normal_json = {"id", "type", "name", "firstImageUrl", "validDate", "discount:cardDiscount", "costPrice:couponCostPrice",
            "salePrice:couponSalePrice", "canReceiveManyTimesBoolean:couponCanReceiveManyTimes", "state", "receiveState",
            "shop.name:storeName", "shop.area.name:storeAreaName", "isRead"};

    private final static String[] card_coupon_info_json = {"cardCoupon.id:id", "cardCoupon.type:type", "cardCoupon.name:name",
            "firstImageUrl", "cardCoupon.validDate:validDate", "cardCoupon.stopTime:pauseDate", "receiveTime:receiveDate",
            "member.token:receiveUserToken", "member.realName:receiveUserName", "cardCoupon.discount:cardDiscount",
            "cardCoupon.costPrice:couponCostPrice", "cardCoupon.salePrice:couponSalePrice", "imageList", "member.mobile:receiveUserPhoneNumber"};


    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private CardCouponService cardCouponService;
    @Autowired
    private CardCouponReceiveService cardCouponReceiveService;
    @Autowired
    private CardCouponLookService cardCouponLookService;

    /**
     * 9.1 发现-卡券列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/cardCouponList", method = GET)
    public ResponseEntity cardCouponPage(String userToken, PageVO pageVO, CardCouponType type, Long provinceId, Long cityId, Long areaId) {
        JSONObject jsonObject = null;
        if (type == CARD) {
            jsonObject = parseCardPage(pageVO, userToken, provinceId, cityId, areaId);
        } else if (type == COUPON) {
            jsonObject = parseCouponPage(pageVO, userToken, provinceId, cityId, areaId);
        }
        return ok(jsonObject);
    }

    /**
     * 解析券列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    private JSONObject parseCouponPage(PageVO pageVO, String memberToken, Long provinceId, Long cityId, Long areaId) {
        Page<CardCoupon> page = cardCouponService.findCardOrCouponPage(pageVO.pageRequest(), provinceId, cityId, areaId, COUPON);
        JSONObject jsonObject = pageToJson(page, cardCoupon -> {
            JSONObject jo = propsFilter(cardCoupon, coupon_list_info);
            jo.put("type", COUPON);
            if (!cardCoupon.getPhotos().isEmpty()) jo.put("firstImageUrl", cardCoupon.getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            if (isNotNull(cardCoupon.getValidDate())) jo.put("validDate", cardCoupon.getValidDate().getTime());
            else jo.put("validDate", null);
            if (isNotBlank(memberToken)) {
                Member member = memberService.findByToken(memberToken);
                jo.put("receiveState", cardCouponReceiveService.viewReceiveStatus(member, cardCoupon));
                jo.put("isRead", cardCouponLookService.judgeIsRead(member, cardCoupon));
            } else valueIsNull(jo, null, "receiveState", "isRead");
            return jo;
        });
        return jsonObject;
    }

    /**
     * 解析卡列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    private JSONObject parseCardPage(PageVO pageVO, String memberToken, Long provinceId, Long cityId, Long areaId) {
        Page<CardCoupon> page = cardCouponService.findCardOrCouponPage(pageVO.pageRequest(), provinceId, cityId, areaId, CARD);
        JSONObject jsonObject = pageToJson(page, cardCoupon -> {
            JSONObject jo = propsFilter(cardCoupon, card_list_info);
            jo.put("type", CARD);
            if (isNotBlank(memberToken)) {
                Member member = memberService.findByToken(memberToken);
                jo.put("receiveState", cardCouponReceiveService.viewReceiveStatus(member, cardCoupon));
                jo.put("isRead", cardCouponLookService.judgeIsRead(member, cardCoupon));
            } else valueIsNull(jo, null, "receiveState", "isRead");
            if (!cardCoupon.getPhotos().isEmpty()) jo.put("firstImageUrl", cardCoupon.getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            if (isNotNull(cardCoupon.getValidDate())) jo.put("validDate", cardCoupon.getValidDate().getTime());
            else jo.put("validDate", null);
            return jo;
        });
        return jsonObject;
    }

    /**
     * 9.2 发现-卡券置为已读
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/toRead", method = POST)
    public ResponseEntity setToRead(String userToken, CardCouponType type, Long id) {
        Member member = memberService.findByToken(userToken);
        cardCouponLookService.setToRead(member, id);
        return OK;
    }

    /**
     * 9.3 我的-卡券列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/myCardAndCoupon", method = GET)
    public ResponseEntity myCardAndCoupon(String userToken, CardCouponType type, String isGetNormalState) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        JSONObject jsonObject = null;
        if (type == CARD) {
            jsonObject = parseMyCardPage(member, isGetNormalState);
        } else if (type == COUPON) {
            jsonObject = parseMyCouponPage(member, isGetNormalState);
        }
        return ok(jsonObject);
    }

    /**
     * 解析我的券列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    private JSONObject parseMyCouponPage(Member member, String isGetNormalState) {
        List<CardCouponReceive> list = cardCouponReceiveService.findMyCardCouponPage(member, isGetNormalState, COUPON);
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> jsons = simpleMap(list, CardcouponReceive -> {
            JSONObject jo = propsFilter(CardcouponReceive, my_coupon_list_info);
            jo.put("type", COUPON);
            if (isNotNull(CardcouponReceive.getCardCoupon().getValidDate()))
                jo.put("validDate", CardcouponReceive.getCardCoupon().getValidDate().getTime());
            else jo.put("validDate", null);
            if (!CardcouponReceive.getCardCoupon().getPhotos().isEmpty())
                jo.put("firstImageUrl", CardcouponReceive.getCardCoupon().getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            jo.put("isRead", cardCouponLookService.judgeIsRead(member, CardcouponReceive.getCardCoupon()));
            return jo;
        });
        jsonObject.put("list", jsons);
        return jsonObject;
    }

    /**
     * 解析我的卡列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    private JSONObject parseMyCardPage(Member member, String isGetNormalState) {
        List<CardCouponReceive> list = cardCouponReceiveService.findMyCardCouponPage(member, isGetNormalState, CARD);
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> jsons = simpleMap(list, cardcouponReceive -> {
            JSONObject jo = propsFilter(cardcouponReceive, my_card_list_info);
            jo.put("type", CARD);
            if (isNotNull(cardcouponReceive.getCardCoupon().getValidDate()))
                jo.put("validDate", cardcouponReceive.getCardCoupon().getValidDate().getTime());
            else jo.put("validDate", null);
            if (!cardcouponReceive.getCardCoupon().getPhotos().isEmpty())
                jo.put("firstImageUrl", cardcouponReceive.getCardCoupon().getPhotos().get(0).getPath());
            else jo.put("firstImageUrl", null);
            jo.put("isRead", cardCouponLookService.judgeIsRead(member, cardcouponReceive.getCardCoupon()));
            return jo;
        });
        jsonObject.put("list", jsons);
        return jsonObject;
    }

    /**
     * 9.4店铺-全部卡券列表
     *
     * @Author mu.jie
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/normal", method = GET)
    public ResponseEntity normalCardCoupon(String userToken, Long id, PageVO pageVO) {
        Page<CardCoupon> page = cardCouponService.findNormalCardCoupon(id, pageVO);
        JSONObject body = parseNormalCardCoupon(userToken, page, true);
        return ok(body);
    }

    private JSONObject parseNormalCardCoupon(String userToken, Page<CardCoupon> page, Boolean flag) {
        Member member = memberService.findByToken(userToken);
        return pageToJson(page, x -> {
            JSONObject jo = propsFilter(x, normal_json);
            ifNotNullThen(x.getValidDate(), d -> jo.replace("validDate", d.getTime()));
            ifTrueThen(x.getPhotos() != null && x.getPhotos().size() > 0, () -> jo.replace("firstImageUrl", x.getPhotos().get(0).getPath()));
            if (flag) {
                jo.replace("receiveState", cardCouponReceiveService.viewReceiveStatus(member, x));
                jo.replace("isRead", cardCouponLookService.judgeIsRead(member, x));
            } else {
                jo.put("isReceived", cardCouponService.isReceived(x));
            }
            jo.replace("storeAreaName", parseShopAreaName(x));
            return jo;
        });
    }

    private String parseShopAreaName(CardCoupon x) {
        String areaName = "全国";
        if (isNotNull(x.getShop()) && isNotNull(x.getShop().getArea())) {
            areaName = x.getShop().getArea().getName();
        } else if (isNotNull(x.getShop()) && isNotNull(x.getShop().getCity())) {
            areaName = x.getShop().getCity().getName();
        } else if (isNotNull(x.getShop()) && isNotNull(x.getShop().getProvince())) {
            areaName = x.getShop().getProvince().getName();
        }
        return areaName;
    }

    /**
     * 9.5店铺-我发布的卡券列表
     *
     * @Author mu.jie
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/myNormal", method = GET)
    public ResponseEntity findMyNormalCardCoupon(String userToken, Long id, PageVO pageVO) {
        Page<CardCoupon> page = cardCouponService.findMyNormalCardCoupon(userToken, id, pageVO);
        JSONObject body = parseNormalCardCoupon(userToken, page, false);
        return ok(body);
    }

    /**
     * 9.6店铺-卡券刷新
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/refresh", method = POST)
    public ResponseEntity refreshTime(String userToken, CardCouponType type, Long id) {
        Member member = memberService.findByToken(userToken);
        cardCouponService.refresh(member, id);
        return OK;
    }

    /**
     * 9.7店铺-卡券暂停/恢复
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/pause", method = POST)
    public ResponseEntity pause(String userToken, CardCouponType type, Long id, OperatorType operatorType) {
        Member member = memberService.findByToken(userToken);
        cardCouponService.pause(member, id, operatorType);
        return OK;
    }

    /**
     * 9.8店铺-卡券删除
     *
     * @Author mu.jie
     * @Date 2017/2/3
     */
    @RequestMapping(value = "/del", method = DELETE)
    public ResponseEntity delete(String userToken, CardCouponType type, Long id) {
        Member member = memberService.findByToken(userToken);
        cardCouponService.deleteCard(member, id);
        return OK;
    }

    /**
     * 9.9 店铺-新增/编辑卡信息
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/card", method = POST)
    public ResponseEntity addOrEditCard(CardCoupon card, String userToken) {
        String[] ADD_CARD_INFO = {"id", "type", "name", "validDate", "discount:cardDiscount", "state", "shop.name:storeName",
                "shop.area.name:storeName",};
        Member member = memberService.findByToken(userToken);
        CardCoupon cardCoupon = cardCouponService.addOrEditCard(userToken, card);
        JSONObject jsonObject = propsFilter(cardCoupon, ADD_CARD_INFO);
        jsonObject.put("firstImageUrl", cardCoupon.getPhotos().get(0).getPath());
        CardCouponReceive cardCouponReceive = cardCouponService.findCardCouponReceive(member, cardCoupon.getId());
        if (isNotNull(cardCouponReceive)) jsonObject.put("receiveState", cardCouponReceive.getState());
        else jsonObject.put("receiveState", NOT_RECEIVING);
        jsonObject.put("isRead", cardCouponLookService.judgeIsRead(member, cardCoupon));
        return ok(jsonObject);
    }

    /**
     * 9.10店铺-新增/编辑券信息
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/coupon", method = POST)
    public ResponseEntity addOrEditCoupon(CardCoupon coupon, String userToken) {
        String[] ADD_COUPON_INFO = {"id", "type", "name", "validDate", "costPrice:couponCostPrice", "salePrice:couponSalePrice",
                "canReceiveManyTimes:couponCanReceiveManyTimes", "state", "shop.name:storeName", "shop.area.name:storeName",};
        Member member = memberService.findByToken(userToken);
        CardCoupon cardCoupon = cardCouponService.addOrEditCoupon(userToken, coupon);
        JSONObject jsonObject = propsFilter(cardCoupon, ADD_COUPON_INFO);
        CardCouponReceive cardCouponReceive = cardCouponService.findCardCouponReceive(member, cardCoupon.getId());
        if (isNotNull(cardCouponReceive)) jsonObject.put("receiveState", cardCouponReceive.getState());
        else jsonObject.put("receiveState", NOT_RECEIVING);
        jsonObject.put("isRead", cardCouponLookService.judgeIsRead(member, cardCoupon));
        return ok(jsonObject);
    }

    /**
     * 9.11 店铺卡券详情
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/cardCouponInfo", method = GET)
    public ResponseEntity cardCouponInfo(String userToken, CardCouponType type, Long id) {
        Member member = memberService.findByToken(userToken);
        JSONObject body = null;
        CardCoupon cardCoupon = cardCouponService.findOne(id);
        if (type == CARD) {
            body = parseCardInfo(member, cardCoupon, null, null, null);
        } else if (type == COUPON) {
            body = parseCouponInfo(member, cardCoupon, null, null, null);
        }
        CardCouponReceiveState state = cardCouponReceiveService.viewReceiveStatus(member, cardCoupon);
        if (state == NOT_RECEIVING) {
            body.put("isReceived", false);
        } else {
            body.put("isReceived", true);
        }
        return ok(body);
    }

    private JSONObject parseCardInfo(Member member, CardCoupon cardCoupon, CardCouponReceiveState state, Date receiveDate, String cardBarCode) {
        String[] card_info = {"id", "type", "name", "firstImageUrl", "validDate", "discount:cardDiscount", "couponCostPrice", "couponSalePrice",
                "couponCanReceiveManyTimes", "state", "receiveState", "shop.name:storeName", "shop.area.name:storeAreaName", "details", "useNotice", "imageList",
                "shop.id:storeId", "shop.longitude:storeLng", "shop.latitude:storeLat", "shop.city.name:storeCityName", "shop.province.name:storeProvinceName",
                "shop.address:storeAddress"};
        JSONObject body = propsFilter(cardCoupon, card_info);
        body.replace("type", CARD);
        ifNotEmptyThen(cardCoupon.getPhotos(), x -> body.replace("firstImageUrl", x.get(0).getPath()));
        ifNotNullThen(cardCoupon.getValidDate(), x -> body.replace("validDate", x.getTime()));
        if (state == null) {
            body.replace("receiveState", cardCouponReceiveService.viewReceiveStatus(member, cardCoupon));
            body.put("pauseDate", cardCoupon.getStopTime());
        } else {
            body.replace("receiveState", state);
            body.put("receiveDate", receiveDate);
            body.put("receiveCardCode", cardBarCode);
        }
        List<JSONObject> imgList = simpleMap(cardCoupon.getPhotos(), x -> propsFilter(x, "path"));
        body.replace("imageList", imgList);
        return body;
    }

    private JSONObject parseCouponInfo(Member member, CardCoupon cardCoupon, CardCouponReceiveState state, Date receiveDate, String cardBarCode) {
        String[] coupon_info = {"id", "type", "name", "firstImageUrl", "validDate", "cardDiscount", "costPrice:couponCostPrice", "salePrice:couponSalePrice",
                "canReceiveManyTimesBoolean:couponCanReceiveManyTimes", "state", "receiveState", "shop.name:storeName", "shop.area.name:storeAreaName",
                "details", "useNotice", "imageList", "shop.id:storeId", "shop.longitude:storeLng", "shop.latitude:storeLat", "shop.city.name:storeCityName",
                "shop.province.name:storeProvinceName", "shop.address:storeAddress"};
        JSONObject body = propsFilter(cardCoupon, coupon_info);
        body.replace("type", COUPON);
        ifNotEmptyThen(cardCoupon.getPhotos(), x -> body.replace("firstImageUrl", x.get(0).getPath()));
        ifNotNullThen(cardCoupon.getValidDate(), x -> body.replace("validDate", x.getTime()));
        if (state == null) {
            body.replace("receiveState", cardCouponReceiveService.viewReceiveStatus(member, cardCoupon));
        } else {
            body.replace("receiveState", state);
            body.put("receiveCardCode", cardBarCode);
            body.put("receiveDate", receiveDate);
        }
        List<JSONObject> imgList = simpleMap(cardCoupon.getPhotos(), x -> propsFilter(x, "path"));
        body.replace("imageList", imgList);
        return body;
    }

    /**
     * 9.12我领取的卡券详情
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    @RequestMapping(value = "/myCardCouponInfo", method = GET)
    public ResponseEntity myCardCouponInfo(String userToken, CardCouponType type, Long id) {
        Member member = memberService.findByToken(userToken);
        CardCouponReceive cardCouponReceive = cardCouponReceiveService.findOne(id);
        JSONObject body = null;
        if (type == CARD) {
            ifNullThrow(cardCouponReceive, TIP_CARD_NOT_EXIST);
            body = parseCardInfo(member, cardCouponReceive.getCardCoupon(), cardCouponReceive.getState(), cardCouponReceive.getReceiveTime(), cardCouponReceive.getCardCouponCode());
        } else if (type == COUPON) {
            ifNullThrow(cardCouponReceive, TIP_COUPON_NOT_EXIST);
            body = parseCouponInfo(member, cardCouponReceive.getCardCoupon(), cardCouponReceive.getState(), cardCouponReceive.getReceiveTime(), cardCouponReceive.getCardCouponCode());
        }
        return ok(body);
    }

    /**
     * 9.13 领取卡劵
     *
     * @Author mu.jie
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/receive", method = POST)
    public ResponseEntity receiveCardCoupon(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        CardCoupon cardCoupon = cardCouponService.findOne(id);
        cardCouponReceiveService.createCardCouponReceive(member, cardCoupon);
        return OK;
    }

    /**
     * 9.14店铺验证卡券
     *
     * @Author mu.jie
     * @Date 2017/2/15
     */
    @RequestMapping(value = "/verify", method = POST)
    public ResponseEntity verifyCard(String storeManagerUserToken, String useCardUserToken, String cardCode) {
        Member shopManager = memberService.findByToken(storeManagerUserToken);
        Member member = memberService.findByToken(useCardUserToken);
        cardCouponReceiveService.verify(shopManager, member, cardCode);
        return OK;
    }

    /**
     * 9.15店铺验证卡券-查询卡券信息
     *
     * @Author mu.jie
     * @Date 2017/2/16
     */
    @RequestMapping(value = "/info", method = GET)
    public ResponseEntity cardCouponInfo(String storeManagerUserToken, String cardCode) {
        Member shopManager = memberService.findByToken(storeManagerUserToken);
        CardCouponReceive cardCouponReceive = cardCouponReceiveService.storeManagerLookCardCoupon(shopManager, cardCode);
        JSONObject body = propsFilter(cardCouponReceive, card_coupon_info_json);
        CardCoupon cardCoupon = cardCouponReceive.getCardCoupon();
        ifNotEmptyThen(cardCoupon.getPhotos(), x -> body.replace("firstImageUrl", x.get(0).getPath()));
        List<JSONObject> imgList = simpleMap(cardCoupon.getPhotos(), x -> propsFilter(x, "path"));
        body.replace("imageList", imgList);
        return ok(body);
    }

}
