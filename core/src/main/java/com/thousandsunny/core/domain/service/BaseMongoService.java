package com.thousandsunny.core.domain.service;

/**
 * Created by guitarist on 5/2/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
//@Transactional
public class BaseMongoService<T> {

   /* @Autowired
    protected MongoRepository<T, String> baseRepository;

    public Result<Boolean> exists(String id) {
        return Result.OK(baseRepository.exists(id));
    }

    public Result<Long> count() {
        return Result.OK(baseRepository.count());
    }

    public Result<T> save(T entity) {
        return Result.CREATED(baseRepository.save(entity));
    }

    public Result delete(String id) {
        baseRepository.delete(id);
        return Result.DELETED();
    }

    public Result delete(T entity) {
        baseRepository.delete(entity);
        return Result.DELETED();
    }

    public Result delete(Iterable<? extends T> entities) {
        baseRepository.delete(entities);
        return Result.DELETED();
    }

    public Result deleteAll() {
        baseRepository.deleteAll();
        return Result.DELETED();
    }

    public Result<T> findOne(String id) {
        return Result.OK(baseRepository.findOne(id));
    }

    public Result<T> save(Iterable<T> iterable) {
        return Result.CREATED(baseRepository.save(iterable));
    }

    public Result<T> findAll() {
        return Result.OK(baseRepository.findAll());
    }

    public Result<T> findAll(Iterable<String> iterable) {
        return Result.OK(baseRepository.findAll(iterable));
    }

    public Result<T> findAll(Sort sort) {
        return Result.OK(baseRepository.findAll(sort));
    }

    public Result<T> insert(T s) {
        return Result.CREATED(baseRepository.insert(s));
    }

    public Result<T> insert(Iterable<T> iterable) {
        return Result.CREATED(baseRepository.insert(iterable));
    }

    public Result<T> findAll(Pageable pageable) {
        return Result.OK(baseRepository.findAll(pageable));
    }
*/
}
