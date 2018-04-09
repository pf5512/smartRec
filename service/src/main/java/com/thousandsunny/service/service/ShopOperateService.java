package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.model.ShopOperate;
import com.thousandsunny.service.model.ShopOperateLook;
import com.thousandsunny.service.repository.ShopOperateLookRepository;
import com.thousandsunny.service.repository.ShopOperateRepository;
import com.thousandsunny.service.repository.ShopRepository;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.OperateType.COOPERATE;
import static com.thousandsunny.service.ModuleKey.OperateType.TRANSFER;
import static com.thousandsunny.service.ModuleTips.*;

/**
 * Created by 13336 on 2017/1/13.
 */
@Service
public class ShopOperateService extends BaseService<ShopOperate> {
    @Autowired
    private ShopOperateRepository shopOperateRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ShopOperateLookRepository shopOperateLookRepository;

    /**
     * 编辑店铺合作信息
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public ShopOperate editCooperate(Long shopId, BigDecimal area, BigDecimal rent, String explain) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(COOPERATE, shopId);
        if (!isNotNull(shopOperate)) {
            shopOperate = new ShopOperate();
            Shop shop = shopRepository.findOne(shopId);
            shopOperate.setShop(shop);
            shopOperate.setType(COOPERATE);
        }
        ShopOperate shopTransfer = shopOperateRepository.findByTypeAndShopId(TRANSFER, shopId);
        ifNotNullThen(shopTransfer, e -> {
            e.setShopSquare(area);
            e.setShopRental(rent);
            save(e);
        });
        shopOperate.setShopSquare(area);
        shopOperate.setShopRental(rent);
        shopOperate.setRemark(explain);
        save(shopOperate);
        return shopOperate;
    }

    /**
     * 刷新店铺合作
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public void refreshCooperate(Long shopId) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(COOPERATE, shopId);
        ifNullThrow(shopOperate, TIP_NO_SHOP_COOPERATE);
        Date date = new Date();
        ifTrueThrow(date.getTime() - shopOperate.getFreshTime().getTime() < 24 * 3600 * 1000, TIP_ONE_DAY_ONE_TIMES);
        shopOperate.setFreshTime(date);
        save(shopOperate);
    }

    /**
     * 开启/关闭 店铺合作
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public void openOrCloseCooperate(Long shopId, String operatorType) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(COOPERATE, shopId);
        ifNullThrow(shopOperate, TIP_NO_SHOP_COOPERATE);
        if ("SURE".equals(operatorType))
            shopOperate.setIsOpen(YES);
        else if ("CANCEL".equals(operatorType))
            shopOperate.setIsOpen(NO);
        save(shopOperate);
    }

    /**
     * 店铺合作列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public Page<ShopOperate> findCooperatePages(Pageable pageable, Long provinceId, Long cityId, Long areaId) {
        Specification<ShopOperate> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("type"), COOPERATE));
            predicates.add(rb.equal(rt.get("isOpen"), YES));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("shop").get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("shop").get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("shop").get("area").get("id"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("freshTime"), false)).getRestriction();
        };
        return shopOperateRepository.findAll(specification, pageable);
    }

    /**
     * 判断用户是否已读
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public boolean judgeIsRead(String token, Long id) {
        Long readNum = shopOperateLookRepository.countByMemberTokenAndShopOperateIdAndIsRead(token, id, YES);
        if (isNotNull(readNum)) return true;
        else return false;
    }

    /**
     * 判断是否申请店铺转让
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    public boolean judgeIsTransfer(Long shopId) {
        Long transferNum = shopOperateRepository.countByShopIdAndTypeAndIsOpen(shopId, TRANSFER, YES);
        if (isNotNull(transferNum) && transferNum > 0) return true;
        else return false;
    }

    /**
     * 判断是否申请店铺合作
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    public boolean judgeIsCooperate(Long shopId) {
        Long transferNum = shopOperateRepository.countByShopIdAndTypeAndIsOpen(shopId, COOPERATE, YES);
        if (isNotNull(transferNum) && transferNum > 0) return true;
        else return false;
    }

    /**
     * 店铺合作/转让置为已读
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    public String setToRead(Member member, Long shopOperateId) {
        ShopOperateLook shopOperateLook = shopOperateLookRepository.findByMemberTokenAndShopOperateId(member.getToken(), shopOperateId);
        if (isNotNull(shopOperateLook)) shopOperateLook.setIsRead(YES);
        else {
            shopOperateLook = new ShopOperateLook();
            shopOperateLook.setShopOperate(shopOperateRepository.findOne(shopOperateId));
            shopOperateLook.setMember(member);
            shopOperateLook.setIsRead(YES);
        }
        shopOperateLookRepository.save(shopOperateLook);
        return "success";
    }

    /**
     * 编辑店铺转让信息
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    public ShopOperate editTransfer(Long shopId, BigDecimal area, BigDecimal rent, String explain) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(TRANSFER, shopId);
        if (!isNotNull(shopOperate)) {
            shopOperate = new ShopOperate();
            Shop shop = shopRepository.findOne(shopId);
            shopOperate.setShop(shop);
            shopOperate.setType(TRANSFER);
        }
        shopOperate.setShopSquare(area);
        shopOperate.setShopRental(rent);
        ShopOperate shopCooperate = shopOperateRepository.findByTypeAndShopId(COOPERATE, shopId);
        ifNotNullThen(shopCooperate, e -> {
            shopCooperate.setShopSquare(area);
            shopCooperate.setShopRental(rent);
            save(shopCooperate);
        });
        shopOperate.setRemark(explain);
        save(shopOperate);
        return shopOperate;
    }

    /**
     * 刷新店铺转让
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    public void refreshTransfer(Long shopId) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(TRANSFER, shopId);
        ifNullThrow(shopOperate, TIP_NO_SHOP_TRANSFER);
        Date now = new Date();
        ifTrueThrow(now.getTime() - shopOperate.getFreshTime().getTime() < 24 * 3600 * 1000, TIP_ONE_DAY_ONE_TIMES);
        shopOperate.setFreshTime(now);
        save(shopOperate);
    }

    /**
     * 开启/关闭 店铺转让
     *
     * @Author xiao xue wei
     * @Date 2017/1/16
     */
    public void openOrCloseTransfer(Long shopId, String operatorType) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(TRANSFER, shopId);
        ifNullThrow(shopOperate, TIP_NO_SHOP_TRANSFER);
        if ("SURE".equals(operatorType))
            shopOperate.setIsOpen(YES);
        else if ("CANCEL".equals(operatorType))
            shopOperate.setIsOpen(NO);
        save(shopOperate);
    }

    /**
     * 店铺转让列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/13
     */
    public Page<ShopOperate> findTransferPages(Pageable pageable, Long provinceId, Long cityId, Long areaId) {
        Specification<ShopOperate> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("type"), TRANSFER));
            predicates.add(rb.equal(rt.get("isOpen"), YES));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("shop").get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("shop").get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("shop").get("area").get("id"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("freshTime"), false)).getRestriction();
        };
        return shopOperateRepository.findAll(specification, pageable);
    }

    public JSONObject getCooperateInfo(Long id) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(COOPERATE, id);
        if(isNotNull(shopOperate)){
            JSONObject jo = new JSONObject();
            jo.put("area", shopOperate.getShopSquare());
            jo.put("rent", shopOperate.getShopRental());
            jo.put("explain", shopOperate.getRemark());
            jo.put("isOpen", shopOperate.getIsOpenBoolean());
            return jo;
        }else return null;
    }

    public JSONObject getTransferInfo(Long id) {
        ShopOperate shopOperate = shopOperateRepository.findByTypeAndShopId(TRANSFER, id);
        if(isNotNull(shopOperate)){
            JSONObject jo = new JSONObject();
            jo.put("area", shopOperate.getShopSquare());
            jo.put("rent", shopOperate.getShopRental());
            jo.put("explain", shopOperate.getRemark());
            jo.put("isOpen", shopOperate.getIsOpenBoolean());
            return jo;
        }else return null;
    }

    public ShopOperate findShopCooperationInfo(Shop shop) {
        return shopOperateRepository.findByTypeAndShopId(COOPERATE, shop.getId());
    }

    public ShopOperate findShopTransferInfo(Shop shop) {
        return shopOperateRepository.findByTypeAndShopId(TRANSFER, shop.getId());
    }
}
