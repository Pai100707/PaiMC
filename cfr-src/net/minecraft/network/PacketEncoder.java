/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.SkipPacketEncoderException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketEncoder<T extends PacketListener>
extends MessageToByteEncoder<Packet<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProtocolInfo<T> protocolInfo;

    public PacketEncoder(ProtocolInfo<T> $$0) {
        this.protocolInfo = $$0;
    }

    protected void encode(ChannelHandlerContext $$0, Packet<T> $$1, ByteBuf $$2) throws Exception {
        PacketType<Packet<T>> $$3 = $$1.type();
        try {
            this.protocolInfo.codec().encode($$2, $$1);
            int $$4 = $$2.readableBytes();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(Connection.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", new Object[]{this.protocolInfo.id().id(), $$3, $$1.getClass().getName(), $$4});
            }
            JvmProfiler.INSTANCE.onPacketSent(this.protocolInfo.id(), $$3, $$0.channel().remoteAddress(), $$4);
        }
        catch (Throwable $$5) {
            LOGGER.error("Error sending packet {}", $$3, (Object)$$5);
            if ($$1.isSkippable()) {
                throw new SkipPacketEncoderException($$5);
            }
            throw $$5;
        }
        finally {
            ProtocolSwapHandler.handleOutboundTerminalPacket($$0, $$1);
        }
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (Packet)object, byteBuf);
    }
}

