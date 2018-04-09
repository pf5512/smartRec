package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.Job;
import com.thousandsunny.service.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.thousandsunny.service.ModuleKey.RecruitmentState;
import static com.thousandsunny.service.ModuleKey.RecruitmentType;

public interface JobRepository extends BaseRepository<Job> {
    Job findTop1ByShopIdAndIsDeleteAndStateOrderByDateDescFreshDesc(Long id, BooleanEnum isDelete,RecruitmentState state);

    List<Job> findByShopOwnerTokenAndIsDeleteOrderByDateDescFreshDesc(String userToken, BooleanEnum isDelete);

    Job findByShopOwnerTokenAndIdAndStateAndIsDelete(String userToken, Long id, RecruitmentState state, BooleanEnum isDelete);

    Job findByIdAndState(Long id, RecruitmentState state);

    Long countByShopAndIsDelete(Shop shop,BooleanEnum flag);

    List<Job> findByRecTypeAndShopAreaId(RecruitmentType recruitmentType, Long id);

    Page<Job> findByRecTypeAndShopAreaId(RecruitmentType recruitmentType, Long id, Pageable pageable);

    @Query(value = "select sr_job.* from sr_job join sr_shop on sr_job.shop_id=sr_shop.id  where job_type_id like :jobtype and  salary_id like :salary and  period_id like :period and sr_job.state like :state and sr_job.is_delete = 'NO' and sr_job.is_enable='YES'" +
            " order by sr_job.rec_type desc, round(6378.138*2*asin(sqrt(pow(sin((:lat*pi()/180-sr_shop.latitude*pi()/180)/2),2)+" +
            "cos(:lat*pi()/180)*cos(sr_shop.latitude*pi()/180)*pow(sin((:lon*pi()/180-sr_shop.longitude*pi()/180)/2),2)))*1000) limit :offset,:pageSize", nativeQuery = true)
    List<Job> findByDistance(@Param("state") String state,@Param("lon") Double longitude, @Param("lat") Double latitude, @Param("jobtype") String jobtype, @Param("salary") String salary, @Param("period") String period, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    List<Job> findByShopAndStateAndIsDelete(Shop shop,RecruitmentState state,BooleanEnum no);

    List<Job> findByShopAndIsDeleteAndIsEnable(Shop shop,BooleanEnum no,BooleanEnum flag);

    Job findByShopOwnerTokenAndIdAndIsDelete(String userToken, Long id, BooleanEnum isDelete);

    List<Job> findByShopOwnerTokenAndStateAndIsDeleteOrderByDateDescFreshDesc(String userToken, RecruitmentState state, BooleanEnum isDelete);

    Job findByIdAndStateAndIsDelete(Long id, RecruitmentState state, BooleanEnum isDelete);

    Page<Job> findByShopProvinceInAndShopCityInAndShopAreaIn(List<Region> province, List<Region> city, List<Region> area, Pageable pageable);

    Page<Job> findByState(RecruitmentState normal, Pageable pageable);

    Page<Job> findByShopAreaIdAndRecTypeAndIsEnableAndStateOrderByDateDesc(Long areaId, RecruitmentType type, BooleanEnum isEnable, RecruitmentState state, Pageable pageable);

    List<Job> findByShopAreaIdAndRecTypeAndIsEnableAndStateOrderByDateDesc(Long areaId, RecruitmentType type, BooleanEnum isEnable, RecruitmentState state);

    List<Job> findByShopId(Long shopId);

    Job findByShopOwnerTokenAndIdAndStateInAndIsDelete(String userToken, Long id, List<RecruitmentState> list, BooleanEnum no);
}
