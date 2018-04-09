package com.thousandsunny.common.lambda;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.ModuleTip;
import com.thousandsunny.common.exception.BaseException;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.data.domain.Page;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by guitarist on 4/23/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class LambdaUtil {

    public static <T> void ifNotNullThen(T value, Consumer<T> function) {
        if (value != null) {
            function.accept(value);
        }
    }

    public static <T> void ifNotEmptyThen(List<T> value, Consumer<List<T>> function) {
        if (isNotEmpty(value)) {
            function.accept(value);
        }
    }

    public static void ifNotBlankThen(String value, Consumer<String> function) {
        if (isNotBlank(value)) {
            function.accept(value);
        }
    }

    public static void ifNullThen(Object obj, VoidFunction function) {
        if (obj == null) {
            function.exec();
        }
    }

    public static void ifTrueThen(Boolean isTrue, VoidFunction supplier) {
        if (isTrue)
            supplier.exec();
    }

    public static void ifFalseThen(Boolean isTrue, VoidFunction supplier) {
        ifTrueThen(!isTrue, supplier);
    }

    public static void ifTrueThrow(Boolean isTrue, ModuleTip msg) {
        if (isTrue)
            throw new BaseException(msg);
    }

    public static void ifTrueThrow(BooleanSupplier supplier, ModuleTip msg) {
        if (supplier.getAsBoolean())
            throw new BaseException(msg);
    }

    public static <T> void ifEmptyThrow(List<T> list, ModuleTip msg) {
        if (isEmpty(list))
            throw new BaseException(msg);
    }

    public static void ifFalseThrow(Boolean isTrue, ModuleTip msg) {
        ifTrueThrow(!isTrue, msg);
    }


    public static <T> void ifNullThrow(T obj, ModuleTip msg) {
        if (isNull(obj))
            throw new BaseException(msg);
    }

    public static <T> void ifNotNullThrow(T obj, ModuleTip msg) {
        if (!isNull(obj))
            throw new BaseException(msg);
    }

    public static <T> Boolean isNotNull(T t) {
        return !isNull(t);
    }

    public static <I, O> List<O> simpleMap(List<I> list, Function<I, O> function) {
        if (isNull(list)) return null;
        return list.stream().map(function).collect(toList());
    }

    public static <I> List<I> simpleSort(List<I> list, Comparator<I> comparator) {
        if (isNull(list)) return null;
        return list.stream().sorted(comparator).collect(toList());
    }

    public static <I> List<I> simpleFilter(List<I> list, Predicate<I> predicate) {
        if (isNull(list)) return null;
        return list.stream().filter(predicate).collect(toList());
    }

    public static <I, O> List<O> simpleMapFilter(List<I> list, Function<I, O> function, Predicate<O> predicate) {
        if (isNull(list)) return null;
        return list.stream().map(function).filter(predicate).collect(toList());
    }

    public static <I, O> List<O> simpleFilterMap(List<I> list, Predicate<I> predicate, Function<I, O> function) {
        if (isNull(list)) return null;
        return list.stream().filter(predicate).map(function).collect(toList());
    }

    public static <I, O> List<O> simpleSortMap(List<I> list, Comparator<I> comparator, Function<I, O> function) {
        if (isNull(list)) return null;
        return list.stream().sorted(comparator).map(function).collect(toList());
    }

    public static String joiner(String separator, String... item) {
        List<String> notNullList = simpleFilter(newArrayList(item), LambdaUtil::isNotNull);
        return on(separator).join(notNullList);
    }

    public static <T> JSONObject listToJson(List<T> list) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", list);
        return jsonObject;
    }

    public static <T> JSONObject pageToJson(Page<T> page, Function<T, JSONObject> function) {
        List<JSONObject> jos = simpleMap(page.getContent(), function);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", jos);
        jsonObject.put("first", !page.hasPrevious());
        jsonObject.put("last", !page.hasNext());
        jsonObject.put("pageNo", page.getNumber());
        return jsonObject;
    }

    public static void copyProperties(Object dest, Object orig) {
        try {
            PropertyUtils.copyProperties(dest, orig);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
