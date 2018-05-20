package com.jiuli.local_share.network.nettysocket.message;

public class RespModel<T> {
    // 成功
    public static final byte CODE_SUCCEED = 1;
    // 未知错误
    public static final byte CODE_ERROR_UNKNOWN = 0;
    // 请求参数错误
    public static final byte CODE_BAD_REQUEST = 2;
    // 服务器错误
    public static final byte CODE_ERROR_SERVICE = 3;
    // 没有权限操作
    public static final byte CODE_ERROR_ACCOUNT_NO_PERMISSION = 4;
    //状态错误
    public static final byte CODE_ERROR_STATE = 5;

    //----------------------------------|---------------------------------

    private byte type;
    private byte code;
    private int sessionID;
    private T result;
    private String message;
    private long time;

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
