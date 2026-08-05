/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelDuplexHandler
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandler
 *  io.netty.channel.ChannelOutboundHandler
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPromise
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  io.netty.util.ReferenceCountUtil
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;

public class UnconfiguredPipelineHandler {
    public static <T extends PacketListener> InboundConfigurationTask setupInboundProtocol(ProtocolInfo<T> $$0) {
        return UnconfiguredPipelineHandler.setupInboundHandler(new PacketDecoder<T>($$0));
    }

    private static InboundConfigurationTask setupInboundHandler(ChannelInboundHandler $$0) {
        return $$1 -> {
            $$1.pipeline().replace($$1.name(), "decoder", (ChannelHandler)$$0);
            $$1.channel().config().setAutoRead(true);
        };
    }

    public static <T extends PacketListener> OutboundConfigurationTask setupOutboundProtocol(ProtocolInfo<T> $$0) {
        return UnconfiguredPipelineHandler.setupOutboundHandler(new PacketEncoder<T>($$0));
    }

    private static OutboundConfigurationTask setupOutboundHandler(ChannelOutboundHandler $$0) {
        return $$1 -> $$1.pipeline().replace($$1.name(), "encoder", (ChannelHandler)$$0);
    }

    @FunctionalInterface
    public static interface InboundConfigurationTask {
        public void run(ChannelHandlerContext var1);

        default public InboundConfigurationTask andThen(InboundConfigurationTask $$0) {
            return $$1 -> {
                this.run($$1);
                $$0.run($$1);
            };
        }
    }

    @FunctionalInterface
    public static interface OutboundConfigurationTask {
        public void run(ChannelHandlerContext var1);

        default public OutboundConfigurationTask andThen(OutboundConfigurationTask $$0) {
            return $$1 -> {
                this.run($$1);
                $$0.run($$1);
            };
        }
    }

    public static class Outbound
    extends ChannelOutboundHandlerAdapter {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) throws Exception {
            if ($$1 instanceof Packet) {
                ReferenceCountUtil.release((Object)$$1);
                throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + String.valueOf($$1));
            }
            if ($$1 instanceof OutboundConfigurationTask) {
                OutboundConfigurationTask $$3 = (OutboundConfigurationTask)$$1;
                try {
                    $$3.run($$0);
                }
                finally {
                    ReferenceCountUtil.release((Object)$$1);
                }
                $$2.setSuccess();
            } else {
                $$0.write($$1, $$2);
            }
        }
    }

    public static class Inbound
    extends ChannelDuplexHandler {
        public void channelRead(ChannelHandlerContext $$0, Object $$1) {
            if ($$1 instanceof ByteBuf || $$1 instanceof Packet) {
                ReferenceCountUtil.release((Object)$$1);
                throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + String.valueOf($$1));
            }
            $$0.fireChannelRead($$1);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) throws Exception {
            if ($$1 instanceof InboundConfigurationTask) {
                InboundConfigurationTask $$3 = (InboundConfigurationTask)$$1;
                try {
                    $$3.run($$0);
                }
                finally {
                    ReferenceCountUtil.release((Object)$$1);
                }
                $$2.setSuccess();
            } else {
                $$0.write($$1, $$2);
            }
        }
    }
}

