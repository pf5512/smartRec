package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.ModuleKey.PhotoType;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.model.SchoolPhoto;
import com.thousandsunny.service.repository.SchoolPhotoRepository;
import com.thousandsunny.service.repository.SchoolRepository;
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
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.PhotoType.ENVIRONMENT;
import static com.thousandsunny.service.ModuleKey.PhotoType.PRODUCTION;
import static com.thousandsunny.service.ModuleTips.TIP_NO_AUTHORITY;
import static com.thousandsunny.service.ModuleTips.TIP_NO_SCHOOL;
import static com.thousandsunny.service.ModuleTips.TIP_NO_SCHOOL_PHOTO;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;

/**
 * Created by 13336 on 2017/2/16.
 */
@Service
public class SchoolPhotoService extends BaseService<SchoolPhoto> {
    @Autowired
    private SchoolPhotoRepository schoolPhotoRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;

    public SchoolPhoto findSchoolFirstPhoto(School e) {
        return schoolPhotoRepository.findTop1BySchoolIdAndTypeAndIsEnableAndIsDeleteOrderByNumber(e.getId(), ENVIRONMENT, YES, NO);
    }

    public List<SchoolPhoto> findSchoolEnvironmentPhotos(School school) {
        return schoolPhotoRepository.findBySchoolIdAndTypeAndIsEnableAndIsDeleteOrderByNumber(school.getId(), ENVIRONMENT, YES, NO);
    }

    public List<SchoolPhoto> findSchoolProductionPhotos(School school) {
        return schoolPhotoRepository.findBySchoolIdAndTypeAndIsEnableAndIsDeleteOrderByNumber(school.getId(), PRODUCTION, YES, NO);
    }

    /**
     * 后台3.3.1
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public Page<SchoolPhoto> findSchoolPhotoPage(Pageable pageable, String text, PhotoType imgType, Member member) {
        Specification<SchoolPhoto> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (member.getRole() == SCHOOL)
                predicates.add(rb.equal(rt.get("school").get("member").get("id"), member.getId()));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("school").get("name"), "%" + e + "%")));
            ifNotNullThen(imgType, e -> predicates.add(rb.equal(rt.get("type"), imgType)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return schoolPhotoRepository.findAll(spec, pageable);
    }

    /**
     * 后台3.3.2
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public void deleteSchoolPhotos(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            SchoolPhoto schoolPhoto = schoolPhotoRepository.findOne(ids[i]);
            ifNullThrow(schoolPhoto, TIP_NO_SCHOOL_PHOTO);
            schoolPhoto.setIsDelete(YES);
            schoolPhotoRepository.save(schoolPhoto);
        }
    }

    /**
     * 后台3.3.3
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public void saveSchoolPhoto(Long id, Member member) {
        ifFalseThrow(member.getRole() == com.thousandsunny.core.ModuleKey.AccountEnum.MANAGER, TIP_NO_AUTHORITY);
        SchoolPhoto schoolPhoto = schoolPhotoRepository.findOne(id);
        ifNullThrow(schoolPhoto, TIP_NO_SCHOOL_PHOTO);
        if (schoolPhoto.getIsEnable() == YES) schoolPhoto.setIsEnable(NO);
        else schoolPhoto.setIsEnable(YES);
        schoolPhotoRepository.save(schoolPhoto);
    }

    /**
     * 后台3.3.4
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public void editSchoolPhoto(String userToken,Long id, Long schoolId, PhotoType imgType,
                                String imgUrl, String imgDescription, Integer sortNo, Date publishTime) {
        SchoolPhoto schoolPhoto;
        if (isNotNull(id)) {
            schoolPhoto = schoolPhotoRepository.findByIdAndIsDelete(id, NO);
            ifNullThrow(schoolPhoto, TIP_NO_SCHOOL_PHOTO);
        } else schoolPhoto = new SchoolPhoto();
        schoolPhoto.setType(imgType);
        CloudFile photo = new CloudFile();
        photo.setPath(imgUrl);
        schoolPhoto.setPhoto(cloudFileRepository.save(photo));
        ifNotBlankThen(imgDescription, e -> schoolPhoto.setText(e));
        ifNotNullThen(sortNo, e -> schoolPhoto.setNumber(e));
        ifNotNullThen(publishTime, e -> schoolPhoto.setCreateTime(e));
        School school;
        if (schoolId != null) {
            school = schoolRepository.findOne(schoolId);
        }else {
            school = schoolRepository.findByMemberToken(userToken);
        }
        ifNullThrow(school, TIP_NO_SCHOOL);
        schoolPhoto.setSchool(school);
        schoolPhoto.setIsEnable(YES);
        schoolPhoto.setIsDelete(NO);
        schoolPhotoRepository.save(schoolPhoto);
    }
}
