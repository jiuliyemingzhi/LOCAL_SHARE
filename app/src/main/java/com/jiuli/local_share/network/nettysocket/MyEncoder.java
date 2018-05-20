package com.jiuli.local_share.network.nettysocket;

import com.jiuli.local_share.network.nettysocket.message.ReqModel;
import com.jiuli.local_share.network.nettysocket.message.TypeUtil;
import com.jiuli.local_share.util.Util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * //Created by r1907 on 2018/4/23.
 */

class MyEncoder extends MessageToByteEncoder<ReqModel> {


    @Override
    protected void encode(ChannelHandlerContext ctx, ReqModel msg, ByteBuf out) throws Exception {
        if (msg == null) {
            return;
        }

        if (msg.getType() == 0) {
            //noinspection unchecked
            msg.setContent(null);
        }

        String json = TypeUtil.getGson().toJson(msg);
        if (Util.stringIsEmpty(json)) {
            return;
        }
        byte[] bytes = json.getBytes(CharsetUtil.UTF_8);
        out.writeMedium(bytes.length + 1);
        out.writeByte(msg.getType());
        out.writeBytes(bytes);
//        System.out.println(msg.getType());
//        System.out.println(json);
    }
}
