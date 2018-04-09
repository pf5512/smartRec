package com.thousandsunny.service.service;

import com.thousandsunny.cms.domain.repository.ArticleRepository;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.AdCategoryEnum;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.AdCategoryEnum.AD_APP_START;
import static com.thousandsunny.service.ModuleKey.AdCategoryEnum.AD_INDEX;
import static org.thymeleaf.util.ListUtils.isEmpty;

/**
 * Created by mu.jie on 2016/11/23.
 */
@Service
public class AdvertisementService extends BaseService<Advertisement> {

    @Autowired
    private AdvertisementRepository advertisementRepository;
    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;

    public Page<Advertisement> findAdList(BackPageVo backPageVo, String text, Long provinceId, Long cityId, Long areaId, AdCategoryEnum adType, Date startTime, Date endTime) {
        Specification<Advertisement> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (provinceId != null && provinceId == 0L) {
                predicates.add(rb.isNull(rt.get("province").get("id")));
                predicates.add(rb.isNull(rt.get("city").get("id")));
                predicates.add(rb.isNull(rt.get("area").get("id")));
            } else {
                ifNotNullThen(provinceId, t -> predicates.add(rb.equal(rt.get("province").get("id"), t)));
                ifNotNullThen(cityId, t -> predicates.add(rb.equal(rt.get("city").get("id"), t)));
                ifNotNullThen(areaId, t -> predicates.add(rb.equal(rt.get("area").get("id"), t)));
            }
            ifNotNullThen(adType, t -> predicates.add(rb.equal(rt.get("category"), t)));
            ifNotBlankThen(text, t -> predicates.add(rb.like(rt.get("name"), "%" + t + "%")));
            ifNotNullThen(startTime, t -> {
                predicates.add(rb.greaterThan(rt.get("endTime"), t));
                predicates.add(rb.greaterThan(rt.get("startTime"), t));

            });
            ifNotNullThen(endTime, t -> {
                predicates.add(rb.lessThan(rt.get("endTime"), t));
                predicates.add(rb.lessThan(rt.get("startTime"), t));
            });
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("weight"), false)).getRestriction();
        };
        return advertisementRepository.findAll(spec, backPageVo.pageRequest());
    }

    public void delAd(String ids) {
        ifNotBlankThen(ids, x -> newArrayList(ids.split(",")).forEach(idStr -> {
            Advertisement ad = advertisementRepository.findOne(Long.parseLong(idStr));
            ad.setIsDelete(YES);
            advertisementRepository.save(ad);
        }));
    }

    public void updateAdList(Advertisement ad, Long no, String img) {
        Advertisement oldAd;
        if (ad.getId() == null) {
            oldAd = advertisementRepository.save(ad);
            Long maxWeight = advertisementRepository.findMaxWeight();
            Long weight = maxWeight == null || maxWeight == 0L ? 1L : (((maxWeight + 10) / 10) * 10);
            oldAd.setWeight(weight);
        } else {
            oldAd = advertisementRepository.findOne(ad.getId());
            ifNotNullThen(ad.getCategory(), x -> oldAd.setCategory(x));
            ifNotNullThen(ad.getName(), x -> oldAd.setName(x));
            ifNotNullThen(ad.getStatus(), x -> oldAd.setStatus(x));
            ifNotNullThen(ad.getType(), x -> oldAd.setType(x));
            ifNotNullThen(ad.getShowWay(), x -> oldAd.setShowWay(x));
            ifNotNullThen(ad.getLink(), x -> oldAd.setLink(x));
            ifNotNullThen(ad.getProvince(), x -> oldAd.setProvince(x));
            ifNotNullThen(ad.getCity(), x -> oldAd.setCity(x));
            ifNotNullThen(ad.getArea(), x -> oldAd.setArea(x));
            ifNotNullThen(ad.getWeight(), x -> oldAd.setWeight(x));
            ifNotNullThen(ad.getStartTime(), x -> oldAd.setStartTime(x));
            ifNotNullThen(ad.getEndTime(), x -> oldAd.setEndTime(x));
            ifNotNullThen(ad.getValid(), x -> oldAd.setValid(x));
            ifNullThen(ad.getValid(), () -> oldAd.setValid(NO));
        }
        ifTrueThen(isNotNull(no) && isNotNull(ad.getType()), () -> {
            switch (ad.getType()) {
                case AD_VIP:
                    Member member = memberService.findOne(no);
                    oldAd.setMember(member);
                    break;
                case AD_SHOP:
                    Shop shop = shopRepository.findOne(no);
                    oldAd.setShop(shop);
                    break;
                case AD_JOB:
                    Job job = jobRepository.findOne(no);
                    oldAd.setJob(job);
                    break;
                case AD_COUPON:
                    break;
                case AD_ARTICLE:
                    Article article = articleRepository.findOne(no);
                    oldAd.setArticle(article);
                    break;
                case AD_VIDEO:
                    Video video = videoRepository.findOne(no);
                    oldAd.setVideo(video);
                    break;
                case AD_SCHOOL:
                    School school = schoolRepository.findOne(no);
                    oldAd.setSchool(school);
                    break;
                case AD_COURSE:
                    Course course = courseRepository.findOne(no);
                    oldAd.setCourse(course);
                    break;
            }
        });
        ifNotBlankThen(img, x -> {
            CloudFile cloudFile = new CloudFile();
            cloudFile.setPath(img);
            cloudFileRepository.save(cloudFile);
            oldAd.setPic(cloudFile);
        });
        advertisementRepository.save(oldAd);
    }

    public List<Advertisement> findIndexAdvertisementList(Long provinceId, Long cityId, Long areaId) {
        Specification<Advertisement> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.equal(rt.get("category"), AD_INDEX));
            predicates.add(rb.equal(rt.get("valid"), YES));
            Date nowDate = new Date();
            predicates.add(rb.lessThanOrEqualTo(rt.get("startTime"), nowDate));
            predicates.add(rb.greaterThan(rt.get("endTime"), nowDate));
            ifNotNullThen(provinceId, t -> predicates.add(rb.or(rb.equal(rt.get("province").get("id"), t),rb.isNull(rt.get("province")))));
            ifNotNullThen(cityId, t -> predicates.add(rb.equal(rt.get("city").get("id"), t)));
            ifNotNullThen(areaId, t -> predicates.add(rb.equal(rt.get("area").get("id"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("weight"), false)).getRestriction();
        };
        return advertisementRepository.findAll(spec);

//        List<Advertisement> list = advertisementRepository.findByCategoryAndProvinceIdAndCityIdAndAreaIdAndValidAndIsDelete(AD_INDEX, provinceId, cityId, areaId, YES, NO);
//        return isEmpty(list) ? null : list;
    }

    public Advertisement findStartAdvertisement() {
        Advertisement advertisement = advertisementRepository.findByCategoryAndValidAndIsDelete(AD_APP_START, YES, NO);
        return advertisement;
    }
}
