package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Job;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.model.TransferRecord;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.NORMAL;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * 如果这些代码有用，那它们是guitarist在02/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/portal/shops", produces = APPLICATION_JSON_UTF8_VALUE)
public class ShopController {

    private static final String[] Shop_Model = {
            "id",
            "name",
            "province.id:provinceId",
            "province.name:provinceName",
            "city.id:cityId",
            "city.name:cityName",
            "area.id:areaId",
            "area.name:areaName",
            "address",
            "longitude:lng",
            "latitude:lat",
            "brightSpots:highlights",
            "logo.path:logo",
            "ownerPosition:position"
    };
    private static final String[] Shop_Detail_Model = {
            "id",
            "name",
            "province.id:provinceId",
            "province.name:provinceName",
            "city.id:cityId",
            "city.name:cityName",
            "area.id:areaId",
            "area.name:areaName",
            "address",
            "longitude:lng",
            "latitude:lat",
            "brightSpots:highlights",
            "logo.path:logo",
            "ownerPosition:position",
            "owner.realName:managerRealName",
            "owner.token:managerUserToken",
            "owner.headImage.path:managerHeaderImageUrl",
            "owner.mobile:managerPhoneNumber",
            "isNoDisturbBool:isManagerPhoneNumberNoDisturb",
            "ownerPosition:managerPosition"
    };
    @Autowired
    private ShopService shopService;
    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private JobService jobService;
    @Autowired
    private TransferRecordService transferRecordService;
    @Autowired
    private OperationService operationService;
    @Autowired
    private ShopOperateService shopOperateService;
    @Autowired
    private CardCouponService cardCouponService;

    /**
     * 店铺详情
     */
    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity get(@PathVariable Long id, String userToken) {
        return ok(shopDetailWrapper(shopService.getOne(id), userToken));
    }

    /**
     * 我的店铺
     */
    @RequestMapping(value = "/owner/{token}", method = GET)
    public ResponseEntity getByOwner(@PathVariable String token) {
        return ok(shopWrapper(shopService.getByOwner(token)));
    }

    /**
     * 收藏我的店铺
     */
    @RequestMapping(value = "/collecting", method = POST)
    public ResponseEntity collectShop(String userToken, Long id, OperatorType type) {
        shopService.collectShop(userToken, id, type);
        return OK;
    }

    /**
     * 店铺相册
     */
    @RequestMapping(value = "/photos/{id}", method = GET)
    public ResponseEntity photos(@PathVariable Long id) {
        Shop shop = shopService.findOne(id);
        List<JSONObject> list = simpleMap(shop.getPhotos(), photo -> propsFilter(photo, "title", "path"));
        list.add(0, propsFilter(shop.getLogo(), "title", "path"));
        return ok(listToJson(list));
    }

    /**
     * 管理权转让
     */
    @RequestMapping(value = "/transferRecord", method = POST)
    public ResponseEntity owner(String userToken, TransferRecord transferRecord) {
        Member member = memberService.findByToken(userToken);
        Shop shop = shopService.findByOwnerId(member.getId());
        ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
        Member receiver = memberService.findByMobile(transferRecord.getReceiverPhoneNumber());
        ifNullThrow(receiver, TIP_NO_MEMBER);
        ifFalseThrow(receiver.getRealName().equals(transferRecord.getReceiverRealName().trim()), TIP_INFORMATION_IS_WRONG);
        ifFalseThrow(isNotBlank(transferRecord.getReceiverHpAccount()) && transferRecord.getReceiverHpAccount().equals(receiver.getHpAccount()), TIP_INFORMATION_IS_WRONG);
        transferRecord.setAssignor(member);
        transferRecordService.createNewRecord(transferRecord);
        return OK;
    }

    private JSONObject shopWrapper(Shop shop) {
        JSONObject body = propsFilter(shop, Shop_Model);
        body.put("albumList", simpleMap(shop.getPhotos(), photo -> propsFilter(photo, "path", "title")));
        body.put("jobCount", jobService.countByShop(shop));
        body.put("cardAndTicketCount", cardCouponService.countShopCardCoupons(shop));
        body.put("cooperateInfo", shopOperateService.getCooperateInfo(shop.getId()));
        body.put("transferInfo", shopOperateService.getTransferInfo(shop.getId()));

        return body;
    }

    private JSONObject shopDetailWrapper(Shop shop, String userToken) {
        ifTrueThrow(shop == null, TIP_NO_CHANSHUERROR);
        JSONObject body = propsFilter(shop, Shop_Detail_Model);
        List<Job> jobs = jobService.findByShop(shop);

        body.put("isCollect", shopService.isCollected(shop, userToken));
        body.put("jobList", simpleFilterMap(jobs, o -> o.getState() == NORMAL, o -> propsFilter(o, "id", "name")));
        Integer cardAndTicketCount = cardCouponService.countShopCardCoupons(shop);
        body.put("cardAndTicketCount", cardAndTicketCount);
        body.put("cooperateInfo", shopOperateService.getCooperateInfo(shop.getId()));
        body.put("transferInfo", shopOperateService.getTransferInfo(shop.getId()));

        return body;
    }

    /**
     * 添加/编辑店铺
     */
    @RequestMapping(method = POST)
    public ResponseEntity list(String userToken, Shop shop) {
        shop.setOwner(memberService.findByToken(userToken));
        shop.setFindHelp(operationService.findOperation("no_open"));
        shop.setIsTransfer(operationService.findOperation("no_open"));
        Shop s = shopService.saveCascade(shop);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", s.getId());
        return ok(jsonObject);
    }

}
