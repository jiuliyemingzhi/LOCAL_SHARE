package com.jiuli.local_share.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.jiuli.local_share.network.nettysocket.ShareSocket;
import com.jiuli.local_share.util.Util;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            int netType = Util.getNetType();
            if (netType == -1) {
                Util.showToast("网络已经断开!!!!!");
            } else {
                if (!ShareSocket.isConnected()) {
                    ShareSocket.securityConnect();
                }
            }
        }
    }


}