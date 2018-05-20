package com.jiuli.local_share.network.uploader;

public interface ProgressListener {
        void progress(float progress, long length, long fileLength);
    }