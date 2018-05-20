package com.jiuli.local_share.network.uploader.model;


@SuppressWarnings("WeakerAccess")
public class ResponseModel<T> {
    //成功
    public static final int SUCCEED = 1;
    //其他错误
    public static final int ERROR_OTHER = 0;
    //错误的请求
    public static final int BAD_REQUEST = 4041;
    //验证错误
    public static final int ERROR_NO_PERMISSION = 2010;
    //服务器错误
    public static final int SERVICE_ERROR = 2010;

    private int code;
    private String message;
    private long time;
    private T result;

    public boolean success() {
        return code == SUCCEED;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
