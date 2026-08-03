package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LocalFrameDecoder extends ChannelInboundHandlerAdapter {
   public void channelRead(ChannelHandlerContext $$0, Object $$1) {
      $$0.fireChannelRead(net.minecraft.network.HiddenByteBuf.unpack($$1));
   }
}
