package com.jiuli.local_share.network.nettysocket.message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

@SuppressWarnings("WeakerAccess")
public class TypeUtil {
    //resp result类型
    public static final byte GROUP = 1;
    public static final byte JOIN = 2;
    public static final byte GROUPS = 3;


    //resp Type
    public static Type TYPE_GROUP;
    public static Type TYPE_JOIN;
    public static Type TYPE_GROUPS;

    //req content类型
    public static final byte USER_INFO = 1;


    public static Type TYPE_NORMAL;

    private static Gson gson;


    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }


    /**
     * 获取resp Type
     *
     * @param type
     * @return
     */
    public static Type getRespType(byte type) {
        switch (type) {
            case GROUP:
                if (TYPE_GROUP == null) {
                    TYPE_GROUP = new TypeToken<RespModel<Group>>() {
                    }.getType();
                }
                return TYPE_GROUP;
            case JOIN:
                if (TYPE_JOIN == null) {
                    TYPE_JOIN = new TypeToken<RespModel<Group>>() {
                    }.getType();
                }
                return TYPE_JOIN;
            case GROUPS:
                if (TYPE_GROUPS == null) {
                    TYPE_GROUPS = new TypeToken<RespModel<Groups>>() {
                    }.getType();
                }
                return TYPE_GROUPS;
        }
        return TYPE_NORMAL == null ? TYPE_NORMAL = RespModel.class : TYPE_NORMAL;
    }

}
