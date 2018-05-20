package com.jiuli.local_share.network.nettysocket.message;

import java.util.List;

/**
 * ..Created by jiuli on 2018/5/5.
 */

public class Group {

    private  byte groupID;
    private List<UserInfo> userInfoList;


    public Group(byte groupID, List<UserInfo> userInfoList) {
        this.groupID = groupID;
        this.userInfoList = userInfoList;
    }

    public byte getGroupID() {
        return groupID;
    }


    public void setGroupID(byte groupID) {
        this.groupID = groupID;
    }

    public List<UserInfo> getUserInfoList() {
        return userInfoList;
    }

    public void setUserInfoList(List<UserInfo> userInfoList) {
        this.userInfoList = userInfoList;
    }
}
