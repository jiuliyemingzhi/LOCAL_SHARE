package com.jiuli.local_share.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jiuli.local_share.R;
import com.jiuli.local_share.network.nettysocket.ShareHandler;
import com.jiuli.local_share.network.nettysocket.message.Group;
import com.jiuli.local_share.network.nettysocket.message.Groups;
import com.jiuli.local_share.network.nettysocket.message.ReqModel;
import com.jiuli.local_share.network.nettysocket.message.RespModel;
import com.jiuli.local_share.network.nettysocket.message.UserInfo;
import com.jiuli.local_share.service.ShareService;
import com.jiuli.local_share.util.DiffUiDataCallback;
import com.jiuli.local_share.util.Util;
import com.jiuli.local_share.view.PortraitView;
import com.jiuli.local_share.view.recycler.RecyclerAdapter;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.UiSettings;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;
import net.qiujuer.genius.ui.Ui;
import net.qiujuer.genius.ui.compat.UiCompat;
import net.qiujuer.genius.ui.widget.Loading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jiuli.local_share.R.id;
import static com.jiuli.local_share.R.layout;

public class MainActivity extends AppCompatActivity implements
        ServiceConnection, ShareService.OnLocationChanged,
        View.OnClickListener, ShareHandler.OnGroupInfoListRespListener {

    private ShareService shareService;//获取地址发送接收消息的门户

    private TencentMap map;//地图位图

    private MapView mapView;//地图

    private TextView tvGroupSimpleInfo;//简单的显示组信息

    private ViewGroup mRoot;//根布局

    private boolean isFirstCenter = true;//是不是调用一次setCenter()

    private RecyclerView mRvShareUsers;//group 成员都简单显示topBar

    private Adapter mAdapter;//mRvShareUsers的adapter

    private TencentSearch mTencentSearch;//腾讯检索服务

    private HashMap<String, Marker> markerHashMap = new HashMap<>();//maker容器

    private BoomMenuButton mBmb;//菜单

    private int AddressCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);
    }


    /**
     * 初始化所有View
     */
    private void initView(Bundle savedInstanceState) {
        tvGroupSimpleInfo = findViewById(id.tv_list_simple_info);
        mRoot = findViewById(id.root);
        mRvShareUsers = findViewById(R.id.recycler_users);
        mRvShareUsers.setLayoutManager(new LinearLayoutManager(this, GridLayoutManager.HORIZONTAL, false));
        mapView = findViewById(id.map_view);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.setZoom(14);
        findViewById(R.id.float_btn_center).setOnClickListener(this);
        mBmb = findViewById(R.id.bmb_menu);
        initBmb();
        findViewById(R.id.fl_trigger_user_list).setOnClickListener(this);
        mapView.setOnTouchListener(new MyOnTouchListener());
        mapView.getUiSettings().setLogoPosition(UiSettings.LOGO_POSITION_LEFT_TOP);
        map.setOnMarkerClickListener(new TencentMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                UserInfo userInfo = (UserInfo) marker.getTag();
                if (userInfo != null) {
                    showUserInfoPop(userInfo);
                }
                return true;
            }
        });

        mTencentSearch = new TencentSearch(this);
    }

    /**
     * 初始化bmb菜单
     */
    private void initBmb() {

        //菜单图标资源
        int[] normalImageResIds = {R.drawable.portrait, R.drawable.list, R.drawable.groups, R.drawable.shutdown};
        //菜单标题资源
        int[] normalTextResIds = {R.string.name, R.string.bmb_group, R.string.bmb_groups, R.string.bmb_shutdown};
        //菜单小标题资源
        final int[] subTextResIds = {R.string.parser_address, R.string.no_group, R.string.bmb_groups_info, R.string.bmb_shutdown_info};

        //处理bmb菜单的点击事件
        OnBMClickListener listener = new OnBMClickListener() {
            @Override
            public void onBoomButtonClick(int index) {
                setRvShareUsersGone();
                switch (index) {
                    case 0:
                        UserActivity.show(MainActivity.this);
                        break;
                    case 1:
                        showGroupPop();
                        break;
                    case 2:
                        showGroupsPop();
                        break;
                    case 3:
                        //shutdown all
                        if (shareService != null) {
                            shareService.stopSelf();
                        }
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                }
            }
        };
        //10dp -->对应的px
        int padding = (int) Ui.dipToPx(getResources(), 10);

        //初始化菜单的每一项
        for (int i = 0; i < mBmb.getPiecePlaceEnum().pieceNumber(); i++) {
            final HamButton.Builder builder = new HamButton.Builder();
            builder.listener(listener);
            builder.normalText(getString(normalTextResIds[i]));
            builder.imagePadding(new Rect(padding, padding, padding, padding));
            builder.textPadding(new Rect(0, -padding, 0, 0));
            builder.subNormalText(getString(subTextResIds[i]));
            if (i != 0) {
                builder.normalImageRes(normalImageResIds[i]);
            }
            mBmb.addBuilder(builder);
        }
    }

    private void setHamButtonBuilderImage(final HamButton.Builder builder) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.portrait)
                .centerCrop()
                .circleCrop();
        Glide.with(this)
                .asBitmap()
                .apply(options)
                .load(Util.getImage())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), resource);
                        builder.normalImageDrawable(drawable);
                    }
                });

    }

    @Override
    public void onGroupInfoListResp(final Group group) {
        List<UserInfo> userInfoList;
        if (group == null
                || (userInfoList = group.getUserInfoList()) == null
                || userInfoList.size() == 0) {
            return;
        }
        if (mAdapter == null) {
            mAdapter = new Adapter();
            mAdapter.replace(userInfoList);
            mAdapter.setListener(new RecyclerAdapter.AdapterListener<UserInfo>() {
                @Override
                public void onItemClick(RecyclerAdapter.ViewHolder<UserInfo> holder, UserInfo userInfo, int position) {
                    final LatLng latLng = new LatLng(userInfo.getLatitude(), userInfo.getLongitude());
                    map.setCenter(latLng);
                    addMarker(userInfo);
                }

                @Override
                public void onItemLongClick(RecyclerAdapter.ViewHolder<UserInfo> holder, UserInfo userInfo, int position) {
                    showUserInfoPop(userInfo);
                }
            });
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    mRvShareUsers.setAdapter(mAdapter);
                }
            });
        } else {
            //异步调用mRvShareUsers刷新视图 避免没有发生变换的position无故刷新
            new RefreshUi(group).execute();
        }
    }

    /**
     * 拖动地图时隐藏mRvShareUsers
     */
    class MyOnTouchListener implements View.OnTouchListener {
        private ShowRunnable showRunnable;

        class ShowRunnable implements Runnable {
            private static final int T = 6;
            private volatile int i;

            private void reset() {
                i = T;
            }

            private void start() {
                i = 0;
            }

            private boolean starting() {
                return i == 0;
            }

            @Override
            public void run() {
                while (--i > 1) try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        setRvShareUsersShow();
                    }
                });
            }
        }


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mRvShareUsers.getVisibility() == View.VISIBLE) {
                        setRvShareUsersGone();
                        if (showRunnable == null) {
                            showRunnable = new ShowRunnable();
                        }
                        showRunnable.start();
                    } else if (showRunnable != null && !showRunnable.starting()) {
                        showRunnable.reset();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (showRunnable != null && showRunnable.starting()) {
                        showRunnable.reset();
                        new Thread(showRunnable).start();
                    }
                    break;
            }
            return false;
        }
    }

    private void initService() {
        bindService(
                new Intent(MainActivity.this, ShareService.class),
                MainActivity.this,
                ShareService.BIND_AUTO_CREATE);
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }


    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        if (shareService != null) {
            unbindService(this);
        }
        ShareHandler.removeOnDataChangeListener(this);
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
        initService();
        addMarker(Util.getUserInfo());
        HamButton.Builder builder = (HamButton.Builder) mBmb.getBuilder(0);
        setHamButtonBuilderImage(builder);
        builder.normalText(Util.getName());
    }


    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        mapView.onRestart();
        super.onRestart();

    }

    /**
     * 当service bind时call
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        shareService = ((ShareService.MyBinder) service).getShareService();
        shareService.setOnLocationChanged(this);
        ShareHandler.addOnDataChangeListener(this);
    }

    /**
     * unbindService时call
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (shareService != null) {
            ShareHandler.removeOnDataChangeListener(this);
        }
        shareService = null;
    }

    /**
     * 当位置改变时call
     */
    @Override
    public void onLocationChanged() {
        UserInfo userInfo = Util.getUserInfo();
        if (mBmb.isBoomed() && AddressCounter++ % 8 == 0) {
            setAddress(userInfo, new OnAddressGetListener() {
                @Override
                public void onGet(String addressOrError) {
                    ((HamButton.Builder) mBmb.getBuilder(0)).subNormalText(addressOrError);
                }
            });
        }
        if (map != null && isFirstCenter && userInfo.getLatitude() > 0) {
            setCenter();
            isFirstCenter = false;
        }
    }


    /**
     * 回到你的位置
     */
    private void setCenter() {
        UserInfo userInfo = Util.getUserInfo();
        final LatLng latLng = new LatLng(userInfo.getLatitude(), userInfo.getLongitude());
        map.setCenter(latLng);
        addMarker(userInfo);
    }

    /**
     * 添加对应userInfo的marker到
     * map和markerHashMap上如果marker存在则刷新marker.
     */
    private void addMarker(UserInfo userInfo) {
        Marker marker = markerHashMap.get(userInfo.getUuid());
        if (marker == null) {
            MarkerOptions options = new MarkerOptions();
            @SuppressLint("InflateParams") LinearLayout inflate =
                    (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(layout.marker_layout, null);
            PortraitView portraitView = inflate.findViewById(R.id.portrait);
            portraitView.setup(this, userInfo.getImage());
            options.markerView(inflate);
            if (userInfo.getUuid().equals(Util.getUUID())) {
                portraitView.setBackgroundResource(R.drawable.site);
            }
            marker = map.addMarker(options);
            markerHashMap.put(userInfo.getUuid(), marker);
        }
        marker.setTag(userInfo);
        LatLng position = marker.getPosition();
        if (position == null || position.getLatitude() != userInfo.getLatitude()
                || position.getLongitude() != userInfo.getLongitude()) {
            marker.setPosition(new LatLng(userInfo.getLatitude(), userInfo.getLongitude()));
        }

        PortraitView portraitView = marker.getMarkerView().findViewById(id.portrait);
        if (!portraitView.getImage().equals(userInfo.getImage())) {
            portraitView.setup(this, userInfo.getImage());
        }
    }


    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    /**
     * 显示分享用户的信息.
     *
     * @param userInfo
     */
    @SuppressWarnings("JavaDoc")
    private void showUserInfoPop(UserInfo userInfo) {
        //隐藏掉顶部RecyclerView
        setBackgroundAlpha(0.7f);

        //设置contentView
        @SuppressLint("InflateParams") View contentView = getLayoutInflater().inflate(R.layout.pop_view, null);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(contentView);
        //设置各个控件的点击响应
        TextView tvName = contentView.findViewById(id.tv_name);
        final TextView tvAddress = contentView.findViewById(id.tv_address);
        //判断网络状态
        if (Util.getNetType() == -1) {
            tvAddress.setText(R.string.network_error);
        } else {
            tvAddress.setText(R.string.parser_address);
        }
        PortraitView portrait = contentView.findViewById(id.portrait);
        setAddress(userInfo, new OnAddressGetListener() {
            @Override
            public void onGet(String addressOrError) {
                tvAddress.setText(addressOrError);
            }
        });
        contentView.findViewById(id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        tvName.setText(userInfo.getName());
        portrait.setup(this, userInfo.getImage());
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1f);
            }
        });
        popupWindow.showAtLocation(mRoot, Gravity.CENTER, 0, -(int) Ui.dipToPx(getResources(), 50f));
    }

    @SuppressWarnings("JavaDoc")
    private void showGroupsPop() {
        //隐藏掉顶部RecyclerView
        setRvShareUsersGone();
        setBackgroundAlpha(0.7f);

        //设置contentView
        @SuppressLint("InflateParams") View contentView = getLayoutInflater().inflate(R.layout.pop_groups, null);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(contentView);

        final Loading loading = contentView.findViewById(R.id.loading);
        final TextView tvGroupCount = contentView.findViewById(R.id.tv_group_count);
        final TextView tvUserCount = contentView.findViewById(R.id.tv_user_count);
        final RecyclerView rvGroups = contentView.findViewById(R.id.rv_groups);
        final TextView tvError = contentView.findViewById(R.id.tv_error);
        final ReqModel reqModel = new ReqModel();
        reqModel.setCode(ReqModel.CODE_GET_GROUPS);
        loading.start();
        ShareHandler.req(reqModel, new ReqModel.OnReqListener() {
            private byte inGroupId;
            private Adapter adapter = new Adapter();

            @Override
            public void onSucceed(final RespModel respModel) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        Object result = respModel.getResult();
                        hideLoading();
                        if (result instanceof Groups) {
                            Groups groups = (Groups) result;
                            inGroupId = groups.getInGroupID();
                            System.out.println("--------Groups--------");
                            tvUserCount.setText(String.format(getString(R.string.user_count), groups.getAllCount()));
                            HashSet<Groups.GroupSimpleInfo> simpleInfoSet = groups.getGroupSimpleInfo();
                            tvGroupCount.setText(String.format(getString(R.string.group_count),
                                    simpleInfoSet == null ? 0 : simpleInfoSet.size()));
                            rvGroups.setVisibility(View.VISIBLE);
                            rvGroups.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                            rvGroups.setAdapter(adapter);
                            adapter.replace(simpleInfoSet);
                        } else {
                            showError(R.string.unknown_error);
                        }

                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        hideLoading();
                        showError(R.string.network_error);
                    }
                });
            }

            private void showError(@StringRes int stringId) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(stringId);
            }

            private void hideLoading() {
                loading.stop();
                ((View) loading.getParent()).setVisibility(View.GONE);
            }

            class Adapter extends RecyclerAdapter<Groups.GroupSimpleInfo> {

                @Override
                public int getItemViewType(int position, Groups.GroupSimpleInfo groupSimpleInfo) {
                    return R.layout.cell_group_simple;
                }

                @Override
                protected ViewHolder<Groups.GroupSimpleInfo> onCreateViewHolder(View root, int viewType) {
                    return new MyViewHolder(root);
                }
            }

            class MyViewHolder extends RecyclerAdapter.ViewHolder<Groups.GroupSimpleInfo> {

                private TextView tvGroupID;
                private TextView tvGroupCount;
                private TextView tvUserNames;
                private TextView tvJoin;

                private MyViewHolder(View itemView) {
                    super(itemView);
                    tvGroupID = itemView.findViewById(R.id.tv_group_id);
                    tvGroupCount = itemView.findViewById(R.id.tv_user_count);
                    tvUserNames = itemView.findViewById(R.id.tv_user_names);
                    tvJoin = itemView.findViewById(R.id.tv_join);
                }

                @Override
                protected void onBind(Groups.GroupSimpleInfo groupSimpleInfo) {
                    tvGroupID.setText(String.format(getString(R.string.id), groupSimpleInfo.getGroupId()));
                    tvJoin.setText(inGroupId == groupSimpleInfo.getGroupId() ? R.string.in_the_group : R.string.join_the_group);
                    tvGroupCount.setText(String.format(getString(R.string.user_count), groupSimpleInfo.getGroupCount()));
                    tvUserNames.setText(groupSimpleInfo.getNames());
                }

            }
        });

        contentView.findViewById(id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1f);
            }
        });
        popupWindow.showAtLocation(mRoot, Gravity.CENTER, 0, -(int) Ui.dipToPx(getResources(), 50f));
    }

    /**
     * 显示所在组信息
     */
    private void showGroupPop() {
        if (shareService == null) {
            return;
        }
        //隐藏掉顶部RecyclerView
        setBackgroundAlpha(0.7f);

        //设置contentView
        @SuppressLint("InflateParams") View contentView = getLayoutInflater().inflate(R.layout.pop_group_layout, null);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(contentView);
        //设置各个控件的点击响应
        final TextView tvGroupID = contentView.findViewById(R.id.tv_group_id);
        final TextView tvGroupCount = contentView.findViewById(R.id.tv_group_count);
        final RecyclerView rvUsers = contentView.findViewById(R.id.rv_users);
        final Loading loading = contentView.findViewById(id.loading);
        loading.start();
        final ShareHandler.OnGroupInfoListRespListener listener = new ShareHandler.OnGroupInfoListRespListener() {

            @Override
            public void onGroupInfoListResp(final Group group) {
                if (group == null) {
                    return;
                }

                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        ((View) loading.getParent()).setVisibility(View.GONE);
                        rvUsers.setVisibility(View.VISIBLE);
                        tvGroupID.setText(String.format(getString(R.string.id), group.getGroupID()));
                        List<UserInfo> userInfoList = group.getUserInfoList();
                        tvGroupCount.setText(String.format(getString(R.string.user_count), userInfoList.size()));
                        if (adapter == null) {
                            adapter = new Adapter();
                            adapter.setListener(new RecyclerAdapter.AdapterListenerImpl<UserInfo>() {
                                @Override
                                public void onItemClick(RecyclerAdapter.ViewHolder<UserInfo> holder, UserInfo userInfo, int position) {
                                    popupWindow.dismiss();
                                    showUserInfoPop(userInfo);
                                }
                            });
                            rvUsers.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                            rvUsers.setAdapter(adapter);
                        }
                        if (refreshCounter++ % 15 == 0) {
                            adapter.replace(userInfoList);
                        }
                    }
                });
            }

            private Adapter adapter;

            private int refreshCounter = 0;

            class Adapter extends RecyclerAdapter<UserInfo> {

                @Override
                public int getItemViewType(int position, UserInfo userInfo) {
                    return R.layout.cell_user_layout;
                }

                @Override
                protected ViewHolder<UserInfo> onCreateViewHolder(View root, int viewType) {
                    return new MyViewHolder(root);
                }
            }

            class MyViewHolder extends RecyclerAdapter.ViewHolder<UserInfo> {

                private TextView tvName;

                private PortraitView portrait;

                private TextView tvAddress;

                MyViewHolder(View itemView) {
                    super(itemView);
                    portrait = itemView.findViewById(R.id.portrait);
                    tvName = itemView.findViewById(R.id.tv_name);
                    tvAddress = itemView.findViewById(R.id.tv_address);
                }

                @Override
                protected void onBind(UserInfo userInfo) {
                    if (!UserInfo.checkStatus(userInfo)) {
                        return;
                    }
                    portrait.setup(MainActivity.this, userInfo.getImage());
                    tvName.setText(userInfo.getName());
                    setAddress(userInfo, new OnAddressGetListener() {
                        @Override
                        public void onGet(String addressOrError) {
                            tvAddress.setText(addressOrError);
                        }
                    });
                }
            }

        };
        ShareHandler.addOnDataChangeListener(listener);

        contentView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1f);
                ShareHandler.removeOnDataChangeListener(listener);
            }
        });

        popupWindow.showAtLocation(mRoot, Gravity.CENTER, 0, -(int) Ui.dipToPx(getResources(), 50f));
    }

    /**
     * 解析userInfo的地址信息并回调给OnAddressGet.
     */
    public void setAddress(UserInfo userInfo, @NonNull final OnAddressGetListener onAddressGetListener) {
        Geo2AddressParam param = new Geo2AddressParam().location(new Location()
                .lat((float) userInfo.getLatitude()).lng((float) userInfo.getLongitude()));
        mTencentSearch.geo2address(param, new HttpResponseListener() {
            @Override
            public void onSuccess(int i, BaseObject baseObject) {
                Geo2AddressResultObject oj = (Geo2AddressResultObject) baseObject;
                onAddressGetListener.onGet(oj.result.address);
            }

            @Override
            public void onFailure(int i, String s, Throwable throwable) {
                onAddressGetListener.onGet(s);
            }
        });
    }

    /**
     * 检索地址的回调
     */
    interface OnAddressGetListener {
        void onGet(String addressOrError);

    }

    /**
     * 完整的一出某个离线userInfo对应的marker
     */
    private void removeMarker(String uuid) {
        Marker marker = markerHashMap.get(uuid);
        if (marker != null) {
            marker.remove();
            markerHashMap.remove(uuid);
        }
    }


    /**
     * 处理点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_btn_center:
                UserInfo userInfo = Util.getUserInfo();
                if (userInfo != null && userInfo.getLatitude() > 0 && userInfo.getLongitude() > 0) {
                    setCenter();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("定位失败")
                            .setMessage("请检查网络和定位服务是否开启!")
                            .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
                break;
            case R.id.fl_trigger_user_list:
                triggerRvShareUsersVisibility();
                break;
        }
    }

    /**
     * 切换mRvShareUsers显示状态
     */
    private void triggerRvShareUsersVisibility() {
        if (mRvShareUsers.getVisibility() == View.VISIBLE) {
            setRvShareUsersGone();
        } else {
            setRvShareUsersShow();
        }
    }


    private void setRvShareUsersShow() {
        mRvShareUsers.setVisibility(View.VISIBLE);
        tvGroupSimpleInfo.setVisibility(View.GONE);
    }

    private void setRvShareUsersGone() {
        mRvShareUsers.setVisibility(View.GONE);
        tvGroupSimpleInfo.setVisibility(View.VISIBLE);
    }


    /**
     * 刷新UI内容
     */
    @SuppressLint("StaticFieldLeak")
    class RefreshUi extends AsyncTask<Void, Void, DiffUtil.DiffResult> {
        private Group group;

        RefreshUi(Group group) {
            this.group = group;
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(Void... voids) {
            //获取前后两次数据有什么不同的地方
            List<UserInfo> infoList = group.getUserInfoList();
            if (infoList == null) {
                return null;
            }
            DiffUiDataCallback<UserInfo> callback
                    = new DiffUiDataCallback<>(mAdapter.getDataList(), infoList);
            return DiffUtil.calculateDiff(callback);
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            List<UserInfo> userInfoList = group.getUserInfoList();
            List<String> keys = new ArrayList<>();
            for (String key : markerHashMap.keySet()) {
                boolean active = false;
                for (UserInfo info : userInfoList) {
                    if (key.equals(info.getUuid())) {
                        active = true;
                        break;
                    }
                }
                if (!active) {
                    keys.add(key);
                }
            }

            String info = String.format(
                    getText(R.string.group_info).toString(),
                    group.getGroupID(),
                    userInfoList.size());

            //刷新简单组信息
            tvGroupSimpleInfo.setText(info);
            ((HamButton.Builder) mBmb.getBuilder(1)).subNormalText(info);
            //移除掉所有已经离线的分享信息marker
            for (String key : keys) {
                removeMarker(key);
            }
            for (UserInfo model : userInfoList) {
                if (UserInfo.checkStatus(model)) {
                    addMarker(model);
                }
            }
            mAdapter.replaceAllData(userInfoList, false);

            //精确刷新mRvShareUsers
            if (diffResult != null) {
                diffResult.dispatchUpdatesTo(mAdapter);
            }
        }

    }


    class Adapter extends RecyclerAdapter<UserInfo> {

        @Override
        public int getItemViewType(int position, UserInfo userInfo) {
            return R.layout.cell_users_portrait;
        }

        @Override
        protected ViewHolder<UserInfo> onCreateViewHolder(View root, int viewType) {
            return new MainActivity.ViewHolder(root);
        }
    }

    class ViewHolder extends RecyclerAdapter.ViewHolder<UserInfo> {

        private final int BLUE_500 = UiCompat.getColor(getResources(), R.color.blue_500);
        private final int YELLOW_900 = UiCompat.getColor(getResources(), R.color.yellow_900);
        PortraitView portrait;
        TextView tvName;


        ViewHolder(View itemView) {
            super(itemView);
            portrait = itemView.findViewById(id.portrait);
            tvName = itemView.findViewById(id.tv_name);
        }

        @Override
        protected void onBind(UserInfo userInfo) {
            if (userInfo == null) {
                return;
            }
            tvName.setText(userInfo.getName());
            if (userInfo.getUuid().equals(Util.getUUID())) {
                tvName.setTextColor(BLUE_500);
            } else {
                tvName.setTextColor(YELLOW_900);
            }
            portrait.setup(MainActivity.this, userInfo.getImage());
        }
    }
}
