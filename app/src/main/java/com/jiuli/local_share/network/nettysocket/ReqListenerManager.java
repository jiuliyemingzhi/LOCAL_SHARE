package com.jiuli.local_share.network.nettysocket;

import android.support.annotation.Nullable;

import com.jiuli.local_share.network.nettysocket.message.ReqModel;
import com.jiuli.local_share.network.nettysocket.message.RespModel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ReqListenerManager {

    private static final ConcurrentHashMap<Integer, ReqModel.OnReqListener> listenerHashMap = new ConcurrentHashMap<>();

    private static int OUT_TIME = 1000 * 5;

    private static Timer timer = new Timer();


    public static ReqModel.OnReqListener remove(int key) {
        return listenerHashMap.remove(key);
    }

    public static ReqModel.OnReqListener get(int sessionID) {
        return listenerHashMap.get(sessionID);
    }

    public static ReqModel.OnReqListener put(final int sessionID, ReqModel.OnReqListener listener) {
        if (listener == null) {
            return null;
        }
        ReqModel.OnReqListener put = listenerHashMap.put(sessionID, listener);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                errorTimeout(sessionID);
            }
        }, OUT_TIME);
        return put;
    }

    private static void errorTimeout(int sessionID) {
        ReqModel.OnReqListener reqListener = get(sessionID);
        if (reqListener != null) {
            reqListener.onError(ReqModel.OnReqListener.ERROR_TIMEOUT);
        }
        remove(sessionID);
    }

    public static void onSucceed(@Nullable RespModel respModel) {
        if (respModel == null) {
            return;
        }
        int sessionID = respModel.getSessionID();
        ReqModel.OnReqListener listener = get(sessionID);
        if (listener != null) {
            listener.onSucceed(respModel);
        }
        remove(sessionID);
    }
}
