package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.protocol.Packet;

public class UnconfiguredPipelineHandler {
   public static <T extends net.minecraft.network.PacketListener> net.minecraft.network.UnconfiguredPipelineHandler.InboundConfigurationTask setupInboundProtocol(
      net.minecraft.network.ProtocolInfo<T> $$0
   ) {
      return setupInboundHandler(new net.minecraft.network.PacketDecoder<T>($$0));
   }

   private static net.minecraft.network.UnconfiguredPipelineHandler.InboundConfigurationTask setupInboundHandler(ChannelInboundHandler $$0) {
      return $$1 -> {
         $$1.pipeline().replace($$1.name(), "decoder", $$0);
         $$1.channel().config().setAutoRead(true);
      };
   }

   public static <T extends net.minecraft.network.PacketListener> net.minecraft.network.UnconfiguredPipelineHandler.OutboundConfigurationTask setupOutboundProtocol(
      net.minecraft.network.ProtocolInfo<T> $$0
   ) {
      return setupOutboundHandler(new net.minecraft.network.PacketEncoder<T>($$0));
   }

   private static net.minecraft.network.UnconfiguredPipelineHandler.OutboundConfigurationTask setupOutboundHandler(ChannelOutboundHandler $$0) {
      return $$1 -> $$1.pipeline().replace($$1.name(), "encoder", $$0);
   }

   public static class Inbound extends ChannelDuplexHandler {
      public void channelRead(ChannelHandlerContext $$0, Object $$1) {
         if (!($$1 instanceof ByteBuf) && !($$1 instanceof Packet)) {
            $$0.fireChannelRead($$1);
         } else {
            ReferenceCountUtil.release($$1);
            throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + $$1);
         }
      }

      public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) throws Exception {
         if ($$1 instanceof net.minecraft.network.UnconfiguredPipelineHandler.InboundConfigurationTask $$3) {
            try {
               $$3.run($$0);
            } finally {
               ReferenceCountUtil.release($$1);
            }

            $$2.setSuccess();
         } else {
            $$0.write($$1, $$2);
         }
      }
   }

   @FunctionalInterface
   public interface InboundConfigurationTask {
      void run(ChannelHandlerContext var1);

      default net.minecraft.network.UnconfiguredPipelineHandler.InboundConfigurationTask andThen(
         net.minecraft.network.UnconfiguredPipelineHandler.InboundConfigurationTask $$0
      ) {
         return $$1 -> {
            this.run($$1);
            $$0.run($$1);
         };
      }
   }

   public static class Outbound extends ChannelOutboundHandlerAdapter {
      public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) throws Exception {
         if ($$1 instanceof Packet) {
            ReferenceCountUtil.release($$1);
            throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + $$1);
         } else {
            if ($$1 instanceof net.minecraft.network.UnconfiguredPipelineHandler.OutboundConfigurationTask $$3) {
               try {
                  $$3.run($$0);
               } finally {
                  ReferenceCountUtil.release($$1);
               }

               $$2.setSuccess();
            } else {
               $$0.write($$1, $$2);
            }
         }
      }
   }

   @FunctionalInterface
   public interface OutboundConfigurationTask {
      void run(ChannelHandlerContext var1);

      default net.minecraft.network.UnconfiguredPipelineHandler.OutboundConfigurationTask andThen(
         net.minecraft.network.UnconfiguredPipelineHandler.OutboundConfigurationTask $$0
      ) {
         return $$1 -> {
            this.run($$1);
            $$0.run($$1);
         };
      }
   }
}
