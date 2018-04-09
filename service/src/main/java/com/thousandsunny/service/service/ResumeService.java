package com.thousandsunny.service.service;


import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.time.DateUtils.isSameDay;

@Service
public class ResumeService extends BaseService<Resume> {
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private ResumeWorkExpRepository resumeWorkExpRepository;
    @Autowired
    private ResumeTrainExpRepository resumeTrainExpRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ResumeCollectRepository resumeCollectRepository;
    @Autowired
    private ResumeLookService resumeLookService;
    @Autowired
    private CloudFileRepository cloudFileRepository;
    @Autowired
    private ResumeIntentionService intentionService;
    @Autowired
    private JobConstantRepository jobConstantRepository;

    /**
     * 简历列表
     */
    public Page<Resume> allResume(String nameKeyword, Long jobType, Long salary, Long period, Long provinceId, Long cityId, Long areaId, Pageable pageable) {
        Specification<Resume> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.isNotNull(rt.get("intention")));
            ifNotNullThen(jobType, x -> predicates.add(rb.like(rt.get("intention").get("type"), "%@" + x + "@%")));
            ifNotNullThen(salary, x -> {
                JobConstant salaryInterval = jobConstantRepository.findOne(salary);
                predicates.add(rb.between(rt.get("intention").get("salary"), salaryInterval.getMinVal(), salaryInterval.getMaxVal()));
            });
            ifNotBlankThen(nameKeyword, x -> predicates.add(rb.like(rt.get("member").get("realName"), "%" + x + "%")));
            ifNotNullThen(period, x -> predicates.add(rb.equal(rt.get("period").get("id"), x)));
            ifNotNullThen(provinceId, x -> predicates.add(rb.equal(rt.get("intention").get("province").get("id"), x)));
            ifNotNullThen(cityId, x -> predicates.add(rb.equal(rt.get("intention").get("city").get("id"), x)));
            ifNotNullThen(areaId, x -> predicates.add(rb.equal(rt.get("intention").get("area").get("id"), x)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("fresh"), false), new OrderImpl(rt.get("modify"), false)).getRestriction();
        };
        return resumeRepository.findAll(specification, pageable);
    }


    /**
     * 简历类别-附近
     */
    public List<Resume> nearResume(Long jobType, Long salary, Long period, Double lng, Double lat, Pageable pageable) {
        String jobTypeStr = jobType == null ? "%" : "%" + jobType + "%";
        Integer salaryMin = Integer.MIN_VALUE;
        Integer salaryMax = Integer.MAX_VALUE;
        if (salary != null) {
            JobConstant salaryInterval = jobConstantRepository.findOne(salary);
            salaryMin = salaryInterval.getMinVal();
            salaryMax = salaryInterval.getMaxVal();
        }
        Integer experienceMin = Integer.MIN_VALUE;
        Integer experienceMax = Integer.MAX_VALUE;
        if (isNotNull(period)){
            JobConstant experience = jobConstantRepository.findOne(period);
            Calendar cal = Calendar.getInstance();
            experienceMax = cal.get(Calendar.YEAR) - experience.getMinVal();
            experienceMin = cal.get(Calendar.YEAR) - experience.getMaxVal();
        }

        return resumeRepository.findByDistance1(jobTypeStr, salaryMin, salaryMax, experienceMin,experienceMax, lng, lat,
                pageable.getOffset(), pageable.getPageSize());
    }

    /**
     * 用户简历信息
     */
    public Resume reviewResume(String userToken, String checkedUserToken) {
        resumeLookService.refreshTrace(userToken, checkedUserToken);//刷新查看记录
        return resumeRepository.findByMemberToken(checkedUserToken);
    }

    public Resume findResume(String userToken) {
        Resume resume = resumeRepository.findByMemberToken(userToken);
        ifNullThrow(resume, TIP_NO_RESUME);
        ifTrueThrow(resume.getHighlights() == null && resume.getIntention() == null && resume.getTrainExps().size() == 0 && resume.getWorkExps().size() == 0, TIP_NO_RESUME);
        return resume;
    }

    public void editResume(String userToken, String highlights) {
        Resume resume = getResume(userToken);
        resume.setHighlights(highlights);
        resumeRepository.save(resume);
    }

    private Resume getResume(String userToken) {
        Resume resume = resumeRepository.findByMemberToken(userToken);
        if (isNull(resume)) {
            resume = new Resume();
        }
        resume.setMember(memberRepository.findByTokenAndIsDelete(userToken, NO));
        return resumeRepository.save(resume);
    }


    /**
     * 编辑求职意向
     */
    public void editResumeIntention(String userToken, ResumeIntention newIntension) {
        Resume resume = getResume(userToken);
        StringBuffer type = new StringBuffer("");
        ifNotEmptyThen(newIntension.getJobTypes(), x -> {
            x.forEach(y -> type.append("@" + y.getId() + "@,"));
            type.deleteCharAt(type.length() -1);
        });
        newIntension.setType(type.toString());
        ifNullThen(resume.getIntention(), () -> {
            resume.setIntention(intentionService.save(newIntension));
            save(resume);
        });
        ifNotNullThen(resume.getIntention(), i -> {
            ifNotEmptyThen(newIntension.getJobTypes(), i::setJobTypes);
            ifNotNullThen(newIntension.getSalary(), i::setSalary);
//            ifNotNullThen(newIntension.getProvince(), i::setProvince);
//            ifNotNullThen(newIntension.getCity(), i::setCity);
//            ifNotNullThen(newIntension.getArea(), i::setArea);
            i.setProvince(newIntension.getProvince());
            i.setCity(newIntension.getCity());
            i.setArea(newIntension.getArea());
            ifNotNullThen(newIntension.getWorkYear(), i::setWorkYear);
            ifNotNullThen(newIntension.getFindJobState(), i::setFindJobState);
            ifNotBlankThen(newIntension.getType(), i :: setType);
            i.setDate(new Date());
            intentionService.save(i);
        });
    }


    /**
     * 新增工作经验
     */
    public ResumeWorkExp editResumeWorkExp(String userToken, ResumeWorkExp resumeWorkExp) {
        Resume resume = getResume(userToken);
        resumeWorkExp.setResume(resume);
        if (isNull(resumeWorkExp.getId())) {//新增工作经验
            return resumeWorkExpRepository.save(resumeWorkExp);
        } else {
            ResumeWorkExp oldResumeWorkExp = resumeWorkExpRepository.findOne(resumeWorkExp.getId());
            resumeWorkExp.setId(oldResumeWorkExp.getId());
            copyProperties(oldResumeWorkExp, resumeWorkExp);
            return resumeWorkExpRepository.save(oldResumeWorkExp);
        }
    }


    public void delResumeWorkExp(String userToken, Long id) {
        Resume resume = resumeRepository.findByMemberToken(userToken);
        ifTrueThrow(isNull(resume), TIP_NO_RESUME);
        ResumeWorkExp resumeWorkExp = resumeWorkExpRepository.findOne(id);
        resume.getWorkExps().remove(resumeWorkExp);
        resumeWorkExpRepository.delete(resumeWorkExp);
        resumeRepository.save(resume);
    }


    /**
     * 新增培训经历
     */
    public ResumeTrainExp addResumeTrainExp(String userToken, ResumeTrainExp trainExp) {
        ifNotEmptyThen(trainExp.getCertification(), l -> trainExp.setCertification(cloudFileRepository.save(l)));
        trainExp.setResume(getResume(userToken));
        if (isNull(trainExp.getId())) {
            if (isNull(trainExp.getCertification())) trainExp.setCertification(new ArrayList<>());
            return resumeTrainExpRepository.save(trainExp);
        } else {
            ResumeTrainExp oldResumeTrainExp = resumeTrainExpRepository.findOne(trainExp.getId());
            trainExp.setId(oldResumeTrainExp.getId());
            copyProperties(oldResumeTrainExp, trainExp);
            return resumeTrainExpRepository.save(oldResumeTrainExp);
        }
    }


    public void delResumeTrainExp(String userToken, Long id) {
        Resume resume = resumeRepository.findByMemberToken(userToken);
        ifTrueThrow(isNull(resume), TIP_NO_RESUME);
        ResumeTrainExp resumeTriainExp = resumeTrainExpRepository.findOne(id);
        resume.getTrainExps().remove(resumeTriainExp);
        resumeTrainExpRepository.delete(resumeTriainExp);
        resumeRepository.save(resume);
    }

    public void freshResume(String userToken) {
        Resume resume = resumeRepository.findByMemberToken(userToken);
        ifTrueThrow(isNull(resume), TIP_NO_RESUME);
        ifTrueThrow(isSameDay(resume.getFresh(), new Date()), TIP_TOFAY_HAS_FRESHED);
        resume.setFresh(new Date());
        resumeRepository.save(resume);
    }


    public void collectResume(String userToken, String collectedUserToken, com.thousandsunny.thirdparty.ModuleKey.OperatorType operatorType) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        Resume resume = resumeRepository.findByMemberToken(collectedUserToken);
        ifNullThrow(resume, TIP_NO_RESUME);
        ResumeCollect resumeCollect = resumeCollectRepository.findByMemberTokenAndResumeId(userToken, resume.getId());
        if (operatorType == SURE) {
            ifTrueThrow(!isNull(resumeCollect) && resumeCollect.getCollectEver() == NO, TIP_COLLECT);
            if (isNull(resumeCollect)) {
                resumeCollect = new ResumeCollect();

            } else {
                resumeCollect.setCollectEver(NO);
            }

            resumeCollect.setMember(member);
            resumeCollect.setResume(resume);
            resumeCollect.setDate(new Date());

        } else {
            ifNullThrow(resumeCollect, TIP_NOT_COLLECT);
            ifTrueThrow(resumeCollect.getCollectEver() == YES, TIP_CANCEL_COLLECT);
            resumeCollect.setCollectEver(YES);
        }
        resumeCollectRepository.save(resumeCollect);
    }

    /**
     * 是否收藏对方简历
     */
    public boolean checkIsCollectionResume(String userToken, String checkedUserToken) {
        ResumeCollect resumeCollect = resumeCollectRepository.findByMemberTokenAndResumeMemberTokenAndCollectEver(userToken, checkedUserToken, NO);
        if (resumeCollect == null) {
            return false;
        }
        return resumeCollect.getCollectEver() == NO;
    }


    public boolean checkIsMeiyeren(Long id) {
        Member member = memberRepository.findOne(id);
        ifNullThrow(member,TIP_NO_CHANSHUERROR);
        Resume resume = resumeRepository.findByMemberToken(member.getToken());
        if (resume != null && resume.getIntention() != null) return true;
        else return false;

    }

    public int countIsMeiyeren(Set<Long> ids) {
        List<Long> collect = ids.stream().collect(toList());
        return resumeRepository.countByMemberIdInAndIntentionIsNotNull(collect);

    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public double GetDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378.137; //6378.137为地球半径，单位为千米；
        s = Math.round(s * 10000) / 10000;
        return s;
    }


}
