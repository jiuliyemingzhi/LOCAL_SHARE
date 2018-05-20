package com.jiuli.local_share.network.uploader;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jiuli.local_share.network.NetWork;
import com.jiuli.local_share.network.RemoteNet;
import com.jiuli.local_share.network.uploader.model.ResponseModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * //Created by r1907 on 2018/4/19.
 */

public class FileUploader {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyyMM");//时间格式化

    private static final String KEY = "4b1c3b41-0f70-48ba-8347-efac7daeb8bf";

    public static final String REPOSITORY_IMAGE = "image";//图片仓库

    public static final String REPOSITORY_FILE = "file";//文件仓库

    public static final String REPOSITORY_PORTRAIT = "portrait";//头像仓库

    private static final int CHUNK_SIZE = 1024 * 128;//字节块大小


    /**
     * 文件上传
     */
    public static final void fileUpload(@NonNull  final File file,
                                        @NonNull  final String repository,
                                        @NonNull  final Callback<ResponseModel<String>> callback,
                                        @Nullable final ProgressListener progressListener) throws Exception {

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        if (!file.canRead()) {
            throw new Exception("文件不可用!");
        }


        //获取retrofit
        Retrofit retrofit = NetWork.getRetrofit();
        RemoteNet remoteNet = retrofit.create(RemoteNet.class);

        // RequestBody requestBody = RequestBody.create(MediaType.parse(getMimeType(file.getPath())), file);

        MultipartBody.Part filePart = MultipartBody
                .Part
                .createFormData(
                        KEY,
                        getFileObjKey(file, repository),
                        createRequestBody(file, progressListener));
        remoteNet.uploadFile(filePart)
                .enqueue(callback);
    }


    /**
     * Returns a new request body that transmits the content of {@code file}.
     * 让requestBody支持块和进度
     */

    public static RequestBody createRequestBody(final File file, final ProgressListener progressListener) {
        if (file == null) throw new NullPointerException("file == null");

        return new RequestBody() {
            private long fileLen = 0;

            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse(getMimeType(file.getPath()));
            }

            @Override
            public long contentLength() throws IOException {
                return fileLen == 0 ? fileLen = file.length() : fileLen;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                long contentLength = contentLength();
                long length = 0;
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = new byte[CHUNK_SIZE];
                    int len;
                    while ((len = fis.read(bytes)) != -1) {
                        sink.write(bytes, 0, len);
                        length += len;
                        if (progressListener != null) {
                            progressListener.progress(length / (contentLength + 0f), length, contentLength);
                        }
                    }
                } finally {
                    sink.flush();
                }
            }
        };
    }

    /**
     * 将当前时间转化为字符串 格式为yyyyMM
     *
     * @return
     */
    private static String getDateString() {
        return bartDateFormat.format(new Date());
    }


    /**
     * 获取文件在服务器下的相对路径
     *
     * @param file
     * @param repository
     * @return
     */

    private static String getFileObjKey(File file, String repository) {
        String fileMd5 = getMD5String(file);
        String name = file.getName();
        return String.format("/%s/%s/%s%s",
                repository,
                getDateString(),
                fileMd5,
                name.substring(name.lastIndexOf(".")));
    }

    /**
     * 获取 MD5
     *
     * @param file
     * @return
     */
    public static String getMD5String(File file) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        InputStream is = null;
        byte[] bytes = new byte[10240];
        try {
            is = new FileInputStream(file);
            for (int readLen; (readLen = is.read(bytes)) > 0; ) {
                md5.update(bytes, 0, readLen);
            }
            return convertToHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 将字节数组转化为16进制String
     *
     * @param bytes
     * @return
     */
    private static String convertToHexString(byte[] bytes) {
        char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b & 0xF0) >>> 4]);
            sb.append(HEX_DIGITS[b & 0x0F]);
        }
        return sb.toString();
    }

    /**
     * 获取文件的mimeType
     *
     * @param fileUrl
     * @return
     */
    public static String getMimeType(String fileUrl) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileUrl);
        return type;
    }
}
