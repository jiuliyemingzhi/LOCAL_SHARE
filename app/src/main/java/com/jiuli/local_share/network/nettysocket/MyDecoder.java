package com.jiuli.local_share.network.nettysocket;

import com.jiuli.local_share.network.nettysocket.message.TypeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.CharsetUtil;



class MyDecoder extends LengthFieldBasedFrameDecoder {
    @SuppressWarnings("WeakerAccess")
    public MyDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buf = (ByteBuf) super.decode(ctx, in);
        if (buf == null) {
            return null;
        }

        buf.readMedium();
        byte type = buf.readByte();
        String json = buf.toString(CharsetUtil.UTF_8);
        System.out.println("readableBytes: " + buf.readableBytes());
        return TypeUtil.getGson().fromJson(json, TypeUtil.getRespType(type));
    }
}
