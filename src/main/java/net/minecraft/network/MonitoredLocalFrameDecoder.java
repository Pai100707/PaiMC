package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MonitoredLocalFrameDecoder extends ChannelInboundHandlerAdapter {
   private final net.minecraft.network.BandwidthDebugMonitor monitor;

   public MonitoredLocalFrameDecoder(net.minecraft.network.BandwidthDebugMonitor $$0) {
      this.monitor = $$0;
   }

   public void channelRead(ChannelHandlerContext $$0, Object $$1) {
      $$1 = net.minecraft.network.HiddenByteBuf.unpack($$1);
      if ($$1 instanceof ByteBuf $$2) {
         this.monitor.onReceive($$2.readableBytes());
      }

      $$0.fireChannelRead($$1);
   }
}
