package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder<T extends net.minecraft.network.PacketListener> extends ByteToMessageDecoder implements net.minecraft.network.ProtocolSwapHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final net.minecraft.network.ProtocolInfo<T> protocolInfo;

   public PacketDecoder(net.minecraft.network.ProtocolInfo<T> $$0) {
      this.protocolInfo = $$0;
   }

   protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) throws Exception {
      int $$3 = $$1.readableBytes();

      Packet<? super T> $$4;
      try {
         $$4 = this.protocolInfo.codec().decode($$1);
      } catch (Exception var7) {
         if (var7 instanceof net.minecraft.network.SkipPacketException) {
            $$1.skipBytes($$1.readableBytes());
         }

         throw var7;
      }

      PacketType<? extends Packet<? super T>> $$7 = $$4.type();
      JvmProfiler.INSTANCE.onPacketReceived(this.protocolInfo.id(), $$7, $$0.channel().remoteAddress(), $$3);
      if ($$1.readableBytes() > 0) {
         throw new IOException(
            "Packet "
               + this.protocolInfo.id().id()
               + "/"
               + $$7
               + " ("
               + $$4.getClass().getSimpleName()
               + ") was larger than I expected, found "
               + $$1.readableBytes()
               + " bytes extra whilst reading packet "
               + $$7
         );
      } else {
         $$2.add($$4);
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
               net.minecraft.network.Connection.PACKET_RECEIVED_MARKER,
               " IN: [{}:{}] {} -> {} bytes",
               new Object[]{this.protocolInfo.id().id(), $$7, $$4.getClass().getName(), $$3}
            );
         }

         net.minecraft.network.ProtocolSwapHandler.handleInboundTerminalPacket($$0, $$4);
      }
   }
}
