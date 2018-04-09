package com.thousandsunny.common.entity;


import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

public class Result<T> implements Serializable {

    private HttpStatus httpStatus;

    private String msg;

    private T data;

    private List<T> list;

    private Page<T> page;

    private Result() {
    }


    private Result(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    private Result(T data, HttpStatus httpStatus) {
        this.data = data;
        this.httpStatus = httpStatus;
    }

    private Result(Page<T> page, HttpStatus httpStatus) {
        this.page = page;
        this.httpStatus = httpStatus;
    }

    private Result(HttpStatus httpStatus, String msg) {
        this.httpStatus = httpStatus;
        this.msg = msg;
    }

    private Result(HttpStatus httpStatus, String msg, T data) {
        this.httpStatus = httpStatus;
        this.msg = msg;
        this.data = data;
    }

    private Result(List<T> list, HttpStatus httpStatus) {
        this.list = list;
        this.httpStatus = httpStatus;
    }

    public Integer getCode() {
        return httpStatus.value();
    }

    public T getData() {
        return data;
    }

    public List<T> getList() {
        return list;
    }

    public String getMessage() {
        return null == msg ? httpStatus.getReasonPhrase() : msg;
    }

    public Page<T> getPage() {
        return page;
    }

    public static <T> Result OK() {
        return new Result<T>(OK);
    }

    public static <T> Result OK(T data) {
        return new Result(data, OK);
    }

    public static <T> Result OK(Page<T> page) {
        return new Result(new BackPage(page), OK);
    }

    public static <T> Result OK(Iterable<T> iterable) {
        return new Result(iterable, OK);
    }

    public static <T> Result OK(List<T> list) {
        return new Result(list, OK);
    }

    /**
     * 参数类型不对
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(BAD_REQUEST, msg);
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(UNAUTHORIZED, msg);
    }

    /**
     * 付费要求
     */
    public static <T> Result<T> paymentRequired(String msg) {
        return new Result<>(PAYMENT_REQUIRED, msg);
    }

    /**
     * 有不符合的条件
     */
    public static <T> Result<T> methodNotAllowed(String msg) {
        return new Result<>(METHOD_NOT_ALLOWED, msg);
    }

    /**
     * 无法接受
     */
    public static <T> Result<T> notAcceptable(String msg) {
        return new Result<>(NOT_ACCEPTABLE, msg);
    }

    /**
     * 冲突
     */
    public static <T> Result<T> conflict(String msg) {
        return new Result<>(CONFLICT, msg);
    }

    /**
     * 资源已被删除
     */
    public static <T> Result<T> gone(String msg) {
        return new Result<>(GONE, msg);
    }

    /**
     * 参数,数据长度不符合
     */
    public static <T> Result<T> lengthRequired(String msg) {
        return new Result<>(LENGTH_REQUIRED, msg);
    }

    /**
     * 某个前置条件不符合
     */
    public static <T> Result<T> preconditionFailed(String msg) {
        return new Result<>(PRECONDITION_FAILED, msg);
    }

    /**
     * 负载太大
     */
    public static <T> Result<T> payloadTooLarge(String msg) {
        return new Result<>(PAYLOAD_TOO_LARGE, msg);
    }

    /**
     * 不支持文件类型
     */
    public static <T> Result<T> unsupportedMediaType(String msg) {
        return new Result<>(UNSUPPORTED_MEDIA_TYPE, msg);
    }

    /**
     * 所请求的范围无法满足
     */
    public static <T> Result<T> requestedRangeNotSatisfiable(String msg) {
        return new Result<>(REQUESTED_RANGE_NOT_SATISFIABLE, msg);
    }

    /**
     * 期望失败
     */
    public static <T> Result<T> expectationFailed(String msg) {
        return new Result<>(REQUESTED_RANGE_NOT_SATISFIABLE, msg);
    }

    /**
     * 错误实体
     */
    public static <T> Result<T> unprocessableEntity(String msg) {
        return new Result<>(UNPROCESSABLE_ENTITY, msg);
    }

    /**
     * 不可用
     */
    public static <T> Result<T> locked(String msg) {
        return new Result<>(LOCKED, msg);
    }

    /**
     * 错误接洽关系
     */
    public static <T> Result<T> failedDependency(String msg) {
        return new Result<>(FAILED_DEPENDENCY, msg);
    }

    /**
     * 需要升级
     */
    public static <T> Result<T> upgradeRequired(String msg) {
        return new Result<>(UPGRADE_REQUIRED, msg);
    }

    /**
     * 要求先决条件
     */
    public static <T> Result<T> preconditionRequired(String msg) {
        return new Result<>(PRECONDITION_REQUIRED, msg);
    }

    /**
     * 请求不允许
     */
    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(FORBIDDEN, msg);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String msg) {
        return new Result<>(NOT_FOUND, msg);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> internalServerError(String msg, T data) {
        return new Result<>(INTERNAL_SERVER_ERROR, msg, data);
    }

    public boolean isSuccess() {
        return httpStatus.is2xxSuccessful();
    }
}
