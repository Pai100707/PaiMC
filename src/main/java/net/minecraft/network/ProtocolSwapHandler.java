package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {
   static void handleInboundTerminalPacket(ChannelHandlerContext $$0, Packet<?> $$1) {
      if ($$1.isTerminal()) {
         $$0.channel().config().setAutoRead(false);
         $$0.pipeline().addBefore($$0.name(), "inbound_config", new net.minecraft.network.UnconfiguredPipelineHandler.Inbound());
         $$0.pipeline().remove($$0.name());
      }
   }

   static void handleOutboundTerminalPacket(ChannelHandlerContext $$0, Packet<?> $$1) {
      if ($$1.isTerminal()) {
         $$0.pipeline().addAfter($$0.name(), "outbound_config", new net.minecraft.network.UnconfiguredPipelineHandler.Outbound());
         $$0.pipeline().remove($$0.name());
      }
   }
}
