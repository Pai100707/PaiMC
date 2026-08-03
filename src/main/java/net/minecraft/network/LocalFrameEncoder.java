package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class LocalFrameEncoder extends ChannelOutboundHandlerAdapter {
   public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) {
      $$0.write(net.minecraft.network.HiddenByteBuf.pack($$1), $$2);
   }
}
