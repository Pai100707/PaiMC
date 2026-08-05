/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder<T extends PacketListener>
extends ByteToMessageDecoder
implements ProtocolSwapHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ProtocolInfo<T> protocolInfo;

    public PacketDecoder(ProtocolInfo<T> $$0) {
        this.protocolInfo = $$0;
    }

    /*
     * WARNING - void declaration
     */
    protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) throws Exception {
        void $$6;
        int $$3 = $$1.readableBytes();
        try {
            Packet $$4 = (Packet)this.protocolInfo.codec().decode($$1);
        }
        catch (Exception $$5) {
            if ($$5 instanceof SkipPacketException) {
                $$1.skipBytes($$1.readableBytes());
            }
            throw $$5;
        }
        PacketType $$7 = $$6.type();
        JvmProfiler.INSTANCE.onPacketReceived(this.protocolInfo.id(), $$7, $$0.channel().remoteAddress(), $$3);
        if ($$1.readableBytes() > 0) {
            throw new IOException("Packet " + this.protocolInfo.id().id() + "/" + String.valueOf($$7) + " (" + $$6.getClass().getSimpleName() + ") was larger than I expected, found " + $$1.readableBytes() + " bytes extra whilst reading packet " + String.valueOf($$7));
        }
        $$2.add($$6);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {} -> {} bytes", new Object[]{this.protocolInfo.id().id(), $$7, $$6.getClass().getName(), $$3});
        }
        ProtocolSwapHandler.handleInboundTerminalPacket($$0, $$6);
    }
}

