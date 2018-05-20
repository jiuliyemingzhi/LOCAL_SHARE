package com.jiuli.local_share.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.jiuli.local_share.R;
import com.jiuli.local_share.fragment.GalleryFragment;
import com.jiuli.local_share.network.Common;
import com.jiuli.local_share.network.uploader.FileUploader;
import com.jiuli.local_share.network.uploader.ProgressListener;
import com.jiuli.local_share.network.uploader.model.ResponseModel;
import com.jiuli.local_share.util.Util;
import com.jiuli.local_share.view.PortraitView;
import com.yalantis.ucrop.UCrop;

import net.qiujuer.genius.ui.compat.UiCompat;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity
        implements View.OnClickListener, GalleryFragment.OnSelectedListener {


    public final String TAG = UserActivity.class.getSimpleName();

    private String mPortraitPath;

    private EditText etName;

    private PortraitView mPortrait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_submit).setOnClickListener(this);
        findViewById(R.id.iv_submit).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        mPortrait = findViewById(R.id.portrait);
        mPortrait.setOnClickListener(this);
        etName = findViewById(R.id.et_name);
        if (!Util.stringIsEmpty(Util.getName())) {
            etName.setText(Util.getName());
        }
        if (!Util.stringIsEmpty(Util.getImage())) {
            mPortrait.setup(this, Util.getImage());
        }

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 12) {
                    etName.setText(s.subSequence(0, 12));
                    new AlertDialog.Builder(UserActivity.this)
                            .setTitle("敬告")
                            .setMessage("名字不能太长哦...")
                            .setNeutralButton("知道了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
            case R.id.iv_submit:
                final String name = etName.getText().toString();
                if (Util.stringIsEmpty(name)) {
                    Util.showToast("昵称和图片不能为空!!");
                }
                if (Util.stringIsEmpty(mPortraitPath)) {
                    if (!Util.stringIsEmpty(Util.getImage())) {
                        Util.setName(name);
                        MainActivity.show(this);
                    } else {
                        Util.showToast("昵称和图片不能为空!!");
                    }
                    return;
                }
                Util.setName(name);
                try {
                    uploadPortrait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.portrait:
                new GalleryFragment()
                        .setListener(this)
                        .show(getSupportFragmentManager(), GalleryFragment.class.getName());
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    private void hideSoftKeyBoard() {
        View view = getCurrentFocus();
        if (view == null) {
            return;
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void uploadPortrait() throws Exception {

        hideSoftKeyBoard();
        final long i = System.currentTimeMillis();
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("正在初始化!");
        dialog.setMessage("正在加载请稍后!");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMax(100);
        dialog.show();
        FileUploader.fileUpload(
                new File(mPortraitPath),
                FileUploader.REPOSITORY_PORTRAIT,
                new Callback<ResponseModel<String>>() {
                    @Override
                    public void onResponse(Call<ResponseModel<String>> call, Response<ResponseModel<String>> response) {
                        if (response != null && response.isSuccessful()) {
                            ResponseModel<String> responseModel = response.body();
                            if (responseModel != null && responseModel.success()) {
                                Util.setImage(Common.UPLOAD_API_URL + responseModel.getResult());
                                MainActivity.show(UserActivity.this);
                                Log.i(TAG, String.valueOf(System.currentTimeMillis() - i));
                                dialog.dismiss();
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseModel<String>> call, Throwable t) {
                        dialog.setMessage("网络错误");
                    }
                },
                new ProgressListener() {
                    @Override
                    public void progress(float progress, long length, long fileLength) {
//                        dialog.setProgress((int) (100 * progress));
                    }
                });
    }

    public static void show(Context context) {
        context.startActivity(new Intent(context, UserActivity.class));
    }

    @Override
    public void imageSelectChanged(String path) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setToolbarTitle(getString(R.string.portrait_crop));
        int color = UiCompat.getColor(getResources(), R.color.blue_300);
        options.setToolbarColor(color);
        options.setLogoColor(color);
        options.setStatusBarColor(color);
        options.setCropFrameColor(color);
        options.setActiveWidgetColor(color);
        options.setCompressionQuality(80);
        File dPath = Util.getPortraitTmpFile();
        UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(dPath))
                .withAspectRatio(1, 1)
                .withMaxResultSize(1024, 1024)
                .withOptions(options)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                mPortrait.setup(Glide.with(this), resultUri);
                mPortraitPath = resultUri.getPath();
                Log.e("TAG", "localPath" + mPortraitPath);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Util.showToast("未知错误!");
        }
    }
}
