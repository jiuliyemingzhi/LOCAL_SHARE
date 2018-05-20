package com.jiuli.local_share.view.recycler;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by jiuli on 17-8-17.
 */

public abstract class RecyclerAdapter<Data>
        extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder<Data>>
        implements View.OnLongClickListener, View.OnClickListener, AdapterCallback<Data> {
    private final List<Data> mDataList;

    public void setListener(AdapterListener<Data> mListener) {
        this.mListener = mListener;
    }

    private AdapterListener<Data> mListener;

    public RecyclerAdapter() {
        this(null);
    }

    public RecyclerAdapter(AdapterListener<Data> listener) {
        this(new ArrayList<Data>(), listener);
    }

    public RecyclerAdapter(ArrayList<Data> dataList, AdapterListener<Data> listener) {
        this.mDataList = dataList;
        this.mListener = listener;
    }

    /**
     * 创建一个ViewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder<Data> onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View root = inflater.inflate(viewType, parent, false);
        ViewHolder<Data> holder = onCreateViewHolder(root, viewType);
        root.setTag(holder);
        root.setOnClickListener(this);
        root.setOnLongClickListener(this);
        holder.callback = this;

        return holder;
    }

    public List<Data> getDataList() {
        return mDataList;
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(position, mDataList.get(position));
    }

    /**
     * 得到布局的类型
     *
     * @param position 　坐标
     * @param data     　当前数据
     * @return xml 文件的ID用于创建ViewHolder
     */
    @LayoutRes
    public abstract int getItemViewType(int position, Data data);

    protected abstract ViewHolder<Data> onCreateViewHolder(View root, int viewType);

    @Override
    public void onBindViewHolder(ViewHolder<Data> holder, int position) {
        Data data = mDataList.get(position);
        holder.bind(data);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public boolean onLongClick(View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (mListener != null) {
            int position = viewHolder.getAdapterPosition();
            mListener.onItemLongClick(viewHolder, mDataList.get(position), position);
            return true;
        }
        return false;
    }


    @Override
    public void onClick(View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (mListener != null) {
            int position = viewHolder.getAdapterPosition();
            mListener.onItemClick(viewHolder, mDataList.get(position), position);
        }
    }

    @Override
    public Data update(Data data, ViewHolder<Data> holder) {
        int position = holder.getAdapterPosition();
        if (position >= 0) {
            Data remove = mDataList.remove(position);
            mDataList.add(position, data);
            notifyItemChanged(position);
            return remove;
        }
        return null;
    }

    private void add(Data data) {
        mDataList.add(data);
        notifyItemInserted(mDataList.size() - 1);
    }

    public void add(Data... dataList) {
        if (dataList != null && dataList.length > 0) {
            int startAggs = mDataList.size();
            Collections.addAll(mDataList, dataList);
            notifyItemRangeInserted(startAggs, dataList.length);
        }
    }

    public void add(Collection<Data> dataList) {
        if (dataList != null && dataList.size() > 0) {
            int startArgs = mDataList.size();
            mDataList.addAll(dataList);
            notifyItemRangeInserted(startArgs, dataList.size());
        }
    }

    public void replace(Collection<Data> dataList) {
        mDataList.clear();
        if (dataList != null && dataList.size() > 0) {
            mDataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public void replaceAllData(Collection<Data> dataList, boolean isNotify) {
        mDataList.clear();
        if (dataList != null && !dataList.isEmpty()) {
            mDataList.addAll(dataList);
            if (isNotify) {
                notifyDataSetChanged();
            }
        }
    }

    public void clear() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    public boolean remove(Data data) {
        boolean remove = mDataList.remove(data);
        notifyDataSetChanged();
        return remove;
    }

    public Data remove(int position) {
        Data remove = mDataList.remove(position);
        notifyDataSetChanged();
        return remove;
    }

    public interface AdapterListener<Data> {

        void onItemClick(ViewHolder<Data> holder, Data data, int position);


        void onItemLongClick(ViewHolder<Data> holder, Data data, int position);
    }


    public static abstract class ViewHolder<Data> extends RecyclerView.ViewHolder {

        protected Data mData;
        private AdapterCallback<Data> callback;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * 用于绑定数据的触发
         *
         * @param data
         */
        void bind(Data data) {
            this.mData = data;
            onBind(data);
        }

        public void refresh() {
            updateData(mData);
        }

        /**
         * 当数据绑定时触发回调，必须覆写
         *
         * @param data
         */


        protected abstract void onBind(Data data);

        public void updateData(Data data) {
            if (callback != null) {
                this.callback.update(data, this);
            }
        }
    }

    public static abstract class AdapterListenerImpl<Data> implements AdapterListener<Data> {

        @Override
        public void onItemClick(ViewHolder<Data> holder, Data data, int position) {

        }

        @Override
        public void onItemLongClick(ViewHolder<Data> holder, Data data, int position) {

        }
    }
}
