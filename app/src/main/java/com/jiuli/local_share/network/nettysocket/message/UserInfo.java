package com.jiuli.local_share.network.nettysocket.message;

import com.jiuli.local_share.util.DiffUiDataCallback;
import com.jiuli.local_share.util.Util;

public class UserInfo extends DiffUiDataCallback<UserInfo>
        implements DiffUiDataCallback.UiDataDiffer<UserInfo> {

    private String name;
    private String uuid;
    private double longitude;
    private double latitude;
    private String image;


    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getImage() {
        return image;
    }

    public static boolean checkStatus(UserInfo userInfo) {
        return !Util.stringIsEmpty(userInfo.getImage())
                && !Util.stringIsEmpty(userInfo.getName())
                && !Util.stringIsEmpty(userInfo.getUuid());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean isSame(UserInfo old) {
        return old != null
                && (uuid == null ? old.uuid == null : uuid.equals(old.uuid));
    }

    @Override
    public boolean isUiContentSame(UserInfo old) {
        return this == old || old != null
                && (uuid != null ? uuid.equals(old.uuid) : old.uuid == null)
                && (name != null ? name.equals(old.name) : old.name == null)
                && (image != null ? image.equals(old.image) : old.image == null);
    }
}
