package com.dm.platform.common;

/**
 * 统一接口返回包装类
 * 前端拿到的所有 JSON 都是 { code, message, data } 这个结构, 方便统一处理
 */
public class Result<T> {

    /** 0 = 成功, 非 0 = 失败 */
    private int code;
    private String message;
    private T data;

    public Result() {
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }

    public static Result<Void> ok() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null);
    }

    // getter / setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
