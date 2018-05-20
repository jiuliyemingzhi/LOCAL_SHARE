package com.jiuli.local_share.network;

import com.jiuli.local_share.network.uploader.model.ResponseModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * //Created by r1907 on 2018/4/19.
 */

public interface RemoteNet {
    @Multipart
    @POST("/upload")
    Call<ResponseModel<String>> uploadFile(
            @Part MultipartBody.Part file);

//    @Multipart
//    @POST("/upload")
//    Call<ResponseModel<List<String>>> uploadFiles(
//            @PartMap MultipartBody.Part files);
}
