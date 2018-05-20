package com.jiuli.local_share.network.nettysocket;

import android.support.annotation.Nullable;

import com.jiuli.local_share.network.Common;
import com.jiuli.local_share.util.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * //Created by r1907 on 2018/4/23.
 */

public class ShareSocket {


    private static ExecutorService mExecutor;

    private static NioEventLoopGroup mLoopGroup;

    private static volatile boolean connected = false;


    public static void connect() {
        connect(null);
    }

    @SuppressWarnings("WeakerAccess")
    public synchronized static void connect(
            @Nullable final String message) {
        if (connected) {
            return;
        }
        connected = true;
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        if (!Util.stringIsEmpty(message)) {
            Util.onUiThreadShowToast(message);
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                NioEventLoopGroup workGroup = new NioEventLoopGroup();
                mLoopGroup = workGroup;
                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(workGroup)
                            .channel(NioSocketChannel.class)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .remoteAddress(Common.SOCKET_HOST, Common.SOCKET_PORT)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline()
                                            .addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS))
                                            .addLast(new MyDecoder(128 * 1024, 0, 3))
                                            .addLast(new MyEncoder())
                                            .addLast(new ShareHandler());
                                }
                            });

                    ChannelFuture future = bootstrap.connect().sync();
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    connected = false;
                    workGroup.shutdownGracefully();
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Util.getNetType() != -1) {
                        connect("正在重连!");
                    }
                }
            }
        });
    }

    public static synchronized void securityConnect() {
        if (connected) {
            return;
        }
        disconnect();
        connect("网络已经恢复,正在重连...");
    }


    public static void disconnect() {
        if (mLoopGroup != null) {
            mLoopGroup.shutdownGracefully();
            mLoopGroup = null;
        }

        try {
            if (mExecutor != null) {
                mExecutor.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        connected = false;
        mExecutor = null;
    }

    public static boolean isConnected() {
        return connected;
    }
}
