package com.jiuli.local_share.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jiuli.local_share.R;
import com.jiuli.local_share.view.recycler.RecyclerAdapter;
import com.jiuli.local_share.view.GalleryView;


/**
 * RefreshUi simple {@link Fragment} subclass.
 */

public class GalleryFragment extends BottomSheetDialogFragment implements RecyclerAdapter.AdapterListener<GalleryView.Image> {
    protected View mRoot;

    private OnSelectedListener mListener;

    GalleryView mGalleryView;

    public GalleryFragment() {

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGalleryView.setup(getLoaderManager(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (mRoot == null) {
            View root = inflater.inflate(R.layout.fragment_galley, container, false);
            mGalleryView = root.findViewById(R.id.gv_image_select);
            mRoot = root;
        } else {
            if (mRoot.getParent() != null) {
                container.removeView(mRoot);
            }
        }
        return mRoot;
    }

    public GalleryFragment setListener(OnSelectedListener listener) {
        this.mListener = listener;
        return this;
    }

    public interface OnSelectedListener {
        void imageSelectChanged(String path);
    }


    @Override
    public void onItemClick(RecyclerAdapter.ViewHolder<GalleryView.Image> holder, GalleryView.Image image, int position) {
        if (mListener != null) {
            mListener.imageSelectChanged(image.getPath());
            dismiss();
        }
    }

    @Override
    public void onItemLongClick(RecyclerAdapter.ViewHolder<GalleryView.Image> holder, GalleryView.Image image, int position) {

    }

}