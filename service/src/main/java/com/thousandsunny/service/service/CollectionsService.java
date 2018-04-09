package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;


/**
 * Created by Thinkpad on 2016/10/31.
 */
@Service
public class CollectionsService extends BaseService<JobCollect> {


    @Autowired
    private JobCollectRepository jobCollectRepository;
    @Autowired
    private ShopCollectRepository shopCollectRepository;
    @Autowired
    private ResumeCollectRepository resumeCollectRepository;
    @Autowired
    private VideoCollectRepository videoCollectRepository;
    @Autowired
    private PictureCollectRepository pictureCollectRepository;
    @Autowired
    private SchoolCollectRepository schoolCollectRepository;
    @Autowired
    private CourseCollectRepository courseCollectRepository;

    /**
     * 岗位收藏列表
     *
     * @return
     */
    public Page<JobCollect> getPostCollectList(String userToken, Pageable pageable) {

        return jobCollectRepository.findByMemberTokenAndCollectEverOrderByDateDesc(userToken, NO, pageable);
    }

    /**
     * 商铺收藏列表
     */

    public Page<ShopCollect> getShopCollectList(String userToken, Pageable pageable) {

        return shopCollectRepository.findByMemberTokenOrderByDateDesc(userToken, pageable);
    }


    /**
     * 简历收藏列表
     */

    public Page<ResumeCollect> getResumeCollectList(String userToken, Pageable pageable) {

        return resumeCollectRepository.findByMemberTokenAndCollectEverOrderByDateDesc(userToken, NO, pageable);
    }


    /**
     * 視頻收藏列表
     */
    public Page<VideoCollect> getVideoCollectList(String userToken, Pageable pageable) {

        return videoCollectRepository.findByMemberTokenAndCollectEverOrderByDateDesc(userToken, NO, pageable);
    }


    /**
     * 图片收藏列表
     */
    public Page<PictureCollect> getPictureCollectList(String userToken, Pageable pageable) {

        return pictureCollectRepository.findByMemberTokenAndCollectEverOrderByDateDesc(userToken, NO, pageable);
    }

    /**
     * 学校收藏列表
     */
    public Page<SchoolCollect> getSchoolCollect(Pageable pageable, String userToken) {
        return schoolCollectRepository.findByMemberTokenAndCollectEverOrderByDateDesc(userToken, NO, pageable);
    }

    /**
     * 课程收藏列表
     */
    public Page<CourseCollect> findCourseCollect(String userToken, Pageable pageable) {
        return courseCollectRepository.findByMemberTokenAndCollectEverOrderByDateDesc(userToken, NO, pageable);
    }
}
