package com.jiuli.local_share.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jiuli.local_share.R;

/**
 * ..Created by jiuli on 17-8-26.
 */

public class PortraitView extends android.support.v7.widget.AppCompatImageView {
    private String image;
    private static RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.portrait)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .circleCrop();
//public class PortraitView extends CircleImageView {
//    private String image;
//
//
//    private static RequestOptions options = new RequestOptions()
//            .placeholder(R.drawable.portrait)
//            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//            .error(R.color.red_200)
//            .centerCrop();

    public PortraitView(Context context) {
        super(context);

    }

    public PortraitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortraitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setup(RequestManager manager, Uri uri) {
        manager
                .load(uri == null ? "" : uri)
                .apply(options)
                .into(this);

    }

    public void setup(RequestManager manager, @NonNull String image) {
        this.image = image;
        manager.load(image)
                .apply(options)
                .into(this);

    }

    public void setup(@NonNull Activity activity, @NonNull String image) {
        this.image = image;
        if (activity.isDestroyed()) {
            return;
        }
        try {
            Glide.with(activity)
                    .load(image)
                    .apply(options)
                    .into(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getImage() {
        return image;
    }
}
