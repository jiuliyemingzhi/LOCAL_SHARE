package com.jiuli.local_share.network.nettysocket.message;

public class ReqModel<T> {
    //要执行的操作
    public static final byte CODE_HEARTBEAT = 0;
    public static final byte CODE_PUSH_SHARE = 1;
    public static final byte CODE_LOGIN = 2;
    public static final byte CODE_JOIN = 3;
    public static final byte CODE_GET_GROUPS = 4;

    private byte code;
    private byte type;
    private String KEY;
    private int sessionID;
    private T content;

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getKEY() {
        return KEY;
    }

    public void setKEY(String KEY) {
        this.KEY = KEY;
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

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public class OnReqRespListenerImpl implements OnReqListener {

        @Override
        public void onSucceed(RespModel respModel) {

        }

        @Override
        public void onError(int errorCode) {

        }

    }

    public interface OnReqListener {

        int ERROR_OTHER = 4;
        int ERROR_NETWORK_ERROR = 3;
        int ERROR_TIMEOUT = 2;

        void onSucceed(RespModel respModel);

        void onError(int errorCode);
    }
}
