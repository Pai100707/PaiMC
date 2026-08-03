package net.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;

public class PacketBundleUnpacker extends MessageToMessageEncoder<Packet<?>> {
   private final BundlerInfo bundlerInfo;

   public PacketBundleUnpacker(BundlerInfo $$0) {
      this.bundlerInfo = $$0;
   }

   protected void encode(ChannelHandlerContext $$0, Packet<?> $$1, List<Object> $$2) throws Exception {
      this.bundlerInfo.unbundlePacket($$1, $$2::add);
      if ($$1.isTerminal()) {
         $$0.pipeline().remove($$0.name());
      }
   }
}
