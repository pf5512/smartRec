package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.ShopOperate;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.service.service.ShopOperateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.common.lambda.LambdaUtil.pageToJson;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by 13336 on 2017/1/13.
 */
@RestController
@RequestMapping(value = "/api/portal/shopOperate", produces = APPLICATION_JSON_UTF8_VALUE)
public class ShopOperateController {
    private String[] shop_cooperate_info = {
            "id", "shop.id:storeId", "shop.name:name", "shop.address:address", "shop.logo.path:logo", "shop.province.name:provinceName",
            "shop.city.name:cityName", "shop.area.name:areaName"
    };

    @Autowired
    private ShopOperateService shopOperateService;
    @Autowired
    private MemberService memberService;

    /**
     * 10.1 编辑店铺合作信息
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    @RequestMapping(value = "/editCooperate", method = POST)
    public ResponseEntity editCooperate(String userToken, Long id, BigDecimal area, BigDecimal rent, String explain) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.editCooperate(id, area, rent, explain);
        return OK;
    }

    /**
     * 10.2 刷新店铺合作
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    @RequestMapping(value = "/refreshCooperate", method = POST)
    public ResponseEntity refreshCooperate(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.refreshCooperate(id);
        return OK;
    }

    /**
     * 10.3 开启/关闭 店铺合作
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    @RequestMapping(value = "/switchCooperate", method = POST)
    public ResponseEntity switchCooperate(String userToken, Long id, String operatorType) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.openOrCloseCooperate(id, operatorType);
        return OK;
    }

    /**
     * 10.4 店铺合作列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    @RequestMapping(value = "/cooperateList", method = GET)
    public ResponseEntity cooperateList(PageVO pageVo, String userToken, Long provinceId, Long cityId, Long areaId) {
        Page<ShopOperate> pages = shopOperateService.findCooperatePages(pageVo.pageRequest(), provinceId, cityId, areaId);
        JSONObject jsonObject = pageToJson(pages, e -> {
            JSONObject jo = propsFilter(e, shop_cooperate_info);
            if (e.getIsOpen() == YES) jo.put("isCooperate", true);
            else jo.put("isCooperate", false);
            if (isNotBlank(userToken)) {
                Member member = memberService.findByToken(userToken);
                jo.put("isRead", shopOperateService.judgeIsRead(member.getToken(), e.getId()));
            } else jo.put("isRead", null);
            jo.put("isTransfer", shopOperateService.judgeIsTransfer(e.getShop().getId()));
            return jo;
        });
        return ok(jsonObject);
    }

    /**
     * 10.5/11.5 店铺合作/转让置为已读
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    @RequestMapping(value = "/setToRead", method = POST)
    public ResponseEntity setToRead(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.setToRead(member, id);
        return OK;
    }

    /**
     * 11.1 编辑店铺转让信息
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    @RequestMapping(value = "/editTransfer", method = POST)
    public ResponseEntity editTransfer(String userToken, Long id, BigDecimal area, BigDecimal rent, String explain) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.editTransfer(id, area, rent, explain);
        return OK;
    }

    /**
     * 11.2 刷新店铺转让
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    @RequestMapping(value = "/refreshTransfer", method = POST)
    public ResponseEntity refreshTransfer(String userToken, Long id) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.refreshTransfer(id);
        return OK;
    }

    /**
     * 11.3 开启/关闭 店铺转让
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    @RequestMapping(value = "/switchTransfer", method = POST)
    public ResponseEntity switchTransfer(String userToken, Long id, String operatorType) {
        Member member = memberService.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        shopOperateService.openOrCloseTransfer(id, operatorType);
        return OK;
    }

    /**
     * 11.4 店铺转让列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    @RequestMapping(value = "/transferList", method = GET)
    public ResponseEntity transferList(PageVO pageVo, String userToken, Long provinceId, Long cityId, Long areaId) {
        Page<ShopOperate> pages = shopOperateService.findTransferPages(pageVo.pageRequest(), provinceId, cityId, areaId);
        JSONObject jsonObject = pageToJson(pages, e -> {
            JSONObject jo = propsFilter(e, shop_cooperate_info);
            if (e.getIsOpen() == YES) jo.put("isTransfer", true);
            else jo.put("isTransfer", false);
            if (isNotBlank(userToken)) {
                Member member = memberService.findByToken(userToken);
                jo.put("isRead", shopOperateService.judgeIsRead(member.getToken(), e.getId()));
            } else jo.put("isRead", null);
            jo.put("isCooperate", shopOperateService.judgeIsCooperate(e.getShop().getId()));
            return jo;
        });
        return ok(jsonObject);
    }
}
