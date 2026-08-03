package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder<T extends net.minecraft.network.PacketListener> extends MessageToByteEncoder<Packet<T>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final net.minecraft.network.ProtocolInfo<T> protocolInfo;

   public PacketEncoder(net.minecraft.network.ProtocolInfo<T> $$0) {
      this.protocolInfo = $$0;
   }

   protected void encode(ChannelHandlerContext $$0, Packet<T> $$1, ByteBuf $$2) throws Exception {
      PacketType<? extends Packet<? super T>> $$3 = $$1.type();

      try {
         this.protocolInfo.codec().encode($$2, $$1);
         int $$4 = $$2.readableBytes();
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
               net.minecraft.network.Connection.PACKET_SENT_MARKER,
               "OUT: [{}:{}] {} -> {} bytes",
               new Object[]{this.protocolInfo.id().id(), $$3, $$1.getClass().getName(), $$4}
            );
         }

         JvmProfiler.INSTANCE.onPacketSent(this.protocolInfo.id(), $$3, $$0.channel().remoteAddress(), $$4);
      } catch (Throwable var9) {
         LOGGER.error("Error sending packet {}", $$3, var9);
         if ($$1.isSkippable()) {
            throw new net.minecraft.network.SkipPacketEncoderException(var9);
         }

         throw var9;
      } finally {
         net.minecraft.network.ProtocolSwapHandler.handleOutboundTerminalPacket($$0, $$1);
      }
   }
}
