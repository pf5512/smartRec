package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.repository.MemberVisitRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberVisit;
import com.thousandsunny.service.model.Resume;
import com.thousandsunny.service.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.getDate;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.PhoneType.*;
import static com.thousandsunny.core.ModuleKey.VisitState.HALF_HOUR;
import static com.thousandsunny.core.ModuleKey.VisitState.ONE_HOUR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by mu.jie on 2016/12/26.
 */
@Service
public class MemberVisitService extends BaseService<MemberVisit> {
    @Autowired
    private MemberVisitRepository memberVisitRepository;
    @Autowired
    private MemberRepository memberRepository;

    public long memberVisitStatistics(Long provinceId, Long cityId, Long areaId, Integer year, Integer month, Integer day) {
        Date startDate = getDate(year, month, day, 0, 0, 0);
        Date endDate = getDate(year, month, day, 23, 59, 59);
        Specification<MemberVisit> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(provinceId, t -> rb.equal(rt.get("province"), t));
            ifNotNullThen(cityId, t -> rb.equal(rt.get("city"), t));
            ifNotNullThen(areaId, t -> rb.equal(rt.get("area"), t));
            predicates.add(rb.greaterThan(rt.get("createTime"), startDate));
            predicates.add(rb.lessThan(rt.get("createTime"), endDate));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return memberVisitRepository.count(spec);
    }
    public long memberRegisterStatistics(Long provinceId, Long cityId, Long areaId, Integer year, Integer month, Integer day){
        Date startDate = getDate(year, month, day, 0, 0, 0);
        Date endDate = getDate(year, month, day, 23, 59, 59);
        Specification<Member> spec = (rt,rq,rb)->{
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(provinceId, t -> rb.equal(rt.get("province"), t));
            ifNotNullThen(cityId, t -> rb.equal(rt.get("city"), t));
            ifNotNullThen(areaId, t -> rb.equal(rt.get("area"), t));
            predicates.add(rb.lessThan(rt.get("createTime"), endDate));
            predicates.add(rb.greaterThan(rt.get("createTime"), startDate));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return memberRepository.count(spec);
    }

    public JSONObject countLoginInfo(Date startTime, Date endTime, String haveUser) {
        JSONObject jo = new JSONObject();
        jo.put("appUse", countLoginInfo1(startTime, endTime, "APP", null, haveUser));
        jo.put("wapUse", countLoginInfo1(startTime, endTime, "WEB", null, haveUser));
        jo.put("appUsehalf", countLoginInfo1(startTime, endTime, "APP", HALF_HOUR, haveUser));
        jo.put("wapUsehalf", countLoginInfo1(startTime, endTime, "WEB", HALF_HOUR, haveUser));
        jo.put("appUsehour", countLoginInfo1(startTime, endTime, "APP", ONE_HOUR, haveUser));
        jo.put("wapUsehour", countLoginInfo1(startTime, endTime, "WEB", ONE_HOUR, haveUser));
        return jo;
    }

    public Long countLoginInfo1(Date startTime, Date endTime, String platformType, ModuleKey.VisitState type, String haveUser) {
        Specification<MemberVisit> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if ("no".equals(haveUser))
                predicates.add(rt.get("member").isNull());
            else if ("yes".equals(haveUser))
                predicates.add(rt.get("member").isNotNull());
            if (isNotBlank(platformType)) {
                if ("WEB".equals(platformType))
                    predicates.add(rb.equal(rt.get("platformType"), WEB));
                else if ("APP".equals(platformType))
                    predicates.add(rb.or(rb.equal(rt.get("platformType"), IOS), rb.equal(rt.get("platformType"), ANDROID)));
            }
            ifNotNullThen(type, e -> predicates.add(rb.equal(rt.get("type"), e)));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createTime"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createTime"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return memberVisitRepository.count(spec);
    }

    public JSONObject countMeiyerenInfo(Date startTime, Date endTime, String isMeiyeren) {
        JSONObject jo = new JSONObject();
        jo.put("appUse", meiyerenInfo(startTime, endTime, isMeiyeren, "APP", null));
        jo.put("wapUse", meiyerenInfo(startTime, endTime, isMeiyeren, "WEB", null));
        jo.put("appUsehalf", meiyerenInfo(startTime, endTime, isMeiyeren, "APP", HALF_HOUR));
        jo.put("wapUsehalf", meiyerenInfo(startTime, endTime, isMeiyeren, "WEB", HALF_HOUR));
        jo.put("appUsehour", meiyerenInfo(startTime, endTime, isMeiyeren, "APP", ONE_HOUR));
        jo.put("wapUsehour", meiyerenInfo(startTime, endTime, isMeiyeren, "WEB", ONE_HOUR));
        return jo;
    }

    public Long meiyerenInfo(Date startTime, Date endTime, String isMeiyeren, String platformType, ModuleKey.VisitState type) {
        Specification<MemberVisit> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rt.get("member").isNotNull());
            if ("yes".equals(isMeiyeren)) predicates.add(rb.equal(rt.get("isMeiyeren"), YES));
            else if ("no".equals(isMeiyeren)) predicates.add(rb.equal(rt.get("isMeiyeren"), NO));
            if ("WEB".equals(platformType))
                predicates.add(rb.equal(rt.get("platformType"), WEB));
            else if ("APP".equals(platformType))
                predicates.add(rb.or(rb.equal(rt.get("platformType"), IOS), rb.equal(rt.get("platformType"), ANDROID)));
            ifNotNullThen(type, e -> predicates.add(rb.equal(rt.get("type"), e)));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createTime"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createTime"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return memberVisitRepository.count(spec);
    }
}
