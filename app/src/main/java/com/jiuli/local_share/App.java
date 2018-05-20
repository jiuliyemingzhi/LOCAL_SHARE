package com.jiuli.local_share;

import android.support.multidex.MultiDexApplication;

import com.jiuli.local_share.util.Util;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Util.init(this);
    }
}
