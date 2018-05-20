package com.jiuli.local_share.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.jiuli.local_share.network.nettysocket.ShareHandler;
import com.jiuli.local_share.network.nettysocket.ShareSocket;
import com.jiuli.local_share.network.nettysocket.message.ReqModel;
import com.jiuli.local_share.network.nettysocket.message.TypeUtil;
import com.jiuli.local_share.network.nettysocket.message.UserInfo;
import com.jiuli.local_share.receiver.NetworkConnectChangedReceiver;
import com.jiuli.local_share.util.Util;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

public final class ShareService extends Service implements TencentLocationListener {

    private static final String TAG = ShareService.class.getSimpleName();

    private OnLocationChanged onLocationChanged;

    private TencentLocationManager locationManager;

    private TencentLocationRequest request;

    private static volatile boolean isStarted = false;

    private NetworkConnectChangedReceiver receiver = new NetworkConnectChangedReceiver();




    public static boolean isStarted() {
        return isStarted;
    }

    public interface OnLocationChanged {
        void onLocationChanged();
    }




    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public void setOnLocationChanged(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (TencentLocation.ERROR_OK == i) {
            Util.getUserInfo().setLongitude(tencentLocation.getLongitude());
            Util.getUserInfo().setLatitude(tencentLocation.getLatitude());
            if (ShareHandler.isReady()) {
                ReqModel<UserInfo> reqModel = new ReqModel<>();
                reqModel.setContent(Util.getUserInfo());
                reqModel.setType(TypeUtil.USER_INFO);
                reqModel.setCode(ReqModel.CODE_PUSH_SHARE);
                System.out.println(reqModel.getType());
                ShareHandler.req(reqModel, null);
            }
            if (onLocationChanged != null) {
                onLocationChanged.onLocationChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        ShareSocket.disconnect();
        request = null;
        locationManager = null;
        Util.destroy();
    }


    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        String desc = "";
        switch (i) {
            case STATUS_DENIED:
                desc = "权限被禁止";
                break;
            case STATUS_DISABLED:
                desc = "模块关闭";
                break;
            case STATUS_ENABLED:
                desc = "模块开启";
                break;
            case STATUS_GPS_AVAILABLE:
                desc = "GPS可用，代表GPS开关打开，且搜星定位成功";
                break;
            case STATUS_GPS_UNAVAILABLE:
                desc = "GPS不可用，可能 gps 权限被禁止或无法成功搜星";
                break;
            case STATUS_LOCATION_SWITCH_OFF:
                desc = "位置信息开关关闭，在android M系统中，此时禁止进行wifi扫描";
                break;
            case STATUS_UNKNOWN:
                desc = "未知错误";
                break;
        }
        Log.e("location", "location status:" + s + ", " + s1 + " " + desc);
    }

    public final class MyBinder extends Binder {
        public ShareService getShareService() {
            return ShareService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStarted() || startId > 1) {
            stopSelf();
        } else {
            registerReceiver();
            bindListener();
            isStarted = true;
            ShareSocket.connect();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    protected void bindListener() {
        request = TencentLocationRequest.create();
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO);
        request.setInterval(1000);
        locationManager = TencentLocationManager.getInstance(this);
        int error = locationManager.requestLocationUpdates(request, this);

        switch (error) {
            case 0:
                Log.e("location", "成功注册监听器");
                break;
            case 1:
                Log.e("location", "设备缺少使用腾讯定位服务需要的基本条件");
                break;
            case 2:
                Log.e("location", "manifest 中配置的 key 不正确");
                break;
            case 3:
                Log.e("location", "自动加载libtencentloc.so失败");
                break;
            default:
                break;
        }

    }


}
