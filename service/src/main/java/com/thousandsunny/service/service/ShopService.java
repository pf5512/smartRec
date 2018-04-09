package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.CloudFileService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.model.ShopCollect;
import com.thousandsunny.service.repository.ShopRepository;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;

/**
 * Created by admin on 2016/10/24.
 */
@Service
public class ShopService extends BaseService<Shop> {
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private CloudFileService cloudFileService;
    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private ShopCollectService shopCollectService;

    public Shop findByOwnerId(Long id) {
        Shop shop = shopRepository.findByOwnerId(id);
        return shop;
    }

    public Shop getOne(Long id) {
        Shop shop = shopRepository.findByIdAndIsDelete(id, NO);
        ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
        return shop;
    }

    public Shop saveCascade(Shop shop) {
        Shop oldShop = shopRepository.findByOwnerId(shop.getOwner().getId());
        ifTrueThrow(oldShop != null && shop.getId() == null, TIP_SHOP_EXIST);
        CloudFile logo = cloudFileService.save(shop.getLogo());
        List<CloudFile> photos = shop.getPhotos();
        ifNotEmptyThen(photos, s -> shop.setPhotos(cloudFileService.save(s)));
        if (shop.getId() == null) shop.setDate(new Date());
        shop.setLogo(logo);
        return save(shop);
    }

    public Shop getByOwner(String userToken) {
        return shopRepository.findByOwnerToken(userToken);
    }

    public Shop collectShop(String userToken, Long id, OperatorType type) {
        Member member = memberService.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        ShopCollect shopCollect = shopCollectService.findByMemberTokenAndShopId(userToken, id);
        Shop shop = shopRepository.findOne(id);
        ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
        if (type == SURE) {
            ifNotNullThrow(shopCollect, TIP_COLLECT);
            shopCollect = new ShopCollect();
            shopCollect.setMember(member);
            shopCollect.setShop(shop);
            shopCollectService.save(shopCollect);
        } else {
            ifNullThrow(shopCollect, TIP_NOT_COLLECT);
            shopCollectService.delete(shopCollect);
        }
        return shop;
    }


    public Integer getShopNumber(Long id) {
        return shopRepository.countByAreaId(id);
    }

    public Boolean isCollected(Shop shop, String userToken) {
        return shopCollectService.findByMemberTokenAndShopIdAndCollectEver(userToken, shop.getId(), NO) != null;
    }


    //以下为管理端的方法


    public Page<Shop> listShops(Pageable pageable, String text, Long province, Long city, Long area, String partnerStatus, String transferStatus) {
        Specification<Shop> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("name"), "%" + t + "%"),
                    rb.like(rt.get("owner").get("realName"), "%" + t + "%"), rb.like(rt.get("owner").get("hpAccount"), "%" + t + "%"))));
            ifNotNullThen(province, t -> predicates.add(rb.equal(rt.get("province").get("id"), t)));
            ifNotNullThen(city, t -> predicates.add(rb.equal(rt.get("city").get("id"), t)));
            ifNotNullThen(area, t -> predicates.add(rb.equal(rt.get("area").get("id"), t)));
            ifNotBlankThen(transferStatus, t -> predicates.add(rb.equal(rt.get("isTransfer").get("code"), t)));
            ifNotBlankThen(partnerStatus, t -> predicates.add(rb.equal(rt.get("findHelp").get("code"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return shopRepository.findAll(specification, pageable);
    }


    public void shopDelete(Long id) {
        Shop shop = shopRepository.findOne(id);
        shop.setIsDelete(YES);
        shopRepository.save(shop);
    }

    public void enabled(Long id) {
        Shop shop = shopRepository.findOne(id);
        shop.setState(shop.getState() == YES ? NO : YES);
        shopRepository.save(shop);

    }

    public Page<Shop> findByAreaIdAndStateOrderByDateDesc(Long areaId, BooleanEnum yes, Pageable pageable) {
        return shopRepository.findByAreaIdAndStateOrderByDateDesc(areaId, yes, pageable);
    }
}