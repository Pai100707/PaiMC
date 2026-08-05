/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.CorruptedFrameException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.VarInt;
import org.jspecify.annotations.Nullable;

public class Varint21FrameDecoder
extends ByteToMessageDecoder {
    private static final int MAX_VARINT21_BYTES = 3;
    private final ByteBuf helperBuf = Unpooled.directBuffer((int)3);
    private final @Nullable BandwidthDebugMonitor monitor;

    public Varint21FrameDecoder(@Nullable BandwidthDebugMonitor $$0) {
        this.monitor = $$0;
    }

    protected void handlerRemoved0(ChannelHandlerContext $$0) {
        this.helperBuf.release();
    }

    private static boolean copyVarint(ByteBuf $$0, ByteBuf $$1) {
        for (int $$2 = 0; $$2 < 3; ++$$2) {
            if (!$$0.isReadable()) {
                return false;
            }
            byte $$3 = $$0.readByte();
            $$1.writeByte((int)$$3);
            if (VarInt.hasContinuationBit($$3)) continue;
            return true;
        }
        throw new CorruptedFrameException("length wider than 21-bit");
    }

    protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) {
        $$1.markReaderIndex();
        this.helperBuf.clear();
        if (!Varint21FrameDecoder.copyVarint($$1, this.helperBuf)) {
            $$1.resetReaderIndex();
            return;
        }
        int $$3 = VarInt.read(this.helperBuf);
        if ($$3 == 0) {
            throw new CorruptedFrameException("Frame length cannot be zero");
        }
        if ($$1.readableBytes() < $$3) {
            $$1.resetReaderIndex();
            return;
        }
        if (this.monitor != null) {
            this.monitor.onReceive($$3 + VarInt.getByteSize($$3));
        }
        $$2.add($$1.readBytes($$3));
    }
}

