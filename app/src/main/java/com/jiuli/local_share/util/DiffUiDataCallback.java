package com.jiuli.local_share.util;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * //Created by jiuli on 17-9-24.
 */

public class DiffUiDataCallback<T extends DiffUiDataCallback.UiDataDiffer<T>> extends DiffUtil.Callback {
    private List<T> mOldList, mNewList;

    public DiffUiDataCallback(List<T> oldList, List<T> newList) {
        this.mNewList = newList;
        this.mOldList = oldList;
    }

    public DiffUiDataCallback() {

    }



    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = mOldList.get(oldItemPosition);
        T newItem = mNewList.get(newItemPosition);
        return newItem.isSame(oldItem);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = mOldList.get(oldItemPosition);
        T newItem = mNewList.get(newItemPosition);
        return newItem.isUiContentSame(oldItem);
    }

    public interface UiDataDiffer<T> {
        boolean isSame(T old);

        boolean isUiContentSame(T old);
    }
}
