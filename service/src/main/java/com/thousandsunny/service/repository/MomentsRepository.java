package com.thousandsunny.service.repository;

import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.Moments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.thousandsunny.service.ModuleKey.StateEnum;

import javax.persistence.ManyToOne;
import java.util.List;

public interface MomentsRepository extends BaseRepository<Moments> {
    Moments findByAuthorTokenAndIdAndIsDelete(String userToken, Long id, StateEnum isDelete);

    Page<Moments> findByAuthorTokenAndIsDeleteOrderByPublishTimeDesc(String userToken, StateEnum isDelete, Pageable pageable);

    @Query(value = "select * from sr_moments m where is_delete = 'NO' or province_id like :province or city_id like :city or area_id like :area order by round(6378.138*2*asin(sqrt(pow(sin((:lat*pi()/180-m.latitude*pi()/180)/2),2)+" +
            "cos(:lat*pi()/180)*cos(m.latitude*pi()/180)*" +
            "pow(sin((:lon*pi()/180-m.longitude*pi()/180)/2),2)))*1000),m.publish_time DESC limit :pageNo,:pageSize", nativeQuery = true)
    List<Moments> findByDistance(@Param("province") String province,@Param("city") String city,@Param("area") String area,@Param("lon") Double longitude,
                                 @Param("lat") Double latitude, @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    @Query(value = "select * from sr_moments m where is_delete = 'NO' or province_id like :province or city_id like :city or area_id like :area order by m.publish_time DESC  limit :pageNo,:pageSize", nativeQuery = true)
    List<Moments> findByRegion(@Param("province") String province,@Param("city") String city,@Param("area") String area,@Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);

    Page<Moments> findByIsDeleteOrderByPublishTimeDesc(StateEnum isDelete,Pageable pageable);

    Moments findByIdAndIsDeleteIn(Long id, List<StateEnum> stateEna);


}
