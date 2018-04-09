package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.Org;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * 如果这些代码有用，那它们是guitarist在7/26/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface OrgRepository extends BaseRepository<Org> {
    List<Org> findByParentIsNullAndIsDelete(BooleanEnum no);

    List<Org> findByParentIdAndIsDelete(Long id, BooleanEnum no);

    @Query("select max(o.orderCode) from Org o")
    Long findMaxWeight();
}
