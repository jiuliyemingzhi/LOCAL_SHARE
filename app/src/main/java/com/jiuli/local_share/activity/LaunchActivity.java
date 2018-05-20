package com.jiuli.local_share.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.jiuli.local_share.R;
import com.jiuli.local_share.service.ShareService;
import com.jiuli.local_share.util.Util;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class LaunchActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    public static final String TAG = "PERMISSIONS";

    @SuppressWarnings("FieldCanBeLocal")
    private final int REQUEST_CODE = 0x100;


    private final static String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CHANGE_NETWORK_STATE,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,

    };

    private boolean haveAll() {
        return EasyPermissions.hasPermissions(this, PERMISSIONS);
    }

    public void requestPermission() {
        EasyPermissions.requestPermissions(this,
                "申请权限",
                REQUEST_CODE, PERMISSIONS);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        initEd();
    }

    private void initEd() {


        if (!ShareService.isStarted()) {
            startService(new Intent(this, ShareService.class));
        }
        if (Util.stringIsEmpty(Util.getImage())) {
            UserActivity.show(this);
        } else {
            MainActivity.show(this);
        }
        finish();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_launch);
        if (haveAll()) {
            initEd();
        } else {
            requestPermission();
        }
    }

}
