package com.jiuli.local_share.network.nettysocket.message;


import java.util.HashSet;

public class Groups {

    private int allCount;
    private byte inGroupID;
    private int capacity;
    private HashSet<GroupSimpleInfo> groupSimpleInfo = new HashSet<>();

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }


    public HashSet<GroupSimpleInfo> getGroupSimpleInfo() {
        return groupSimpleInfo;
    }

    public void setGroupSimpleInfo(HashSet<GroupSimpleInfo> groupSimpleInfo) {
        this.groupSimpleInfo = groupSimpleInfo;
    }

    public byte getInGroupID() {
        return inGroupID;
    }

    public void setInGroupID(byte inGroupID) {
        this.inGroupID = inGroupID;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public static class GroupSimpleInfo {
        private byte groupId;
        private byte groupCount;
        private String names;

        GroupSimpleInfo(byte groupId, byte groupCount, String names) {
            this.groupId = groupId;
            this.groupCount = groupCount;
            this.names = names;
        }

        public byte getGroupCount() {
            return groupCount;
        }

        public void setGroupCount(byte groupCount) {
            this.groupCount = groupCount;
        }

        public String getNames() {
            return names;
        }

        public void setNames(String names) {
            this.names = names;
        }

        public byte getGroupId() {
            return groupId;
        }

        public void setGroupId(byte groupId) {
            this.groupId = groupId;
        }

    }
}
