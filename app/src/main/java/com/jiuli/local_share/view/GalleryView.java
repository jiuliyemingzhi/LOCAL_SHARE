package com.jiuli.local_share.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jiuli.local_share.R;
import com.jiuli.local_share.util.Util;
import com.jiuli.local_share.view.recycler.RecyclerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * .Created by si ye on 17-8-26 .
 */

public class GalleryView extends RecyclerView {
    private static final int LOADER_ID = 0x0100;
    private static final int MIN_IMAGE_FILE_SIZE = 2 << 12;
    private Adapter mAdapter = new Adapter();
    private LoaderCallback mLoaderCallback = new LoaderCallback();


    public GalleryView(Context context) {
        super(context);
        init();
    }

    public GalleryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GalleryView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(new GridLayoutManager(getContext(), 3));
        setAdapter(mAdapter);
        Util.setRecyclerViewItemAnimationDuration(this);
    }


    public int setup(LoaderManager loaderManager, RecyclerAdapter.AdapterListener<Image> mListener) {
        mAdapter.setListener(mListener);
        loaderManager.initLoader(LOADER_ID, null, mLoaderCallback);
        return LOADER_ID;
    }


    private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String[] IMAGE_PROJECTION = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        };


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            if (id == LOADER_ID) {
                return new CursorLoader(getContext(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION,
                        null,
                        null,
                        IMAGE_PROJECTION[2] + " DESC");
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            List<Image> images = new ArrayList<>();
            if (cursor != null) {
                int count = cursor.getCount();
                if (count > 0) {
                    cursor.moveToFirst();
                    int indexId = cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]);
                    int indexPath = cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[1]);
                    int indexDate = cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]);
                    do {
                        int id = cursor.getInt(indexId);
                        String path = cursor.getString(indexPath);
                        long dateTime = cursor.getLong(indexDate);
                        File file = new File(path);
                        if (file.exists() && file.length() > MIN_IMAGE_FILE_SIZE) {
                            Image image = new Image();
                            image.id = id;
                            image.path = path;
                            image.date = dateTime;
                            images.add(image);
                        }
                    } while (cursor.moveToNext());
                }
            }
            updateSource(images);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            updateSource(null);
        }
    }


    private void updateSource(List<Image> images) {
        mAdapter.replace(images);
    }


    public final static class Image {
        int id;
        String path;
        long date;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Image image = (Image) o;

            return path != null ? path.equals(image.path) : image.path == null;
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }

        public String getPath() {
            return path;
        }
    }

    private class Adapter extends RecyclerAdapter<Image> {

        @Override
        protected ViewHolder onCreateViewHolder(View root, int viewType) {
            return new GalleryView.ViewHolder(root);
        }

        @Override
        public int getItemViewType(int position, Image image) {
            return R.layout.cell_layout;
        }
    }


    private RequestOptions options  = new RequestOptions()
            .centerCrop()
            .placeholder(R.color.yellow_50)
            .error(R.color.red_300)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

    private class ViewHolder extends RecyclerAdapter.ViewHolder<Image> {
        private ImageView mPic;


        public ViewHolder(View itemView) {
            super(itemView);
            mPic = itemView.findViewById(R.id.im_image);
        }

        @Override
        protected void onBind(Image image) {

            Glide.with(getContext())
                    .load(image.path) // 加载路径
                    .apply(options)
                    .into(mPic);
        }
    }

}
