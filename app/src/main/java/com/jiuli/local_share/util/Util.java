package com.jiuli.local_share.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.jiuli.local_share.network.nettysocket.message.UserInfo;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.io.File;
import java.util.UUID;

public class Util {
    public static final String KEY = "1b46037a-09ee-4a28-8361-21cdb1af8a51";

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private static SharedPreferences sharedPreferences;

    private static UserInfo userInfo;


    public static void init(Context context) {
        mContext = context;
        sharedPreferences = context.getSharedPreferences("INFO", Context.MODE_PRIVATE);
    }


    public static UserInfo getUserInfo() {
        if (userInfo == null) {
            userInfo = new UserInfo();
            String uuid = sharedPreferences.getString("uuid", "");
            if (stringIsEmpty(uuid)) {
                uuid = UUID.randomUUID().toString();
                sharedPreferences.edit().putString("uuid", uuid).apply();
            }
            userInfo.setUuid(uuid);
            userInfo.setImage(sharedPreferences.getString("image", ""));
            userInfo.setName(sharedPreferences.getString("name", ""));
        }
        return userInfo;
    }


    public static String getUUID() {
        return getUserInfo().getUuid();
    }

    public static String getName() {
        return getUserInfo().getName();
    }

    public static void setName(String name) {
        getUserInfo().setName(name);
        sharedPreferences.edit().putString("name", name).apply();
    }


    public static String getImage() {
        return getUserInfo().getImage();
    }

    public static void setImage(String image) {
        getUserInfo().setImage(image);
        sharedPreferences.edit().putString("image", image).apply();
    }

    public static boolean stringIsEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static void destroy() {
        mContext = null;
    }

    public static void showToast(CharSequence charSequence) {
        if (mContext != null) {
            Toast.makeText(mContext, charSequence, Toast.LENGTH_SHORT).show();
        }
    }

    public static void onUiThreadShowToast(final  CharSequence charSequence) {
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                if (mContext != null) {
                    Toast.makeText(mContext, charSequence, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static int getNetType() {
        if (mContext == null) {
            return -1;
        }
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return -1;
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                netType = 3;
            } else {
                netType = 2;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }

    public static void setRecyclerViewItemAnimationDuration(RecyclerView recyclerView) {
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        animator.setRemoveDuration((long) 30);
        animator.setChangeDuration(0);
        animator.setAddDuration((long) 30);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getPortraitTmpFile() {
        File dir = new File(getCacheDirFile(), "portrait");
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
        File path = new File(dir, SystemClock.uptimeMillis() + ".jpg");
        return path.getAbsoluteFile();
    }

    private static File getCacheDirFile() {
        return mContext.getCacheDir();
    }

    private static String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }

    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }
}
