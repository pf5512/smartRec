package com.thousandsunny.core.domain.service;


import com.google.common.collect.ImmutableMap;
import com.thousandsunny.common.entity.BFFunction;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.repository.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.thousandsunny.common.entity.Result.OK;

@Transactional
public class BaseService<T> {

    protected Logger _logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected BaseRepository<T> baseRepository;

    public boolean exists(Long id) {
        return baseRepository.exists(id);
    }

    public long count() {
        return baseRepository.count();
    }

    public long count(Specification<T> spec) {
        return baseRepository.count(spec);
    }

//    @CachePut(value = "T", key = "targetClass+'_'+#id")
    public T save(T entity) {
        return baseRepository.save(entity);
    }

    public List<T> save(Iterable<T> entities) {
        return baseRepository.save(entities);
    }

    public T saveAndFlush(T entity) {
        return baseRepository.saveAndFlush(entity);
    }

    public void flush() {
        baseRepository.flush();
    }

    public Result delete(Long id) {
        baseRepository.delete(id);
        return OK();
    }

    public Result delete(T entity) {
        baseRepository.delete(entity);
        return OK();
    }

    public Result delete(Iterable<? extends T> entities) {
        baseRepository.delete(entities);
        return OK();
    }

    public Result deleteAll() {
        baseRepository.deleteAll();
        return OK();
    }

    public Result deleteInBatch(Iterable<T> entities) {
        baseRepository.deleteInBatch(entities);
        return OK();
    }

    public Result deleteAllInBatch() {
        baseRepository.deleteAllInBatch();
        return OK();
    }

//    @Cacheable(value = "T", key = "targetClass+'_'+#id")
    public T findOne(Long id) {
        return baseRepository.findOne(id);
    }

    public T findOne(Specification<T> spec) {
        return baseRepository.findOne(spec);
    }

    public List<T> findAll() {
        return baseRepository.findAll();
    }

    public List<T> findAll(Sort sort) {
        return baseRepository.findAll(sort);
    }

    public Page<T> findAll(Pageable pageable) {
        return baseRepository.findAll(pageable);
    }

    public List<T> findAll(Iterable<Long> ids) {
        return baseRepository.findAll(ids);
    }

    public List<T> findAll(Specification<T> spec) {
        return baseRepository.findAll(spec);
    }

    public Page<T> findAll(Specification<T> specification, Pageable pageable) {
        return baseRepository.findAll(specification, pageable);
    }

    public List<T> findAll(Specification<T> spec, Sort sort) {
        return baseRepository.findAll(spec, sort);
    }

    /**
     * 以下是自己扩展的方法
     */

    public static Specification mapToEqualSpec(ImmutableMap<String, Object> keyAndValue) {
        return (root, query, cb) -> mapToEqualCri(keyAndValue, root, query, cb).getRestriction();
    }

    public static Specification mapToEqualDescSpec(ImmutableMap<String, Object> keyAndValue, String desc) {
        return (root, query, cb) -> mapToEqualCri(keyAndValue, root, query, cb)
                .orderBy(cb.desc(stringToPath(root, desc))).getRestriction();
    }

    public static Specification customSpec(BFFunction bf) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaQuery.where(bf.apply(criteriaBuilder, root)).getRestriction();
    }

    public static Specification customDescSpec(BFFunction bf, String descBy) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaQuery.where(bf.apply(criteriaBuilder, root)).orderBy(criteriaBuilder.desc(stringToPath(root, descBy))).getRestriction();
    }

    private static CriteriaQuery<?> mapToEqualCri(ImmutableMap<String, Object> keyAndValue, Root root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        keyAndValue.forEach((keys, value) -> stringToPredicate(root, cb, predicates, keys, value));
        return query.where(predicates.toArray(new Predicate[]{}));
    }

    private static void stringToPredicate(Root root, CriteriaBuilder cb, List<Predicate> predicates, String keys, Object value) {
        final Path path = stringToPath(root, keys);
        predicates.add(cb.equal(path, value));
    }

    private static Path stringToPath(Root root, String keys) {
        final Path[] path = new Path[1];
        StringTokenizer stringTokenizer = new StringTokenizer(keys, ".");
        while (stringTokenizer.hasMoreTokens()) {
            if (path[0] == null) {
                path[0] = root.get(stringTokenizer.nextToken());
            } else {
                path[0] = path[0].get(stringTokenizer.nextToken());
            }
        }
        return path[0];
    }

}
