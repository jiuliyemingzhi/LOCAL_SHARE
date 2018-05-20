package com.jiuli.local_share.network.nettysocket;

import android.support.annotation.NonNull;

import com.jiuli.local_share.network.nettysocket.message.Group;
import com.jiuli.local_share.network.nettysocket.message.ReqModel;
import com.jiuli.local_share.network.nettysocket.message.RespModel;
import com.jiuli.local_share.util.Util;

import java.util.Vector;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * //Created by r1907 on 2018/4/23.
 */

@ChannelHandler.Sharable
public class ShareHandler extends ChannelInboundHandlerAdapter {

    private static final ReqModel HEARTBEAT = new ReqModel();

    private volatile static int sessionID = 1;

    private static ChannelHandlerContext mCtx;

    private int currentTime = 0;

    private static final int TRY_TIMES = 2;

    private static final Vector<OnGroupInfoListRespListener> listRespListeners = new Vector<>();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.READER_IDLE) {
            if (currentTime++ < TRY_TIMES) {
                currentTime++;
                ctx.channel().writeAndFlush(HEARTBEAT);
            } else {
                close();
            }
        }
    }

    private void login(ChannelHandlerContext ctx) {
        ReqModel<Object> reqModel = new ReqModel<>();
        reqModel.setCode(ReqModel.CODE_LOGIN);
        reqModel.setKEY(Util.KEY);
        ctx.channel().writeAndFlush(reqModel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        mCtx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        close();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        login(ctx);
    }


    public static void addOnDataChangeListener(OnGroupInfoListRespListener listener) {
        listRespListeners.add(listener);
    }

    public static void removeOnDataChangeListener(OnGroupInfoListRespListener listener) {
        listRespListeners.remove(listener);
    }

    public interface OnGroupInfoListRespListener {
        void onGroupInfoListResp(Group group);

    }

    private static void close() {
        if (mCtx != null) {
            mCtx.close();
            mCtx = null;
        }
    }

    public static synchronized boolean isReady() {
        return mCtx != null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        currentTime = 0;
        mCtx = ctx;
        if (msg instanceof RespModel) {
            RespModel respModel = (RespModel) msg;
            if (respModel.getSessionID() == 0 && respModel.getResult() != null) {
                Group group = ((Group) respModel.getResult());
                if (listRespListeners.size() > 0) {
                    for (OnGroupInfoListRespListener listener : listRespListeners) {
                        if (listener != null) {
                            listener.onGroupInfoListResp(group);
                        }
                    }
                }
            }

            if (respModel.getSessionID() > 0) {
                ReqListenerManager.onSucceed(respModel);
            }
        }

    }

    public static void req(@NonNull ReqModel reqModel, ReqModel.OnReqListener reqListener) {
        if (mCtx == null) {
            if (reqListener != null) {
                reqListener.onError(ReqModel.OnReqListener.ERROR_OTHER);
            }
            return;
        }

        if (reqListener == null) {
            //所有的分享消息都不需要处理,不用添加到ReqListenerManager中
            reqModel.setSessionID(0);
        } else {
            int sessionID = getSessionID();
            reqModel.setSessionID(sessionID);
            ReqListenerManager.put(sessionID, reqListener);
        }
        try {
            mCtx.writeAndFlush(reqModel);
        } catch (Exception e) {
            if (reqListener != null) {
                reqListener.onError(ReqModel.OnReqListener.ERROR_NETWORK_ERROR);
            }
        }
    }

    /**
     * 在timeout时间内最多也不可能处理超1000个连接
     */

    private static synchronized int getSessionID() {
        if (sessionID > 1000) {
            sessionID = 1;
        }
        return sessionID++;
    }


}
