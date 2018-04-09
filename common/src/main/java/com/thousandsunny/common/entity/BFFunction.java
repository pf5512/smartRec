package com.thousandsunny.common.entity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.function.BiFunction;

/**
 * Created by guitarist on 5/6/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public interface BFFunction extends BiFunction<CriteriaBuilder, Root, Predicate[]> {

    static Predicate[] ps(Predicate... predicates) {
        return predicates;
    }
}